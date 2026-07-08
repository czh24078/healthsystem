# Health Check System - Quick Start (PowerShell)
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Health Check System - Quick Start" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# 1. MySQL
Write-Host "`n[1/3] Checking MySQL..." -ForegroundColor Yellow
$mysql = Get-Service MySQL80 -ErrorAction SilentlyContinue
if ($mysql -and $mysql.Status -eq "Running") {
    Write-Host "  [OK] MySQL running" -ForegroundColor Green
} else {
    Write-Host "  Starting MySQL..." -ForegroundColor Gray
    try { Start-Service MySQL80 -ErrorAction Stop; Write-Host "  [OK] MySQL started" -ForegroundColor Green }
    catch { Write-Host "  [WARN] Cannot start MySQL (run as Admin)" -ForegroundColor Yellow }
}

# 2. Build + Install
Write-Host "`n[2/3] Building..." -ForegroundColor Yellow
$mvn = "$env:USERPROFILE\apache-maven-3.9.16\bin\mvn.cmd"
& $mvn install -DskipTests -q 2>&1 | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "  [ERROR] Build failed!" -ForegroundColor Red
    & $mvn install -DskipTests
    pause; exit 1
}
Write-Host "  [OK] Build success" -ForegroundColor Green

# 3. Classpath + Launch
Write-Host "`n[3/3] Launching..." -ForegroundColor Yellow
$tmpFile = "$env:TEMP\health_cp.txt"
& $mvn -pl health-ui dependency:build-classpath -DincludeScope=runtime "-Dmdep.outputFile=$tmpFile" -q 2>&1 | Out-Null

if (Test-Path $tmpFile) {
    $cp = (Get-Content $tmpFile -Raw).Trim()
    Remove-Item $tmpFile
} else {
    $cp = ""
}

$classes = "health-ui\target\classes;health-common\target\classes;health-dao\target\classes;health-service\target\classes"
$fullCp = "$classes;$cp"

Start-Process javaw -ArgumentList "-cp `"$fullCp`"", "com.healthsys.ui.Launcher"
Write-Host "  [OK] App launched!" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Cyan
