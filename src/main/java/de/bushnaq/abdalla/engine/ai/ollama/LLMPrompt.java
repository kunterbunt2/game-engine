package de.bushnaq.abdalla.engine.ai.ollama;

import de.bushnaq.abdalla.engine.ai.PromptTags;
import lombok.Getter;
import lombok.Setter;

@Getter
public class LLMPrompt {
    @Setter
    private       String prompt;
    private final String systemPrompt;

    public LLMPrompt(String prompt, String systemPrompt) {
        this.prompt       = prompt;
        this.systemPrompt = systemPrompt;
    }

    public LLMPrompt(LLMPrompt other, PromptTags tags) {
        this.prompt       = tags.replaceAllPreTags(other.prompt);
        this.systemPrompt = tags.replaceAllPreTags(other.systemPrompt);
    }

}
