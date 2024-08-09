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

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import net.mgsx.gltf.scene3d.shaders.PBRShader;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

/**
 * @author kunterbunt
 */
public class SsaoShaderProvider extends PBRShaderProvider {

    private final FrameBuffer postFbo;
    private final Ssao        ssao;
    public        SsaoShader  ssaoShader;

    public SsaoShaderProvider(final PBRShaderConfig config, final Ssao ssao, FrameBuffer postFbo) {
        super(config);
        this.ssao    = ssao;
        this.postFbo = postFbo;
    }

    public static SsaoShaderProvider createDefault(final PBRShaderConfig config, final Ssao ssao, FrameBuffer postFbo) {
        return new SsaoShaderProvider(config, ssao, postFbo);
    }

    public String createPrefixBase(final Renderable renderable, final Config config) {

        final String defaultPrefix = DefaultShader.createPrefix(renderable, config);
        String       version       = null;
        if (isGL3()) {
            if (Gdx.app.getType() == ApplicationType.Desktop) {
                if (version == null) {
//					version = "#version 150\n" + "#define GLSL3\n";
                }
            } else if (Gdx.app.getType() == ApplicationType.Android) {
                if (version == null)
                    version = "#version 300 es\n" + "#define GLSL3\n";
            }
        }
        String prefix = "";
        if (version != null)
            prefix += version;
        prefix += defaultPrefix;

        return prefix;
    }

    @Override
    public String createPrefixBase(Renderable renderable, PBRShaderConfig config) {

        String defaultPrefix = DefaultShader.createPrefix(renderable, config);
        String version       = config.glslVersion;
        if (isGL3()) {
            if (Gdx.app.getType() == ApplicationType.Desktop) {
                if (version == null) {
//					version = "#version 150\n" + "#define GLSL3\n";
                }
            } else if (Gdx.app.getType() == ApplicationType.Android) {
                if (version == null) {
                    version = "#version 300 es\n" + "#define GLSL3\n";
                }
            }
        }
        String prefix = "";
        if (version != null)
            prefix += version;
        if (config.prefix != null)
            prefix += config.prefix;
        prefix += defaultPrefix;

        return prefix;
    }

    @Override
    protected Shader createShader(final Renderable renderable) {
        return createSsaoShader(renderable);
    }

    @Override
    protected PBRShader createShader(final Renderable renderable, final PBRShaderConfig config, final String prefix) {
        return new SsaoShader(renderable, config, prefix, ssao, postFbo);
    }

    //TODO remove
    public Shader createShaderPublic(final Renderable renderable) {
        return createShader(renderable);
    }

    private Shader createSsaoShader(final Renderable renderable) {
        final String prefix = createPrefixBase(renderable, config);
        final Config config = new Config();
        config.vertexShader   = Gdx.files.internal("shader/ssao/ssao_vertex.glsl").readString();
        config.fragmentShader = Gdx.files.internal("shader/ssao/ssao_fragment.glsl").readString();
//        config.vertexShader   = Gdx.files.internal("net/mgsx/gltf/shaders/gdx-pbr.vs.glsl").readString();
//        config.fragmentShader = Gdx.files.internal("net/mgsx/gltf/shaders/gdx-pbr.fs.glsl").readString();
        ssaoShader = new SsaoShader(renderable, config, prefix, ssao, postFbo);
        return ssaoShader;

    }

    @Override
    public void dispose() {
        // pbrShader.dispose();
        // waterShader.dispose();
        super.dispose();
    }

    @Override
    protected boolean isGL3() {
        return Gdx.graphics.getGLVersion().isVersionEqualToOrHigher(3, 0);
    }

}
