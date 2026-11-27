@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

if not exist bin mkdir bin

echo Compiling Parking Lot Management System...
del sources.txt 2>nul
for /f "delims=" %%f in ('dir /s /b src\*.java') do (
    set "p=%%f"
    echo "!p:\=/!" >> sources.txt
)
javac -d bin -sourcepath src @sources.txt
set ERR=%ERRORLEVEL%
del sources.txt

if %ERR% neq 0 (
    echo Compilation failed!
    pause
    exit /b %ERR%
)

echo Running Verification Tests...
java -cp bin com.parking.test.ParkingLotTest
echo.
pause
endlocal
