package de.bushnaq.abdalla.engine.ai;

public interface TTSProvider {
    byte[] generateMinionSpeech(String text, String voice, Float pitchShift, Float speedFactor, Float formantShift) throws Exception;

    byte[] generateSpeech(String text) throws Exception;

    byte[] generateSpeech(String text, String voice) throws Exception;

    String[] getAvailableVoices() throws Exception;

    boolean isHealthy() throws Exception;

    String[] listLanguages() throws Exception;
}
