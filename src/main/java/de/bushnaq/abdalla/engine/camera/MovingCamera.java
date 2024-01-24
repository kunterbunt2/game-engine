package de.bushnaq.abdalla.engine.camera;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * @author kunterbunt
 *
 */
public class MovingCamera extends PerspectiveCamera {
	boolean			dirty		= false;
	public Vector3	lookat		= new Vector3(0f, 0f, 0f);
	public Vector3	velocity	= new Vector3(0f, 0f, 0f);

	public MovingCamera(final float fieldOfViewY, final float viewportWidth, final float viewportHeight) {
		super(fieldOfViewY, viewportWidth, viewportHeight);
	}

	public float getPitch() {
		final float camAngle = -(float) Math.atan2(up.x, up.y) * MathUtils.radiansToDegrees + 180;
		return camAngle;
	}

	/**
	 * get camera pitch
	 *
	 * @return
	 */
	public float getYaw() {
		// yaw
		final Vector3	pitchAxis	= up.crs(direction);
		final float		camAngle	= -(float) Math.atan2(pitchAxis.x, pitchAxis.y) * MathUtils.radiansToDegrees + 180;
		return camAngle;
		// float camAngle = -(float) Math.atan2(up.x, up.y) * MathUtils.radiansToDegrees + 180;
		// return camAngle;
	}

	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void lookAt(final float x, final float y, final float z) {
		lookat.set(x, y, z);
		super.lookAt(x, y, z);
	}

	public void setDirty(final boolean dirty) {
		this.dirty = dirty;
	}
}
