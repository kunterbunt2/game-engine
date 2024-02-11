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

package de.bushnaq.abdalla.engine.shader.mirror;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;

/**
 * @author kunterbunt
 */
public class Mirror {
    private boolean     present = false;
    private FrameBuffer reflectionFbo;

    private float reflectivity = 0.5f;

    public Mirror() {

    }

    public void createFrameBuffer() {
        {
            final FrameBufferBuilder frameBufferBuilder = new FrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGBA8, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE);
            frameBufferBuilder.addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24);
            reflectionFbo = frameBufferBuilder.build();
        }
    }

    public void dispose() {
        reflectionFbo.dispose();
    }

    public FrameBuffer getReflectionFbo() {
        return reflectionFbo;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public void setReflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
    }

}
