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
    private final Logger             logger   = LoggerFactory.getLogger(this.getClass());
    private final List<String>       messages = new ArrayList<>();
    protected     FileHandle         file;
    private       int                channels = 1;
    private       int                format   = AL_FORMAT_MONO16;
    //    private       float              highGain = 0.0f;
    private       Wav.WavInputStream input;
    //    private       float              lowGain  = 1.0f;
    private       boolean            optIn    = false;//by default ttsPlayer is opting out, which means that it is disabled by  the AudioEngine
//    private       int                sampleRate = 16000;//default for tts

    public TTSPlayer(AudioEngine audioEngine) throws OpenAlException {
        super(16000);
        setAmbient(true);//always follows camera
        setRadio(true);//radio effect
        this.audioEngine = audioEngine;
        filters.highGain = 1.0f;
        filters.lowGain  = 0.05f;
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

    public void enable(final OpenAlSource source) throws OpenAlException {
        enabled     = true;
        this.source = source;
        this.source.attach(this);
        this.source.setGain(gain);
        this.source.updateFilter(true, filters.lowGain, filters.highGain);
//        filters.setFilter(true);
        if (isPlaying())
            this.source.play();//we should be playing
        this.source.unparkOrStartThread();
    }

    @Override
    public boolean isOptIn() {
        return optIn;
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
            if (!bufferNextMessage()) {
                fastZero(byteBuffer);
                return;
            }
        }
        for (int i = 0; i < byteBuffer.capacity() / 2; i += 1) {
            int byte1;
            int byte2;
            try {
                byte1 = input.read();
                byte2 = input.read();
                if (byte2 == -1) {
                    if (!bufferNextMessage()) {
                        input = null;
//                    logger.info("break");
                        fastZero(byteBuffer);
                        return;
                    } else {
                        byte1 = input.read();
                        byte2 = input.read();
                    }
                }
//                if (filters.filter != null) {
//                    int   intValue1   = byte1 + (byte2 << 8);
//                    float floatValue1 = (float) intValue1 / 32768 - 1f;
//                    float floatValue2 = filters.filter.process(floatValue1);
//                    int   intValue2   = (int) ((floatValue2 + 1f) * 32768);
//                    intValue2 = Math.max(intValue2, 0);
//                    intValue2 = (int) Math.min((long) intValue2, 256L * 256L);
//                    int nbyte2 = intValue2 >> 8;
//                    int nbyte1 = intValue2 & 0xff;
//                    if (nbyte1 != byte1)
//                        logger.info(String.format("%d %d", byte1, nbyte1));
//                    if (nbyte2 != byte2)
//                        logger.info(String.format("%d %d", byte2, nbyte2));
//                    logger.info(String.format("%d %d %d %f %f", byte1, byte2, intValue1, floatValue1, floatValue2));
//                    byteBuffer.put(i * 2, (byte) nbyte1);
//                    byteBuffer.put(i * 2 + 1, (byte) nbyte2);
//                } else
                {
                    byteBuffer.put(i * 2, (byte) byte1);
                    byteBuffer.put(i * 2 + 1, (byte) byte2);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void reset() {
        StreamUtils.closeQuietly(input);
        input = null;
    }

    public void setOptIn(boolean optIn) {
        this.optIn = optIn;
    }

    protected void setup(final int channels, final int sampleRate) {
        this.format = channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
//        this.sampleRate = sampleRate;
    }

    public void speak(String msg) {
        List<String> tokens = audioEngine.radioTTS.tokenize(msg);
        messages.addAll(tokens);
    }

}
