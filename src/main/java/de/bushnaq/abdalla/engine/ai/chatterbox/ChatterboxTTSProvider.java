package de.bushnaq.abdalla.engine.ai.chatterbox;

import de.bushnaq.abdalla.engine.ai.TTSProvider;

public class ChatterboxTTSProvider implements TTSProvider {
    @Override
    public byte[] generateMinionSpeech(String text, String voice, Float pitchShift, Float speedFactor, Float formantShift) throws Exception {
        return ChatterboxTTS.generateSpeech(text, voice);
    }

    @Override
    public byte[] generateSpeech(String text) throws Exception {
        return ChatterboxTTS.generateSpeech(text);
    }

    @Override
    public byte[] generateSpeech(String text, String voice) throws Exception {
        return ChatterboxTTS.generateSpeech(text, voice);
    }

    @Override
    public String[] getAvailableVoices() throws Exception {
        return ChatterboxTTS.getVoices();
    }

    @Override
    public boolean isHealthy() throws Exception {
        try {
            ChatterboxTTS.getVoices();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String[] listLanguages() throws Exception {
        return ChatterboxTTS.getLanguages();
    }
}
