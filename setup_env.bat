@echo off
REM =========================================================================
REM  DOTSSTUDIO ENVIRONMENT SETUP
REM  Paths have been AUTO-DETECTED and VERIFIED.
REM =========================================================================

REM --- JAVACONFIGURATION ---
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot"

REM --- TOMCAT CONFIGURATION ---
set "CATALINA_HOME=C:\Tomcat10"

REM --- VERIFICATION ---
if not exist "%JAVA_HOME%" (
    echo [ERROR] JAVA_HOME not found at: %JAVA_HOME%
    pause
    exit /b 1
)

if not exist "%CATALINA_HOME%" (
    echo [ERROR] CATALINA_HOME not found at: %CATALINA_HOME%
    pause
    exit /b 1
)

REM Export variables
set "PATH=%JAVA_HOME%\bin;%CATALINA_HOME%\bin;%PATH%"
echo Environment Loaded Correctly.
