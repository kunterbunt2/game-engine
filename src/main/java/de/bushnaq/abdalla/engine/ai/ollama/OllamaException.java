package de.bushnaq.abdalla.engine.ai.ollama;

/**
 * Exception thrown when there are errors communicating with Ollama
 */
public class OllamaException extends Exception {

    public OllamaException(String message) {
        super(message);
    }

    public OllamaException(String message, Throwable cause) {
        super(message, cause);
    }

    public OllamaException(Throwable cause) {
        super(cause);
    }
}
