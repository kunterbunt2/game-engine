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

package com.scottlogic.util;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * A factory for {@link ShaderProgram}s, which supports some convenience options.
 *
 * @author damios
 */
public final class ShaderProgramFactory {

    private ShaderProgramFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a ShaderProgram and automatically throws an {@link GdxRuntimeException} when it couldn't be compiled.
     *
     * @param vertexShader
     * @param fragmentShader
     * @return the shader program
     */
    public static ShaderProgram fromFile(FileHandle vertexShader, FileHandle fragmentShader) {
        return fromString(vertexShader.readString(), fragmentShader.readString());
    }

    /**
     * Creates a ShaderProgram and automatically throws an {@link GdxRuntimeException} when it couldn't be compiled.
     *
     * @param vertexShader
     * @param fragmentShader
     * @return the shader program
     */
    public static ShaderProgram fromString(String vertexShader, String fragmentShader) {
        return fromString(vertexShader, fragmentShader, true);
    }

    /**
     * @param vertexShader
     * @param fragmentShader
     * @param throwException whether to throw an exception when the shader couldn't be compiled
     * @return the shader program
     */
    public static ShaderProgram fromString(String vertexShader, String fragmentShader, boolean throwException) {
        return fromString(vertexShader, fragmentShader, throwException, false);
    }

    /**
     * Creates a {@link ShaderProgram}.
     *
     * @param vertexShader   the vertex shader code
     * @param fragmentShader the fragment shader code
     * @param throwException whether to throw an exception when the shader couldn't be compiled ({@link ShaderPreconditions#checkCompilation(ShaderProgram)})
     * @param ignorePrepend  whether to ignore the code in {@link ShaderProgram#prependFragmentCode} and {@link ShaderProgram#prependVertexCode}; is useful to prevent the version being set twice
     * @return the shader program
     */
    public static ShaderProgram fromString(String vertexShader, String fragmentShader, boolean throwException, boolean ignorePrepend) {
        String prependVertexCode = null, prependFragmentCode = null;
        if (ignorePrepend) {
            prependVertexCode                 = ShaderProgram.prependVertexCode;
            ShaderProgram.prependVertexCode   = null;
            prependFragmentCode               = ShaderProgram.prependFragmentCode;
            ShaderProgram.prependFragmentCode = null;
        }

        ShaderProgram program = new ShaderProgram(vertexShader, fragmentShader);

        if (ignorePrepend) {
            ShaderProgram.prependVertexCode   = prependVertexCode;
            ShaderProgram.prependFragmentCode = prependFragmentCode;
        }

        if (throwException)
            ShaderPreconditions.checkCompilation(program);

        return program;
    }

    /**
     * A simple preconditions class used to check whether a {@link ShaderProgram} was properly compiled.
     *
     * @author damios
     */
    public static final class ShaderPreconditions {

        private ShaderPreconditions() {
            throw new UnsupportedOperationException();
        }

        /**
         * Throws a {@link GdxRuntimeException} when the program was not compiled. The compilation log is printed as part of the exception's message.
         *
         * @param program
         */
        public static void checkCompilation(ShaderProgram program) {
            checkCompilation(program, "");
        }

        /**
         * Throws a {@link GdxRuntimeException} when the program was not compiled. The compilation log is appended to {@code msg}.
         *
         * @param program
         * @param msg     the exception's message
         */
        public static void checkCompilation(ShaderProgram program, String msg) {
            if (!program.isCompiled())
                throw new GdxRuntimeException(msg + program.getLog());
        }

    }

}
