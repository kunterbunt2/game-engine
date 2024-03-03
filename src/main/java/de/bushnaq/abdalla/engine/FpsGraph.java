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
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;

/**
 * @author kunterbunt
 */
public class FpsGraph extends Graph {
    public static final long NANOSECONDS_PER_SECOND = 1000000000L;
    private             long absolute               = 0;

    public FpsGraph(String graphName, Color graphTipColor, Color graphColor, Color backgroundColor, int width, int height, BitmapFont font, BitmapFont captionFont, AtlasRegion atlasRegion) {
        super(graphName, "fps", 1f, graphTipColor, graphColor, backgroundColor, width, height, font, captionFont, atlasRegion);
    }

    public void begin() {
        absolute = System.nanoTime();

    }

    public void end() {
        update(NANOSECONDS_PER_SECOND / (System.nanoTime() - absolute));
    }

}
