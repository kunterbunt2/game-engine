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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

class Frame {
    long delta;
}

/**
 * @author kunterbunt
 */
public class TimeGraph extends Array<Frame> {
    private static final long           FACTOR   = 100000L;
    private              long           absolute = 0;
    private              AtlasRegion    atlasRegion;
    private              Color          backgroundColor;
    private              long           delta    = 0;
    private              FrameBuffer    fbo;
    private              BitmapFont     font;
    private              Color          highlightColor;
    private              int            maxFrames;
    private              ScreenViewport viewport = new ScreenViewport();

    public TimeGraph(Color highlightColor, Color backgroundColor, int width, int height, BitmapFont font, AtlasRegion atlasRegion) {
        this.highlightColor  = highlightColor;
        this.backgroundColor = backgroundColor;
        this.maxFrames       = width;
        viewport.update(width, height, true);
        this.font        = font;
        this.atlasRegion = atlasRegion;
        {
            final FrameBufferBuilder frameBufferBuilder = new FrameBufferBuilder(width, height);
            frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGBA8, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE);
            fbo = frameBufferBuilder.build();
        }
    }

    public void begin() {
        absolute = System.nanoTime();

    }

    public void dispose() {
        fbo.dispose();
    }

    public void draw(PolygonSpriteBatch batch2D) {
        {
            batch2D.setProjectionMatrix(viewport.getCamera().combined);
            fbo.begin();
            batch2D.begin();
            batch2D.enableBlending();
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            {
                for (int y = 50; y < 500; y += 50) {
                    String            text   = "" + y / 10 + "ms";
                    final GlyphLayout layout = new GlyphLayout();
                    layout.setText(font, text);
                    final float width = layout.width;// contains the width of the current set text
                    batch2D.setColor(Color.RED);
                    batch2D.draw(atlasRegion, 0, y, 5, 1);
                    font.setColor(Color.RED);
                    font.draw(batch2D, text, 6, y + layout.height / 2, width, Align.left, false);
                }
            }

            for (int i = 0; i < size; i++) {
                Frame frame = get(i);
                batch2D.setColor(highlightColor);
                batch2D.draw(atlasRegion, i, frame.delta, 1, 1);
                batch2D.setColor(backgroundColor);
                if (frame.delta > 0) {
                    batch2D.draw(atlasRegion, i, 0, 1, frame.delta - 1);
                }
            }
            batch2D.end();
            fbo.end();
            batch2D.setColor(Color.WHITE);
        }
    }

    public void end() {
        delta = (System.nanoTime() - absolute) / FACTOR;
    }

    public FrameBuffer getFbo() {
        return fbo;
    }

    public void update() {
        {
            Frame frame;
            if (size == maxFrames) {
                frame = removeIndex(0);
            } else {
                frame = new Frame();
            }
            frame.delta = delta;
            add(frame);
        }
    }

}
