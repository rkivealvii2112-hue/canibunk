@echo off
REM ==============================================================
REM  CanIBunk.com - Run Swing GUI Script
REM ==============================================================

chcp 65001 > nul
if not exist bin mkdir bin

echo [CanIBunk] Compiling Java files...
javac -encoding UTF-8 -cp "lib/*" -d bin src\com\canibunk\model\*.java src\com\canibunk\ds\*.java src\com\canibunk\service\*.java src\com\canibunk\scraper\*.java src\com\canibunk\*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %errorlevel%
)

echo [CanIBunk] Launching CanIBunk Swing GUI...
java -cp "bin;lib/*" com.canibunk.CanIBunkGUI
pause
