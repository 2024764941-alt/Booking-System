@echo off
echo Compiling Java Source Files...

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot"
set "CATALINA_HOME=C:\Tomcat10"
set "CLASSPATH=%CATALINA_HOME%\lib\servlet-api.jar;%CATALINA_HOME%\lib\jsp-api.jar;WebContent\WEB-INF\lib\*"

if not exist "WebContent\WEB-INF\classes" mkdir "WebContent\WEB-INF\classes"

"%JAVA_HOME%\bin\javac" --release 17 -d WebContent\WEB-INF\classes -cp "%CLASSPATH%" src\com\dottstudio\util\*.java src\com\dottstudio\controller\*.java src\com\dottstudio\model\*.java src\com\dottstudio\test\*.java

if %errorlevel% neq 0 (
    echo COMPILATION FAILED!
    exit /b 1
)

echo COMPILATION SUCCESSFUL!
