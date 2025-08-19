package de.bushnaq.abdalla.engine.audio.synthesis.util;

import de.bushnaq.abdalla.engine.ai.PromptTags;
import de.bushnaq.abdalla.engine.audio.IRadio;
import de.bushnaq.abdalla.engine.audio.RadioMessage;

public class BasciRadio implements IRadio {

    @Override
    public String resolveString(String id, PromptTags tags, boolean silent) {
        return "";
    }

    @Override
    public void talk(RadioMessage rm) {
        
    }
}
