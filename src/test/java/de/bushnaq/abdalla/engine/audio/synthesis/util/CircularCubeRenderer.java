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

import de.bushnaq.abdalla.engine.RenderEngine3D;

public class CircularCubeRenderer extends CubeRenderer {
    public float angle;
    public float angleSpeed;
    public float radius1;
    public float radius2;

    public CircularCubeRenderer(CircularCubeActor cube, int mode) {
        super(cube, mode);
    }

    @Override
    public void create(final RenderEngine3D<BasicGameEngine> renderEngine) {
        final float grid = MAX_GRID_SIZE;
        final float x    = getRandomGenerator().nextInt(grid) - grid / 2;
        final float y    = CUBE_SIZE / 2;
        final float z    = getRandomGenerator().nextInt(grid) - grid / 2;
        origin.set(x, y, z);
        angle   = 0;
        radius1 = MAX_GRID_SIZE / 2 + getRandomGenerator().nextInt(MAX_GRID_SIZE / 2);
        radius2 = MAX_GRID_SIZE / 2 + getRandomGenerator().nextInt(MAX_GRID_SIZE / 2);
        final float averageRadius = (radius1 + radius2) / 2;
        angleSpeed = generateAngleSpeed(averageRadius);

        super.create(renderEngine);
    }

    @Override
    public void update(final RenderEngine3D<BasicGameEngine> renderEngine, final long currentTime, final float timeOfDay, final int index, final boolean selected) throws Exception {
        final float x = origin.x + (float) (radius1 * Math.sin((angle / 180) * 3.14f));
        final float z = origin.z + (float) (radius2 * Math.cos((angle / 180) * 3.14f));
        position.set(x, 32, z);

        final float vx = (float) (Math.PI * radius1 * angleSpeed * Math.cos((angle / 180) * 3.14f)) / 180;
        final float vz = -(float) (Math.PI * radius2 * angleSpeed * Math.sin((angle / 180) * 3.14f)) / 180;

        velocity.set(vx, 0, vz);
        angle += angleSpeed;
        super.update(renderEngine, currentTime, timeOfDay, index, selected);
    }

    private float generateAngleSpeed(final float radius) {
        final float speed = MIN_ENGINE_SPEED + getRandomGenerator().nextInt(MAX_ENGINE_SPEED - MIN_ENGINE_SPEED);
        return (float) (speed * 180 / (Math.PI * radius));
    }

}
