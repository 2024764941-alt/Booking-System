@echo off
echo ========================================
echo Setting up Booking Database Tables (PostgreSQL)
echo ========================================

:: Prompt for password if needed.
echo Please enter your Postgres password if prompted.

"C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d booking_db -f "database/setup_booking_full.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo DATABASE SETUP SUCCESSFUL!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo ERROR: Database setup failed.
    echo ========================================
)
pause
