# Manual Shepherd Setup (Windows)

This guide covers manual setup of OWASP Security Shepherd on Windows without Docker. For the standard Docker-based setup, see the [Docker Environment Setup](https://github.com/OWASP/SecurityShepherd/wiki/Docker-Environment-Setup) guide.

**Tested Environment:**
- Windows 11
- Java 8 (Amazon Corretto)
- Maven 3.9.9
- MariaDB 12.1
- Apache Tomcat 9.0

**Estimated Time:** 30-40 minutes

---

# Before Setting up Shepherd

You need to have the following things installed and setup locally before setting up Shepherd:

1. **Java Development Kit (JDK) 8** - Required for building and running Shepherd
2. **Apache Maven** - Build tool for compiling the project
3. **Apache Tomcat 9.0** - Web application server (Tomcat 8.5+ supported, but avoid Tomcat 11+ as it requires Java 11)
4. **MariaDB 10.6+** - Database server (MySQL 5.7+ also supported)

---

## 1. Install Java Development Kit (JDK) 8

Security Shepherd is built with Java 8. While newer Java versions exist, Shepherd requires Java 8 for compatibility.

**Download:** [Amazon Corretto 8](https://docs.aws.amazon.com/corretto/latest/corretto-8-ug/downloads-list.html) (free, production-ready OpenJDK distribution)

**Installation:**
1. Download the Windows x64 MSI installer
2. Run the installer (default location: `C:\Program Files\Amazon Corretto\jdk1.8.0_xxx`)
3. The installer should automatically add Java to your PATH

**Verify Installation:**
```powershell
java -version
```

Expected output: `openjdk version "1.8.0_xxx"`

**Set JAVA_HOME:**
```powershell
# Set permanently (requires admin PowerShell)
[System.Environment]::SetEnvironmentVariable('JAVA_HOME', 'C:\Program Files\Amazon Corretto\jdk1.8.0_482', 'Machine')

# Or set for current session only
$env:JAVA_HOME = "C:\Program Files\Amazon Corretto\jdk1.8.0_482"
```

---

## 2. Install Apache Maven

Maven is the build tool used to compile Security Shepherd from source.

**Download:** [Apache Maven 3.9.x](https://maven.apache.org/download.cgi)

**Installation:**
1. Download the binary zip archive (e.g., `apache-maven-3.9.9-bin.zip`)
2. Extract to a permanent location (e.g., `C:\Program Files\apache-maven-3.9.9`)
3. Add Maven's `bin` directory to your PATH:

```powershell
# Add to PATH permanently (requires admin PowerShell)
$currentPath = [System.Environment]::GetEnvironmentVariable('Path', 'Machine')
[System.Environment]::SetEnvironmentVariable('Path', "$currentPath;C:\Program Files\apache-maven-3.9.9\bin", 'Machine')
```

**Verify Installation:**
```powershell
mvn -version
```

Expected output showing Maven version and Java version.

---

## 3. Install Apache Tomcat 9

Tomcat is the web application server that runs Security Shepherd.

**Important:** Use Tomcat 9.0.x (compatible with Java 8). Tomcat 10+ requires Java 11 or higher.

**Download:** [Apache Tomcat 9.0](https://tomcat.apache.org/download-90.cgi)

**Installation:**
1. Download the Windows Service Installer (`.exe`)
2. Run the installer:
   - Installation directory: `C:\Program Files\Apache Software Foundation\Tomcat 9.0`
   - HTTP Port: `8080` (or your preferred port)
   - Install as Windows service
   - Service startup: Manual (you'll start it when needed)

**Important Configuration:**

Ensure the Tomcat user can write to `$CATALINA_HOME\conf` directory (required for the database setup wizard):

```powershell
# Grant write permissions to the conf directory
$tomcatConf = "C:\Program Files\Apache Software Foundation\Tomcat 9.0\conf"
$acl = Get-Acl $tomcatConf
$permission = "Users","Modify","Allow"
$accessRule = New-Object System.Security.AccessControl.FileSystemAccessRule $permission
$acl.SetAccessRule($accessRule)
Set-Acl $tomcatConf $acl
```

**Set CATALINA_HOME:**
```powershell
[System.Environment]::SetEnvironmentVariable('CATALINA_HOME', 'C:\Program Files\Apache Software Foundation\Tomcat 9.0', 'Machine')
```

---

## 4. Install MariaDB Server

MariaDB is the database server for Security Shepherd (MySQL 5.7+ also works).

**Download:** [MariaDB 10.6+](https://mariadb.org/download/)

**Installation:**
1. Download the Windows MSI installer
2. Install with these settings:
   - Enable networking
   - Port: `3306` (default)
   - Set a root password (remember this for later)
   - Install as Windows service
   - Start service automatically

**Verify Installation:**
```powershell
# Check service status
Get-Service MariaDB

# Test connection
mysql -u root -p
```

---

# The Shepherd Setup

Now that all prerequisites are installed, follow these steps to set up Security Shepherd:

---

## Part 2: Database Configuration

### 2.1 Create Security Shepherd Databases

**Steps:**

1. Connect to MariaDB:
   ```powershell
   mysql -u root -pCowSaysMoo
   ```

2. Create databases:
   ```sql
   -- Main application database
   CREATE DATABASE core;
   
   -- SQL Injection lesson database
   CREATE DATABASE SqlInjLesson;
   
   -- Verify creation
   SHOW DATABASES;
   ```

3. Exit MySQL:
   ```sql
   EXIT;
   ```

---

### 2.2 Create Database Users

Security Shepherd creates users automatically during setup, but you can pre-create them:

```sql
-- Connect as root
mysql -u root -pCowSaysMoo

-- Create shepherd3 user (main application)
CREATE USER 'shepherd3'@'localhost' IDENTIFIED BY 'CowSaysMoo';
GRANT ALL PRIVILEGES ON core.* TO 'shepherd3'@'localhost';
GRANT ALL PRIVILEGES ON SqlInjLesson.* TO 'shepherd3'@'localhost';

-- Create firstBloodyMessL user (lesson challenges)
CREATE USER 'firstBloodyMessL'@'localhost' IDENTIFIED BY 'FirstBloodySomePassword';
GRANT SELECT ON core.* TO 'firstBloodyMessL'@'localhost';

-- Apply changes
FLUSH PRIVILEGES;

-- Verify users
SELECT User, Host FROM mysql.user WHERE User LIKE '%shepherd%' OR User LIKE '%firstBlood%';

EXIT;
```

---

## Part 3: Build Security Shepherd

### 3.1 Clone Repository

```powershell
# Navigate to your workspace
cd C:\Users\YourUsername

# Clone the repository
git clone https://github.com/OWASP/SecurityShepherd.git SecurityShepherd-dev

# Or if you already have it:
cd C:\Users\YourUsername\SecurityShepherd-dev
```

---

### 3.2 Configure Build Environment

**Ensure environment variables are set:**

```powershell
# Verify JAVA_HOME
$env:JAVA_HOME
# Should show: C:\Program Files\Amazon Corretto\jdk1.8.0_482

# If not set, set it for current session:
$env:JAVA_HOME = "C:\Program Files\Amazon Corretto\jdk1.8.0_482"
$env:Path = "C:\Program Files\Amazon Corretto\jdk1.8.0_482\bin;$env:Path"

# Verify Maven
mvn -version
# Should show: Apache Maven 3.9.9, Java version: 1.8.0_482
```

---

### 3.3 Build the WAR File

```powershell
# Navigate to project directory
cd C:\Users\YourUsername\SecurityShepherd-dev

# Clean and build (skip tests for faster build)
mvn clean package -DskipTests

# Build output:
# - Duration: ~15-20 seconds
# - Output location: target/owaspSecurityShepherd.war
# - File size: ~22.3 MB
```

**Expected Output:**
```
[INFO] Building owaspSecurityShepherd 3.1.0-SNAPSHOT
[INFO] Compiling 168 source files
[INFO] Building war: C:\Users\...\target\owaspSecurityShepherd.war
[INFO] BUILD SUCCESS
```

**Troubleshooting:**

If build fails with "Failed to delete target":
```powershell
# Stop any running Java processes
Get-Process java | Stop-Process -Force

# Then rebuild
mvn clean package -DskipTests
```

---

## Part 4: Deploy to Tomcat

### 4.1 Deploy WAR File

```powershell
# Stop Tomcat if running
Get-Process | Where-Object { $_.ProcessName -eq "java" -and $_.Path -like "*Tomcat*" } | Stop-Process -Force

# Clean previous deployment
$tomcat = "C:\Program Files\Apache Software Foundation\Tomcat 9.0"
Remove-Item "$tomcat\webapps\owaspSecurityShepherd*" -Recurse -Force -ErrorAction SilentlyContinue

# Copy new WAR file
Copy-Item "C:\Users\YourUsername\SecurityShepherd-dev\target\owaspSecurityShepherd.war" `
          -Destination "$tomcat\webapps\"

# Verify copy
Get-Item "$tomcat\webapps\owaspSecurityShepherd.war" | Select-Object Name, Length
```

---

### 4.2 Start Tomcat

```powershell
# Set environment variables
$env:JAVA_HOME = "C:\Program Files\Amazon Corretto\jdk1.8.0_482"
$env:CATALINA_HOME = "C:\Program Files\Apache Software Foundation\Tomcat 9.0"

# Start Tomcat
& "$env:CATALINA_HOME\bin\startup.bat"

# Wait for deployment (usually 30-60 seconds)
Start-Sleep -Seconds 45

# Verify deployment
Test-Path "$env:CATALINA_HOME\webapps\owaspSecurityShepherd\WEB-INF\web.xml"
# Should return: True
```

**Check Tomcat is running:**
```powershell
# Check process
Get-Process | Where-Object { $_.ProcessName -match "java" -and $_.Path -like "*Tomcat*" }

# Check port 8080
netstat -ano | Select-String ":8080"
# Should show: LISTENING
```

---

## Part 5: Initial Setup

### 5.1 Access Setup Wizard

1. Open browser and navigate to:
   ```
   http://localhost:8080/owaspSecurityShepherd/
   ```

2. You should see: "No Database Configuration" setup page

---

### 5.2 Retrieve Authentication Token

Security Shepherd generates a one-time setup token on first startup.

```powershell
# Read the authentication token
Get-Content "C:\Program Files\Apache Software Foundation\Tomcat 9.0\conf\SecurityShepherd.auth"

# Example output: 666ba403-fa40-4976-8ddf-bbbce09ee570
```

**Copy this token** - you'll need it for setup.

---

### 5.3 Complete Setup Form

Fill in the setup wizard with these values:

| Field | Value | Notes |
|-------|-------|-------|
| **Hostname** | localhost | MariaDB server address |
| **Port** | 3306 | Default MariaDB port |
| **DB Username** | root | Database administrator |
| **DB Password** | CowSaysMoo | (or your chosen password) |
| **Override Databases** | Fresh Database | Select from dropdown |
| **Authentication Token** | [token from file] | Paste token from SecurityShepherd.auth |
| **Enable NoSQL Challenge** | ☐ Unchecked | (Optional: requires MongoDB) |
| **Enable Unsafe Challenges** | ☐ Unchecked | (Optional: enables risky challenges) |

Click **Submit**.

---

### 5.4 Wait for Database Initialization

The setup process will:
1. Test database connection
2. Create core schema tables
3. Create module schema tables
4. Insert default data
5. Create admin user

**Duration:** 10-30 seconds

**Success message:**
```
Database Configuration Successful
Click here to proceed to login
```

---

## Part 6: First Login

### 6.1 Default Credentials

**Default admin credentials:**
- Username: `admin`
- Password: `password`

**After first login**, you will be prompted to update the password.

**You're Done!** 🎉

---

# Verification

After successful setup and login, verify the installation:

1. **Dashboard loads** - You should see the Security Shepherd home page
2. **Modules available** - Navigate to lessons/challenges menu
3. **Test a lesson** - Try opening and completing a simple challenge
4. **Admin panel** - Access admin features for managing users/classes

---

# Optional: MongoDB Setup for NoSQL Challenges

By default, Security Shepherd's NoSQL challenges are disabled. To enable them:

1. **Install MongoDB** (4.4+ recommended)
2. **Enable NoSQL during setup** - Check the "Enable NoSQL Challenge" option in the setup wizard
3. **Restart the setup** if you need to enable it later:
   ```powershell
   # Remove database configuration
   Remove-Item "$env:CATALINA_HOME\conf\database.properties" -Force
   
   # Restart Tomcat to run setup wizard again
   ```

---

# Troubleshooting

## Build Issues

### Maven Build Fails

**Problem:** Compilation errors or dependency issues

**Solution:**
```powershell
# Clear Maven cache and force update
mvn clean install -DskipTests -U
```

---

## Deployment Issues

### 404 Error - Application Not Found

**Problem:** Browser shows "HTTP Status 404" when accessing Shepherd

**Causes & Solutions:**

1. **WAR file didn't deploy:**
   ```powershell
   # Check if application extracted
   Test-Path "$env:CATALINA_HOME\webapps\owaspSecurityShepherd\WEB-INF\web.xml"
   ```

2. **Wrong URL:**
   - If deployed as `owaspSecurityShepherd.war`: Use `http://localhost:8080/owaspSecurityShepherd/`
   - If deployed as `ROOT.war`: Use `http://localhost:8080/`

3. **Tomcat didn't start:**
   ```powershell
   # Check Tomcat logs
   Get-Content "$env:CATALINA_HOME\logs\catalina.$(Get-Date -Format 'yyyy-MM-dd').log" -Tail 50
   ```

---

### Tomcat Won't Start - Port Already in Use

**Problem:** Error message about port 8080 already in use

**Solution:**
```powershell
# Find what's using port 8080
netstat -ano | findstr :8080

# Stop the conflicting process
Stop-Process -Id <PID> -Force
```

---

## Database Setup Issues

### Setup Wizard Shows 500 Error

**Problem:** Setup page crashes when submitting configuration

**Common Causes:**

1. **Database not running:**
   ```powershell
   Get-Service MariaDB
   Start-Service MariaDB
   ```

2. **Wrong credentials:** Double-check your database username/password

3. **Invalid auth token:** Get the token again:
   ```powershell
   Get-Content "$env:CATALINA_HOME\conf\SecurityShepherd.auth"
   ```

---

### "Table doesn't exist" Error After Setup

**Problem:** Application shows database errors after setup completed

**Cause:** Partial setup - database.properties exists but tables weren't created

**Solution:**
```powershell
# Remove database configuration
Remove-Item "$env:CATALINA_HOME\conf\database.properties" -Force

# Restart Tomcat and run setup wizard again
& "$env:CATALINA_HOME\bin\shutdown.bat"
& "$env:CATALINA_HOME\bin\startup.bat"
```

---

### Can't Login with Default Credentials

**Problem:** admin/password doesn't work after setup

**Verification:**
```sql
mysql -u root -p core
SELECT userName FROM users WHERE userName = 'admin';
```

**If admin user doesn't exist:**
```powershell
# Database initialization failed - redo setup
mysql -u root -p -e "DROP DATABASE IF EXISTS core; DROP DATABASE IF EXISTS SqlInjLesson;"
Remove-Item "$env:CATALINA_HOME\conf\database.properties" -Force
# Restart Tomcat and complete setup wizard again
```

---

## Advanced: Stopping and Restarting

### Stop Security Shepherd
```powershell
# Stop Tomcat gracefully
& "C:\Program Files\Apache Software Foundation\Tomcat 9.0\bin\shutdown.bat"

# Or force stop
Get-Process | Where-Object { $_.ProcessName -eq "java" -and $_.Path -like "*Tomcat*" } | Stop-Process -Force
```

### Start Security Shepherd
` Maintenance & Operations

## Starting and Stopping Shepherd

### Start Tomcat/Shepherd:
```powershell
$env:CATALINA_HOME = "C:\Program Files\Apache Software Foundation\Tomcat 9.0"
& "$env:CATALINA_HOME\bin\startup.bat"
```

### Stop Tomcat/Shepherd:
```powershell
& "$env:CATALINA_HOME\bin\shutdown.bat"
```

### Check if Running:
```powershell
# Check Tomcat process
Get-Process | Where-Object { $_.ProcessName -match "java" }

# Check port 8080
netstat -ano | Select-String ":8080"
```

---

## Viewing Logs

**Tomcat startup/shutdown logs:**
```powershell
Get-Content "$env:CATALINA_HOME\logs\catalina.$(Get-Date -Format 'yyyy-MM-dd').log" -Tail 50
```

**Application logs:**
```powershell
Get-Content "$env:CATALINA_HOME\logs\localhost.$(Get-Date -Format 'yyyy-MM-dd').log" -Tail 50
```

---

## Redeploy After Code Changes

If you make code changes and need to redeploy:

```powershell
# 1. Stop Tomcat
& "$env:CATALINA_HOME\bin\shutdown.bat"
Start-Sleep -Seconds 5

# 2. Rebuild the project
cd SecurityShepherd
mvn clean package -DskipTests

# 3. Remove old deployment
Remove-Item "$env:CATALINA_HOME\webapps\owaspSecurityShepherd*" -Recurse -Force -ErrorAction SilentlyContinue

# 4. Copy new WAR
Copy-Item "target\owaspSecurityShepherd.war" -Destination "$env:CATALINA_HOME\webapps\"

# 5. Start Tomcat
& "$env:CATALINA_HOME\bin\startup.bat"
```

---

## Database Operations

**Connect to database:**
```powershell
mysql -u root -p core
```

**Common queries:**
```sql
-- List all users
SELECT userName, userRole FROM users;

-- Check all tables
SHOW TABLES;

-- View challenge completion stats
SELECT COUNT(*) FROM challenges WHERE completed = 1;
```

---

# Additional Resources

- **Main Repository:** [github.com/OWASP/SecurityShepherd](https://github.com/OWASP/SecurityShepherd)
- **OWASP Project Page:** [owasp.org/www-project-security-shepherd](https://owasp.org/www-project-security-shepherd/)
- **Wiki Documentation:** [github.com/OWASP/SecurityShepherd/wiki](https://github.com/OWASP/SecurityShepherd/wiki)
- **Docker Setup (Alternative):** [Docker Environment Setup](https://github.com/OWASP/SecurityShepherd/wiki/Docker-Environment-Setup)
- **Contributing Guide:** [CONTRIBUTING.md](https://github.com/OWASP/SecurityShepherd/blob/master/CONTRIBUTING.md)

---

# Known Issues & Fixes

## Windows-Specific Bug Fixes Applied

This guide includes fixes for 6 critical bugs that prevent setup on Windows with spaces in the installation path (e.g., "Program Files"). These fixes are documented in `COMMIT-NOTES.md` and include:

1. URL encoding path issues (5 instances)
2. Broken conditional logic for hostname/port validation
3. Missing null-safety checks for request parameters
4. Uninitialized database option variables
5. Incompatible MySQL Connector version with MariaDB 12.1
6. Obsolete JDBC driver class name

If you encounter setup issues on Windows, ensure your version includes these fixes or apply them manually from the commit notes.