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

package de.bushnaq.abdalla.engine.shader.effect.ssao;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;
import de.bushnaq.abdalla.engine.RenderEngineExtension;
import de.bushnaq.abdalla.engine.camera.MovingCamera;
import de.bushnaq.abdalla.engine.shader.effect.MyVfxGLUtils;

import java.util.Random;

public class SsaoEffect<T extends RenderEngineExtension> extends ShaderVfxEffect implements ChainVfxEffect {

    private static final String       uni_texture0_name = "uni_texture0";
    private static final String       uni_texture1_name = "uni_texture1";
    private static final String       uni_texture2_name = "uni_texture2";
    private final        float        bias              = 0.002f;
    private final        MovingCamera camera;
    private              boolean      enabled           = false;
    private final        float[]      kernelSamples;
    private final        int          kernelSize        = 64;
    private final        Texture      noiseTexture;
    private final        FrameBuffer  postFbo;
    private final        float        radius            = 1.0f;
    private final        Vector2      resolution        = new Vector2();
    private final        int          samples;
    private final        Ssao         ssao;
    private final        int          uni_P;
    private final        VfxManager   vfxManager;

    public SsaoEffect(VfxManager vfxManager, final FrameBuffer postFbo, final MovingCamera camera, Ssao ssao) {
        super(MyVfxGLUtils.compileShader(Gdx.files.classpath("shader/ssao/ssao-vert.glsl"), Gdx.files.classpath("shader/ssao/ssao-frag.glsl"), ""));
        this.vfxManager = vfxManager;
        this.postFbo    = postFbo;
        this.camera     = camera;
        this.ssao       = ssao;
        samples         = program.fetchUniformLocation("samples", true);
        uni_P           = program.fetchUniformLocation("uni_P", true);
        kernelSamples   = Util.kernel();
//        FloatBuffer noiseBuffer = Util.noise();
        noiseTexture = createNoiseTexture();
        resolution.x = Gdx.graphics.getWidth();
        resolution.y = Gdx.graphics.getHeight();
        rebind();
    }

    private Texture createNoiseTexture() {
        Random  random = new Random();
        float[] noise  = new float[16 * 3];
        for (int i = 0; i < 16; i++) {
            noise[i * 3]     = random.nextFloat() * 2.0f - 1.0f;
            noise[i * 3 + 1] = random.nextFloat() * 2.0f - 1.0f;
            noise[i * 3 + 2] = 0;
        }

        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGB888);
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                int index = (x + y * 4) * 3;
                int r     = (int) ((noise[index] * 0.5f + 0.5f) * 255);
                int g     = (int) ((noise[index + 1] * 0.5f + 0.5f) * 255);
                int b     = 0/*(int) ((noise[index + 2] * 0.5f + 0.5f) * 255)*/;
//                pixmap.setColor(noise[index], noise[index + 1], noise[index + 2], 1f);
//                pixmap.drawPixel(x, y);
                long result = ((long) r << 24) | ((long) g << 16) | (b << 8) | 0xFF;
                pixmap.drawPixel(x, y, (r << 24) | (g << 16) | (b << 8) | 0xFF);
            }
        }

        Texture noiseTexture = new Texture(pixmap);
        noiseTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        noiseTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        pixmap.dispose();
        return noiseTexture;
    }

    public MovingCamera getCamera() {
        return camera;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void rebind() {
        super.rebind();
        program.begin();
        program.setUniformi(uni_texture0_name, TEXTURE_HANDLE0);
        program.setUniformi(uni_texture1_name, TEXTURE_HANDLE1);
        program.setUniformi(uni_texture2_name, TEXTURE_HANDLE2);
        program.end();
    }

    @Override
    public void render(final VfxRenderContext context, final VfxPingPongWrapper buffers) {
        render(context, buffers.getSrcBuffer(), buffers.getDstBuffer());
    }

    public void render(final VfxRenderContext context, final VfxFrameBuffer src, final VfxFrameBuffer dst) {
        program.bind();
        // Bind textures
        ssao.getSsaoFbo().getTextureAttachments().get(0).bind(TEXTURE_HANDLE0);
        ssao.getSsaoFbo().getTextureAttachments().get(1).bind(TEXTURE_HANDLE1);
        noiseTexture.bind(TEXTURE_HANDLE2);
        program.setUniform1fv(samples, kernelSamples, 0, kernelSamples.length);
        program.setUniformi("kernelSize", kernelSize);
        program.setUniformf("radius", radius);
        program.setUniformf("bias", bias);
        program.setUniformMatrix(uni_P, camera.projection);
        program.setUniformf("noiseScale", resolution);

        // Apply shader effect and render result to dst buffer.
        renderShader(context, dst);
    }

    @Override
    public void resize(final int width, final int height) {
        super.resize(width, height);
        this.resolution.set(width, height);
        rebind();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled)
            vfxManager.addEffect(this);
        else
            vfxManager.removeEffect(this);
    }

    @Override
    public void update(final float delta) {
        super.update(delta);
    }

}
