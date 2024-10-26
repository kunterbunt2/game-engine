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

import de.bushnaq.abdalla.engine.IGameEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.bushnaq.abdalla.engine.shader.effect.scheduled.SchedulePhase.EXECUTE;
import static de.bushnaq.abdalla.engine.shader.effect.scheduled.SchedulePhase.START;

public class FadeOutTask<T extends IGameEngine> extends ScheduledTask<T> {
    protected final Logger        logger = LoggerFactory.getLogger(this.getClass());
    private         SchedulePhase mode   = START;

    public FadeOutTask(T gameEngine) {
        super(gameEngine, 1);
    }

    public FadeOutTask(T gameEngine, float durationSeconds) {
        super(gameEngine, durationSeconds);
    }

    public boolean execute(float deltaTime) {
        boolean returnValue = false;
        switch (mode) {
            case EXECUTE -> {
                subexecute(deltaTime);
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

    public void subexecute(float deltaTime) {
        long deltaSeconds = (System.currentTimeMillis() - taskStartTime);
        gameEngine.getRenderEngine().getFadeEffect().setIntensity(1f - ((float) deltaSeconds / durationMs));
    }

}
