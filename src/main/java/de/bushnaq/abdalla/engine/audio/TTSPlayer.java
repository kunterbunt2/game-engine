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

import com.badlogic.gdx.backends.lwjgl3.audio.OggInputStream;
import com.badlogic.gdx.backends.lwjgl3.audio.Wav;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;

public class TTSPlayer extends AbstractAudioProducer {
    static private final int         bufferSize     = 4096 * 10;
    static private final int         bytesPerSample = 2;
    private final        AudioEngine audioEngine;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected            FileHandle  file;
    int          channels;
    List<String> messages = new ArrayList<>();
    private int format, sampleRate;
    private Wav.WavInputStream input;
    private OggInputStream     previousInput;
    private float              renderedSeconds, maxSecondsPerBuffer;

    public TTSPlayer(AudioEngine audioEngine) {
        setAmbient(false);//always follows camera
        this.audioEngine = audioEngine;
    }

    private boolean bufferNextMessage() {
        if (messages.isEmpty())
            return false;
        String msg = messages.remove(0);
        logger.info(String.format("TTS: %s", msg));
        file  = audioEngine.radioTTS.getFileHandle(msg);
        input = new Wav.WavInputStream(file);
        setup(input.channels, input.sampleRate);
        previousInput = null; // release this reference
        return true;
    }

    @Override
    public int getChannels() {
        return channels;
    }

    @Override
    public int getOpenAlFormat() {
        return this.format;
    }

    @Override
    public void processBuffer(final ByteBuffer byteBuffer) {
        if (input == null) {
            bufferNextMessage();
        }
        for (int i = 0; i < byteBuffer.capacity(); i++) {
            final int value;
            try {
                value = input.read();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (value == -1) {
                if (!bufferNextMessage())
                    break;
            }
            byteBuffer.put(i, (byte) value);
        }
    }

    public void reset() {
        StreamUtils.closeQuietly(input);
        previousInput = null;
        input         = null;
    }

    public void setFile(final FileHandle file) throws OpenAlException {
        this.file = file;
        input     = new Wav.WavInputStream(file);
        channels  = input.channels;
        setup(input.channels, input.sampleRate);
    }

    protected void setup(final int channels, final int sampleRate) {
        this.format         = channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
        this.sampleRate     = sampleRate;
        maxSecondsPerBuffer = (float) bufferSize / (bytesPerSample * channels * sampleRate);
    }

    public void speak(String msg) {
        messages.add(msg);
    }
}
