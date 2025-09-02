# Docker Setup for Game Engine Project

This project uses Docker containers to manage AI services for Text-to-Speech (TTS) and Large Language Model (LLM)
capabilities. This document explains all Docker-related files and how they work together.

## Overview

The project runs two separate containerized services:

- **TTS Service**: Provides text-to-speech functionality using Coqui TTS
- **Ollama Service**: Provides large language model capabilities using Ollama with Llama 3.2

## Docker Files Structure

### TTS Service Files

#### `Dockerfile.tts`

Builds the TTS container image:

- **Base Image**: `ghcr.io/coqui-ai/tts:latest` (official Coqui TTS image)
- **Dependencies**: Installs Flask for REST API and PyWorld for audio processing
- **Server Script**: Copies `tts_server.py` into the container
- **Port**: Exposes port 5000 for the TTS REST API
- **Entrypoint**: Runs the custom Python TTS server

#### `docker-compose-tts.yml`

Orchestrates the TTS service:

- **Build Context**: Uses `Dockerfile.tts` to build the image
- **Port Mapping**: Maps host port 5000 to container port 5000
- **Volumes**:
    - `./tts_cache:/root/.cache/tts` - Persists TTS model cache
    - `./tts_server.py:/app/tts_server.py` - Live updates to server code
- **GPU Support**: Reserves NVIDIA GPU for faster TTS processing
- **Health Check**: Monitors service health via `/health` endpoint
- **Restart Policy**: Automatically restarts unless manually stopped

#### `tts_server.py`

The Flask-based REST API server that provides TTS endpoints. This file is volume-mounted so changes are reflected
without rebuilding the container.

### Ollama Service Files

#### `Dockerfile.ollama`

Builds the Ollama container image:

- **Base Image**: `ollama/ollama:latest` (official Ollama image)
- **Port**: Exposes port 11434 for Ollama API
- **Setup Script**: Copies and makes executable `pull_model.sh`
- **Entrypoint**: Runs the model pulling script

#### `docker-compose-ollama.yml`

Orchestrates the Ollama service:

- **Build Context**: Uses `Dockerfile.ollama` to build the image
- **Container Name**: `ollama-llama3.2` for easy identification
- **Port Mapping**: Maps host port 11434 to container port 11434
- **Persistent Volume**: `ollama_data` stores downloaded models
- **Environment**: Sets `OLLAMA_HOST=0.0.0.0` to accept external connections
- **Health Check**: Monitors service via `/api/tags` endpoint
- **Start Period**: 60-second grace period for model downloading

#### `pull_model.sh`

Initialization script for Ollama:

- **Server Start**: Launches Ollama server in background
- **Model Download**: Automatically pulls `llama3.2:3b-instruct-q4_0` model
- **Wait Loop**: Keeps container running after setup

## Helper Scripts

### Batch Files (Windows)

- `start-ollama.bat` - Starts the Ollama service
- `check-ollama.bat` - Checks Ollama service status
- `test-ollama-client.bat` - Tests Ollama API connectivity
- `pull-model.bat` - Pulls additional models
- `tts-helper.bat` - TTS service management

### Shell Scripts (Linux/Mac)

- `pull_model.sh` - Model pulling script (used in container)

## Container Management

### Starting Services

**TTS Service:**

```bash
docker-compose -f docker-compose-tts.yml up -d
```

**Ollama Service:**

```bash
docker-compose -f docker-compose-ollama.yml up -d
```

### Stopping Services

**TTS Service:**

```bash
docker-compose -f docker-compose-tts.yml down
```

**Ollama Service:**

```bash
docker-compose -f docker-compose-ollama.yml down
```

### Rebuilding After Changes

When you modify `Dockerfile.tts` or `Dockerfile.ollama`, you need to rebuild:

**TTS Service:**

```bash
docker-compose -f docker-compose-tts.yml down
docker-compose -f docker-compose-tts.yml build --no-cache
docker-compose -f docker-compose-tts.yml up -d
```

**Ollama Service:**

```bash
docker-compose -f docker-compose-ollama.yml down
docker-compose -f docker-compose-ollama.yml build --no-cache
docker-compose -f docker-compose-ollama.yml up -d
```

## Service Endpoints

### TTS Service (Port 5000)

- **Health Check**: `GET http://localhost:5000/health`
- **Generate Speech**: `POST http://localhost:5000/tts` (with JSON payload)

### Ollama Service (Port 11434)

- **API Status**: `GET http://localhost:11434/api/tags`
- **Chat Completion**: `POST http://localhost:11434/api/chat`
- **Generate**: `POST http://localhost:11434/api/generate`

## Data Persistence

### TTS Service

- **Model Cache**: `./tts_cache/` - Stores downloaded TTS models
- **Server Code**: `./tts_server.py` - Live-mounted for development

### Ollama Service

- **Models**: Docker volume `ollama_data` - Stores downloaded LLM models
- **Location**: Models persist across container restarts

## GPU Requirements

### TTS Service

- Requires NVIDIA GPU with CUDA support
- Uses `nvidia-container-runtime` for GPU access
- Significantly faster TTS generation with GPU

### Ollama Service

- Can run on CPU or GPU
- GPU support optional but recommended for better performance

## Troubleshooting

### Common Issues

1. **Port Conflicts**: Ensure ports 5000 and 11434 are not in use
2. **GPU Access**: Verify NVIDIA Docker runtime is installed
3. **Model Downloads**: First startup may take time to download models
4. **Volume Permissions**: Ensure Docker has access to local directories

### Logs

Check service logs:

```bash
# TTS logs
docker-compose -f docker-compose-tts.yml logs -f

# Ollama logs
docker-compose -f docker-compose-ollama.yml logs -f
```

### Health Checks

Both services include health checks that monitor service availability and will restart containers if health checks fail.

## Architecture Benefits

1. **Isolation**: Each service runs in its own container
2. **Scalability**: Services can be scaled independently
3. **Portability**: Containers run consistently across environments
4. **Resource Management**: GPU allocation and resource limits
5. **Persistence**: Data survives container restarts
6. **Development**: Live code updates for TTS service
