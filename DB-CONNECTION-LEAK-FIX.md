# Database Connection Leak Fix - Issue #536

## Problem Analysis

Security Shepherd has a critical database connection leak issue where database connections are not properly closed in several scenarios. When automated tools (like ZAP fuzzer, directory traversal, or spiders) send hundreds of requests, these unclosed connections accumulate and eventually exhaust all available MySQL/MariaDB connections, causing the server to crash.

### Root Causes Identified:

1. **Early returns without closing connections** in `Getter.java` `authUser()` method
2. **No connection cleanup** in `SqlInjectionLesson.java` `getSqlInjectionResult()` method
3. **No try-with-resources** or proper finally blocks to ensure connections are always closed

### Specific Code Issues:

#### Issue 1: `Getter.java` - Line ~189
```java
if (suspendedUntil.after(currentTime)) {
  // User is suspended
  result = null;
  return result;  // ❌ Connection NOT closed!
}
```

#### Issue 2: `Getter.java` - Line ~228
```java
// TODO: will this close the db connection if we return here?
return result;  // ❌ Connection NOT closed!
```

#### Issue 3: `SqlInjectionLesson.java` - Lines 133-153
```java
public static String[][] getSqlInjectionResult(String ApplicationRoot, String username) {
  String[][] result = new String[10][3];
  try {
    Connection conn = Database.getSqlInjLessonConnection(ApplicationRoot);
    Statement stmt;
    stmt = conn.createStatement();
    ResultSet resultSet = stmt.executeQuery("SELECT * FROM tb_users WHERE username = '" + username + "'");
    // ... processing ...
  } catch (SQLException e) {
    // ...
  } catch (Exception e) {
    // ...
  }
  return result;  // ❌ Connection NEVER closed!
}
```

---

## Solution Options

### Option 1: Quick Fix - Reduce MySQL Timeout (Short-term)

**Effort:** 5 minutes  
**Impact:** Immediate relief  
**Downside:** Workaround, not a real fix

Edit MariaDB/MySQL configuration:

```bash
# For Docker deployment, edit docker/mariadb/Dockerfile or mount config
# For manual installation, edit /etc/mysql/mariadb.conf.d/50-server.cnf

[mysqld]
wait_timeout=10          # Kill idle connections after 10 seconds (default: 28800)
interactive_timeout=10   # Same for interactive connections
max_connections=500      # Increase if needed (default: 151)
```

**When to use:** 
- Emergency mitigation during active incidents
- Temporary fix while preparing proper code fix
- Already confirmed working by multiple users in issue #536

---

### Option 2: Proper Code Fix - Close Connections (Recommended)

**Effort:** 1-2 hours  
**Impact:** Permanent fix  
**Downside:** Requires code changes and testing

Fix the connection leaks by ensuring all connections are properly closed using try-with-resources.

#### Implementation Steps:

1. **Fix `Getter.java` authUser() method**
2. **Fix `SqlInjectionLesson.java` getSqlInjectionResult() method**
3. **Audit all other database access methods**
4. **Add unit tests to verify connections are closed**

---

## Detailed Implementation

### Fix 1: Getter.java - authUser() Method

**File:** `src/main/java/dbProcs/Getter.java`

Replace the current implementation (lines 72-241) with try-with-resources:

```java
public static String[] authUser(String ApplicationRoot, String userName, String password) {
  String[] result = null;
  log.debug("$$$ Getter.authUser $$$");
  log.debug("userName = " + userName);

  boolean userFound = false;
  boolean userVerified = false;

  // Use try-with-resources to ensure connection is ALWAYS closed
  try (Connection conn = Database.getCoreConnection(ApplicationRoot);
       CallableStatement callstmt = conn.prepareCall(
           "SELECT userId, userName, userPass, userRole, badLoginCount, tempPassword, classId,"
               + " suspendedUntil, loginType, tempUsername FROM `users` WHERE userName = ?")) {
    
    callstmt.setString(1, userName);
    
    try (ResultSet userResult = callstmt.executeQuery()) {
      
      if (userResult.next()) {
        log.debug("User Found");
        userFound = true;
      } else {
        log.debug("User did not exist");
        return null;  // ✅ Connection closed automatically
      }

      if (userFound) {
        // Authenticate User
        Argon2 argon2 = Argon2Factory.create();
        log.debug("Getting password hash");
        String dbHash = userResult.getString(3);
        log.debug("Verifying hash");
        userVerified = argon2.verify(dbHash, password.toCharArray());

        if (userVerified) {
          log.debug("Hash matches");
          result = new String[6];
          int badLoginCount;
          String loginType;
          Timestamp suspendedUntil;

          result[0] = userResult.getString(1);
          result[1] = userResult.getString(2);
          result[2] = userResult.getString(4);
          badLoginCount = userResult.getInt(5);
          result[3] = Boolean.toString(userResult.getBoolean(6));
          result[4] = userResult.getString(7);
          suspendedUntil = userResult.getTimestamp(8);
          loginType = userResult.getString(9);
          result[5] = Boolean.toString(userResult.getBoolean(10));

          if (!loginType.equals("login")) {
            log.debug("User is SSO user, can't login with password!");
            return null;  // ✅ Connection closed automatically
          }

          Timestamp currentTime = new Timestamp(System.currentTimeMillis());
          if (suspendedUntil.after(currentTime)) {
            log.debug("User is suspended");
            return null;  // ✅ Connection closed automatically (FIXED!)
          }

          if (!result[1].equalsIgnoreCase(userName)) {
            log.fatal("User Name used (" + userName + ") and User Name retrieved ("
                + result[1] + ") were not the Same. Nulling Result");
            return null;  // ✅ Connection closed automatically
          }

          log.debug("User '" + userName + "' has logged in");
          
          // Clear bad login history if needed
          if (badLoginCount > 0) {
            log.debug("Clearing Bad Login History");
            try (CallableStatement resetStmt = conn.prepareCall("call userBadLoginReset(?)")) {
              resetStmt.setString(1, result[0]);
              resetStmt.execute();
            }
            log.debug("userBadLoginReset executed!");
          }
          
          return result;  // ✅ Connection closed automatically (FIXED!)
        } else {
          log.debug("Hash did not match, authentication failed");
        }
      }
    }
  } catch (SQLException e) {
    log.fatal("Database error in authUser: " + e.toString());
    throw new RuntimeException(e);
  }

  log.debug("$$$ End authUser $$$");
  return result;  // ✅ Connection closed automatically
}
```

**Key Changes:**
- Wrapped `Connection`, `CallableStatement`, and `ResultSet` in try-with-resources
- All early returns now automatically close the connection
- Removed manual `Database.closeConnection(conn)` call (handled by try-with-resources)
- Fixed the suspended user case that was leaking connections

---

### Fix 2: SqlInjectionLesson.java - getSqlInjectionResult() Method

**File:** `src/main/java/servlets/module/lesson/SqlInjectionLesson.java`

Replace the current implementation (lines 133-153) with try-with-resources:

```java
public static String[][] getSqlInjectionResult(String ApplicationRoot, String username) {
  String[][] result = new String[10][3];
  
  // Use try-with-resources to ensure connection is ALWAYS closed
  try (Connection conn = Database.getSqlInjLessonConnection(ApplicationRoot);
       Statement stmt = conn.createStatement();
       ResultSet resultSet = stmt.executeQuery(
           "SELECT * FROM tb_users WHERE username = '" + username + "'")) {
    
    log.debug("Opening Result Set from query");
    for (int i = 0; resultSet.next() && i < 10; i++) {
      log.debug("Row " + i + ": User ID = " + resultSet.getString(1));
      result[i][0] = Encode.forHtml(resultSet.getString(1));
      result[i][1] = Encode.forHtml(resultSet.getString(2));
      result[i][2] = Encode.forHtml(resultSet.getString(3));
    }
    log.debug("That's All");
    
  } catch (SQLException e) {
    log.debug("SQL Error caught - " + e.toString());
    result[0][0] = "error";
    result[0][1] = Encode.forHtml(e.toString());
  } catch (Exception e) {
    log.fatal("Error: " + e.toString());
  }
  
  return result;  // ✅ Connection closed automatically (FIXED!)
}
```

**Key Changes:**
- Wrapped `Connection`, `Statement`, and `ResultSet` in try-with-resources
- Connection is now guaranteed to close even if exceptions occur
- Simplified the for loop slightly

---

### Fix 3: Audit Other Files (Additional Work Needed)

Based on the codebase pattern, you should audit these files for similar issues:

```bash
# Find all files that get database connections
grep -r "Database.get.*Connection" src/main/java/

# Common patterns to look for:
# 1. Connection conn = Database.getConnection(...)
# 2. Early returns without closing connections
# 3. No try-with-resources or finally blocks
```

**Files likely affected:**
- `src/main/java/dbProcs/Setter.java`
- `src/main/java/dbProcs/Getter.java` (other methods)
- Any servlet in `src/main/java/servlets/module/`

---

## Testing Plan

### 1. Unit Tests

Create a test to verify connections are being closed:

```java
@Test
public void testConnectionsAreClosed() throws Exception {
  // Get initial connection count
  int initialConnections = getActiveConnectionCount();
  
  // Perform 100 authentication attempts
  for (int i = 0; i < 100; i++) {
    Getter.authUser(applicationRoot, "testuser", "wrongpassword");
  }
  
  // Wait for connections to close
  Thread.sleep(2000);
  
  // Verify connection count hasn't grown significantly
  int finalConnections = getActiveConnectionCount();
  assertTrue("Connection leak detected", 
             finalConnections < initialConnections + 5);
}

private int getActiveConnectionCount() throws SQLException {
  try (Connection conn = Database.getCoreConnection(applicationRoot);
       Statement stmt = conn.createStatement();
       ResultSet rs = stmt.executeQuery("SHOW PROCESSLIST")) {
    int count = 0;
    while (rs.next()) {
      count++;
    }
    return count;
  }
}
```

### 2. Load Testing with ZAP

1. Deploy the fixed version
2. Configure ZAP to proxy through Security Shepherd
3. Run fuzzer on a lesson page
4. Monitor database connections:

```sql
-- Run this query while fuzzer is active
SHOW PROCESSLIST;

-- Count active connections
SELECT COUNT(*) FROM information_schema.processlist 
WHERE user = 'shepherd3';
```

**Expected Results:**
- Connection count should remain low (< 10-20)
- No connections in "Sleep" state for extended periods
- No server crashes after extended fuzzing

### 3. Monitor in Production

Add monitoring to track connection pool usage:

```bash
# Watch connection count
watch -n 1 'mysql -u root -p -e "SHOW PROCESSLIST" | grep shepherd | wc -l'

# Log connection statistics
mysql -u root -p -e "SHOW STATUS LIKE 'Threads_connected';"
```

---

## Deployment Steps

### For Docker Deployment:

1. **Apply code fixes** to the source files
2. **Rebuild the application:**
   ```bash
   mvn -Pdocker clean install -DskipTests
   ```
3. **Rebuild Docker image:**
   ```bash
   docker-compose build web
   ```
4. **Stop and restart:**
   ```bash
   docker-compose stop web
   docker-compose up -d web
   ```
5. **Verify connections:**
   ```bash
   docker exec secshep_mariadb mysql -u root -pCowSaysMoo -e "SHOW PROCESSLIST"
   ```

### For Manual Tomcat Deployment:

1. **Apply code fixes**
2. **Rebuild WAR file:**
   ```bash
   mvn clean package -DskipTests
   ```
3. **Stop Tomcat:**
   ```bash
   sudo systemctl stop tomcat
   ```
4. **Deploy new WAR:**
   ```bash
   sudo cp target/owaspSecurityShepherd.war /var/lib/tomcat/webapps/
   ```
5. **Start Tomcat:**
   ```bash
   sudo systemctl start tomcat
   ```

---

## Verification Checklist

After deploying the fix:

- [ ] Application starts successfully
- [ ] Users can log in
- [ ] Lessons work correctly
- [ ] Database connection count stays low during normal use
- [ ] No connection leaks after 100+ requests
- [ ] Server remains stable during ZAP fuzzing
- [ ] No "Too many connections" errors in logs

---

## Rollback Plan

If issues occur after deployment:

```bash
# Docker deployment
git checkout HEAD~1  # Revert to previous commit
mvn -Pdocker clean install -DskipTests
docker-compose build web
docker-compose up -d web

# OR use quick fix while investigating
# Add to docker/mariadb/my.cnf:
[mysqld]
wait_timeout=10
interactive_timeout=10
```

---

## Estimated Impact

**Before Fix:**
- ❌ 30+ idle connections after 1 minute of fuzzing
- ❌ Server crash after 100+ connections exhausted
- ❌ Hours-long outages requiring manual intervention

**After Fix:**
- ✅ 2-5 active connections during normal use
- ✅ Stable under automated tool attacks
- ✅ No manual intervention needed

---

## Additional Improvements (Future Work)

1. **Implement Connection Pooling**
   - Use HikariCP or Apache DBCP2
   - Configure pool size, timeout, validation
   - Much more efficient than one-connection-per-request

2. **Add Connection Leak Detection**
   - Log warnings for connections open > 30 seconds
   - Automatically close leaked connections
   - Alert admins when leaks detected

3. **Rate Limiting**
   - Limit requests per IP address
   - Throttle automated tools
   - Protect against DoS attacks

4. **Database Health Monitoring**
   - CloudWatch alarms for high connection count
   - Prometheus metrics export
   - Automatic scaling when needed

---

## References

- **Original Issue:** https://github.com/OWASP/SecurityShepherd/issues/536
- **Related Issue:** https://github.com/OWASP/SecurityShepherd/issues/535
- **MySQL wait_timeout:** https://dev.mysql.com/doc/refman/5.5/en/server-system-variables.html#sysvar_wait_timeout
- **Try-with-resources:** https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html

---

## Support

If you encounter issues implementing this fix:

1. **Review the code changes carefully** - Ensure try-with-resources syntax is correct
2. **Check the logs** - Look for SQLException or RuntimeException
3. **Test incrementally** - Fix one file at a time and test
4. **Monitor connections** - Use `SHOW PROCESSLIST` to verify improvement

**Questions?** Open a discussion in the Security Shepherd repository or comment on issue #536.
