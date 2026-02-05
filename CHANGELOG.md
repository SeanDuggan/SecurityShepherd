# Changelog

All notable changes to the Security Shepherd project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] - 2026-02-05

### Added
- Created `StartShepherd.ps1` - PowerShell script for automated Windows startup
  - Auto-detects and starts MySQL/MariaDB service
  - Auto-detects and starts MongoDB service
  - Initializes database schemas automatically
  - Deploys WAR file to Tomcat
  - Opens webapp in browser with default credentials

### Changed

#### Dependency Updates (Security & Compatibility)
- **MySQL Connector: 5.1.49 → 8.0.33**
  - Migrated to `com.mysql:mysql-connector-j` (new Maven coordinates)
  - Fixes: MySQL Connector 5.x is EOL and has known security vulnerabilities
  
- **Commons-IO: 2.11.0 → 2.18.0**
  - Contains bug fixes and performance improvements
  
- **MongoDB Java Driver: 3.12.11 → 3.12.14**
  - Latest stable release in 3.x series with bug fixes
  
- **javax.mail → com.sun.mail 1.4.7 → 1.6.2**
  - Updated to actively maintained version
  - Migrated from deprecated `javax.mail:mail` to `com.sun.mail:javax.mail`
  
- **OneLogin SAML: 2.5.0 → 2.9.0**
  - Security fixes and improvements
  - Breaking change: Removed `XMLEntityException` (no longer thrown)
  
- **Mockito: 4.11.0 → 5.14.2**
  - Major version upgrade, backward compatible
  - Enhanced mocking capabilities for tests
  
- **Spring Framework: 5.3.39 → 5.3.31**
  - Updated all Spring modules: spring-web, spring-test, spring-core, spring-context
  - Latest stable release in 5.x series
  
- **Maven Compiler Plugin: 3.10.1 → 3.13.0**
  - Improved Java compilation performance
  
- **Maven Resources Plugin: 3.1.0 → 3.3.1**
  - Better resource handling and filtering

#### Build Configuration
- **Fixed Maven resource filtering for i18n files** ([pom.xml](pom.xml#L446-L460))
  - Excluded `i18n/**/*.properties` files from variable substitution filtering
  - Resolves: `MalformedInputException` when copying internationalization files
  - All i18n property files now copy without encoding issues

#### Code Changes
- **Updated Logout servlet for SAML 2.9.0 compatibility** ([Logout.java](src/main/java/servlets/Logout.java))
  - Removed catch block for `XMLEntityException` (no longer thrown in OneLogin SAML 2.9.0)
  - Removed import statement for deprecated exception class
  - Fixed try-catch structure for SAML logout flow

### Fixed
- Resolved Gaelic properties file encoding issue preventing build
- Fixed compilation error in SAML logout functionality after library upgrade
- Corrected Maven resource filtering causing international character corruption

### Build & Deployment
- Project now builds successfully with: `mvn clean package -DskipTests`
- WAR file generated: `target/owaspSecurityShepherd.war`
- All 168 Java source files compile without errors
- All dependency conflicts resolved

### Security
- Eliminated usage of EOL MySQL Connector 5.x with known CVEs
- Updated to latest stable versions of all security-related dependencies
- All dependencies now on supported versions with active security patches

### Documentation
- Added comprehensive Windows local setup documentation
- Created automated startup script with database initialization
- Updated deployment procedures for non-Docker environments

---

## Previous Releases

See [GitHub Releases](https://github.com/OWASP/SecurityShepherd/releases) for earlier version history.
