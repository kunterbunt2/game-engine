package de.bushnaq.abdalla.engine;


/**
 * @author kunterbunt
 *
 */
public abstract class ObjectRenderer<T> {
	public void create(final float x, final float y, final float z, final RenderEngine3D<T> renderEngine) {
	}

	public void create(final RenderEngine3D<T> renderEngine) {
	}

	public void destroy(final RenderEngine3D<T> renderEngine) {
	}

	public void render(final float px, final float py, final RenderEngine2D<T> renderEngine, final int index, final boolean selected) {}
	public void renderText(final float aX, final float aY, final float aZ, final RenderEngine3D<T> renderEngine, final int index) {
	}

	public void renderText(final RenderEngine3D<T> renderEngine, final int index, final boolean selected) {
	}

	public void update(final float x, final float y, final float z, final RenderEngine3D<T> renderEngine, final long currentTime, final float timeOfDay, final int index, final boolean selected) throws Exception {
	}

	public void update(final RenderEngine3D<T> renderEngine, final long currentTime, final float timeOfDay, final int index, final boolean selected) throws Exception {
	}

	public boolean withinBounds(final float x, final float y) {
		return false;
	}
}
