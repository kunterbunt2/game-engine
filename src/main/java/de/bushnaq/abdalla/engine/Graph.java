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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

class Frame {
    long delta;
}

/**
 * @author kunterbunt
 */
public abstract class Graph extends Array<Frame> {
    private static final float          Y_AXIS_WIDTH = 45;
    private final        AtlasRegion    atlasRegion;
    private final        Color          backgroundColor;
    private final        BitmapFont     captionFont;
    private final        FrameBuffer    fbo;
    private final        BitmapFont     font;
    private final        Color          graphColor;
    private final        String         graphName;
    private final        Color          graphTipColor;
    final                GlyphLayout    layout       = new GlyphLayout();
    private final        int            maxFrames;
    private final        float          pixelToUnitFactor;
    private final        String         unit;
    private final        ScreenViewport viewport     = new ScreenViewport();

    public Graph(String graphName, String unit, float pixelToUnitFactor, Color graphTipColor, Color graphColor, Color backgroundColor, int width, int height, BitmapFont font, BitmapFont captionFont, AtlasRegion atlasRegion) {
        this.graphName         = graphName;
        this.unit              = unit;
        this.pixelToUnitFactor = pixelToUnitFactor;
        this.graphTipColor     = graphTipColor;
        this.graphColor        = graphColor;
        this.backgroundColor   = backgroundColor;
        this.maxFrames         = width - (int) Y_AXIS_WIDTH;
        viewport.update(width, height, true);
        this.font        = font;
        this.captionFont = captionFont;
        this.atlasRegion = atlasRegion;
        layout.setText(font, "1234567890");
        {
            final FrameBufferBuilder frameBufferBuilder = new FrameBufferBuilder(width, height);
            frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGBA8, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE);
            fbo = frameBufferBuilder.build();
        }
    }

    public abstract void begin();

    public void dispose() {
        fbo.dispose();
    }

    public void draw(PolygonSpriteBatch batch2D) {
        batch2D.setProjectionMatrix(viewport.getCamera().combined);
        fbo.begin();
        batch2D.begin();
        batch2D.enableBlending();
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        {
            float d = (float) ((int) layout.height / 2);
            // Y-Axis
            batch2D.setColor(backgroundColor);
            batch2D.draw(atlasRegion, 0, 50, Y_AXIS_WIDTH, viewport.getScreenHeight());
            batch2D.draw(atlasRegion, Y_AXIS_WIDTH, 0, maxFrames, layout.height);
            for (int y = 50; y < viewport.getScreenHeight(); y += 50) {
                String text = (int) (y * pixelToUnitFactor) + " " + unit;
                batch2D.setColor(Color.WHITE);
                batch2D.draw(atlasRegion, Y_AXIS_WIDTH - 5, y, 5, 1);
                batch2D.setColor(backgroundColor);
                batch2D.draw(atlasRegion, Y_AXIS_WIDTH, y, viewport.getScreenWidth() - Y_AXIS_WIDTH, 1);
                font.setColor(Color.WHITE);
                font.draw(batch2D, text, 0, y + d);
            }
            font.setColor(Color.WHITE);
            captionFont.draw(batch2D, graphName, viewport.getScreenWidth() / 2, layout.height);
        }

        for (int i = 0; i < size; i++) {
            Frame frame = get(i);
            batch2D.setColor(graphTipColor);
            batch2D.draw(atlasRegion, Y_AXIS_WIDTH + i, layout.height + frame.delta, 1, 1);
            batch2D.setColor(graphColor);
            if (frame.delta > 0) {
                batch2D.draw(atlasRegion, Y_AXIS_WIDTH + i, layout.height, 1, frame.delta - 1);
            }
        }

        batch2D.end();
        fbo.end();
        batch2D.setColor(Color.WHITE);
    }

    public abstract void end();

    public FrameBuffer getFbo() {
        return fbo;
    }

    protected void update(long value) {
        Frame frame;
        if (size == maxFrames) {
            frame = removeIndex(0);
        } else {
            frame = new Frame();
        }
        frame.delta = value;
        add(frame);
    }

}
