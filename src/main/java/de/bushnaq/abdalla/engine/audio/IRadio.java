package de.bushnaq.abdalla.engine.audio;

import de.bushnaq.abdalla.engine.ai.PromptTags;

/**
 * interface handling communication between the game NPCs.
 */
public interface IRadio {
    void queueRadioMessageGeneration(RadioMessage rm);

    void radio(RadioMessage rm);

    void renderRadio() throws OpenAlException;

    String resolveString(String id, PromptTags tags, boolean silent);
}