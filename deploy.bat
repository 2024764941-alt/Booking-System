@echo off
echo Deploying DotsStudio to Tomcat...

REM --- CONFIGURATION ---
set SRC_DIR=%CD%
call setup_env.bat

set APP_NAME=dott
REM ---------------------

if not exist "%CATALINA_HOME%\webapps" (
    echo ERROR: Tomcat webapps folder not found at %CATALINA_HOME%\webapps
    echo check your Tomcat installation.
    pause
    exit /b 1
)

echo Source: %SRC_DIR%
echo Target: %CATALINA_HOME%\webapps\%APP_NAME%

REM Stop Tomcat (Creates a clean state)
echo Stopping Tomcat...
call "%CATALINA_HOME%\bin\shutdown.bat"
timeout /t 5

REM Additional Cleanup (Work/Temp)
echo Cleaning up Tomcat work and temp directories...
if exist "%CATALINA_HOME%\work\Catalina\localhost\%APP_NAME%" (
    echo Deleting work directory...
    rmdir /s /q "%CATALINA_HOME%\work\Catalina\localhost\%APP_NAME%"
)
if exist "%CATALINA_HOME%\temp" (
    echo Cleaning temp directory...
    del /q "%CATALINA_HOME%\temp\*"
)

REM Cleaner deployment: Remove old folder if exists
if exist "%CATALINA_HOME%\webapps\%APP_NAME%" (
    echo Removing old deployment...
    rmdir /s /q "%CATALINA_HOME%\webapps\%APP_NAME%"
)

REM Create directory
mkdir "%CATALINA_HOME%\webapps\%APP_NAME%"

REM Copy files
echo Copying files...
xcopy "%SRC_DIR%\WebContent\*" "%CATALINA_HOME%\webapps\%APP_NAME%\" /E /Y /EXCLUDE:WebContent\deploy_exclude.txt

REM Start Tomcat
echo Starting Tomcat...
call "%CATALINA_HOME%\bin\startup.bat"

echo.
echo ========================================================
echo   DEPLOYMENT COMPLETE!
echo   New URL: http://localhost:8080/%APP_NAME%/index.jsp
echo ========================================================
pause
