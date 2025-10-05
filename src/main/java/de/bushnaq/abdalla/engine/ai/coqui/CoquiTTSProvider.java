package de.bushnaq.abdalla.engine.ai.coqui;

import de.bushnaq.abdalla.engine.ai.TTSProvider;

public class CoquiTTSProvider implements TTSProvider {
    @Override
    public byte[] generateMinionSpeech(String text, String voice, Float pitchShift, Float speedFactor, Float formantShift) throws Exception {
        return CoquiTTS.generateMinionSpeech(text, Integer.parseInt(voice), pitchShift, speedFactor, formantShift);
    }

    @Override
    public byte[] generateSpeech(String text) throws Exception {
        return CoquiTTS.generateSpeech(text);
    }

    @Override
    public byte[] generateSpeech(String text, String voice) throws Exception {
        return CoquiTTS.generateSpeech(text, voice, null);
    }

    @Override
    public String[] getAvailableVoices() throws Exception {
        return CoquiTTS.listSpeakers().getSpeakers().toArray(new String[0]);
    }

    @Override
    public boolean isHealthy() throws Exception {
        try {
            CoquiTTS.getHealth();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String[] listLanguages() throws Exception {
        return CoquiTTS.listLanguages().getLanguages().toArray(new String[0]);
    }
}
