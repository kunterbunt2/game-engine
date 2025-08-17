#!/bin/bash

# Start Ollama server in the background
ollama serve &

# Wait for the server to be ready
echo "Waiting for Ollama server to start..."
sleep 5

# Pull the Llama 3.2:3B-Q4_0 model
echo "Pulling llama3.2:3b-instruct-q4_0 model..."
ollama pull llama3.2:3b-instruct-q4_0

echo "Model pulled successfully. Ollama server is ready!"

# Keep the container running
wait
