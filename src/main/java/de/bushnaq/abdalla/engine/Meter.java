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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;

public class Meter<T extends RenderEngineExtension> {
    private RenderEngine3D<T> engine;
    private float             focalDepth;
    GameObject<T> instance1;
    GameObject<T> instance2;
    public Model rayCube;
    Ray rayX;
    Ray rayY;

    void createFocusCross(RenderEngine3D<T> engine, float focalDepth) {
        this.engine     = engine;
        this.focalDepth = focalDepth;
        createRayCube();
        final Vector3 position = new Vector3(0, 0, 0);
//        position.add(engine.getCamera().direction.scl(focalDepth));
        final Vector3 xVector = new Vector3(1, 0, 0);
        final Vector3 yVector = new Vector3(0, 1, 0);
        rayX      = new Ray(position, xVector);
        rayY      = new Ray(position, yVector);
        instance1 = createRay(rayX, 128f);
        instance2 = createRay(rayY, 128f);
    }

    private GameObject<T> createRay(final Ray ray, Float length) {
        final GameObject<T> instance = new GameObject<T>(new ModelInstanceHack(rayCube), null);
        instance.instance.materials.get(0).set(ColorAttribute.createDiffuse(Color.RED));
        engine.addStatic(instance);
        return instance;
    }

    private void createRayCube() {
        if (engine.isPbr() && rayCube == null) {
            final Attribute    color        = new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, Color.WHITE);
            final Attribute    metallic     = PBRFloatAttribute.createMetallic(0.5f);
            final Attribute    roughness    = PBRFloatAttribute.createRoughness(0.5f);
            final Attribute    occlusion    = PBRFloatAttribute.createOcclusionStrength(1.0f);
            final Material     material     = new Material(metallic, roughness, color, occlusion);
            final ModelBuilder modelBuilder = new ModelBuilder();
            rayCube = modelBuilder.createBox(1.0f, 1.0f, 1.0f, material, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        }
    }

    public void update() {
        update(instance1, rayX);
        update(instance2, rayY);
    }

    private void update(GameObject<T> instance, Ray ray) {
        final Vector3 direction = new Vector3(ray.direction.x, ray.direction.y, ray.direction.z);
//        final Vector3 position  = ray.origin.cpy();
        final Vector3 position = engine.getCamera().position.cpy();
        Vector3       cd       = engine.getCamera().direction.cpy();
        cd.scl(focalDepth);
        position.add(cd);
        final Vector3 xVector = new Vector3(1, 0, 0);
        direction.nor();
        position.x += direction.x;
        position.y += direction.y;
        position.z += direction.z;
        instance.instance.transform.setToTranslation(position);
        instance.instance.transform.rotate(xVector, direction);
        instance.instance.transform.scale(128f, 0.5f, 0.5f);
        instance.update();
    }
}
