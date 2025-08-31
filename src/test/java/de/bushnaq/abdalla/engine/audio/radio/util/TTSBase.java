package de.bushnaq.abdalla.engine.audio.radio.util;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class TTSBase {
    private static final   int     MAX_STARTUP_WAIT_SECONDS = 300; // Increased to 5 minutes for build time
    protected static final String  TTS_SERVICE_URL          = "http://localhost:5000";
    private                Process dockerProcess;

    protected void buildAndStartDockerContainer() throws Exception {
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

    protected static void buildContainer() throws IOException, InterruptedException {
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

    protected void cleanupExistingContainers() throws IOException, InterruptedException {
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

    protected void cleanupStoppedContainers() throws IOException, InterruptedException {
        System.out.println("Cleaning up stopped TTS containers...");
        ProcessBuilder pruneBuilder = new ProcessBuilder(
                "docker", "container", "prune", "-f", "--filter", "name=game-engine-tts-server"
        );
        pruneBuilder.directory(new File("."));
        Process pruneProcess = pruneBuilder.start();
        pruneProcess.waitFor();
    }

    // Optional: Add a method to force cleanup if needed
    protected void forceCleanup() throws Exception {
        System.out.println("Force stopping and removing TTS Docker container...");
        ProcessBuilder downBuilder = new ProcessBuilder(
                "docker-compose", "-f", "docker-compose-tts.yml", "down"
        );
        downBuilder.directory(new File("."));
        Process downProcess = downBuilder.start();
        downProcess.waitFor();
        System.out.println("Docker container removed");
    }

    protected boolean isContainerExists() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("docker", "ps", "-a", "--filter", "name=game-engine-tts-server", "--format", "{{.Names}}");
        pb.directory(new File("."));
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String output = reader.readLine();
            process.waitFor();
            return output != null && output.contains("game-engine-tts-server");
        }
    }

    protected boolean isContainerRunning() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("docker", "ps", "--filter", "name=game-engine-tts-server", "--format", "{{.Names}}");
        pb.directory(new File("."));
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String output = reader.readLine();
            process.waitFor();
            return output != null && output.contains("game-engine-tts-server");
        }
    }

    protected boolean isImageExists() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("docker", "images", "--filter", "reference=game-engine-tts-server", "--format", "{{.Repository}}");
        pb.directory(new File("."));
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String output = reader.readLine();
            process.waitFor();
            return output != null && output.contains("game-engine-tts-server");
        }
    }

    protected static void playBlocking(String fileName) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(new File(fileName)));
            clip.start();
            while (!clip.isRunning())
                Thread.sleep(10);
            while (clip.isRunning())
                Thread.sleep(10);
            clip.close();
        } catch (Exception exc) {
            exc.printStackTrace(System.out);
        }
    }

    protected String readProcessOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().reduce("", (a, b) -> a + "\n" + b);
        }
    }

    protected void startContainer() throws IOException, InterruptedException {
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

    protected void startExistingContainer() throws IOException, InterruptedException {
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

    protected void stopDockerContainer() throws Exception {
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

    protected void waitForServiceToBeReady() throws Exception {
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
