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

import com.badlogic.gdx.math.Vector3;
import de.bushnaq.abdalla.engine.audio.synthesis.util.LiniarTranslation;
import de.bushnaq.abdalla.engine.audio.synthesis.util.TranslationUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MercatorSoundTest extends TranslationUtil<LiniarTranslation> {
    protected static final int    NUMBER_OF_SOURCES      = 1;
    protected static final int    START_SPEED            = 2;
    private static final   float  CUBE_SIZE              = 64;
    private static final   int    MAX_LINIAR_TRANSLATION = 2000;
    private final          Logger logger                 = LoggerFactory.getLogger(this.getClass());
    Vector3 minSpeed = new Vector3(MIN_ENGINE_SPEED, 0, 0);
    private float factor          = 2;
    private float factorIncrement = 1;
    private int   switchedDirection;

    @Override
    public void create() {
        //		simulateBassBoost = true;
        super.create(NUMBER_OF_SOURCES);
        try {
            for (int l = 0; l < NUMBER_OF_SOURCES; l++) {
                final float             x = 0;
                final float             y = CUBE_SIZE / 2;
                final float             z = 0;
                final LiniarTranslation t = new LiniarTranslation();
                t.origin.set(x, y, z);
                t.position.set(x - MAX_LINIAR_TRANSLATION, y, z);
                t.velocity.set(new Vector3(START_SPEED, 0, 0));
                translation.add(t);
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Test
    public void liniarTranslatingSources() throws Exception {
        runFor = 10000;
        startLwjgl();
    }

    @Override
    protected void updateTranslation() {
        for (int i = 0; i < gameObjects.size(); i++) {
            final LiniarTranslation t = translation.get(i);
            if (Math.abs(t.position.x + t.velocity.x - t.origin.x) > MAX_LINIAR_TRANSLATION) {
                t.velocity.x = -t.velocity.x;
                switchedDirection++;
                logger.info(String.format("on the x edge = %f", Math.abs(t.position.x - t.origin.x)));
            } else t.position.x += t.velocity.x;
            if (Math.abs(t.position.z + t.velocity.z - t.origin.z) > MAX_LINIAR_TRANSLATION) {
                t.velocity.z = -t.velocity.z;
                switchedDirection++;
                logger.info(String.format("on the z edge = %f", Math.abs(t.position.z - t.origin.z)));
            } else t.position.z += t.velocity.z;
            if (switchedDirection > 0) {
                logger.info(String.format("switching direction = %d", switchedDirection));
                final Vector3 newSpeed = t.velocity.cpy().nor().scl(minSpeed).scl(factor);
                //					logger.info("x=" + newSpeed.x + " y=" + newSpeed.y + " z=" + newSpeed.z);
                t.velocity.set(newSpeed);
                switchedDirection = 0;
                if (t.velocity.len() >= MAX_ENGINE_SPEED || t.velocity.len() <= MIN_ENGINE_SPEED) {
                    factorIncrement = -factorIncrement;
                    factor += factorIncrement;
                    logger.info(String.format("factor = %f factorIncrement = %f", factor, factorIncrement));
                } else {
                    factor += factorIncrement;
                    logger.info(String.format("factor = %f factorIncrement = %f", factor, factorIncrement));
                }
            }
        }
    }
}