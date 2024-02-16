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

package de.bushnaq.abdalla.engine.audio.synthesis.util;

import java.util.Random;

public class BasicRandomGenerator {
    private final Random random;
    public        int    index;

    public BasicRandomGenerator(final int seed) {
        random = new Random(seed);
    }

    public float nextFloat() {
        return random.nextFloat();
    }

    public int nextInt(final float bound) {
        return random.nextInt((int) bound);
    }

    public int nextInt(final int bound) {
        return random.nextInt(bound);
    }

    public int nextInt(final long currentTime, final Object who, final int bound) {
        if (bound == 0)
            return 0;
        final int nextInt = random.nextInt(bound);
        return nextInt;
    }
}
