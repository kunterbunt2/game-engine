@echo off
REM Chatterbox TTS Start Script
REM This script starts the Chatterbox TTS API container

echo ====================================
echo Starting Chatterbox TTS Container
echo ====================================

set CHATTERBOX_DIR=E:\github\chatterbox-tts-api
set CONTAINER_NAME=chatterbox-tts-api
set PORT=4123

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
echo Checking if container is already running...
docker ps --filter "name=%CONTAINER_NAME%" --format "table {{.Names}}\t{{.Status}}" | findstr %CONTAINER_NAME% >nul
if not errorlevel 1 (
    echo Container is already running.
    goto :show_status
)

echo.
echo Detecting optimal configuration...
nvidia-smi >nul 2>&1
if errorlevel 1 (
    echo Using CPU-only configuration (no GPU detected)
    set DOCKER_COMPOSE_FILE=docker/docker-compose.cpu.yml
) else (
    echo ✓ NVIDIA GPU detected - using GPU acceleration
    set DOCKER_COMPOSE_FILE=docker/docker-compose.uv.gpu.yml
)

echo.
echo Starting container with %DOCKER_COMPOSE_FILE%...
docker compose -f %DOCKER_COMPOSE_FILE% up -d
if errorlevel 1 (
    echo ERROR: Failed to start with optimal configuration
    if "%DOCKER_COMPOSE_FILE%"=="docker/docker-compose.uv.gpu.yml" (
        echo Trying fallback to standard GPU configuration...
        docker compose -f docker/docker-compose.gpu.yml up -d
        if errorlevel 1 (
            echo Trying fallback to CPU configuration...
            docker compose -f docker/docker-compose.cpu.yml up -d
        )
    )
    if errorlevel 1 (
        echo ERROR: Failed to start container with all configurations
        pause
        exit /b 1
    )
)

echo.
echo Container started successfully!

:show_status
echo.
echo ====================================
echo Container Status
echo ====================================
docker ps --filter "name=%CONTAINER_NAME%" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
echo ====================================
echo Service Information
echo ====================================
echo Service URL: http://localhost:%PORT%
echo Health Check: http://localhost:%PORT%/health
echo API Documentation: http://localhost:%PORT%/docs
echo.
echo The service may take a few minutes to fully initialize.
echo GPU acceleration will significantly improve performance!
echo You can monitor the logs with: chatterbox-logs.bat
echo.

REM Wait a moment and test if the service is responding
echo Waiting for service to start...
timeout /t 5 /nobreak >nul

echo Testing service availability...
curl -s http://localhost:%PORT%/health >nul 2>&1
if not errorlevel 1 (
    echo ✅ Service is responding
) else (
    echo ⚠ Service may still be initializing
    echo Monitor logs for startup progress: chatterbox-logs.bat
)

echo.
pause
