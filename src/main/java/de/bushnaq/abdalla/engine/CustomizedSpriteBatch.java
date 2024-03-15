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
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

/**
 * Draws batched quads using indices.
 *
 * @author mzechner
 * @author Nathan Sweet
 * @author Abdalla Bushnaq
 * @see Batch
 */
public class CustomizedSpriteBatch extends PolygonSpriteBatch {
    public CustomizedSpriteBatch(final int size) {
        super(size);
    }

    public CustomizedSpriteBatch(int size, ShaderProgram defaultShader) {
        super(size, size * 2, defaultShader);
    }

    @Override
    public void begin() {
        if (!isDrawing())
            super.begin();
    }

    @Override
    public void end() {
        if (isDrawing())
            super.end();
    }

    public void circle(final TextureRegion region, final float x, final float y, final float radius, final float width, final int edges) {
        if (!isDrawing())
            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
        final float startRadius = radius - width;
        final float endRadius   = radius + width;
        final float startAngle  = 0f;
        final float endAngle    = (float) Math.PI * 2;
        // float border = 0.02f;//---between this and the next pie piece
        // float overlapping = 0.03f;//---used to ensure no artifact are visible where
        // the squares overlap
        // edges = (int)Math.min( edges, ( endAngle - border - startAngle ) /
        // overlapping );
        final float[] vertices      = new float[edges * 2 * 2];
        final short[] triangles     = new short[edges * 2 * 3];
        int           verticesIndex = 0;
        int           triangleIndex = 0;
        // ---Center point
        // final float u = region.getU();
        // final float v = region.getV2();
        // final float u2 = region.getU2();
        // final float v2 = region.getV();
        // float w2 = (u2 - u) / 2;
        // float h2 = (v2 - v) / 2;
        // float color = this.getPackedColor();
        final float shiftX = region.getRegionWidth() / 2;
        final float shiftY = region.getRegionHeight() / 2;
        for (int edge = 0; edge < edges; edge++) {
            final float angle = startAngle + edge * (endAngle - startAngle) / edges;
            // float nextAngle = startAngle + ( edge + 1 ) * ( endAngle - startAngle ) /
            // edges;
            // ---vertice 0
            vertices[verticesIndex++] = shiftX + (float) Math.sin(angle) * (region.getRegionWidth() / 2) * (startRadius / endRadius);
            vertices[verticesIndex++] = shiftY + (float) Math.cos(angle) * (region.getRegionHeight() / 2) * (startRadius / endRadius);
            // vertices[verticesIndex++] = color;
            // vertices[verticesIndex++] = u + w2 + (float) Math.sin(angle) * w2 / 2;
            // vertices[verticesIndex++] = v + h2 + (float) Math.cos(angle) * h2 / 2;
            // ---vertice 1
            vertices[verticesIndex++] = shiftX + (float) Math.sin(angle) * region.getRegionWidth() / 2;
            vertices[verticesIndex++] = shiftY + (float) Math.cos(angle) * region.getRegionHeight() / 2;
            // vertices[verticesIndex++] = color;
            // vertices[verticesIndex++] = u + w2 + (float) Math.sin(angle) * w2;
            // vertices[verticesIndex++] = v + h2 + (float) Math.cos(angle) * h2;
            // triangle 0-3-1
            // triangle 2-5-3
            // triangle 4-7-5
            // triangle 6-1-7
            triangles[triangleIndex++] = (short) (edge * 2);
            if (edge < edges - 1) {
                triangles[triangleIndex++] = (short) (edge * 2 + 3);
            } else {
                triangles[triangleIndex++] = (short) 1;
            }
            triangles[triangleIndex++] = (short) (edge * 2 + 1);
            // triangle 2-3-0
            // triangle 4-5-2
            // triangle 6-7-4
            // triangle 0-1-6
            if (edge < edges - 1) {
                triangles[triangleIndex++] = (short) (edge * 2 + 2);
                triangles[triangleIndex++] = (short) (edge * 2 + 3);
            } else {
                triangles[triangleIndex++] = (short) (0);
                triangles[triangleIndex++] = (short) (1);
            }
            triangles[triangleIndex++] = (short) (edge * 2 + 0);
        }
        // for ( int veri = 0; veri < vertices.length; veri += VERTEX_SIZE )
        // {
        // System.out.printf( "%f, %f, %f, %f, %f\n", vertices[veri], vertices[veri +
        // 1], vertices[veri + 2], vertices[veri + 3], vertices[veri + 4] );
        // }
        // for ( int trii = 0; trii < triangles.length; trii += 3 )
        // {
        // System.out.printf( "%d, %d, %d\n", triangles[trii], triangles[trii + 1],
        // triangles[trii + 2] );
        // }
        // drawVertices(region, vertices, triangles, x - shiftX, y - shiftY, endRadius * 2 + 1, endRadius * 2 + 1);
        final float w = (endRadius * 2 + 1);
        final float h = (endRadius * 2 + 1);
        drawVertices(region, vertices, triangles, x - shiftX * (w / region.getRegionWidth()), y - shiftY * (h / region.getRegionHeight()), w, h);
        /*
         * float startRadius = radius - width; float endRadius = radius + width; float startAngle = 0f; float endAngle = (float)Math.PI * 2; float border = 0.02f;//---between this and the next pie piece float overlapping
         * = 0.03f;//---used to ensure no artifact are visible where the squares overlap edges = (int)Math.min( edges, ( endAngle - border - startAngle ) / overlapping ); if ( !drawing ) throw new IllegalStateException(
         * "SpriteBatch.begin must be called before draw." ); float[] vertices = this.vertices; Texture texture = region.texture; if ( texture != lastTexture ) { switchTexture( texture ); } else if ( idx + edges * 4 * 5
         * >= vertices.length ) // flush(); final float u = region.u; final float v = region.v2; final float u2 = region.u2; final float v2 = region.v; float color = this.color; int idx = this.idx; for ( int edge = 0;
         * edge < edges; edge++ ) { float angle = startAngle + edge * ( endAngle - startAngle ) / edges; float nextAngle = startAngle + ( edge + 1 ) * ( endAngle - startAngle ) / edges; //---Center vertices[idx++] = x +
         * (float)Math.sin( angle ) * startRadius; vertices[idx++] = y + (float)Math.cos( angle ) * startRadius; vertices[idx++] = color; vertices[idx++] = u; vertices[idx++] = v; vertices[idx++] = x + (float)Math.sin(
         * angle ) * endRadius; vertices[idx++] = y + (float)Math.cos( angle ) * endRadius; vertices[idx++] = color; vertices[idx++] = u; vertices[idx++] = v2; if ( edge == edges - 1 ) { overlapping = 0.0f; }
         * vertices[idx++] = x + (float)Math.sin( nextAngle + overlapping ) * endRadius; vertices[idx++] = y + (float)Math.cos( nextAngle + overlapping ) * endRadius; vertices[idx++] = color; vertices[idx++] = u2;
         * vertices[idx++] = v2; vertices[idx++] = x + (float)Math.sin( nextAngle + overlapping ) * startRadius; vertices[idx++] = y + (float)Math.cos( nextAngle + overlapping ) * startRadius; vertices[idx++] = color;
         * vertices[idx++] = u; vertices[idx++] = v; } this.idx = idx;
         */
    }

    private void drawVertices(final TextureRegion region, final float[] vertices, final short[] triangles, final float x, final float y, final float width, final float height) {
        final PolygonRegion polygonRegion = new PolygonRegion(region, vertices, triangles);
        draw(polygonRegion, x, y, width, height);
    }

    public void fillCircle(final TextureRegion region, final float x, final float y, final float radius, final int edges) {
        final float[] vertices      = new float[(edges + 1) * 2];
        final short[] triangles     = new short[edges * 3];
        int           verticesIndex = 0;
        int           triangleIndex = 0;
        // ---Center point
        // final float u = region.getU();
        // final float v = region.getV2();
        // final float u2 = region.getU2();
        // final float v2 = region.getV();
        // float w2 = (u2 - u) / 2;
        // float h2 = (v2 - v) / 2;
        final float shiftX = region.getRegionWidth() / 2;
        final float shiftY = region.getRegionHeight() / 2;
        vertices[verticesIndex++] = shiftX;
        vertices[verticesIndex++] = shiftY;
        // vertices[verticesIndex++] = getPackedColor();
        // vertices[verticesIndex++] = u + w2;
        // vertices[verticesIndex++] = v + h2;
        for (int edge = 0; edge < edges; edge++) {
            final float angle = (float) (edge * Math.PI * 2 / edges);
            // float nextAngle = (float)( ( edge + 1 ) * Math.PI * 2 / edges );
            // ---
            vertices[verticesIndex++] = shiftX + (float) Math.sin(angle) * region.getRegionWidth() / 2;
            vertices[verticesIndex++] = shiftY + (float) Math.cos(angle) * region.getRegionHeight() / 2;
            // vertices[verticesIndex++] = getPackedColor();
            // vertices[verticesIndex++] = u + w2 + (float) Math.sin(angle) * w2;
            // vertices[verticesIndex++] = v + h2 + (float) Math.cos(angle) * h2;
            triangles[triangleIndex++] = 0;
            if (edge < edges - 1) {
                triangles[triangleIndex++] = (short) (edge + 2);
            } else {
                triangles[triangleIndex++] = (short) (1);
            }
            triangles[triangleIndex++] = (short) (edge + 1);
        }
        // for ( int veri = 0; veri < vertices.length; veri += VERTEX_SIZE )
        // {
        // System.out.printf( "%f, %f, %f, %f, %f\n", vertices[veri], vertices[veri +
        // 1], vertices[veri + 2], vertices[veri + 3], vertices[veri + 4] );
        // }
        // for ( int trii = 0; trii < triangles.length; trii += 3 )
        // {
        // System.out.printf( "%d, %d, %d\n", triangles[trii], triangles[trii + 1],
        // triangles[trii + 2] );
        // }
        final float width  = (radius * 2 + 1);
        final float height = (radius * 2 + 1);
        drawVertices(region, vertices, triangles, x - shiftX * (width / region.getRegionWidth()), y - shiftY * (height / region.getRegionHeight()), (radius * 2 + 1), (radius * 2 + 1));
        /*
         * if ( !drawing ) throw new IllegalStateException( "SpriteBatch.begin must be called before draw." ); float[] vertices = this.vertices; Texture texture = region.texture; if ( texture != lastTexture ) {
         * switchTexture( texture ); } else if ( idx + edges * 4 * 5 >= vertices.length ) // flush(); final float u = region.u; final float v = region.v2; final float u2 = region.u2; final float v2 = region.v; float
         * color = this.color; int idx = this.idx; for ( int edge = 0; edge < edges; edge++ ) { float angle = (float)( edge * Math.PI * 2 / edges ); float nextAngle = (float)( ( edge + 1 ) * Math.PI * 2 / edges );
         * //---Center vertices[idx++] = x; vertices[idx++] = y; vertices[idx++] = color; vertices[idx++] = u; vertices[idx++] = v; vertices[idx++] = x + (float)Math.sin( angle ) * radius; vertices[idx++] = y +
         * (float)Math.cos( angle ) * radius; vertices[idx++] = color; vertices[idx++] = u; vertices[idx++] = v2; vertices[idx++] = x + (float)Math.sin( nextAngle + 0.05 ) * radius; vertices[idx++] = y + (float)Math.cos(
         * nextAngle + 0.05 ) * radius; vertices[idx++] = color; vertices[idx++] = u2; vertices[idx++] = v2; vertices[idx++] = x; vertices[idx++] = y; vertices[idx++] = color; vertices[idx++] = u; vertices[idx++] = v; }
         * this.idx = idx;
         */
    }

    public void fillPie(final TextureRegion region, final float x, final float y, final float startRadius, final float endRadius, final float startAngle, final float endAngle, final int edges) {
        if (!isDrawing())
            throw new IllegalStateException("SpriteBatch.begin must be called before draw.");
        // float border = 0.02f;//---between this and the next pie piece
        // float overlapping = 0.03f;//---used to ensure no artifact are visible where the squares overlap
        // edges = (int)Math.min( edges, ( endAngle - border - startAngle ) / overlapping );
        final float[] vertices      = new float[(edges + 1) * 2 * 2];
        final short[] triangles     = new short[(edges + 1) * 2 * 3];
        int           verticesIndex = 0;
        int           triangleIndex = 0;
        // ---Center point
        // final float u = region.getU();
        // final float v = region.getV2();
        // final float u2 = region.getU2();
        // final float v2 = region.getV();
        // float w2 = (u2 - u) / 2;
        // float h2 = (v2 - v) / 2;
        // float color = this.getPackedColor();
        final float shiftX = region.getRegionWidth() / 2;
        final float shiftY = region.getRegionHeight() / 2;
        for (int edge = 0; edge <= edges; edge++) {
            final float angle = startAngle + edge * (endAngle - startAngle) / edges;
            // float nextAngle = startAngle + ( edge + 1 ) * ( endAngle - startAngle ) / edges;
            // float nextAngle = startAngle + ( edge + 1 ) * ( endAngle - startAngle ) /
            // edges;
            // ---vertice 0
            vertices[verticesIndex++] = shiftX + (float) Math.sin(angle) * (region.getRegionWidth() / 2) * (startRadius / endRadius);
            vertices[verticesIndex++] = shiftY + (float) Math.cos(angle) * (region.getRegionHeight() / 2) * (startRadius / endRadius);
            // vertices[verticesIndex++] = color;
            // vertices[verticesIndex++] = u + w2 + (float) Math.sin(angle) * w2 / 2;
            // vertices[verticesIndex++] = v + h2 + (float) Math.cos(angle) * h2 / 2;
            // ---vertice 1
            vertices[verticesIndex++] = shiftX + (float) Math.sin(angle) * region.getRegionWidth() / 2;
            vertices[verticesIndex++] = shiftY + (float) Math.cos(angle) * region.getRegionHeight() / 2;
            // vertices[verticesIndex++] = color;
            // vertices[verticesIndex++] = u + w2 + (float) Math.sin(angle) * w2;
            // vertices[verticesIndex++] = v + h2 + (float) Math.cos(angle) * h2;
            // triangle 0-3-1
            // triangle 2-5-3
            // triangle 4-7-5
            // triangle 6-1-7
            triangles[triangleIndex++] = (short) (edge * 2);
            if (edge < edges) {
                triangles[triangleIndex++] = (short) (edge * 2 + 3);
            }
            // else
            // {
            // triangles[triangleIndex++] = (short)1;
            // }
            triangles[triangleIndex++] = (short) (edge * 2 + 1);
            // triangle 2-3-0
            // triangle 4-5-2
            // triangle 6-7-4
            // triangle 0-1-6
            if (edge < edges) {
                triangles[triangleIndex++] = (short) (edge * 2 + 2);
                triangles[triangleIndex++] = (short) (edge * 2 + 3);
            }
            // else
            // {
            // triangles[triangleIndex++] = (short)( 0 );
            // triangles[triangleIndex++] = (short)( 1 );
            // }
            triangles[triangleIndex++] = (short) (edge * 2 + 0);
        }
        final float width  = (endRadius * 2 + 1);
        final float height = (endRadius * 2 + 1);
        drawVertices(region, vertices, triangles, x - shiftX * (width / region.getRegionWidth()), y - shiftY * (height / region.getRegionHeight()), width, height);

        /*
         * float border = 0.02f;//---between this and the next pie piece float overlapping = 0.03f;//---used to ensure no artifact are visible where the squares overlap edges = (int)Math.min( edges, ( endAngle - border -
         * startAngle ) / overlapping ); if ( !drawing ) throw new IllegalStateException( "SpriteBatch.begin must be called before draw." ); float[] vertices = this.vertices; Texture texture = region.texture; if (
         * texture != lastTexture ) { switchTexture( texture ); } else if ( idx + edges * 4 * 5 >= vertices.length ) // flush(); final float u = region.u; final float v = region.v2; final float u2 = region.u2; final
         * float v2 = region.v; float color = this.color; int idx = this.idx; endAngle -= border; for ( int edge = 0; edge < edges; edge++ ) { float angle = startAngle + edge * ( endAngle - startAngle ) / edges; float
         * nextAngle = startAngle + ( edge + 1 ) * ( endAngle - startAngle ) / edges; //---Center vertices[idx++] = x + (float)Math.sin( angle ) * startRadius; vertices[idx++] = y + (float)Math.cos( angle ) *
         * startRadius; vertices[idx++] = color; vertices[idx++] = u; vertices[idx++] = v; vertices[idx++] = x + (float)Math.sin( angle ) * endRadius; vertices[idx++] = y + (float)Math.cos( angle ) * endRadius;
         * vertices[idx++] = color; vertices[idx++] = u; vertices[idx++] = v2; if ( edge == edges - 1 ) { overlapping = 0.0f; } vertices[idx++] = x + (float)Math.sin( nextAngle + overlapping ) * endRadius;
         * vertices[idx++] = y + (float)Math.cos( nextAngle + overlapping ) * endRadius; vertices[idx++] = color; vertices[idx++] = u2; vertices[idx++] = v2; vertices[idx++] = x + (float)Math.sin( nextAngle + overlapping
         * ) * startRadius; vertices[idx++] = y + (float)Math.cos( nextAngle + overlapping ) * startRadius; vertices[idx++] = color; vertices[idx++] = u; vertices[idx++] = v; } this.idx = idx;
         */
    }

    public void line(final TextureRegion texture, final float x1, final float y1, final float z1, final float x2, final float y2, final float z2, final Color color, final float aThickness) {
        // the center of your hand
        final Vector3 center = new Vector3(x1, y1, z1);
        // you need a vector from the center to your touchpoint
        final Vector3 touchPoint = new Vector3(x2, y2, z2);
        touchPoint.sub(center);
        // now convert into polar angle
        double rotation = Math.atan2(touchPoint.z, touchPoint.x);
        // rotation should now be between -PI and PI
        // so scale to 0..1
        rotation = (rotation + Math.PI) / (Math.PI * 2);
        // SpriteBatch.draw needs degrees
        rotation *= 360;
        // add Offset because of reasons
        rotation += 90;
        setColor(color);
        draw(texture, x1, // x, center of rotation
                z1, // y, center of rotation
                aThickness / 2, // origin x in the texture region
                0, // origin y in the texture region
                aThickness, // width
                touchPoint.len(), // height
                1.0f, // scale x
                1.0f, // scale y
                (float) rotation);
    }
}
