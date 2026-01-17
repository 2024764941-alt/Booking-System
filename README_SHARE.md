# How to Run DotsStudio (Booking System)

This guide will help you set up the project in Eclipse with PostgreSQL.

## 1. Prerequisites
*   **Java JDK 17** or higher.
*   **Eclipse IDE for Enterprise Java and Web Developers** (latest version).
*   **Apache Tomcat 10.1** (or version compatible with Jakarta EE).
*   **PostgreSQL** (Active server).

## 2. Database Setup
1.  Open **pgAdmin** or your terminal.
2.  Create a database named `booking_db`.
    ```sql
    CREATE DATABASE booking_db;
    ```
3.  Run the installation script to create tables and data:
    *   File: `database/FULL_INSTALL.sql`
    *   Command:
        ```bash
        psql -U postgres -d booking_db -f "database/FULL_INSTALL.sql"
        ```
    *   *Note: Ensure your PostgreSQL password is 'postgres' or update `src/com/dottstudio/util/DBConnection.java` and `auth-service/src/com/auth/util/DBConnection.java`.*

## 3. Project Setup (Eclipse)
1.  **Open Eclipse**.
2.  **File** > **Open Projects from File System...**
3.  Click **Directory** and select the root folder `booking system`.
4.  It should detect:
    *   `BookingSystem` (Main App)
    *   *If `auth-service` is not detected as a separate project*, you may need to import it separately or Eclipse might treat it as a sub-folder.
    *   **Recommendation**: If `auth-service` doesn't show up as a runnable project, do **File > Open Projects...** again and select the `booking system/auth-service` folder specifically.
5.  Click **Finish**.

## 4. Dependencies
*   Ensure the `Java Build Path` includes Apache Tomcat Runtime.
*   The project uses `WebContent/WEB-INF/lib` for JARs (PostgreSQL JSBC, etc.). Ensure these are in the build path (Right-click Project > Build Path > Configure Build Path > Libraries > Classpath > Add JARs... > Select JARs from `WebContent/WEB-INF/lib`).

## 5. Running the Application
### A. Start Auth Service
1.  Use the `update_auth.bat` script (if on Windows) to compile and deploy.
2.  **OR in Eclipse**:
    *   Right-click `auth-service` project > **Run As** > **Run on Server**.
    *   Select Tomcat.
    *   Base URL: `http://localhost:8081/auth`

### B. Start Main App
1.  Right-click `BookingSystem` project > **Run As** > **Run on Server**.
2.  Select Tomcat.
3.  Base URL: `http://localhost:8081/dott`

## 6. Access
*   Go to: `http://localhost:8081/dott/index.jsp`
*   Login: `admin@dott.com` / `admin123`
