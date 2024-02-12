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

public class BassBoost {
    double omega, sn, cs, a, shape, beta, b0, b1, b2, a0, a1, a2;
    double xn1, xn2, yn1, yn2;

    public BassBoost(final float frequency, final float dB_boost, final int sampleRate) {
        init(frequency, dB_boost, sampleRate);
    }

    private void init(final float frequency, final float dB_boost, final int sampleRate) {
        xn1 = 0;
        xn2 = 0;
        yn1 = 0;
        yn2 = 0;

        omega = 2 * Math.PI * frequency / sampleRate;
        sn    = Math.sin(omega);
        cs    = Math.cos(omega);
        a     = Math.exp(Math.log(10.0) * dB_boost / 40);
        shape = 1.0;
        beta  = Math.sqrt((a * a + 1) / shape - (Math.pow((a - 1), 2)));
        /* Coefficients */
        b0 = a * ((a + 1) - (a - 1) * cs + beta * sn);
        b1 = 2 * a * ((a - 1) - (a + 1) * cs);
        b2 = a * ((a + 1) - (a - 1) * cs - beta * sn);
        a0 = ((a + 1) + (a - 1) * cs + beta * sn);
        a1 = -2 * ((a - 1) + (a + 1) * cs);
        a2 = (a + 1) + (a - 1) * cs - beta * sn;
    }

    public double process(final double value, final int length) {
        double out, in = 0;

        in  = value;
        out = (b0 * in + b1 * xn1 + b2 * xn2 - a1 * yn1 - a2 * yn2) / a0;
        xn2 = xn1;
        xn1 = in;
        yn2 = yn1;
        yn1 = out;

        if (out < -1.0)
            out = -1.0;
        else if (out > 1.0)
            out = 1.0; // Prevents clipping
        return out;
    }

    public void set(final float bassBoostFrequency, final float bassBoostDbGain, final int sampleRate) {
        init(bassBoostFrequency, bassBoostDbGain, sampleRate);
    }
}