@echo off
echo Checking Ollama status and available models...

REM Test if Ollama is running
echo Testing connection to Ollama...
curl -s http://localhost:11434/api/tags >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Ollama is not accessible at http://localhost:11434
    echo.
    echo Please ensure Ollama is running:
    echo   1. Start Ollama: ollama serve
    echo   2. Or check if Ollama service is running
    echo   3. Verify firewall settings
    pause
    exit /b 1
)

echo ✅ Ollama is accessible!
echo.

echo Current models installed:
ollama list
echo.

echo Available models on registry (popular ones):
echo   - llama3.2:1b (smallest, fastest)
echo   - llama3.2:3b (good balance)
echo   - llama3.1:8b (larger, more capable)
echo   - codellama:7b (for coding tasks)
echo.

echo To pull a model, run: ollama pull MODEL_NAME
echo Example: ollama pull llama3.2:3b
echo.

pause
