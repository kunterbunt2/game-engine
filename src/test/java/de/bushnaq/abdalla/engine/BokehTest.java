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
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Vector3;
import de.bushnaq.abdalla.engine.audio.synthesis.util.BasicGameEngine;
import de.bushnaq.abdalla.engine.audio.synthesis.util.CircularCubeActor;
import de.bushnaq.abdalla.engine.util.ExtendedGLProfiler;
import de.bushnaq.abdalla.engine.util.ModelCreator;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class BokehTest extends BasicGameEngine {
    private static final String CALLS                            = "calls";
    public static final  float  CUBE_DISTANCE                    = 128;
    public static final  float  CUBE_SIZE                        = 32;
    private static final int    DIMENSION_SIZE_X                 = 10;
    private static final int    DIMENSION_SIZE_Y                 = 10;
    private static final int    DIMENSION_SIZE_Z                 = 10;
    private static final String DRAW_CALLS                       = "drawCalls";
    private static final String DYNAMIC_TEXT_3_D                 = "dynamicText3D";
    private static final String FPS                              = "fps";
    public static final  float  LIGHT_SIZE                       = 3;
    private static final String SHADER_SWITCHES                  = "shaderSwitches";
    private static final String STATIC_TEXT_3_D                  = "staticText3D";
    private static final String TEXTURE_BINDINGS                 = "textureBindings";
    private static final String TEXTURE_GET_NUM_MANAGED_TEXTURES = "Texture.getNumManagedTextures()";
    public static final  String VISIBLE_DYNAMIC_GAME_OBJECTS     = "visibleDynamicGameObjects";
    private static final String VISIBLE_STATIC_GAME_OBJECTS      = "visibleStaticGameObjects";
    CircularCubeActor[] ccaa = new CircularCubeActor[DIMENSION_SIZE_X];
    private final Logger                 logger              = LoggerFactory.getLogger(this.getClass());
    private final Meter<BasicGameEngine> meter               = new Meter();
    private final Map<String, Integer>   performanceCounters = new HashMap<>();
    private final long                   started             = System.currentTimeMillis();

//    private void assesPerformanceCounters() {
//        if (getRenderEngine().isShowGraphs()) {
//            assertEquals(52, performanceCounters.get(TEXTURE_BINDINGS));
//            assertEquals(72, performanceCounters.get(DRAW_CALLS));
//            assertEquals(10, performanceCounters.get(SHADER_SWITCHES));
//            assertEquals(1254, performanceCounters.get(CALLS));
//            assertEquals(5, performanceCounters.get(TEXTURE_GET_NUM_MANAGED_TEXTURES));
//            assertEquals(120, performanceCounters.get(FPS));
//        } else {
//            assertEquals(45, performanceCounters.get(TEXTURE_BINDINGS));
//            assertEquals(65, performanceCounters.get(DRAW_CALLS));
//            assertEquals(7, performanceCounters.get(SHADER_SWITCHES));
//            assertEquals(1140, performanceCounters.get(CALLS));
//            assertEquals(4, performanceCounters.get(TEXTURE_GET_NUM_MANAGED_TEXTURES));
//            assertEquals(120, performanceCounters.get(FPS));
//            assertEquals(0, performanceCounters.get(DYNAMIC_TEXT_3_D));
//            assertEquals(0, performanceCounters.get(STATIC_TEXT_3_D));
//            assertEquals(10, performanceCounters.get(VISIBLE_DYNAMIC_GAME_OBJECTS));
//            assertEquals(0, performanceCounters.get(VISIBLE_STATIC_GAME_OBJECTS));
//        }
//    }

    @Test
    public void circularTranslatingSources() {
//        runFor = 10000;
        startLwjgl();
    }

    @Override
    public void create() {
        super.create();
        getRenderEngine().setShowGraphs(false);
        getRenderEngine().setShadowEnabled(true);
        getRenderEngine().getDepthOfFieldEffect().setEnabled(true);
//        getRenderEngine().getDepthOfFieldEffect().setEnabled(true);
        float focalDepth = CUBE_DISTANCE * 2;
        getRenderEngine().getDepthOfFieldEffect().setFocalDepth(focalDepth);
        Vector3 position = new Vector3(0, 0, CUBE_DISTANCE);
        Vector3 lookat   = new Vector3(0, 0, -CUBE_SIZE * 4);
        camera.position.set(position);
        camera.up.set(0, 1, 0);
        camera.near = 1f;
        camera.far  = 8000f;
        camera.lookAt(lookat);
        camera.update();
        meter.createFocusCross(getRenderEngine(), focalDepth);
//        time1 = System.currentTimeMillis();
        for (int z = -DIMENSION_SIZE_Z; z <= DIMENSION_SIZE_Z; z++) {
            for (int y = -DIMENSION_SIZE_Y; y <= DIMENSION_SIZE_Y; y++) {
                for (int x = -DIMENSION_SIZE_X; x <= DIMENSION_SIZE_X; x++) {
                    float tx = x * CUBE_DISTANCE;
                    float ty = y * CUBE_DISTANCE;
                    float tz = z * CUBE_DISTANCE - DIMENSION_SIZE_Z * CUBE_DISTANCE;
                    {
                        GameObject<BasicGameEngine> go = new GameObject<>(new ModelInstanceHack(createCube()), null, null);
                        go.instance.transform.setToTranslationAndScaling(tx, ty, tz, CUBE_SIZE, CUBE_SIZE, CUBE_SIZE);
                        getRenderEngine().addStatic(go);

                    }
                    if (Math.random() < .1f) {
                        {
                            GameObject<BasicGameEngine> go = new GameObject<>(new ModelInstanceHack(createRedEmissiveModel()), null, null);
                            go.instance.transform.setToTranslationAndScaling(tx - CUBE_SIZE / 2, ty + CUBE_SIZE / 2, tz + CUBE_SIZE / 2, LIGHT_SIZE, LIGHT_SIZE, LIGHT_SIZE);
                            getRenderEngine().addStatic(go);
                        }
                        {
                            PointLight pointLight = new PointLight();
                            float      intensity  = 100f;
                            pointLight.set(Color.WHITE, tx - CUBE_SIZE / 2, ty + CUBE_SIZE / 2, tz + CUBE_SIZE / 2, intensity);
                            getRenderEngine().add(pointLight, true);
                        }
                    }
                }
            }
        }
    }

    private Model createCube() {
        final ModelCreator modelCreator = new ModelCreator();
        final Attribute    color        = new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, Color.WHITE);
        final Attribute    metallic     = PBRFloatAttribute.createMetallic(0.5f);
        final Attribute    roughness    = PBRFloatAttribute.createRoughness(0.5f);
        final Material     material     = new Material(metallic, roughness, color);
        return modelCreator.createBox(material);
    }

    private Model createRedEmissiveModel() {
        final ModelCreator modelCreator = new ModelCreator();
        final Material     material     = new Material();
        material.set(PBRColorAttribute.createEmissive(Color.WHITE));
        return modelCreator.createBox(material);
    }

    @Override
    public void dispose() {
//        printPerformanceCounters();
//        assesPerformanceCounters();
        super.dispose();
    }

    //    private void printPerformanceCounters() {
//        for (String counter : performanceCounters.keySet()) {
//            logger.info(String.format("%s = %d", counter, performanceCounters.get(counter)));
//        }
//    }
    @Override
    public boolean keyDown(final int keycode) {
        float speed = 20.0f;
        super.keyDown(keycode);
        switch (keycode) {
            case Input.Keys.NUM_3:
                getRenderEngine().getDepthOfFieldEffect().setEnabled(!getRenderEngine().getDepthOfFieldEffect().isEnabled());
                return true;
            case Input.Keys.W:
                camera.position.add(0, 0, -1 * speed);
                camera.update();
                return true;
            case Input.Keys.S:
                camera.position.add(0, 0, 1 * speed);
                camera.update();
                return true;
            case Input.Keys.A:
                camera.position.add(-1 * speed, 0, 0);
                camera.update();
                return true;
            case Input.Keys.D:
                camera.position.add(1 * speed, 0, 0);
                camera.update();
                return true;
            case Input.Keys.Q:
                camera.position.add(0, 1 * speed, 0);
                camera.update();
                return true;
            case Input.Keys.E:
                camera.position.add(0, -1 * speed, 0);
                camera.update();
                return true;

        }
        return false;
    }

    @Override
    protected void update() throws Exception {
//        camera.position.y += 10;
//        camera.position.x = 0;
//        camera.position.z = 0;
//        camera.lookAt(0, 0, 0);
//        camera.update();
//        for (int i = 0; i < DIMENSION_SIZE_X; i++) {
//            ccaa[i].get3DRenderer().update(getRenderEngine(), 0, 0, 0, false);
//        }
//        super.update();
        meter.update();
        updateCounters(getRenderEngine().getProfiler());
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