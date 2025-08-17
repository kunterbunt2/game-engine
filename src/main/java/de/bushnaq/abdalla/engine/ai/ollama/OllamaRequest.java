package de.bushnaq.abdalla.engine.ai.ollama;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Request object for Ollama API calls
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaRequest {

    @JsonProperty("context")
    private List<Integer>       context;
    @JsonProperty("model")
    private String              model;
    @JsonProperty("options")
    private Map<String, Object> options;
    @JsonProperty("prompt")
    private String              prompt;
    @JsonProperty("stream")
    private Boolean             stream = false;
    @JsonProperty("system")
    private String              system;
    @JsonProperty("template")
    private String              template;

    public OllamaRequest() {
    }

    public OllamaRequest(String model, String prompt) {
        this.model  = model;
        this.prompt = prompt;
    }

    public List<Integer> getContext() {
        return context;
    }

    // Getters and setters
    public String getModel() {
        return model;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public String getPrompt() {
        return prompt;
    }

    public Boolean getStream() {
        return stream;
    }

    public String getSystem() {
        return system;
    }

    public String getTemplate() {
        return template;
    }

    public void setContext(List<Integer> context) {
        this.context = context;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
