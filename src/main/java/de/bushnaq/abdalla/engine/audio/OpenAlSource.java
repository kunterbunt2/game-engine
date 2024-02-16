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

import com.badlogic.gdx.math.Vector3;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EXTEfx;
import org.lwjgl.system.libc.LibCStdlib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class OpenAlSource extends Thread {
    private static final int                       BUFFER_COUNT         = 3;
    private final        int                       bits;
    private final        int[]                     bufferId             = new int[BUFFER_COUNT];
    private final        List<Integer>             bufferQueue          = new ArrayList<>(); // A quick and dirty queue of buffer objects
    private final        List<Integer>             buffersUnqueued      = new ArrayList<>(); // A quick and dirty queue of buffer objects
    private final        int                       channels;
    //	private long lastIndex = 0;
    private final        Logger                    logger               = LoggerFactory.getLogger(this.getClass());
    private final        Vector3                   position             = new Vector3();//last position submitted to openal
    private final        int                       samplerate;
    private final        long                      samples;
    private final        Vector3                   velocity             = new Vector3();//last velocity submitted to openal
    private              AudioProducer             audio;
    private              int                       auxiliaryEffectSlot  = 0;
    private              long                      buffersize;
    private              ByteBuffer                byteBuffer;
    private              List<ByteBufferContainer> byteBufferCopyList   = new ArrayList<>();
    private volatile     boolean                   end                  = false;
    private              int                       filter;
    private              boolean                   keepCopy             = false;
    private              boolean                   play;//source should be in play state
    private              int                       restartedSourceCount = 0;
    private              boolean                   sleeping             = false;
    private              int                       source;

    public OpenAlSource(final long samples, final int samplerate, final int bits, final int channels, final int auxiliaryEffectSlot) throws OpenAlException {
        this.samples             = samples;
        this.samplerate          = samplerate;
        this.bits                = bits;
        this.channels            = channels;
        this.auxiliaryEffectSlot = auxiliaryEffectSlot;
        createBuffer();
        createSource();
        setName("OpenAlSource-" + source);
    }

    public void attach(final AudioProducer audio) {
        this.audio = audio;
    }

    private void createBuffer() throws OpenAlException {
        buffersize = samples * channels * bits / 8;
        byteBuffer = LibCStdlib.malloc(buffersize);
        AL10.alGenBuffers(bufferId);
        AudioEngine.checkAlError("Openal error #");
        logger.trace("created filter " + bufferId[0] + "-" + bufferId[BUFFER_COUNT - 1]);
        for (int i = 0; i < BUFFER_COUNT; i++) {
            buffersUnqueued.add(bufferId[i]);
        }
    }

    private void createFilter() throws OpenAlException {
        //create a filter
        filter = EXTEfx.alGenFilters();
        AudioEngine.checkAlError("Failed to create filter with error #");
        logger.trace("created filter " + filter);
        if (EXTEfx.alIsFilter(filter)) {
            // Set Filter type to Low-Pass and set parameters
            EXTEfx.alFilteri(filter, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
            AudioEngine.checkAlError("Low pass filter not supported error #");
            //			EXTEfx.alFilterf(filter, EXTEfx.AL_LOWPASS_GAIN, lowGain);
            //			EXTEfx.alFilterf(filter, EXTEfx.AL_LOWPASS_GAINHF, highGain);
            logger.trace("Low pass filter created.");

            AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, filter);
            AudioEngine.checkAlError("Assigning direct filter failed with error #");
        }
    }

    private void createSource() throws OpenAlException {
        source = AL10.alGenSources();
        AudioEngine.checkAlError("Openal error #");
        logger.trace("created source " + source);

        AL10.alSourcef(source, AL10.AL_REFERENCE_DISTANCE, 1f);
        AudioEngine.checkAlError("Openal error #");

        AL10.alSourcef(source, AL10.AL_MAX_DISTANCE, Float.MAX_VALUE);
        AudioEngine.checkAlError("Openal error #");

        AL10.alSourcef(source, AL10.AL_ROLLOFF_FACTOR, 1f);
        AudioEngine.checkAlError("Openal error #");

        //		AL10.alSourcef(source, AL10.AL_GAIN, 1.0f);
        //		AudioEngine.checkAlError("Openal error #");

        AL10.alDopplerFactor(3.0f);
        AudioEngine.checkAlError("Openal error #");

        AL10.alDopplerVelocity(1.0f);
        AudioEngine.checkAlError("Openal error #");

        AL11.alSource3i(source, EXTEfx.AL_AUXILIARY_SEND_FILTER, auxiliaryEffectSlot, 1, 0);
    }

    void dispose() throws OpenAlException {
        end = true;
        unparkThread();
        //wait for the thread to terminate before manipulating any objects
        while (this.isAlive()) {
            try {
                Thread.sleep(1);
            } catch (final InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        removeFilter();
        removeSource();
        removeBuffers();
    }

    public long getBuffersize() {
        return buffersize;
    }

    public List<ByteBufferContainer> getByteBufferCopyList() {
        return byteBufferCopyList;
    }

    public int getRestartedSourceCount() {
        return restartedSourceCount;
    }

    public long getSamples() {
        return samples;
    }

    public boolean isKeepCopy() {
        return keepCopy;
    }

    boolean isPlay() throws OpenAlException {
        return play;
    }
    //	private void unqueueAllBuffers() throws Exception {
    //		final int queuedBuffers = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
    //
    //		while (bufferQueue.size() != BUFFER_COUNT) {
    //			final int availBuffers = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
    //			if (availBuffers > 0) {
    //				final int[] buffHolder = new int[BUFFER_COUNT];
    //				AL10.alSourceUnqueueBuffers(source, buffHolder);
    //
    //				checkError("Failed alSourceUnqueueBuffers with error #");
    //				for (int ii = 0; ii < availBuffers; ++ii) {
    //					// Push the recovered buffers back on the queue
    //					bufferQueue.add(buffHolder[ii]);
    //				}
    //			}
    //		}
    //	}

    boolean isPlaying() throws OpenAlException {
        int current_playing_state = 0;
        current_playing_state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
        AudioEngine.checkAlError("Openal error #");
        return AL10.AL_PLAYING == current_playing_state;
    }

    private synchronized void parkThread() {
        //		logger.info("parked thread");
        try {
            wait();
            sleeping = false;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread interrupted", e);
        }
        //		logger.info("unparked thread");
    }

    /**
     * source will be moved to unused list
     * pause all thread activity until continue is called
     *
     * @throws OpenAlException
     */
    void pause() throws OpenAlException {
        if (!sleeping)
            sleeping = true;
        if (play) {
            AL10.alSourcePause(source);
            AudioEngine.checkAlError("Openal error #");
            play = false;
        }
    }

    public void play() throws OpenAlException {
        if (sleeping)
            unparkOrStartThread();
        if (!play) {
            AL10.alSourcePlay(source);
            AudioEngine.checkAlError("Openal error #");
            play = true;
        }
    }

    private void queueBuffers() throws OpenAlException {
        for (final Integer bufferId : buffersUnqueued) {
            audio.processBuffer(byteBuffer);
            AL10.alBufferData(bufferId, audio.getOpenAlFormat(), byteBuffer, samplerate);
            AudioEngine.checkAlError("Openal error #");
            AL10.alSourceQueueBuffers(source, bufferId);
            AudioEngine.checkAlError("Openal error #");
        }
        buffersUnqueued.clear();
    }

    //	void renderBuffer() {
    //		for (int sampleIndex = 0, bufferIndex = 0; sampleIndex < samples; sampleIndex++, bufferIndex += 2) {
    //			byteBuffer.putShort(bufferIndex, audio.process(lastIndex + sampleIndex));
    //		}
    //		lastIndex += samples;
    //	}

    private void removeBuffers() throws OpenAlException {
        //TODO unqueing and delete buffers fails
        //		unqueueAllBuffers();
        //		AL10.alDeleteBuffers(bufferId);
        //		AudioEngine.checkAlError("Openal error #");
        for (final ByteBufferContainer b : byteBufferCopyList)
            LibCStdlib.free(b.byteBuffer);
        LibCStdlib.free(byteBuffer);
    }

    private void removeFilter() throws OpenAlException {
        if (filter != 0) {
            AL10.alSourcei(source, EXTEfx.AL_DIRECT_FILTER, EXTEfx.AL_FILTER_NULL);
            AudioEngine.checkAlError("Openal error #");

            EXTEfx.alDeleteFilters(filter);
            AudioEngine.checkAlError("Openal error #");
            filter = 0;
        }
    }

    private void removeSource() throws OpenAlException {
        final int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
        AL10.alSourceStop(source);
        AudioEngine.checkAlError("Openal error #");
        logger.trace("stopped source " + source);

        AL10.alDeleteSources(source);
        AudioEngine.checkAlError("Openal error #");
    }

    public void renderBuffer() throws OpenAlcException {
        audio.processBuffer(byteBuffer);
    }

    @Override
    public void run() {
        try {
            queueBuffers();
        } catch (final OpenAlException e) {
            logger.error(e.getMessage(), e);
        }
        do {
            try {
                Thread.sleep(1);
                if (sleeping) {
                    parkThread();
                }

            } catch (final InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            try {
                // Poll for recoverable buffers
                final int availBuffers = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
                AudioEngine.checkAlError("Failed AL_BUFFERS_PROCESSED with error #");

                if (availBuffers > 0) {
                    //					System.out.println(String.format("%d buffers processed.", availBuffers));

                    final int[] buffHolder = new int[availBuffers];
                    AL10.alSourceUnqueueBuffers(source, buffHolder);
                    AudioEngine.checkAlError("Failed alSourceUnqueueBuffers with error #");
                    //					System.out.println(String.format("Unqueued %d processed buffers.", availBuffers));
                    for (int ii = 0; ii < availBuffers; ++ii) {
                        // Push the recovered buffers back on the queue
                        bufferQueue.add(buffHolder[ii]);
                    }
                } else {
                    //					System.out.println(String.format("Found %d processed buffers.", availBuffers));
                }
                // generate new sound for the empty buffers
                if (!bufferQueue.isEmpty()) {
                    //					System.out.println(String.format("Found %d empty buffers.", bufferQueue.size()));
                    final int myBuff = bufferQueue.remove(0);
                    audio.processBuffer(byteBuffer);
                    AL10.alBufferData(myBuff, audio.getOpenAlFormat(), byteBuffer, samplerate);
                    AudioEngine.checkAlError("Failed alBufferData with error #");
                    // Queue the buffer
                    AL10.alSourceQueueBuffers(source, myBuff);
                    AudioEngine.checkAlError("Failed alSourceQueueBuffers with error #");
                    if (play) {
                        // Restart the source if needed (if we take too long and the queue dries up, the source stops playing).
                        final int state = AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE);
                        AudioEngine.checkAlError("Failed alGetSourcei AL_SOURCE_STATE with error #");
                        if (state != AL10.AL_PLAYING) {
                            System.out.println(String.format("Had to restart source %d.", source));
                            restartedSourceCount++;
                            AL10.alSourcePlay(source);
                            AudioEngine.checkAlError("Failed alSourcePlay with error #");
                        }
                    }
                }
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        } while (!end);
    }

    public void setGain(final float gain) throws OpenAlException {
        AL10.alSourcef(source, AL10.AL_GAIN, gain);
        AudioEngine.checkAlError("Failed alSourcef AL_GAIN with error #");
    }

    public void setKeepCopy(final boolean keepCopy) {
        this.keepCopy = keepCopy;
    }

    public void setPosition(final float[] position) throws OpenAlException {
        if (this.position.x != position[0] || this.position.y != position[1] || this.position.z != position[2]) {
            this.position.set(position[0], position[1], position[2]);
            AL10.alSourcefv(source, AL10.AL_POSITION, position);
            AudioEngine.checkAlError("Failed to set source position with error #");
        }
    }

    public void setVelocity(final float[] position, final float[] velocity) throws OpenAlException {
        if (this.velocity.x != velocity[0] || this.velocity.y != velocity[1] || this.velocity.z != velocity[2]) {
            this.velocity.set(velocity[0], velocity[1], velocity[2]);
            AL10.alSourcefv(source, AL10.AL_VELOCITY, velocity);
            AudioEngine.checkAlError("Failed to set source velocity with error #");
        }
    }

    public synchronized void unparkOrStartThread() {
        if (isAlive()) {
            notify();
        } else {
            start();
        }
    }

    private synchronized void unparkThread() {
        if (isAlive()) {
            notify();
        }
    }

    public void updateFilter(final boolean enableFilter, final float lowGain, final float highGain) throws OpenAlException {
        if (filter != 0) {
            if (enableFilter) {
                EXTEfx.alFilterf(filter, EXTEfx.AL_LOWPASS_GAIN, lowGain);
                AudioEngine.checkAlError("Failed to set filter lowGain with error #");

                EXTEfx.alFilterf(filter, EXTEfx.AL_LOWPASS_GAINHF, highGain);
                AudioEngine.checkAlError("Failed to set filter highgain with error #");
            } else {
                removeFilter();
            }
        } else {
            //no filter
            if (enableFilter) {
                createFilter();
                EXTEfx.alFilterf(filter, EXTEfx.AL_LOWPASS_GAIN, lowGain);
                AudioEngine.checkAlError("Failed to set filter lowGain with error #");

                EXTEfx.alFilterf(filter, EXTEfx.AL_LOWPASS_GAINHF, highGain);
                AudioEngine.checkAlError("Failed to set filter highgain with error #");
            } else {
                //ok
            }
        }
    }

    private void writeByteBufferToDisk(final ByteBuffer byteBuffer, final String fileName) throws IOException {
        final byte[] buffer = new byte[byteBuffer.capacity()];
        for (int i = 0; i < buffer.length; i++) {
            final int x = byteBuffer.getShort(i);
            buffer[i++] = (byte) x;
            buffer[i]   = (byte) (x >>> 8);
        }
        final File                 out              = new File(fileName + ".wav");
        final AudioFormat          format           = new AudioFormat(samplerate, bits, channels, true, false);
        final ByteArrayInputStream bais             = new ByteArrayInputStream(buffer);
        final AudioInputStream     audioInputStream = new AudioInputStream(bais, format, buffer.length);
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, out);
        audioInputStream.close();
    }

    void writeWav(final String fileName) throws IOException {
        if (isKeepCopy()) {
            for (int i = 0; i < byteBufferCopyList.size(); i++) {
                final ByteBuffer b = byteBufferCopyList.get(i).byteBuffer;
                writeByteBufferToDisk(b, fileName + i + "-" + byteBufferCopyList.get(i).startFrequency + "-" + byteBufferCopyList.get(i).endFrequency);
            }
        } else {
            writeByteBufferToDisk(byteBuffer, fileName);
        }
    }

}