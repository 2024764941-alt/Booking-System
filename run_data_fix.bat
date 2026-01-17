@echo off
echo Adding missing data...

set PGPASSWORD=postgres
"C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -d booking_db -f "database/add_missing_data.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS! Data added.
) else (
    echo.
    echo ERROR! Failed to add data.
)
pause
