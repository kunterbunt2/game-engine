package de.bushnaq.abdalla.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

import net.mgsx.gltf.scene3d.attributes.FogAttribute;

/**
 * @author kunterbunt
 *
 */
public class Fog {
	private float			beginDistance	= 15;
	private Color			color			= Color.BLACK;
	private FogAttribute	equation		= null;
	private float			falloffGradiant	= 0.5f;
	private float			fullDistance	= 30;

	public Fog(Color color, float fogMinDistance, float fogMaxDistance, float fogMixValue) {
		this.color = color;
		this.setBeginDistance(fogMinDistance);
		this.fullDistance = fogMaxDistance;
		this.falloffGradiant = fogMixValue;

	}

	public float getBeginDistance() {
		return beginDistance;
	}

	public Color getColor() {
		return color;
	}

	public float getFalloffGradiant() {
		return falloffGradiant;
	}

	public float getFullDistance() {
		return fullDistance;
	}

	public void setBeginDistance(float beginDistance) {
		this.beginDistance = beginDistance;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setFalloffGradiant(float falloffGradiant) {
		this.falloffGradiant = falloffGradiant;
	}

	public void setFogEquation(Environment environment) {
		equation = environment.get(FogAttribute.class, FogAttribute.FogEquation);
	}

	public void setFullDistance(float fullDistance) {
		this.fullDistance = fullDistance;
	}

	public void updateFog(Environment environment) {
		if (equation != null) {
			// fogEquation.x is where the fog begins
			// .y should be where it reaches 100%
			// then z is how quickly it falls off
			// fogEquation.value.set(MathUtils.lerp(sceneManager.camera.near,
			// sceneManager.camera.far, (FOG_X + 1f) / 2f),
			// MathUtils.lerp(sceneManager.camera.near, sceneManager.camera.far, (FAG_Y +
			// 1f) / 2f),
			// 1000f * (FOG_Z + 1f) / 2f);
			equation.value.set(getBeginDistance(), fullDistance, falloffGradiant);
			environment.set(new ColorAttribute(ColorAttribute.Fog, getColor()));
		}
	}
}
