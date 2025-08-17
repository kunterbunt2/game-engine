package de.bushnaq.abdalla.engine.ai.ollama;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client for communicating with Ollama API to interact with Llama models
 */
public class OllamaClient {

    private static final Logger logger = LoggerFactory.getLogger(OllamaClient.class);

    private final String              baseUrl;
    private final ExecutorService     executorService;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper        objectMapper;

    /**
     * Default constructor using localhost:11434
     */
    public OllamaClient() {
        this("http://localhost:11434");
    }

    /**
     * Constructor with custom Ollama server URL
     *
     * @param baseUrl The base URL of the Ollama server (e.g., "http://localhost:11434")
     */
    public OllamaClient(String baseUrl) {
        this.baseUrl         = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient      = HttpClients.createDefault();
        this.objectMapper    = new ObjectMapper();
        this.executorService = Executors.newCachedThreadPool();

        logger.info("OllamaClient initialized with base URL: {}", this.baseUrl);
    }

    /**
     * Create a builder for more complex request configuration
     *
     * @param model The model name
     * @return A request builder
     */
    public RequestBuilder builder(String model) {
        return new RequestBuilder(this, model);
    }

    /**
     * Close the client and release resources
     */
    public void close() {
        try {
            httpClient.close();
            executorService.shutdown();
        } catch (IOException e) {
            logger.warn("Error closing HTTP client", e);
        }
    }

    // --- Model management methods --------------------------------------------

    /**
     * Ensure the model exists, pulling it if necessary.
     *
     * @param model   The model name to ensure exists
     * @param timeout Maximum time to wait for pull if needed
     * @throws OllamaException if there's an error ensuring the model exists
     */
    public void ensureModel(String model, Duration timeout) throws OllamaException {
        if (hasModel(model)) {
            logger.debug("Model '{}' already exists", model);
            return;
        }
        logger.info("Model '{}' not found, pulling...", model);
        pullModel(model, timeout);
    }

    /**
     * Send a simple prompt to a Llama model and get the response
     *
     * @param model  The model name (e.g., "llama2", "llama3.1")
     * @param prompt The prompt to send to the model
     * @return The model's response
     * @throws OllamaException if there's an error communicating with Ollama
     */
    public OllamaResponse generate(String model, String prompt) throws OllamaException {
        OllamaRequest request = new OllamaRequest(model, prompt);
        return generate(request);
    }

    /**
     * Send a prompt with system instructions to a Llama model
     *
     * @param model        The model name
     * @param prompt       The user prompt
     * @param systemPrompt The system instructions
     * @return The model's response
     * @throws OllamaException if there's an error communicating with Ollama
     */
    public OllamaResponse generate(String model, String prompt, String systemPrompt) throws OllamaException {
        OllamaRequest request = new OllamaRequest(model, prompt);
        request.setSystem(systemPrompt);
        return generate(request);
    }

    /**
     * Send a request with custom options to a Llama model
     *
     * @param model   The model name
     * @param prompt  The prompt
     * @param options Custom options (temperature, top_k, top_p, etc.)
     * @return The model's response
     * @throws OllamaException if there's an error communicating with Ollama
     */
    public OllamaResponse generate(String model, String prompt, Map<String, Object> options) throws OllamaException {
        OllamaRequest request = new OllamaRequest(model, prompt);
        request.setOptions(options);
        return generate(request);
    }

    // --- Text generation methods --------------------------------------------

    /**
     * Send a full OllamaRequest object
     *
     * @param request The complete request object
     * @return The model's response
     * @throws OllamaException if there's an error communicating with Ollama
     */
    public OllamaResponse generate(OllamaRequest request) throws OllamaException {
        try {
            String   url      = baseUrl + "/api/generate";
            HttpPost httpPost = new HttpPost(url);

            // Convert request to JSON
            String jsonRequest = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(jsonRequest, ContentType.APPLICATION_JSON));
            httpPost.setHeader("Accept", "application/json");

//            logger.debug("Sending request to Ollama URL: {}", url);
//            logger.debug("Request JSON: {}", jsonRequest);

            return httpClient.execute(httpPost, response -> {
                try {
                    int    statusCode   = response.getCode();
                    String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);

//                    logger.debug("Response status: {}", statusCode);
//                    logger.debug("Response body: {}", responseBody);

                    if (statusCode != 200) {
                        throw new RuntimeException(new OllamaException("HTTP " + statusCode + ": " + response.getReasonPhrase() + " - Response: " + responseBody));
                    }

                    return objectMapper.readValue(responseBody, OllamaResponse.class);
                } catch (Exception e) {
                    if (e instanceof RuntimeException && e.getCause() instanceof OllamaException) {
                        throw (RuntimeException) e;
                    }
                    throw new RuntimeException(new OllamaException("Error processing response: " + e.getMessage(), e));
                }
            });

        } catch (IOException e) {
            throw new OllamaException("Error communicating with Ollama: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            // Check if this is a wrapped OllamaException from the lambda
            if (e.getCause() instanceof OllamaException) {
                throw (OllamaException) e.getCause();
            }
            throw new OllamaException("Unexpected error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new OllamaException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Send a request asynchronously
     *
     * @param model  The model name
     * @param prompt The prompt
     * @return CompletableFuture containing the response
     */
    public CompletableFuture<OllamaResponse> generateAsync(String model, String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return generate(model, prompt);
            } catch (OllamaException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    /**
     * Send a request asynchronously with system prompt
     *
     * @param model        The model name
     * @param prompt       The prompt
     * @param systemPrompt The system instructions
     * @return CompletableFuture containing the response
     */
    public CompletableFuture<OllamaResponse> generateAsync(String model, String prompt, String systemPrompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return generate(model, prompt, systemPrompt);
            } catch (OllamaException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    /**
     * Check if a specific model is present on the server.
     *
     * @param model The model name to check
     * @return true if the model exists, false otherwise
     * @throws OllamaException if there's an error communicating with Ollama
     */
    public boolean hasModel(String model) throws OllamaException {
        List<String> models = listModels();
        return models.stream().anyMatch(name -> name.equalsIgnoreCase(model));
    }

    /**
     * List available model names on the Ollama server.
     *
     * @return List of model names available on the server
     * @throws OllamaException if there's an error communicating with Ollama
     */
    public List<String> listModels() throws OllamaException {
        try {
            String  url  = baseUrl + "/api/tags";
            HttpGet http = new HttpGet(url);
            http.setHeader("Accept", "application/json");

            return httpClient.execute(http, response -> {
                int    statusCode   = response.getCode();
                String responseBody = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                logger.debug("/api/tags status: {} body: {}", statusCode, responseBody);

                if (statusCode != 200) {
                    throw new RuntimeException(new OllamaException("HTTP " + statusCode + ": " + response.getReasonPhrase()));
                }

                JsonNode     root  = objectMapper.readTree(responseBody);
                List<String> names = new ArrayList<>();

                if (root.has("models") && root.get("models").isArray()) {
                    for (JsonNode m : root.get("models")) {
                        JsonNode nameNode = m.get("name");
                        if (nameNode != null) {
                            names.add(nameNode.asText());
                        }
                    }
                }
                return names;
            });
        } catch (IOException e) {
            throw new OllamaException("Error listing models: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof OllamaException) {
                throw (OllamaException) e.getCause();
            }
            throw new OllamaException("Unexpected error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new OllamaException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Pull a model on the server and wait until it's ready.
     * This consumes the streaming NDJSON from /api/pull until success.
     *
     * @param model   The model name to pull
     * @param timeout Maximum time to wait for the pull to complete
     * @throws OllamaException if there's an error pulling the model
     */
    public void pullModel(String model, Duration timeout) throws OllamaException {
        try {
            String              url      = baseUrl + "/api/pull";
            HttpPost            httpPost = new HttpPost(url);
            Map<String, Object> body     = Map.of("name", model);
            httpPost.setEntity(new StringEntity(objectMapper.writeValueAsString(body), ContentType.APPLICATION_JSON));
            httpPost.setHeader("Accept", "application/json");

            Instant start = Instant.now();
            logger.info("Pulling model '{}' from {}", model, url);

            httpClient.execute(httpPost, response -> {
                int statusCode = response.getCode();
                if (statusCode != 200) {
                    String err = new String(response.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
                    throw new RuntimeException(new OllamaException("HTTP " + statusCode + ": " + err));
                }

                try (InputStream is = response.getEntity().getContent();
                     BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String  line;
                    boolean success = false;
                    while ((line = br.readLine()) != null) {
                        logger.debug("Pull event: {}", line);

                        if (Duration.between(start, Instant.now()).compareTo(timeout) > 0) {
                            throw new RuntimeException(new OllamaException("Timed out pulling model: " + model));
                        }

                        if (line.isBlank()) continue;

                        JsonNode evt = objectMapper.readTree(line);
                        if (evt.has("error")) {
                            throw new RuntimeException(new OllamaException("Pull error: " + evt.get("error").asText()));
                        }
                        if (evt.has("status") && evt.get("status").asText().toLowerCase().contains("success")) {
                            success = true;
                            break;
                        }
                    }

                    if (!success) {
                        throw new RuntimeException(new OllamaException("Pull did not complete for model: " + model));
                    }
                    return null;
                }
            });

            logger.info("Successfully pulled model: {}", model);
        } catch (IOException e) {
            throw new OllamaException("Error pulling model: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof OllamaException) {
                throw (OllamaException) e.getCause();
            }
            throw new OllamaException("Unexpected error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new OllamaException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Builder class for constructing complex requests
     */
    public static class RequestBuilder {
        private final OllamaClient  client;
        private final OllamaRequest request;

        RequestBuilder(OllamaClient client, String model) {
            this.client  = client;
            this.request = new OllamaRequest(model, null);
        }

        public RequestBuilder context(List<Integer> context) {
            request.setContext(context);
            return this;
        }

        public OllamaResponse generate() throws OllamaException {
            return client.generate(request);
        }

        public CompletableFuture<OllamaResponse> generateAsync() {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    return client.generate(request);
                } catch (OllamaException e) {
                    throw new RuntimeException(e);
                }
            }, client.executorService);
        }

        public RequestBuilder option(String key, Object value) {
            if (request.getOptions() == null) {
                request.setOptions(new HashMap<>());
            }
            request.getOptions().put(key, value);
            return this;
        }

        public RequestBuilder prompt(String prompt) {
            request.setPrompt(prompt);
            return this;
        }

        public RequestBuilder system(String system) {
            request.setSystem(system);
            return this;
        }

        public RequestBuilder temperature(double temperature) {
            if (request.getOptions() == null) {
                request.setOptions(new HashMap<>());
            }
            request.getOptions().put("temperature", temperature);
            return this;
        }

        public RequestBuilder topK(int topK) {
            if (request.getOptions() == null) {
                request.setOptions(new HashMap<>());
            }
            request.getOptions().put("top_k", topK);
            return this;
        }

        public RequestBuilder topP(double topP) {
            if (request.getOptions() == null) {
                request.setOptions(new HashMap<>());
            }
            request.getOptions().put("top_p", topP);
            return this;
        }
    }
}
