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

package de.bushnaq.abdalla.engine.audio.synthesis.util;

import com.badlogic.gdx.*;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import de.bushnaq.abdalla.engine.RenderEngine3D;
import de.bushnaq.abdalla.engine.RenderEngineExtension;
import de.bushnaq.abdalla.engine.audio.AudioEngine;
import de.bushnaq.abdalla.engine.audio.OpenAlException;
import de.bushnaq.abdalla.engine.camera.MovingCamera;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicGameEngine implements ApplicationListener, InputProcessor, RenderEngineExtension {
    private static final float                 CAMERA_OFFSET_X = 300f;
    private static final float                 CAMERA_OFFSET_Y = 500f;
    private static final float                 CAMERA_OFFSET_Z = 400f;
    private              BasicAtlasManager     atlasManager;
    private final        AudioEngine           audioEngine     = new BasicAudioEngine();
    private              Texture               brdfLUT;
    private              CameraInputController camController;
    public               MovingCamera          camera;
    private              OrthographicCamera    camera2D;
    private              long                  currentTime     = 0L;
    protected            Cubemap               diffuseCubemap;
    Cubemap environmentCubemap;
    //    private              BitmapFont                      font;
    private       boolean hrtfEnabled    = true;
    private final Matrix4 identityMatrix = new Matrix4();
    InputMultiplexer inputMultiplexer = new InputMultiplexer();
    private final List<Label>                     labels          = new ArrayList<>();
    private       long                            lastTime        = 0;
    private final Logger                          logger          = LoggerFactory.getLogger(this.getClass());
    private final BasicRandomGenerator            randomGenerator = new BasicRandomGenerator(1);
    private       RenderEngine3D<BasicGameEngine> renderEngine;
    //    private       boolean                         simulateBassBoost = true;
    protected     Cubemap                         specularCubemap;
    private       Stage                           stage;
    private       StringBuilder                   stringBuilder;
    private       boolean                         takeScreenShot  = false;
    private       long                            timeDelta       = 0L;

    public void advanceInTime() {
        long fixedDelta = 20L;
        timeDelta += System.currentTimeMillis() - lastTime;
        if (timeDelta > 1000) timeDelta = 0;
        if (timeDelta >= fixedDelta) {
            timeDelta -= fixedDelta;
            currentTime += fixedDelta;
        }
        lastTime = System.currentTimeMillis();
    }

    @Override
    public void create() {
        try {
            BasicDesktopContextFactory contextFactory = new BasicDesktopContextFactory();
            Context                    context        = contextFactory.create();
            createCamera();
            createInputProcessor(this);
            atlasManager = new BasicAtlasManager();
            atlasManager.init();
            createStage();
            renderEngine = new RenderEngine3D<BasicGameEngine>(context, this, camera, camera2D, getAtlasManager().menuFont, getAtlasManager().menuBoldFont, getAtlasManager().systemTextureRegion);
//            renderEngine.setPbr(false);
            renderEngine.setSceneBoxMin(new Vector3(-1000, -1000, -1000));
            renderEngine.setSceneBoxMax(new Vector3(1000, 1000, 1000));
            renderEngine.getWater().setPresent(false);
            renderEngine.getMirror().setPresent(false);
            renderEngine.getFog().setEnabled(false);

            renderEngine.setSkyBox(true);
            renderEngine.setDayAmbientLight(.9f, .9f, .9f, 1f);
            renderEngine.setNightAmbientLight(.01f, .01f, .01f, 1f);
            renderEngine.setAlwaysDay(true);
            renderEngine.setDynamicDayTime(true);
            renderEngine.setShadowEnabled(true);

            renderEngine.setShowGraphs(true);
            createEnvironment();
            getAudioEngine().create("E:/github/game-engine/app/assets");
            audioEngine.radioTTS.loadResource(this.getClass());
            audioEngine.radioTTS.loadAudio();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void createCamera() throws Exception {
        camera = new MovingCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Vector3 lookat = new Vector3(0, 0, 0);
        camera.position.set(lookat.x + CAMERA_OFFSET_X, lookat.y + CAMERA_OFFSET_Y, lookat.z + CAMERA_OFFSET_Z);
        camera.up.set(0, 1, 0);
        camera.lookAt(lookat);
        camera.near = 8f;
        camera.far  = 80000f;
        camera.update();
        camera.setDirty(true);
        camera2D = new OrthographicCamera();
    }

    private Lwjgl3ApplicationConfiguration createConfig() {
        Lwjgl3ApplicationConfiguration config;
        config = new Lwjgl3ApplicationConfiguration();
        config.useVsync(false);
        config.setForegroundFPS(1000);
        config.setResizable(false);
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL32, 3, 2); // use GL 3.0 (emulated by OpenGL 3.2)
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 16);
        config.setTitle("game-engine-test");
        {
            ShaderProgram.prependVertexCode   = "#version 150\n"//
                    + "#define GLSL3\n"//
                    + "#ifdef GLSL3\n"//
                    + "#define attribute in\n"//
                    + "#define varying out\n"//
                    + "#endif\n";//
            ShaderProgram.prependFragmentCode = "#version 150\n"//
                    + "#define GLSL3\n"//
                    + "#ifdef GLSL3\n"//
                    + "#define textureCube texture\n"//
                    + "#define texture2D texture\n"//
                    + "#define varying in\n"//
                    + "#endif\n";//
        }
        final Monitor[]   monitors    = Lwjgl3ApplicationConfiguration.getMonitors();
        final DisplayMode primaryMode = Lwjgl3ApplicationConfiguration.getDisplayMode(monitors[1]);
        config.setFullscreenMode(primaryMode);
        return config;
    }

    private void createEnvironment() {
        // setup IBL (image based lighting)
        if (getRenderEngine().isPbr()) {
            setupImageBasedLightingByFaceNames();
//            renderEngine.setDaySkyBox(new SceneSkybox(environmentCubemap));
//            renderEngine.setNightSkyBox(new SceneSkybox(environmentCubemap));
            renderEngine.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
            renderEngine.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
            renderEngine.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
            renderEngine.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 0f));
        } else {
        }
    }

    private void createInputProcessor(final InputProcessor inputProcessor) {
        camController                = new CameraInputController(camera);
        camController.scrollFactor   = -0.1f;
        camController.translateUnits = 1000f;
        inputMultiplexer.addProcessor(inputProcessor);
        inputMultiplexer.addProcessor(camController);
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    private void createStage() {
        final int height = 12;
        stage = new Stage();
//        font  = new BitmapFont();
        for (int i = 0; i < 8; i++) {
            final Label label = new Label(" ", new Label.LabelStyle(getAtlasManager().menuFont, Color.WHITE));
            label.setPosition(0, i * height);
            stage.addActor(label);
            labels.add(label);
        }
        stringBuilder = new StringBuilder();
    }

    @Override
    public void dispose() {
        try {
            stage.dispose();
            getAudioEngine().dispose();
//            font.dispose();
            getRenderEngine().dispose();
            Gdx.app.exit();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void exitGame() {
    }

    BasicAtlasManager getAtlasManager() {
        return atlasManager;
    }

    public AudioEngine getAudioEngine() {
        return audioEngine;
    }

    public BasicRandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    public RenderEngine3D<BasicGameEngine> getRenderEngine() {
        return renderEngine;
    }

//    public boolean isSimulateBassBoost() {
//        return simulateBassBoost;
//    }

    @Override
    public boolean keyDown(final int keycode) {
        switch (keycode) {
            case Input.Keys.ESCAPE:
                Gdx.app.exit();
                return true;
//            case Input.Keys.NUM_2:
//                setSimulateBassBoost(!isSimulateBassBoost());
//                if (isSimulateBassBoost()) logger.info("bassBoost on");
//                else logger.info("bassBoost off");
//                return true;
            case Input.Keys.F1:
                renderEngine.setGammaCorrected(!renderEngine.isGammaCorrected());
                if (renderEngine.isGammaCorrected()) logger.info("gamma correction on");
                else logger.info("gamma correction off");
                return true;
            case Input.Keys.F2:
                renderEngine.getDepthOfFieldEffect().setEnabled(!renderEngine.getDepthOfFieldEffect().isEnabled());
                if (renderEngine.getDepthOfFieldEffect().isEnabled()) logger.info("depth of field on");
                else logger.info("depth of field off");
                return true;
            case Input.Keys.F3:
                renderEngine.setRenderBokeh(!renderEngine.isRenderBokeh());
                if (renderEngine.isRenderBokeh()) logger.info("render bokeh on");
                else logger.info("render bokeh off");
                return true;
            case Input.Keys.F9:
                renderEngine.setShowGraphs(!renderEngine.isShowGraphs());
                if (renderEngine.isShowGraphs()) logger.info("graphs are on");
                else logger.info("graphs are off");
                return true;
            case Input.Keys.F10:
                renderEngine.setDebugMode(!renderEngine.isDebugMode());
                if (renderEngine.isDebugMode()) logger.info("debug mode on");
                else logger.info("debug mode off");
                return true;
            case Input.Keys.H:
                try {
                    if (hrtfEnabled) {
                        getAudioEngine().disableHrtf(0);
                        hrtfEnabled = false;
                    } else {
                        getAudioEngine().enableHrtf(0);
                        hrtfEnabled = true;
                    }
                } catch (final OpenAlException e) {
                    logger.error(e.getMessage(), e);
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean keyTyped(final char character) {
        return false;
    }

    @Override
    public boolean keyUp(final int keycode) {
        return false;
    }

    @Override
    public boolean mouseMoved(final int screenX, final int screenY) {
        return false;
    }

    @Override
    public void pause() {
    }

    @Override
    public void render() {
        try {
            advanceInTime();
            update();
            if (renderEngine.getProfiler().isEnabled()) {
                renderEngine.getProfiler().reset();// reset on each frame
            }
            getRenderEngine().render(currentTime, Gdx.graphics.getDeltaTime(), takeScreenShot);
//            getRenderEngine().postProcessRender();
            Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
//            getRenderEngine().renderEngine2D.batch.enableBlending();
//            getRenderEngine().renderEngine2D.batch.begin();
//            getRenderEngine().renderEngine2D.batch.setProjectionMatrix(getRenderEngine().getCamera().combined);
//            getRenderEngine().renderEngine2D.batch.end();
//            getRenderEngine().renderEngine2D.batch.setTransformMatrix(identityMatrix);//fix transformMatrix
            renderStage();
            takeScreenShot = false;
            getAudioEngine().begin(getRenderEngine().getCamera(), true);
            getAudioEngine().end();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void render2Dxz() {

    }

    private void renderStage() {
        int labelIndex = 0;
        // fps
        {
            stringBuilder.setLength(0);
            stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
            labels.get(labelIndex++).setText(stringBuilder);
        }
        //camera y
        {
            stringBuilder.setLength(0);
            stringBuilder.append(" camera height: ").append(camera.position.y);
            labels.get(labelIndex++).setText(stringBuilder);
        }
        //depth of field
        {
            stringBuilder.setLength(0);
            stringBuilder.append(String.format(" focal depth: [%f]", renderEngine.getDepthOfFieldEffect().getFocalDepth()));
            labels.get(labelIndex++).setText(stringBuilder);
        }
        //audio sources
        {
            stringBuilder.setLength(0);
            stringBuilder.append(" audio sources: ").append(getAudioEngine().getEnabledAudioSourceCount() + " / " + getAudioEngine().getDisabledAudioSourceCount());
            labels.get(labelIndex++).setText(stringBuilder);
        }
        stage.draw();
    }

    @Override
    public void resize(final int width, final int height) {
    }

    @Override
    public void resume() {
    }

    @Override
    public boolean scrolled(final float amountX, final float amountY) {
        return false;
    }

//    public void setSimulateBassBoost(boolean simulateBassBoost) {
//        this.simulateBassBoost = simulateBassBoost;
//    }

    protected void setupImageBasedLightingByFaceNames() {
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        DirectionalLightEx light = new DirectionalLightEx();
        light.direction.set(1, -1, 1).nor();
        light.color.set(Color.WHITE);
        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
        environmentCubemap = iblBuilder.buildEnvMap(1024);
        diffuseCubemap     = iblBuilder.buildIrradianceMap(256);
        specularCubemap    = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose();
    }

    protected void startLwjgl() {
        final Lwjgl3ApplicationConfiguration config = createConfig();
        new Lwjgl3Application(this, config);
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

//    protected abstract void renderText();

    @Override
    public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
        return false;
    }

    @Override
    public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
        return false;
    }

    @Override
    public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
        return false;
    }

    protected abstract void update() throws Exception;

    @Override
    public boolean updateEnvironment(float timeOfDay) {
        return false;
    }
}
