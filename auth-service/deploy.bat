@echo off
call ..\setup_env.bat

set APP_NAME=auth
set DEPLOY_DIR=%CATALINA_HOME%\webapps\%APP_NAME%

echo ========================================
echo Deploying Auth Service to %DEPLOY_DIR%
echo ========================================

:: Clean old deployment
if exist "%DEPLOY_DIR%" rmdir /s /q "%DEPLOY_DIR%"
if exist "%DEPLOY_DIR%.war" del "%DEPLOY_DIR%.war"
mkdir "%DEPLOY_DIR%"

:: Copy WebContent
xcopy "WebContent\*" "%DEPLOY_DIR%\" /E /Y /Q

echo.
echo ========================================================
echo  AUTH SERVICE DEPLOYED!
echo  URL: http://localhost:8081/%APP_NAME%/api/login
echo ========================================================
pause
