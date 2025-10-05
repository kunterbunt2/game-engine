@echo off
REM Chatterbox TTS Test Script
REM This script tests the Chatterbox TTS API functionality

echo ====================================
echo Chatterbox TTS API Test
echo ====================================

set CONTAINER_NAME=chatterbox-tts-api
set PORT=4123
set TEST_TEXT=Hello from Chatterbox TTS! This is a test message.

echo.
echo Checking if container is running...
docker ps --filter "name=%CONTAINER_NAME%" --format "{{.Names}}" | findstr %CONTAINER_NAME% >nul
if errorlevel 1 (
    echo ❌ Container is not running
    echo Please start the container first: chatterbox-start.bat
    pause
    exit /b 1
)

echo ✅ Container is running

echo.
echo Testing API endpoints...
echo ====================================

echo.
echo 1. Health Check Test:
curl -s -w "Response Code: %%{http_code}\n" http://localhost:%PORT%/health
if errorlevel 1 (
    echo ❌ Health check failed
) else (
    echo ✅ Health check passed
)

echo.
echo 2. API Documentation Test:
curl -s -w "Response Code: %%{http_code}\n" -o nul http://localhost:%PORT%/docs
if errorlevel 1 (
    echo ❌ Documentation endpoint failed
) else (
    echo ✅ Documentation endpoint accessible
)

echo.
echo 3. Speech Synthesis Test:
echo Testing with text: "%TEST_TEXT%"

set timestamp=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set timestamp=%timestamp: =0%
set output_file=test-chatterbox-%timestamp%.wav

curl -X POST http://localhost:%PORT%/v1/audio/speech ^
  -H "Content-Type: application/json" ^
  -d "{\"input\": \"%TEST_TEXT%\"}" ^
  --output %output_file% ^
  -w "Response Code: %%{http_code}\n"

if exist %output_file% (
    for %%A in (%output_file%) do set filesize=%%~zA
    if !filesize! gtr 1000 (
        echo ✅ Speech synthesis successful
        echo   File: %output_file%
        echo   Size: !filesize! bytes
    ) else (
        echo ❌ Speech synthesis failed - file too small
        del %output_file% 2>nul
    )
) else (
    echo ❌ Speech synthesis failed - no output file
)

echo.
echo 4. Java Integration Test:
echo Testing Java TTSManager integration...
cd /d "E:\github\game-engine"
mvn test -Dtest=TTSManagerTest#testChatterboxTTSProvider -q
if errorlevel 1 (
    echo ❌ Java integration test failed
    echo Check Java test logs for details
) else (
    echo ✅ Java integration test passed
)

echo.
echo ====================================
echo Test Summary
echo ====================================
echo All tests completed.
echo Generated files are in the current directory.
echo.
echo API Documentation: http://localhost:%PORT%/docs
echo.
pause
