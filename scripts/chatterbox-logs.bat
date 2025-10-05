@echo off
REM Chatterbox TTS Logs Script
REM This script shows and follows the logs of the Chatterbox TTS API container

echo ====================================
echo Chatterbox TTS Container Logs
echo ====================================

set CONTAINER_NAME=chatterbox-tts-api

echo Checking if container is running...
docker ps --filter "name=%CONTAINER_NAME%" --format "table {{.Names}}\t{{.Status}}" | findstr %CONTAINER_NAME% >nul
if errorlevel 1 (
    echo Container is not running.
    echo.
    echo Showing last logs from stopped container...
    docker logs %CONTAINER_NAME% --tail 50
    echo.
    echo To start the container, run: chatterbox-start.bat
    pause
    exit /b 0
)

echo.
echo Container is running. Showing live logs...
echo Press Ctrl+C to stop following logs
echo.
echo ====================================

REM Show last 50 lines and follow new logs
docker logs %CONTAINER_NAME% --tail 50 --follow
