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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;

import static com.badlogic.gdx.graphics.profiling.GLInterceptor.resolveErrorNumber;

/**
 * @author kunterbunt
 */
public class MyGLErrorListener implements GLErrorListener {

    public MyGLErrorListener() {
    }

    @Override
    public void onError(final int error) {
        String place = null;
        Thread.dumpStack();
        try {
            final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            for (int i = 0; i < stack.length; i++) {
                if ("check".equals(stack[i].getMethodName())) {
                    if (i + 1 < stack.length) {
                        final StackTraceElement glMethod = stack[i + 1];
                        place = glMethod.getMethodName();
                    }
                    break;
                }
            }
        } catch (final Exception ignored) {
        }

        if (place != null) {
            Gdx.app.error("GLProfiler", "Error " + resolveErrorNumber(error) + " from " + place);
        } else {
            Gdx.app.error("GLProfiler", "Error " + resolveErrorNumber(error) + " at: ", new Exception());
            // This will capture current stack trace for logging, if possible
        }
        // System.exit(error);
    }
}