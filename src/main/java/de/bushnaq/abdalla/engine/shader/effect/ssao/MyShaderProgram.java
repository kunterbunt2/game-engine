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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class MyShaderProgram extends ShaderProgram {
    public MyShaderProgram(String vertexShader, String fragmentShader) {
        super(vertexShader, fragmentShader);
    }

    public MyShaderProgram(FileHandle internal, FileHandle internal1) {
        super(internal, internal1);
    }

    public String createPrefixBase(final Renderable renderable, final DefaultShader.Config config) {

        final String defaultPrefix = DefaultShader.createPrefix(renderable, config);
        String       version       = null;
//        if (isGL3())
        {
            if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
                if (version == null) {
//					version = "#version 150\n" + "#define GLSL3\n";
                }
            } else if (Gdx.app.getType() == Application.ApplicationType.Android) {
                if (version == null)
                    version = "#version 330 es\n" + "#define GLSL3\n";
            }
        }
        String prefix = "";
        if (version != null)
            prefix += version;
        prefix += defaultPrefix;

        return prefix;
    }

}
