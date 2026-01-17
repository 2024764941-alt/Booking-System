@echo off
setlocal
cd /d "%~dp0"

echo ==========================================
echo      Auth Service Database Setup
echo ==========================================
echo.
echo This script will:
echo 1. Connect to PostgreSQL
echo 2. Create the 'auth_db' database
echo 3. Create tables (USERS, ROLES)
echo.

set "PG_PATH=C:\Program Files\PostgreSQL\18\bin"

echo Please enter your PostgreSQL 'postgres' user password.
echo (If you don't know it, try 'password' or 'admin', or just press Enter if none)
set /p PG_PWD=Password: 

set PGPASSWORD=%PG_PWD%

echo.
echo [1/2] Creating Database 'booking_db'...
"%PG_PATH%\psql.exe" -U postgres -c "CREATE DATABASE booking_db;"
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [WARNING] assert failure or DB already exists. Attempting to proceed...
)

echo.
echo [2/2] Creating Tables...
"%PG_PATH%\psql.exe" -U postgres -d booking_db -f "auth-service/setup_auth_db.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Database setup complete!
) else (
    echo.
    echo [ERROR] Failed to run SQL script. Check your password.
)

pause
