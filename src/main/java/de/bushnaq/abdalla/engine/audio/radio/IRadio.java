package de.bushnaq.abdalla.engine.audio.radio;

import de.bushnaq.abdalla.engine.ai.PromptTags;
import de.bushnaq.abdalla.engine.audio.OpenAlException;

/**
 * interface handling communication between the game NPCs.
 */
public interface IRadio {
    String generateLlmAnswer(String id, String prompt, PromptTags tags, boolean silent);

    void queueRadioMessageGeneration(RadioMessage rm);

    void radio(RadioMessage rm);

    void renderRadio() throws OpenAlException;
}