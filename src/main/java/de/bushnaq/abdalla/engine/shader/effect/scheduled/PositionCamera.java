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

public class PositionCamera<T extends IGameEngine> extends ScheduledTask<T> {
    private final float   fieldOfView;
    private final Vector3 lookat;
    private final Vector3 position;
    private final int     zoomIndex;

    public PositionCamera(T gameEngine, int zoomIndex, Vector3 position, Vector3 lookat, float fieldOfView) {
        super(gameEngine, 0);
        this.zoomIndex   = zoomIndex;
        this.position    = position;
        this.lookat      = lookat;
        this.fieldOfView = fieldOfView;
    }

    @Override
    public boolean execute(float deltaTime) {
        gameEngine.getCamera().position.set(position);
        gameEngine.getCamera().up.set(0, 1, 0);
        gameEngine.getRenderEngine().getDepthOfFieldEffect().setFocalDepth(position.dst(lookat) + 10f);
        gameEngine.getCamera().lookAt(lookat);
        gameEngine.getCamera().fieldOfView = fieldOfView;
//        gameEngine.getCamController().setTargetZoomIndex(zoomIndex);
//        gameEngine.getCamController().zoomIndex = zoomIndex;
        gameEngine.getCamController().update();
        gameEngine.getCamera().update(true);
        gameEngine.getCamera().setDirty(true);
        return true;
    }

    @Override
    public long secondToRun() {
        return 0;
    }

    @Override
    public void subexecute(float deltaTime) {

    }
}
