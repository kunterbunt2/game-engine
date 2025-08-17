package de.bushnaq.abdalla.engine.ai.ollama;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test client for Llama 3.2:3B model running in standalone Ollama
 */
public class Llama32TestClient {

    // Updated to use the standard model name
    private static final String LLAMA32_MODEL = "llama3.2:3b";
    private static final Logger logger        = LoggerFactory.getLogger(Llama32TestClient.class);

    public static void main(String[] args) {
        OllamaClient client = new OllamaClient("http://localhost:11434");

        try {
            System.out.println("=== Testing Llama 3.2:3B Model ===");
            System.out.println("Connecting to Ollama at http://localhost:11434");
            System.out.println("Model: " + LLAMA32_MODEL);
            System.out.println();

            // Check if model exists, if not try to pull it
            System.out.println("Checking if model exists...");
            if (!client.hasModel(LLAMA32_MODEL)) {
                System.out.println("Model not found. Available models:");
                client.listModels().forEach(model -> System.out.println("  - " + model));
                System.out.println("Attempting to pull model: " + LLAMA32_MODEL);
                client.pullModel(LLAMA32_MODEL, java.time.Duration.ofMinutes(10));
                System.out.println("✅ Model pulled successfully!");
            } else {
                System.out.println("✅ Model found!");
            }
            System.out.println();

            // Test 1: Simple question
            System.out.println("Test 1: Simple factual question");
            System.out.println("Question: What is the capital of Japan?");
            OllamaResponse response1 = client.generate(LLAMA32_MODEL, "What is the capital of Japan?");
            System.out.println("Answer: " + response1.getResponse().trim());
            System.out.println("Tokens generated: " + response1.getEvalCount());
            System.out.println();

            // Test 2: Creative task
            System.out.println("Test 2: Creative writing");
            System.out.println("Prompt: Write a haiku about programming");
            OllamaResponse response2 = client.generate(LLAMA32_MODEL, "Write a haiku about programming");
            System.out.println("Response: " + response2.getResponse().trim());
            System.out.println();

            // Test 3: Code assistance
            System.out.println("Test 3: Code assistance");
            String codePrompt = "Write a simple Java method that calculates the factorial of a number";
            System.out.println("Prompt: " + codePrompt);
            OllamaResponse response3 = client.builder(LLAMA32_MODEL)
                    .prompt(codePrompt)
                    .temperature(0.3) // Lower temperature for more deterministic code
                    .generate();
            System.out.println("Response: " + response3.getResponse().trim());
            System.out.println();

            // Test 4: Conversation with context
            System.out.println("Test 4: Conversation with context");
            System.out.println("First message: My favorite color is blue. What are some things that are this color?");
            OllamaResponse firstMsg = client.generate(LLAMA32_MODEL, "My favorite color is blue. What are some things that are this color?");
            System.out.println("Response: " + firstMsg.getResponse().trim());

            System.out.println("Follow-up: Can you suggest a paint color name for my favorite color?");
            OllamaRequest followUp = new OllamaRequest(LLAMA32_MODEL, "Can you suggest a paint color name for my favorite color?");
            followUp.setContext(firstMsg.getContext());
            OllamaResponse followUpResponse = client.generate(followUp);
            System.out.println("Response: " + followUpResponse.getResponse().trim());
            System.out.println();

            // Performance info
            System.out.println("=== Performance Summary ===");
            System.out.println("Last response timing:");
            if (followUpResponse.getTotalDuration() != null) {
                System.out.println("Total duration: " + (followUpResponse.getTotalDuration() / 1_000_000) + "ms");
            }
            if (followUpResponse.getLoadDuration() != null) {
                System.out.println("Load duration: " + (followUpResponse.getLoadDuration() / 1_000_000) + "ms");
            }
            if (followUpResponse.getEvalDuration() != null && followUpResponse.getEvalCount() != null) {
                long tokensPerSecond = (followUpResponse.getEvalCount() * 1_000_000_000L) / followUpResponse.getEvalDuration();
                System.out.println("Generation speed: " + tokensPerSecond + " tokens/second");
            }

            System.out.println("\n✅ All tests completed successfully!");

        } catch (OllamaException e) {
            System.err.println("❌ Error communicating with Ollama: " + e.getMessage());
            System.err.println("\nTroubleshooting:");
            System.err.println("1. Make sure Ollama is running: ollama serve");
            System.err.println("2. Pull the model: ollama pull " + LLAMA32_MODEL);
            System.err.println("3. Check available models: ollama list");
            System.err.println("4. Verify Ollama is accessible: curl http://localhost:11434/api/tags");
            logger.error("main", e);
        } finally {
            client.close();
        }
    }
}
