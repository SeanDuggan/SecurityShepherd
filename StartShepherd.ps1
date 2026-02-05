# Security Shepherd - Quick Start Script
# Run this after installing MariaDB

param(
    [switch]$SkipDBCheck,
    [switch]$SkipMongoDB
)

Write-Host "`n=== Security Shepherd Startup Script ===" -ForegroundColor Cyan
Write-Host "This script will start all required services and the webapp`n" -ForegroundColor White

# Configuration
$TOMCAT_HOME = "C:\Program Files\Apache Software Foundation\Tomcat 9.0"
$WAR_FILE = "target\owaspSecurityShepherd.war"

# Step 1: Check MariaDB/MySQL
Write-Host "[1/5] Checking MySQL/MariaDB..." -ForegroundColor Yellow
$mysqlService = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue | Select-Object -First 1

if ($null -eq $mysqlService -and -not $SkipDBCheck) {
    Write-Host "  ❌ MySQL/MariaDB not found!" -ForegroundColor Red
    Write-Host "`n  Please install MariaDB first:" -ForegroundColor White
    Write-Host "  1. Download: https://mariadb.org/download/" -ForegroundColor Cyan
    Write-Host "  2. During install, set root password to: CowSaysMoo" -ForegroundColor Cyan
    Write-Host "  3. Run this script again`n" -ForegroundColor Cyan
    exit 1
}

if ($mysqlService) {
    Write-Host "  ✅ Found: $($mysqlService.DisplayName)" -ForegroundColor Green
    
    if ($mysqlService.Status -ne 'Running') {
        Write-Host "  Starting MySQL service..." -ForegroundColor Yellow
        try {
            Start-Service $mysqlService.Name
            Write-Host "  ✅ MySQL service started" -ForegroundColor Green
        } catch {
            Write-Host "  ⚠️  Could not start MySQL automatically" -ForegroundColor Yellow
            Write-Host "  Please start it manually: Start-Service $($mysqlService.Name)" -ForegroundColor White
        }
    } else {
        Write-Host "  ✅ MySQL service is running" -ForegroundColor Green
    }
}

# Step 2: Check MongoDB
Write-Host "`n[2/5] Checking MongoDB..." -ForegroundColor Yellow
$mongoService = Get-Service -Name "MongoDB" -ErrorAction SilentlyContinue

if ($null -eq $mongoService) {
    Write-Host "  ⚠️  MongoDB service not found" -ForegroundColor Yellow
    if (-not $SkipMongoDB) {
        Write-Host "  MongoDB is required for some challenges" -ForegroundColor White
        Write-Host "  Continue anyway? (Y/N): " -NoNewline -ForegroundColor White
        $response = Read-Host
        if ($response -ne 'Y' -and $response -ne 'y') {
            exit 1
        }
    }
} else {
    Write-Host "  ✅ Found: MongoDB" -ForegroundColor Green
    
    if ($mongoService.Status -ne 'Running') {
        Write-Host "  Starting MongoDB service..." -ForegroundColor Yellow
        try {
            Start-Service MongoDB
            Start-Sleep -Seconds 2
            Write-Host "  ✅ MongoDB service started" -ForegroundColor Green
        } catch {
            Write-Host "  ⚠️  Could not start MongoDB" -ForegroundColor Yellow
            Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
            Write-Host "  Some MongoDB challenges may not work" -ForegroundColor White
        }
    } else {
        Write-Host "  ✅ MongoDB service is running" -ForegroundColor Green
    }
}

# Step 3: Initialize Databases
Write-Host "`n[3/5] Checking database schema..." -ForegroundColor Yellow

# Check if we can connect to MySQL
$mysqlAvailable = $false
if ($mysqlService -and $mysqlService.Status -eq 'Running') {
    # Try to find mysql client
    $mysqlPath = $null
    $possiblePaths = @(
        "C:\Program Files\MariaDB*\bin\mysql.exe",
        "C:\Program Files (x86)\MariaDB*\bin\mysql.exe",
        "C:\MySQL\bin\mysql.exe",
        "C:\xampp\mysql\bin\mysql.exe"
    )
    
    foreach ($path in $possiblePaths) {
        $found = Get-Item $path -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            $mysqlPath = $found.FullName
            break
        }
    }
    
    if ($mysqlPath) {
        Write-Host "  ✅ MySQL client found: $mysqlPath" -ForegroundColor Green
        Write-Host "`n  To initialize the database, run these commands:" -ForegroundColor Cyan
        Write-Host "  `"$mysqlPath`" -u root -pCowSaysMoo < src\main\resources\database\coreSchema.sql" -ForegroundColor White
        Write-Host "  `"$mysqlPath`" -u root -pCowSaysMoo < src\main\resources\database\moduleSchemas.sql" -ForegroundColor White
        Write-Host "`n  Run initialization now? (Y/N): " -NoNewline -ForegroundColor Yellow
        $response = Read-Host
        
        if ($response -eq 'Y' -or $response -eq 'y') {
            Write-Host "  Initializing database..." -ForegroundColor Yellow
            
            # Execute SQL files
            $coreSchema = "src\main\resources\database\coreSchema.sql"
            $moduleSchema = "src\main\resources\database\moduleSchemas.sql"
            
            if (Test-Path $coreSchema) {
                & $mysqlPath -u root -pCowSaysMoo < $coreSchema 2>&1 | Out-Null
                if ($LASTEXITCODE -eq 0) {
                    Write-Host "  ✅ Core schema initialized" -ForegroundColor Green
                } else {
                    Write-Host "  ⚠️  Core schema may already exist or had errors" -ForegroundColor Yellow
                }
            }
            
            if (Test-Path $moduleSchema) {
                & $mysqlPath -u root -pCowSaysMoo < $moduleSchema 2>&1 | Out-Null
                if ($LASTEXITCODE -eq 0) {
                    Write-Host "  ✅ Module schemas initialized" -ForegroundColor Green
                } else {
                    Write-Host "  ⚠️  Module schemas may already exist or had errors" -ForegroundColor Yellow
                }
            }
        }
    } else {
        Write-Host "  ⚠️  MySQL client not found in PATH" -ForegroundColor Yellow
        Write-Host "  Database must be initialized manually" -ForegroundColor White
    }
}

# Step 4: Deploy WAR file
Write-Host "`n[4/5] Deploying application..." -ForegroundColor Yellow

if (Test-Path $WAR_FILE) {
    Write-Host "  ✅ WAR file found: $WAR_FILE" -ForegroundColor Green
    
    $targetPath = "$TOMCAT_HOME\webapps\owaspSecurityShepherd.war"
    
    # Check if Tomcat needs the WAR
    if (-not (Test-Path $targetPath) -or ((Get-Item $WAR_FILE).LastWriteTime -gt (Get-Item $targetPath).LastWriteTime)) {
        Write-Host "  Copying WAR to Tomcat..." -ForegroundColor Yellow
        Copy-Item $WAR_FILE $targetPath -Force
        Write-Host "  ✅ WAR deployed" -ForegroundColor Green
    } else {
        Write-Host "  ✅ WAR already deployed" -ForegroundColor Green
    }
} else {
    Write-Host "  ⚠️  WAR file not found!" -ForegroundColor Yellow
    Write-Host "  Build it with: mvn clean package -DskipTests" -ForegroundColor White
}

# Step 5: Start Tomcat
Write-Host "`n[5/5] Starting Tomcat..." -ForegroundColor Yellow

$tomcatService = Get-Service -Name "Tomcat9" -ErrorAction SilentlyContinue

if ($tomcatService) {
    if ($tomcatService.Status -ne 'Running') {
        Write-Host "  Starting Tomcat service..." -ForegroundColor Yellow
        try {
            Start-Service Tomcat9
            Write-Host "  ✅ Tomcat service started" -ForegroundColor Green
        } catch {
            Write-Host "  ❌ Could not start Tomcat service" -ForegroundColor Red
            Write-Host "  Error: $($_.Exception.Message)" -ForegroundColor Red
            exit 1
        }
    } else {
        Write-Host "  ✅ Tomcat service is already running" -ForegroundColor Green
        Write-Host "  Restarting for fresh deployment..." -ForegroundColor Yellow
        Restart-Service Tomcat9
        Write-Host "  ✅ Tomcat restarted" -ForegroundColor Green
    }
} else {
    Write-Host "  ❌ Tomcat9 service not found" -ForegroundColor Red
    exit 1
}

# Wait for Tomcat to start
Write-Host "`n  Waiting for Tomcat to start (15 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Final status
Write-Host "`n=== Startup Complete! ===" -ForegroundColor Green
Write-Host "`nSecurity Shepherd should now be accessible at:" -ForegroundColor White
Write-Host "  🌐 http://localhost:8080/owaspSecurityShepherd" -ForegroundColor Cyan
Write-Host "`nDefault credentials:" -ForegroundColor White
Write-Host "  Username: admin" -ForegroundColor Cyan
Write-Host "  Password: password" -ForegroundColor Cyan
Write-Host "`n(You will be prompted to change the password on first login)`n" -ForegroundColor Yellow

Write-Host "Opening browser..." -ForegroundColor Yellow
Start-Process "http://localhost:8080/owaspSecurityShepherd"
