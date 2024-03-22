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
import com.badlogic.gdx.files.FileHandle;

import java.nio.ByteBuffer;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;

public class OggPlayer extends AbstractAudioProducer {
    static private final int        bufferSize     = 4096 * 10;
    static private final int        bytesPerSample = 2;
    protected            FileHandle file;
    int channels;
    private int format, sampleRate;
    private OggInputStream input;
    private boolean        loop;
    private OggInputStream previousInput;
    private float          renderedSeconds, maxSecondsPerBuffer;

    public OggPlayer() {
        setAmbient(true);//always follows camera
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
//        if (input == null) {
//            input = new OggInputStream(file.read());
//            setup(input.getChannels(), input.getSampleRate());
//            previousInput = null; // release this reference
//        }
        for (int i = 0; i < byteBuffer.capacity(); i++) {
            int value = input.read();
            if (value == -1) {
                if (loop) {
                    loop();
                    value = input.read();
                } else {
                    break;
                }
            }
            byteBuffer.put(i, (byte) value);
        }
    }

    private void loop() {
        input = new OggInputStream(file.read(), input);
    }

//    public void reset() {
//        StreamUtils.closeQuietly(input);
//        previousInput = null;
//        input         = null;
//    }

    public void setFile(final FileHandle file) throws OpenAlException {
        this.file = file;
        input     = new OggInputStream(file.read());
        channels  = input.getChannels();
        setup(input.getChannels(), input.getSampleRate());
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    protected void setup(final int channels, final int sampleRate) {
        this.format         = channels > 1 ? AL_FORMAT_STEREO16 : AL_FORMAT_MONO16;
        this.sampleRate     = sampleRate;
        maxSecondsPerBuffer = (float) bufferSize / (bytesPerSample * channels * sampleRate);
    }

}
