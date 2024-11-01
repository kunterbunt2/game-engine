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

package de.bushnaq.abdalla.engine.chronos;

import com.badlogic.gdx.math.Vector3;
import de.bushnaq.abdalla.engine.IGameEngine;

/**
 * positions the {@link de.bushnaq.abdalla.engine.camera.MovingCamera gameEngine.getCamera()  } at <code>position</code>.<br>
 * rotates the {@link de.bushnaq.abdalla.engine.camera.MovingCamera gameEngine.getCamera()  } to look at <code>lookat</code>.<br>
 * sets {@link de.bushnaq.abdalla.engine.camera.MovingCamera gameEngine.getCamera().fieldOfView  } to <code>fieldOfView</code>.<br>
 * sets focal depth of the {@link de.bushnaq.abdalla.engine.shader.effect.DepthOfFieldEffect gameEngine.getRenderEngine().getDepthOfFieldEffect()  } to <code>distance between camera and lookAt point+10</code>.<br>
 *
 * @param <T> GameEngine that implements IGameEngine
 */
public class PositionCamera<T extends IGameEngine> extends Task<T> {
    private final float   fieldOfView;
    private final Vector3 lookAt;
    private final Vector3 position;
    private final int     zoomIndex;

    public PositionCamera(T gameEngine, int zoomIndex, Vector3 position, Vector3 lookAt, float fieldOfView) {
        super(gameEngine, 0);
        this.zoomIndex   = zoomIndex;
        this.position    = position;
        this.lookAt      = lookAt;
        this.fieldOfView = fieldOfView;
    }

    @Override
    public boolean execute(float deltaTime) {
        logger.info("execute PositionCamera");
        gameEngine.getCamera().position.set(position);
        gameEngine.getCamera().up.set(0, 1, 0);
        gameEngine.getRenderEngine().getDepthOfFieldEffect().setFocalDepth(position.dst(lookAt) + 10f);
        gameEngine.getCamera().lookAt(lookAt);
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
    public void subExecute(float deltaTime) {

    }
}
