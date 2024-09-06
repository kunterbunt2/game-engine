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
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import de.bushnaq.abdalla.engine.audio.synthesis.util.BasicGameEngine;
import de.bushnaq.abdalla.engine.util.ModelCreator;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClippingTest extends BasicGameEngine {
    public static final float                       BIG_CUBE_SIZE       = 32;
    public static final float                       CUBE_DISTANCE0      = 128;
    public static final float                       CUBE_DISTANCE1      = 40;
    public static final float                       CUBE_SIZE0          = 32;
    public static final float                       SMALL_CUBE_SIZE     = 1;
    private final       Logger                      logger              = LoggerFactory.getLogger(this.getClass());
    private             Meter<BasicGameEngine>      meter               = null;
    private             GameObject<BasicGameEngine> mirrorGameObject    = null;
    public              float                       mirrorLevel         = 10;
    private             GameObject<BasicGameEngine> smallCubeGameObject = null;
    private final       int                         testCase            = 1;

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
        Vector3 position = null;
        Vector3 lookat   = null;
        switch (testCase) {
            case 0: {
                getRenderEngine().getMirror().setPresent(false);
                getRenderEngine().getMirror().setReflectivity(0.2f);
                position = new Vector3(0, 0, CUBE_DISTANCE0);
                lookat   = new Vector3(0, 0, -CUBE_SIZE0 * 4);
                {
                    GameObject<BasicGameEngine> go = new GameObject<>(new ModelInstanceHack(createCube()), null, null);
                    go.instance.transform.setToTranslationAndScaling(0, 0, 0, BIG_CUBE_SIZE, BIG_CUBE_SIZE, BIG_CUBE_SIZE);
                    getRenderEngine().addStatic(go);
                }
                meter = new Meter<BasicGameEngine>();
                meter.createFocusCross(getRenderEngine(), CUBE_DISTANCE0);
            }
            break;
            case 1: {
                getRenderEngine().getMirror().setPresent(true);
                getRenderEngine().getMirror().setReflectivity(0.2f);
                position = new Vector3(0, mirrorLevel + BIG_CUBE_SIZE / 2, CUBE_DISTANCE1);
                lookat   = new Vector3(0, mirrorLevel, 0);
                {
                    {
                        smallCubeGameObject = new GameObject<>(new ModelInstanceHack(createCube()), null, null);
                        smallCubeGameObject.instance.transform.setToTranslationAndScaling(0, mirrorLevel + SMALL_CUBE_SIZE / 2, 0, SMALL_CUBE_SIZE, SMALL_CUBE_SIZE, SMALL_CUBE_SIZE);
                        getRenderEngine().addDynamic(smallCubeGameObject);
                    }
                    {
                        mirrorGameObject = new GameObject<>(new ModelInstanceHack(createMirror()), null, null);
                        mirrorGameObject.instance.transform.setToTranslationAndScaling(0, mirrorLevel, 0, BIG_CUBE_SIZE, 1, BIG_CUBE_SIZE);
                        getRenderEngine().addDynamic(mirrorGameObject);
                    }
                }
            }
            break;
        }
        camera.position.set(position);
        camera.up.set(0, 1, 0);
        camera.near = 1f;
        camera.far  = 8000f;
        camera.lookAt(lookat);
        camera.update();
    }

    private Model createCube() {
        final ModelCreator modelCreator = new ModelCreator();
        final Attribute    color        = new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, Color.WHITE);
        final Attribute    metallic     = PBRFloatAttribute.createMetallic(0.5f);
        final Attribute    roughness    = PBRFloatAttribute.createRoughness(0.5f);
        final Material     material     = new Material(metallic, roughness, color);
        return modelCreator.createBox(material);
    }

    private Model createMirror() {
        final ColorAttribute diffuseColor = ColorAttribute.createDiffuse(Color.BLACK);
        final Material       material     = new Material(diffuseColor);
        material.id = "mirror";
        final ModelBuilder modelBuilder = new ModelBuilder();
        return createSquare(modelBuilder, 0.5f, 0.5f, material);
    }

    private Model createSquare(final ModelBuilder modelBuilder, final float sx, final float sz, final Material material) {
        return modelBuilder.createRect(-sx, 0f, sz, sx, 0f, sz, sx, 0f, -sz, -sx, 0f, -sz, 0f, 1f, 0f, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
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
            case Input.Keys.UP:
                mirrorLevel += 5;
                return true;
            case Input.Keys.DOWN:
                mirrorLevel += -5;
                return true;

        }
        return false;
    }

    @Override
    protected void update() throws Exception {
        getRenderEngine().getGameEngine().context.enableClipping();
        getRenderEngine().getMirror().setMirrorLevel(mirrorLevel);
        switch (testCase) {
            case 0: {
                getRenderEngine().gameShaderProvider.setClippingPlane(getRenderEngine().getMirror().getReflectionClippingPlane());
                meter.update();
            }
            break;
            case 1: {
            }
            break;
        }

        smallCubeGameObject.instance.transform.setToTranslation(0, mirrorLevel + SMALL_CUBE_SIZE / 2, 0);
        smallCubeGameObject.update();

        mirrorGameObject.instance.transform.setToTranslationAndScaling(0, mirrorLevel, 0, BIG_CUBE_SIZE, 1, BIG_CUBE_SIZE);
        mirrorGameObject.update();

    }

}