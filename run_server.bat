@echo off
echo Starting DotsStudio Server...

REM --- CONFIGURATION ---
call setup_env.bat
REM ---------------------

if not exist "%JAVA_HOME%" (
    echo ERROR: Java not found at %JAVA_HOME%
    pause
    exit /b 1
)

if not exist "%CATALINA_HOME%" (
    echo ERROR: Tomcat not found at %CATALINA_HOME%
    pause
    exit /b 1
)

echo Using JAVA_HOME: %JAVA_HOME%
echo Using CATALINA_HOME: %CATALINA_HOME%

call "%CATALINA_HOME%\bin\startup.bat"

if %errorlevel% neq 0 (
    echo Server failed to start!
    pause
)
