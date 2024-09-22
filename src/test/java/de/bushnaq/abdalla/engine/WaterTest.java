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
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import de.bushnaq.abdalla.engine.audio.synthesis.util.BasicAtlasManager;
import de.bushnaq.abdalla.engine.audio.synthesis.util.BasicGameEngine;
import de.bushnaq.abdalla.engine.util.ModelCreator;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaterTest extends BasicGameEngine {
    public static final float CUBE_DISTANCE = 128;
    GameObject<BasicGameEngine> boundariesXNegGameObject;
    GameObject<BasicGameEngine> boundariesXPosGameObject;
    GameObject<BasicGameEngine> boundariesZNegGameObject;
    GameObject<BasicGameEngine> boundariesZPosGameObject;
    private Texture brdfLUT;
    float dimension = 128f;
    private Cubemap environmentDayCubemap;
    private Cubemap environmentNightCubemap;
    float height = 10;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    GameObject<BasicGameEngine> planeGameObject;
    Vector3                     pos = new Vector3(0, 0, -64);
    private final float waterLevel = height - 1;
    private       Model waterModel;

    @Test
    public void circularTranslatingSources() {
//        runFor = 10000;
        startLwjgl();
    }

    @Override
    public void create() {
        super.create();
        float focalDepth = CUBE_DISTANCE * 2;
        getRenderEngine().setShowGraphs(false);
//        getRenderEngine().setShadowEnabled(true);
        getRenderEngine().getDepthOfFieldEffect().setEnabled(false);
        getRenderEngine().getDepthOfFieldEffect().setFocalDepth(focalDepth);

        setupImageBasedLightingByFaceNames("clouds", "jpg", "jpg", "jpg", 10);
//        renderEngine.setDayAmbientLight(.9f, .9f, .9f, 10f);
//        renderEngine.setNightAmbientLight(.01f, .01f, .01f, 1f);
        // setup skybox
        renderEngine.setSkyBox(true);
        renderEngine.setDaySkyBox(new SceneSkybox(environmentDayCubemap));
        renderEngine.setNightSkyBox(new SceneSkybox(environmentNightCubemap));
        renderEngine.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
        renderEngine.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        renderEngine.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        renderEngine.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 0f));

        renderEngine.setShadowEnabled(true);
        // time
        renderEngine.setAlwaysDay(true);
        renderEngine.setDynamicDayTime(true);
//        renderEngine.setFixedDayTime(8);
        // fog
        renderEngine.getFog().setBeginDistance(128f);
        renderEngine.getFog().setFullDistance(265f);
        renderEngine.getFog().setColor(Color.BLACK);
        // water
        renderEngine.getWater().setPresent(true);
        renderEngine.getWater().setWaveStrength(0.09f);
        renderEngine.getWater().setWaveSpeed(0.01f);
        renderEngine.getWater().setTiling(4f);
        renderEngine.getWater().setRefractiveMultiplicator(1f);


//        renderEngine.setSkyBox(true);
        Vector3 position = new Vector3(0, height * 2, 20);
        Vector3 lookat   = new Vector3(0, 0, -dimension / 2);
        camera.position.set(position);
        camera.up.set(0, 1, 0);
        camera.near = 1f;
        camera.far  = 8000f;
        camera.lookAt(lookat);
        camera.update();
//        meter.createFocusCross(getRenderEngine(), focalDepth);
//        time1 = System.currentTimeMillis();
        createWaterModel();
        createPool();
        createWater();
    }

    private Model createCube() {
        final ModelCreator modelCreator = new ModelCreator();
        final Attribute    color        = new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, Color.WHITE);
        final Attribute    metallic     = PBRFloatAttribute.createMetallic(0.2f);
        final Attribute    roughness    = PBRFloatAttribute.createRoughness(0.5f);
        final Material     material     = new Material(metallic, roughness, color);
        return modelCreator.createBox(material);
    }

    private void createPool() {
//        float planeLevel = 0;
        Model model = createCube();
//        Vector3     min         = renderEngine.getSceneBox().min;
//        Vector3     max         = renderEngine.getSceneBox().max;
//        BoundingBox boundingBox = new BoundingBox(new Vector3(-dimension, planeLevel, -dimension), new Vector3(dimension, planeLevel, 0));
        {
            Matrix4 m = new Matrix4();
            m.setToTranslationAndScaling(pos.x, pos.y - 0.5f, pos.z, dimension, 1, dimension);
            planeGameObject = new GameObject<>(new ModelInstanceHack(model), null);
            planeGameObject.instance.transform.set(m);
            renderEngine.addDynamic(planeGameObject);
            planeGameObject.update();
        }
        {
            Matrix4 m = new Matrix4();
            m.setToTranslationAndScaling(pos.x, pos.y + height / 2, pos.z - dimension / 2, dimension, height, 1);
            boundariesZNegGameObject = new GameObject<>(new ModelInstanceHack(model), null);
            boundariesZNegGameObject.instance.transform.set(m);
            renderEngine.addDynamic(boundariesZNegGameObject);
            boundariesZNegGameObject.update();
        }
        {
            Matrix4 m = new Matrix4();
            m.setToTranslationAndScaling(pos.x, pos.y + height / 2, pos.z + dimension / 2, dimension, height, 1);
            boundariesZPosGameObject = new GameObject<>(new ModelInstanceHack(model), null);
            boundariesZPosGameObject.instance.transform.set(m);
            renderEngine.addDynamic(boundariesZPosGameObject);
            boundariesZPosGameObject.update();
        }
        {
            Matrix4 m = new Matrix4();
            m.setToTranslationAndScaling(pos.x - dimension / 2, pos.y + height / 2, pos.z, 1, height, dimension);
            boundariesXPosGameObject = new GameObject<>(new ModelInstanceHack(model), null);
            boundariesXPosGameObject.instance.transform.set(m);
            renderEngine.addDynamic(boundariesXPosGameObject);
            boundariesXPosGameObject.update();
        }
        {
            Matrix4 m = new Matrix4();
            m.setToTranslationAndScaling(pos.x + dimension / 2, pos.y + height / 2, pos.z, 1, height, dimension);
            boundariesXNegGameObject = new GameObject<>(new ModelInstanceHack(model), null);
            boundariesXNegGameObject.instance.transform.set(m);
            renderEngine.addDynamic(boundariesXNegGameObject);
            boundariesXNegGameObject.update();
        }
    }

    private Model createSquare(final ModelBuilder modelBuilder, final float sx, final float sz, final Material material) {
        return modelBuilder.createRect(-sx, 0f, sz, sx, 0f, sz, sx, 0f, -sz, -sx, 0f, -sz, 0f, 1f, 0f, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
    }

    protected void createWater() {
        // water
        renderEngine.getWater().setPresent(true);
        renderEngine.getWater().setWaterLevel(waterLevel);
        final GameObject<BasicGameEngine> water = new GameObject<>(new ModelInstanceHack(waterModel), null);
        water.instance.transform.setToTranslationAndScaling(pos.x, waterLevel, pos.z, dimension, .1f, dimension);
        water.update();
        renderEngine.addDynamic(water);
    }

    private void createWaterModel() {
        final ModelBuilder   modelBuilder = new ModelBuilder();
        final ColorAttribute diffuseColor = ColorAttribute.createDiffuse(Color.WHITE);
        final Material       material     = new Material(diffuseColor);
        material.id = "water";
        waterModel  = createSquare(modelBuilder, 0.5f, 0.5f, material);
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

    private void setupImageBasedLightingByFaceNames(final String name, final String diffuseExtension, final String environmentExtension, final String specularExtension, final int specularIterations) {
        diffuseCubemap          = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), BasicAtlasManager.getAssetsFolderName() + "/cubemaps/" + name + "/diffuse/diffuse_", "_0." + diffuseExtension, EnvironmentUtil.FACE_NAMES_FULL);
        environmentDayCubemap   = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), BasicAtlasManager.getAssetsFolderName() + "/cubemaps/" + name + "/environmentDay/environment_", "_0." + environmentExtension, EnvironmentUtil.FACE_NAMES_FULL);
        environmentNightCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), BasicAtlasManager.getAssetsFolderName() + "/cubemaps/" + name + "/environmentNight/environment_", "_0." + environmentExtension, EnvironmentUtil.FACE_NAMES_FULL);
        specularCubemap         = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), BasicAtlasManager.getAssetsFolderName() + "/cubemaps/" + name + "/specular/specular_", "_", "." + specularExtension, specularIterations, EnvironmentUtil.FACE_NAMES_FULL);
        brdfLUT                 = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
    }

    @Override
    protected void update() throws Exception {
    }

}