package de.bushnaq.abdalla.engine.shader;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Plane;

/**
 * @author kunterbunt
 *
 */
public class MyShader extends DefaultShader {
	private static Plane	clippingPlane;
	public final int		u_clippingPlane	= register("u_clippingPlane");

	public MyShader(final Renderable renderable, final Config config, final String prefix) {
		super(renderable, config, prefix);
	}

	@Override
	public void begin(final Camera camera, final RenderContext context) {
		super.begin(camera, context);
		if (clippingPlane != null)
			set(u_clippingPlane, clippingPlane.normal.x, clippingPlane.normal.y, clippingPlane.normal.z, clippingPlane.d);
	}

	@Override
	public boolean canRender(final Renderable renderable) {
		if (renderable.material.id.equals("water")) {
			return false;
		} else if (renderable.material.id.equals("mirror")) {
			return false;
		} else if (renderable.material.id.equals("post")) {
			return false;
		} else {
			return super.canRender(renderable);
		}
	}

	public void setClippingPlane(final Plane clippingPlane) {
		MyShader.clippingPlane = clippingPlane;
	}

}
