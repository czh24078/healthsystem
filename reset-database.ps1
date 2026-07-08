# Reset and reinitialize the healthsys database using UTF-8 encoding
# Use this script only in development or when you want to rebuild the database.
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

$mysqlExe = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (-not (Test-Path $mysqlExe)) {
    Write-Error "MySQL client not found: $mysqlExe"
    exit 1
}

$sqlFile = Join-Path $root "sql\init_database.sql"
if (-not (Test-Path $sqlFile)) {
    Write-Error "SQL file not found: $sqlFile"
    exit 1
}

$username = "root"
$password = "@32158566Abc#"
$port = 3307

Write-Host "[1/2] Dropping existing healthsys database if present..." -ForegroundColor Cyan
& $mysqlExe --default-character-set=utf8 -u $username "-p$password" -P $port -h localhost -e "DROP DATABASE IF EXISTS healthsys; CREATE DATABASE healthsys CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to drop/create database."
    exit 1
}

Write-Host "[2/2] Importing init_database.sql with utf8mb4..." -ForegroundColor Cyan
Get-Content $sqlFile -Encoding UTF8 | & $mysqlExe --default-character-set=utf8 -u $username "-p$password" -P $port -h localhost healthsys
if ($LASTEXITCODE -ne 0) {
    Write-Error "Database import failed."
    exit 1
}

Write-Host "Database reset complete." -ForegroundColor Green
