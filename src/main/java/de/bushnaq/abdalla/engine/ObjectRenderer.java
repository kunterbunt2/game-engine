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

package de.bushnaq.abdalla.engine;


/**
 * @author kunterbunt
 */
public abstract class ObjectRenderer<T extends RenderEngineExtension> {
    public void create(final float x, final float y, final float z, final RenderEngine3D<T> renderEngine) {
    }

    public void create(final RenderEngine3D<T> renderEngine) {
    }

    public void destroy(final RenderEngine3D<T> renderEngine) {
    }

    public void render(final float px, final float py, final RenderEngine2D<T> renderEngine, final int index, final boolean selected) {
    }

    public void render2D(final RenderEngine3D<T> renderEngine, final int index, final boolean selected) {
    }

    public void render2D(final float px, final float py, final RenderEngine3D<T> renderEngine, final int index, final boolean selected) {
    }

    public void render2Da(final RenderEngine3D<T> renderEngine, final int index, final boolean selected) {
    }

    public void renderText(final float aX, final float aY, final float aZ, final RenderEngine3D<T> renderEngine, final int index) {
    }

    public void renderText(final RenderEngine3D<T> renderEngine, final int index, final boolean selected) {
    }

    public void update(final float x, final float y, final float z, final RenderEngine3D<T> renderEngine, final long currentTime, final float timeOfDay, final int index, final boolean selected) throws Exception {
    }

    public void update(final RenderEngine3D<T> renderEngine, final long currentTime, final float timeOfDay, final int index, final boolean selected) throws Exception {
    }

    public boolean withinBounds(final float x, final float y) {
        return false;
    }
}
