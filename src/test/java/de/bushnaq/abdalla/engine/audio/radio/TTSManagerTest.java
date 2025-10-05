package de.bushnaq.abdalla.engine.audio.radio;

import de.bushnaq.abdalla.engine.ai.TTSManager;
import de.bushnaq.abdalla.engine.audio.radio.util.TTSBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class TTSManagerTest extends TTSBase {
    @Test
    public void testChatterboxTTSProvider() throws Exception {
        TTSManager ttsManager = new TTSManager();
        ttsManager.setEngine(TTSManager.TTSEngine.CHATTERBOX);
        for (String voice : ttsManager.getAvailableVoices()) {
            System.out.println(voice);
        }
        for (String language : ttsManager.listLanguages()) {
            System.out.println(language);
        }
        String testText = "I was sitting in the corner, when this man showed up and started shooting all the people in the coffee shop!";

        byte[] audio = ttsManager.generateSpeech(testText);
        Assertions.assertNotNull(audio);
        Assertions.assertTrue(audio.length > 100, "Audio data should be non-trivial");
        Path out = Path.of("target/test-chatterbox.wav");
        Files.write(out, audio);
        playBlocking(out.toString());
        System.out.println("ChatterboxTTS audio written to: " + out.toAbsolutePath());
    }

    @Test
    public void testCoquiTTSProvider() throws Exception {
        TTSManager ttsManager = new TTSManager();
        ttsManager.setEngine(TTSManager.TTSEngine.COQUI);
        for (String voice : ttsManager.getAvailableVoices()) {
            System.out.println(voice);
        }
        for (String language : ttsManager.listLanguages()) {
            System.out.println(language);
        }
        String testText = "I was sitting in the corner, when this man showed up and started shooting all the people in the coffee shop!";
        byte[] audio    = ttsManager.generateSpeech(testText);
        Assertions.assertNotNull(audio);
        Assertions.assertTrue(audio.length > 100, "Audio data should be non-trivial");
        // Optionally write to file for manual inspection
        Path out = Path.of("target/test-coqui.wav");
        Files.write(out, audio);
        playBlocking(out.toString());
        System.out.println("CoquiTTS audio written to: " + out.toAbsolutePath());
    }
}

