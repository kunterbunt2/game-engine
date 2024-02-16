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

import de.bushnaq.abdalla.engine.audio.synthesis.util.LiniarTranslation;
import de.bushnaq.abdalla.engine.audio.synthesis.util.TranslationUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiniarTranslationTest extends TranslationUtil<LiniarTranslation> {
    protected static final int    NUMBER_OF_SOURCES      = 10;
    private static final   float  CUBE_SIZE              = 64;
    private static final   float  MAX_GRID_SIZE          = 1000f;
    private static final   int    MAX_LINIAR_TRANSLATION = 1000;
    private final          Logger logger                 = LoggerFactory.getLogger(this.getClass());

    @Override
    public void create() {
        super.create(NUMBER_OF_SOURCES);
        try {
            for (int l = 0; l < NUMBER_OF_SOURCES; l++) {
                final float             grid = MAX_GRID_SIZE;
                final float             x    = getRandomGenerator().nextInt(grid) - grid / 2;
                final float             y    = CUBE_SIZE / 2;
                final float             z    = getRandomGenerator().nextInt(grid) - grid / 2;
                final LiniarTranslation t    = new LiniarTranslation();
                t.origin.set(x, y, z);
                t.position.set(x, y, z);
                final float speed = MIN_ENGINE_SPEED + getRandomGenerator().nextInt(MAX_ENGINE_SPEED - MIN_ENGINE_SPEED);
                switch (getRandomGenerator().nextInt(2)) {
                    case 0:
                        t.velocity.set(speed, 0, 0);
                        break;
                    case 1:
                        t.velocity.set(0, 0, speed);
                        break;
                }
                translation.add(t);
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void liniarTranslatingSources() throws Exception {
        runFor = 5000;
        startLwjgl();
    }

    @Override
    protected void updateTranslation() {
        for (int i = 0; i < gameObjects.size(); i++) {
            final LiniarTranslation t = translation.get(i);
            t.position.x += t.velocity.x;
            t.position.z += t.velocity.z;
            if (Math.abs(t.position.x - t.origin.x) > MAX_LINIAR_TRANSLATION) t.velocity.x = -t.velocity.x;
            if (Math.abs(t.position.z - t.origin.z) > MAX_LINIAR_TRANSLATION) t.velocity.z = -t.velocity.z;
        }
    }

}