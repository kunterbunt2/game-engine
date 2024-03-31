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

public class SawOscillator implements Oscilator {
    private float lfoDepth = 0;//cent
    private float lfoFreq  = 1;//Hz
    private float oscFreq  = 440f;//Hz
    private int   samplerate;//1/s

    public SawOscillator() {
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }


//	@Override
//    protected void finalize() {
    //		System.out.println(String.format("min=%f, max=%f", min, max));
//	}

    @Override
    public float gen(long i) {
        final float oscFreqDetune      = (float) (lfoDepth / 20 * Math.sin((2 * Math.PI * lfoFreq / samplerate) * i));
        final float oscFreqWithVibrato = (float) (oscFreq * Math.pow(2, oscFreqDetune / 1200));

        final float fraction = samplerate / oscFreqWithVibrato;
        final float value    = -1 + 2 * (i % fraction) / fraction;
        return value;
    }

    //	@Override
    public float getFrequency() {
        return oscFreq;
    }

    @Override
    public void setFrequency(float frequency) {
        this.oscFreq = frequency;
    }

    /**
     * Vibrato control of the Oscillator
     *
     * @param lfoFreq  in Hz
     * @param lfoDepth in cent
     */
//	@Override
    public void setLfo(final float lfoFreq, final float lfoDepth) {
        this.lfoFreq  = lfoFreq;
        this.lfoDepth = lfoDepth;
    }

    //	@Override
    public void setOscillator(final float oscFreq) {
        this.oscFreq = oscFreq;
    }

    //	@Override
    public void setSampleRate(final int samplerate) {
        this.samplerate = samplerate;
    }
}