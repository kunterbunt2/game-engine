@echo off
REM Chatterbox TTS Stop Script
REM This script stops the Chatterbox TTS API container

echo ====================================
echo Stopping Chatterbox TTS Container
echo ====================================

set CHATTERBOX_DIR=E:\github\chatterbox-tts-api
set CONTAINER_NAME=chatterbox-tts-api

echo Checking if repository exists...
if not exist "%CHATTERBOX_DIR%" (
    echo ERROR: Chatterbox TTS repository not found at %CHATTERBOX_DIR%
    echo Please run chatterbox-setup.bat first
    pause
    exit /b 1
)

echo Navigating to Chatterbox directory...
cd /d "%CHATTERBOX_DIR%"

echo.
echo Checking container status...
docker ps --filter "name=%CONTAINER_NAME%" --format "table {{.Names}}\t{{.Status}}" | findstr %CONTAINER_NAME% >nul
if errorlevel 1 (
    echo Container is not running.
    goto :show_final_status
)

echo.
echo Detecting configuration and stopping container...
nvidia-smi >nul 2>&1
if errorlevel 1 (
    echo Stopping CPU configuration...
    docker compose -f docker/docker-compose.cpu.yml down
) else (
    echo Stopping GPU configuration...
    docker compose -f docker/docker-compose.uv.gpu.yml down
    if errorlevel 1 (
        echo Trying standard GPU configuration...
        docker compose -f docker/docker-compose.gpu.yml down
        if errorlevel 1 (
            echo Trying CPU configuration...
            docker compose -f docker/docker-compose.cpu.yml down
            if errorlevel 1 (
                echo WARNING: Could not stop with compose, trying direct docker stop...
                docker stop %CONTAINER_NAME%
            )
        )
    )
)

echo Container stopped successfully.

:show_final_status
echo.
echo ====================================
echo Final Status
echo ====================================
docker ps --filter "name=%CONTAINER_NAME%" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
echo Container has been stopped.
echo To start again, run: chatterbox-start.bat
echo.
pause
