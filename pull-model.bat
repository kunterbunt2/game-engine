@echo off
echo Pulling Llama 3.2:3B model from Ollama...

REM Check if Ollama is running
curl -s http://localhost:11434/api/tags >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Ollama is not running on localhost:11434
    echo Please start Ollama by running: ollama serve
    echo Or ensure the Ollama service is running.
    pause
    exit /b 1
)

echo Ollama is accessible. Current models:
ollama list

echo.
echo Pulling llama3.2:3b model...
ollama pull llama3.2:3b

if %errorlevel% equ 0 (
    echo ✅ Successfully pulled llama3.2:3b
) else (
    echo ❌ Failed to pull llama3.2:3b, trying llama3.2:3b-instruct...
    ollama pull llama3.2:3b-instruct
    if %errorlevel% equ 0 (
        echo ✅ Successfully pulled llama3.2:3b-instruct
    ) else (
        echo ❌ Failed to pull model. Available models on Ollama registry:
        echo Try: ollama pull llama3.2:1b, ollama pull llama3.2:3b, ollama pull llama3.1:8b
    )
)

echo.
echo Final model list:
ollama list

pause
