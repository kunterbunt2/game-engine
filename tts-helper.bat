@echo off
REM Helper script for managing the TTS Docker service

if "%1"=="start" goto start
if "%1"=="stop" goto stop
if "%1"=="build" goto build
if "%1"=="logs" goto logs
if "%1"=="status" goto status
if "%1"=="test" goto test

echo Usage: tts-helper.bat [start^|stop^|build^|logs^|status^|test]
echo.
echo Commands:
echo   start  - Start the TTS service
echo   stop   - Stop the TTS service
echo   build  - Build the Docker image
echo   logs   - Show service logs
echo   status - Check service status
echo   test   - Test the service with a sample request
goto end

:build
echo Building TTS Docker image...
docker-compose -f docker-compose-tts.yml build
goto end

:start
echo Starting TTS service...
docker-compose -f docker-compose-tts.yml up -d
echo Waiting for service to be ready...
timeout /t 10 /nobreak >nul
curl -s http://localhost:5000/health
goto end

:stop
echo Stopping TTS service...
docker-compose -f docker-compose-tts.yml down
goto end

:logs
echo Showing TTS service logs...
docker-compose -f docker-compose-tts.yml logs -f
goto end

:status
echo Checking TTS service status...
docker-compose -f docker-compose-tts.yml ps
echo.
echo Health check:
curl -s http://localhost:5000/health
goto end

:test
echo Testing TTS service...
curl -X POST http://localhost:5000/speak ^
  -H "Content-Type: application/json" ^
  -d "{\"text\":\"Hello, this is a test of the TTS service.\"}" ^
  --output test-speech.wav
echo.
echo Audio saved to test-speech.wav
goto end

:end
