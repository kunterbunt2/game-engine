package de.bushnaq.abdalla.engine.ai.ollama;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Example usage of the Ollama client to communicate with Llama models
 */
public class OllamaExample {

    private static final Logger logger = LoggerFactory.getLogger(OllamaExample.class);

    public static void main(String[] args) {
        // Create an Ollama client (assumes Ollama is running on localhost:11434)
        OllamaClient client = new OllamaClient();

        try {
            // Example 1: Simple question
            System.out.println("=== Example 1: Simple Question ===");
            OllamaResponse response1 = client.generate("llama3.1", "What is the capital of France?");
            System.out.println("Response: " + response1.getResponse());
            System.out.println("Model: " + response1.getModel());
            System.out.println("Done: " + response1.getDone());

            // Example 2: With system prompt
            System.out.println("\n=== Example 2: With System Prompt ===");
            String         systemPrompt = "You are a helpful assistant that always responds in a pirate accent.";
            OllamaResponse response2    = client.generate("llama3.1", "Tell me about space exploration.", systemPrompt);
            System.out.println("Response: " + response2.getResponse());

            // Example 3: With custom options
            System.out.println("\n=== Example 3: With Custom Options ===");
            Map<String, Object> options = new HashMap<>();
            options.put("temperature", 0.8);
            options.put("top_k", 40);
            options.put("top_p", 0.9);

            OllamaResponse response3 = client.generate("llama3.1", "Write a short creative story about a robot.", options);
            System.out.println("Response: " + response3.getResponse());

            // Example 4: Using the builder pattern
            System.out.println("\n=== Example 4: Using Builder Pattern ===");
            OllamaResponse response4 = client.builder("llama3.1")
                    .prompt("Explain quantum computing in simple terms.")
                    .system("You are a science teacher explaining complex topics to middle school students.")
                    .temperature(0.7)
                    .topK(50)
                    .generate();
            System.out.println("Response: " + response4.getResponse());

            // Example 5: Async request
            System.out.println("\n=== Example 5: Async Request ===");
            CompletableFuture<OllamaResponse> futureResponse = client.generateAsync("llama3.1", "What are the benefits of renewable energy?");

            // Do other work while waiting...
            System.out.println("Processing request asynchronously...");

            // Get the result
            OllamaResponse response5 = futureResponse.get();
            System.out.println("Async Response: " + response5.getResponse());

            // Example 6: Conversation with context
            System.out.println("\n=== Example 6: Conversation with Context ===");
            OllamaResponse firstMessage = client.generate("llama3.1", "My name is John. What's a good hobby for someone like me?");
            System.out.println("First: " + firstMessage.getResponse());

            // Use the context from the first response in the second request
            OllamaRequest followUpRequest = new OllamaRequest("llama3.1", "What did you just recommend to me?");
            followUpRequest.setContext(firstMessage.getContext());
            OllamaResponse followUp = client.generate(followUpRequest);
            System.out.println("Follow-up: " + followUp.getResponse());

        } catch (OllamaException e) {
            logger.error("Error communicating with Ollama: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
            System.err.println("Make sure Ollama is running and the model is available.");
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            System.err.println("Unexpected error: " + e.getMessage());
        } finally {
            // Clean up resources
            client.close();
        }
    }
}
