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
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g3d.Renderable;
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
import com.scottlogic.util.GL32CMacIssueHandler;
import com.scottlogic.util.ShaderCompatibilityHelper;
import de.bushnaq.abdalla.engine.camera.MovingCamera;
import de.bushnaq.abdalla.engine.shader.DepthOfFieldEffect;
import de.bushnaq.abdalla.engine.shader.GamePbrShaderProvider;
import de.bushnaq.abdalla.engine.shader.GameShaderProvider;
import de.bushnaq.abdalla.engine.shader.GameShaderProviderInterface;
import de.bushnaq.abdalla.engine.shader.mirror.Mirror;
import de.bushnaq.abdalla.engine.shader.water.Water;
import de.bushnaq.abdalla.engine.util.logger.Logger;
import de.bushnaq.abdalla.engine.util.logger.LoggerFactory;
import net.mgsx.gltf.scene3d.attributes.FogAttribute;
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
public class RenderEngine3D<T> {
    private static final float                       DAY_AMBIENT_INTENSITY_B          = 1f;
    private static final float                       DAY_AMBIENT_INTENSITY_G          = 1f;
    private static final float                       DAY_AMBIENT_INTENSITY_R          = 1f;
    private static final float                       DAY_SHADOW_INTENSITY             = 5.0f;
    private static final float                       NIGHT_AMBIENT_INTENSITY_B        = 0.2f;
    private static final float                       NIGHT_AMBIENT_INTENSITY_G        = 0.2f;
    private static final float                       NIGHT_AMBIENT_INTENSITY_R        = 0.2f;
    private static final float                       NIGHT_SHADOW_INTENSITY           = 0.2f;
    public final         Array<GameObject<T>>        staticModelInstances             = new Array<>();
    private final        Camera                      camera2D;
    private final        EnvironmentCache            computedEnvironement             = new EnvironmentCache();
    private final        IContext                    context;
    private final        ModelCache                  dynamicCache                     = new ModelCache();
    private final        Set<ObjectRenderer<T>>      dynamicText3DList                = new HashSet<>();
    private final        Fog                         fog                              = new Fog(Color.BLACK, 15f, 30f, 0.5f);
    private final        BitmapFont                  font;
    private final        T                           gameEngine;
    private final        Matrix4                     identityMatrix                   = new Matrix4();
    //    private              GameObject                  lookatCube;
    private final        Mirror                      mirror                           = new Mirror();
    private final        PointLightsAttribute        pointLights                      = new PointLightsAttribute();
    private final        Vector3                     position                         = new Vector3();
    // private final Ray ray = new Ray(new Vector3(), new Vector3());
    private final        Plane                       reflectionClippingPlane          = new Plane(new Vector3(0f, 1f, 0f), 0.1f);                                // render everything above the
    private final        Plane                       refractionClippingPlane          = new Plane(new Vector3(0f, -1f, 0f), (-0.1f));                            // render everything below the
    private final        Array<ModelInstance>        renderableProviders              = new Array<>();
    //    private final        Vector3                     shadowLightDirection             = new Vector3();
    private final        int                         speed                            = 5;                                                                    // speed of time
    private final        SpotLightsAttribute         spotLights                       = new SpotLightsAttribute();
    private final        ModelCache                  staticCache                      = new ModelCache();
    private final        Set<ObjectRenderer<T>>      staticText3DList                 = new HashSet<>();
    private final        Set<Text2D>                 text2DList                       = new HashSet<>();
    private final        boolean                     useDynamicCache                  = false;
    private final        boolean                     useStaticCache                   = true;
    private final        Array<ModelInstance>        visibleDynamicModelInstances     = new Array<>();
    private final        Array<ModelInstance>        visibleStaticModelInstances      = new Array<>();
    private final        Array<RenderableProvider>   visibleStaticRenderableProviders = new Array<>();
    private final        Water                       water                            = new Water();
    public               PolygonSpriteBatch          batch2D;
    public               TimeGraph                   cpuGraph;
    public               Array<GameObject<T>>        dynamicModelInstances            = new Array<>();
    public               Environment                 environment                      = new Environment();
    public               GameShaderProviderInterface gameShaderProvider;
    public               TimeGraph                   gpuGraph;
    public               SceneSkybox                 nightSkyBox;
    public               Model                       rayCube;
    public               int                         visibleDynamicGameObjectCount    = 0;
    public               int                         visibleDynamicLightCount         = 0;
    public               int                         visibleStaticGameObjectCount     = 0;
    public               int                         visibleStaticLightCount          = 0;
    private              boolean                     alwaysDay                        = true;
    private              ColorAttribute              ambientLight;
    private              float                       angle;
    private              AtlasRegion                 atlasRegion;
    private              ModelBatch                  batch;
    private              MovingCamera                camera;
    //    private              GameObject                  cameraCube;
    private              float                       currentDayTime;
    private              SceneSkybox                 daySkyBox;
    private              boolean                     debugMode                        = false;
    private              ModelBatch                  depthBatch;
    // GaussianBlurEffect effect1;
//	BloomEffect								effect2;
    private              boolean                     depthOfField                     = true;
    private              DepthOfFieldEffect          depthOfFieldEffect;
    //    private              GameObject                  depthOfFieldMeter;
    private              boolean                     dynamicDayTime                   = false;
    private              float                       fixedDayTime                     = 10;
    //    private              InputProcessor              inputProcessor;
    private              Logger                      logger                           = LoggerFactory.getLogger(this.getClass());
    //    private              float                       northDirectionDegree             = 90;
    private              boolean                     pbr;
    private              FrameBuffer                 postFbo;
    private              RenderableSorter            renderableSorter;
    private              Vector3                     sceneBoxMax                      = new Vector3(2000, 2000, 1000);
    //    public final BoundingBox sceneBox = new BoundingBox(new Vector3(-20, -50, -30), new Vector3(20, 20, 2));
    private              Vector3                     sceneBoxMin                      = new Vector3(-2000, -2000, -1000);
    private final        BoundingBox                 sceneBox                         = new BoundingBox(sceneBoxMin, sceneBoxMax);
    private              boolean                     shadowEnabled                    = true;
    private              DirectionalShadowLight      shadowLight                      = null;
    // OrthographicCamera debugCamera;
    private              boolean                     skyBox                           = false;
    private              Stage                       stage;
    private              boolean                     staticCacheDirty                 = true;
    private              int                         staticCacheDirtyCount            = 0;
    private              float                       timeOfDay                        = 8;                                                                    // 24h time
    private              VfxManager                  vfxManager                       = null;

    public RenderEngine3D(final IContext context, T gameEngine, final InputProcessor inputProcessor, MovingCamera camera, Camera camera2D, BitmapFont font, AtlasRegion atlasRegion) throws Exception {
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
        logger.info(String.format("width = %d height = %d", Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        this.context    = context;
        this.gameEngine = gameEngine;
//        this.inputProcessor = inputProcessor;
        this.camera      = camera;
        this.camera2D    = camera2D;
        this.font        = font;
        this.atlasRegion = atlasRegion;
        create();
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
        dynamicModelInstances.add(instance);
    }

    public void addStatic(ObjectRenderer<T> renderer) {
        staticText3DList.add(renderer);
    }

    public void addStatic(final GameObject<T> instance) {
        staticModelInstances.add(instance);
        if (isVisible(instance)) {
            staticCacheDirty = true;
            staticCacheDirtyCount++;
            visibleStaticModelInstances.add(instance.instance);
        }
    }

    public void addStatic(RenderableProvider renderableProvider) {
        visibleStaticRenderableProviders.add(renderableProvider);
        staticCacheDirty = true;
        staticCacheDirtyCount++;
    }

    public void clearViewport() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);// modelBatch will change this state anyway, so better enable it when you
        // need it
        Gdx.gl.glClearColor(getFog().getColor().r, getFog().getColor().g, getFog().getColor().b, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    public void create() throws Exception {
        pbr       = context.getPbrModeProperty();
        debugMode = context.getDebugModeProperty();
        createFrameBuffer();
        createShader();
        createEnvironment();
        createStage();
//		createRayCube();
//		vfxManager = new VfxManager(Pixmap.Format.RGBA8888);
//		vfxManager.addEffect(new DepthOfFieldEffect(postFbo, camera, 1));
//		vfxManager.addEffect(new DepthOfFieldEffect(postFbo, camera, 0));
//		createBlurEffect();
//		createBloomEffect();
//		vfxManager.addEffect(new FxaaEffect());
//		vfxManager.addEffect(new FilmGrainEffect());
//		vfxManager.addEffect(new OldTvEffect());
        vfxManager         = new VfxManager(Pixmap.Format.RGBA8888);
        depthOfFieldEffect = new DepthOfFieldEffect(postFbo, camera);
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
        final Vector3 position = new Vector3(0, 0, 0);
        final Vector3 xVector  = new Vector3(1, 0, 0);
        final Vector3 yVector  = new Vector3(0, 1, 0);
        final Vector3 zVector  = new Vector3(0, 0, 1);
        final Ray     rayX     = new Ray(position, xVector);
        final Ray     rayY     = new Ray(position, yVector);
        final Ray     rayZ     = new Ray(position, zVector);
        createRay(rayX, null);
        createRay(rayY, null);
        createRay(rayZ, null);
    }

    private void createEnvironment() {
        // shadow
        int shadowMapSize = context.getShadowMapSizeProperty();
        shadowLight = new DirectionalShadowLight(4048 * 2, 4048 * 2);
//                new DirectionalShadowLight(shadowMapSize, shadowMapSize, GameSettings.SHADOW_VIEWPORT_WIDTH, GameSettings.SHADOW_VIEWPORT_HEIGHT, GameSettings.SHADOW_NEAR, GameSettings.SHADOW_FAR);
        final Matrix4 m = new Matrix4();
        sceneBox.mul(m);
        shadowLight.setBounds(sceneBox);
        shadowLight.direction.set(-.5f, -.7f, .5f).nor();
        shadowLight.color.set(Color.WHITE);
        shadowLight.intensity = 1.0f;
        environment.add(shadowLight);
//		csm = new CascadeShadowMap(3);
//		csm.setCascades(camera,shadowLight, 0f, 0.5f);

        // setup IBL (image based lighting)
//		if (isPbr()) {
////			setupImageBasedLightingByFaceNames("ruins", "jpg", "png", "jpg", 10);
//			setupImageBasedLightingByFaceNames("clouds", "jpg", "jpg", "jpg", 10);
////			setupImageBasedLightingByFaceNames("moonless_golf_2k", "jpg", "jpg", "jpg", 10);
//			// setup skybox
//			daySkyBox = new SceneSkybox(environmentDayCubemap);
//			nightSkyBox = new SceneSkybox(environmentNightCubemap);
//			environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
//			environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
//			environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
//			environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 0f));
//		} else {
//		}
        final float lum = 0.0f;
        ambientLight = new ColorAttribute(ColorAttribute.AmbientLight, lum, lum, lum, 1.0f);
        environment.set(ambientLight);
        environment.set(new ColorAttribute(ColorAttribute.Fog, getFog().getColor()));
        environment.set(new FogAttribute(FogAttribute.FogEquation));
        getFog().setFogEquation(environment);
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
        }
    }

    private void createGraphs() {
        cpuGraph = new TimeGraph(new Color(1f, 0f, 0f, 1f), new Color(1f, 0, 0, 0.6f), Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 4, font, atlasRegion);
        gpuGraph = new TimeGraph(new Color(0f, 1f, 0f, 1f), new Color(0f, 1f, 0f, 0.6f), Gdx.graphics.getWidth(), Gdx.graphics.getHeight() / 4, font, atlasRegion);
    }

    private GameObject<T> createRay(final Ray ray, Float length) {
        if (length == null) length = 10000f;
//		final float			length		= 10000f;
        final Vector3       direction = new Vector3(ray.direction.x, ray.direction.y, ray.direction.z);
        final Vector3       position  = ray.origin.cpy();
        final GameObject<T> instance  = new GameObject<T>(new ModelInstanceHack(rayCube), null);
        instance.instance.materials.get(0).set(ColorAttribute.createDiffuse(Color.RED));
        addDynamic(instance);
        final Vector3 xVector = new Vector3(1, 0, 0);
        direction.nor();
        position.x += direction.x * length / 2;
        position.y += direction.y * length / 2;
        position.z += direction.z * length / 2;
        instance.instance.transform.setToTranslation(position);
        instance.instance.transform.rotate(xVector, direction);
        instance.instance.transform.scale(length, 0.5f, 0.5f);
        instance.update();
        return instance;
        // System.out.println("created ray");
    }

    private void createRayCube() {
        if (isPbr()) {
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
        renderableSorter = new SceneRenderableSorter();
        if (isPbr()) {
            depthBatch = new ModelBatch(PBRShaderProvider.createDefaultDepth(0));
        } else {
            depthBatch = new ModelBatch(new DepthShaderProvider());
        }
        // oceanBatch = new ModelBatch(new OceanShaderProvider());
        batch = new ModelBatch(createShaderProvider(), renderableSorter);
        // batch = new ModelBatch(PBRShaderProvider.createDefaultDepth(0));
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
        stage = new Stage(new ScreenViewport(), batch2D);
    }

    private void cullLights() {
        visibleDynamicLightCount = 0;
        final PointLightsAttribute pla = environment.get(PointLightsAttribute.class, PointLightsAttribute.Type);
        if (pla != null) {
            for (final PointLight light : pla.lights) {
                if (light instanceof PointLightEx) {
                    final PointLightEx l = (PointLightEx) light;
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
                if (light instanceof SpotLightEx) {
                    final SpotLightEx l = (SpotLightEx) light;
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
        staticCache.dispose();
        dynamicCache.dispose();
        vfxManager.dispose();
        depthOfFieldEffect.dispose();
        disposeGraphs();
        disposeStage();
        disposeEnvironment();
        disposeShader();
        disposeFrameBuffer();
    }

    private void disposeEnvironment() {
        if (isPbr()) {
            nightSkyBox.dispose();
            daySkyBox.dispose();
        }
        shadowLight.dispose();
        environment.clear();
    }

    private void disposeFrameBuffer() {
        postFbo.dispose();
        mirror.dispose();
        water.dispose();
    }

    private void disposeGraphs() {
        gpuGraph.dispose();
        cpuGraph.dispose();
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

    private void disposeShader() {
        gameShaderProvider.dispose();
        batch2D.dispose();
        batch.dispose();
        depthBatch.dispose();
    }

    private void disposeStage() {
        stage.dispose();
    }

    public void end() {
    }

    public MovingCamera getCamera() {
        return camera;
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

    public float getCurrentDayTime() {
        return currentDayTime;
    }

    public DepthOfFieldEffect getDepthOfFieldEffect() {
        return depthOfFieldEffect;
    }

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
//		createRay(ray);
        GameObject<T> result   = null;
        float         distance = -1;
        for (int i = 0; i < dynamicModelInstances.size; ++i) {
            final GameObject<T> instance = dynamicModelInstances.get(i);
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
        for (int i = 0; i < staticModelInstances.size; ++i) {
            final GameObject<T> instance = staticModelInstances.get(i);
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

    public boolean isDepthOfField() {
        return depthOfField;
    }

    public boolean isDynamicDayTime() {
        return dynamicDayTime;
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

    private boolean isShowGraphs() {
        return context.getShowGraphsProperty();
    }

    private boolean isSkyBox() {
        return skyBox;
    }

    private boolean isVisible(final GameObject<T> gameObject) {
        return camera.frustum.boundsInFrustum(gameObject.transformedBoundingBox);
    }

    public boolean isWaterPresent() {
        return water.isPresent() /* && isPbr() */;
    }

    public void postProcessRender() throws Exception {
        if (isDepthOfField()) {
            // Clean up the screen.
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            // Clean up internal buffers, as we don't need any information from the last render.
            vfxManager.cleanUpBuffers();
            vfxManager.applyEffects();
            // Render result to the screen.
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
        dynamicModelInstances.clear();
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

    public boolean removeDynamic(final GameObject<T> instance) {
        return dynamicModelInstances.removeValue(instance, true);
    }

    public void removeStatic(ObjectRenderer<T> renderer) {
        staticText3DList.remove(renderer);
    }

    public boolean removeStatic(final GameObject<T> instance) {
        final boolean result = staticModelInstances.removeValue(instance, true);
        if (isVisible(instance)) {
            staticCacheDirty = true;
            staticCacheDirtyCount++;
            visibleStaticModelInstances.removeValue(instance.instance, true);
        }
        return result;
    }

    public void render(final long currentTime, final float deltaTime, final boolean takeScreenShot) throws Exception {
        float x1 = shadowLight.getCamera().position.x;
        if (isDynamicDayTime()) {
            // keep only what is smaller than 10000
            currentDayTime = currentTime - ((currentTime / (50000L / speed * 24)) * (50000L / speed * 24));
            currentDayTime /= 50000 / speed;
        } else {
            setCurrentDayTime(getFixedDayTime());
        }
        updateEnvironment(getCurrentDayTime());
//        float depth = Math.max(sceneBox.getWidth(), Math.max(sceneBox.getHeight(), sceneBox.getDepth()));
//		csm.setCascades(camera, shadowLight, depth, 4f);
        renderableProviders.clear();
        updateDynamicModelInstanceCache();
        updateStaticModelInstanceCache();

        getFog().updateFog(environment);
        update(deltaTime);
        if (isShadowEnabled()) {
            renderShadows(takeScreenShot);
        }
        if (isPbr()) {
            PBRCommon.enableSeamlessCubemaps();
        }
        computedEnvironement.shadowMap = environment.shadowMap;
        // handleFrameBufferScreenshot(takeScreenShot);

        // FBO
        if (isWaterPresent()) {
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
        if (isMirrorPresent()) {
            // waterReflectionFbo
            context.enableClipping();
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
        if (isDepthOfField())
            postFbo.begin();
//		createCameraCube();
//		createLookatCube();
//		createDepthOfFieldMeter();
        renderColors(takeScreenShot);
//        render2DText();
        render3DText();
        if (isDepthOfField())
            postFbo.end();

        camera.setDirty(false);
        staticCacheDirtyCount = 0;
        renderGraphs();

        if (isDepthOfField())
            postFbo.begin();
        renderFbos(takeScreenShot);
        if (isDepthOfField())
            postFbo.end();

//        fboToScreen();
    }

    private void render2DText() {
        batch2D.setProjectionMatrix(stage.getViewport().getCamera().combined);
        batch2D.begin();
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        batch2D.enableBlending();
        for (Text2D text2d : text2DList) {
            text2d.draw(batch2D);
        }
        batch2D.end();
    }

    private void render3DText() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        batch2D.begin();
        batch2D.enableBlending();
        batch2D.setProjectionMatrix(camera.combined);
        for (final ObjectRenderer<T> renderer : staticText3DList) {
            renderer.renderText(this, 0, false);
        }
        for (final ObjectRenderer<T> renderer : dynamicText3DList) {
            renderer.renderText(this, 0, false);
        }
        Array<com.badlogic.gdx.graphics.g3d.Renderable> renderables = new Array<Renderable>();
        staticCache.getRenderables(renderables, null);


        for (GameObject<T> gameObject : staticModelInstances) {
            if (isVisible(gameObject)) {
                if (gameObject.objectRenderer != null) gameObject.objectRenderer.renderText(this, 0, false);
            }
        }
        for (GameObject<T> gameObject : dynamicModelInstances) {
            if (isVisible(gameObject)) {
                if (gameObject.objectRenderer != null) gameObject.objectRenderer.renderText(this, 0, false);
            }
        }

//		for (final Stone stone : context.stoneList) {
//			stone.get3DRenderer().renderText(this, 0, false);
//		}
//		for (final Digit digit : context.digitList) {
//			digit.get3DRenderer().renderText(this, 0, false);
//		}
        batch2D.end();
        batch2D.setTransformMatrix(identityMatrix);// fix transformMatrix
    }

    /**
     * Render colors only. You should call {@link #renderShadows(boolean takeScreenShot)} before. (useful when you're using your own frame buffer to render scenes)
     */
    private void renderColors(final boolean takeScreenShot) {
        clearViewport();

        batch.begin(camera);
        if (useStaticCache) batch.render(staticCache, computedEnvironement);
//         else
//         batch.render(visibleStaticModelInstances, computedEnvironement);
        if (useDynamicCache) batch.render(dynamicCache, computedEnvironement);
        else batch.render(visibleDynamicModelInstances, computedEnvironement);
//         batch.render(ocean.instance, oceanShader);
        if (isSkyBox()) {
            if (daySkyBox != null && isDay()) batch.render(daySkyBox);
            else if (nightSkyBox != null && isNight()) batch.render(nightSkyBox);
        }
        batch.end();
    }

    /**
     * Render only depth (packed 32 bits), usefull for post processing effects. You typically render it to a FBO with depth enabled.
     */
    // private void renderDepth() {
    // renderDepth(camera);
    // }
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
        batch2D.begin();
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        batch2D.enableBlending();
        batch2D.setProjectionMatrix(stage.getViewport().getCamera().combined);
        if (debugMode) {
            if (isWaterPresent()) {
                // up left (water refraction)
                {
                    Texture t = water.getRefractionFbo().getColorBufferTexture();
                    batch2D.draw(t, 0, Gdx.graphics.getHeight() - t.getHeight() / 4, t.getWidth() / 4, t.getHeight() / 4, 0, 0, t.getWidth(), t.getHeight(), false, true);
                }
                // up right (water refraction depth buffer)
                {
                    Texture t = water.getRefractionFbo().getTextureAttachments().get(1);
                    batch2D.draw(t, Gdx.graphics.getWidth() - t.getWidth() / 4, Gdx.graphics.getHeight() - t.getHeight() / 4, t.getWidth() / 4, t.getHeight() / 4, 0, 0, t.getWidth(), t.getHeight(), false, true);
                }
                // middle-up left (water reflection)
                {
                    Texture t = water.getReflectionFbo().getColorBufferTexture();
                    batch2D.draw(t, 0, Gdx.graphics.getHeight() - (t.getHeight() / 4) * 2, t.getWidth() / 4, t.getHeight() / 4, 0, 0, t.getWidth(), t.getHeight(), false, true);
                }
            }
            if (isMirrorPresent()) {
                // middle-up right (mirror reflection)
                {
                    Texture t = mirror.getReflectionFbo().getColorBufferTexture();
                    batch2D.draw(t, Gdx.graphics.getWidth() - t.getWidth() / 4, Gdx.graphics.getHeight() - (t.getHeight() / 4) * 2, t.getWidth() / 4, t.getHeight() / 4, 0, 0, t.getWidth(), t.getHeight(), false, true);
                }
            }
            if (isShadowEnabled()) {
                // lower left (shadow depth buffer)
                {

                    Texture t = shadowLight.getFrameBuffer().getColorBufferTexture();
                    batch2D.draw(t, 0, 0, t.getWidth() / 8, t.getHeight() / 8, 0, 0, t.getWidth(), t.getHeight(), false, true);
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
                batch2D.draw(t, 0, 0, t.getWidth(), t.getHeight(), 0, 0, t.getWidth(), t.getHeight(), false, true);
            }
            {
                Texture t = gpuGraph.getFbo().getColorBufferTexture();
                handleFrameBufferScreenshot(takeScreenShot, gpuGraph.getFbo(), "gpu.fbo");
                batch2D.draw(t, 0, cpuGraph.getFbo().getHeight(), t.getWidth(), t.getHeight(), 0, 0, t.getWidth(), t.getHeight(), false, true);
            }
        }
        batch2D.end();

    }

    public void renderGraphs() {
        if (context.isShowGraphs()) {
            cpuGraph.update();
            gpuGraph.update();
        }
        if (isShowGraphs()) {
            cpuGraph.draw(batch2D);
            gpuGraph.draw(batch2D);
        }
//		batch2D.setTransformMatrix(identityMatrix);// fix transformMatrix
    }

    /**
     * Render shadows only to interal frame buffers. (useful when you're using your own frame buffer to render scenes)
     */
    public void renderShadows(final boolean takeScreenShot) {
        final DirectionalLight light = shadowLight;
        if (light instanceof DirectionalShadowLight) {
            final DirectionalShadowLight shadowLight = (DirectionalShadowLight) light;
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

    private void setAmbientLight(final float rLum, final float gLum, final float bLum) {
        ambientLight.color.set(rLum, gLum, bLum, 1f);
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

    public void setDaySkyBox(SceneSkybox daySkyBox) {
        this.daySkyBox = daySkyBox;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void setDepthOfField(final boolean depthOfField) {
        this.depthOfField = depthOfField;
    }

    public void setDynamicDayTime(boolean dynamicDayTime) {
        this.dynamicDayTime = dynamicDayTime;
    }

    public void setFixedDayTime(float fixedDayTime) {
        this.fixedDayTime = fixedDayTime;
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

//	private void updateFog() {
//		if (fogEquation != null) {
//			// fogEquation.x is where the fog begins
//			// .y should be where it reaches 100%
//			// then z is how quickly it falls off
//			// fogEquation.value.set(MathUtils.lerp(sceneManager.camera.near,
//			// sceneManager.camera.far, (FOG_X + 1f) / 2f),
//			// MathUtils.lerp(sceneManager.camera.near, sceneManager.camera.far, (FAG_Y +
//			// 1f) / 2f),
//			// 1000f * (FOG_Z + 1f) / 2f);
//
//			fogEquation.value.set(fogMinDistance, fogMaxDistance, fogMixValue);
//		}
//	}

    public void setSceneBoxMin(Vector3 sceneBoxMin) {
        this.sceneBoxMin = sceneBoxMin;
        sceneBox.set(sceneBoxMin, sceneBoxMax);
    }

    public void setShadowEnabled(boolean shadowEnabled) {
        this.shadowEnabled = shadowEnabled;
    }

//	private void setShowGraphs(boolean showGraphs) {
//		this.showGraphs = showGraphs;
//	}

    private void setShadowLight(final float lum) {
        shadowLight.intensity = lum;
//		shadowLight.set(GameSettings.SHADOW_INTENSITY, GameSettings.SHADOW_INTENSITY, GameSettings.SHADOW_INTENSITY,shadowLightDirection.nor());
//        shadowLight.set(lum, lum, lum, shadowLightDirection.nor());
    }

    public void setSkyBox(boolean skyBox) {
        this.skyBox = skyBox;
    }

//	public void toggleShowGraphs() {
//		setShowGraphs(!isShowGraphs());
//	}

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
//        if (centerXD != 0f || centerYD != 0f || centerZD != 0f)
        {
            camera.translate(centerXD, centerYD, centerZD);
            camera.lookat.add(centerXD, centerYD, centerZD);
            camera.lookAt(camera.lookat);
            camera.update();
            Vector3 v1 = new Vector3(camera.position);
            Vector3 v2 = new Vector3(camera.position);
//        sceneBox.set(v1.add(-2000-300,-2000-500,-2000-400),v2.add(2000-300,2000-500,2000-400));
            sceneBox.set(v1.add(sceneBoxMin), v2.add(sceneBoxMax));
            shadowLight.setBounds(sceneBox);
            shadowLight.direction.set(-.5f, -.7f, .5f).nor();
            camera.setDirty(true);

//        shadowLight.getCamera().translate(centerXD, centerYD, centerZD);
//        shadowLight.setCenter(camera.position);
//        if (centerXD != 0f)
//            if (shadowLight.getCamera().position.x != x + centerXD)
//                logger.info("");
//        shadowLight.getCamera().update();
        }
    }

    private void updateDynamicModelInstanceCache() {

        {
            visibleDynamicGameObjectCount = 0;
            if (useDynamicCache) {
                dynamicCache.begin(camera);
                for (final GameObject<T> instance : dynamicModelInstances) {
                    if (isVisible(instance)) {
                        dynamicCache.add(instance.instance);
                        visibleDynamicGameObjectCount++;
                        renderableProviders.add(instance.instance);
                    }
                }
                dynamicCache.end();
            } else {
                visibleDynamicModelInstances.clear();
                for (final GameObject<T> instance : dynamicModelInstances) {
                    if (isVisible(instance)) {
                        visibleDynamicGameObjectCount++;
                        renderableProviders.add(instance.instance);
                        visibleDynamicModelInstances.add(instance.instance);
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
                    computedEnvironement.replaceCache(pointLights);
                } else if (a instanceof SpotLightsAttribute) {
                    spotLights.lights.addAll(((SpotLightsAttribute) a).lights);
                    computedEnvironement.replaceCache(spotLights);
                } else {
                    computedEnvironement.set(a);
                }
            }
        }
        cullLights();
    }
//	private CascadeShadowMap csm;

    public void updateEnvironment(final float timeOfDay) {
        if (Math.abs(this.timeOfDay - timeOfDay) > 0.01) {
//            angle = (float) (Math.PI * (timeOfDay - 6) / 12);
//            shadowLightDirection.x = (float) Math.cos(angle);
//            shadowLightDirection.z = Math.abs((float) (Math.sin(angle)));
//            shadowLightDirection.y = -Math.abs((float) Math.sin(angle));
//            shadowLightDirection.nor();
//            shadowLight.setDirection(shadowLightDirection);
//            shadowLight.setCenter(0f,-14f,-15);

            // day break
            if (!alwaysDay && timeOfDay > 5 && timeOfDay <= 6) {
                final float intensity = (timeOfDay - 5);
                final float r         = DAY_AMBIENT_INTENSITY_R * intensity;
                final float g         = DAY_AMBIENT_INTENSITY_G * intensity;
                final float b         = DAY_AMBIENT_INTENSITY_B * intensity;
                setShadowLight(DAY_SHADOW_INTENSITY * intensity);
                setAmbientLight(r, g, b);
            }
            // day
            else if (isDay()) {
                final float intensity = 1.0f;
                final float r         = DAY_AMBIENT_INTENSITY_R;
                final float g         = DAY_AMBIENT_INTENSITY_G;
                final float b         = DAY_AMBIENT_INTENSITY_B;
                setShadowLight(DAY_SHADOW_INTENSITY * intensity);
                setAmbientLight(r, g, b);
            }
            // sunset
            else if (timeOfDay > 18 && timeOfDay <= 19) {
                final float intensity = 1.0f - (timeOfDay - 18);
                final float r         = DAY_AMBIENT_INTENSITY_R * intensity;
                final float g         = DAY_AMBIENT_INTENSITY_G * intensity;
                final float b         = DAY_AMBIENT_INTENSITY_B * intensity;
                setShadowLight(DAY_SHADOW_INTENSITY * intensity);
                setAmbientLight(r, g, b);
            }
            // night
            else if (isNight()) {
                // setShadowLight(0.01f);
                // setAmbientLight(0.0f, 0.0f, 0.0f);
                final float intensity = (float) Math.abs(Math.abs(Math.sin(angle)));
                final float r         = NIGHT_AMBIENT_INTENSITY_R * intensity;
                final float g         = NIGHT_AMBIENT_INTENSITY_G * intensity;
                final float b         = NIGHT_AMBIENT_INTENSITY_B * intensity;
                setShadowLight(NIGHT_SHADOW_INTENSITY * intensity);
                setAmbientLight(r, g, b);
            }
            this.timeOfDay = timeOfDay;
        }
    }

    private void updateStaticModelInstanceCache() throws Exception {

        if (useStaticCache) {
            if (staticCacheDirty) {
                // there where visible instances added or removed
                visibleStaticGameObjectCount = 0;
                staticCache.begin(camera);
                for (final ModelInstance instance : visibleStaticModelInstances) {
                    staticCache.add(instance);
                    visibleStaticGameObjectCount++;
                    renderableProviders.add(instance);
                }
                for (final RenderableProvider renderableProvider : visibleStaticRenderableProviders) {
                    staticCache.add(renderableProvider);
                    visibleStaticGameObjectCount++;
                    // renderableProviders.add(renderableProvider);
                }

                staticCache.end();
                staticCacheDirty = false;
            }
            if (camera.isDirty()) {
                // audioEngine.setListenerPosition(camera.position);
                visibleStaticGameObjectCount = 0;
                visibleStaticModelInstances.clear();
                staticCache.begin(camera);
                for (final GameObject<T> instance : staticModelInstances) {
                    if (isVisible(instance)) {
                        visibleStaticModelInstances.add(instance.instance);
                        staticCache.add(instance.instance);
                        visibleStaticGameObjectCount++;
                        renderableProviders.add(instance.instance);
                    }
                }
                for (final RenderableProvider renderableProvider : visibleStaticRenderableProviders) {
                    staticCache.add(renderableProvider);
                    visibleStaticGameObjectCount++;
                    // renderableProviders.add(renderableProvider);
                }
                staticCache.end();
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
