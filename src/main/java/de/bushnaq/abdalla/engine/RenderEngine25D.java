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
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.scottlogic.util.GL32CMacIssueHandler;
import com.scottlogic.util.ShaderCompatibilityHelper;

/**
 * engine to draw 2D using 3D camera
 * This engine assumes that you manipulate teh batch matrix before the call
 * this means that frustum checking has to happen before the call to any of the draw methods.
 *
 * @param <T>
 */
public class RenderEngine25D<T> {
    final static Vector3               xVector = new Vector3(1, 0, 0);
    final static Vector3               yVector = new Vector3(0, 1, 0);
    public       CustomizedSpriteBatch batch;
    public       OrthographicCamera    camera;
    //    public       int                   height;
//    public       int                   width;
    T           gameEngine;
    GlyphLayout layout = new GlyphLayout();

    public RenderEngine25D(T gameEngine, OrthographicCamera camera) {
        this.gameEngine = gameEngine;
        this.camera     = camera;
        create();
    }

    public void bar(final TextureRegion image, final float x1, final float y1, final float x2, final float y2, final Color color) {
        final float width  = x2 - x1 + 1;
        final float height = y2 - y1 - 1;
        batch.setColor(color);
        batch.draw(image, x1, y1, width, height);
    }

    public void bar(final TextureRegion image, final float x1, final float y1, final float x2, final float y2, final float aZ, final Color color) {
        final float width  = x2 - x1 + 1;
        final float height = y2 - y1 - 1;
        batch.setColor(color);
        batch.draw(image, x1, y1, width, height);
    }

    private float calculateFontScaling(BitmapFont font, float size) {
        return size / font.getLineHeight();
    }

    private float calculateHShift(HAlignment hAlignment, float hShift) {
        return switch (hAlignment) {
            case LEFT -> 0;
            case CENTER -> -hShift;
            case RIGHT -> -hShift * 2;
        };
    }

    private float calculateMaxWidth(BitmapFont font, String... values) {
        float maxWidth = 0;
        for (String text : values) {
            final GlyphLayout layout = new GlyphLayout();
            layout.setText(font, text);
            maxWidth = Math.max(maxWidth, layout.width);
        }
        return maxWidth;
    }

    private float calculateVShift(VAlignment vAlignment, float vShift) {
        return switch (vAlignment) {
            case TOP -> 0;
            case CENTER -> -vShift;
            case BOTTOM -> -vShift * 2;

        };
    }

    public void circle(final TextureRegion image, final float x1, final float y1, final float radius, final float width, final Color color, final int edges) {
        batch.setColor(color);
        batch.circle(image, x1, y1, radius, width, edges);
    }

    public void create() {
        createShader();
    }

    private void createShader() {

        batch = new CustomizedSpriteBatch(5460, ShaderCompatibilityHelper.mustUse32CShader() ? GL32CMacIssueHandler.createSpriteBatchShader() : null);
    }

    public void dispose() {
        batch.dispose();
    }

    public void fillCircle(final TextureRegion image, final float x1, final float y1, final float radius, final int edges, final Color color) {
        batch.setColor(color);
        batch.fillCircle(image, x1, y1, radius, edges);
    }

    public void fillPie(final TextureRegion region, final float x1, final float y1, final float startRadius, final float endRadius, final float startAngle, final float endAngle, final Color color, final int edges, final BitmapFont font, final Color nameColor, final String name) {
        batch.setColor(color);
        batch.fillPie(region, x1, y1, startRadius, endRadius, startAngle, endAngle, edges);
        if (name != null) {
            batch.setColor(nameColor);
            final float angle = startAngle + (endAngle - startAngle) / 2;
            // ---Center
            layout.setText(font, name);
            final float tx = x1 + (float) Math.sin(angle) * (startRadius + (endRadius - startRadius) / 2) - layout.width / 2;
            final float ty = y1 - (float) Math.cos(angle) * (startRadius + (endRadius - startRadius) / 2) - layout.height / 2;
            text(tx, ty, font, nameColor, nameColor, name);
        }
    }

    public T getGameEngine() {
        return gameEngine;
    }

    public void label(Vector3 translation, float yRotation, final TextureRegion textureRegion, final float x1, final float y1, final float z1, final float radius, HAlignment hAlignment, VAlignment vAlignment, float thickness, final BitmapFont font, final Color lableColor, final String name, final Color nameColor, final String value, final Color valueColor) {
        float         nameSize  = 10;
        float         valueSize = 7;
        float         margin    = 1;
        final Matrix4 m         = new Matrix4();
        //move center of text to center of trader
        m.setToTranslation(translation.x, translation.y, translation.z);
        m.rotate(yVector, yRotation);
        //move to the top and back on engine
        m.translate(0, y1, 0);
        //rotate into the xz layer
        m.rotate(xVector, -90);
        batch.setTransformMatrix(m);
        final float      angle   = (float) (Math.PI / 6.0);//30deg
        final float      x2;
        final float      x3;
        final HAlignment textHAlignment;
        final float      scaling = calculateFontScaling(font, nameSize);
        switch (hAlignment) {
            case RIGHT:
            default:
                //label is oriented to the right
                x2 = (float) (x1 + radius * Math.cos(angle));
                x3 = x2 + calculateMaxWidth(font, name, value) * scaling;
                textHAlignment = HAlignment.LEFT;
                break;
            case LEFT:
                //label is oriented to the left
                x2 = (float) (x1 - radius * Math.cos(angle));
                x3 = x2 - calculateMaxWidth(font, name, value) * scaling;
                textHAlignment = HAlignment.RIGHT;
                break;
        }
        final float z2;
        switch (vAlignment) {
            default:
            case TOP:
                //label is oriented up
                z2 = (float) (-z1 + radius * Math.sin(angle));
                break;
            case BOTTOM:
                //label is oriented down
                z2 = (float) (-z1 - radius * Math.sin(angle));
                break;

        }
        batch.line(textureRegion, x1, y1, -z1, x2, y1, z2, lableColor, thickness);
        batch.line(textureRegion, x2, y1, z2, x3, y1, z2, lableColor, thickness);
        renderText(translation, yRotation, x2, y1, z2 + margin, font, nameColor, nameColor, name, nameSize, textHAlignment, VAlignment.BOTTOM);
        renderText(translation, yRotation, x2, y1, z2 - margin, font, nameColor, valueColor, value, valueSize, textHAlignment, VAlignment.TOP);
    }

    public void renderText(Vector3 translation, float yRotation, final float dx, final float dy, final float dz, BitmapFont font, final Color backgroundColor, final Color textColor, String text, final float size, HAlignment hAlignment, VAlignment vAlignment) {
        final float x = translation.x;
        final float y = translation.y;
        final float z = translation.z;
        //draw text
        {
            final Matrix4     m       = new Matrix4();
            final float       scaling = calculateFontScaling(font, size);
            final GlyphLayout layout  = new GlyphLayout();
            layout.setText(font, text);
            final float width  = layout.width;// contains the width of the current set text
            final float height = layout.height; // contains the height of the current set text
            float       hShift = calculateHShift(hAlignment, width * scaling / 2);
            float       vShift = calculateVShift(vAlignment, height * scaling / 2);
            //on top
            {
                //move center of text to center of trader
                m.setToTranslation(x, y, z);
                m.rotate(yVector, yRotation);
                //move to the top and back on engine
                m.translate(hShift + dx, dy, vShift - dz);
                //rotate into the xz layer
                m.rotate(xVector, -90);
                //scale to fit trader engine
                m.scale(scaling, scaling, 1f);
            }
            batch.setTransformMatrix(m);
            text(0, 0, font, Color.BLACK, textColor, text);
        }
    }

    public void renderTextCenterOnTop(Vector3 translation, float yRotation, final float dx, final float dy, final float dz, BitmapFont font, final Color backgroundColor, final Color textColor, String text, final float size) {
        final float x = translation.x;
        final float y = translation.y;
        final float z = translation.z;
        //draw text
        {
            final Matrix4 m = new Matrix4();
//            final float   fontSize = font.getLineHeight();
            final float       scaling = calculateFontScaling(font, size);
            final GlyphLayout layout  = new GlyphLayout();
            layout.setText(font, text);
            final float width  = layout.width;// contains the width of the current set text
            final float height = layout.height; // contains the height of the current set text
            //on top
            {
                //move center of text to center of trader
                m.setToTranslation(x, y, z);
                m.rotate(yVector, yRotation);
                //move to the top and back on engine
                m.translate(-width * scaling / 2 - dx, dy, -height * scaling / 2 - dz);
                //rotate into the xz layer
                m.rotate(xVector, -90);
                //scale to fit trader engine
                m.scale(scaling, scaling, 1f);
            }
            batch.setTransformMatrix(m);
            text(0, 0, font, Color.BLACK, textColor, text);
        }
    }

    public void setTransformMatrix(Matrix4 m) {
        batch.setTransformMatrix(m);
    }

    public void text(final float x1, final float y1, final BitmapFont font, final Color aBackgroundColor, final Color aTextColor, final String aString) {
        font.setColor(aTextColor);
        font.draw(batch, aString, x1, y1);
    }

}
