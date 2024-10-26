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

package de.bushnaq.abdalla.engine.shader.effect.scheduled;

import com.badlogic.gdx.math.Vector3;
import de.bushnaq.abdalla.engine.IGameEngine;

public class RotatingCamera<T extends IGameEngine> extends WaitDuringExecuteAbstractTask<T> {
    private final float angle;

    public RotatingCamera(T gameEngine, int durationSeconds, float angle) {
        super(gameEngine, durationSeconds);
        this.angle = angle;
    }

    @Override
    public void subexecute(float deltaTime) {
        gameEngine.getCamera().rotateAround(gameEngine.getCamera().lookat, Vector3.Y, -angle * deltaTime);
        gameEngine.getCamera().setDirty(true);
        gameEngine.getCamera().update();
    }

}
