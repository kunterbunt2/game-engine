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

import de.bushnaq.abdalla.engine.IGameEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.bushnaq.abdalla.engine.chronos.ChronosPhase.*;

/**
 * extend if you need a {@link Task Task} that executes {@link #subExecute(float) subExecute} once and then waits for durationSeconds.
 *
 * @param <T> GameEngine that implements IGameEngine
 */
public abstract class AbstractWaitAfterExecute<T extends IGameEngine> extends Task<T> {
    protected final Logger       logger = LoggerFactory.getLogger(this.getClass());
    private         ChronosPhase mode   = START;

    public AbstractWaitAfterExecute(T gameEngine, float durationSeconds) {
        super(gameEngine, durationSeconds);
    }

    public boolean execute(float deltaTime) {
        boolean returnValue = false;
        switch (mode) {
            case EXECUTE -> {
                subExecute(deltaTime);
                mode = WAIT;
            }
            case START -> {
                logger.info(String.format("start %s", this.getClass().getSimpleName()));
                mode          = EXECUTE;
                taskStartTime = System.currentTimeMillis();
            }
            case WAIT -> {
                if (taskStartTime + durationMs <= System.currentTimeMillis()) {
                    logger.info(String.format("stop %s", this.getClass().getSimpleName()));
                    returnValue = true;
                }
            }
        }
        return returnValue;
    }

    @Override
    public long secondToRun() {
        return taskStartTime + durationMs - System.currentTimeMillis();
    }

}
