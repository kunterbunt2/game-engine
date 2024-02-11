package de.bushnaq.abdalla.engine;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import net.mgsx.gltf.scene3d.animation.AnimationControllerHack;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;

/**
 * @author kunterbunt
 */
public class GameObject<T> {

    public BoundingBox boundingBox = new BoundingBox();
    public final Vector3 center = new Vector3();
    public AnimationControllerHack controller;
    public ModelInstanceHack instance;
    public Object interactive;
    public BoundingBox transformedBoundingBox = new BoundingBox();
    public ObjectRenderer<T> objectRenderer = null;//used to render 3D text

    public GameObject(final ModelInstanceHack instance, final Object interactive) {
        this(instance, interactive, null);
    }

    public GameObject(final ModelInstanceHack instance, final Object interactive, ObjectRenderer<T> objectRenderer) {
        this.instance = instance;
        this.interactive = interactive;
        this.objectRenderer = objectRenderer;
        boundingBox = new BoundingBox();
        instance.calculateBoundingBox(boundingBox);
        boundingBox.getCenter(center);
    }

    public void update() {
        transformedBoundingBox.set(boundingBox).mul(instance.transform);
    }
}
