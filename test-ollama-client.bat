@echo off
echo Testing Ollama Client...
echo.

cd /d "%~dp0"

:: Build the project first
call mvn compile -q

if %ERRORLEVEL% neq 0 (
    echo Build failed!
    pause
    exit /b 1
)

:: Set up classpath with all dependencies
for /f %%i in ('mvn dependency:build-classpath -q -Dmdep.outputFile=cp.txt') do set CLASSPATH=%%i
if exist cp.txt (
    set /p MAVEN_CLASSPATH=<cp.txt
    del cp.txt
) else (
    echo Could not determine Maven classpath
    pause
    exit /b 1
)

:: Run the test client
java -cp "target/classes;%MAVEN_CLASSPATH%" de.bushnaq.abdalla.engine.ai.ollama.Llama32TestClient

echo.
echo Test completed.
pause
