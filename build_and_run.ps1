# ==============================================================
#  CanIBunk.com - Phase 1 & 2 Build & Run Script (PowerShell)
# ==============================================================

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

Write-Host "[CanIBunk] Compiling Java source files..." -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$sources = (Get-ChildItem -Recurse -Filter "*.java" -Path "src").FullName

javac -encoding UTF-8 -cp "lib/*" -d bin $sources

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "[CanIBunk] Compilation successful!" -ForegroundColor Green

if ($args.Count -gt 0) {
    java -cp "bin;lib/*" com.canibunk.Main $args
} else {
    java -cp "bin;lib/*" com.canibunk.Main
}

