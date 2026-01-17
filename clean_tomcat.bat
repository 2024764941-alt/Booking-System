@echo off
echo ========================================
echo   Updating, Compiling, Deploying DotsStudio
echo ========================================
echo.

REM --- CONFIGURATION ---
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot"
set CATALINA_HOME=C:\Tomcat10
set APP_NAME=dott

REM Use double quotes safely around the whole classpath string
set "LIBS=%CATALINA_HOME%\lib\*;WebContent\WEB-INF\lib\*"

REM --- 1. COMPILATION ---
echo Compiling Java Source Code...

if not exist "WebContent\WEB-INF\classes" mkdir "WebContent\WEB-INF\classes"

REM Compile using wildcards to avoid space-in-path issues with @sourcefiles
"%JAVA_HOME%\bin\javac" -cp "%LIBS%" -d "WebContent\WEB-INF\classes" src\com\dottstudio\model\*.java src\com\dottstudio\util\*.java src\com\dottstudio\controller\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    echo   COMPILATION FAILED!
    echo !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    echo.
    pause
    exit /b 1
)

echo Compilation Successful!
echo.

REM --- 2. CLEANUP ---
echo Stopping Tomcat...
call "%CATALINA_HOME%\bin\shutdown.bat" 2>nul
timeout /t 3 /nobreak >nul

echo Cleaning compiled JSP files...
if exist "%CATALINA_HOME%\work\Catalina\localhost\%APP_NAME%" (
    rmdir /s /q "%CATALINA_HOME%\work\Catalina\localhost\%APP_NAME%"
)

echo Cleaning temp directory...
del /q "%CATALINA_HOME%\temp\*" 2>nul

echo Cleaning old deployment...
if exist "%CATALINA_HOME%\webapps\%APP_NAME%" (
    rmdir /s /q "%CATALINA_HOME%\webapps\%APP_NAME%"
)

REM --- 3. DEPLOY ---
echo Deploying to Tomcat...
mkdir "%CATALINA_HOME%\webapps\%APP_NAME%"
xcopy "WebContent\*" "%CATALINA_HOME%\webapps\%APP_NAME%\" /E /Y /Q

echo.
echo Starting Tomcat...
call "%CATALINA_HOME%\bin\startup.bat"

echo.
echo ========================================
echo   Full Update Complete!
echo ========================================
pause
