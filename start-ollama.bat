@echo off
echo Starting standalone Ollama and pulling Llama 3.2:3B model...

REM Check if Ollama is running
curl -s http://localhost:11434/api/tags >nul 2>&1
if %errorlevel% neq 0 (
    echo Ollama is not running. Please start Ollama first.
    echo You can start it by running: ollama serve
    echo Or if you have Ollama installed as a service, make sure it's started.
    pause
    exit /b 1
)

echo Ollama is running. Checking for llama3.2:3b model...

REM First, try to list models to see what's available
echo Current models:
curl -s http://localhost:11434/api/tags

echo.
echo Pulling llama3.2:3b model (this may take a few minutes)...
ollama pull llama3.2:3b

if %errorlevel% equ 0 (
    echo.
    echo ✅ Model pulled successfully!
    echo You can now run the test client: test-ollama-client.bat
) else (
    echo.
    echo ❌ Failed to pull model. Trying alternative model name...
    ollama pull llama3.2:3b-instruct-q4_0
    if %errorlevel% equ 0 (
        echo ✅ Alternative model pulled successfully!
    ) else (
        echo ❌ Failed to pull model. Please check your internet connection and try again.
    )
)

echo.
echo Available models after pull:
ollama list

echo.
pause
