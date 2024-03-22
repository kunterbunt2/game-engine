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

import de.bushnaq.abdalla.engine.audio.synthesis.util.OggCubeActor;
import de.bushnaq.abdalla.engine.audio.synthesis.util.SynthType;
import de.bushnaq.abdalla.engine.audio.synthesis.util.TranslationUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmbientOggStreamingTest extends TranslationUtil {
    private static final int    NUMBER_OF_SOURCES = 1;
    private final        Logger logger            = LoggerFactory.getLogger(this.getClass());
    OggCubeActor[] ccaa = new OggCubeActor[NUMBER_OF_SOURCES];

    @Test
    public void circularTranslatingSources() {
        runFor = 20000;
        startLwjgl();
    }

    @Override
    public void create() {
        super.create();
        for (int i = 0; i < NUMBER_OF_SOURCES; i++) {
            ccaa[i] = new OggCubeActor(i, SynthType.AMBIENT_OGG);
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