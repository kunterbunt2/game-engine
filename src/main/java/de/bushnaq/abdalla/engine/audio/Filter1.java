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

public class Filter1 implements AudioFilter {


    public  float value;
    private float c, a1, a2, a3, b1, b2;
    private float    frequency;
    /// <summary>
/// Array of input values, latest are in front
/// </summary>
    private float[]  inputHistory  = new float[2];
    /// <summary>
/// Array of output values, latest are in front
/// </summary>
    private float[]  outputHistory = new float[3];
    private PassType passType;
    /// <summary>
/// rez amount, from sqrt(2) to ~ 0.1
/// </summary>
    private float    resonance;
    private int      sampleRate;

    public Filter1(float frequency, int sampleRate, PassType passType, float resonance) {
        init(frequency, sampleRate, passType, resonance);
    }

    private void init(float frequency, int sampleRate, PassType passType, float resonance) {
        this.resonance  = resonance;
        this.frequency  = frequency;
        this.sampleRate = sampleRate;
        this.passType   = passType;

        switch (passType) {
            case Lowpass:
                c = 1.0f / (float) Math.tan(Math.PI * frequency / sampleRate);
                a1 = 1.0f / (1.0f + resonance * c + c * c);
                a2 = 2f * a1;
                a3 = a1;
                b1 = 2.0f * (1.0f - c * c) * a1;
                b2 = (1.0f - resonance * c + c * c) * a1;
                break;
            case Highpass:
                c = (float) Math.tan(Math.PI * frequency / sampleRate);
                a1 = 1.0f / (1.0f + resonance * c + c * c);
                a2 = -2f * a1;
                a3 = a1;
                b1 = 2.0f * (c * c - 1.0f) * a1;
                b2 = (1.0f - resonance * c + c * c) * a1;
                break;
        }
    }

    public float process(float value) {
        float newOutput = a1 * value + a2 * this.inputHistory[0] + a3 * this.inputHistory[1] - b1 * this.outputHistory[0] - b2 * this.outputHistory[1];

        this.inputHistory[1] = this.inputHistory[0];
        this.inputHistory[0] = value;

        this.outputHistory[2] = this.outputHistory[1];
        this.outputHistory[1] = this.outputHistory[0];
        this.outputHistory[0] = newOutput;
        return this.outputHistory[0];
    }

    public void set(float frequency, int sampleRate, PassType passType, float resonance) {
        init(frequency, sampleRate, passType, resonance);
    }

    public enum PassType {
        Highpass,
        Lowpass,
    }
}