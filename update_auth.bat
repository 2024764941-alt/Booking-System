@echo off
echo Updating Auth Service...
cd auth-service
call compile.bat
if %ERRORLEVEL% NEQ 0 (
    echo Compile failed!
    cd ..
    pause
    exit /b 1
)
call deploy.bat
cd ..
echo.
echo ========================================
echo Auth Service Updated Successfully!
echo ========================================
pause
