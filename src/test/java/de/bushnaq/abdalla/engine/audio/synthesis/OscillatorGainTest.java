/*
 * Copyright (C) 2024 Abdalla Bushnaq
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bushnaq.abdalla.engine.audio.synthesis;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3NativesLoader;
import de.bushnaq.abdalla.engine.audio.synthesis.util.BasicAudioEngine;
import de.bushnaq.abdalla.engine.audio.synthesis.util.ExampleSynthesizer;
import de.bushnaq.abdalla.engine.audio.synthesis.util.TranslationUtil;
import de.bushnaq.abdalla.engine.camera.MovingCamera;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OscillatorGainTest {
    private static final float        HIGHEST_FREQUENCY = 2 * 261.6256f;//C5
    private static final float        LOWEST_FREQUENCY  = 32.70320f;//C1
    private final        Logger       logger            = LoggerFactory.getLogger(this.getClass());
    private              MovingCamera camera;

    private void createCamera() throws Exception {
        Gdx.files = new Lwjgl3Files();
        Lwjgl3NativesLoader.load();
        camera = new MovingCamera(67f, 640, 480);
        camera.position.set(300f, 500f, 400f);
        camera.lookAt(camera.lookat);
        camera.near = 1f;
        camera.far  = 8000f;
        camera.update();
    }

    @Test
    public void gainTest() throws Exception {
        final BasicAudioEngine audioEngine = new BasicAudioEngine(44100 * 5);
        createCamera();
        audioEngine.create();
        {
            final Synthesizer synth = audioEngine.createAudioProducer(ExampleSynthesizer.class);
            audioEngine.begin(camera);
            for (int i = 2; i <= 25; i++) {
                final float speed = i;
                synth.adaptToVelocity(i);
                final float gain      = synth.getGain();
                final float frequency = LOWEST_FREQUENCY + (HIGHEST_FREQUENCY - LOWEST_FREQUENCY) * (speed - TranslationUtil.MIN_ENGINE_SPEED) / (TranslationUtil.MAX_ENGINE_SPEED - TranslationUtil.MIN_ENGINE_SPEED);
                final float bassGain  = 1 - (speed - TranslationUtil.MIN_ENGINE_SPEED) / (TranslationUtil.MAX_ENGINE_SPEED - TranslationUtil.MIN_ENGINE_SPEED);
                logger.info(String.format("speed = %d bassGain = %f gain = %f freq = %f", i, bassGain, gain, frequency));
            }
            audioEngine.end();
        }
        audioEngine.dispose();
    }

}