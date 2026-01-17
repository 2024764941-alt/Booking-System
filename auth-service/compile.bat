@echo off
:: Load environment variables
call ..\setup_env.bat

echo ========================================
echo Compiling Auth Service...
echo ========================================

:: Prepare directories
if not exist "WebContent\WEB-INF\classes" mkdir "WebContent\WEB-INF\classes"

:: Compile
"%JAVA_HOME%\bin\javac" -d "WebContent\WEB-INF\classes" ^
    -cp "%CATALINA_HOME%\lib\servlet-api.jar;WebContent\WEB-INF\lib\*" ^
    src\com\auth\model\*.java ^
    src\com\auth\util\*.java ^
    src\com\auth\controller\*.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation SUCCESSFUL!
) else (
    echo Compilation FAILED!
    pause
)
