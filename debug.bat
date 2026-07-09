@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

set LOG=debug_log.txt
echo ============================================ > "%LOG%"
echo   Health 1.0 - Debug Log >> "%LOG%"
echo   Time: %date% %time% >> "%LOG%"
echo ============================================ >> "%LOG%"
echo. >> "%LOG%"

REM =============================================
REM  [1] Environment
REM =============================================
echo [1] Environment >> "%LOG%"
echo   USERPROFILE=%USERPROFILE% >> "%LOG%"
echo   JAVA_HOME=%JAVA_HOME% >> "%LOG%"
echo   PATH first javaw: >> "%LOG%"
for /f "delims=" %%i in ('where javaw 2^>nul') do echo     %%i >> "%LOG%"
echo. >> "%LOG%"

REM =============================================
REM  [2] db.properties
REM =============================================
echo [2] db.properties >> "%LOG%"
set PROPS_FILE=health-common\src\main\resources\db.properties
if exist "%PROPS_FILE%" (
    echo   Source file: >> "%LOG%"
    type "%PROPS_FILE%" >> "%LOG%"
) else (
    echo   SOURCE NOT FOUND! >> "%LOG%"
)
if exist "health-common\target\classes\db.properties" (
    echo   Target file: >> "%LOG%"
    type "health-common\target\classes\db.properties" >> "%LOG%"
) else (
    echo   TARGET NOT FOUND! >> "%LOG%"
)
echo. >> "%LOG%"

REM =============================================
REM  [3] Find Maven
REM =============================================
echo [3] Maven Detection >> "%LOG%"
set MVN_CMD=
if exist "%USERPROFILE%\.m2\wrapper\dists\" (
    for /f "delims=" %%i in ('dir /b /s "%USERPROFILE%\.m2\wrapper\dists\mvn.cmd" 2^>nul') do (
        if not defined MVN_CMD set MVN_CMD=%%i
    )
)
if not defined MVN_CMD (
    for /f "delims=" %%i in ('where mvn.cmd 2^>nul') do set MVN_CMD=%%i
)
if defined MVN_CMD (
    echo   Found: !MVN_CMD! >> "%LOG%"
) else (
    echo   NOT FOUND! >> "%LOG%"
    echo MAVEN_NOT_FOUND >> "%LOG%"
    goto :error
)
echo. >> "%LOG%"

REM =============================================
REM  [4] Compile
REM =============================================
echo [4] Maven Compile >> "%LOG%"
call "%MVN_CMD%" compile -DskipTests -q 2>> "%LOG%"
if errorlevel 1 (
    echo   COMPILE FAILED >> "%LOG%"
    goto :error
)
echo   OK >> "%LOG%"
echo. >> "%LOG%"

REM =============================================
REM  [5] Classpath
REM =============================================
echo [5] Classpath >> "%LOG%"
set CP_FILE=%TEMP%\hc_cp_dbg_%RANDOM%.txt
call "%MVN_CMD%" -pl health-ui dependency:build-classpath -DincludeScope=runtime -Dmdep.outputFile="%CP_FILE%" -q 2>> "%LOG%"
set CP=
if exist "%CP_FILE%" (
    for /f "usebackq delims=" %%a in ("%CP_FILE%") do set CP=%%a
    echo   dependency JARs: >> "%LOG%"
    echo   !CP! >> "%LOG%"
    del "%CP_FILE%" 2>nul
) else (
    echo   Classpath file not created! >> "%LOG%"
)

set CLASSES=health-ui\target\classes;health-common\target\classes;health-dao\target\classes;health-service\target\classes
echo. >> "%LOG%"
echo   Target classes: >> "%LOG%"
echo   %CLASSES% >> "%LOG%"

REM Check if classes actually exist
for %%d in (health-ui health-common health-dao health-service) do (
    if exist "%%d\target\classes" (
        echo   %%d/target/classes: EXISTS >> "%LOG%"
    ) else (
        echo   %%d/target/classes: MISSING! >> "%LOG%"
    )
)

set RUN_CP=%CLASSES%;!CP!
echo. >> "%LOG%"
echo   Full classpath: >> "%LOG%"
echo   !RUN_CP! >> "%LOG%"

REM =============================================
REM  [6] Run diagnostic Java test
REM =============================================
echo. >> "%LOG%"
echo [6] Diagnostic Query >> "%LOG%"

REM Write a small test class inline
echo import com.healthsys.common.util.DbUtil; > DiagRun.java
echo import java.sql.*; >> DiagRun.java
echo public class DiagRun { >> DiagRun.java
echo     public static void main(String[] args) throws Exception { >> DiagRun.java
echo         java.io.InputStream in = DiagRun.class.getClassLoader().getResourceAsStream("db.properties"); >> DiagRun.java
echo         if (in == null) { System.out.println("DIAG:db.properties NOT ON CLASSPATH"); return; } >> DiagRun.java
echo         java.util.Properties p = new java.util.Properties(); >> DiagRun.java
echo         p.load(in); >> DiagRun.java
echo         System.out.println("DIAG:URL=" + p.getProperty("db.url")); >> DiagRun.java
echo         System.out.println("DIAG:USER=" + p.getProperty("db.user")); >> DiagRun.java
echo         System.out.println("DIAG:PASS_LEN=" + p.getProperty("db.password").length()); >> DiagRun.java
echo         Connection conn = DbUtil.getConnection(); >> DiagRun.java
echo         System.out.println("DIAG:CONN=" + (conn != null ? "OK" : "NULL")); >> DiagRun.java
echo         System.out.println("DIAG:DB=" + conn.getCatalog()); >> DiagRun.java
echo         PreparedStatement ps = conn.prepareStatement("SELECT username, real_name FROM admins WHERE username = ?"); >> DiagRun.java
echo         ps.setString(1, "admin"); >> DiagRun.java
echo         ResultSet rs = ps.executeQuery(); >> DiagRun.java
echo         if (rs.next()) { >> DiagRun.java
echo             System.out.println("DIAG:ADMIN_FOUND=" + rs.getString("real_name")); >> DiagRun.java
echo         } else { >> DiagRun.java
echo             System.out.println("DIAG:ADMIN_NOT_FOUND"); >> DiagRun.java
echo         } >> DiagRun.java
echo         conn.close(); >> DiagRun.java
echo     } >> DiagRun.java
echo } >> DiagRun.java

javac -cp "%RUN_CP%" DiagRun.java 2>> "%LOG%"
if exist DiagRun.class (
    java -cp ".;%RUN_CP%" DiagRun >> "%LOG%" 2>&1
    del DiagRun.class 2>nul
) else (
    echo   DiagRun compilation FAILED >> "%LOG%"
)
del DiagRun.java 2>nul

echo. >> "%LOG%"
echo [7] Launching application... >> "%LOG%"
echo   Using: java (with console for debug) >> "%LOG%"

REM Use java (not javaw) to see console errors
start "Health Check System" java -Dfile.encoding=UTF-8 -cp "%RUN_CP%" com.healthsys.ui.Launcher 2>> "%LOG%"

echo. >> "%LOG%"
echo ============================================ >> "%LOG%"
echo   Debug log saved to: %LOG% >> "%LOG%"
echo ============================================ >> "%LOG%"

echo ============================================
echo   Debug Complete!
echo   Log file: %CD%\%LOG%
echo ============================================
echo   Admin:  admin / admin123
echo   Doctor: d001 / 123456
echo   User:   13900000001 / 123456
echo ============================================
timeout /t 5 >nul
exit /b 0

:error
echo ============================================
echo   ERROR - Check debug_log.txt
echo ============================================
pause
exit /b 1
