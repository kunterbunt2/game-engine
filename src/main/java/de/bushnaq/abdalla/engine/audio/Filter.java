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

import de.bushnaq.abdalla.engine.audio.synthesis.AudioFilter;
import uk.me.berndporr.iirj.Butterworth;

public class Filter implements AudioFilter {

    Butterworth butterworth = new Butterworth();
    int         order       = 12;
    private float           frequency;
    private Filter.PassType passType;
    private float           resonance;
    private int             sampleRate;

    public Filter(float frequency, int sampleRate, Filter.PassType passType, float resonance) {
        init(frequency, sampleRate, passType, resonance);
    }

    private void init(float frequency, int sampleRate, Filter.PassType passType, float resonance) {
        this.resonance  = resonance;
        this.frequency  = frequency;
        this.sampleRate = sampleRate;
        this.passType   = passType;
        butterworth.highPass(order, sampleRate, frequency);
    }

    @Override
    public float process(float value) {

        return (float) butterworth.filter(value);
    }

    public void set(float frequency, int sampleRate, Filter.PassType passType, float resonance) {
        init(frequency, sampleRate, passType, resonance);
    }

    public enum PassType {
        Highpass,
        Lowpass,
        BANDPASS,
        BANDSTOP
    }
}
