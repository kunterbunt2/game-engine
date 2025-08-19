package de.bushnaq.abdalla.engine.audio.synthesis.util;

import de.bushnaq.abdalla.engine.ai.PromptTags;
import de.bushnaq.abdalla.engine.audio.IRadio;
import de.bushnaq.abdalla.engine.audio.OpenAlException;
import de.bushnaq.abdalla.engine.audio.RadioMessage;
import de.bushnaq.abdalla.engine.audio.RadioRequest;

public class BasciRadio implements IRadio {

    @Override
    public void queueRadioMessageGeneration(RadioRequest rr) {

    }

    @Override
    public void radio(RadioMessage rm) {

    }

    @Override
    public void renderRadio() throws OpenAlException {

    }

    @Override
    public String resolveString(String id, PromptTags tags, boolean silent) {
        return "";
    }
}
