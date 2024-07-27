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

package de.bushnaq.abdalla.engine.shader.effect;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;
import com.crashinvaders.vfx.gl.VfxGLUtils;

public class FadeEffect extends ShaderVfxEffect implements ChainVfxEffect {

    private static final String  SATURATION         = "u_saturation";
    private static final String  SATURATION_MUL     = "u_saturationMul";
    private static final String  TEXTURE0           = "u_texture0";
    private static final String  VIGNETTE_INTENSITY = "u_intensity";
    private              float   intensity          = 1f;
    private              float   saturation         = 1f;
    private final        boolean saturationEnabled;
    private              float   saturationMul      = 1f;

    public FadeEffect(boolean controlSaturation) {
        super(VfxGLUtils.compileShader(
                Gdx.files.classpath("gdxvfx/shaders/screenspace.vert"),
                Gdx.files.classpath("shader/fade.frag"),
                (controlSaturation ? "#define CONTROL_SATURATION" : "")));
        this.saturationEnabled = controlSaturation;
        rebind();
    }

    public float getIntensity() {
        return intensity;
    }

    public float getSaturation() {
        return saturation;
    }

    public float getSaturationMul() {
        return saturationMul;
    }

    public boolean isSaturationControlEnabled() {
        return saturationEnabled;
    }

    @Override
    public void rebind() {
        program.begin();
        program.setUniformi(TEXTURE0, TEXTURE_HANDLE0);

        if (saturationEnabled) {
            program.setUniformf(SATURATION, saturation);
            program.setUniformf(SATURATION_MUL, saturationMul);
        }

        program.setUniformf(VIGNETTE_INTENSITY, intensity);
        program.end();
    }

    @Override
    public void render(VfxRenderContext context, VfxPingPongWrapper buffers) {
        render(context, buffers.getSrcBuffer(), buffers.getDstBuffer());
    }

    public void render(VfxRenderContext context, VfxFrameBuffer src, VfxFrameBuffer dst) {
        // Bind src buffer's texture as a primary one.
        src.getTexture().bind(TEXTURE_HANDLE0);
        program.begin();

        if (saturationEnabled) {
            program.setUniformf(SATURATION, saturation);
            program.setUniformf(SATURATION_MUL, saturationMul);
        }

        program.setUniformf(VIGNETTE_INTENSITY, intensity);
        program.end();
        // Apply shader effect and render result to dst buffer.
        renderShader(context, dst);
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
//        setUniform(VIGNETTE_INTENSITY, intensity);
        setSaturation(intensity);
    }

    public void setSaturation(float saturation) {
        this.saturation = saturation;
        if (saturationEnabled) {
//            setUniform(SATURATION, saturation);
        }
    }

    public void setSaturationMul(float saturationMul) {
        this.saturationMul = saturationMul;
        if (saturationEnabled) {
//            setUniform(SATURATION_MUL, saturationMul);
        }
    }

}
