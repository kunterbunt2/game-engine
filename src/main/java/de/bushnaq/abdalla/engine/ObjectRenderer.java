package de.bushnaq.abdalla.engine;


/**
 * @author kunterbunt
 *
 */
public abstract class ObjectRenderer<T> {
	public void create(final float x, final float y, final float z, final T gameEngine) {
	}

	public void create(final T gameEngine) {
	}

	public void destroy(final T gameEngine) {
	}

	public void renderText(final float aX, final float aY, final float aZ, final RenderEngine sceneManager, final int index) {
	}

	public void renderText(final RenderEngine sceneManager, final int index, final boolean selected) {
	}

	public void update(final float x, final float y, final float z, final T gameEnginer, final long currentTime, final float timeOfDay, final int index, final boolean selected) throws Exception {
	}

	public void update(final T gameEngine, final long currentTime, final float timeOfDay, final int index, final boolean selected) throws Exception {
	}

	public boolean withinBounds(final float x, final float y) {
		return false;
	}
}
