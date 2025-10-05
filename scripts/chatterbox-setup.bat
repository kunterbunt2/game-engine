@echo off
REM Chatterbox TTS Setup Script
REM This script sets up the Chatterbox TTS API container

echo ====================================
echo Chatterbox TTS Container Setup
echo ====================================

set CHATTERBOX_DIR=E:\github\chatterbox-tts-api
set CONTAINER_NAME=chatterbox-tts-api
set PORT=4123

echo Checking if Chatterbox TTS repository exists...
if not exist "%CHATTERBOX_DIR%" (
    echo Repository not found. Cloning from GitHub...
    cd E:\github
    git clone https://github.com/travisvn/chatterbox-tts-api.git
    if errorlevel 1 (
        echo ERROR: Failed to clone repository
        pause
        exit /b 1
    )
    echo Repository cloned successfully.
) else (
    echo Repository already exists at %CHATTERBOX_DIR%
)

echo.
echo Navigating to Chatterbox directory...
cd /d "%CHATTERBOX_DIR%"

echo.
echo Setting up environment file...
if exist .env.example.docker (
    copy .env.example.docker .env
    echo Environment file created from Docker example.
) else if exist .env.example (
    copy .env.example .env
    echo Environment file created from example.
) else (
    echo WARNING: No environment example file found.
)

echo.
echo Checking GPU availability...
nvidia-smi >nul 2>&1
if errorlevel 1 (
    echo WARNING: NVIDIA GPU not detected or drivers not installed
    echo Will use CPU-only configuration
    set DOCKER_COMPOSE_FILE=docker/docker-compose.cpu.yml
) else (
    echo ✓ NVIDIA GPU detected - using GPU acceleration
    set DOCKER_COMPOSE_FILE=docker/docker-compose.uv.gpu.yml
)

echo.
echo Pulling latest changes...
git pull origin main

echo.
echo Building Docker image with GPU support...
echo Using configuration: %DOCKER_COMPOSE_FILE%
docker compose -f %DOCKER_COMPOSE_FILE% build
if errorlevel 1 (
    echo ERROR: Failed to build Docker image
    if "%DOCKER_COMPOSE_FILE%"=="docker/docker-compose.uv.gpu.yml" (
        echo Trying fallback to standard GPU configuration...
        docker compose -f docker/docker-compose.gpu.yml build
        if errorlevel 1 (
            echo Trying fallback to CPU configuration...
            docker compose -f docker/docker-compose.cpu.yml build
        )
    )
    if errorlevel 1 (
        echo ERROR: All build attempts failed
        pause
        exit /b 1
    )
)

echo.
echo ====================================
echo Setup completed successfully!
echo ====================================
echo.
echo GPU Acceleration: %DOCKER_COMPOSE_FILE%
echo To start the container, run: chatterbox-start.bat
echo To stop the container, run: chatterbox-stop.bat
echo To view logs, run: chatterbox-logs.bat
echo.
pause
