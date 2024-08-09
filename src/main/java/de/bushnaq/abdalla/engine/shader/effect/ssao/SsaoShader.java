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

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import net.mgsx.gltf.scene3d.shaders.PBRShader;

import java.util.Random;

/**
 * @author kunterbunt
 */
public class SsaoShader extends PBRShader {
    private static final int         KERNEL_SIZE          = 16;
    private final        float[]     kernelSamples;
    private final        Texture     noiseTexture;
    private final        FrameBuffer postFbo;
    private final        Ssao        ssao;
    private final        int         u_bias               = register("uBias");
    private final        int         u_depthTexture       = register("u_depthMap");
    private final        int         u_intensity          = register("uIntensity");
    private final        int         u_noiseScale         = register("u_noiseScale");
    private final        int         u_noiseTexture       = register("u_noise");
    private final        int         u_projectionMatrix   = register("u_projection");
    private final        int         u_projection_inverse = register("u_projection_inverse");
    private final        int         u_radius             = register("u_sampleRad");
    private final        int         u_sourceTexture      = register("u_sourceTexture");

    public SsaoShader(final Renderable renderable, final Config config, final String prefix, Ssao ssao, FrameBuffer postFbo) {
        super(renderable, config, prefix);
        this.ssao     = ssao;
        this.postFbo  = postFbo;
        noiseTexture  = createNoiseTexture();
        kernelSamples = new float[KERNEL_SIZE * 3];
        Random random = new Random();
        for (int i = 0; i < KERNEL_SIZE; i++) {
            float scale = (float) i / (float) KERNEL_SIZE;
            float x     = random.nextFloat() * 2.0f - 1.0f;
            float y     = random.nextFloat() * 2.0f - 1.0f;
            float z     = random.nextFloat(); // only in hemisphere
//            kernelSamples.put(new float[]{x, y, z});
            kernelSamples[i * 3]     = x * scale;
            kernelSamples[i * 3 + 1] = y * scale;
            kernelSamples[i * 3 + 2] = z * scale;
        }
//        kernelSamples.flip();
    }

    @Override
    public void begin(final Camera camera, final RenderContext context) {
        super.begin(camera, context);

        program.bind();
        // Bind textures
//        set(u_sourceTexture, postFbo.getTextureAttachments().get(0));
        set(u_depthTexture, postFbo.getTextureAttachments().get(1));
        set(u_noiseTexture, noiseTexture);
        set(u_projectionMatrix, camera.projection);
        set(u_projection_inverse, camera.invProjectionView);
//        set(u_noiseScale, Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 4);
        set(u_radius, 0.5f);//default 0.5f
        set(u_bias, 0.025f);//default 0.025f
        set(u_intensity, 10.0f);//default 1.0f
//        program.setUniformf("znear", camera.near);
//        program.setUniformf("zfar", camera.far);
        program.setUniform3fv("u_kernel", kernelSamples, 0, KERNEL_SIZE * 3);
    }

    @Override
    public boolean canRender(final Renderable renderable) {
        return true;
    }

    private Texture createNoiseTexture() {
        Random  random = new Random();
        float[] noise  = new float[16 * 3];
        for (int i = 0; i < 16 * 3; i++) {
            noise[i] = random.nextFloat() * 2.0f - 1.0f;
        }

        Pixmap pixmap = new Pixmap(4, 4, Pixmap.Format.RGB888);
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                int index = (x + y * 4) * 3;
                int r     = (int) ((noise[index] * 0.5f + 0.5f) * 255);
                int g     = (int) ((noise[index + 1] * 0.5f + 0.5f) * 255);
                int b     = (int) ((noise[index + 2] * 0.5f + 0.5f) * 255);
                pixmap.drawPixel(x, y, (r << 24) | (g << 16) | (b << 8) | 0xFF);
            }
        }

        Texture noiseTexture = new Texture(pixmap);
        pixmap.dispose();
        return noiseTexture;
    }

    public String getLog() {
        return program.getLog();
    }

    @Override
    public void render(final Renderable renderable) {
        super.render(renderable);
    }

}
