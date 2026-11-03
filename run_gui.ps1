# ==============================================================
#  BUNKiT - Run Swing GUI Script (PowerShell)
# ==============================================================

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding  = [System.Text.Encoding]::UTF8

# Detect Adoptium / JAVA_HOME JDK path if needed
if (Test-Path "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin") {
    $env:PATH = "C:\Program Files\Eclipse Adoptium\jdk-21.0.13.9-hotspot\bin;" + $env:PATH
} elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin")) {
    $env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
}

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[BUNKiT] Compiling Java files..." -ForegroundColor Cyan
$sources = (Get-ChildItem -Recurse -Filter "*.java" -Path "src").FullName
javac -encoding UTF-8 -cp "lib/*" -d bin $sources

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "[BUNKiT] Launching BUNKiT Swing GUI..." -ForegroundColor Green
java -cp "bin;lib/*" com.canibunk.CanIBunkGUI

