@echo off
echo Deploying DotsStudio to Tomcat...

set SRC_DIR=%CD%
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot"
set CATALINA_HOME=C:\Tomcat10
set APP_NAME=dott

echo Stopping Tomcat...
call "%CATALINA_HOME%\bin\shutdown.bat"
timeout /t 5 /nobreak

echo Cleaning up...
if exist "%CATALINA_HOME%\work\Catalina\localhost\%APP_NAME%" rmdir /s /q "%CATALINA_HOME%\work\Catalina\localhost\%APP_NAME%"
if exist "%CATALINA_HOME%\webapps\%APP_NAME%" rmdir /s /q "%CATALINA_HOME%\webapps\%APP_NAME%"

echo Copying files...
mkdir "%CATALINA_HOME%\webapps\%APP_NAME%"
xcopy "%SRC_DIR%\WebContent\*" "%CATALINA_HOME%\webapps\%APP_NAME%\" /E /Y /EXCLUDE:WebContent\deploy_exclude.txt

echo Starting Tomcat...
call "%CATALINA_HOME%\bin\startup.bat"

echo DEPLOYMENT COMPLETE!
