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

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import de.bushnaq.abdalla.engine.audio.synthesis.util.BasicGameEngine;
import de.bushnaq.abdalla.engine.util.ModelCreator;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class DepthOfFieldTest extends BasicGameEngine {
    private static final String                 CALLS                            = "calls";
    public static final  float                  CUBE_DISTANCE                    = 128;
    public static final  float                  CUBE_SIZE                        = 32;
    private static final int                    DIMENSION_SIZE_X                 = 10;
    private static final int                    DIMENSION_SIZE_Y                 = 10;
    private static final int                    DIMENSION_SIZE_Z                 = 10;
    private static final String                 DRAW_CALLS                       = "drawCalls";
    private static final String                 DYNAMIC_TEXT_3_D                 = "dynamicText3D";
    private static final String                 FPS                              = "fps";
    public static final  float                  LIGHT_SIZE                       = 1;
    private static final String                 SHADER_SWITCHES                  = "shaderSwitches";
    private static final String                 STATIC_TEXT_3_D                  = "staticText3D";
    private static final String                 TEXTURE_BINDINGS                 = "textureBindings";
    private static final String                 TEXTURE_GET_NUM_MANAGED_TEXTURES = "Texture.getNumManagedTextures()";
    public static final  String                 VISIBLE_DYNAMIC_GAME_OBJECTS     = "visibleDynamicGameObjects";
    private static final String                 VISIBLE_STATIC_GAME_OBJECTS      = "visibleStaticGameObjects";
    //    private final        CircularCubeActor[] ccaa                        = new CircularCubeActor[DIMENSION_SIZE_X];
//    private final        Logger              logger                      = LoggerFactory.getLogger(this.getClass());
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
        getRenderEngine().getDepthOfFieldEffect().setEnabled(true);
        float focalDepth = CUBE_DISTANCE * 2 * 2;
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
                    {
                        GameObject<BasicGameEngine> go = new GameObject<>(new ModelInstanceHack(createRedEmissiveBohkeyModel()), null, null);
                        go.instance.transform.setToTranslationAndScaling(tx - CUBE_SIZE / 2, ty + CUBE_SIZE / 2, tz + CUBE_SIZE / 2, LIGHT_SIZE, LIGHT_SIZE, LIGHT_SIZE);
                        getRenderEngine().addStatic(go);
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

    private Model createRedEmissiveBohkeyModel() {
        final ModelCreator modelCreator = new ModelCreator();
        final Material     material     = new Material();
//        material.set(PBRColorAttribute.createBaseColorFactor(new Color(Color.WHITE).fromHsv(15, .9f, .8f)));
        material.set(PBRColorAttribute.createEmissive(new Color(Color.RED)));
        return modelCreator.createBox(material);
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
    }

    private void updateCounter(String counter, int value) {
        performanceCounters.merge(counter, value, (a, b) -> Math.max(b, a));
    }

}