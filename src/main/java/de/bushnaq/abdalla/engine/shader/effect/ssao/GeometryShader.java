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

package de.bushnaq.abdalla.engine.shader.effect.ssao;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import net.mgsx.gltf.scene3d.shaders.PBRShader;

/**
 * @author kunterbunt
 */
public class GeometryShader extends PBRShader {
    private final FrameBuffer postFbo;
    private final Ssao        ssao;

    public GeometryShader(final Renderable renderable, final Config config, final String prefix, Ssao ssao, FrameBuffer postFbo) {
        super(renderable, config, prefix);
        this.ssao    = ssao;
        this.postFbo = postFbo;
    }

    @Override
    public void begin(final Camera camera, final RenderContext context) {
        super.begin(camera, context);

        program.bind();
    }

    @Override
    public boolean canRender(final Renderable renderable) {
        return true;
    }

    public String getLog() {
        return program.getLog();
    }

    @Override
    public void render(final Renderable renderable) {
        super.render(renderable);
    }

}
