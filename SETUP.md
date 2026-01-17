# Booking System - Setup Guide

## Prerequisites

### 1. Java Development Kit (JDK)
- **Required**: JDK 17 or higher
- **Current Configuration**: JDK 25 (Eclipse Adoptium)
- **Location**: `C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot`

### 2. Apache Tomcat
- **Required**: Tomcat 10.0 or higher
- **Current Configuration**: Tomcat 10.0
- **Location**: `C:\Tomcat10`

### 3. Oracle Database
- **Required**: Oracle Database 19c, 21c, or 23c Free Edition
- **Current Configuration**: Oracle Database Free (localhost:1521:FREE)
- **Database User**: `dott_app`
- **Password**: `password123` (configured in `DBConnection.java`)

### 4. Oracle JDBC Driver
- **Required**: `ojdbc11.jar` (version 23.2.0.0 or higher)
- **Status**: ✅ Already included in `WEB-INF/lib/ojdbc11.jar`
- **Auto-Downloaded From**: Maven Central Repository

> **Note**: The JDBC driver has been automatically downloaded and configured for you. No manual action needed.

## Database Setup

### 1. Create Database User and Schema

Connect to Oracle Database as SYSDBA and run:

```sql
-- Create user
CREATE USER dott_app IDENTIFIED BY password123;

-- Grant privileges
GRANT CONNECT, RESOURCE TO dott_app;
GRANT CREATE SESSION TO dott_app;
GRANT CREATE TABLE TO dott_app;
GRANT UNLIMITED TABLESPACE TO dott_app;
```

### 2. Initialize Database Schema

Run the schema creation script:

```bash
sqlplus dott_app/password123@localhost:1521/FREE @database_schema.sql
```

Or use the provided test class:
```bash
cd "c:\Users\ptrah\OneDrive\Desktop\booking system"
javac -d bin -cp "WEB-INF\lib\*" src\com\dottstudio\util\DatabaseSetupNormalized.java
java -cp "WEB-INF\lib\*;bin" com.dottstudio.util.DatabaseSetupNormalized
```

## Building the Application

### Compile Java Sources

```bash
cd "c:\Users\ptrah\OneDrive\Desktop\booking system"
.\compile_auto.bat
```

This will compile all Java source files into `WEB-INF\classes`.

### Deploy to Tomcat

```bash
cd "c:\Users\ptrah\OneDrive\Desktop\booking system"
.\deploy_auto.bat
```

This will copy the application files to Tomcat's `webapps\booking-system` directory.

## Running the Application

### Start Tomcat Server

```bash
cd "c:\Users\ptrah\OneDrive\Desktop\booking system"
.\run_server.bat
```

### Access the Application

Open your browser and navigate to:
- **Home Page**: http://localhost:8080/booking-system/index.jsp
- **Login Page**: http://localhost:8080/booking-system/login.jsp
- **Register Page**: http://localhost:8080/booking-system/register.jsp

## Testing

### Test Database Connection

```bash
cd "c:\Users\ptrah\OneDrive\Desktop\booking system"
javac -d bin -cp "WEB-INF\lib\*" TestConnection.java
java -cp "WEB-INF\lib\*;bin" TestConnection
```

Expected output: "Connection successful!"

### Test Customer Login (Standalone)

```bash
cd "c:\Users\ptrah\OneDrive\Desktop\booking system"
javac -d bin -cp "WEB-INF\lib\*;bin" src\com\dottstudio\test\TestCustomerLogin.java
java -cp "WEB-INF\lib\*;bin" com.dottstudio.test.TestCustomerLogin
```

## Configuration

### Database Connection

Edit `src\com\dottstudio\util\DBConnection.java` to change database connection settings:

```java
private static final String URL = "jdbc:oracle:thin:@localhost:1521:FREE";
private static final String USERNAME = "dott_app";
private static final String PASSWORD = "password123";
```

### Tomcat Settings

If your Tomcat installation is in a different location, update the batch scripts:
- `compile_auto.bat` - Update `CATALINA_HOME` variable
- `deploy_auto.bat` - Update deployment path
- `run_server.bat` - Update Tomcat bin path

## Troubleshooting

### ClassNotFoundException: oracle.jdbc.OracleDriver

**Cause**: Oracle JDBC driver is missing from classpath.

**Solution**: This should already be resolved - verify `WEB-INF/lib/ojdbc11.jar` exists (6.9 MB).

### ORA-12541: Cannot connect. No listener

**Cause**: Oracle Database is not running or listener is down.

**Solution**: 
1. Check Oracle services are running
2. Verify connection string in `DBConnection.java`
3. Test with: `lsnrctl status`

### Login Returns "System Error"

**Cause**: Database connection failure or missing JDBC driver.

**Solution**:
1. Verify `ojdbc11.jar` is in `WEB-INF/lib`
2. Check database credentials in `DBConnection.java`
3. Run `TestConnection.java` to verify database connectivity

### Compilation Errors in Eclipse

**Cause**: JDBC driver not in Eclipse classpath.

**Solution**: 
1. Refresh project (F5)
2. Verify `.classpath` includes `WEB-INF/lib/ojdbc11.jar`
3. Clean and rebuild project

## Project Structure

```
booking system/
├── WEB-INF/
│   ├── classes/          # Compiled Java classes
│   ├── lib/              # External JAR dependencies
│   │   └── ojdbc11.jar   # Oracle JDBC driver
│   └── web.xml           # Deployment descriptor
├── src/
│   └── com/dottstudio/
│       ├── controller/   # Servlets
│       ├── model/        # Domain models
│       ├── util/         # Utilities (DAO, DB connection)
│       └── test/         # Test classes
├── *.jsp                 # JSP pages
├── style.css             # Stylesheets
├── script.js             # JavaScript
└── *.bat                 # Build and deployment scripts
```

## Support

For issues or questions, refer to:
- Database schema: `database_schema.sql`
- Eclipse setup: `eclipse_setup_guide.md`
- How to open in Eclipse: `How_To_Open_In_Eclipse.md`
