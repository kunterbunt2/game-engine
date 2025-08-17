package de.bushnaq.abdalla.engine;

import de.bushnaq.abdalla.engine.audio.OpenAlException;

import java.io.IOException;

public interface ISubtitles {
    void add(String subtitle);

    void render(float deltaTime) throws IOException, OpenAlException;
}
