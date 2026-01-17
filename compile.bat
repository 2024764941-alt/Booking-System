@echo off
echo Compiling Java Source Files...

call setup_env.bat

set "CLASSPATH=%CATALINA_HOME%\lib\servlet-api.jar;%CATALINA_HOME%\lib\jsp-api.jar;WebContent\WEB-INF\lib\*"

if not exist "WebContent\WEB-INF\classes" mkdir "WebContent\WEB-INF\classes"

"%JAVA_HOME%\bin\javac" --release 17 -d WebContent\WEB-INF\classes -cp "%CLASSPATH%" src\com\dottstudio\util\*.java src\com\dottstudio\controller\*.java src\com\dottstudio\model\*.java src\com\dottstudio\test\*.java

if %errorlevel% neq 0 (
    echo.
    echo ========================================
    echo COMPILATION FAILED! Check errors above.
    echo ========================================
    pause
    exit /b 1
)

echo.
echo ========================================
echo COMPILATION SUCCESSFUL!
echo Classes updated in WEB-INF/classes
echo ========================================
echo.
echo Now you can restart the server using run_server.bat
pause
