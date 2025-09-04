package de.bushnaq.abdalla.engine.audio.synthesis.util;

import de.bushnaq.abdalla.engine.ai.PromptTags;
import de.bushnaq.abdalla.engine.audio.OpenAlException;
import de.bushnaq.abdalla.engine.audio.radio.IRadio;
import de.bushnaq.abdalla.engine.audio.radio.RadioMessage;

public class BasciRadio implements IRadio {

    @Override
    public String generateLlmAnswer(String id, String prompt, PromptTags tags, boolean silent) {
        return "";
    }

    @Override
    public void queueRadioMessageGeneration(RadioMessage rm) {

    }

    @Override
    public void radio(RadioMessage rm) {

    }

    @Override
    public void renderRadio() throws OpenAlException {

    }
}
