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

package de.bushnaq.abdalla.engine.shader.effect.ssao;

import com.badlogic.gdx.math.Vector3;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Random;

public class Util {

    /**
     * Kernel sampling
     *
     * @return Kernel buffer
     */
    public static float[] kernel() {
        float[] kernelBuffer = new float[8 * 8 * 3];
        Random  r            = new Random(Double.doubleToLongBits(Math.random()));
        for (int i = 0; i < 64; i++) {
            Vector3 sample = new Vector3((float) (r.nextDouble() * 2.0 - 1.0), (float) (r.nextDouble() * 2.0 - 1.0),
                    (float) (r.nextDouble()));
            sample = sample.nor();
            sample = sample.scl((float) r.nextDouble());
            float scale = (float) i / 64.0f;
            scale                   = lerp(0.1f, 1.0f, scale * scale);
            sample                  = sample.scl(scale);
            kernelBuffer[i * 3]     = sample.x;
            kernelBuffer[i * 3 + 1] = sample.y;
            kernelBuffer[i * 3 + 2] = sample.z;
        }
//        kernelBuffer.flip();
        return kernelBuffer;
    }

    /**
     * Linear interpolation
     *
     * @param a
     * @param b
     * @param f
     * @return
     */
    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }

    /**
     * Noise for kernel rotation
     *
     * @return Noise buffer
     */
    public static FloatBuffer noise() {
        FloatBuffer noiseBuffer = BufferUtils.createFloatBuffer(4 * 4 * 3);
        Random      r           = new Random(Double.doubleToLongBits(Math.random()));
        for (int i = 0; i < 16; i++) {
            Vector3 sample = new Vector3((float) (r.nextDouble() * 2.0 - 1.0), (float) (r.nextDouble() * 2.0 - 1.0), 0);
            noiseBuffer.put(sample.x);
            noiseBuffer.put(sample.y);
            noiseBuffer.put(sample.z);
        }
        noiseBuffer.flip();
        return noiseBuffer;
    }

}
