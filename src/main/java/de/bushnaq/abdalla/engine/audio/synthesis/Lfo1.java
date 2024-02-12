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

public class Lfo1 implements Lfo {
    private final float freq1Max = 70f;
    private final float freq1Min = 50f;
    //	private final List<Lfo1> state = new ArrayList<>();
    private       float factor;
    private       float freq     = 44f;
    private       float incr     = 0.00f;
    //	private long lastI = 0;
    //	private float max = Float.MIN_VALUE;
    //	private float min = Float.MAX_VALUE;
    private       int   samplerate;

    public Lfo1() {
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public double gen(final long i) {
        final double value = factor * Math.sin((2 * 3.14 * freq / samplerate) * i);
        prepareNextSample(i);
        return value;
    }

    @Override
    public float getFactor() {
        return factor;
    }

    @Override
    public void setFrequency(final float freq, final float factor) {
        this.freq   = freq;
        this.factor = factor;
    }

    @Override
    public void setSampleRate(final int samplerate) {
        this.samplerate = samplerate;
    }

    private void prepareNextSample(final long i) {
        //		System.out.println(String.format("%d %f", i, freq));
        //		state.add((Flo1) this.clone());
        //		lastI = i;
        freq += incr;
        //		min = Math.min(min, freq);
        //		max = Math.max(max, freq);
        if (freq1Min > freq || freq > freq1Max) {
            incr *= -1.0f;
        }
    }

}
