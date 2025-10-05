@echo off
REM Chatterbox TTS Status Script
REM This script shows the status and health of the Chatterbox TTS API container

echo ====================================
echo Chatterbox TTS Container Status
echo ====================================

set CONTAINER_NAME=chatterbox-tts-api
set PORT=4123

echo.
echo Container Status:
echo ====================================
docker ps -a --filter "name=%CONTAINER_NAME%" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}\t{{.Image}}"

echo.
echo Checking if container is running...
docker ps --filter "name=%CONTAINER_NAME%" --format "{{.Names}}" | findstr %CONTAINER_NAME% >nul
if errorlevel 1 (
    echo ❌ Container is not running
    echo To start: chatterbox-start.bat
    goto :end
)

echo ✅ Container is running

echo.
echo Service Health Check:
echo ====================================
curl -s http://localhost:%PORT%/health 2>nul
if not errorlevel 1 (
    echo ✅ Service is responding
) else (
    echo ❌ Service is not responding
    echo The service may still be initializing...
)

echo.
echo.
echo API Endpoints:
echo ====================================
echo Health Check: http://localhost:%PORT%/health
echo Speech Synthesis: http://localhost:%PORT%/v1/audio/speech
echo API Documentation: http://localhost:%PORT%/docs
echo.

echo Resource Usage:
echo ====================================
docker stats %CONTAINER_NAME% --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}"

:end
echo.
echo For logs: chatterbox-logs.bat
echo To stop: chatterbox-stop.bat
echo.
pause
