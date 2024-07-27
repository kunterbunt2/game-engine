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

package de.bushnaq.abdalla.engine.shader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;
import com.crashinvaders.vfx.gl.VfxGLUtils;
import de.bushnaq.abdalla.engine.RenderEngineExtension;
import de.bushnaq.abdalla.engine.camera.MovingCamera;

class MyVfxGLUtils2 extends VfxGLUtils {
    private static final String  TAG            = MyVfxGLUtils2.class.getSimpleName();
    private static final boolean blurBackground = true;
    private static final int     maxBlur        = 50;

    public static ShaderProgram compileShader(final FileHandle vertexFile, final FileHandle fragmentFile, final String defines) {
        if (fragmentFile == null) {
            throw new IllegalArgumentException("Vertex shader file cannot be null.");
        }
        if (vertexFile == null) {
            throw new IllegalArgumentException("Fragment shader file cannot be null.");
        }
        if (defines == null) {
            throw new IllegalArgumentException("Defines cannot be null.");
        }

        final StringBuilder sb = new StringBuilder();
        sb.append("Compiling \"").append(vertexFile.name()).append('/').append(fragmentFile.name()).append('\"');
        if (defines.length() > 0) {
            sb.append(" w/ (").append(defines.replace("\n", ", ")).append(")");
        }
        sb.append("...");
        Gdx.app.log(TAG, sb.toString());

        final String prependVert = prependVertexCode + defines;
        final String prependFrag = prependFragmentCode + defines;
        final String srcVert     = vertexFile.readString();
        final String srcFrag     = fragmentFile.readString();
//        exchange(fragmentFile.readString());

        final ShaderProgram shader = new ShaderProgram(prependVert + "\n" + srcVert, prependFrag + "\n" + srcFrag);

        if (!shader.isCompiled()) {
            throw new GdxRuntimeException("Shader compile error: " + vertexFile.name() + "/" + fragmentFile.name() + "\n" + shader.getLog());
        }
        return shader;
    }

}

public class SsaoEffect<T extends RenderEngineExtension> extends ShaderVfxEffect implements ChainVfxEffect {

    private static final String       Texture0   = "u_sourceTexture";
    private static final String       Texture1   = "u_depthTexture";
    private final        MovingCamera camera;
    private              boolean      enabled    = false;
    private final        FrameBuffer  postFbo;
    private final        Vector2      resolution = new Vector2();
    private final        VfxManager   vfxManager;

    public SsaoEffect(VfxManager vfxManager, final FrameBuffer postFbo, final MovingCamera camera) {
        super(MyVfxGLUtils2.compileShader(Gdx.files.classpath("shader/ssao.vs.glsl"), Gdx.files.classpath("shader/ssao.fs.glsl"), ""));
        this.vfxManager = vfxManager;
        this.postFbo    = postFbo;
        this.camera     = camera;
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
        program.setUniformi(Texture0, TEXTURE_HANDLE0);
        program.setUniformi(Texture1, TEXTURE_HANDLE1);
        program.end();
    }

    @Override
    public void render(final VfxRenderContext context, final VfxPingPongWrapper buffers) {
        render(context, buffers.getSrcBuffer(), buffers.getDstBuffer());
    }

    public void render(final VfxRenderContext context, final VfxFrameBuffer src, final VfxFrameBuffer dst) {
//        Gdx.gl.glDepthMask(false);
        // Bind src buffer's texture as a primary one.
        program.begin();
//        postFbo.getColorBufferTexture().bind(TEXTURE_HANDLE0);
//        postFbo.getTextureAttachments().get(1).bind(TEXTURE_HANDLE1);
//        glBindFramebuffer(GL_FRAMEBUFFER, ssaoFramebuffer);
//        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//        glUseProgram(shaders[1].shaderID);
//        camera.uploadUniforms(shaders[1]);
//
//        glActiveTexture(GL_TEXTURE0);
//        glBindTexture(GL_TEXTURE_2D, geometryPositionTexture);
//        glUniform1i(shaders[1].uni_texture[0], 0);
//
//        glActiveTexture(GL_TEXTURE1);
//        glBindTexture(GL_TEXTURE_2D, geometryNormalTexture);
//        glUniform1i(shaders[1].uni_texture[1], 1);
//
//        glActiveTexture(GL_TEXTURE2);
//        glBindTexture(GL_TEXTURE_2D, ssaoNoiseTexture);
//        glUniform1i(shaders[1].uni_texture[2], 2);
//
//        glUniform1(shaders[1].uni_special[0], kernelSamples);
//        glUniform1i(shaders[1].uni_special[1], kernelSize);
//        glUniform1f(shaders[1].uni_special[2], radius);
//        glUniform1f(shaders[1].uni_special[3], bias);

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

    @Override
    public void update(final float delta) {
        super.update(delta);
    }
}
