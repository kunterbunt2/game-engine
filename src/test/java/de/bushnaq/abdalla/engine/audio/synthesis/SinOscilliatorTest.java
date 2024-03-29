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
import de.bushnaq.abdalla.engine.audio.synthesis.util.SinAudioEngine;
import de.bushnaq.abdalla.engine.audio.synthesis.util.SinSynthesizer;
import de.bushnaq.abdalla.engine.audio.synthesis.util.TranslationUtil;
import de.bushnaq.abdalla.engine.camera.MovingCamera;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

public class SinOscilliatorTest {
    private static final int          SECONDS_2 = 2000;
    private final        Logger       logger    = LoggerFactory.getLogger(this.getClass());
    private              MovingCamera camera;

    private float calcualteSpeed(final float frequency) {
        return TranslationUtil.MIN_ENGINE_SPEED + (((frequency - 220) / (500 - 220)) * (TranslationUtil.MAX_ENGINE_SPEED - TranslationUtil.MIN_ENGINE_SPEED));
    }

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
        final SinAudioEngine audioEngine = new SinAudioEngine();
        audioEngine.create(null);
        createCamera();
        final int               numberOfSources = audioEngine.getMaxMonoSources();
        final List<Synthesizer> synths          = new ArrayList<>();
        //create synths
        logger.info("Start creating synths");
        for (int i = 0; i < numberOfSources; i++) {
            synths.add(audioEngine.createAudioProducer(SinSynthesizer.class));
        }
        logger.info("End creating synths");

        audioEngine.begin(camera, true);
        audioEngine.end();
        logger.info("Start rendering synths");
        final long time1 = System.currentTimeMillis();
        for (final Synthesizer synth : synths) {
            synth.renderBuffer();
        }
        final long time2 = System.currentTimeMillis();
        logger.info("End rendering synths");
        final long delta = time2 - time1;
        audioEngine.dispose();
        logger.info(String.format("Rendered %d buffers each %d samples in %dms", numberOfSources, SinAudioEngine.samples, delta));
        assertThat(String.format("expected to render %d buffers each %d samples in less than 1s", numberOfSources, SinAudioEngine.samples), delta, is(lessThan(1000L)));
    }

    @Test
    public void renderSpeedTest() throws Exception {
        final SinAudioEngine audioEngine = new SinAudioEngine();
        audioEngine.create(null);
        createCamera();
        {
            final SinSynthesizer synth = audioEngine.createAudioProducer(SinSynthesizer.class);
            synth.setGain(5);

            float frequency = 220.0f;
            float speed     = calcualteSpeed(frequency);
            synth.sin1.setFrequency(frequency);

            synth.setPositionAndVelocity(new float[]{frequency, 0, 0}, new float[]{speed, 0, 0});
            synth.play();
            audioEngine.begin(camera, true);
            final long time1    = System.currentTimeMillis();
            long       lastTime = 0;
            long       time2;
            float      delta    = 2;
            synth.sin1.setOscillator(frequency);
            synth.setGain(20.0f);
            //						synth.setKeepCopy(true);
            do {
                time2 = System.currentTimeMillis();
                if (time2 - lastTime > 20) {
                    frequency += delta;
                    lastTime = time2;
                    if (frequency > 500 || frequency < 220) {
                        delta *= -1f;
                    }

                } else {
                    Thread.sleep(10);
                }
                speed = calcualteSpeed(frequency);
                //				logger.info(String.format("speed = %f", speed));
                synth.setPositionAndVelocity(new float[]{frequency, 0, 0}, new float[]{speed, 0, 0});
            } while (time2 - time1 < 6000);
            synth.writeWav("target/sinSpeed");
            synth.setKeepCopy(false);
            audioEngine.end();
        }
        audioEngine.dispose();
    }

    @Test
    public void renderTest() throws Exception {
        final SinAudioEngine audioEngine = new SinAudioEngine();
        audioEngine.create(null);
        createCamera();
        {
            final Synthesizer synth = audioEngine.createAudioProducer(SinSynthesizer.class);
            synth.setGain(5);
            synth.play();
            audioEngine.begin(camera, true);
            final long time1 = System.currentTimeMillis();
            do {
            } while (System.currentTimeMillis() - time1 < SECONDS_2);
            synth.renderBuffer();
            synth.writeWav("target/sin.wav");
            audioEngine.end();
        }
        audioEngine.dispose();
    }

}