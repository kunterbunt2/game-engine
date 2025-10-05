@echo off
REM Chatterbox TTS Maintenance Script
REM This script performs maintenance tasks for the Chatterbox TTS API container

echo ====================================
echo Chatterbox TTS Container Maintenance
echo ====================================

set CHATTERBOX_DIR=E:\github\chatterbox-tts-api
set CONTAINER_NAME=chatterbox-tts-api

echo.
echo Select maintenance operation:
echo 1. Update repository and rebuild container
echo 2. Clean up old containers and images
echo 3. Reset container (stop, remove, rebuild)
echo 4. View container resource usage
echo 5. Export container logs
echo 6. Exit
echo.
set /p choice="Enter your choice (1-6): "

if "%choice%"=="1" goto :update
if "%choice%"=="2" goto :cleanup
if "%choice%"=="3" goto :reset
if "%choice%"=="4" goto :resources
if "%choice%"=="5" goto :export_logs
if "%choice%"=="6" goto :end
echo Invalid choice. Please try again.
pause
goto :end

:update
echo.
echo ====================================
echo Updating Repository and Rebuilding
echo ====================================
cd /d "%CHATTERBOX_DIR%"
echo Pulling latest changes...
git pull origin main
echo.
echo Stopping existing container...
docker compose -f docker/docker-compose.cpu.yml down
echo.
echo Rebuilding container...
docker compose -f docker/docker-compose.cpu.yml build --no-cache
echo.
echo Update completed. Start container with: chatterbox-start.bat
goto :end

:cleanup
echo.
echo ====================================
echo Cleaning Up Docker Resources
echo ====================================
echo Removing stopped containers...
docker container prune -f
echo.
echo Removing unused images...
docker image prune -f
echo.
echo Removing unused volumes...
docker volume prune -f
echo.
echo Cleanup completed.
goto :end

:reset
echo.
echo ====================================
echo Resetting Container
echo ====================================
cd /d "%CHATTERBOX_DIR%"
echo Stopping container...
docker compose -f docker/docker-compose.cpu.yml down
echo.
echo Removing container and images...
docker container rm %CONTAINER_NAME% 2>nul
docker rmi $(docker images | grep chatterbox-tts) 2>nul
echo.
echo Rebuilding from scratch...
docker compose -f docker/docker-compose.cpu.yml build --no-cache
echo.
echo Reset completed. Start container with: chatterbox-start.bat
goto :end

:resources
echo.
echo ====================================
echo Container Resource Usage
echo ====================================
docker stats %CONTAINER_NAME% --no-stream
echo.
echo Detailed container information:
docker inspect %CONTAINER_NAME% | findstr "Status\|StartedAt\|RestartCount"
goto :end

:export_logs
echo.
echo ====================================
echo Exporting Container Logs
echo ====================================
set timestamp=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set timestamp=%timestamp: =0%
set logfile=chatterbox-logs_%timestamp%.txt
echo Exporting logs to %logfile%...
docker logs %CONTAINER_NAME% > %logfile% 2>&1
echo Logs exported to %logfile%
goto :end

:end
echo.
pause
