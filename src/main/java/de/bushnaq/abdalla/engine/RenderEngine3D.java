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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Config;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.crashinvaders.vfx.VfxManager;
import de.bushnaq.abdalla.engine.camera.MovingCamera;
import de.bushnaq.abdalla.engine.shader.GamePbrShaderProvider;
import de.bushnaq.abdalla.engine.shader.GameSettings;
import de.bushnaq.abdalla.engine.shader.GameShaderProvider;
import de.bushnaq.abdalla.engine.shader.GameShaderProviderInterface;
import de.bushnaq.abdalla.engine.shader.mirror.Mirror;
import de.bushnaq.abdalla.engine.shader.util.GL32CMacIssueHandler;
import de.bushnaq.abdalla.engine.shader.util.ShaderCompatibilityHelper;
import de.bushnaq.abdalla.engine.shader.water.Water;
import de.bushnaq.abdalla.engine.util.ExtendedGLProfiler;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.lights.PointLightEx;
import net.mgsx.gltf.scene3d.lights.SpotLightEx;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;
import net.mgsx.gltf.scene3d.scene.SceneRenderableSorter;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.scene.Updatable;
import net.mgsx.gltf.scene3d.shaders.PBRCommon;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.EnvironmentCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.Deflater;

/**
 * Project independent 3d class that renders everything
 *
 * @author kunterbunt
 */
public class RenderEngine3D<T extends RenderEngineExtension> {
    private       boolean                     alwaysDay                        = true;
    private       ColorAttribute              ambientLight;
    public        float                       angle;
    final         AtlasRegion                 atlasRegion;
    public        ModelBatch                  batch;//TODO make private again
    public        CustomizedSpriteBatch       batch2D;
    private final BitmapFont                  boldFont;
    private final MovingCamera                camera;
    private final OrthographicCamera          camera2D;
    private final EnvironmentCache            computedEnvironement             = new EnvironmentCache();
    private final IContext                    context;
    public        Graph                       cpuGraph;
    //    private              GameObject                  cameraCube;
    private       float                       currentDayTime;
    private       float                       dayAmbientIntensityB             = 1f;
    private       float                       dayAmbientIntensityG             = 1f;
    private       float                       dayAmbientIntensityR             = 1f;
    private       float                       dayShadowIntensity               = 5f;
    private       SceneSkybox                 daySkyBox;
    private       boolean                     debugMode                        = false;
    private       ModelBatch                  depthBatch;
    private       DepthOfFieldEffect          depthOfFieldEffect;
    private final ModelCache                  dynamicCache                     = new ModelCache();
    private       boolean                     dynamicDayTime                   = false;
    public        Array<GameObject<T>>        dynamicGameObjects               = new Array<>();
    private final Set<ObjectRenderer<T>>      dynamicText3DList                = new HashSet<>();
    private       boolean                     enableProfiling                  = true;
    public        Environment                 environment                      = new Environment();
    private       float                       fixedDayTime                     = 10;
    private       boolean                     fixedShadowDirection             = false;
    private final Fog                         fog                              = new Fog(Color.BLACK, 15f, 30f, 0.5f);
    private final BitmapFont                  font;
    private       Graph                       fpsGraph;
    private final T                           gameEngine;
    public        GameShaderProviderInterface gameShaderProvider;
    public        Graph                       gpuGraph;
    public final  Matrix4                     identityMatrix                   = new Matrix4();
    private final Logger                      logger                           = LoggerFactory.getLogger(this.getClass());
    //    private              GameObject                  lookatCube;
    private final Mirror                      mirror                           = new Mirror();
    private       float                       nightAmbientIntensityB           = .2f;
    private       float                       nightAmbientIntensityG           = .2f;
    private       float                       nightAmbientIntensityR           = .2f;
    private       float                       nightShadowIntensity             = .2f;
    public        SceneSkybox                 nightSkyBox;
    private       boolean                     pbr;
    final         PointLightsAttribute        pointLights                      = new PointLightsAttribute();
    private final Vector3                     position                         = new Vector3();
    private       FrameBuffer                 postFbo;
    private       FrameBuffer                 postMSFbo;
    private       ExtendedGLProfiler          profiler;
    public        Model                       rayCube;
    // private final Ray ray = new Ray(new Vector3(), new Vector3());
    private final Plane                       reflectionClippingPlane          = new Plane(new Vector3(0f, 1f, 0f), 0.1f);                                // render everything above the
    private final Plane                       refractionClippingPlane          = new Plane(new Vector3(0f, -1f, 0f), (-0.1f));                            // render everything below the
    public        boolean                     render2D                         = true;
    public        boolean                     render3D                         = true;
    public        RenderEngine25D<T>          renderEngine25D;
    public        RenderEngine2D<T>           renderEngine2D;
    private final Array<ModelInstance>        renderableProviders              = new Array<>();
    public        Render2Dxz<T>               renderutils2Dxz;
    private       Vector3                     sceneBoxMax                      = new Vector3(1000, 1000, 1000);
    private       Vector3                     sceneBoxMin                      = new Vector3(-1000, -1000, -1000);
    private final BoundingBox                 sceneBox                         = new BoundingBox(sceneBoxMin, sceneBoxMax);
    private       boolean                     shadowEnabled                    = true;
    private       DirectionalShadowLight      shadowLight                      = null;
    private final Vector3                     shadowLightDirection             = new Vector3();
    private       boolean                     skyBox                           = false;
    private final int                         speed                            = 5;                                                                    // speed of time
    private final SpotLightsAttribute         spotLights                       = new SpotLightsAttribute();
    private       Stage                       stage;
    private final ModelCache                  staticCache                      = new ModelCache();
    private       boolean                     staticCacheDirty                 = true;
    private       int                         staticCacheDirtyCount            = 0;
    public final  Array<GameObject<T>>        staticGameObjects                = new Array<>();
    private final Set<ObjectRenderer<T>>      staticText3DList                 = new HashSet<>();
    public        int                         testCase                         = 1;
    private final Set<Text2D>                 text2DList                       = new HashSet<>();
    private       float                       timeOfDay                        = 8;                                                                    // 24h time
    private final boolean                     useDynamicCache                  = false;
    private final boolean                     useStaticCache                   = true;
    private       VfxManager                  vfxManager                       = null;
    public        int                         visibleDynamicGameObjectCount    = 0;
    private final Array<GameObject<T>>        visibleDynamicGameObjects        = new Array<>();
    public        int                         visibleDynamicLightCount         = 0;
    private final Array<ModelInstance>        visibleDynamicModelInstances     = new Array<>();
    public        int                         visibleStaticGameObjectCount     = 0;
    private final Array<GameObject<T>>        visibleStaticGameObjects         = new Array<>();
    public        int                         visibleStaticLightCount          = 0;
    private final Array<ModelInstance>        visibleStaticModelInstances      = new Array<>();
    private final Array<RenderableProvider>   visibleStaticRenderableProviders = new Array<>();
    private final Water                       water                            = new Water();
    final         Vector3                     xVector                          = new Vector3(1, 0, 0);

    public RenderEngine3D(final IContext context, T gameEngine, MovingCamera camera, OrthographicCamera camera2D, BitmapFont font, BitmapFont boldFont, AtlasRegion atlasRegion) throws Exception {
//		logger.info(String.format("GL_VERSION = %s", Gdx.gl.glGetString(GL20.GL_VERSION)));
//		logger.info(String.format("GL_ES_VERSION_2_0 = %s", Gdx.gl.glGetString(GL20.GL_ES_VERSION_2_0)));
//		logger.info(String.format("GL_SHADING_LANGUAGE_VERSION = %s", Gdx.gl.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION)));
//		{
//			String exts = Gdx.gl.glGetString(GL20.GL_EXTENSIONS);
//			if (exts != null) {
//				int i = 0;
//				for (String ext : exts.split(" ")) {
//					System.out.println(i++ + " " + ext);
//				}
//			}
//		}
        logger.info("----------------------------------------------------------------------------------");
        logger.info(String.format("Gdx.graphics.getWidth() = %d Gdx.graphics.getHeight() = %d", Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        this.context     = context;
        this.gameEngine  = gameEngine;
        this.camera      = camera;
        this.camera2D    = camera2D;
        this.font        = font;
        this.boldFont    = boldFont;
        this.atlasRegion = atlasRegion;
        renderEngine2D   = new RenderEngine2D<>(gameEngine, camera2D);
        renderEngine25D  = new RenderEngine25D<>(gameEngine, camera2D);
        if (testCase == 1) {
            renderutils2Dxz = new Render2Dxz<>(gameEngine, camera);
        }
        if (testCase == 2) {
            renderutils2Dxz = new Render2Dxz<>(gameEngine, camera2D);
        }
        if (testCase == 3) {
//            renderutils2Dxz = new Render2Dxz<>(gameEngine, camera2D);
        }

        create();
        logger.info(String.format("fog = %b", getFog().isEnabled()));
        logger.info(String.format("pbr = %b", isPbr()));
        logger.info(String.format("mirror = %b", isMirrorPresent()));
        logger.info(String.format("water = %b", isWaterPresent()));
        logger.info(String.format("shadow = %b", isShadowEnabled()));
        logger.info(String.format("depth of field 2 = %b", depthOfFieldEffect.isEnabled()));
        logger.info(String.format("dynamic day= %b", isDynamicDayTime()));
        logger.info(String.format("debug mode = %b", isDebugMode()));
        logger.info(String.format("sky box = %b", isSkyBox()));
        logger.info(String.format("graphs = %b", isShowGraphs()));
        logger.info("----------------------------------------------------------------------------------");
    }

    public void add(final PointLight pointLight, final boolean dynamic) {

        if (dynamic) {
            environment.add(pointLight);
        } else {
            environment.add(pointLight);
        }
    }

    public void add(Text2D text2d) {
        text2DList.add(text2d);
    }

    public void addBloomEffect() {
//		vfxManager.addEffect(effect2);
    }

    public void addBlurEffect() {
//		vfxManager.addEffect(effect1);
    }

    public void addDynamic(ObjectRenderer<T> renderer) {
        dynamicText3DList.add(renderer);
    }

    public void addDynamic(final GameObject<T> instance) {
        dynamicGameObjects.add(instance);
    }

    public void addStatic(ObjectRenderer<T> renderer) {
        staticText3DList.add(renderer);
    }

    public void addStatic(final GameObject<T> gameObject) {
        staticGameObjects.add(gameObject);
        if (isVisible(gameObject)) {
            staticCacheDirty = true;
            staticCacheDirtyCount++;
            visibleStaticModelInstances.add(gameObject.instance);
            //do we have 3D text to render?
            if (gameObject.objectRenderer != null) visibleStaticGameObjects.add(gameObject);
        }
    }

    public void addStatic(RenderableProvider renderableProvider) {
        visibleStaticRenderableProviders.add(renderableProvider);
        staticCacheDirty = true;
        staticCacheDirtyCount++;
    }

    public void clearViewport() {
//        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);// modelBatch will change this state anyway, so better enable it when you
        // need it
//        Gdx.gl.glClearColor(getFog().getColor().r, getFog().getColor().g, getFog().getColor().b, 1.0f);
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
//        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
//        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void create() throws Exception {
        profiler = new ExtendedGLProfiler(Gdx.graphics);
        profiler.setListener(GLErrorListener.LOGGING_LISTENER);// ---enable exception throwing in case of error
        profiler.setListener(new MyGLErrorListener());
        if (isEnableProfiling()) {
            profiler.enable();
        }
        pbr       = context.getPbrModeProperty();
        debugMode = context.getDebugModeProperty();
        createFrameBuffer();
        createShader();
        createEnvironment();
        createStage();
//        createCoordinates();
//		createRayCube();
//		createBlurEffect();
//		createBloomEffect();
//		vfxManager.addEffect(new FxaaEffect());
//		vfxManager.addEffect(new FilmGrainEffect());
//		vfxManager.addEffect(new OldTvEffect());
        vfxManager         = new VfxManager(Pixmap.Format.RGBA8888);
        depthOfFieldEffect = new DepthOfFieldEffect(this, vfxManager, postFbo, camera);
        depthOfFieldEffect.setEnabled(true);
        vfxManager.addEffect(depthOfFieldEffect);
//		vfxManager.addEffect(new FxaaEffect());
        createGraphs();
    }

    private void createBloomEffect() {
//		Settings s = new Settings(50, 0.999f, 1.0f, 1.0f, 10.0f, 0.5f);
//		effect2 = new BloomEffect(s);
    }

    private void createBlurEffect() {
//		effect = new MotionBlurEffect(Pixmap.Format.RGBA8888, MixEffect.Method.MAX, .8f);
//		effect1 = new GaussianBlurEffect();
//		effect1.setType(BlurType.Gaussian5x5);
//		effect1.setAmount(100);
//		effect1.setPasses(32);
    }

    private void createCoordinates() {
        createRayCube();
        final Vector3 position = new Vector3(0, 0, 0);
        final Vector3 xVector  = new Vector3(1, 0, 0);
        final Vector3 yVector  = new Vector3(0, 1, 0);
        final Vector3 zVector  = new Vector3(0, 0, 1);
        final Ray     rayX     = new Ray(position, xVector);
        final Ray     rayY     = new Ray(position, yVector);
        final Ray     rayZ     = new Ray(position, zVector);
        createRay(rayX, null, false);
        createRay(rayY, null, false);
        createRay(rayZ, null, false);
    }

    private void createEnvironment() {
        // shadow
        int shadowMapSize = context.getShadowMapSizeProperty();
        shadowLight = new DirectionalShadowLight(shadowMapSize, shadowMapSize, GameSettings.SHADOW_VIEWPORT_WIDTH, GameSettings.SHADOW_VIEWPORT_HEIGHT, GameSettings.SHADOW_NEAR, GameSettings.SHADOW_FAR);
        final Matrix4 m = new Matrix4();
        sceneBox.mul(m);
        shadowLight.setBounds(sceneBox);
        shadowLight.direction.set(-.5f, -.7f, .5f).nor();
        shadowLight.color.set(Color.WHITE);
        shadowLight.intensity = 10.0f;
        environment.add(shadowLight);

        final float lum = 0.0f;
        ambientLight = new ColorAttribute(ColorAttribute.AmbientLight, lum, lum, lum, 1.0f);
        environment.set(ambientLight);
        getFog().createFog(environment);
    }

    private String createFileName(final Date date, final String append) {
        final String           pattern          = "yyyy-MM-dd-HH-mm-ss";
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        final String           dateAsString     = simpleDateFormat.format(date);
        final String           fileName         = "screenshots/" + dateAsString + "-" + append + ".png";
        return fileName;
    }

    private void createFrameBuffer() {
        water.createFrameBuffer();
        getMirror().createFrameBuffer();
        {
            final GLFrameBuffer.FrameBufferBuilder frameBufferBuilder = new GLFrameBuffer.FrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGBA8, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE);
            frameBufferBuilder.addDepthTextureAttachment(GL30.GL_DEPTH_COMPONENT24, GL20.GL_UNSIGNED_BYTE);
            postFbo = frameBufferBuilder.build();
            postFbo.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }
        {
            final GLFrameBuffer.FrameBufferBuilder frameBufferBuilder = new GLFrameBuffer.FrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), context.getMSAASamples());
            frameBufferBuilder.addColorRenderBuffer(GL30.GL_RGBA8).addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24).build();
            postMSFbo = frameBufferBuilder.build();
        }
    }


    private void createGraphs() {
        cpuGraph = new TimeGraph("CPU", new Color(1f, 0f, 0f, 1f), new Color(1f, 0, 0, 0.6f), new Color(0f, 0f, 0f, .6f), Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 3, font, boldFont, atlasRegion);
        gpuGraph = new TimeGraph("GPU", new Color(0f, 1f, 0f, 1f), new Color(0f, 1f, 0f, 0.6f), new Color(0f, 0f, 0f, .6f), Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 3, font, boldFont, atlasRegion);
        fpsGraph = new FpsGraph("FPS", new Color(0f, 0f, 1f, 1f), new Color(0f, 0f, 1f, 0.6f), new Color(0f, 0f, 0f, .6f), Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 3, font, boldFont, atlasRegion);
    }

    private GameObject<T> createRay(final Ray ray, Float length, boolean center) {
        if (length == null) length = 10000f;
//		final float			length		= 10000f;
        final Vector3       direction = new Vector3(ray.direction.x, ray.direction.y, ray.direction.z);
        final Vector3       position  = ray.origin.cpy();
        final GameObject<T> instance  = new GameObject<T>(new ModelInstanceHack(rayCube), null);
        instance.instance.materials.get(0).set(ColorAttribute.createDiffuse(Color.RED));
        addStatic(instance);
        final Vector3 xVector = new Vector3(1, 0, 0);
        direction.nor();
        if (center) {
            position.x += direction.x /** length / 2*/;
            position.y += direction.y /** length / 2*/;
            position.z += direction.z /** length / 2*/;
        } else {
            position.x += direction.x * length / 2;
            position.y += direction.y * length / 2;
            position.z += direction.z * length / 2;
        }
        instance.instance.transform.setToTranslation(position);
        instance.instance.transform.rotate(xVector, direction);
        instance.instance.transform.scale(length, 0.5f, 0.5f);
        instance.update();
        return instance;
        // System.out.println("created ray");
    }

    private void createRayCube() {
        if (isPbr() && rayCube == null) {
            final Attribute    color        = new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, Color.WHITE);
            final Attribute    metallic     = PBRFloatAttribute.createMetallic(0.5f);
            final Attribute    roughness    = PBRFloatAttribute.createRoughness(0.5f);
            final Attribute    occlusion    = PBRFloatAttribute.createOcclusionStrength(1.0f);
            final Material     material     = new Material(metallic, roughness, color, occlusion);
            final ModelBuilder modelBuilder = new ModelBuilder();
            rayCube = modelBuilder.createBox(1.0f, 1.0f, 1.0f, material, Usage.Position | Usage.Normal);
        }
    }

    private void createShader() {
        RenderableSorter renderableSorter = new SceneRenderableSorter();
        if (isPbr()) {
            depthBatch = new ModelBatch(PBRShaderProvider.createDefaultDepth(0));
        } else {
            depthBatch = new ModelBatch(new DepthShaderProvider());
        }
        batch = new ModelBatch(createShaderProvider(), renderableSorter);
//        batch2D = new CustomizedSpriteBatch(5460, ShaderCompatibilityHelper.mustUse32CShader() ? GL32CMacIssueHandler.createSpriteBatchShader() : null);
        batch2D = new CustomizedSpriteBatch(5460, ShaderCompatibilityHelper.mustUse32CShader() ? GL32CMacIssueHandler.createSpriteBatchShader() : null);
    }

    private ShaderProvider createShaderProvider() {
        if (isPbr()) {
            final PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
            config.numBones             = 0;
            config.numDirectionalLights = 1;
            config.numPointLights       = context.getMaxPointLights();
            config.numSpotLights        = 0;
            gameShaderProvider          = GamePbrShaderProvider.createDefault(config, water, mirror);
            return gameShaderProvider;
        } else {

            DefaultShader.Config config = new Config();
//			config.numDirectionalLights = 2;
//			config.numPointLights = context.getMaxPointLights();
//			config.numSpotLights = 0;
            gameShaderProvider = GameShaderProvider.createDefault(config, water, mirror);
            return gameShaderProvider;
        }
    }

    private void createStage() {
        stage = new Stage(new ScreenViewport(), renderEngine2D.batch);
    }

    private void cullLights() {
        visibleDynamicLightCount = 0;
        final PointLightsAttribute pla = environment.get(PointLightsAttribute.class, PointLightsAttribute.Type);
        if (pla != null) {
            for (final PointLight light : pla.lights) {
                if (light instanceof PointLightEx l) {
                    if (l.range != null && !camera.frustum.sphereInFrustum(l.position, l.range)) {
                        pointLights.lights.removeValue(l, true);
                    }
                } else if (light instanceof PointLight) {
                    final PointLight l = light;
                    if (!camera.frustum.sphereInFrustum(l.position, 50)) {
                        pointLights.lights.removeValue(l, true);
                    } else {
                        visibleDynamicLightCount++;
                    }
                }
            }
        }
        final SpotLightsAttribute sla = environment.get(SpotLightsAttribute.class, SpotLightsAttribute.Type);
        if (sla != null) {
            for (final SpotLight light : sla.lights) {
                if (light instanceof SpotLightEx l) {
                    if (l.range != null && !camera.frustum.sphereInFrustum(l.position, l.range)) {
                        spotLights.lights.removeValue(l, true);
                    }
                } else if (light instanceof SpotLight) {
                    final SpotLight l = light;
                    if (!camera.frustum.sphereInFrustum(l.position, 50)) {
                        spotLights.lights.removeValue(l, true);
                    } else {
                        visibleDynamicLightCount++;
                    }
                }
            }
        }
    }

    public void dispose() throws Exception {
        if (profiler.isEnabled()) {
            profiler.disable();
        }
        staticCache.dispose();
        dynamicCache.dispose();
        vfxManager.dispose();
        depthOfFieldEffect.dispose();
        disposeGraphs();
        disposeStage();
        disposeEnvironment();
        disposeShader();
        disposeFrameBuffer();
        renderEngine2D.dispose();
    }

    private void disposeEnvironment() {
        if (isPbr()) {
            if (nightSkyBox != null) nightSkyBox.dispose();
            if (daySkyBox != null) daySkyBox.dispose();
        }
        shadowLight.dispose();
        environment.clear();
    }

    private void disposeFrameBuffer() {
        postMSFbo.dispose();
        mirror.dispose();
        water.dispose();
    }

    private void disposeGraphs() {
        gpuGraph.dispose();
        cpuGraph.dispose();
    }

    private void disposeShader() {
        gameShaderProvider.dispose();
//        batch2D.dispose();
        batch.dispose();
        depthBatch.dispose();
    }

    private void disposeStage() {
        stage.dispose();
    }

    public void end() {
    }

//	private void createDepthOfFieldMeter() {
//		if (isDebugMode()) {
//
//			if (depthOfFieldMeter == null) {
//				final Ray	ray	= new Ray();
//				Vector3		o	= new Vector3(camera.position);
//				o.add(0f, 0f, -camera.near);
//				ray.set(o, camera.direction);
//				depthOfFieldMeter = createRay(ray, camera.far - camera.near);
//
//				addStatic(depthOfFieldMeter);
//			}
//		} else {
//			if (depthOfFieldMeter != null) {
//				removeStatic(depthOfFieldMeter);
//			}
//		}
//	}

//	private void createCameraCube() {
//		if (isDebugMode()) {
//			if (cameraCube == null) {
//				cameraCube = new GameObject(new ModelInstanceHack(rayCube), null);
//				cameraCube.instance.materials.get(0).set(ColorAttribute.createDiffuse(Color.RED));
//				cameraCube.instance.transform.scale(0.5f, 0.5f, 0.5f);
//				addDynamic(cameraCube);
//			}
//			final Vector3 position = new Vector3();
//			cameraCube.instance.transform.getTranslation(position);
//			if (!position.equals(camera.position)) {
//				cameraCube.instance.transform.setToTranslation(camera.position);
//				cameraCube.update();
//			}
//		} else {
//			if (cameraCube != null) {
//				removeDynamic(cameraCube);
//			}
//		}
//	}

    public MovingCamera getCamera() {
        return camera;
    }

    public float getCurrentDayTime() {
        return currentDayTime;
    }

    public DepthOfFieldEffect getDepthOfFieldEffect() {
        return depthOfFieldEffect;
    }

//    private void fboToScreen() {
//        clearViewport();
//        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
//        batch2D.disableBlending();
//        batch2D.setProjectionMatrix(camera2D.combined);
//        batch2D.begin();
//        batch2D.draw(postFbo.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, postFbo.getWidth(), postFbo.getHeight(), false, true);
//        batch2D.end();
//        batch2D.enableBlending();
//    }

//	private void createLookatCube() {
//		if (isDebugMode()) {
//			if (lookatCube == null) {
//				lookatCube = new GameObject(new ModelInstanceHack(rayCube), null);
//				lookatCube.instance.materials.get(0).set(ColorAttribute.createDiffuse(Color.RED));
//				lookatCube.instance.transform.scale(0.5f, 0.5f, 0.5f);
//				addDynamic(lookatCube);
//			}
//			final Vector3 position = new Vector3();
//			lookatCube.instance.transform.getTranslation(position);
//			if (!position.equals(camera.lookat)) {
//				lookatCube.instance.transform.setToTranslation(camera.lookat);
//				lookatCube.update();
//			}
//		} else {
//			if (lookatCube != null) {
//				removeDynamic(lookatCube);
//			}
//		}
//	}

    public float getFixedDayTime() {
        return fixedDayTime;
    }

    public Fog getFog() {
        return fog;
    }

    public T getGameEngine() {
        return gameEngine;
    }

    public GameObject<T> getGameObject(final int screenX, final int screenY) {
        final Ray ray = camera.getPickRay(screenX, screenY);
//        createRay(ray, null);
        GameObject<T> result   = null;
        float         distance = -1;
        for (int i = 0; i < dynamicGameObjects.size; ++i) {
            final GameObject<T> instance = dynamicGameObjects.get(i);
            if (instance.interactive != null) {
                instance.instance.transform.getTranslation(position);
                position.add(instance.center);
                final float dist2 = ray.origin.dst2(position);
                if (distance >= 0f && dist2 > distance) continue;
                if (Intersector.intersectRayBoundsFast(ray, instance.transformedBoundingBox)) {
                    result   = instance;
                    distance = dist2;
                }
            }
        }
        for (int i = 0; i < staticGameObjects.size; ++i) {
            final GameObject<T> instance = staticGameObjects.get(i);
            if (instance.interactive != null) {
                instance.instance.transform.getTranslation(position);
                position.add(instance.center);
                final float dist2 = ray.origin.dst2(position);
                if (distance >= 0f && dist2 > distance) continue;
                if (Intersector.intersectRayBoundsFast(ray, instance.transformedBoundingBox)) {
                    result   = instance;
                    distance = dist2;
                }
            }
        }
        return result;
    }

    public Mirror getMirror() {
        return mirror;
    }

    public ExtendedGLProfiler getProfiler() {
        return profiler;
    }

    public Array<ModelInstance> getRenderableProviders() {
        return renderableProviders;
    }

    public BoundingBox getSceneBox() {
        return sceneBox;
    }

    public DirectionalShadowLight getShadowLight() {
        return shadowLight;
    }

    public float getTimeOfDay() {
        return timeOfDay;
    }

    public Water getWater() {
        return water;
    }

    private void handleFrameBufferScreenshot(boolean takeScreenShot, final FrameBuffer frameBuffer, final String name) {
        if (takeScreenShot) {
            final Date   date     = new Date();
            final String fileName = createFileName(date, name);
            writeFrameBufferToDisk(fileName, frameBuffer);
            takeScreenShot = false;
        }

    }

    public void handleQueuedScreenshot(final boolean takeScreenShot) {
        if (takeScreenShot) {
            final Date   date     = new Date();
            final String fileName = createFileName(date, "frame.buffer");
            final byte[] pixels   = ScreenUtils.getFrameBufferPixels(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), true);
            // This loop makes sure the whole screenshot is opaque and looks exactly like
            // what the user is seeing
            for (int i = 4; i < pixels.length; i += 4) {
                pixels[i - 1] = (byte) 255;
            }
            final Pixmap pixmap = new Pixmap(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight(), Pixmap.Format.RGBA8888);
            BufferUtils.copy(pixels, 0, pixmap.getPixels(), pixels.length);
            final FileHandle handle = Gdx.files.local(fileName);
            PixmapIO.writePNG(handle, pixmap);
            pixmap.dispose();
        }
    }

    public boolean isAlwaysDay() {
        return alwaysDay;
    }

    public boolean isDay() {
        return (alwaysDay || (timeOfDay > 6 && timeOfDay <= 18));
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isDynamicDayTime() {
        return dynamicDayTime;
    }

    public boolean isEnableProfiling() {
        return enableProfiling;
    }

//    public boolean isDepthOfField() {
//        return depthOfField;
//    }

    public boolean isFixedShadowDirection() {
        return fixedShadowDirection;
    }

    public boolean isMirrorPresent() {
        return mirror.isPresent() /* && isPbr() */;
    }

    public boolean isNight() {
        return (!alwaysDay && (timeOfDay > 19 || timeOfDay <= 5));
    }

    public boolean isPbr() {
        return pbr;
    }

    public boolean isShadowEnabled() {
        return shadowEnabled;
    }

    public boolean isShowGraphs() {
        return context.getShowGraphsProperty();
    }

    public boolean isSkyBox() {
        return skyBox;
    }

    private boolean isVisible(final GameObject<T> gameObject) {
        return camera.frustum.boundsInFrustum(gameObject.transformedBoundingBox);
    }

    public boolean isWaterPresent() {
        return water.isPresent() /* && isPbr() */;
    }

    public void postProcessRender() throws Exception {
        if (depthOfFieldEffect.isEnabled() && render3D) {
            // Clean up the screen.
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            postMSFbo.transfer(postFbo);
            // Clean up internal buffers, as we don't need any information from the last render.
            vfxManager.cleanUpBuffers();
            vfxManager.applyEffects();
            // Render result to the screen.
            postFbo.begin();
            postFbo.end();
            vfxManager.renderToScreen();
        } else {
//            clearViewport();
//            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
//            batch2D.disableBlending();
//            batch2D.setProjectionMatrix(camera2D.combined);
//            batch2D.begin();
//            batch2D.draw(postFbo.getColorBufferTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false, true);
//            batch2D.end();
//            batch2D.enableBlending();
        }

    }

    public void remove(final PointLight pointLight, final boolean dynamic) {
        if (dynamic) {
            environment.remove(pointLight);
        } else {
            environment.remove(pointLight);
        }
    }

    public boolean removeAllDynamic() {
        dynamicGameObjects.clear();
        return true;
    }

    public void removeAllDynamicText3D() {
        dynamicText3DList.clear();
    }

    public void removeAllEffects() {
//		vfxManager.removeAllEffects();
    }

    public boolean removeAllStatic() {
        staticCacheDirty      = true;
        staticCacheDirtyCount = 0;

        visibleStaticModelInstances.clear();
        return true;
    }

    public void removeAllStaticText3D() {
        staticText3DList.clear();
    }

    public void removeAllText2D() {
        text2DList.clear();
    }

    public void removeBloomEffect() {
//		vfxManager.removeEffect(effect2);
    }

    public void removeBlurEffect() {
//		vfxManager.removeEffect(effect1);
    }

    public void removeDynamic(ObjectRenderer<T> renderer) {
        dynamicText3DList.remove(renderer);
    }

    public boolean removeDynamic(final GameObject<T> gameObject) {
        return dynamicGameObjects.removeValue(gameObject, true);
    }

    public void removeStatic(ObjectRenderer<T> renderer) {
        staticText3DList.remove(renderer);
    }

    public boolean removeStatic(final GameObject<T> gameObject) {
        final boolean result = staticGameObjects.removeValue(gameObject, true);
        if (isVisible(gameObject)) {
            staticCacheDirty = true;
            staticCacheDirtyCount++;
            visibleStaticModelInstances.removeValue(gameObject.instance, true);
        }
        return result;
    }

    public void render(final long currentTime, final float deltaTime, final boolean takeScreenShot) throws Exception {
        fpsGraph.end();
        fpsGraph.begin();
        float x1 = shadowLight.getCamera().position.x;
        if (isDynamicDayTime()) {
            // keep only what is smaller than 10000
            currentDayTime = currentTime - ((currentTime / (50000L / speed * 24)) * (50000L / speed * 24));
            currentDayTime /= 50000 / speed;
        } else {
            setCurrentDayTime(getFixedDayTime());
        }
        if (render3D) updateEnvironment(getCurrentDayTime());
        renderableProviders.clear();
        if (render3D) {
            updateDynamicModelInstanceCache();
            updateStaticModelInstanceCache();
            getFog().updateFog(environment);
        }

        if (render3D) update(deltaTime);
        if (isShadowEnabled() && render3D) {
            renderShadows(takeScreenShot);
        }
        if (isPbr() && render3D) {
            PBRCommon.enableSeamlessCubemaps();
        }
        if (render3D) computedEnvironement.shadowMap = environment.shadowMap;
        // handleFrameBufferScreenshot(takeScreenShot);

        // FBO
        if (isWaterPresent() && render3D) {
//			boolean skyBox = isSkyBox();
//			setSkyBox(true);
            // waterRefractionFbo
            context.enableClipping();
            water.getRefractionFbo().begin();
            gameShaderProvider.setClippingPlane(refractionClippingPlane);
            renderColors(takeScreenShot);
            water.getRefractionFbo().end();
            handleFrameBufferScreenshot(takeScreenShot, water.getRefractionFbo(), "water.refraction.fbo");

            // waterReflectionFbo
            gameShaderProvider.setClippingPlane(reflectionClippingPlane);
            final float cameraYDistance = 2 * (camera.position.y - context.getWaterLevel());
            final float lookatYDistance = 2 * (camera.lookat.y - context.getWaterLevel());
            camera.position.y -= cameraYDistance;
            camera.lookat.y -= lookatYDistance;
            camera.up.set(0, 1, 0);
            camera.lookAt(camera.lookat);
            camera.update();
//			createCameraCube();
//			createLookatCube();
            water.getReflectionFbo().begin();
            renderColors(takeScreenShot);
            water.getReflectionFbo().end();
            camera.position.y += cameraYDistance;
            camera.lookat.y += lookatYDistance;
            camera.up.set(0, 1, 0);
            camera.lookAt(camera.lookat);
            camera.update();
            handleFrameBufferScreenshot(takeScreenShot, water.getReflectionFbo(), "water.reflection.fbo");

            context.disableClipping();
//			setSkyBox(skyBox);
        }
        if (isMirrorPresent() && render3D) {
            // waterReflectionFbo
            context.enableClipping();

            gameShaderProvider.setClippingPlane(reflectionClippingPlane);
            final float cameraYDistance = 2 * (camera.position.y - context.getMirrorLevel());
            final float lookatYDistance = 2 * (camera.lookat.y - context.getMirrorLevel());
            camera.position.y -= cameraYDistance;
            camera.lookat.y -= lookatYDistance;
            camera.up.set(0, 1, 0);
            camera.lookAt(camera.lookat);
            camera.update();
//			createCameraCube();
//			createLookatCube();
            mirror.getReflectionFbo().begin();
            renderColors(takeScreenShot);
            mirror.getReflectionFbo().end();

            camera.position.y += cameraYDistance;
            camera.lookat.y += lookatYDistance;
            camera.up.set(0, 1, 0);
            camera.lookAt(camera.lookat);
            camera.update();
            handleFrameBufferScreenshot(takeScreenShot, mirror.getReflectionFbo(), "mirror.reflection.fbo");

            context.disableClipping();
        }
        // if (firstTime) {
        if (depthOfFieldEffect.isEnabled() && render3D) postMSFbo.begin();
//		createCameraCube();
//		createLookatCube();
//		createDepthOfFieldMeter();
        renderColors(takeScreenShot);
        render3DText();
        renderBokeh();
        render2Dxz();
        if (depthOfFieldEffect.isEnabled() && render3D) postMSFbo.end();

        camera.setDirty(false);
        staticCacheDirtyCount = 0;
        renderGraphs();

        if (depthOfFieldEffect.isEnabled() && render3D) postMSFbo.begin();
        if (render3D) renderFbos(takeScreenShot);
        if (depthOfFieldEffect.isEnabled() && render3D) postMSFbo.end();

        postProcessRender();
        render2DText();
    }

    private void render2DText() {
        renderEngine2D.batch.setProjectionMatrix(stage.getViewport().getCamera().combined);
        renderEngine2D.batch.begin();
        Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
        renderEngine2D.batch.enableBlending();
        for (Text2D text2d : text2DList) {
            text2d.draw(renderEngine2D.batch);
        }
        renderEngine2D.batch.end();
    }

    private void render2Dxz() {
        if (render2D) {
            Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
            renderutils2Dxz.batch.begin();
//            renderutils2Dxz.batch.setColor(new Color(.9f, .4f, .5f, 0.45f));
//            renderutils2Dxz.batch.fillCircle(atlasRegion, 0, 0, 32, 32);
            gameEngine.render2Dxz();

//            for (GameObject<T> gameObject : dynamicGameObjects) {
//                if (gameObject.objectRenderer != null) {
//                    gameObject.objectRenderer.render2D(this, 0, false);
//                }
//            }
//            for (GameObject<T> gameObject : staticGameObjects) {
//                if (gameObject.objectRenderer != null) {
//                    gameObject.objectRenderer.render2D(this, 0, false);
//                }
//            }
            renderutils2Dxz.batch.end();
        }
    }

    private void render3DText() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        if (render3D) {
            renderEngine25D.batch.begin();
            renderEngine25D.batch.enableBlending();
            renderEngine25D.batch.setProjectionMatrix(camera.combined);
            profiler.setStaticText3D(staticText3DList.size());
            for (final ObjectRenderer<T> renderer : staticText3DList) {
                renderer.renderText(this, 0, false);
            }
            profiler.setDynamicText3D(dynamicText3DList.size());
            for (final ObjectRenderer<T> renderer : dynamicText3DList) {
                renderer.renderText(this, 0, false);
            }
            profiler.setVisibleStaticGameObjects(visibleStaticGameObjects.size);
            for (GameObject<T> gameObject : visibleStaticGameObjects) {
                if (gameObject.objectRenderer != null) gameObject.objectRenderer.renderText(this, 0, false);
            }
            profiler.setVisibleDynamicGameObjects(visibleDynamicGameObjects.size);
            for (GameObject<T> gameObject : visibleDynamicGameObjects) {
                if (gameObject.objectRenderer != null) {
                    gameObject.objectRenderer.renderText(this, 0, false);
//                if (drawMode == drawMode.DrawMode2D)
                    {
//                    gameObject.objectRenderer.render2D(this, 0, false);
                    }
                }
            }
            renderEngine25D.batch.end();
            renderEngine25D.batch.setTransformMatrix(identityMatrix);// fix transformMatrix
        }
    }

    /**
     * render a bokeh for every visible light source.
     */
    private void renderBokeh() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        if (render3D) {
            renderEngine25D.batch.begin();
            renderEngine25D.batch.enableBlending();
            renderEngine25D.batch.setProjectionMatrix(camera.combined);
            if (getDepthOfFieldEffect().isEnabled()) {
                float focalDepth;
                focalDepth = getDepthOfFieldEffect().getFocalDepth();
                for (PointLight light : pointLights.lights) {
                    float depth = light.position.dst(camera.position);
                    if (!depthOfFieldEffect.isInFocus(depth)) {
                        {
                            final Matrix4 m = new Matrix4();
                            m.setToTranslation(light.position.x, light.position.y, light.position.z);
                            m.rotateTowardTarget(camera.position, camera.up);
                            renderEngine25D.setTransformMatrix(m);
                        }
                        if (camera.frustum.pointInFrustum(light.position.x, light.position.y, light.position.z)) {
                            //the further the light, the bigger the bokeh
                            float size;
                            if (depth > focalDepth) {
                                size = 8 * ((depth - focalDepth - depthOfFieldEffect.getFarDofStart()) / (depthOfFieldEffect.getFarDofDist() - depthOfFieldEffect.getFarDofStart()));
                            } else {
//                                size = 8 * ((focalDepth - depth - depthOfFieldEffect.getNearDofStart()) / (depthOfFieldEffect.getNearDofDist() - depthOfFieldEffect.getNearDofStart()));
                                break;
                            }
//                            if (camera.frustum.pointInFrustum(light.position))
//                                if (light.position.z < -1000)
//                                    if (size < 0)
//                                        System.out.println(" size=" + size + "dist=" + light.position.dst(camera.position));
                            Color c = light.color;
                            c.a = 0.5f;
                            renderEngine25D.fillCircle(atlasRegion, 0, 0, size - 0.4f, 32, c);
                            c.a = .3f;
                            renderEngine25D.circle(atlasRegion, 0, 0, size, 0.8f, c, 32);
                        }
                    }
                }
            }
            renderEngine25D.batch.end();
            renderEngine25D.batch.setTransformMatrix(identityMatrix);// fix transformMatrix
        }
    }

    /**
     * Render colors only. You should call {@link #renderShadows(boolean takeScreenShot)} before. (useful when you're using your own frame buffer to render scenes)
     */
    private void renderColors(final boolean takeScreenShot) {
        clearViewport();
        if (render3D) {
            batch.begin(camera);
            if (useStaticCache) batch.render(staticCache, computedEnvironement);
//         else
//         batch.render(visibleStaticModelInstances, computedEnvironement);
            if (useDynamicCache) batch.render(dynamicCache, computedEnvironement);
            else batch.render(visibleDynamicModelInstances, computedEnvironement);
            if (isSkyBox()) {
                if (daySkyBox != null && isDay()) batch.render(daySkyBox);
                else if (nightSkyBox != null && isNight()) batch.render(nightSkyBox);
            }
            batch.end();
        }
    }

    /**
     * Render only depth (packed 32 bits), useful for post-processing effects. You typically render it to a FBO with depth enabled.
     */
    private void renderDepth(final Camera camera) {
        depthBatch.begin(camera);
        if (useStaticCache) depthBatch.render(staticCache);
        // else
        // depthBatch.render(visibleStaticModelInstances);
        if (useDynamicCache) depthBatch.render(dynamicCache);
        else depthBatch.render(visibleDynamicModelInstances);
        depthBatch.end();
    }

    private void renderFbos(boolean takeScreenShot) {
        renderEngine2D.batch.begin();
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        renderEngine2D.batch.enableBlending();
        renderEngine2D.batch.setProjectionMatrix(stage.getViewport().getCamera().combined);
        if (debugMode) {
            if (isWaterPresent()) {
                // up left (water refraction)
                {
                    Texture t = water.getRefractionFbo().getColorBufferTexture();
                    renderEngine2D.batch.draw(t, 0, (float) (Gdx.graphics.getHeight() - t.getHeight() / 4), (float) (t.getWidth() / 4), (float) (t.getHeight() / 4), 0, 0, t.getWidth(), t.getHeight(), false, true);
                }
                // up right (water refraction depth buffer)
                {
                    Texture t = water.getRefractionFbo().getTextureAttachments().get(1);
                    renderEngine2D.batch.draw(t, (float) (Gdx.graphics.getWidth() - t.getWidth() / 4), (float) (Gdx.graphics.getHeight() - t.getHeight() / 4), (float) (t.getWidth() / 4), (float) (t.getHeight() / 4), 0, 0, t.getWidth(), t.getHeight(), false, true);
                }
                // middle-up left (water reflection)
                {
                    Texture t = water.getReflectionFbo().getColorBufferTexture();
                    renderEngine2D.batch.draw(t, 0, (float) (Gdx.graphics.getHeight() - (t.getHeight() / 4) * 2), (float) (t.getWidth() / 4), (float) (t.getHeight() / 4), 0, 0, t.getWidth(), t.getHeight(), false, true);
                }
            }
            if (isMirrorPresent()) {
                // middle-up right (mirror reflection)
                {
                    Texture t = mirror.getReflectionFbo().getColorBufferTexture();
                    renderEngine2D.batch.draw(t, (float) (Gdx.graphics.getWidth() - t.getWidth() / 4), (float) (Gdx.graphics.getHeight() - (t.getHeight() / 4) * 2), (float) (t.getWidth() / 4), (float) (t.getHeight() / 4), 0, 0, t.getWidth(), t.getHeight(), false, true);
                }
            }
            if (isShadowEnabled()) {
                // lower left (shadow depth buffer)
                {

                    Texture t = shadowLight.getFrameBuffer().getColorBufferTexture();
                    renderEngine2D.batch.draw(t, 0, 0, t.getWidth() / 8, t.getHeight() / 8, 0, 0, t.getWidth(), t.getHeight(), false, true);
                }
            }
            // lower right
            {
//				Texture t = water.getRefractionFbo().getTextureAttachments().get(1);
//				batch2D.draw(t, Gdx.graphics.getWidth() - t.getWidth() / 4, 0, t.getWidth() / 4, t.getHeight() / 4, 0, 0, t.getWidth(), t.getHeight(), false, true);
            }
        }
        if (isShowGraphs()) {

            {
                Texture t = cpuGraph.getFbo().getColorBufferTexture();
                handleFrameBufferScreenshot(takeScreenShot, cpuGraph.getFbo(), "cpu.fbo");
                renderEngine2D.batch.draw(t, 0, 0, t.getWidth(), t.getHeight(), 0, 0, t.getWidth(), t.getHeight(), false, true);
            }
            {
                Texture t = gpuGraph.getFbo().getColorBufferTexture();
                handleFrameBufferScreenshot(takeScreenShot, gpuGraph.getFbo(), "gpu.fbo");
                renderEngine2D.batch.draw(t, 0, cpuGraph.getFbo().getHeight(), t.getWidth(), t.getHeight(), 0, 0, t.getWidth(), t.getHeight(), false, true);
            }
            {
                Texture t = fpsGraph.getFbo().getColorBufferTexture();
                handleFrameBufferScreenshot(takeScreenShot, fpsGraph.getFbo(), "fps.fbo");
                renderEngine2D.batch.draw(t, 0, fpsGraph.getFbo().getHeight() * 2, t.getWidth(), t.getHeight(), 0, 0, t.getWidth(), t.getHeight(), false, true);
            }
        }
        renderEngine2D.batch.end();

    }

    public void renderGraphs() {
//        if (context.isShowGraphs()) {
//            cpuGraph.update();
//            gpuGraph.update();
//        }
        if (isShowGraphs()) {
            cpuGraph.draw(renderEngine2D.batch);
            gpuGraph.draw(renderEngine2D.batch);
            fpsGraph.draw(renderEngine2D.batch);
        }
//		batch2D.setTransformMatrix(identityMatrix);// fix transformMatrix
    }

    /**
     * Render shadows only to interal frame buffers. (useful when you're using your own frame buffer to render scenes)
     */
    public void renderShadows(final boolean takeScreenShot) {
        final DirectionalLight light = shadowLight;
        if (light instanceof DirectionalShadowLight shadowLight) {
            shadowLight.begin();
            renderDepth(shadowLight.getCamera());
            handleFrameBufferScreenshot(takeScreenShot, shadowLight.getFrameBuffer(), "shadow.depth.buffer");
            shadowLight.end();
            environment.shadowMap = shadowLight;
        } else {
            environment.shadowMap = null;
        }
    }

    public void setAlwaysDay(final boolean alwaysDay) {
        this.alwaysDay = alwaysDay;
    }

    public void setAmbientLight(final float rLum, final float gLum, final float bLum) {
        ambientLight.color.set(rLum, gLum, bLum, 1f);
        environment.set(ambientLight);
    }

    public void setCamera(final Vector3 position, final Vector3 up, final Vector3 LookAt) throws Exception {
        camera.position.set(position);
        camera.up.set(up);
        camera.lookAt(LookAt);
        camera.update();
        // camController.notifyListener(camera);
    }

    public void setCameraTo(final float x, final float z, final boolean setDirty) throws Exception {
        camera.position.add(x - camera.lookat.x, 0, z - camera.lookat.z);
        camera.update();
        camera.setDirty(setDirty);// only set dirty if requested
        camera.lookat.x = x;
        camera.lookat.z = z;
        // camController.notifyListener(camera);
    }

    private void setCurrentDayTime(float currentDayTime) {
        this.currentDayTime = currentDayTime;
    }

    public void setDayAmbientLight(float r, float g, float b, float shadowIntensity) {
        dayAmbientIntensityR = r;
        dayAmbientIntensityG = g;
        dayAmbientIntensityB = b;
        dayShadowIntensity   = shadowIntensity;
    }

    public void setDaySkyBox(SceneSkybox daySkyBox) {
        this.daySkyBox = daySkyBox;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void setDynamicDayTime(boolean dynamicDayTime) {
        this.dynamicDayTime = dynamicDayTime;
    }

//    public void setDepthOfField(final boolean depthOfField) {
//        this.depthOfField = depthOfField;
//    }

    public void setEnableProfiling(boolean enableProfiling) {
        this.enableProfiling = enableProfiling;
    }

    public void setFixedDayTime(float fixedDayTime) {
        this.fixedDayTime = fixedDayTime;
    }

    public void setFixedShadowDirection(boolean fixedShadowDirection) {
        this.fixedShadowDirection = fixedShadowDirection;
    }

    public void setNightAmbientLight(float r, float g, float b, float shadowIntensity) {
        nightAmbientIntensityR = r;
        nightAmbientIntensityG = g;
        nightAmbientIntensityB = b;
        nightShadowIntensity   = shadowIntensity;
    }

    public void setNightSkyBox(SceneSkybox nightSkyBox) {
        this.nightSkyBox = nightSkyBox;
    }

    public void setPbr(boolean pbr) {
        this.pbr = pbr;
    }

    public void setReflectionClippingPlane(float distance) {
        reflectionClippingPlane.d = distance;
    }

    public void setRefractionClippingPlane(float distance) {
        refractionClippingPlane.d = distance;
    }

    public void setSceneBoxMax(Vector3 sceneBoxMax) {
        this.sceneBoxMax = sceneBoxMax;
        sceneBox.set(sceneBoxMin, sceneBoxMax);
    }

    public void setSceneBoxMin(Vector3 sceneBoxMin) {
        this.sceneBoxMin = sceneBoxMin;
        sceneBox.set(sceneBoxMin, sceneBoxMax);
    }

    public void setShadowEnabled(boolean shadowEnabled) {
        this.shadowEnabled = shadowEnabled;
    }

    public void setShadowLight(final float lum) {
        shadowLight.intensity = lum;
    }

    public void setShowGraphs(boolean enable) {
        context.setShowGraphs(enable);
    }

    public void setSkyBox(boolean skyBox) {
        this.skyBox = skyBox;
    }


    /**
     * should be called in order to perform light culling, skybox update and animations.
     *
     * @param delta
     */
    private void update(final float delta) {
        if (camera != null) {
            updateEnvironment();
            for (final RenderableProvider r : renderableProviders) {
                if (r instanceof Updatable) {
                    ((Updatable) r).update(camera, delta);
                }
            }
            if (daySkyBox != null && isDay()) daySkyBox.update(camera, delta);
            else if (nightSkyBox != null && isNight()) nightSkyBox.update(camera, delta);
        }
    }

    public void updateBlurEffect(int passes, float amount) {
//		effect1.setAmount(amount);
//		effect1.setPasses(passes);
    }

    public void updateCamera(final float centerXD, final float centerYD, final float centerZD) {
        if (centerXD != 0f || /*centerYD != 0f ||*/ centerZD != 0f)//TODO do not update if nothing has changed
        {
            Vector3 backup = new Vector3(camera.lookat);
            camera.translate(centerXD, 0, centerZD);
            camera.lookat.add(centerXD, 0, centerZD);
            camera.up.set(0, 1, 0);
//            camera.lookAt(backup/*camera.lookat*/);
//            camera.lookAt(camera.lookat);
            camera.update();
            camera.setDirty(true);
//            System.out.println("updateCamera");
        }
        if (testCase == 1) {
            if (camera.isDirty()) {
                Vector3 v1 = new Vector3(camera.position);
                Vector3 v2 = new Vector3(camera.position);
                sceneBox.set(v1.add(sceneBoxMin), v2.add(sceneBoxMax));
                shadowLight.setBounds(sceneBox);

                renderutils2Dxz.batch.setTransformMatrix(identityMatrix);
                renderutils2Dxz.batch.enableBlending();
                renderutils2Dxz.batch.setProjectionMatrix(camera.combined);
                final Matrix4 m = new Matrix4();
                m.translate(0, 0.2f, 0);
                m.rotate(xVector, -90);
                renderutils2Dxz.batch.setTransformMatrix(m);
            }
        }
        if (testCase == 2) {
//            if (camera2D.isDirty())
            {
                renderutils2Dxz.batch.setTransformMatrix(identityMatrix);
                renderutils2Dxz.batch.enableBlending();
                renderutils2Dxz.batch.setProjectionMatrix(camera2D.combined);
                final Matrix4 m       = new Matrix4();
                final Vector3 xVector = new Vector3(1, 0, 0);
                final Vector3 yVector = new Vector3(0, 1, 0);
                final Vector3 zVector = new Vector3(0, 0, 1);
                m.rotate(xVector, -90);
                renderutils2Dxz.batch.setTransformMatrix(m);
            }
        }
        if (testCase == 3) {
//            if (camera2D.isDirty())
            {
                renderEngine2D.batch.setTransformMatrix(identityMatrix);
                renderEngine2D.batch.enableBlending();
                renderEngine2D.batch.setProjectionMatrix(camera2D.combined);
                final Matrix4 m       = new Matrix4();
                final Vector3 xVector = new Vector3(1, 0, 0);
                final Vector3 yVector = new Vector3(0, 1, 0);
                final Vector3 zVector = new Vector3(0, 0, 1);
                m.rotate(xVector, -90);
                renderEngine2D.batch.setTransformMatrix(m);
            }
        }
    }

    private void updateDynamicModelInstanceCache() {

        {
            visibleDynamicGameObjectCount = 0;
            visibleDynamicGameObjects.clear();
            if (useDynamicCache) {
                if (render3D) dynamicCache.begin(camera);
                for (final GameObject<T> gameObject : dynamicGameObjects) {
                    if (isVisible(gameObject)) {
                        if (render3D) dynamicCache.add(gameObject.instance);
                        if (gameObject.objectRenderer != null) visibleDynamicGameObjects.add(gameObject);
                        visibleDynamicGameObjectCount++;
                        renderableProviders.add(gameObject.instance);
                    }
                }
                if (render3D) dynamicCache.end();
            } else {
                visibleDynamicModelInstances.clear();
                for (final GameObject<T> gameObject : dynamicGameObjects) {
                    if (isVisible(gameObject)) {
                        visibleDynamicGameObjectCount++;
                        renderableProviders.add(gameObject.instance);
                        visibleDynamicModelInstances.add(gameObject.instance);
                        if (gameObject.objectRenderer != null) visibleDynamicGameObjects.add(gameObject);
                    }
                }
            }
        }
    }

    private void updateEnvironment() {
        computedEnvironement.setCache(environment);
        pointLights.lights.clear();
        spotLights.lights.clear();
        if (environment != null) {
            for (final Attribute a : environment) {
                if (a instanceof PointLightsAttribute) {
                    pointLights.lights.addAll(((PointLightsAttribute) a).lights);
                    computedEnvironement.replaceCache(pointLights);//TODO enable light
                } else if (a instanceof SpotLightsAttribute) {
                    spotLights.lights.addAll(((SpotLightsAttribute) a).lights);
                    computedEnvironement.replaceCache(spotLights);//TODO enable light
                } else {
                    computedEnvironement.set(a);
                }
            }
        }
        cullLights();
    }

    public void updateEnvironment(final float timeOfDay) {
        if (gameEngine.updateEnvironment(timeOfDay)) return;
        if (Math.abs(this.timeOfDay - timeOfDay) > 0.01) {
            angle                  = (float) (Math.PI * (timeOfDay - 6) / 12);
            shadowLightDirection.x = (float) Math.cos(angle);
            shadowLightDirection.z = Math.abs((float) (Math.sin(angle)));
            shadowLightDirection.y = -Math.abs((float) Math.sin(angle));
            shadowLightDirection.nor();
            shadowLight.setDirection(shadowLightDirection);

            // day break
            if (!alwaysDay && timeOfDay > 5 && timeOfDay <= 6) {
                final float intensity = (timeOfDay - 5);
                final float r         = dayAmbientIntensityR * intensity;
                final float g         = dayAmbientIntensityG * intensity;
                final float b         = dayAmbientIntensityB * intensity;
                setShadowLight(dayShadowIntensity * intensity);
                setAmbientLight(r, g, b);
            }
            // day
            else if (isDay()) {
                final float intensity = 1.0f;
                final float r         = dayAmbientIntensityR;
                final float g         = dayAmbientIntensityG;
                final float b         = dayAmbientIntensityB;
                setShadowLight(dayShadowIntensity * intensity);
                setAmbientLight(r, g, b);
            }
            // sunset
            else if (timeOfDay > 18 && timeOfDay <= 19) {
                final float intensity = 1.0f - (timeOfDay - 18);
                final float r         = dayAmbientIntensityR * intensity;
                final float g         = dayAmbientIntensityG * intensity;
                final float b         = dayAmbientIntensityB * intensity;
                setShadowLight(dayShadowIntensity * intensity);
                setAmbientLight(r, g, b);
            }
            // night
            else if (isNight()) {
                // setShadowLight(0.01f);
                // setAmbientLight(0.0f, 0.0f, 0.0f);
                final float intensity = (float) Math.abs(Math.abs(Math.sin(angle)));
                final float r         = nightAmbientIntensityR * intensity;
                final float g         = nightAmbientIntensityG * intensity;
                final float b         = nightAmbientIntensityB * intensity;
                setShadowLight(nightShadowIntensity * intensity);
                setAmbientLight(r, g, b);
            }
            this.timeOfDay = timeOfDay;
        }
    }

    private void updateStaticModelInstanceCache() throws Exception {

        if (useStaticCache) {
            if (camera.isDirty()) {
                visibleStaticGameObjectCount = 0;
                visibleStaticModelInstances.clear();
                visibleStaticGameObjects.clear();
                if (render3D) staticCache.begin(camera);
                for (final GameObject<T> gameObject : staticGameObjects) {
                    if (isVisible(gameObject)) {
                        visibleStaticModelInstances.add(gameObject.instance);
                        //do we have 3D text to render?
                        if (gameObject.objectRenderer != null) visibleStaticGameObjects.add(gameObject);
                        if (render3D) staticCache.add(gameObject.instance);
                        visibleStaticGameObjectCount++;
                        renderableProviders.add(gameObject.instance);
                    }
                }
                for (final RenderableProvider renderableProvider : visibleStaticRenderableProviders) {
                    if (render3D) staticCache.add(renderableProvider);
                    visibleStaticGameObjectCount++;
                }
                if (render3D) staticCache.end();
                staticCacheDirty = false;
            }
            if (staticCacheDirty) {
                // there were visible instances added or removed
                visibleStaticGameObjectCount = 0;
                if (render3D) staticCache.begin(camera);
                for (final ModelInstance instance : visibleStaticModelInstances) {
                    if (render3D) staticCache.add(instance);
                    visibleStaticGameObjectCount++;
                    renderableProviders.add(instance);
                }
                for (final RenderableProvider renderableProvider : visibleStaticRenderableProviders) {
                    if (render3D) staticCache.add(renderableProvider);
                    visibleStaticGameObjectCount++;
                    // renderableProviders.add(renderableProvider);
                }

                if (render3D) staticCache.end();
                staticCacheDirty = false;
            }

        }
        // else {
        // if (staticCacheDirty || camera.isDirty()) {
        // visibleStaticGameObjectCount = 0;
        // for (GameObject instance : staticModelInstances) {
        // if (isVisible(instance)) {
        // visibleStaticModelInstances.add(instance.instance);
        // visibleStaticGameObjectCount++;
        // renderableProviders.add(instance.instance);
        // }
        // }
        // staticCacheDirty = false;
        // }
        //
        // }
    }

    private void writeFrameBufferToDisk(final String fileName, final FrameBuffer frameBuffer) {
        frameBuffer.bind();
        // final FrameBuffer frameBuffer = shadowLight.getFrameBuffer();
//		final Texture	texture				= frameBuffer.getColorBufferTexture();
        // TextureData textureData = texture.getTextureData();
        // if (!textureData.isPrepared()) {
        // textureData.prepare();
        // }
        // Pixmap pixmap = textureData.consumePixmap();
        final Pixmap frameBufferPixmap = Pixmap.createFromFrameBuffer(0, 0, frameBuffer.getWidth(), frameBuffer.getHeight());
        // final Pixmap frameBufferPixmap = ScreenUtils.getFrameBufferPixmap(0, 0,
        // frameBuffer.getWidth(), frameBuffer.getHeight());
        PixmapIO.writePNG(Gdx.files.local(fileName), frameBufferPixmap, Deflater.DEFAULT_COMPRESSION, true);
        FrameBuffer.unbind();
    }

}
