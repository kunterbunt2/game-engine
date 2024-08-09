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

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Plane;
import de.bushnaq.abdalla.engine.shader.effect.ssao.Ssao;
import de.bushnaq.abdalla.engine.shader.effect.ssao.SsaoShader;
import de.bushnaq.abdalla.engine.shader.mirror.Mirror;
import de.bushnaq.abdalla.engine.shader.mirror.MirrorShader;
import de.bushnaq.abdalla.engine.shader.water.Water;
import de.bushnaq.abdalla.engine.shader.water.WaterShader;

/**
 * @author kunterbunt
 */
public class GameShaderProvider extends DefaultShaderProvider implements GameShaderProviderInterface {

    private       Plane        clippingPlane;
    private final Mirror       mirror;
    public        MirrorShader mirrorShader;
    private final FrameBuffer  postFbo;
    public        MyShader     shader;
    private final Ssao         ssao;
    private       boolean      ssaoEnabled = false;
    public        SsaoShader   ssaoShader;
    //	private final float			waterTiling;
//	private float				waveSpeed;
//	private float				waveStrength;
    private final Water        water;
    public        WaterShader  waterShader;

    public GameShaderProvider(final Config config, final Water water, final Mirror mirror, final Ssao ssao, FrameBuffer postFbo) {
        super(config);
        this.water   = water;
        this.mirror  = mirror;
        this.ssao    = ssao;
        this.postFbo = postFbo;
    }

    public static GameShaderProvider createDefault(final Config config, final Water water, final Mirror mirror, final Ssao ssao, FrameBuffer postFbo) {
        return new GameShaderProvider(config, water, mirror, ssao, postFbo);
    }

    private Shader createMirrorShader(final Renderable renderable) {
        final String prefix = createPrefixBase(renderable, config);
//		final String	prefix	= "";
//		final Config	config	= new Config();
        config.vertexShader   = Gdx.files.internal("shader/mirror/mirror.vs.glsl").readString();
        config.fragmentShader = Gdx.files.internal("shader/mirror/mirror.fs.glsl").readString();
        mirrorShader          = new MirrorShader(renderable, config, prefix, mirror);
//		setWaterAttribute(waterAttribute);
        mirrorShader.setClippingPlane(clippingPlane);
        return mirrorShader;

    }

    public String createPrefixBase(final Renderable renderable, final Config config) {

        final String defaultPrefix = DefaultShader.createPrefix(renderable, config);
        String       version       = null;
        if (isGL3()) {
            if (Gdx.app.getType() == ApplicationType.Desktop) {
                if (version == null) {
//					version = /* "#version 130\n" + */ "#define GLSL3\n";
                }
            } else if (Gdx.app.getType() == ApplicationType.Android) {
                if (version == null) version = "#version 300 es\n" + "#define GLSL3\n";
            }
        }
        String prefix = "";
        if (version != null) prefix += version;
        prefix += defaultPrefix;

        return prefix;
    }

    @Override
    protected Shader createShader(final Renderable renderable) {
        if (ssaoEnabled) return createSsaoShader(renderable);
        else if (renderable.material.id.equals("water")) {
            return createWaterShader(renderable);
        } else if (renderable.material.id.equals("shader/mirror")) {
            return createMirrorShader(renderable);
        } else {
            final String prefix = createPrefixBase(renderable, config);
            config.fragmentShader = null;
            config.vertexShader   = null;
            shader                = new MyShader(renderable, config, prefix);
            shader.setClippingPlane(clippingPlane);
            return shader;

        }
    }

    //	@Override
//	protected PBRShader createShader(final Renderable renderable, final Config config, final String prefix) {
//		return new MyPBRShader(renderable, config, prefix);
//	}
    private Shader createSsaoShader(final Renderable renderable) {
        final String prefix = createPrefixBase(renderable, config);
        final Config config = new Config();
        config.vertexShader   = Gdx.files.internal("shader/mirror/ssao_vertex.glsl").readString();
        config.fragmentShader = Gdx.files.internal("shader/mirror/ssao_fragment.glsl").readString();
        ssaoShader            = new SsaoShader(renderable, config, prefix, ssao, postFbo);
        return ssaoShader;

    }

    private Shader createWaterShader(final Renderable renderable) {
        final String prefix = createPrefixBase(renderable, config);
//		final String	prefix	= "";
        final Config config = new Config();
        config.vertexShader   = Gdx.files.internal("shader/water/water.vs.glsl").readString();
        config.fragmentShader = Gdx.files.internal("shader/water/water.fs.glsl").readString();
        waterShader           = new WaterShader(renderable, config, prefix, water);
//		setWaterAttribute(waterAttribute);
        waterShader.setClippingPlane(clippingPlane);
        return waterShader;

    }

    @Override
    public void dispose() {
        // pbrShader.dispose();
        // waterShader.dispose();
        super.dispose();
    }

    protected boolean isGL3() {
        return Gdx.graphics.getGLVersion().isVersionEqualToOrHigher(3, 0);
    }

    public boolean isSsaoEnabled() {
        return ssaoEnabled;
    }

    @Override
    public void setClippingPlane(final Plane clippingPlane) {
        this.clippingPlane = clippingPlane;
        if (waterShader != null) {
            waterShader.setClippingPlane(clippingPlane);
        }
        if (mirrorShader != null) {
            mirrorShader.setClippingPlane(clippingPlane);
        }
        if (shader != null) {
            shader.setClippingPlane(clippingPlane);
        }
    }

//	@Override
//	protected boolean isGL3() {
//		return Gdx.graphics.getGLVersion().isVersionEqualToOrHigher(3, 0);
//	}

    public void setSsaoEnabled(boolean ssaoEnabled) {
        this.ssaoEnabled = ssaoEnabled;
    }

//	@Override
//	public void setWaterAttribute(WaterAttribute waterAttribute) {
//		waterShader.setTiling(waterAttribute.getTiling() * 2 * /* 4 * 2 **/ 3.0f / Context.WORLD_SCALE);
//		waterShader.setWaveStrength(waterAttribute.getWaveStrength());
//		waterShader.setWaveSpeed(waterAttribute.getWaveSpeed());
//	}

}
