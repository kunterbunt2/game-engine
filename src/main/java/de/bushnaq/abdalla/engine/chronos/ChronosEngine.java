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
import de.bushnaq.abdalla.engine.audio.OpenAlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ChronosEngine<T extends IGameEngine> {
    private       boolean       enabled;
    private final float         fieldOfView;
    //    boolean firstTask = true;
    private final T             gameEngine;
    public        int           index  = 0;
    private final Logger        logger = LoggerFactory.getLogger(this.getClass());
    public        long          startTime;//start time of demo
    private final List<Task<T>> tasks  = new ArrayList<>();
    private final float         textY  = 0;

    public ChronosEngine(T gameEngine, boolean enabled) throws OpenAlException {
        this.gameEngine  = gameEngine;
        this.enabled     = enabled;
        this.fieldOfView = gameEngine.getCamera().fieldOfView;
//        if (enabled) {
//            startDemoMode();
//        }
    }

    public void add(Task<T> task) {
        if (tasks.isEmpty())
            startTime = System.currentTimeMillis();
        tasks.add(task);
    }

    public void executeTasks(float deltaTime) throws OpenAlException {
//        if (tasks.isEmpty()) startDemoMode();
        if (!tasks.isEmpty()) {
            if (tasks.get(0).execute(deltaTime)) {
                tasks.remove(0);
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

//    public void renderDemo(float deltaTime) throws IOException, OpenAlException {
//
//        if (enabled) {
//            executeTasks(deltaTime);
//        } else {
//            gameEngine.getRenderEngine().getFadeEffect().setEnabled(false);
////            gameEngine.getCamera().fieldOfView = GameEngine.FIELD_OF_VIEW_Y;
//        }
//    }

    public void resetAllEffects() {
        logger.info("resetAllEffects");
        gameEngine.getCamera().fieldOfView = fieldOfView;
        tasks.clear();
    }

    public long runningSince() {
        if (tasks.isEmpty())
            return 0;
        return tasks.get(0).secondToRun();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
