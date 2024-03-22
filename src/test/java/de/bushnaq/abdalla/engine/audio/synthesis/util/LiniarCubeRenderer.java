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

import com.badlogic.gdx.math.Vector3;
import de.bushnaq.abdalla.engine.RenderEngine3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiniarCubeRenderer extends CubeRenderer {
    private static final int     MAX_LINIAR_TRANSLATION = 2000;
    private static final int     START_SPEED            = 2;
    private final        Logger  logger                 = LoggerFactory.getLogger(this.getClass());
    private              float   factor                 = 2;
    private              float   factorIncrement        = 1;
    private              Vector3 minSpeed               = new Vector3(MIN_ENGINE_SPEED * 2, 0, 0);
    private              int     switchedDirection;

    public LiniarCubeRenderer(LiniarCubeActor cube, int mode) {
        super(cube, mode, SynthType.SYNTH);
    }

    @Override
    public void create(final RenderEngine3D<BasicGameEngine> renderEngine) {
        switch (mode) {
            case 0: {
                final float grid = MAX_GRID_SIZE;
                final float x    = getRandomGenerator().nextInt(grid) - grid / 2;
                final float y    = CUBE_SIZE / 2;
                final float z    = getRandomGenerator().nextInt(grid) - grid / 2;
                origin.set(x, y, z);
                position.set(x, y, z);
                final float speed = MIN_ENGINE_SPEED + getRandomGenerator().nextInt(MAX_ENGINE_SPEED - MIN_ENGINE_SPEED);
                switch (getRandomGenerator().nextInt(2)) {
                    case 0:
                        velocity.set(speed, 0, 0);
                        break;
                    case 1:
                        velocity.set(0, 0, speed);
                        break;
                }
            }
            break;
            case 1: {
                final float x = 0;
                final float y = CUBE_SIZE / 2;
                final float z = 0;
                origin.set(x, y, z);
                position.set(x - MAX_LINIAR_TRANSLATION, y, z);
                velocity.set(new Vector3(START_SPEED, 0, 0));

            }
            break;
        }
        super.create(renderEngine);
    }

    @Override
    public void update(final RenderEngine3D<BasicGameEngine> renderEngine, final long currentTime, final float timeOfDay, final int index, final boolean selected) throws Exception {
        switch (mode) {
            case 0: {
                position.x += velocity.x;
                position.z += velocity.z;
                if (Math.abs(position.x - origin.x) > MAX_LINIAR_TRANSLATION) velocity.x = -velocity.x;
                if (Math.abs(position.z - origin.z) > MAX_LINIAR_TRANSLATION) velocity.z = -velocity.z;
            }
            break;
            case 1: {
                if (Math.abs(position.x + velocity.x - origin.x) > MAX_LINIAR_TRANSLATION) {
                    velocity.x = -velocity.x;
                    switchedDirection++;
                    logger.info(String.format("on the x edge = %f", Math.abs(position.x - origin.x)));
                } else position.x += velocity.x;
                if (Math.abs(position.z + velocity.z - origin.z) > MAX_LINIAR_TRANSLATION) {
                    velocity.z = -velocity.z;
                    switchedDirection++;
                    logger.info(String.format("on the z edge = %f", Math.abs(position.z - origin.z)));
                } else position.z += velocity.z;
                if (switchedDirection > 0) {
                    logger.info(String.format("switching direction = %d", switchedDirection));
                    final Vector3 newSpeed = velocity.cpy().nor().scl(minSpeed).scl(factor);
                    //					logger.info("x=" + newSpeed.x + " y=" + newSpeed.y + " z=" + newSpeed.z);
                    velocity.set(newSpeed);
                    switchedDirection = 0;
                    if (velocity.len() >= MAX_ENGINE_SPEED || velocity.len() <= MIN_ENGINE_SPEED) {
                        factorIncrement = -factorIncrement;
                        factor += factorIncrement;
                        logger.info(String.format("factor = %f factorIncrement = %f", factor, factorIncrement));
                    } else {
                        factor += factorIncrement;
                        logger.info(String.format("factor = %f factorIncrement = %f", factor, factorIncrement));
                    }
                }
            }
            break;
        }
        super.update(renderEngine, currentTime, timeOfDay, index, selected);
    }

}
