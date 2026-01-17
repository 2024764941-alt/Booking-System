@echo off
echo Building WAR file with source...

call compile_auto.bat
if %errorlevel% neq 0 (
    echo Compilation failed.
    exit /b 1
)

set "JDK_BIN=C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot\bin"
if exist dist_temp rmdir /s /q dist_temp
mkdir dist_temp

echo Copying Web Content...
xcopy WebContent dist_temp\ /E /I /Q >nul

echo Copying Source Code...
if not exist dist_temp\WEB-INF\src mkdir dist_temp\WEB-INF\src
xcopy src dist_temp\WEB-INF\src /E /I /Q >nul

echo Packaging WAR...
"%JDK_BIN%\jar.exe" -cvf booking_system.war -C dist_temp .

echo Cleaning up...
rmdir /s /q dist_temp

echo WAR file created: booking_system.war
