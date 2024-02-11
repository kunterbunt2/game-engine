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
import com.badlogic.gdx.math.Plane;
import de.bushnaq.abdalla.engine.shader.mirror.Mirror;
import de.bushnaq.abdalla.engine.shader.mirror.MirrorShader;
import de.bushnaq.abdalla.engine.shader.water.Water;
import de.bushnaq.abdalla.engine.shader.water.WaterShader;
import net.mgsx.gltf.scene3d.shaders.PBRShader;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

/**
 * @author kunterbunt
 */
public class GamePbrShaderProvider extends PBRShaderProvider implements GameShaderProviderInterface {

    public  MirrorShader mirrorShader;
    public  MyPBRShader  pbrShader;
    // private final FrameBuffer waterReflectionFbo;
//	private final FrameBuffer	waterRefractionFbo;
    public  WaterShader  waterShader;
    private Plane        clippingPlane;
    private Mirror       mirror;
    //	private final float			waterTiling;
//	private float				waveSpeed;
//	private float				waveStrength;
    private Water        water;

    public GamePbrShaderProvider(final PBRShaderConfig config, final Water water, final Mirror mirror) {
        super(config);
//		this.waterTiling = waterTiling;
//		this.waveStrength = waveStrength;
//		this.waveSpeed = waveSpeed;
        this.water  = water;
        this.mirror = mirror;
    }

    public static GamePbrShaderProvider createDefault(final PBRShaderConfig config, final Water water, final Mirror mirror) {
        return new GamePbrShaderProvider(config, water, mirror);
    }

    private Shader createMirrorShader(final Renderable renderable) {
        final String prefix = createPrefixBase(renderable, config);
        final Config config = new Config();
        config.vertexShader   = Gdx.files.internal("shader/mirror.vs.glsl").readString();
        config.fragmentShader = Gdx.files.internal("shader/mirror.fs.glsl").readString();
        mirrorShader          = new MirrorShader(renderable, config, prefix, mirror);
        mirrorShader.setClippingPlane(clippingPlane);
        return mirrorShader;

    }

    private MyPBRShader createPBRShader(final Renderable renderable) {
        pbrShader = (MyPBRShader) super.createShader(renderable);
        pbrShader.setClippingPlane(clippingPlane);
        return pbrShader;
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

    private Shader createWaterShader(final Renderable renderable) {
        final String prefix = createPrefixBase(renderable, config);
        final Config config = new Config();
        config.vertexShader   = Gdx.files.internal("shader/water.vs.glsl").readString();
        config.fragmentShader = Gdx.files.internal("shader/water.fs.glsl").readString();
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

    @Override
    protected boolean isGL3() {
        return Gdx.graphics.getGLVersion().isVersionEqualToOrHigher(3, 0);
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
        if (renderable.material.id.equals("water")) {
            return createWaterShader(renderable);
        } else if (renderable.material.id.equals("mirror")) {
            return createMirrorShader(renderable);
        } else
            return createPBRShader(renderable);
    }

    @Override
    protected PBRShader createShader(final Renderable renderable, final PBRShaderConfig config, final String prefix) {
        return new MyPBRShader(renderable, config, prefix);
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
        if (pbrShader != null) {
            pbrShader.setClippingPlane(clippingPlane);
        }
    }

//	@Override
//	public void setWaterAttribute(WaterAttribute waterAttribute) {
//		waterShader.setTiling(waterAttribute.getTiling() * 2 * /* 4 * 2 **/ 3.0f / Context.WORLD_SCALE);
//		waterShader.setWaveStrength(waterAttribute.getWaveStrength());
//		waterShader.setWaveSpeed(waterAttribute.getWaveSpeed());
//	}

}
