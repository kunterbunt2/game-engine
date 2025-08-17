package de.bushnaq.abdalla.engine.ai.ollama;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response object for Ollama API calls
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaResponse {

    @JsonProperty("context")
    private List<Integer> context;
    @JsonProperty("created_at")
    private String        createdAt;
    @JsonProperty("done")
    private Boolean       done;
    @JsonProperty("done_reason")
    private String        doneReason;
    @JsonProperty("eval_count")
    private Integer       evalCount;
    @JsonProperty("eval_duration")
    private Long          evalDuration;
    @JsonProperty("load_duration")
    private Long          loadDuration;
    @JsonProperty("model")
    private String        model;
    @JsonProperty("prompt_eval_count")
    private Integer       promptEvalCount;

    @JsonProperty("prompt_eval_duration")
    private Long   promptEvalDuration;
    @JsonProperty("response")
    private String response;
    @JsonProperty("total_duration")
    private Long   totalDuration;

    public OllamaResponse() {
    }

    public List<Integer> getContext() {
        return context;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Boolean getDone() {
        return done;
    }

    public String getDoneReason() {
        return doneReason;
    }

    public Integer getEvalCount() {
        return evalCount;
    }

    public Long getEvalDuration() {
        return evalDuration;
    }

    public Long getLoadDuration() {
        return loadDuration;
    }

    // Getters and setters
    public String getModel() {
        return model;
    }

    public Integer getPromptEvalCount() {
        return promptEvalCount;
    }

    public Long getPromptEvalDuration() {
        return promptEvalDuration;
    }

    public String getResponse() {
        return response;
    }

    public Long getTotalDuration() {
        return totalDuration;
    }

    public void setContext(List<Integer> context) {
        this.context = context;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public void setDoneReason(String doneReason) {
        this.doneReason = doneReason;
    }

    public void setEvalCount(Integer evalCount) {
        this.evalCount = evalCount;
    }

    public void setEvalDuration(Long evalDuration) {
        this.evalDuration = evalDuration;
    }

    public void setLoadDuration(Long loadDuration) {
        this.loadDuration = loadDuration;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setPromptEvalCount(Integer promptEvalCount) {
        this.promptEvalCount = promptEvalCount;
    }

    public void setPromptEvalDuration(Long promptEvalDuration) {
        this.promptEvalDuration = promptEvalDuration;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public void setTotalDuration(Long totalDuration) {
        this.totalDuration = totalDuration;
    }
}
