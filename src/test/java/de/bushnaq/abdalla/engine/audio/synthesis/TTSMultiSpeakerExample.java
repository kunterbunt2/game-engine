package de.bushnaq.abdalla.engine.audio.synthesis;

import de.bushnaq.abdalla.engine.audio.*;
import de.bushnaq.abdalla.engine.audio.synthesis.util.TTSBase;

public class TTSMultiSpeakerExample extends TTSBase {
    private static TtsHealthInfo getTtsHealthInfo() throws Exception {
        System.out.println("=== Checking TTS Service Health ===");
        TtsHealthInfo health = CoquiTTS.getHealth();
        System.out.println("Health status: " + health.getStatus());
        System.out.println("Model loaded: " + health.isModelLoaded());
        System.out.println("Current model: " + health.getCurrentModel());
        return health;
    }

    private static void loadVocoder(String currentModel, String vocoderName) throws Exception {
        // Example 3: Try loading a model with vocoder
        System.out.println("\n=== Loading Model with Vocoder ===");
        try {
            String vocoderResult = CoquiTTS.loadModel(currentModel, vocoderName, true);// Use GPU if available
            System.out.println("Model+Vocoder load result: " + vocoderResult);
        } catch (Exception e) {
            System.out.println("Could not load model with vocoder: " + e.getMessage());
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
        try {

            // List available models
            System.out.println("\n=== Listing Available Models ===");
            TtsModelList models = CoquiTTS.listModels();
            System.out.println("Found " + models.getModels().size() + " models:");
            for (String model : models.getModels()) {
                System.out.println("  - " + model);
            }

            String        loadResult   = CoquiTTS.loadModel("tts_models/en/vctk/vits");
            TtsHealthInfo health       = getTtsHealthInfo();
            String        currentModel = health.getCurrentModel();
            for (int i = 0; i < 5; i++) {
                speak(currentModel);
            }

//"tts_models/en/ljspeech/tacotron2-DDC"

            System.out.println("\n=== TTS Example Complete ===");

        } catch (Exception e) {
            System.err.println("Error in TTS example: " + e.getMessage());
            throw e;
        }
    }

    private static void speak(String currentModel) throws Exception {
        System.out.println("---------------------------------------------------");

        // list available vocoders
        System.out.println("=== Listing Available Vocoders ===");
        TtsVocoderList vocoders = CoquiTTS.listVocoders();
        System.out.println("Found " + vocoders.getVocoderCount() + " vocoders:");
        for (String vocoder : vocoders.getVocoders()) {
            System.out.println("  - " + vocoder);
        }

        String vocoder = vocoders.getVocoders().get((int) (Math.random() * vocoders.getVocoderCount()));
        //"vocoder_models/en/ljspeech/hifigan_v2"
        loadVocoder(currentModel, vocoder);


        // Check available speakers for the current model
        System.out.println("\n=== Checking Available Speakers ===");
        TtsSpeakerInfo speakerInfo = CoquiTTS.listSpeakers();
        System.out.println("Current model: " + speakerInfo.getCurrentModel());
        System.out.println("Multi-speaker support: " + speakerInfo.isMultiSpeaker());
        System.out.println("Number of speakers: " + speakerInfo.getSpeakerCount());
        if (speakerInfo.getSpeakerCount() > 0) {
            System.out.println("Available speakers: " + speakerInfo.getSpeakers());
        }

        // Check available languages for the current model
        System.out.println("\n=== Checking Available Languages ===");
        TtsLanguageInfo languageInfo = CoquiTTS.listLanguages();
        System.out.println("Current model: " + languageInfo.getCurrentModel());
        System.out.println("Multi-lingual support: " + languageInfo.isMultiLingual());
        System.out.println("Number of languages: " + languageInfo.getLanguageCount());
        if (languageInfo.getLanguageCount() > 0) {
            System.out.println("Available languages: " + languageInfo.getLanguages());
        }

        // Example 1: Use default model with different speakers (if supported)
        System.out.println("\n=== Generating Speech with Different Speakers ===");

        // Basic speech generation
        String language = null;
        String speaker  = null;
        if (languageInfo.getLanguageCount() > 0) {
            language = languageInfo.getLanguages().get((int) (Math.random() * languageInfo.getLanguageCount()));
        }
        if (speakerInfo.isMultiSpeaker() && speakerInfo.getSpeakerCount() > 1) {
            speaker = speakerInfo.getSpeakers().get((int) (Math.random() * speakerInfo.getSpeakerCount()));
        }

        String text;
        if (speaker != null && language != null)
            text = "Hello, this speech uses model " + currentModel + "is using speaker " + speaker + " and language " + language + ".";
        else if (speaker != null)
            text = "Hello, this speech uses model " + currentModel + "is using speaker " + speaker + ".";
        else
            text = "Hello, this speech uses model " + currentModel + "is using language " + language + ".";
        System.out.println(text);

        byte[] speakerSpeech = CoquiTTS.generateSpeech(text, speaker, language);
        CoquiTTS.writeWav(speakerSpeech, "basic_speech.wav");
        playBlocking("basic_speech.wav");

        System.out.println("Generated basic speech: " + speakerSpeech.length + " bytes");
        System.out.println("---------------------------------------------------");
    }
}
