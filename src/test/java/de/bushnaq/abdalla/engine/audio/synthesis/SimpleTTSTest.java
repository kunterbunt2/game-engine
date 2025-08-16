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

package de.bushnaq.abdalla.engine.audio.synthesis;

import de.bushnaq.abdalla.engine.audio.CoquiTTS;
import de.bushnaq.abdalla.engine.audio.synthesis.util.TTSBase;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SimpleTTSTest extends TTSBase {

    private static final int     MAX_STARTUP_WAIT_SECONDS = 300; // Increased to 5 minutes for build time
    private static final String  TTS_SERVICE_URL          = "http://localhost:5000";
    private              Process dockerProcess;

    private void buildAndStartDockerContainer() throws Exception {
        // Check if container is already running
        if (isContainerRunning()) {
            System.out.println("TTS container is already running. Reusing existing container.");
            return;
        }

        // Check if container exists but is stopped
        if (isContainerExists()) {
            System.out.println("TTS container exists but is stopped. Starting existing container...");
            startExistingContainer();
            return;
        }

        // Check if Docker image exists
        if (isImageExists()) {
            System.out.println("TTS Docker image exists. Creating new container...");
            startContainer();
            return;
        }

        // No image exists, need to build
        System.out.println("No TTS Docker image found. Building new image...");
        buildContainer();
        startContainer();
    }

    private static void buildContainer() throws IOException, InterruptedException {
        // Build the Docker image first
        System.out.println("Building Docker image (this may take a few minutes on first run)...");
        ProcessBuilder buildBuilder = new ProcessBuilder(
                "docker-compose", "-f", "docker-compose-tts.yml", "build"
        );
        buildBuilder.directory(new File("."));

        // Redirect output so we can see build progress
        buildBuilder.redirectErrorStream(true);
        Process buildProcess = buildBuilder.start();

        // Monitor build progress
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(buildProcess.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("BUILD: " + line);
                }
            } catch (IOException e) {
                // Ignore
            }
        });
        outputThread.start();

        int buildExitCode = buildProcess.waitFor();
        outputThread.join(5000); // Wait max 5 seconds for output thread to finish

        if (buildExitCode != 0) {
            throw new RuntimeException("Failed to build Docker image. Exit code: " + buildExitCode);
        }

        System.out.println("Docker image built successfully. Starting container...");
    }

    private void cleanupExistingContainers() throws IOException, InterruptedException {
        // Only clean up if there's actually a port conflict
        // First check if our expected container is using the port
        if (isContainerRunning()) {
            System.out.println("Our TTS container is already running on port 5000. No cleanup needed.");
            return;
        }

        // Check if there are any other containers using port 5000
        ProcessBuilder checkPortBuilder = new ProcessBuilder(
                "docker", "ps", "--filter", "publish=5000", "--format", "{{.Names}}"
        );
        checkPortBuilder.directory(new File("."));
        Process checkProcess = checkPortBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(checkProcess.getInputStream()))) {
            String conflictingContainer = reader.readLine();
            checkProcess.waitFor();

            if (conflictingContainer != null && !conflictingContainer.trim().isEmpty()) {
                System.out.println("Found conflicting container using port 5000: " + conflictingContainer);
                System.out.println("Stopping conflicting container to free port 5000...");

                // Stop only the conflicting container
                ProcessBuilder stopBuilder = new ProcessBuilder("docker", "stop", conflictingContainer.trim());
                stopBuilder.directory(new File("."));
                Process stopProcess = stopBuilder.start();
                stopProcess.waitFor();

                System.out.println("Conflicting container stopped. Port 5000 is now available.");
            } else {
                System.out.println("No containers are using port 5000. No cleanup needed.");
            }
        }
    }

    private void cleanupStoppedContainers() throws IOException, InterruptedException {
        System.out.println("Cleaning up stopped TTS containers...");
        ProcessBuilder pruneBuilder = new ProcessBuilder(
                "docker", "container", "prune", "-f", "--filter", "name=game-engine-tts-server"
        );
        pruneBuilder.directory(new File("."));
        Process pruneProcess = pruneBuilder.start();
        pruneProcess.waitFor();
    }

    // Optional: Add a method to force cleanup if needed
    public void forceCleanup() throws Exception {
        System.out.println("Force stopping and removing TTS Docker container...");
        ProcessBuilder downBuilder = new ProcessBuilder(
                "docker-compose", "-f", "docker-compose-tts.yml", "down"
        );
        downBuilder.directory(new File("."));
        Process downProcess = downBuilder.start();
        downProcess.waitFor();
        System.out.println("Docker container removed");
    }

    private boolean isContainerExists() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("docker", "ps", "-a", "--filter", "name=game-engine-tts-server", "--format", "{{.Names}}");
        pb.directory(new File("."));
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String output = reader.readLine();
            process.waitFor();
            return output != null && output.contains("game-engine-tts-server");
        }
    }

    private boolean isContainerRunning() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("docker", "ps", "--filter", "name=game-engine-tts-server", "--format", "{{.Names}}");
        pb.directory(new File("."));
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String output = reader.readLine();
            process.waitFor();
            return output != null && output.contains("game-engine-tts-server");
        }
    }

    private boolean isImageExists() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("docker", "images", "--filter", "reference=game-engine-tts-server", "--format", "{{.Repository}}");
        pb.directory(new File("."));
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String output = reader.readLine();
            process.waitFor();
            return output != null && output.contains("game-engine-tts-server");
        }
    }

    private String readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().reduce("", (a, b) -> a + "\n" + b);
        }
    }

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

    private void startContainer() throws IOException, InterruptedException {
        // Check if we should use docker-compose up or docker start
        if (isContainerExists() && !isContainerRunning()) {
            // Container exists but is stopped, restart it
            System.out.println("Restarting existing stopped container...");
            ProcessBuilder restartBuilder = new ProcessBuilder(
                    "docker-compose", "-f", "docker-compose-tts.yml", "start"
            );
            restartBuilder.directory(new File("."));
            dockerProcess = restartBuilder.start();

            int restartExitCode = dockerProcess.waitFor();
            if (restartExitCode != 0) {
                String error = readProcessOutput(dockerProcess.getErrorStream());
                throw new RuntimeException("Failed to restart Docker container: " + error);
            }
        } else {
            // Start the container with docker-compose up
            ProcessBuilder startBuilder = new ProcessBuilder(
                    "docker-compose", "-f", "docker-compose-tts.yml", "up", "-d"
            );
            startBuilder.directory(new File("."));
            dockerProcess = startBuilder.start();

            int startExitCode = dockerProcess.waitFor();
            if (startExitCode != 0) {
                String error = readProcessOutput(dockerProcess.getErrorStream());
                throw new RuntimeException("Failed to start Docker container: " + error);
            }
        }

        System.out.println("Docker container started successfully");
    }

    private void startExistingContainer() throws IOException, InterruptedException {
        System.out.println("Starting existing TTS container...");
        ProcessBuilder startBuilder = new ProcessBuilder(
                "docker-compose", "-f", "docker-compose-tts.yml", "start"
        );
        startBuilder.directory(new File("."));
        Process startProcess = startBuilder.start();

        int startExitCode = startProcess.waitFor();
        if (startExitCode != 0) {
            String error = readProcessOutput(startProcess.getErrorStream());
            throw new RuntimeException("Failed to start existing Docker container: " + error);
        }

        System.out.println("Existing Docker container started successfully");
    }

    private void stopDockerContainer() throws Exception {
        if (dockerProcess != null && dockerProcess.isAlive()) {
            dockerProcess.destroy();
        }

        // Stop the container using docker-compose stop (keeps container for reuse)
        // Use 'stop' instead of 'down' to preserve the container
        ProcessBuilder stopBuilder = new ProcessBuilder(
                "docker-compose", "-f", "docker-compose-tts.yml", "stop"
        );
        stopBuilder.directory(new File("."));
        Process stopProcess = stopBuilder.start();
        stopProcess.waitFor();

        System.out.println("Docker container stopped (but preserved for future use)");
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
        String[] testText = {"Hello, this is a test of the Coqui TTS system running in Docker.", " This is a longer sentence to ensure we can handle various lengths of text.", " Let's see how it performs with different inputs."};

        for (String text : testText) {
            long time = System.currentTimeMillis();
            System.out.println("Testing TTS with text: " + text);
            // Call the TTS service

            byte[] audioData = CoquiTTS.generateSpeech(text);

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

    private void waitForServiceToBeReady() throws Exception {
        System.out.println("Waiting for TTS service to be ready...");
        System.out.println("This includes downloading the TTS model on first startup...");

        for (int i = 0; i < MAX_STARTUP_WAIT_SECONDS; i++) {
            try {
                URL               url  = new URL(TTS_SERVICE_URL + "/health");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    System.out.println("TTS service is ready!");
                    return;
                }
            } catch (Exception e) {
                // Service not ready yet, continue waiting
            }

            Thread.sleep(1000);
            if (i % 30 == 0 && i > 0) { // Log every 30 seconds instead of every 10
                System.out.println("Still waiting for TTS service... (" + i + "s elapsed)");
                // Check container logs if taking too long
                if (i > 60) {
                    try {
                        ProcessBuilder logBuilder = new ProcessBuilder("docker-compose", "-f", "docker-compose-tts.yml", "logs", "--tail", "10");
                        logBuilder.directory(new File("."));
                        Process logProcess = logBuilder.start();
                        String  logs       = readProcessOutput(logProcess.getInputStream());
                        System.out.println("Recent container logs:\n" + logs);
                    } catch (Exception logEx) {
                        // Ignore log errors
                    }
                }
            }
        }

        throw new RuntimeException("TTS service did not become ready within " + MAX_STARTUP_WAIT_SECONDS + " seconds");
    }

}
