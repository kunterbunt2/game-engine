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

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.scottlogic.util.GL32CMacIssueHandler;
import com.scottlogic.util.ShaderCompatibilityHelper;

public class Render2Dxz<T> {
    public CustomizedSpriteBatch batch;
    public Camera                camera;
    public int                   height;
    public int                   width;
    T           gameEngine;
    GlyphLayout layout = new GlyphLayout();
    float       zoom   = 1.0f;

    public Render2Dxz(T gameEngine, Camera camera) {
        this.gameEngine = gameEngine;
        this.camera     = camera;
        create();
    }

    public void bar(final TextureRegion image, final float aX1, final float aY1, final float aZ1, final float aX2, final float aY2, final float aZ2, final Color color) {
        final float       x1     = transformX(aX1);
        final float       y1     = transformY(aY1);
        final float       z1     = transformY(aZ1);
        final float       x2     = transformX(aX2);
        final float       y2     = transformY(aY2);
        final float       z2     = transformY(aZ2);
        final float       width  = x2 - x1 + 1;
        final float       height = -z2 + z1 - 1;
        final Vector3     p1     = new Vector3(x1, y1, z1);
        final Vector3     p2     = new Vector3(x2, y2, z2);
        final BoundingBox bb     = new BoundingBox(p2, p1);
        // Vector3[] v3 = camera.frustum.planePoints;
        if (camera.frustum.boundsInFrustum(bb)) {
            batch.setColor(color);
            batch.draw(image, x1, -z1, width, height);
        }
    }

    public void bar(final TextureRegion image, final float aX1, final float aY1, final float aX2, final float aY2, final float aZ, final Color color) {
        final float       x1     = transformX(aX1, aZ);
        final float       y1     = transformY(aY1, aZ);
        final float       x2     = transformX(aX2, aZ);
        final float       y2     = transformY(aY2, aZ);
        final float       width  = x2 - x1 + 1;
        final float       height = y2 - y1 - 1;
        final Vector3     p1     = new Vector3(x1, y1, 0);
        final Vector3     p2     = new Vector3(x2, y2, 0);
        final BoundingBox bb     = new BoundingBox(p2, p1);
        // Vector3[] v3 = camera.frustum.planePoints;
        if (camera.frustum.boundsInFrustum(bb)) {
            batch.setColor(color);
            batch.draw(image, x1, y1, width, height);
        }
    }

    public void circle(final TextureRegion image, final float aX1, final float aY1, float aZ1, final float radius, final float width, final Color color, final int edges) {
        final float x1 = transformX(aX1);
        final float y1 = transformY(aY1);
        final float z1 = transformY(aZ1);
        if (camera.frustum.sphereInFrustum(x1, y1, z1, radius)) {
            batch.setColor(color);
            batch.circle(image, x1, -z1, radius, width, edges);
        }
    }

    public void create() {
        createShader();
    }

    private void createShader() {

        batch = new CustomizedSpriteBatch(5460, ShaderCompatibilityHelper.mustUse32CShader() ? GL32CMacIssueHandler.createSpriteBatchShader() : null);
//        batch = new CustomizedSpriteBatch(5460);
    }

    public void dispose() {
        batch.dispose();
        //		for (FontData fontData : fontDataList) {
        //			fontData.font.dispose();
        //		}
        //		defaultFont.dispose();
        //		menuFont.dispose();
        //		timeMachineFont.dispose();
        //		atlas.dispose();
    }

    public void fillCircle(final TextureRegion image, final float x, final float y, float z, final float radius, final int edges, final Color color) {
        if (camera.frustum.sphereInFrustum(x, y, z, radius)) {
            batch.setColor(color);
            batch.fillCircle(image, x, -z, radius, edges);
        }
    }

    public void fillPie(final TextureRegion region, final float x, final float y, float z, final float startRadius, final float endRadius, final float startAngle, final float endAngle, final Color color, final int edges, final BitmapFont font, final Color nameColor, final String name) {
        final float x1 = transformX(x);
        final float y1 = transformY(y);
        final float z1 = transformY(z);
        if (camera.frustum.sphereInFrustum(x1, y1, z1, endRadius)) {
            batch.setColor(color);
            batch.fillPie(region, x1, -z1, startRadius, endRadius, startAngle, endAngle, edges);
            if (name != null) {
                batch.setColor(nameColor);
                final float angle = startAngle + (endAngle - startAngle) / 2;
                // ---Center
                layout.setText(font, name);
                final float tx = x + (float) Math.sin(angle) * (startRadius + (endRadius - startRadius) / 2) - layout.width / 2;
                final float tz = z - (float) Math.cos(angle) * (startRadius + (endRadius - startRadius) / 2) - layout.height / 2;
                text(tx, y1, tz, font, nameColor, nameColor, name);
            }
        }
    }

    public T getGameEngine() {
        return gameEngine;
    }

    /**
     * draw a label in the upper right corner of the center
     *
     * @param textureRegion
     * @param x
     * @param y
     * @param width
     * @param height
     * @param start
     * @param end
     * @param font
     * @param lableColor
     * @param name
     * @param nameColor
     * @param value
     * @param valueColor
     */
    public void label(final TextureRegion textureRegion, float x, float y, float z, float width, float height, float start, float end,
                      BitmapFont font, Color lableColor, String name, Color nameColor, String value, Color valueColor) {
//        if (camera.frustum.sphereInFrustum(x, y, z, Math.max(width, height)))
        {
            final float angle    = (float) (Math.PI / 6.0);
            final float x1       = (float) (x + width / 2 + start * Math.sin(angle));
            final float z1       = (float) (z - height / 2 + start * Math.cos(angle));
            final float x2       = (float) (x + width / 2 + end * Math.sin(angle));
            final float z2       = (float) (z - height / 2 + end * Math.cos(angle));
            final float x3       = x2 + width * 3 * zoom;
            final float thicknes = 2.0f * zoom;
            line(textureRegion, x1, 0, z1, x2, 0, z2, lableColor, thicknes);
            line(textureRegion, x2, 0, z2, x3, 0, z2, lableColor, thicknes);
            layout.setText(font, name);
            text(x2, y, (z2 - layout.height * 1.1f), font, nameColor, nameColor, name);
            layout.setText(font, value);
            text(x2, y, (z2 + layout.height * 0.1f), font, valueColor, valueColor, value);
        }
    }

    public void line(final TextureRegion texture, final float aX1, final float aY1, float aZ1, final float aX2, final float aY2, float aZ2, final Color color, final float aThickness) {
        final float x1 = transformX(aX1);
        final float y1 = transformY(aY1);
        final float z1 = transformX(aZ1);
        final float x2 = transformX(aX2);
        final float y2 = transformY(aY2);
        final float z2 = transformY(aZ2);
        // the center of your hand
        final Vector3 center = new Vector3(x1, -z1, 0);
        // you need a vector from the center to your touchpoint
        final Vector3 touchPoint = new Vector3(x2, -z2, 0);
        touchPoint.sub(center);
        // now convert into polar angle
        double rotation = Math.atan2(touchPoint.y, touchPoint.x);
        // rotation should now be between -PI and PI
        // so scale to 0..1
        rotation = (rotation + Math.PI) / (Math.PI * 2);
        // SpriteBatch.draw needs degrees
        rotation *= 360;
        // add Offset because of reasons
        rotation += 90;
        batch.setColor(color);
        batch.draw(texture, x1, // x, center of rotation
                -z1, // y, center of rotation
                aThickness / 2, // origin x in the texture region
                0, // origin y in the texture region
                aThickness, // width
                touchPoint.len(), // height
                1.0f, // scale x
                1.0f, // scale y
                (float) rotation);
    }

    public void text(final float aX1, final float aY1, float aZ1, final BitmapFont font, final Color aBackgroundColor, final Color aTextColor, final String aString) {
        final float x1 = transformX(aX1);
        final float y1 = transformY(aY1);
        final float z1 = transformY(aZ1);
        // layout.setText( font, aString );
        // Vector3 p1 = new Vector3( x1, y1, 0 );
        // Vector3 p2 = new Vector3( x1 + layout.width, y1 + layout.height, 0 );
        // BoundingBox bb = new BoundingBox( p2, p1 );
//         if ( camera.frustum.boundsInFrustum( bb ) )
        {
            final float ascent = font.getAscent() - 1;// awtFont.getLineMetrics( aString, fontRenderContext ).getAscent();
            font.setColor(aTextColor);
            font.draw(batch, aString, x1, (int) -(z1 - ascent));
        }
    }

    public float transformX(final float aX, final float aZ) {
        return aX;
    }

    public float transformX(final float aX) {
        return aX;
    }

    public float transformY(final float aY, final float aZ) {
        return aY;
    }

    public float transformY(final float aY) {
        return aY;
    }

}
