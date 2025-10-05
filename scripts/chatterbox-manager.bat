@echo off
REM Chatterbox TTS Manager
REM Main menu script for managing the Chatterbox TTS API container

:main_menu
cls
echo ====================================
echo    Chatterbox TTS Manager
echo ====================================
echo.
echo Current Status:
set CONTAINER_NAME=chatterbox-tts-api
docker ps --filter "name=%CONTAINER_NAME%" --format "{{.Names}}" | findstr %CONTAINER_NAME% >nul
if errorlevel 1 (
    echo ❌ Container: STOPPED
) else (
    echo ✅ Container: RUNNING
    curl -s http://localhost:4123/health >nul 2>&1
    if not errorlevel 1 (
        echo ✅ Service: RESPONDING
    ) else (
        echo ⚠️  Service: INITIALIZING
    )
)

echo.
echo ====================================
echo Available Operations:
echo ====================================
echo 1. Setup Chatterbox TTS (first time)
echo 2. Start Container
echo 3. Stop Container
echo 4. View Status
echo 5. View Logs
echo 6. Test API
echo 7. Maintenance Menu
echo 8. Quick Test Speech
echo 9. Exit
echo.
set /p choice="Select option (1-9): "

if "%choice%"=="1" goto :setup
if "%choice%"=="2" goto :start
if "%choice%"=="3" goto :stop
if "%choice%"=="4" goto :status
if "%choice%"=="5" goto :logs
if "%choice%"=="6" goto :test
if "%choice%"=="7" goto :maintenance
if "%choice%"=="8" goto :quick_test
if "%choice%"=="9" goto :exit
echo Invalid choice. Please try again.
pause
goto :main_menu

:setup
echo.
echo Running setup...
call "%~dp0chatterbox-setup.bat"
goto :main_menu

:start
echo.
echo Starting container...
call "%~dp0chatterbox-start.bat"
goto :main_menu

:stop
echo.
echo Stopping container...
call "%~dp0chatterbox-stop.bat"
goto :main_menu

:status
echo.
echo Checking status...
call "%~dp0chatterbox-status.bat"
goto :main_menu

:logs
echo.
echo Opening logs...
call "%~dp0chatterbox-logs.bat"
goto :main_menu

:test
echo.
echo Running tests...
call "%~dp0chatterbox-test.bat"
goto :main_menu

:maintenance
echo.
echo Opening maintenance menu...
call "%~dp0chatterbox-maintenance.bat"
goto :main_menu

:quick_test
echo.
echo ====================================
echo Quick Speech Test
echo ====================================
set /p test_text="Enter text to convert to speech: "
if "%test_text%"=="" set test_text=Hello from Chatterbox TTS!

echo.
echo Generating speech for: "%test_text%"
set timestamp=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set timestamp=%timestamp: =0%
set output_file=quick-test-%timestamp%.wav

curl -X POST http://localhost:4123/v1/audio/speech ^
  -H "Content-Type: application/json" ^
  -d "{\"input\": \"%test_text%\"}" ^
  --output %output_file% ^
  -s

if exist %output_file% (
    echo ✅ Speech generated: %output_file%
    echo Opening audio file...
    start %output_file%
) else (
    echo ❌ Failed to generate speech
    echo Make sure the container is running and healthy
)
echo.
pause
goto :main_menu

:exit
echo.
echo Goodbye!
exit /b 0
