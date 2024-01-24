package de.bushnaq.abdalla.engine.shader.water;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Plane;

/**
 * @author kunterbunt
 *
 */
public class WaterShader extends DefaultShader {
	private static Plane		clippingPlane;
	private static final String	DUDV_MAP_FILE_NAME			= "shader/texture/waterDUDV.png";
	private static final String	NORMAL_MAP_FILE_NAME		= "shader/texture/normal.png";
	private float				moveFactor					= 0f;
	private final Texture		normalMap;
	private final int			u_clippingPlane				= register("u_clippingPlane");
	private final int			u_depthMap					= register("u_depthMap");
	private final int			u_dudvMapTexture			= register("u_dudvMapTexture");
	private final int			u_moveFactor				= register("u_moveFactor");
	private final int			u_normalMap					= register("u_normalMap");
	private final int			u_reflectionTexture			= register("u_reflectionTexture");
	private final int			u_refractionTexture			= register("u_refractionTexture");
	private final int			u_refractiveMultiplicator	= register("u_refractiveMultiplicator");
	private final int			u_tiling					= register("u_tiling");
	private final int			u_waveStrength				= register("u_waveStrength");
	private Water				water;
	private final Texture		waterDuDv;

	public WaterShader(final Renderable renderable, final Config config, final String prefix, final Water water) {
		super(renderable, config, prefix);
		this.water = water;
		// GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, 4.0f);
		waterDuDv = new Texture(Gdx.files.internal(DUDV_MAP_FILE_NAME));
		waterDuDv.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		normalMap = new Texture(Gdx.files.internal(NORMAL_MAP_FILE_NAME));
		normalMap.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
	}

	@Override
	public void begin(final Camera camera, final RenderContext context) {
		super.begin(camera, context);
		if (clippingPlane != null)
			set(u_clippingPlane, clippingPlane.normal.x, clippingPlane.normal.y, clippingPlane.normal.z, clippingPlane.d);
		set(u_refractionTexture, water.getRefractionFbo().getColorBufferTexture());
		set(u_reflectionTexture, water.getReflectionFbo().getColorBufferTexture());
		if (Gdx.app.getType() != ApplicationType.iOS) {
			set(u_depthMap, water.getRefractionFbo().getTextureAttachments().get(1));
		}
		set(u_dudvMapTexture, waterDuDv);
		set(u_normalMap, normalMap);
		set(u_tiling, water.getTiling());
		set(u_refractiveMultiplicator, water.getRefractiveMultiplicator());
		moveFactor += water.getWaveSpeed() * Gdx.graphics.getDeltaTime();
		moveFactor %= 1.0;
		set(u_moveFactor, moveFactor);
		set(u_waveStrength, water.getWaveStrength());

	}

	@Override
	public boolean canRender(final Renderable renderable) {
		if (renderable.material.id.equals("water"))
			return true;
		else
			return false;
	}

	public String getLog() {
		return program.getLog();
	}

	@Override
	public void render(final Renderable renderable) {
		super.render(renderable);
	}

	public void setClippingPlane(final Plane clippingPlane) {
		WaterShader.clippingPlane = clippingPlane;
	}

}
