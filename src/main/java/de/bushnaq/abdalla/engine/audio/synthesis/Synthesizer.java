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

package de.bushnaq.abdalla.engine.audio.synthesis;

import de.bushnaq.abdalla.engine.audio.*;
import org.lwjgl.openal.AL10;
import org.lwjgl.system.libc.LibCStdlib;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Synthesizer extends AbstractAudioProducer {

    private final List<Lfo>       lfos          = new ArrayList<>();
    private final List<Oscilator> oscillators   = new ArrayList<>();
    //	private boolean play = false;//is the source playing?
    //	private final Vector3 position = new Vector3();//position of the audio source
    volatile      double          lastFrequency = 0.0;
    long lastIndex;
    //	private boolean enabled = false;//a disabled synth does not possess an audio source and any of the source attached resource like filters and buffers
    //	private float gain = 1.0f;
    //	private OpenAlSource source = null;//if enabled, this will hold the attached openal source, otherwise null
    //	private final Vector3 velocity = new Vector3();//velocity of the audio source

    public Synthesizer(final int samplerate) throws OpenAlException {
        super(samplerate);
    }

    public void add(final Lfo lfo) {
        lfo.setSampleRate(samplerate);
        lfos.add(lfo);
    }

    //	public OpenAlSource disable() throws OpenAlException {
    //		enabled = false;
    //		source.pause();
    //		final OpenAlSource sourceBuffer = source;
    //		source = null;
    //		return sourceBuffer;
    //	}

    public void add(final Oscilator generator) {
        generator.setSampleRate(samplerate);
        oscillators.add(generator);
    }


    //	public Vector3 getPosition() {
    //		return position;
    //	}

    //	public boolean isEnabled() {
    //		return enabled;
    //	}

    //	public boolean isPlaying() throws OpenAlException {
    //		return play;
    //	}

    //	public void pause() throws OpenAlException {
    //		if (this.play) {
    //			play = false;
    //		}
    //		if (isEnabled())
    //			source.pause();
    //	}

    //	public void play() throws OpenAlException {
    //		if (!this.play) {
    //			play = true;
    //		}
    //		if (isEnabled())
    //			source.play();
    //	}

    @Override
    public void dispose() throws OpenAlException {
        super.dispose();
        //		if (isEnabled())
        //			source.dispose();
        for (final Oscilator osc : oscillators) {
            osc.dispose();
        }
        for (final Lfo lfo : lfos) {
            lfo.dispose();
        }
    }

    @Override
    public void enable(final OpenAlSource source) throws OpenAlException {
        enabled     = true;
        this.source = source;
        this.source.attach(this);
        this.source.setGain(gain);
        this.source.updateFilter(filters.enableFilter, filters.lowGain, filters.highGain);
        if (isPlaying())
            this.source.play();//we should be playing
        this.source.unparkOrStartThread();
    }

    @Override
    public int getChannels() {
        return 1;
    }

    @Override
    public int getOpenAlFormat() {
        return AL10.AL_FORMAT_MONO16;
    }

    @Override
    public void processBuffer(final ByteBuffer byteBuffer) throws OpenAlcException {
        double              f1                  = -1;
        double              f2                  = 0.0;
        ByteBufferContainer byteBufferContainer = null;
        if (isKeepCopy()) {
            byteBufferContainer            = new ByteBufferContainer();
            byteBufferContainer.byteBuffer = LibCStdlib.malloc(source.getBuffersize());
            source.getByteBufferCopyList().add(byteBufferContainer);
        }
        for (int sampleIndex = 0, bufferIndex = 0; sampleIndex < source.getSamples(); sampleIndex++, bufferIndex += 2) {
            final Short value = process(lastIndex + sampleIndex);
            f2 = lastFrequency;
            if (f1 == -1)
                f1 = f2;
            byteBuffer.putShort(bufferIndex, value);
            if (isKeepCopy()) {
                byteBufferContainer.byteBuffer.putShort(bufferIndex, value);
            }
        }
        if (isKeepCopy()) {
            byteBufferContainer.startFrequency = f1;
            byteBufferContainer.endFrequency   = f2;
        }
        lastIndex += source.getSamples();
    }

    //	/**
    //	 * Convenience method used for debugging
    //	 * @throws OpenAlcException
    //	 */
    //	public void renderBuffer() throws OpenAlcException {
    //		if (isEnabled()) {
    //			source.renderBuffer();
    //		} else {
    //			throw new OpenAlcException("Synth is disabled");
    //		}
    //	}

    public short process(final long i) {
        float value = 0;
        for (final Oscilator osc : oscillators) {
            value += osc.gen(i) / oscillators.size();
            lastFrequency = osc.getFrequency();
        }
        for (final Lfo lfo : lfos) {
            value *= (1 + lfo.gen(i)) / (1 + lfo.getFactor());
        }

        if (filters.bassBoost != null)
            value = filters.bassBoost.process(value);

        //Short.MAX_VALUE;
        return (short) (32760 * value);

    }


    //	public void setGain(final float gain) throws OpenAlException {
    //		if (this.gain != gain && isEnabled()) {
    //			source.setGain(gain);
    //		}
    //		this.gain = gain;
    //	}

    //	public void setPositionAndVelocity(final float[] position, final float[] velocity) throws OpenAlException {
    //		if (this.getPosition().x != position[0] || this.getPosition().y != position[1] || this.getPosition().z != position[2]) {
    //			this.getPosition().set(position[0], position[1], position[2]);
    //		}
    //		if (isEnabled()) {
    //			source.setPosition(position);
    //		}
    //		//		}
    //		if (this.velocity.x != velocity[0] || this.velocity.y != velocity[1] || this.velocity.z != velocity[2]) {
    //			this.velocity.set(velocity[0], velocity[1], velocity[2]);
    //			adaptToVelocity(this.velocity.len());
    //		}
    //		if (isEnabled()) {
    //			source.setVelocity(position, velocity);
    //		}
    //		//		}
    //	}

    public void setFilter(final boolean enableFilter) throws OpenAlException {
        this.filters.enableFilter = enableFilter;
        if (isEnabled())
            this.source.updateFilter(enableFilter, filters.lowGain, filters.highGain);
    }

    public void setFilterGain(final float lowGain, final float highGain) throws OpenAlException {
        this.filters.lowGain  = lowGain;
        this.filters.highGain = highGain;
        if (isEnabled())
            this.source.updateFilter(filters.enableFilter, lowGain, highGain);
    }


    //	public void waitForPlay() throws InterruptedException, OpenAlException {
    //		if (isEnabled()) {
    //			do {
    //				Thread.sleep(100); // should use a thread sleep NOT sleep() for a more responsive finish
    //			} while (source.isPlaying());
    //		}
    //	}

    //	public void writeWav(final String fileName) throws IOException, OpenAlcException {
    //		if (isEnabled()) {
    //			source.writeWav(fileName);
    //		} else {
    //			throw new OpenAlcException("Synth is disabled");
    //		}
    //	}

}