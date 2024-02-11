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

package de.bushnaq.abdalla.engine.shader.mirror;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Plane;

/**
 * @author kunterbunt
 */
public class MirrorShader extends DefaultShader {
    private static Plane  clippingPlane;
    private final  int    u_clippingPlane     = register("u_clippingPlane");
    private final  int    u_reflectionTexture = register("u_reflectionTexture");
    private final  int    u_reflectivity      = register("u_reflectivity");
    private        Mirror mirror;

    public MirrorShader(final Renderable renderable, final Config config, final String prefix, final Mirror mirror) {
        super(renderable, config, prefix);
        this.mirror = mirror;
    }

    @Override
    public boolean canRender(final Renderable renderable) {
        if (renderable.material.id.equals("mirror"))
            return true;
        else
            return false;
    }

    @Override
    public void begin(final Camera camera, final RenderContext context) {
        super.begin(camera, context);
        if (clippingPlane != null)
            set(u_clippingPlane, clippingPlane.normal.x, clippingPlane.normal.y, clippingPlane.normal.z, clippingPlane.d);
        set(u_reflectionTexture, mirror.getReflectionFbo().getColorBufferTexture());
        set(u_reflectivity, mirror.getReflectivity());
    }

    public String getLog() {
        return program.getLog();
    }

    @Override
    public void render(final Renderable renderable) {
        super.render(renderable);
    }

    public void setClippingPlane(final Plane clippingPlane) {
        MirrorShader.clippingPlane = clippingPlane;
    }

}
