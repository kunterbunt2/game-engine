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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.*;
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
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicGameEngine implements ApplicationListener, InputProcessor, RenderEngineExtension {
    private static final float                           CAMERA_OFFSET_X   = 300f;
    private static final float                           CAMERA_OFFSET_Y   = 500f;
    private static final float                           CAMERA_OFFSET_Z   = 400f;
    private final        AudioEngine                     audioEngine       = new BasicAudioEngine();
    private final        Matrix4                         identityMatrix    = new Matrix4();
    private final        List<Label>                     labels            = new ArrayList<>();
    private final        Logger                          logger            = LoggerFactory.getLogger(this.getClass());
    private final        BasicRandomGenerator            randomGenerator   = new BasicRandomGenerator(1);
    private              BasicAtlasManager               atlasManager;
    private              Texture                         brdfLUT;
    private              MovingCamera                    camera;
    private              OrthographicCamera              camera2D;
    private              long                            currentTime       = 0L;
    private              Cubemap                         diffuseCubemap;
    //    private              BitmapFont                      font;
    private              boolean                         hrtfEnabled       = true;
    private              long                            lastTime          = 0;
    private              RenderEngine3D<BasicGameEngine> renderEngine;
    private              boolean                         simulateBassBoost = true;
    private              Cubemap                         specularCubemap;
    private              Stage                           stage;
    private              StringBuilder                   stringBuilder;
    private              boolean                         takeScreenShot    = false;
    private              long                            timeDelta         = 0L;

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
            BaiscDesktopContextFactory contextFactory = new BaiscDesktopContextFactory();
            Context                    context        = contextFactory.create();
            createCamera();
            atlasManager = new BasicAtlasManager();
            atlasManager.init();
            createStage();
            renderEngine = new RenderEngine3D<BasicGameEngine>(context, this, camera, camera2D, getAtlasManager().menuFont, getAtlasManager().systemTextureRegion);
            getRenderEngine().getWater().setPresent(false);
            getRenderEngine().getMirror().setPresent(false);
            getRenderEngine().setShadowEnabled(true);
            getRenderEngine().getFog().setEnabled(false);
            getRenderEngine().setDynamicDayTime(true);
            getRenderEngine().setSkyBox(false);
            getRenderEngine().setSceneBoxMin(new Vector3(-1000, -1000, -1000));
            getRenderEngine().setSceneBoxMax(new Vector3(1000, 1000, 1000));
            getRenderEngine().setShowGraphs(true);
            createEnvironment();
            getAudioEngine().create();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void resize(final int width, final int height) {
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
            getRenderEngine().postProcessRender();
            Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
            getRenderEngine().renderEngine2D.batch.enableBlending();
            getRenderEngine().renderEngine2D.batch.begin();
            getRenderEngine().renderEngine2D.batch.setProjectionMatrix(getRenderEngine().getCamera().combined);
//            renderText();
            getRenderEngine().renderEngine2D.batch.end();
            getRenderEngine().renderEngine2D.batch.setTransformMatrix(identityMatrix);//fix transformMatrix
            renderStage();
            takeScreenShot = false;
            getAudioEngine().begin(getRenderEngine().getCamera());
            getAudioEngine().end();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
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

    private void createCamera() throws Exception {
        camera = new MovingCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Vector3 lookat = new Vector3(0, 0, 0);
        camera.position.set(lookat.x + CAMERA_OFFSET_X, lookat.y + CAMERA_OFFSET_Y, lookat.z + CAMERA_OFFSET_Z);
        camera.up.set(0, 1, 0);
        camera.lookAt(lookat);
        camera.near = 8f;
        camera.far  = 8000f;
        camera.update();
        camera.setDirty(true);
        camera2D = new OrthographicCamera();
    }

    private Lwjgl3ApplicationConfiguration createConfig() {
        Lwjgl3ApplicationConfiguration config;
        config = new Lwjgl3ApplicationConfiguration();
        config.useVsync(true);
        config.setForegroundFPS(0);
        config.setResizable(false);
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL30, 3, 2); // use GL 3.0 (emulated by OpenGL 3.2)
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
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
            setupImageBasedLightingByFaceNames("clouds", "jpg", "jpg", "jpg", 10);
            getRenderEngine().environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
            getRenderEngine().environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
            getRenderEngine().environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
            getRenderEngine().environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 0f));
        } else {
        }
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

    public boolean isSimulateBassBoost() {
        return simulateBassBoost;
    }

    @Override
    public boolean keyDown(final int keycode) {
        switch (keycode) {
            case Input.Keys.Q:
                Gdx.app.exit();
                return true;
            case Input.Keys.NUM_2:
                setSimulateBassBoost(!isSimulateBassBoost());
                if (isSimulateBassBoost()) logger.info("bassBoost on");
                else logger.info("bassBoost off");
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
    public boolean keyUp(final int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(final char character) {
        return false;
    }

    @Override
    public boolean touchDown(final int screenX, final int screenY, final int pointer, final int button) {
        return false;
    }

    @Override
    public boolean touchUp(final int screenX, final int screenY, final int pointer, final int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(final int screenX, final int screenY, final int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(final int screenX, final int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(final float amountX, final float amountY) {
        return false;
    }

    public void render2Dxz() {

    }

//    protected abstract void renderText();

    private void renderStage() {
        int labelIndex = 0;
        // fps
        {
            stringBuilder.setLength(0);
            stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
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

    public void setSimulateBassBoost(boolean simulateBassBoost) {
        this.simulateBassBoost = simulateBassBoost;
    }

    private void setupImageBasedLightingByFaceNames(final String name, final String diffuseExtension, final String environmentExtension, final String specularExtension, final int specularIterations) {
        diffuseCubemap  = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), BasicAtlasManager.getAssetsFolderName() + "/textures/" + name + "/diffuse/diffuse_", "_0." + diffuseExtension, EnvironmentUtil.FACE_NAMES_FULL);
        specularCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), BasicAtlasManager.getAssetsFolderName() + "/textures/" + name + "/specular/specular_", "_", "." + specularExtension, specularIterations, EnvironmentUtil.FACE_NAMES_FULL);
        brdfLUT         = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
    }

    protected void startLwjgl() {
        final Lwjgl3ApplicationConfiguration config = createConfig();
        new Lwjgl3Application(this, config);
    }

    protected abstract void update() throws Exception;
}
