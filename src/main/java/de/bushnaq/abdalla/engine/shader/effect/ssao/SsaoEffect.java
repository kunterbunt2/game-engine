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

public class SsaoEffect<T extends RenderEngineExtension> extends ShaderVfxEffect implements ChainVfxEffect {

    private static final String       u_colorTexture = "u_colorTexture";
    private static final String       u_ssaoTexture  = "u_ssaoTexture";
    private final        MovingCamera camera;
    private              boolean      enabled        = false;
    private final        FrameBuffer  postFbo;
    private final        Vector2      resolution     = new Vector2();
    private final        Ssao         ssao;
    private final        VfxManager   vfxManager;

    public SsaoEffect(VfxManager vfxManager, final FrameBuffer postFbo, final MovingCamera camera, Ssao ssao) {
        super(MyVfxGLUtils.compileShader(Gdx.files.classpath("shader/ssao/ssao_combine_vertex.glsl"), Gdx.files.classpath("shader/ssao/ssao_combine_fragment.glsl"), ""));
        this.vfxManager = vfxManager;
        this.postFbo    = postFbo;
        this.camera     = camera;
        this.ssao       = ssao;

        rebind();
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
        program.setUniformi(u_colorTexture, TEXTURE_HANDLE0);
        program.setUniformi(u_ssaoTexture, TEXTURE_HANDLE1);
        program.end();
    }

    public void render(final VfxRenderContext context, final VfxFrameBuffer src, final VfxFrameBuffer dst) {
        program.bind();
        // Bind textures
        postFbo.getColorBufferTexture().bind(TEXTURE_HANDLE0);
        ssao.getSsaoFbo().getColorBufferTexture().bind(TEXTURE_HANDLE1);

        // Apply shader effect and render result to dst buffer.
        renderShader(context, dst);
    }

    @Override
    public void render(final VfxRenderContext context, final VfxPingPongWrapper buffers) {
        render(context, buffers.getSrcBuffer(), buffers.getDstBuffer());
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
