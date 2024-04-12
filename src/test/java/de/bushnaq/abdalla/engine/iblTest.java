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

package de.bushnaq.abdalla.engine;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import de.bushnaq.abdalla.engine.audio.synthesis.util.BasicGameEngine;
import de.bushnaq.abdalla.engine.audio.synthesis.util.CircularCubeActor;
import de.bushnaq.abdalla.engine.util.ExtendedGLProfiler;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class iblTest extends BasicGameEngine {
    public static final  String               VISIBLE_DYNAMIC_GAME_OBJECTS     = "visibleDynamicGameObjects";
    private static final String               CALLS                            = "calls";
    private static final String               DRAW_CALLS                       = "drawCalls";
    private static final String               DYNAMIC_TEXT_3_D                 = "dynamicText3D";
    private static final String               FPS                              = "fps";
    private static final int                  NUMBER_OF_SOURCES                = 1;
    private static final String               SHADER_SWITCHES                  = "shaderSwitches";
    private static final float                SPHERE_SIZE                      = 512;
    private static final String               STATIC_TEXT_3_D                  = "staticText3D";
    private static final String               TEXTURE_BINDINGS                 = "textureBindings";
    private static final String               TEXTURE_GET_NUM_MANAGED_TEXTURES = "Texture.getNumManagedTextures()";
    private static final String               VISIBLE_STATIC_GAME_OBJECTS      = "visibleStaticGameObjects";
    private final        Logger               logger                           = LoggerFactory.getLogger(this.getClass());
    private final        Map<String, Integer> performanceCounters              = new HashMap<>();
    private final        long                 started                          = System.currentTimeMillis();
    protected            long                 runFor                           = 30000;//ms
    CircularCubeActor[] ccaa = new CircularCubeActor[NUMBER_OF_SOURCES];
    private Model sphereModel;
    private long  time1;

    private void assesPerformanceCounters() {
        if (getRenderEngine().isShowGraphs()) {
            assertEquals(18, performanceCounters.get(TEXTURE_BINDINGS));
            assertEquals(20, performanceCounters.get(DRAW_CALLS));
            assertEquals(10, performanceCounters.get(SHADER_SWITCHES));
            assertEquals(439, performanceCounters.get(CALLS));
            assertEquals(5, performanceCounters.get(TEXTURE_GET_NUM_MANAGED_TEXTURES));
            assertEquals(120, performanceCounters.get(FPS));
        } else {
            assertEquals(45, performanceCounters.get(TEXTURE_BINDINGS));
            assertEquals(65, performanceCounters.get(DRAW_CALLS));
            assertEquals(7, performanceCounters.get(SHADER_SWITCHES));
            assertEquals(1140, performanceCounters.get(CALLS));
            assertEquals(4, performanceCounters.get(TEXTURE_GET_NUM_MANAGED_TEXTURES));
            assertEquals(120, performanceCounters.get(FPS));
            assertEquals(0, performanceCounters.get(DYNAMIC_TEXT_3_D));
            assertEquals(0, performanceCounters.get(STATIC_TEXT_3_D));
            assertEquals(10, performanceCounters.get(VISIBLE_DYNAMIC_GAME_OBJECTS));
            assertEquals(0, performanceCounters.get(VISIBLE_STATIC_GAME_OBJECTS));
        }
    }

    @Test
    public void circularTranslatingSources() {
        runFor = 20000;
        startLwjgl();
    }

    @Override
    public void create() {
        super.create();
        try {
            createSphere();
            GameObject<BasicGameEngine> gameObject = new GameObject<>(new ModelInstanceHack(sphereModel), null);
            gameObject.instance.transform.setToTranslationAndScaling(0, SPHERE_SIZE / 2, 0, SPHERE_SIZE, SPHERE_SIZE, SPHERE_SIZE);
            getRenderEngine().addStatic(gameObject);
            time1 = System.currentTimeMillis();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        for (int i = 0; i < NUMBER_OF_SOURCES; i++) {
            ccaa[i] = new CircularCubeActor(i, 0);
            ccaa[i].get3DRenderer().create(getRenderEngine());
        }

    }

    @Override
    public void dispose() {
        printPerformanceCounters();
        assesPerformanceCounters();
        super.dispose();
    }

    @Override
    public void setupImageBasedLightingByFaceNames() {
        super.setupImageBasedLightingByFaceNames();
//        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        DirectionalLightEx sun = new DirectionalLightEx();
        sun.direction.set(1, -1, 1).nor();
        sun.color.set(Color.WHITE);
        myIBLBuilder       ibl   = new myIBLBuilder(null);
        myIBLBuilder.Light light = new myIBLBuilder.Light();
        light.direction.set(sun.direction).nor();
        light.color.set(sun.color);
        light.exponent = 10f;
        ibl.lights.add(light);
        ibl.nearGroundColor.set(0.0F, 0.0F, 0.0F, 1.0F);
        ibl.farGroundColor.set(0.0F, 0.0F, .0F, 1.0F);
        ibl.nearSkyColor.set(0.0F, 0.0F, .0F, 1.0F);
        ibl.farSkyColor.set(0.0F, 0.0F, .0F, 1.0F);
        Cubemap environmentCubemap = ibl.buildEnvMap(1024, null, null);
        getRenderEngine().setDaySkyBox(new SceneSkybox(environmentCubemap));
        getRenderEngine().setNightSkyBox(new SceneSkybox(environmentCubemap));
        diffuseCubemap  = ibl.buildIrradianceMap(256, null, null);
        specularCubemap = ibl.buildRadianceMap(10, null, null);
        ibl.dispose();
    }

    @Override
    protected void update() throws Exception {
        for (int i = 0; i < NUMBER_OF_SOURCES; i++) {
            ccaa[i].get3DRenderer().update(getRenderEngine(), 0, 0, 0, false);
        }
//        super.update();
        updateCounters(getRenderEngine().getProfiler());
        if (System.currentTimeMillis() - time1 > runFor) Gdx.app.exit();
    }

    private void createSphere() {
        final ModelBuilder modelBuilder = new ModelBuilder();
        {
            final Attribute color     = new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, Color.LIGHT_GRAY);
            final Attribute metallic  = PBRFloatAttribute.createMetallic(1f);
            final Attribute roughness = PBRFloatAttribute.createRoughness(0.0f);
            final Material  material  = new Material(metallic, roughness, color);
            sphereModel = modelBuilder.createSphere(1.0f, 1.0f, 1.0f, 64, 64, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        }
    }

    private void printPerformanceCounters() {
        for (String counter : performanceCounters.keySet()) {
            logger.info(String.format("%s = %d", counter, performanceCounters.get(counter)));
        }
    }

    private void updateCounter(String counter, int value) {
        performanceCounters.merge(counter, value, (a, b) -> Math.max(b, a));
    }

    void updateCounters(ExtendedGLProfiler profiler) {
        if (System.currentTimeMillis() - started > 2000) {
            updateCounter(TEXTURE_BINDINGS, profiler.getTextureBindings());
            updateCounter(DRAW_CALLS, profiler.getDrawCalls());
            updateCounter(SHADER_SWITCHES, profiler.getShaderSwitches());
            updateCounter(CALLS, profiler.getCalls());
            updateCounter(TEXTURE_GET_NUM_MANAGED_TEXTURES, Texture.getNumManagedTextures());
            updateCounter(FPS, Gdx.graphics.getFramesPerSecond());
            updateCounter(DYNAMIC_TEXT_3_D, profiler.getDynamicText3D());
            updateCounter(STATIC_TEXT_3_D, profiler.getStaticText3D());
            updateCounter(VISIBLE_DYNAMIC_GAME_OBJECTS, profiler.getVisibleDynamicGameObjects());
            updateCounter(VISIBLE_STATIC_GAME_OBJECTS, profiler.getVisibleStaticGameObjects());
        }
    }

}