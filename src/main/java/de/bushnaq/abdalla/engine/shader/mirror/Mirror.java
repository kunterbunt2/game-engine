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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;

/**
 * @author kunterbunt
 */
public class Mirror {
    private       float       mirrorLevel             = 0;
    private       boolean     present                 = false;
    private final Plane       reflectionClippingPlane = new Plane(new Vector3(0f, 1f, 0f), 0.1f);// render everything above d
    private       FrameBuffer reflectionFbo;
    FrameBuffer reflectionPostFbo;
    private float reflectivity = 0.5f;

    public Mirror() {

    }

    public void copyFbo() {
        reflectionFbo.transfer(reflectionPostFbo);
    }

    public void createFrameBuffer(int msaaSamples) {
        {
            final FrameBufferBuilder frameBufferBuilder = new FrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), msaaSamples);
            frameBufferBuilder.addColorRenderBuffer(GL30.GL_RGBA8).addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24).build();
            reflectionFbo = frameBufferBuilder.build();
        }
        {
            final GLFrameBuffer.FrameBufferBuilder frameBufferBuilder = new GLFrameBuffer.FrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGBA8, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE);
            frameBufferBuilder.addDepthTextureAttachment(GL30.GL_DEPTH_COMPONENT24, GL20.GL_UNSIGNED_BYTE);
            reflectionPostFbo = frameBufferBuilder.build();
            reflectionPostFbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }

    }

    public void dispose() {
        reflectionFbo.dispose();
        reflectionPostFbo.dispose();
    }

    public float getMirrorLevel() {
        return mirrorLevel;
    }

    public Plane getReflectionClippingPlane() {
        return reflectionClippingPlane;
    }

    public FrameBuffer getReflectionFbo() {
        return reflectionFbo;
    }

    public FrameBuffer getReflectionPostFbo() {
        return reflectionPostFbo;
    }

    public float getReflectivity() {
        return reflectivity;
    }

    public boolean isPresent() {
        return present;
    }

    public void setMirrorLevel(float level) {
        mirrorLevel               = level;
        reflectionClippingPlane.d = -mirrorLevel + .1f;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public void setReflectivity(float reflectivity) {
        this.reflectivity = reflectivity;
    }

}
