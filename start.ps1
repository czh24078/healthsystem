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
    try {
        Start-Service MySQL80 -ErrorAction Stop
        Write-Host "  [OK] MySQL start requested" -ForegroundColor Green

        # 等待服务状态并检测数据库端口（从 db.properties 可知使用 3307）
        $timeout = 30
        $elapsed = 0
        while ($elapsed -lt $timeout) {
            Start-Sleep -Seconds 1
            $elapsed++
            $mysql = Get-Service MySQL80 -ErrorAction SilentlyContinue
            $svcRunning = $mysql -and $mysql.Status -eq "Running"
            $portOpen = $false
            try {
                $portOpen = Test-NetConnection -ComputerName 'localhost' -Port 3307 -InformationLevel Quiet
            } catch { $portOpen = $false }
            if ($svcRunning -and $portOpen) {
                Write-Host "  [OK] MySQL is running and reachable" -ForegroundColor Green
                break
            }
        }
        if ($elapsed -ge $timeout) {
            Write-Host "  [WARN] MySQL not reachable after ${timeout}s" -ForegroundColor Yellow
            $ans = Read-Host "继续启动应用？(Y/N)"
            if ($ans -ne 'Y' -and $ans -ne 'y') { Write-Host 'Aborting startup'; exit 1 }
        }
    }
    catch { Write-Host "  [WARN] Cannot start MySQL (run as Admin): $_" -ForegroundColor Yellow }
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

# 3. Launch via Maven exec (ensures runtime dependencies like MySQL driver are on classpath)
Write-Host "`n[3/3] Launching via Maven exec..." -ForegroundColor Yellow
try {
    $execArgs = @(
        '-f', 'health-ui\pom.xml',
        'org.codehaus.mojo:exec-maven-plugin:3.1.0:java',
        '-Dexec.mainClass=com.healthsys.ui.Launcher',
        '-Dexec.classpathScope=runtime'
    )
    & $mvn @execArgs
    $exitCode = $LASTEXITCODE
    if ($exitCode -eq 0) {
        Write-Host "  [OK] App process exited normally (exit code 0)" -ForegroundColor Green
    } else {
        Write-Host "  [WARN] App process exited with code $exitCode" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  [ERROR] Failed to launch application: $_" -ForegroundColor Red
}
Write-Host "============================================" -ForegroundColor Cyan
