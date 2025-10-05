package de.bushnaq.abdalla.engine.ai;

import de.bushnaq.abdalla.engine.ai.chatterbox.ChatterboxTTSProvider;
import de.bushnaq.abdalla.engine.ai.coqui.CoquiTTSProvider;
import lombok.Getter;

public class TTSManager {
    @Getter
    private TTSEngine   currentEngine;
    private TTSProvider currentProvider;

    public TTSManager() {
        setEngine(TTSEngine.COQUI);
    }

    public byte[] generateMinionSpeech(String text, String voice, Float pitchShift, Float speedFactor, Float formantShift) throws Exception {
        return currentProvider.generateMinionSpeech(text, voice, pitchShift, speedFactor, formantShift);
    }

    public byte[] generateMinionSpeech(String text, Float pitchShift, Float speedFactor, Float formantShift) throws Exception {
        return currentProvider.generateSpeech(text);
    }

    public byte[] generateMinionSpeech(String text, String speaker, String language, Float pitchShift, Float speedFactor, Float formantShift) throws Exception {
        return currentProvider.generateSpeech(text);
    }

    public byte[] generateSpeech(String text) throws Exception {
        return currentProvider.generateSpeech(text);
    }

    public byte[] generateSpeech(String text, String voice) throws Exception {
        return currentProvider.generateSpeech(text, voice);
    }

    public byte[] generateSpeech(String text, int speakerId) throws Exception {
        return currentProvider.generateSpeech(text);
    }

    public byte[] generateSpeech(String text, String speaker, String language) throws Exception {
        return currentProvider.generateSpeech(text);
    }

    public String[] getAvailableVoices() throws Exception {
        return currentProvider.getAvailableVoices();
    }

    public boolean isCurrentEngineHealthy() throws Exception {
        return currentProvider.isHealthy();
    }

    public String[] listLanguages() throws Exception {
        return currentProvider.listLanguages();
    }

    public void setEngine(TTSEngine engine) {
        this.currentEngine = engine;
        switch (engine) {
            case COQUI:
                this.currentProvider = new CoquiTTSProvider();
                break;
            case CHATTERBOX:
                this.currentProvider = new ChatterboxTTSProvider();
                break;
        }
    }

    public enum TTSEngine {
        COQUI, CHATTERBOX
    }
}
