package de.bushnaq.abdalla.engine.ai.ollama;

import de.bushnaq.abdalla.engine.ai.PromptTags;

public class LLMPrompt {
    private final String prompt;
    private final String systemPrompt;

    public LLMPrompt(String prompt, String systemPrompt) {
        this.prompt       = prompt;
        this.systemPrompt = systemPrompt;
    }

    public LLMPrompt(LLMPrompt other, PromptTags tags) {
        this.prompt       = tags.replaceAllPreTags(other.prompt);
        this.systemPrompt = tags.replaceAllPreTags(other.systemPrompt);
    }

    public String getPrompt() {
        return prompt;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }
}
