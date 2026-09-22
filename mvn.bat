@echo off
where mvn.cmd >nul 2>nul
if errorlevel 1 (
    echo Maven is not installed or is not on PATH. Install Maven 3.9+ first.
    exit /b 1
)
call mvn.cmd %*
exit /b %errorlevel%
