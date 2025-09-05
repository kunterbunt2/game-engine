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
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;
import de.bushnaq.abdalla.engine.IGameEngine;
import de.bushnaq.abdalla.engine.camera.MovingCamera;
import lombok.Getter;
import lombok.Setter;

public class DepthOfFieldEffect<T extends IGameEngine> extends ShaderVfxEffect implements ChainVfxEffect {

    public static final  float        DISTANT_FOCAL_DEPTH_DIST_MULTIPLIER  = 10;
    public static final  float        DISTANT_FOCAL_DEPTH_START_MULTIPLIER = 2;
    private static final String       Texture0                             = "u_sourceTexture";
    private static final String       Texture1                             = "u_depthTexture";
    @Getter
    private final        MovingCamera camera;
    @Getter
    private              boolean      enabled                              = false;
    @Getter
    private              float        focalDepth                           = 100f;
    @Getter
    private              float        farDofStart                          = focalDepth / 20f;
    @Getter
    private              float        farDofDist                           = focalDepth * DISTANT_FOCAL_DEPTH_DIST_MULTIPLIER;
    @Setter
    @Getter
    private              float        gain                                 = 1;
    @Setter
    @Getter
    private              float        maxblur                              = 1f;
    @Getter
    private              float        nearDofDist                          = focalDepth * DISTANT_FOCAL_DEPTH_DIST_MULTIPLIER;
    private final        boolean      nearDofEnabled                       = false;
    @Getter
    private              float        nearDofStart                         = focalDepth / 20f;
    private final        FrameBuffer  postFbo;
    private final        Vector2      resolution                           = new Vector2();
    @Setter
    @Getter
    private              float        threshold                            = .9f;
    private final        VfxManager   vfxManager;

    public DepthOfFieldEffect(VfxManager vfxManager, final FrameBuffer postFbo, final MovingCamera camera) {
        super(MyVfxGLUtils.compileShader(Gdx.files.classpath("shader/depthOfField/depthOfField.vs.glsl"), Gdx.files.classpath("shader/depthOfField/depthOfField.fs.glsl"), ""));
        this.vfxManager = vfxManager;
        this.postFbo    = postFbo;
        this.camera     = camera;
        rebind();
    }

    public boolean isInFocus(float depth) {
        return depth < focalDepth + farDofStart && depth > focalDepth - nearDofStart;
    }

    @Override
    public void rebind() {
        super.rebind();
        program.begin();
        program.setUniformi(Texture0, TEXTURE_HANDLE0);
        program.setUniformi(Texture1, TEXTURE_HANDLE1);
        program.end();
    }

    @Override
    public void render(final VfxRenderContext context, final VfxPingPongWrapper buffers) {
        render(context, buffers.getSrcBuffer(), buffers.getDstBuffer());
    }

    public void render(final VfxRenderContext context, final VfxFrameBuffer src, final VfxFrameBuffer dst) {
        program.begin();
        postFbo.getColorBufferTexture().bind(TEXTURE_HANDLE0);
        postFbo.getTextureAttachments().get(1).bind(TEXTURE_HANDLE1);
        program.setUniformf("focalDepth", focalDepth);
        program.setUniformf("ndofstart", nearDofStart);
        program.setUniformf("ndofdist", nearDofDist);
        program.setUniformf("fdofstart", farDofStart);
        program.setUniformf("fdofdist", farDofDist);
        program.setUniformf("znear", camera.near);
        program.setUniformf("zfar", camera.far);
        program.setUniformf("threshold", threshold);
        program.setUniformf("gain", gain);
        program.setUniformf("maxblur", maxblur);
        program.end();

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

    public void setFocalDepth(float focalDepth) {
//        System.out.printf("focalDepth%f%n", focalDepth);
        this.focalDepth = focalDepth;
        if (nearDofEnabled) {
            nearDofStart = focalDepth / 20f;
            nearDofDist  = focalDepth * DISTANT_FOCAL_DEPTH_DIST_MULTIPLIER;
        } else {
            nearDofStart = focalDepth * DISTANT_FOCAL_DEPTH_DIST_MULTIPLIER;
            nearDofDist  = focalDepth * DISTANT_FOCAL_DEPTH_DIST_MULTIPLIER;
        }
        farDofStart = focalDepth * DISTANT_FOCAL_DEPTH_START_MULTIPLIER;
        farDofDist  = focalDepth * DISTANT_FOCAL_DEPTH_DIST_MULTIPLIER;
    }

    @Override
    public void update(final float delta) {
        super.update(delta);
    }
}

