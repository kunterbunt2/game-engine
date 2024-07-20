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
import de.bushnaq.abdalla.engine.camera.MovingCamera;

public class DepthOfFieldEffect extends ShaderVfxEffect implements ChainVfxEffect {

    private static final String       Texture0     = "u_sourceTexture";
    private static final String       Texture1     = "u_depthTexture";
    private final        MovingCamera camera;
    private              boolean      enabled      = false;
    private              float        focalDepth   = 100f;
    private              float        farDofStart  = focalDepth / 20f;
    private              float        farDofDist   = focalDepth * 1.5f;
    private              float        nearDofDist  = focalDepth * 1.5f;
    private              float        nearDofStart = focalDepth / 20f;
    private final        FrameBuffer  postFbo;
    private final        Vector2      resolution   = new Vector2();
    private final        VfxManager   vfxManager;

    public DepthOfFieldEffect(VfxManager vfxManager, final FrameBuffer postFbo, final MovingCamera camera) {
        super(MyVfxGLUtils2.compileShader(Gdx.files.classpath("shader/depthOfField.vs.glsl"), Gdx.files.classpath("shader/depthOfField2.fs.glsl"), ""));
        this.vfxManager = vfxManager;
        this.postFbo    = postFbo;
        this.camera     = camera;
        rebind();
    }

    public MovingCamera getCamera() {
        return camera;
    }

    public float getFarDofDist() {
        return farDofDist;
    }

    public float getFarDofStart() {
        return farDofStart;
    }

    public float getFocalDepth() {
        return focalDepth;
    }

    public float getNearDofDist() {
        return nearDofDist;
    }

    public float getNearDofStart() {
        return nearDofStart;
    }

//    public Vector2 getFocusDistance() {
//        return focusDistance;
//    }

    public boolean isEnabled() {
        return enabled;
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
        // Bind src buffer's texture as a primary one.
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
        this.focalDepth = focalDepth;
        nearDofStart    = focalDepth / 20f;
        nearDofDist     = focalDepth * 1.5f;
        farDofStart     = focalDepth / 20f;
        farDofDist      = focalDepth * 1.5f;
    }

//    public void setFocusDistance(Vector2 focusDistance) {
//        this.focusDistance = focusDistance;
//    }

    @Override
    public void update(final float delta) {
        super.update(delta);
    }

    //	public void enableDepthOfFiled(boolean enableDepthOfField) {
    //		if (enableDepthOfField) {
    //			nearDistanceBlur = 100.0f;
    //			farDistanceBlur = 100.0f;
    //		} else {
    //			nearDistanceBlur = 0.0f;
    //			farDistanceBlur = 0.0f;
    //			nearDistanceBlur = 100.0f;
    //			farDistanceBlur = 100.0f;
    //		}
    //	}
}

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
        final String srcFrag     = exchange(fragmentFile.readString());

        final ShaderProgram shader = new ShaderProgram(prependVert + "\n" + srcVert, prependFrag + "\n" + srcFrag);

        if (!shader.isCompiled()) {
            throw new GdxRuntimeException("Shader compile error: " + vertexFile.name() + "/" + fragmentFile.name() + "\n" + shader.getLog());
        }
        return shader;
    }

    public static String exchange(String shader) {
        shader = shader.replaceAll("MAX_BLUR", String.valueOf(maxBlur));
        shader = shader.replaceAll("UNPACK_FUNCTION;", Gdx.files.classpath("shader/unpackVec3ToFloat.glsl").readString());
        shader = shader.replaceAll("BLUR_BACKGROUND", String.valueOf(blurBackground));
        return shader;
    }
}
