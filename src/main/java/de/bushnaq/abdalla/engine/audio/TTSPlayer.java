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
    private final AudioEngine        audioEngine;
    private final Logger             logger     = LoggerFactory.getLogger(this.getClass());
    private final List<String>       messages   = new ArrayList<>();
    protected     FileHandle         file;
    private       int                channels   = 1;
    private       int                format     = AL_FORMAT_MONO16;
    private       Wav.WavInputStream input;
    private       boolean            optIn      = false;//by default ttsPlayer is opting out, which means that it is disabled by  the AudioEngine
    private       int                sampleRate = 16000;//default for tts

    public TTSPlayer(AudioEngine audioEngine) {
        setAmbient(true);//always follows camera
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
    public int getSamplerate() {
        return sampleRate;
    }

    @Override
    public void processBuffer(final ByteBuffer byteBuffer) {
        if (input == null) {
            if (!bufferNextMessage()) {
                fastZero(byteBuffer);
                return;
            }
        }
        for (int i = 0; i < byteBuffer.capacity(); i++) {
            int value;
            try {
                value = input.read();
                if (value == -1) {
                    if (!bufferNextMessage()) {
                        input = null;
//                    logger.info("break");
                        fastZero(byteBuffer);
                        return;
                    } else {
                        value = input.read();
                    }
                }
                byteBuffer.put(i, (byte) value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Override
    public boolean isOptIn() {
        return optIn;
    }

    public void reset() {
        StreamUtils.closeQuietly(input);
        input = null;
    }

    public void setOptIn(boolean optIn) {
        this.optIn = optIn;
    }

    protected void setup(final int channels, final int sampleRate) {
        this.format     = channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
        this.sampleRate = sampleRate;
    }

    public void speak(String msg) {
        List<String> tokens = audioEngine.radioTTS.tokenize(msg);
        messages.addAll(tokens);
    }

}
