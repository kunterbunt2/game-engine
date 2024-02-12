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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SinOscillator implements Oscilator {
    private final Logger logger       = LoggerFactory.getLogger(this.getClass());
    public        float  maxFrequency = 0f;
    public        float  minFrequency = 1000000f;
    private       float  lfoDepth     = 0;//cent
    private       float  lfoFreq      = 1;//Hz
    private       float  oscFreq      = 440f;
    private       int    samplerate;

    public SinOscillator() {
    }

    @Override
    public void dispose() {
//		logger.info(String.format("minFrequency=%f oscFreq=%f maxFrequency=%f", minFrequency, oscFreq, maxFrequency));
    }

    @Override
    public double gen(final long i) {
        final float oscFreqDetune      = (float) (lfoDepth / 10 * Math.sin((2 * Math.PI * lfoFreq / samplerate) * i));
        final float oscFreqWithVibrato = (float) (oscFreq * Math.pow(2, oscFreqDetune / 1200));
        minFrequency = Math.min(minFrequency, oscFreqWithVibrato);
        maxFrequency = Math.max(maxFrequency, oscFreqWithVibrato);

        final double value = Math.sin(((2 * Math.PI * oscFreqWithVibrato) / samplerate) * i);
        return value;
    }

    @Override
    public double getFrequency() {
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
    @Override
    public void setLfo(final float lfoFreq, final float lfoDepth) {
        this.lfoFreq  = lfoFreq;
        this.lfoDepth = lfoDepth;
    }

    @Override
    public void setOscillator(final float oscFreq) {
        this.oscFreq = oscFreq;
    }

    @Override
    public void setSampleRate(final int samplerate) {
        this.samplerate = samplerate;
    }
}