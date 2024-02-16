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
import de.bushnaq.abdalla.engine.audio.synthesis.util.SawAudioEngine;
import de.bushnaq.abdalla.engine.camera.MovingCamera;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

public class MercatorOscillatorTest {
    private static final int          SECONDS_2 = 2000;
    private final        Logger       logger    = LoggerFactory.getLogger(this.getClass());
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
    public void renderPerformanceTest() throws Exception {
        final BasicAudioEngine audioEngine = new BasicAudioEngine();
        audioEngine.create();
        createCamera();
        final int               numberOfSources = audioEngine.getMaxMonoSources();
        final List<Synthesizer> synths          = new ArrayList<>();
        //create synths
        for (int i = 0; i < numberOfSources; i++) {
            synths.add(audioEngine.createAudioProducer(ExampleSynthesizer.class));
        }

        audioEngine.begin(camera);
        final long time1 = System.currentTimeMillis();
        for (final Synthesizer synth : synths) {
            synth.renderBuffer();
        }
        final long time2 = System.currentTimeMillis();
        final long delta = time2 - time1;
        audioEngine.end();
        audioEngine.dispose();
        logger.info(String.format("Rendered %d buffers each %d samples in %dms", numberOfSources, SawAudioEngine.samplerate, delta));
        assertThat(String.format("expected to render %d buffers each %d samples in less than 1s", numberOfSources, SawAudioEngine.samples), delta, is(lessThan(3000L)));
    }

    @Test
    public void renderTest() throws Exception {
        final BasicAudioEngine audioEngine = new BasicAudioEngine(44100 * 5);
        audioEngine.create();
        createCamera();
        {
            final Synthesizer synth = audioEngine.createAudioProducer(ExampleSynthesizer.class);
            synth.setGain(5);
            synth.play();
            audioEngine.begin(camera);
            final long time1 = System.currentTimeMillis();
            do {
            } while (System.currentTimeMillis() - time1 < SECONDS_2);
            synth.renderBuffer();
            synth.writeWav("target/mercator");
            audioEngine.end();
        }
        audioEngine.dispose();
    }

}