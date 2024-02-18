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

package de.bushnaq.abdalla.engine.audio.synthesis;

import de.bushnaq.abdalla.engine.audio.synthesis.util.CircularCubeActor;
import de.bushnaq.abdalla.engine.audio.synthesis.util.TranslationUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircularTranslationTest extends TranslationUtil {
    private static final int    NUMBER_OF_SOURCES = 10;
    private final        Logger logger            = LoggerFactory.getLogger(this.getClass());
    CircularCubeActor[] ccaa = new CircularCubeActor[NUMBER_OF_SOURCES];

    @Test
    public void circularTranslatingSources() {
        runFor = 10000;
        startLwjgl();
    }

    @Override
    public void create() {
        super.create();
        for (int i = 0; i < NUMBER_OF_SOURCES; i++) {
            ccaa[i] = new CircularCubeActor(i, 0);
            ccaa[i].get3DRenderer().create(getRenderEngine());
        }
    }

    @Override
    protected void update() throws Exception {
        for (int i = 0; i < NUMBER_OF_SOURCES; i++) {
            ccaa[i].get3DRenderer().update(getRenderEngine(), 0, 0, 0, false);
        }
        super.update();
    }

}