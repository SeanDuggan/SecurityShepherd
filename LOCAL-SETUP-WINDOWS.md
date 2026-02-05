# Security Shepherd - Local Setup on Windows (No Docker)

Complete guide for running Security Shepherd locally on Windows without Docker.

## Prerequisites Check

You already have:
- ✅ Maven 3.9.9
- ✅ Java 17 (OpenJDK Corretto)

You need to install:
- ⬜ MariaDB or MySQL
- ⬜ MongoDB
- ⬜ Apache Tomcat 9

---

## Step 1: Install MariaDB

### Download and Install

1. **Download MariaDB:**
   - Go to: https://mariadb.org/download/
   - Version: MariaDB 10.11 (LTS)
   - Select: Windows x64
   - Download the MSI installer

2. **Run the installer:**
   - Launch the MSI file
   - Click "Next" through the wizard
   
3. **Important Configuration:**
   - When prompted for "Root Password", set it to: `CowSaysMoo` (this matches the default Security Shepherd config)
   - Check "Use UTF8 as default server's character set"
   - Check "Enable access from remote machines for 'root' user" (for localhost access)
   - Install as Windows Service: ✅ Yes
   - Service Name: `MySQL` (default)
   - Click "Install"

4. **Verify Installation:**
   Open PowerShell and run:
   ```powershell
   # Check if MariaDB is running
   Get-Service MySQL
   
   # Test connection
   mysql -u root -pCowSaysMoo -e "SELECT VERSION();"
   ```

### Create Security Shepherd Databases

```powershell
# Connect to MariaDB
mysql -u root -pCowSaysMoo

# In the MySQL prompt, run these commands:
```

```sql
-- Create the main core database
CREATE DATABASE IF NOT EXISTS core;

-- Create the SQL injection lesson database
CREATE DATABASE IF NOT EXISTS SqlInjLesson;

-- Create user and grant privileges
CREATE USER IF NOT EXISTS 'shepherd3'@'localhost' IDENTIFIED BY 'CowSaysMoo';
GRANT ALL PRIVILEGES ON core.* TO 'shepherd3'@'localhost';
GRANT ALL PRIVILEGES ON SqlInjLesson.* TO 'shepherd3'@'localhost';

-- Create user for SQL injection lessons
CREATE USER IF NOT EXISTS 'firstBloodyMessL'@'localhost' IDENTIFIED BY 'FirstBloodySomePassword';
GRANT SELECT ON SqlInjLesson.* TO 'firstBloodyMessL'@'localhost';

FLUSH PRIVILEGES;

-- Verify databases
SHOW DATABASES;

-- Exit
EXIT;
```

---

## Step 2: Install MongoDB

### Download and Install

1. **Download MongoDB:**
   - Go to: https://www.mongodb.com/try/download/community
   - Version: MongoDB 7.0.x (Community Edition)
   - Platform: Windows x64
   - Package: MSI

2. **Run the installer:**
   - Choose "Complete" installation
   - Install MongoDB as a Service: ✅ Yes
   - Service Name: `MongoDB`
   - Data Directory: `C:\Program Files\MongoDB\Server\7.0\data\`
   - Log Directory: `C:\Program Files\MongoDB\Server\7.0\log\`
   - Click "Install"

3. **Verify Installation:**
   ```powershell
   # Check if MongoDB is running
   Get-Service MongoDB
   
   # Test connection (mongosh should be in PATH)
   mongosh --eval "db.version()"
   ```

4. **Create MongoDB Databases:**
   ```powershell
   mongosh
   ```
   
   In the MongoDB shell:
   ```javascript
   // Create NoSQL injection lesson database
   use NoSqlInjection
   db.createCollection("users")
   
   // Insert sample data
   db.users.insertMany([
     { username: "admin", password: "admin123", comment: "Administrator" },
     { username: "user1", password: "password", comment: "Regular user" }
   ])
   
   // Verify
   db.users.find()
   
   // Exit
   exit
   ```

---

## Step 3: Install Apache Tomcat

### Download and Install

1. **Download Tomcat:**
   - Go to: https://tomcat.apache.org/download-90.cgi
   - Version: Tomcat 9.0.x (latest)
   - Download: "32-bit/64-bit Windows Service Installer" (.exe)

2. **Run the installer:**
   - Click "Next" through the wizard
   - Components: Select "Service Startup" and "Tomcat"
   - Tomcat Administrator Login:
     - Username: `admin`
     - Password: Choose a secure password
   - Port: `8080` (default)
   - Install location: `C:\Program Files\Apache Software Foundation\Tomcat 9.0`
   - Click "Install"

3. **Verify Installation:**
   ```powershell
   # Check if Tomcat is running
   Get-Service Tomcat9
   
   # Open browser to: http://localhost:8080
   # You should see the Tomcat welcome page
   ```

---

## Step 4: Configure Security Shepherd

### Update Configuration Files

We need to configure Security Shepherd to use your local databases.

**File:** `src/main/resources/database.properties`

Create this file if it doesn't exist:

```powershell
cd C:\Users\DuggSe01\SecurityShepherd-dev
New-Item -Path "src\main\resources\database.properties" -ItemType File -Force
```

Add this content:

```properties
# Core Database Configuration
core.jdbc.url=jdbc:mysql://localhost:3306/core?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
core.jdbc.username=shepherd3
core.jdbc.password=CowSaysMoo
core.jdbc.driver=org.mariadb.jdbc.Driver

# SQL Injection Lesson Database
sqli.jdbc.url=jdbc:mysql://localhost:3306/SqlInjLesson?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
sqli.jdbc.username=firstBloodyMessL
sqli.jdbc.password=FirstBloodySomePassword
sqli.jdbc.driver=org.mariadb.jdbc.Driver

# MongoDB Configuration
mongo.host=localhost
mongo.port=27017
mongo.db=NoSqlInjection
```

---

## Step 5: Build Security Shepherd

### Build the WAR File

```powershell
cd C:\Users\DuggSe01\SecurityShepherd-dev

# Clean previous builds
mvn clean

# Build the WAR file (this takes 10-15 minutes first time)
mvn package -DskipTests

# The WAR file will be created at:
# target\owaspSecurityShepherd.war
```

### Verify Build Success

```powershell
# Check if WAR file exists
Test-Path target\owaspSecurityShepherd.war

# Check WAR file size (should be ~50-80 MB)
(Get-Item target\owaspSecurityShepherd.war).Length / 1MB
```

---

## Step 6: Initialize Database Schema

Security Shepherd needs to initialize the database tables on first run.

### Option A: Let Application Initialize (Recommended)

The application will automatically create tables on first deployment.

### Option B: Manual Initialization (If needed)

If you want to pre-initialize the database:

```powershell
# Look for SQL scripts in:
Get-ChildItem -Path src\main\resources\database -Recurse -Filter *.sql
```

Run the initialization scripts:

```powershell
mysql -u root -pCowSaysMoo core < src\main\resources\database\coreSchema.sql
mysql -u root -pCowSaysMoo core < src\main\resources\database\moduleSchemas.sql
```

---

## Step 7: Deploy to Tomcat

### Deploy the WAR File

**Option A: Copy to Tomcat webapps (Easiest)**

```powershell
# Stop Tomcat
Stop-Service Tomcat9

# Copy WAR file to Tomcat webapps directory
Copy-Item target\owaspSecurityShepherd.war -Destination "C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\" -Force

# Start Tomcat
Start-Service Tomcat9

# Wait for deployment (30-60 seconds)
Start-Sleep -Seconds 60
```

**Option B: Use Tomcat Manager (Alternative)**

1. Open browser: `http://localhost:8080/manager/html`
2. Login with admin credentials
3. Scroll to "WAR file to deploy"
4. Click "Browse" and select `target\owaspSecurityShepherd.war`
5. Click "Deploy"

---

## Step 8: Access Security Shepherd

### Open the Application

1. **Open your browser** and navigate to:
   ```
   http://localhost:8080/owaspSecurityShepherd/
   ```

2. **Initial Login:**
   - Username: `admin`
   - Password: `password`

3. **You'll be prompted to:**
   - Change the admin password immediately
   - Complete the setup wizard

4. **Verify Everything Works:**
   - Try logging in with new password
   - Navigate to a lesson
   - Check that database connections work

---

## Troubleshooting

### Issue: "Can't connect to database"

**Check database services:**
```powershell
Get-Service MySQL, MongoDB | Format-Table -AutoSize
```

If not running:
```powershell
Start-Service MySQL
Start-Service MongoDB
```

**Test database connections:**
```powershell
# Test MariaDB
mysql -u shepherd3 -pCowSaysMoo core -e "SELECT 1;"

# Test MongoDB
mongosh --eval "db.version()"
```

### Issue: "Tomcat won't start"

**Check Tomcat logs:**
```powershell
# View latest Tomcat logs
Get-Content "C:\Program Files\Apache Software Foundation\Tomcat 9.0\logs\catalina.*.log" -Tail 50
```

**Common fixes:**
- Port 8080 already in use? Check with: `netstat -ano | findstr :8080`
- Not enough memory? Increase Tomcat memory in `bin\catalina.bat`

### Issue: "Application deployed but shows errors"

**Check application logs:**
```powershell
# Security Shepherd logs
Get-Content "C:\Program Files\Apache Software Foundation\Tomcat 9.0\logs\localhost.*.log" -Tail 50

# Application-specific logs
Get-Content "C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\owaspSecurityShepherd\WEB-INF\classes\log4j2.properties"
```

### Issue: "WAR file deployment failed"

**Verify prerequisites:**
```powershell
# Java version
java -version

# Maven version
mvn -version

# WAR file exists
Test-Path target\owaspSecurityShepherd.war
```

**Rebuild cleanly:**
```powershell
mvn clean package -DskipTests -X
```

### Issue: "Database tables not created"

**Manually create schema:**
```powershell
# Find SQL scripts
Get-ChildItem -Path src\main\resources -Recurse -Filter "*.sql"

# Run initialization scripts
mysql -u root -pCowSaysMoo core < src\main\resources\database\coreSchema.sql
```

---

## Common Commands

### Start/Stop Services

```powershell
# Start all services
Start-Service MySQL
Start-Service MongoDB
Start-Service Tomcat9

# Stop all services
Stop-Service Tomcat9
Stop-Service MongoDB
Stop-Service MySQL

# Check service status
Get-Service MySQL, MongoDB, Tomcat9 | Format-Table -AutoSize
```

### Rebuild and Redeploy

```powershell
# Stop Tomcat
Stop-Service Tomcat9

# Clean old deployment
Remove-Item "C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\owaspSecurityShepherd*" -Recurse -Force

# Rebuild
cd C:\Users\DuggSe01\SecurityShepherd-dev
mvn clean package -DskipTests

# Deploy
Copy-Item target\owaspSecurityShepherd.war -Destination "C:\Program Files\Apache Software Foundation\Tomcat 9.0\webapps\" -Force

# Start Tomcat
Start-Service Tomcat9
```

### View Logs in Real-time

```powershell
# Watch Tomcat logs
Get-Content "C:\Program Files\Apache Software Foundation\Tomcat 9.0\logs\catalina.$(Get-Date -Format yyyy-MM-dd).log" -Wait -Tail 20

# Watch application logs
Get-Content "C:\Program Files\Apache Software Foundation\Tomcat 9.0\logs\localhost.$(Get-Date -Format yyyy-MM-dd).log" -Wait -Tail 20
```

### Database Backups

```powershell
# Backup MariaDB
mysqldump -u root -pCowSaysMoo --all-databases > "C:\Backups\shepherd_backup_$(Get-Date -Format yyyyMMdd).sql"

# Backup MongoDB
mongodump --out "C:\Backups\mongodb_$(Get-Date -Format yyyyMMdd)"
```

---

## Performance Tuning

### Increase Tomcat Memory

Edit: `C:\Program Files\Apache Software Foundation\Tomcat 9.0\bin\setenv.bat`

Create if it doesn't exist:

```batch
@echo off
set CATALINA_OPTS=-Xms512m -Xmx2048m -XX:PermSize=256m -XX:MaxPermSize=512m
```

### Optimize MariaDB

Edit: `C:\Program Files\MariaDB 10.11\data\my.ini`

Add under `[mysqld]`:

```ini
[mysqld]
max_connections=200
innodb_buffer_pool_size=256M
query_cache_size=32M
```

Restart MariaDB:
```powershell
Restart-Service MySQL
```

---

## Next Steps

Once Security Shepherd is running locally:

1. ✅ Test the application thoroughly
2. ✅ Try different lessons and modules
3. ✅ Verify database connections are working
4. ✅ Check for any errors in logs
5. ✅ Test the connection leak issue (Issue #536)
6. 🔧 Apply the database connection fix
7. 🧪 Test the fix locally
8. 🚀 Deploy to AWS

---

## Uninstall / Cleanup

If you need to start fresh:

```powershell
# Stop services
Stop-Service Tomcat9, MySQL, MongoDB

# Uninstall via Windows Settings > Apps
# - Apache Tomcat
# - MariaDB
# - MongoDB

# Remove data directories
Remove-Item "C:\Program Files\Apache Software Foundation" -Recurse -Force
Remove-Item "C:\Program Files\MariaDB*" -Recurse -Force
Remove-Item "C:\Program Files\MongoDB" -Recurse -Force
```

---

## Quick Reference

**Application URL:** `http://localhost:8080/owaspSecurityShepherd/`  
**Default Login:** `admin` / `password`

**Database Credentials:**
- MariaDB Root: `root` / `CowSaysMoo`
- MariaDB App: `shepherd3` / `CowSaysMoo`
- MongoDB: No authentication (localhost only)

**Service Names:**
- Tomcat: `Tomcat9`
- MariaDB: `MySQL`
- MongoDB: `MongoDB`

**Important Paths:**
- Tomcat: `C:\Program Files\Apache Software Foundation\Tomcat 9.0\`
- WAR File: `C:\Users\DuggSe01\SecurityShepherd-dev\target\owaspSecurityShepherd.war`
- Logs: `C:\Program Files\Apache Software Foundation\Tomcat 9.0\logs\`

---

**Need Help?** Refer to:
- [Security Shepherd Wiki](https://github.com/OWASP/SecurityShepherd/wiki)
- [Tomcat Documentation](https://tomcat.apache.org/tomcat-9.0-doc/)
- [MariaDB Documentation](https://mariadb.com/kb/en/documentation/)
- [MongoDB Documentation](https://www.mongodb.com/docs/)
