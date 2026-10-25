@echo off
REM ==============================================================
REM  CanIBunk.com - Build & Run Script (Windows CMD)
REM ==============================================================

chcp 65001 > nul
echo [CanIBunk] Compiling Java source files...
if not exist bin mkdir bin

javac -encoding UTF-8 -cp "lib/*" -d bin src\com\canibunk\model\*.java src\com\canibunk\ds\*.java src\com\canibunk\service\*.java src\com\canibunk\scraper\*.java src\com\canibunk\*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %errorlevel%
)

echo [CanIBunk] Compilation successful!
echo.
echo Launching CanIBunk CLI Application...
echo.
java -cp "bin;lib/*" com.canibunk.Main
pause
