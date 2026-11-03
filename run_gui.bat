@echo off
REM ==============================================================
REM  BUNKiT - Run Swing GUI Script
REM ==============================================================

chcp 65001 > nul

REM Detect JDK path if not yet refreshed in current terminal session
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)
if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin" (
    set "PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin;%PATH%"
)

if not exist bin mkdir bin

echo [BUNKiT] Compiling Java files...
javac -encoding UTF-8 -cp "lib/*" -d bin src\com\canibunk\model\*.java src\com\canibunk\ds\*.java src\com\canibunk\service\*.java src\com\canibunk\scraper\*.java src\com\canibunk\*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %errorlevel%
)

echo [BUNKiT] Launching BUNKiT Swing GUI...
java -cp "bin;lib/*" com.canibunk.CanIBunkGUI
pause
