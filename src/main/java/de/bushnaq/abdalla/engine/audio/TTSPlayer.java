/*
 * Copyright (C) 2024 Abdalla Bushnaq
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bushnaq.abdalla.engine.audio;

import com.badlogic.gdx.backends.lwjgl3.audio.Wav;
import com.badlogic.gdx.files.FileHandle;
import de.bushnaq.abdalla.engine.ai.coqui.CoquiTTS;
import de.bushnaq.abdalla.engine.ai.coqui.TtsEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;

public class TTSPlayer extends AbstractAudioProducer {
    private       int                      arrayIndex   = 0;
    private final AudioEngine              audioEngine;
    private       byte[]                   bytes        = null;
    private final int                      channels     = 1;
    // Configurable sample rate for COQUI TTS
//    private       int         coquiSampleRate = 22050; // Default to 22050Hz for better quality
    private       RadioMessage             currentRadioMessage;//currently playing
    protected     FileHandle               file;
    private       int                      format       = AL_FORMAT_MONO16;
    private final DateTimeFormatter        formatter    = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    //    private       float              highGain = 0.0f;
    private       Wav.WavInputStream       input;
    private final Logger                   logger       = LoggerFactory.getLogger(this.getClass());
    private final List<RadioMessage>       messages     = new ArrayList<>();
    private final Object                   messagesLock = new Object(); // Lock for thread-safe access to messages
    //    private       float              lowGain  = 1.0f;
    private       boolean                  optIn        = false;//by default ttsPlayer is opting out, which means that it is disabled by  the AudioEngine
    // Thread management for silent message checking
    private       ScheduledExecutorService silentMessageChecker;
    private       long                     startTime;//time we started rendering
    private       long                     stopTime;
    //    private       int                sampleRate = 16000;//default for tts
    private final TtsEngine                ttsEngine    = TtsEngine.COQUI;


    public TTSPlayer(AudioEngine audioEngine) throws OpenAlException {
        super(22050);
        setAmbient(true);//always follows camera
        setRadio(true);//radio effect
        this.audioEngine = audioEngine;
//        filters.highGain = 1.0f;
//        filters.lowGain  = 0.05f;
        File eventsFile = new File("radio.txt");
        if (eventsFile.exists()) {
            boolean deleted = eventsFile.delete();
            if (!deleted) {
                logger.warn("Could not delete existing radio.txt file");
            }
        }
        startSilentMessageChecker();
    }

    private boolean bufferNextMessage() {
        synchronized (messagesLock) {
            if (messages.isEmpty())
                return false;
            RadioMessage firstMessage = messages.get(0);
            currentRadioMessage = messages.remove(0);
        }
//        logger.info(String.format("TTS: %s to %s", currentRadioMessage.message, currentRadioMessage.to.getName()));
        switch (ttsEngine) {
//            case FREETTS:
//                file = audioEngine.radio.getFileHandle(msg.message);
//                input = new Wav.WavInputStream(file);
//                setup(input.channels, input.sampleRate);
//                break;
            case COQUI:
                try {
                    writeRadioToFile(currentRadioMessage);
                    arrayIndex = 0;
                    byte[] wavFileBytes = CoquiTTS.generateSpeech(currentRadioMessage.message, currentRadioMessage.from.getId());
                    bytes = extractAudioDataFromWav(wavFileBytes);
//                    System.out.println("TTS: " + msg + " (extracted " + bytes.length + " audio bytes from " + wavFileBytes.length + " total bytes)");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                logger.error("Unknown TTS engine: " + ttsEngine);
                return false;
        }
        return true;
    }

    /**
     * Check if the first message in the queue is a silent message that has expired
     */
    private void checkSilentMessages() {
        synchronized (messagesLock) {
            if (startTime == 0) {
                if (!messages.isEmpty()) {
                    startTime = System.currentTimeMillis();
                    stopTime  = startTime + messages.getFirst().message.length() * 120L;//estimate time to speak the message
                }
            } else {
                // Calculate the end time for this silent message
                long currentTime = System.currentTimeMillis();
                if (stopTime < currentTime) {
                    // Silent message has expired, remove it
                    if (!messages.isEmpty()) {
                        RadioMessage msg = messages.removeFirst();
//                        logger.debug("Removed expired silent message: {} (expired at {}ms, current time {}ms)", msg.message, stopTime, currentTime);
                        // Notify the partner that the silent message is finished
                        if (msg.to != null) {
                            msg.to.notifyFinishedTalking(msg);
                        }
                        startTime = 0;
                    }
                }

            }
        }
    }

    @Override
    public OpenAlSource disable() throws OpenAlException {
        final OpenAlSource sourceBuffer = super.disable();
        startSilentMessageChecker();
        return sourceBuffer;
    }

    @Override
    public void enable(final OpenAlSource source) throws OpenAlException {
        enabled     = true;
        this.source = source;
        this.source.attach(this);
        this.source.setGain(gain);
        this.source.updateFilter(true, 0.02f, 1.0f);
//        filters.setFilter(true);
        if (isPlaying())
            this.source.play();//we should be playing
        this.source.unparkOrStartThread();
        stopSilentMessageChecker();
    }

    /**
     * Extract audio data from WAV file bytes, skipping the header
     */
    private byte[] extractAudioDataFromWav(byte[] wavFileBytes) {
        if (wavFileBytes.length < 44) {
            throw new RuntimeException("WAV file too small, expected at least 44 bytes for header");
        }

        // Check for "RIFF" signature at the beginning
        if (wavFileBytes[0] != 'R' || wavFileBytes[1] != 'I' || wavFileBytes[2] != 'F' || wavFileBytes[3] != 'F') {
            throw new RuntimeException("Invalid WAV file: missing RIFF signature");
        }

        // Check for "WAVE" format
        if (wavFileBytes[8] != 'W' || wavFileBytes[9] != 'A' || wavFileBytes[10] != 'V' || wavFileBytes[11] != 'E') {
            throw new RuntimeException("Invalid WAV file: missing WAVE format");
        }

        // Parse WAV format information
        int sampleRate    = 0;
        int numChannels   = 0;
        int bitsPerSample = 0;

        // Find the "fmt " and "data" chunks
        int    dataChunkOffset = 12; // Start after "RIFF" header
        byte[] audioData       = null;

        while (dataChunkOffset < wavFileBytes.length - 8) {
            // Read chunk ID (4 bytes)
            String chunkId = new String(wavFileBytes, dataChunkOffset, 4);

            // Read chunk size (4 bytes, little-endian)
            int chunkSize = (wavFileBytes[dataChunkOffset + 4] & 0xFF) | ((wavFileBytes[dataChunkOffset + 5] & 0xFF) << 8) | ((wavFileBytes[dataChunkOffset + 6] & 0xFF) << 16) | ((wavFileBytes[dataChunkOffset + 7] & 0xFF) << 24);

            if ("fmt ".equals(chunkId)) {
                // Parse format chunk
                int formatOffset = dataChunkOffset + 8;

                // Skip format tag (2 bytes)
                // Read number of channels (2 bytes, little-endian)
                numChannels = (wavFileBytes[formatOffset + 2] & 0xFF) | ((wavFileBytes[formatOffset + 3] & 0xFF) << 8);

                // Read sample rate (4 bytes, little-endian)
                sampleRate = (wavFileBytes[formatOffset + 4] & 0xFF) | ((wavFileBytes[formatOffset + 5] & 0xFF) << 8) | ((wavFileBytes[formatOffset + 6] & 0xFF) << 16) | ((wavFileBytes[formatOffset + 7] & 0xFF) << 24);

                // Skip byte rate (4 bytes) and block align (2 bytes)
                // Read bits per sample (2 bytes, little-endian)
                bitsPerSample = (wavFileBytes[formatOffset + 14] & 0xFF) | ((wavFileBytes[formatOffset + 15] & 0xFF) << 8);

//                logger.info("WAV format: " + numChannels + " channels, " + sampleRate + "Hz, " + bitsPerSample + " bits");

            } else if ("data".equals(chunkId)) {
                // Found data chunk, extract audio data
                int audioDataStart  = dataChunkOffset + 8;
                int audioDataLength = Math.min(chunkSize, wavFileBytes.length - audioDataStart);

                audioData = new byte[audioDataLength];
                System.arraycopy(wavFileBytes, audioDataStart, audioData, 0, audioDataLength);

//                logger.info("WAV header parsed: found data chunk at offset " + audioDataStart + ", audio data length: " + audioDataLength + " bytes");
            }

            // Move to next chunk
            dataChunkOffset += 8 + chunkSize;

            // Align to even byte boundary (WAV chunks are word-aligned)
            if (chunkSize % 2 == 1) {
                dataChunkOffset++;
            }
        }

        if (audioData == null) {
            throw new RuntimeException("Could not find 'data' chunk in WAV file");
        }

        // Update the audio format based on parsed WAV info
        if (sampleRate > 0 && numChannels > 0) {
            setup(numChannels, sampleRate);
        } else {
            logger.warn("Could not parse WAV format info, using defaults");
        }

        return audioData;
    }

    @Override
    public int getChannels() {
        return channels;
    }

//    public int getCoquiSampleRate() {
//        return coquiSampleRate;
//    }

    @Override
    public int getOpenAlFormat() {
        return this.format;
    }

    @Override
    public boolean isOptIn() {
        return optIn;
    }


    //    private void processWaveFile(ByteBuffer byteBuffer) {
//        if (input == null) {
//            if (!bufferNextMessage()) {
//                fastZero(byteBuffer);
//                return;
//            }
//        }
//        for (int i = 0; i < byteBuffer.capacity() / 2; i += 1) {
//            int byte1;
//            int byte2;
//            try {
//                byte1 = input.read();
//                byte2 = input.read();
//                if (byte2 == -1) {
//                    if (!bufferNextMessage()) {
//                        input = null;
//                        fastZero(byteBuffer);
//                        return;
//                    } else {
//                        byte1 = input.read();
//                        byte2 = input.read();
//                    }
//                }
//                {
//                    byteBuffer.put(i * 2, (byte) byte1);
//                    byteBuffer.put(i * 2 + 1, (byte) byte2);
//                }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
    private void notifyPartner() {
        if (currentRadioMessage != null) {
//            System.out.println("TTSPLayer=" + currentRadioMessage.to.getName());
            currentRadioMessage.to.notifyFinishedTalking(currentRadioMessage);
            currentRadioMessage = null; // Clear after notifying
        }
    }

    @Override
    public void processBuffer(final ByteBuffer byteBuffer) {
        switch (ttsEngine) {
//            case FREETTS:
//                processWaveFile(byteBuffer);
//                break;
            case COQUI:
                processBytesArray(byteBuffer);
                break;
        }
    }

    private void processBytesArray(ByteBuffer byteBuffer) {
        for (int i = 0; i < byteBuffer.capacity() / 2; i += 1) {
            int byte1;
            int byte2;
            if (bytes == null || (bytes.length > 0 && arrayIndex > bytes.length - 2)) {
                if (bytes != null) {
//                    logger.info("arrayIndex=" + arrayIndex);
                    notifyPartner();
                }
                byte2 = -1;
                if (!bufferNextMessage()) {
                    input = null;
                    bytes = null;
                    fastZero(byteBuffer);
                    return;
                } else {
                    byte1 = bytes[arrayIndex++];
                    byte2 = bytes[arrayIndex++];
                }
            } else {
                byte1 = bytes[arrayIndex++];
                byte2 = bytes[arrayIndex++];
            }
            {
                byteBuffer.put(i * 2, (byte) byte1);
                byteBuffer.put(i * 2 + 1, (byte) byte2);
            }
        }
    }

//    public void reset() {
//        StreamUtils.closeQuietly(input);
//        input = null;
//    }

    /**
     * Set the sample rate for COQUI TTS generation
     * Common values: 16000 (telephone quality), 22050 (CD quality/2), 44100 (CD quality)
     */
//    public void setCoquiSampleRate(int sampleRate) {
//        this.coquiSampleRate = sampleRate;
//        logger.info("COQUI TTS sample rate set to: " + sampleRate + "Hz");
//    }
    public void setOptIn(boolean optIn) {
        this.optIn = optIn;
    }

    protected void setup(final int channels, final int samplerate) {
        this.format     = channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
        this.sampleRate = samplerate;
    }

    public void speak(RadioMessage msg) {
        switch (ttsEngine) {
//            case FREETTS:
//                List<String> tokens = audioEngine.radio.tokenize(msg);
//                messages.addAll(tokens);
//                break;
            case COQUI:
                if (!msg.silent)
                    logger.info("speak" + msg.message);
                synchronized (messagesLock) {
                    messages.add(msg);
                }
                break;
        }
    }

    /**
     * Start the background thread that checks for expired silent messages
     */
    private void startSilentMessageChecker() {
        if (silentMessageChecker == null) {
            silentMessageChecker = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "TTSPlayer-SilentMessageChecker");
                t.setDaemon(true); // Don't prevent JVM shutdown
                return t;
            });

            // Check every 100ms for expired silent messages
            silentMessageChecker.scheduleAtFixedRate(this::checkSilentMessages, 0, 100, TimeUnit.MILLISECONDS);
//            logger.info("Silent message checker thread started");
        }
    }

    /**
     * Stop the silent message checker thread
     */
    public void stopSilentMessageChecker() {
        if (silentMessageChecker != null && !silentMessageChecker.isShutdown()) {
            silentMessageChecker.shutdown();
            try {
                if (!silentMessageChecker.awaitTermination(1, TimeUnit.SECONDS)) {
                    silentMessageChecker.shutdownNow();
                }
            } catch (InterruptedException e) {
                silentMessageChecker.shutdownNow();
                Thread.currentThread().interrupt();
            }
//            logger.info("Silent message checker thread stopped");
            silentMessageChecker = null;
        }
    }

    private void writeRadioToFile(RadioMessage rm) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("radio.txt", true))) {
            String formattedEvent = String.format("%s %s->%s: %s", LocalDateTime.now().format(formatter), rm.from.getName(), rm.to.getName(), rm.message);
            writer.println(formattedEvent);
        } catch (IOException e) {
            logger.error("Failed to write event to file", e);
        }
    }
}
