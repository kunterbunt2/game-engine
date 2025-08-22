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
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.loader.ObjLoader;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import de.bushnaq.abdalla.engine.audio.synthesis.util.BasicAtlasManager;
import de.bushnaq.abdalla.engine.audio.synthesis.util.BasicGameEngine;
import de.bushnaq.abdalla.engine.util.ExtendedGLProfiler;
import de.bushnaq.abdalla.engine.util.ModelCreator;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class SSAOTest extends BasicGameEngine {
    private static final String                 CALLS                            = "calls";
    public static final  float                  CUBE_DISTANCE                    = 128;
    public static final  float                  CUBE_SIZE                        = 4;
    private static final int                    DIMENSION_SIZE_X                 = 80;
    private static final int                    DIMENSION_SIZE_Z                 = 80;
    private static final String                 DRAW_CALLS                       = "drawCalls";
    private static final String                 DYNAMIC_TEXT_3_D                 = "dynamicText3D";
    private static final String                 FPS                              = "fps";
    private static final String                 SHADER_SWITCHES                  = "shaderSwitches";
    private static final String                 STATIC_TEXT_3_D                  = "staticText3D";
    private static final String                 TEXTURE_BINDINGS                 = "textureBindings";
    private static final String                 TEXTURE_GET_NUM_MANAGED_TEXTURES = "Texture.getNumManagedTextures()";
    public static final  String                 VISIBLE_DYNAMIC_GAME_OBJECTS     = "visibleDynamicGameObjects";
    private static final String                 VISIBLE_STATIC_GAME_OBJECTS      = "visibleStaticGameObjects";
    private final        Meter<BasicGameEngine> meter                            = new Meter();
    private final        Map<String, Integer>   performanceCounters              = new HashMap<>();
    private final        long                   started                          = System.currentTimeMillis();

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

        getRenderEngine().getDepthOfFieldEffect().setEnabled(false);
        getRenderEngine().getSsaoEffect().setEnabled(true);
        getRenderEngine().getSsaoCombineEffect().setEnabled(true);

        getRenderEngine().setDynamicDayTime(false);
        float   focalDepth = CUBE_DISTANCE * 2;
        Vector3 position   = new Vector3(0, CUBE_DISTANCE, CUBE_DISTANCE);
        Vector3 lookat     = new Vector3(0, 0, -CUBE_SIZE * 4);
        camera.position.set(position);
        camera.up.set(0, 1, 0);
        camera.near = 10f;
        camera.far  = 2000f;
        camera.lookAt(lookat);
        camera.update();
        meter.createFocusCross(getRenderEngine(), focalDepth);
        Vector3 scenePosition = new Vector3(0f, 0f, -DIMENSION_SIZE_Z * CUBE_SIZE);

        {
            GameObject<BasicGameEngine> go = new GameObject<>(new ModelInstanceHack(createCube()), null, null);
            go.instance.transform.setToTranslationAndScaling(scenePosition.x, scenePosition.y - CUBE_SIZE, scenePosition.z, 2 * DIMENSION_SIZE_X * CUBE_SIZE + CUBE_SIZE, CUBE_SIZE, 2 * DIMENSION_SIZE_Z * CUBE_SIZE + CUBE_SIZE);
            getRenderEngine().addStatic(go);
        }
        {
            GameObject<BasicGameEngine> go = new GameObject<>(new ModelInstanceHack(createSuite()), null, null);
            go.instance.transform.setToTranslationAndScaling(scenePosition.x, scenePosition.y - CUBE_SIZE, scenePosition.z, 10, 10, 10);
            getRenderEngine().addStatic(go);
        }
        for (int z = -DIMENSION_SIZE_Z; z <= DIMENSION_SIZE_Z; z++) {
            for (int x = -DIMENSION_SIZE_X; x <= DIMENSION_SIZE_X; x++) {
                if (Math.random() < .2f) {
                    float tx = x * CUBE_SIZE + scenePosition.x;
                    float ty = 0 * CUBE_SIZE + scenePosition.y;
                    float tz = z * CUBE_SIZE + scenePosition.z;
                    {
                        GameObject<BasicGameEngine> go = new GameObject<>(new ModelInstanceHack(createCube()), null, null);
                        go.instance.transform.setToTranslationAndScaling(tx, ty, tz, CUBE_SIZE, CUBE_SIZE, CUBE_SIZE);
                        getRenderEngine().addStatic(go);
                    }
                }
            }
        }
    }

    private Model createCube() {
        final ModelBuilder modelBuilder = new ModelBuilder();
        final Attribute    color        = new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, Color.WHITE);
        final Attribute    metallic     = PBRFloatAttribute.createMetallic(0.5f);
        final Attribute    roughness    = PBRFloatAttribute.createRoughness(0.5f);
        final Material     material     = new Material(metallic, roughness, color);
        return modelBuilder.createBox(1.0f, 1.0f, 1.0f, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);

    }

    private Mesh createQuad() {
        final ModelCreator modelCreator = new ModelCreator();
        return modelCreator.createQuad();
    }

    private Model createSuite() {
        ModelLoader loader = new ObjLoader();
        Model       model  = loader.loadModel(Gdx.files.internal(BasicAtlasManager.getAssetsFolderName() + "/models/nanosuit3.obj"));
        return model;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

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