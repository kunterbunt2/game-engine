package de.bushnaq.abdalla.engine;

public class LLMPrompt {
    private final String prompt;
    private final String systemPrompt;

    public LLMPrompt(String prompt, String systemPrompt) {
        this.prompt       = prompt;
        this.systemPrompt = systemPrompt;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }
}
