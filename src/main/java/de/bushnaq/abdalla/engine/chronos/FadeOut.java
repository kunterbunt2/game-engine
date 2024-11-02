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

import com.badlogic.gdx.graphics.Color;
import de.bushnaq.abdalla.engine.IGameEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.bushnaq.abdalla.engine.chronos.ChronosPhase.EXECUTE;
import static de.bushnaq.abdalla.engine.chronos.ChronosPhase.START;

public class FadeOut<T extends IGameEngine> extends Task<T> {
    private         Color  backgroundColor =Color.BLACK;
    protected final Logger logger          = LoggerFactory.getLogger(this.getClass());
    private         ChronosPhase mode   = START;

    public FadeOut(T gameEngine) {
        super(gameEngine, 1);
    }

    public FadeOut(T gameEngine, Color backgroundColor, float durationSeconds) {
        super(gameEngine, durationSeconds);
        this.backgroundColor = backgroundColor;
    }

    public boolean execute(float deltaTime) {
        boolean returnValue = false;
        switch (mode) {
            case EXECUTE -> {
                subExecute(deltaTime);
                if (taskStartTime + durationMs <= System.currentTimeMillis()) {
                    logger.info("stop FadeOutTask");
                    returnValue = true;
                }
            }
            case START -> {
                logger.info("start FadeOutTask");
                mode          = EXECUTE;
                taskStartTime = System.currentTimeMillis();
            }
        }
        return returnValue;
    }

    @Override
    public long secondToRun() {
        return taskStartTime + durationMs - System.currentTimeMillis();
    }

    public void subExecute(float deltaTime) {
        long deltaSeconds = (System.currentTimeMillis() - taskStartTime);
        gameEngine.getRenderEngine().getFadeEffect().setIntensity(1f - ((float) deltaSeconds / durationMs));
        gameEngine.getRenderEngine().getFadeEffect().setBackgroundColor(backgroundColor);
    }

}
