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
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class AbstractAudioProducer implements AudioProducer {
    @Getter
    @Setter
    private         boolean      ambient      = false;//position always follows camera
    protected       boolean      enabled      = false;//a disabled synth does not possess an audio source and any of the source attached resource like filters and buffers
    protected       Filters      filters;
    @Getter
    protected       float        gain         = 8.0f;
    @Getter
    protected       boolean      ignore;
    @Getter
    @Setter
    private         String       name;
    private final   byte[]       oneKiloBytes = new byte[1024];//used to fast zero the byte buffer in times of silence
    @Getter
    protected       boolean      playing      = false;//is the source playing?
    @Getter
    protected final Vector3      position     = new Vector3();//position of the audio source
    @Getter
    @Setter
    private         boolean      radio        = false;
    @Getter
    protected       int          sampleRate;
    protected       OpenAlSource source       = null;//if enabled, this will hold the attached openal source, otherwise null
    @Getter
    private         float        sourceGain;
    protected final Vector3      velocity     = new Vector3();//velocity of the audio source

    public AbstractAudioProducer(int sampleRate, String name) {
        this.sampleRate = sampleRate;
        this.name       = name;
        filters         = new Filters(sampleRate, this);
    }

    /**
     * adapt synthesizer to the current source velocity
     *
     * @param speed
     * @throws OpenAlException
     */
    @Override
    public void adaptToVelocity(final float speed) throws OpenAlException {
    }

    @Override
    public OpenAlSource disable() throws OpenAlException {
        enabled = false;
        if (source != null) {
            source.pause();
        }
        final OpenAlSource sourceBuffer = source;
        source = null;
        return sourceBuffer;
    }

    @Override
    public void dispose() throws OpenAlException {
        if (ignore)
            if (isEnabled()) source.dispose();
    }

    @Override
    public void enable(final OpenAlSource source) throws OpenAlException {
        enabled     = true;
        this.source = source;
        this.source.attach(this);
        this.source.setGain(gain);
        if (isPlaying()) this.source.play();//we should be playing
        this.source.unparkOrStartThread();
    }

    protected void fastZero(ByteBuffer byteBuffer) {
        //fast zero the buffer
        //TODO this code is causing crash, not sure why
//        for (int i = byteBuffer.position(); i < (byteBuffer.capacity() - byteBuffer.position()) / oneKiloBytes.length; i++) {
//            byteBuffer.put(oneKiloBytes);
//        }
        //zero the rest
        for (int i = byteBuffer.position(); i < byteBuffer.capacity(); i++) {
            byteBuffer.put(i, (byte) 0);
        }
    }

    public void ignore(boolean value) {
        this.ignore = value;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isKeepCopy() throws OpenAlcException {
        if (isEnabled()) {
            return source.isKeepCopy();
        } else {
            throw new OpenAlcException("Synth is disabled");
        }
    }

    /**
     * default implementation wants to be enabled whenever possible
     *
     * @return true to opt in and get enabled if we are in listening distance
     */
    @Override
    public boolean isOptIn() {
        return true;
    }

    @Override
    public void pause() throws OpenAlException {
        if (!ignore) {
            if (this.playing) {
                playing = false;
            }
            if (isEnabled()) source.pause();
        }
    }

    @Override
    public void play() throws OpenAlException {
        if (!ignore) {
            if (!this.playing) {
                playing = true;
            }
            if (isEnabled()) source.play();
        }
    }

    /**
     * Convenience method used for debugging
     *
     * @throws OpenAlcException
     */
    public void renderBuffer() throws OpenAlException {
        if (isEnabled()) {
            source.renderBuffer();
        } else {
            throw new OpenAlcException("Synth is disabled");
        }
    }

    @Override
    public void setGain(final float gain) throws OpenAlException {
        if (Math.abs(this.sourceGain - gain) > 0.1f && isEnabled()) {
            sourceGain = gain;
            source.setGain(gain);
        }
        this.gain = gain;
    }

    public void setKeepCopy(final boolean enable) throws OpenAlcException {
        if (isEnabled()) {
            source.setKeepCopy(enable);
        } else {
            throw new OpenAlcException("Synth is disabled");
        }
    }

    @Override
    public void setPositionAndVelocity(final float[] position, final float[] velocity) throws OpenAlException {
        if (!ambient) {
            if (this.getPosition().x != position[0] || this.getPosition().y != position[1] || this.getPosition().z != position[2]) {
                this.getPosition().set(position[0], position[1], position[2]);
            }
            if (isEnabled()) {
                source.setPosition(position);
                //			source.setPosition(new float[] {0,0,0});
            }
            if (velocity != null && (this.velocity.x != velocity[0] || this.velocity.y != velocity[1] || this.velocity.z != velocity[2])) {
                this.velocity.set(velocity[0], velocity[1], velocity[2]);
                adaptToVelocity(this.velocity.len());
            }
//        if (isEnabled()) {
            //			source.setVelocity(position, velocity);
//        }
        }
    }

    @Override
    public void waitForPlay() throws InterruptedException, OpenAlException {
        if (isEnabled()) {
            do {
                Thread.sleep(100); // should use a thread sleep NOT sleep() for a more responsive finish
            } while (source.isPlaying());
        }
    }

    @Override
    public void writeWav(final String fileName) throws IOException, OpenAlcException {
        if (isEnabled()) {
            source.writeWav(fileName);
        } else {
            throw new OpenAlcException("Synth is disabled");
        }
    }

}
