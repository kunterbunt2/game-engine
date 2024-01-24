package de.bushnaq.abdalla.engine.shader;

import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.math.Plane;

/**
 * @author kunterbunt
 *
 */
public interface GameShaderProviderInterface extends ShaderProvider {
	@Override
	public void dispose();

	public void setClippingPlane(final Plane clippingPlane);

//	public void setWaterAttribute(WaterAttribute waterAttribute);

}
