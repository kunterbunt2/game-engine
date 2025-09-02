/*
 *
 * Copyright (C) 2025-2025 Abdalla Bushnaq
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.bushnaq.abdalla.engine.audio.radio;

import de.bushnaq.abdalla.engine.ai.coqui.CoquiTTS;
import de.bushnaq.abdalla.engine.ai.coqui.TtsModelList;
import de.bushnaq.abdalla.engine.audio.radio.util.TTSBase;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SimpleTTSTest extends TTSBase {


    @BeforeEach
    public void setUp() throws Exception {
        // Ensure container is running before each test
        if (!isContainerRunning()) {
            System.out.println("Container not running, starting it...");
            buildAndStartDockerContainer();
            waitForServiceToBeReady();
        }
    }

    @BeforeAll
    static void setUpClass() throws Exception {
        System.out.println("Starting TTS Docker container...");
        System.out.println("Note: First build may take several minutes to download dependencies...");
        SimpleTTSTest testInstance = new SimpleTTSTest();
        testInstance.buildAndStartDockerContainer();
        testInstance.waitForServiceToBeReady();
    }

    @AfterAll
    public void tearDown() throws Exception {
        // Don't stop the container automatically to preserve it for future test runs
        // This allows our optimization to work on subsequent runs
        System.out.println("Test completed. TTS Docker container left running for future use.");
        System.out.println("To manually stop: docker-compose -f docker-compose-tts.yml stop");
        System.out.println("To manually remove: docker-compose -f docker-compose-tts.yml down");
    }

    @Test
    public void testHealthCheck() throws Exception {
        URL               url  = new URL(TTS_SERVICE_URL + "/health");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        assertEquals(200, responseCode, "Health check should return 200");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String response = reader.lines().reduce("", (a, b) -> a + b);
            assertTrue(response.contains("healthy"), "Response should indicate service is healthy");
            System.out.println("Health check response: " + response);
        }
    }

    @Test
    public void testTextToSpeech() throws Exception {
        String[] testText = {
                "Hi,[clears throat] this is testing the pause feature,. and if it can be extended.",//
                "Tango 4 4 4 to Papa 4 6 1 - requesting approval to dock.",//
                "[laughs], Let's see how it performs, with different inputs."//
        };

        TtsModelList ttsModelList = CoquiTTS.listModels();
        for (String model : ttsModelList.getModels()) {
            System.out.println(model);
        }


//        CoquiTTS.loadModel("tts_models/multilingual/multi-dataset/bark");
//        CoquiTTS.loadModel("tts_models/multilingual/multi-dataset/your_tts");
//        CoquiTTS.loadModel("tts_models/multilingual/multi-dataset/xtts_v2");
        CoquiTTS.loadModel("tts_models/en/vctk/vits");
//        CoquiTTS.loadModel("tts_models/multilingual/multi-dataset/bark");
//        CoquiTTS.loadModel("tts_models/uk/mai/glow-tts");
//        CoquiTTS.loadModel("tts_models/multilingual/multi-dataset/vits");
        for (String text : testText) {
            long time = System.currentTimeMillis();
            System.out.println("Testing TTS with text: " + text);
            // Call the TTS service
            byte[] audioData = CoquiTTS.generateMinionSpeech(text, 1.3f, 1.05f, 1.1f);

            // Verify we got audio data
            assertNotNull(audioData, "Audio data should not be null");
            assertTrue(audioData.length > 0, "Audio data should not be empty");

            // Save the audio file for verification (optional)
            CoquiTTS.writeWav(audioData, "test-output.wav");

            playBlocking("test-output.wav");
            System.out.println("Speech generated successfully!");
            System.out.println("Audio file size: " + audioData.length + " bytes");
            System.out.println("Time taken for TTS: " + (System.currentTimeMillis() - time) + " ms");
        }
    }


}
