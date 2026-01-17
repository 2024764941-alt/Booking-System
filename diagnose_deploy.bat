@echo off
echo ===================================================
echo   DIAGNOSE AND REDEPLOY
echo ===================================================

echo 1. Loading Environment...
call setup_env.bat

echo 2. Stopping Tomcat...
call "%CATALINA_HOME%\bin\shutdown.bat"
timeout /t 3

echo 3. Forcing cleanup of work/temp...
if exist "%CATALINA_HOME%\work\Catalina\localhost\dott" rmdir /s /q "%CATALINA_HOME%\work\Catalina\localhost\dott"
if exist "%CATALINA_HOME%\temp" del /q "%CATALINA_HOME%\temp\*"

echo 4. Redeploying Frontend Files...
copy "WebContent\script.js" "%CATALINA_HOME%\webapps\dott\script.js" /Y
copy "WebContent\selected-dates-panel.js" "%CATALINA_HOME%\webapps\dott\selected-dates-panel.js" /Y
copy "WebContent\custom-confirm.js" "%CATALINA_HOME%\webapps\dott\custom-confirm.js" /Y
copy "WebContent\designer-dashboard.jsp" "%CATALINA_HOME%\webapps\dott\designer-dashboard.jsp" /Y
xcopy "build\classes" "%CATALINA_HOME%\webapps\dott\WEB-INF\classes" /E /Y

echo 5. Starting Tomcat...
call "%CATALINA_HOME%\bin\startup.bat"

echo ===================================================
echo   DONE! Please Refresh Browser with CTRL + SHIFT + R
echo ===================================================
pause
