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

package de.bushnaq.abdalla.engine.util;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.profiling.GLProfiler;

public class ExtendedGLProfiler extends GLProfiler {
    private int dynamicText3D             = 0;
    private int staticText3D              = 0;
    private int visibleDynamicGameObjects = 0;
    private int visibleStaticGameObjects  = 0;

    /**
     * Create a new instance of GLProfiler to monitor a {@link Graphics} instance's gl calls
     *
     * @param graphics instance to monitor with this instance, With Lwjgl 2.x you can pass in Gdx.graphics, with Lwjgl3 use
     *                 Lwjgl3Window.getGraphics()
     */
    public ExtendedGLProfiler(Graphics graphics) {
        super(graphics);
    }

    public int getDynamicText3D() {
        return dynamicText3D;
    }

    public int getStaticText3D() {
        return staticText3D;
    }

    public int getVisibleDynamicGameObjects() {
        return visibleDynamicGameObjects;
    }

    public int getVisibleStaticGameObjects() {
        return visibleStaticGameObjects;
    }

    public void reset() {
        super.reset();
        setStaticText3D(0);
        setDynamicText3D(0);
        setVisibleStaticGameObjects(0);
        setVisibleDynamicGameObjects(0);
    }

    public void setDynamicText3D(int dynamicText3D) {
        this.dynamicText3D = dynamicText3D;
    }

    public void setStaticText3D(int staticText3D) {
        this.staticText3D = staticText3D;
    }

    public void setVisibleDynamicGameObjects(int visibleDynamicGameObjects) {
        this.visibleDynamicGameObjects = visibleDynamicGameObjects;
    }

    public void setVisibleStaticGameObjects(int visibleStaticGameObjects) {
        this.visibleStaticGameObjects = visibleStaticGameObjects;
    }
}
