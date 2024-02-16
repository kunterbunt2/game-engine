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

import de.bushnaq.abdalla.engine.audio.synthesis.util.CircularTranslation;
import de.bushnaq.abdalla.engine.audio.synthesis.util.TranslationUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircularTranslationTest extends TranslationUtil<CircularTranslation> {
    private static final float  MAX_GRID_SIZE     = 1000f;
    private static final int    NUMBER_OF_SOURCES = 10;
    private final        Logger logger            = LoggerFactory.getLogger(this.getClass());

    @Test
    public void circularTranslatingSources() {
        runFor = 10000;
        startLwjgl();
    }

    @Override
    public void create() {
        super.create(NUMBER_OF_SOURCES);
        try {
            for (int i = 0; i < NUMBER_OF_SOURCES; i++) {
                final float               grid = MAX_GRID_SIZE;
                final float               x    = getRandomGenerator().nextInt(grid) - grid / 2;
                final float               y    = CUBE_SIZE / 2;
                final float               z    = getRandomGenerator().nextInt(grid) - grid / 2;
                final CircularTranslation t    = new CircularTranslation();
                t.origin.set(x, y, z);
                t.angle   = 0;
                t.radius1 = MAX_GRID_SIZE / 2 + getRandomGenerator().nextInt(MAX_GRID_SIZE / 2);
                t.radius2 = MAX_GRID_SIZE / 2 + getRandomGenerator().nextInt(MAX_GRID_SIZE / 2);
                final float averageRadius = (t.radius1 + t.radius2) / 2;
                t.angleSpeed = generateAngleSpeed(averageRadius);
                translation.add(t);
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private float generateAngleSpeed(final float radius) {
        final float speed = MIN_ENGINE_SPEED + getRandomGenerator().nextInt(MAX_ENGINE_SPEED - MIN_ENGINE_SPEED);
        //		System.out.println("speed =" + speed);
        return (float) (speed * 180 / (Math.PI * radius));
    }

    @Override
    protected void updateTranslation() {

        for (int i = 0; i < gameObjects.size(); i++) {
            final CircularTranslation t = translation.get(i);

            final float x = t.origin.x + (float) (t.radius1 * Math.sin((t.angle / 180) * 3.14f));
            final float z = t.origin.z + (float) (t.radius2 * Math.cos((t.angle / 180) * 3.14f));
            t.position.set(x, 32, z);

            final float vx = (float) (Math.PI * t.radius1 * t.angleSpeed * Math.cos((t.angle / 180) * 3.14f)) / 180;
            final float vz = -(float) (Math.PI * t.radius2 * t.angleSpeed * Math.sin((t.angle / 180) * 3.14f)) / 180;

            t.velocity.set(vx, 0, vz);
            t.angle += t.angleSpeed;
        }
    }

}