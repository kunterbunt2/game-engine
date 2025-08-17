package de.bushnaq.abdalla.engine.ai.ollama;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for OllamaClient
 * Note: These tests require a running Ollama server with a model available
 */
public class OllamaClientTest {

    private OllamaClient client;

    @BeforeEach
    void setUp() {
        client = new OllamaClient();
    }

    @Test
//    @Disabled("Requires running Ollama server - enable for integration testing")
    void testBuilderPattern() throws OllamaException {
        OllamaResponse response = client.builder("llama3.2:3b")
                .prompt("Test message")
                .temperature(0.7)
                .topK(40)
                .generate();

        assertNotNull(response);
        assertNotNull(response.getResponse());
    }

    @Test
//    @Disabled("Requires running Ollama server - enable for integration testing")
    void testGenerateWithSystem() throws OllamaException {
        String         systemPrompt = "You are a helpful assistant.";
        OllamaResponse response     = client.generate("llama3.2:3b", "Hello", systemPrompt);

        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertTrue(response.getDone());
    }

    @Test
    void testRequestCreation() {
        OllamaRequest request = new OllamaRequest("llama3.2:3b", "Test prompt");

        assertEquals("llama3.2:3b", request.getModel());
        assertEquals("Test prompt", request.getPrompt());
        assertFalse(request.getStream());
    }

    @Test
    void testResponseCreation() {
        OllamaResponse response = new OllamaResponse();
        response.setModel("llama3.2:3b");
        response.setResponse("Test response");
        response.setDone(true);

        assertEquals("llama3.2:3b", response.getModel());
        assertEquals("Test response", response.getResponse());
        assertTrue(response.getDone());
    }

    @Test
//    @Disabled("Requires running Ollama server - enable for integration testing")
    void testSimpleGenerate() throws OllamaException {
        OllamaResponse response = client.generate("llama3.2:3b", "Say hello");

        assertNotNull(response);
        assertNotNull(response.getResponse());
        assertTrue(response.getDone());
        assertEquals("llama3.2:3b", response.getModel());
    }
}
