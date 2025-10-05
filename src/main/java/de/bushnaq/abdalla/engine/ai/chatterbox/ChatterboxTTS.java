package de.bushnaq.abdalla.engine.ai.chatterbox;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ChatterboxTTS {
    private static final ObjectMapper objectMapper    = new ObjectMapper();
    private static       String       TTS_SERVICE_URL = "http://localhost:4123";

    public static byte[] generateSpeech(String text) throws Exception {
        return generateSpeech(text, null);
    }

    public static byte[] generateSpeech(String text, String voice) throws Exception {
        URL               url  = new URL(TTS_SERVICE_URL + "/v1/audio/speech");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        java.util.Map<String, Object> jsonMap = new java.util.HashMap<>();
        jsonMap.put("input", text); // Chatterbox API expects 'input' not 'text'
        jsonMap.put("exaggeration", "2");
        jsonMap.put("cfg_weight", "0.3");
        // If the API supports voice selection, add it here:
        if (voice != null && !voice.trim().isEmpty()) {
            jsonMap.put("voice", voice);
        }
        String jsonPayload = objectMapper.writeValueAsString(jsonMap);
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String error = readProcessOutput(conn.getErrorStream());
            throw new RuntimeException("Chatterbox TTS service returned error " + responseCode + ": " + error);
        }
        try (InputStream is = conn.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int    bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    public static String[] getLanguages() throws Exception {
        URL               url  = new URL(TTS_SERVICE_URL + "/languages");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String error = readProcessOutput(conn.getErrorStream());
            throw new RuntimeException("Failed to get languages " + responseCode + ": " + error);
        }
        String response = readProcessOutput(conn.getInputStream());
        // Parse as Map and extract 'languages' array
        java.util.Map<?, ?> map          = objectMapper.readValue(response, java.util.Map.class);
        Object              languagesObj = map.get("languages");
        if (languagesObj instanceof List<?> languagesList) {
            List<String> codes = new java.util.ArrayList<>();
            for (Object langObj : languagesList) {
                if (langObj instanceof java.util.Map<?, ?> langMap) {
                    Object code = langMap.get("code");
                    if (code != null) codes.add(code.toString());
                }
            }
            return codes.toArray(new String[0]);
        } else {
            throw new RuntimeException("Unexpected response format: 'languages' field missing or not a list");
        }
    }

    public static String[] getVoices() throws Exception {
        URL               url  = new URL(TTS_SERVICE_URL + "/voices");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String error = readProcessOutput(conn.getErrorStream());
            throw new RuntimeException("Failed to get voices " + responseCode + ": " + error);
        }
        String response = readProcessOutput(conn.getInputStream());
        // Parse as Map and extract voices array
        java.util.Map<?, ?> map       = objectMapper.readValue(response, java.util.Map.class);
        Object              voicesObj = map.get("voices");
        if (voicesObj instanceof List<?> voicesList) {
            String[] voices = voicesList.stream().map(Object::toString).toArray(String[]::new);
            return voices;
        } else {
            throw new RuntimeException("Unexpected response format: 'voices' field missing or not a list");
        }
    }

    private static String readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().reduce("", (a, b) -> a + "\n" + b);
        }
    }

    public static void setServiceUrl(String url) {
        TTS_SERVICE_URL = url;
    }

    public static void writeWav(byte[] audioData, String fileName) throws IOException {
        Path outputPath = Paths.get(fileName);
        Files.write(outputPath, audioData);
        System.out.printf("Audio saved to '%s'.\n", outputPath.toAbsolutePath());
    }
}
