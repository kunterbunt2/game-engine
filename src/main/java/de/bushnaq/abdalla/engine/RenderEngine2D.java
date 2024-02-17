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
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.scottlogic.util.GL32CMacIssueHandler;
import com.scottlogic.util.ShaderCompatibilityHelper;

public class RenderEngine2D<T> {
    public CustomizedSpriteBatch batch;
    public OrthographicCamera    camera;
    public float                 centerX;
    public float                 centerY;
    public int                   height;
    public int                   width;
    T           gameEngine;
    GlyphLayout layout = new GlyphLayout();

    public RenderEngine2D(T gameEngine, OrthographicCamera camera) {
        this.gameEngine = gameEngine;
        this.camera     = camera;
        create();
    }

    public void bar(final TextureRegion image, final float aX1, final float aY1, final float aX2, final float aY2, final Color color) {
        final float       x1     = transformX(aX1);
        final float       y1     = transformY(aY1);
        final float       x2     = transformX(aX2);
        final float       y2     = transformY(aY2);
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

    public void circle(final TextureRegion image, final float aX1, final float aY1, final float radius, final float width, final Color color, final int edges) {
        final float x1 = transformX(aX1);
        final float y1 = transformY(aY1);
        if (camera.frustum.sphereInFrustum(x1, y1, 0.0f, radius)) {
            batch.setColor(color);
            batch.circle(image, x1, y1, radius, width, edges);
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

    public void fillCircle(final TextureRegion image, final float aX1, final float aY1, final float radius, final int edges, final Color color) {
        final float x1 = transformX(aX1);
        final float y1 = transformY(aY1);
        if (camera.frustum.sphereInFrustum(x1, y1, 0.0f, radius)) {
            batch.setColor(color);
            batch.fillCircle(image, x1, y1, radius, edges);
        }
    }

    public void fillPie(final TextureRegion region, final float x, final float y, final float startRadius, final float endRadius, final float startAngle, final float endAngle, final Color color, final int edges, final BitmapFont font, final Color nameColor, final String name) {
        final float x1 = transformX(x);
        final float y1 = transformY(y);
        if (camera.frustum.sphereInFrustum(x1, y1, 0.0f, endRadius)) {
            batch.setColor(color);
            batch.fillPie(region, x1, y1, startRadius, endRadius, startAngle, endAngle, edges);
            if (name != null) {
                batch.setColor(nameColor);
                final float angle = startAngle + (endAngle - startAngle) / 2;
                // ---Center
                layout.setText(font, name);
                final float tx = x + (float) Math.sin(angle) * (startRadius + (endRadius - startRadius) / 2) - layout.width / 2;
                final float ty = y - (float) Math.cos(angle) * (startRadius + (endRadius - startRadius) / 2) - layout.height / 2;
                text(tx, ty, font, nameColor, nameColor, name);
            }
        }
    }

    public T getGameEngine() {
        return gameEngine;
    }

    public void lable(final TextureRegion textureRegion, final float x, final float y, float width, float height, final float start, final float end, final BitmapFont font, final Color lableColor, final String name, final Color nameColor, final String value, final Color valueColor) {
        final float angle    = (float) (Math.PI / 6.0);
        final float x1       = (float) (x + width / 2 + start * Math.sin(angle));
        final float y1       = (float) (y + height / 2 - start * Math.cos(angle));
        final float x2       = (float) (x + width / 2 + end * Math.sin(angle));
        final float y2       = (float) (y + height / 2 - end * Math.cos(angle));
        final float x3       = x2 + width * 3 * camera.zoom;
        final float thicknes = 2.0f * camera.zoom;
        line(textureRegion, x1, y1, x2, y2, lableColor, thicknes);
        line(textureRegion, x2, y2, x3, y2, lableColor, thicknes);
        layout.setText(font, name);
        text(x2, y2 - layout.height * 1.1f, font, nameColor, nameColor, name);
        layout.setText(font, value);
        text(x2, y2 + layout.height * 0.1f, font, valueColor, valueColor, value);
    }

    public void line(final TextureRegion texture, final float aX1, final float aY1, final float aX2, final float aY2, final Color color, final float aThickness) {
        final float x1 = transformX(aX1);
        final float y1 = transformY(aY1);
        final float x2 = transformX(aX2);
        final float y2 = transformY(aY2);
        // the center of your hand
        final Vector3 center = new Vector3(x1, y1, 0);
        // you need a vector from the center to your touchpoint
        final Vector3 touchPoint = new Vector3(x2, y2, 0);
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
                y1, // y, center of rotation
                aThickness / 2, // origin x in the texture region
                0, // origin y in the texture region
                aThickness, // width
                touchPoint.len(), // height
                1.0f, // scale x
                1.0f, // scale y
                (float) rotation);
    }

    public void moveCenter(final int aX, final int aY) {
        //		System.out.println("------------------------------------------");
        // System.out.printf( "mouse x=%d, y=%d\n", aX, aY );
        // System.out.printf( "canvas x=%d, y=%d\n", myCanvas.getCanvas().getX(),
        // myCanvas.getCanvas().getY() );
        // Point pt = new Point( myCanvas.getCanvas().getLocation() );
        // SwingUtilities.convertPointToScreen( pt, myCanvas.getCanvas().getParent() );
        // System.out.printf( "points x=%d, y=%d\n", pt.x, pt.y );
        // System.out.printf( "delta x=%d, y=%d\n", aX - width / 2, height / 2 - aY );
        centerX += (aX - width / 2) / camera.zoom;
        centerY += (aY - height / 2) / camera.zoom;
        //		System.out.println("------------------------------------------");
    }

    public void soomIn(final int aX, final int aY) {
        if (camera.zoom > 1.0) {
            camera.zoom /= 1.2f;
            camera.update();
            batch.setProjectionMatrix(camera.combined);

//			atlasManager.defaultFont.getData().setScale(camera.zoom, camera.zoom);
//			defaultFontSize = (int) (Screen2D.FONT_SIZE * camera.zoom);
//			atlasManager.timeMachineFont.getData().setScale(camera.zoom, camera.zoom);
//			timeMachineFontSize = (int) (Screen2D.TIME_MACHINE_FONT_SIZE * camera.zoom);
            //			System.out.printf("Zoom = %f\n", camera.zoom);
        }
    }

    public void soomOut(final int aX, final int aY) {
        if (camera.zoom < 8.0) {
            camera.zoom *= 1.2f;
            camera.update();
            batch.setProjectionMatrix(camera.combined);
//			atlasManager.defaultFont.getData().setScale(camera.zoom, camera.zoom);
//			defaultFontSize = (int) (Screen2D.FONT_SIZE * camera.zoom);
//			atlasManager.timeMachineFont.getData().setScale(camera.zoom, camera.zoom);
//			timeMachineFontSize = (int) (Screen2D.TIME_MACHINE_FONT_SIZE * camera.zoom);
            //			System.out.printf("Zoom = %f\n", camera.zoom);
        }
    }

    public void text(final float aX1, final float aY1, final BitmapFont font, final Color aBackgroundColor, final Color aTextColor, final String aString) {
        final float x1 = transformX(aX1);
        final float y1 = transformY(aY1);
        // layout.setText( font, aString );
        // Vector3 p1 = new Vector3( x1, y1, 0 );
        // Vector3 p2 = new Vector3( x1 + layout.width, y1 + layout.height, 0 );
        // BoundingBox bb = new BoundingBox( p2, p1 );
        // if ( camera.frustum.boundsInFrustum( bb ) )
        {
            final float ascent = font.getAscent() - 1;// awtFont.getLineMetrics( aString, fontRenderContext ).getAscent();
            font.setColor(aTextColor);
            font.draw(batch, aString, x1, (int) (y1 - ascent));
        }
    }

    public float transformX(final float aX) {
        return (aX - centerX) + width / 2;
    }

    public float transformX(final float aX, final float aZ) {
        return (aX - centerX / aZ / camera.zoom) * camera.zoom + width / 2;
    }

    public float transformY(final float aY) {
        return height / 2 - (aY - centerY);
    }

    public float transformY(final float aY, final float aZ) {
        return height / 2 - (aY - centerY / aZ / camera.zoom) * camera.zoom;
    }

    public float untransformX(final float aX) {
        return (aX - width / 2) * camera.zoom + width / 2;
    }

    public float untransformY(final float aY) {
        return (aY - height / 2) * camera.zoom + height / 2;
    }
}
