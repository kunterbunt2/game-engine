package de.bushnaq.abdalla.engine.audio;

import de.bushnaq.abdalla.engine.ai.PromptTags;

/**
 * interface handling communication between the game NPCs.
 */
public interface IRadio {
    String resolveString(String id, PromptTags tags, boolean silent);

    void talk(RadioMessage rm);
}