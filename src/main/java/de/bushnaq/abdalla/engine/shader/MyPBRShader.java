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

package de.bushnaq.abdalla.engine.shader;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Plane;
import net.mgsx.gltf.scene3d.shaders.PBRShader;

/**
 * @author kunterbunt
 */
public class MyPBRShader extends PBRShader {
    private static Plane clippingPlane;
    public final   int   u_clippingPlane = register("u_clippingPlane");

    public MyPBRShader(final Renderable renderable, final Config config, final String prefix) {
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
        MyPBRShader.clippingPlane = clippingPlane;
    }

}
