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

package de.bushnaq.abdalla.engine.shader.water;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;

/**
 * @author kunterbunt
 */
public class Water {
    private       boolean     present                 = false;
    private       FrameBuffer reflectionFbo;
    private       FrameBuffer refractionFbo;
    private       float       refractiveMultiplicator = 1.0f;
    private       float       tiling                  = 1f;
    private       float       waveSpeed               = 0.0f;
    private       float       waveStrength            = 0.00f;
    private final Plane       reflectionClippingPlane = new Plane(new Vector3(0f, 1f, 0f), 0.1f);                                // render everything above d
    private final Plane       refractionClippingPlane = new Plane(new Vector3(0f, -1f, 0f), (-0.1f));                            // render everything below d
    private float waterLevel  = 0;

    public Water() {

    }
    public float getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(float level) {
         waterLevel = level;
        reflectionClippingPlane.d = waterLevel+.1f;
        refractionClippingPlane.d = waterLevel-.1f;
    }
    public Plane getRefractionClippingPlane()
    {
        return refractionClippingPlane;
    }
    public Plane getReflectionClippingPlane()
    {
        return reflectionClippingPlane;
    }
    public void createFrameBuffer() {
        {
            final FrameBufferBuilder frameBufferBuilder = new FrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGBA8, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE);
            if (Gdx.app.getType() == ApplicationType.iOS) {
                frameBufferBuilder.addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24);// ios
            } else {
                frameBufferBuilder.addDepthTextureAttachment(GL30.GL_DEPTH_COMPONENT24, GL20.GL_UNSIGNED_BYTE);
            }
            refractionFbo = frameBufferBuilder.build();
        }
        {
            final FrameBufferBuilder frameBufferBuilder = new FrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGBA8, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE);
            frameBufferBuilder.addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24);
            reflectionFbo = frameBufferBuilder.build();
        }
    }

    public void dispose() {
        reflectionFbo.dispose();
        refractionFbo.dispose();
    }

    public FrameBuffer getReflectionFbo() {
        return reflectionFbo;
    }

    public FrameBuffer getRefractionFbo() {
        return refractionFbo;
    }

    public float getRefractiveMultiplicator() {
        return refractiveMultiplicator;
    }

    public float getTiling() {
        return tiling;
    }

    public float getWaveSpeed() {
        return waveSpeed;
    }

    public float getWaveStrength() {
        return waveStrength;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public void setRefractiveMultiplicator(float refractiveMultiplicator) {
        this.refractiveMultiplicator = refractiveMultiplicator;
    }

    public void setTiling(float tiling) {
        this.tiling = tiling;
    }

    public void setWaveSpeed(float waveSpeed) {
        this.waveSpeed = waveSpeed;
    }

    public void setWaveStrength(float waveStrength) {
        this.waveStrength = waveStrength;
    }

}
