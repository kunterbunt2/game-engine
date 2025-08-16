package de.bushnaq.abdalla.engine.audio;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CoquiTTS {
    private static final int    DEFAULT_SAMPLE_RATE      = 22050; // Default sample rate for better quality
    private static final int    MAX_STARTUP_WAIT_SECONDS = 300; // Increased to 5 minutes for build time
    private static final String TTS_SERVICE_URL          = "http://localhost:5000";

    public static byte[] generateSpeech(String text) throws Exception {
        return generateSpeech(text, DEFAULT_SAMPLE_RATE);
    }

    public static byte[] generateSpeech(String text, int sampleRate) throws Exception {
        URL               url  = new URL(TTS_SERVICE_URL + "/speak");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Set up the request
        System.out.println("Connecting to TTS service at " + TTS_SERVICE_URL + " with sample rate " + sampleRate + "Hz");
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Send the JSON payload with sample rate
        String jsonPayload = "{\"text\":\"" + text.replace("\"", "\\\"") + "\",\"sample_rate\":" + sampleRate + "}";
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Check response code
        int responseCode = conn.getResponseCode();
        System.out.println("TTS service response code: " + responseCode);
        if (responseCode != 200) {
            String error = readProcessOutput(conn.getErrorStream());
            throw new RuntimeException("TTS service returned error " + responseCode + ": " + error);
        }

        // Read the audio data
        try (InputStream is = conn.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int    bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }

            byte[] byteArray = baos.toByteArray();
            System.out.println("TTS audio data received successfully " + byteArray.length + " bytes at " + sampleRate + "Hz.");
            return byteArray;
        }
    }

    private static String readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().reduce("", (a, b) -> a + "\n" + b);
        }
    }

}
