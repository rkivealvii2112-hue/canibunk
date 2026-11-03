@echo off
REM ==============================================================
REM  BUNKiT - Build & Run Script (Windows CMD)
REM ==============================================================

chcp 65001 > nul

REM Detect JDK path if not yet refreshed in current terminal session
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)
if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin" (
    set "PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin;%PATH%"
)

echo [BUNKiT] Compiling Java source files...
if not exist bin mkdir bin

javac -encoding UTF-8 -cp "lib/*" -d bin src\com\canibunk\model\*.java src\com\canibunk\ds\*.java src\com\canibunk\service\*.java src\com\canibunk\scraper\*.java src\com\canibunk\*.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %errorlevel%
)

echo [BUNKiT] Compilation successful!
echo.
echo Launching BUNKiT CLI Application...
echo.
java -cp "bin;lib/*" com.canibunk.Main
pause
