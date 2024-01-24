package de.bushnaq.abdalla.engine;

/**
 * @author kunterbunt
 *
 */
public class Renderable<T> {

	public ObjectRenderer<T> renderer3D;

	public ObjectRenderer<T> get3DRenderer() {
		return renderer3D;
	}

	public void set3DRenderer(final ObjectRenderer<T> renderer3D) {
		this.renderer3D = renderer3D;
	}
}
