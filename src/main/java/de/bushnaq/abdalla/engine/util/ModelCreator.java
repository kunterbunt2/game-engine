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

package de.bushnaq.abdalla.engine.util;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;

public class ModelCreator {
    public Model createBox(final Material material) {
        final ModelBuilder builder = new ModelBuilder();
        builder.begin();
        final PBRTextureAttribute ta1 = (PBRTextureAttribute) material.get(PBRTextureAttribute.BaseColorTexture);
        if (ta1 != null) {
            final PBRTextureAttribute ta2 = (PBRTextureAttribute) material.get(PBRTextureAttribute.MetallicRoughnessTexture);
            builder.part("1", createQuad(ta1.offsetU, ta1.offsetV, ta1.scaleU + ta1.offsetU, ta1.scaleV + ta1.offsetV, ta2.offsetU, ta2.offsetV, ta2.scaleU + ta2.offsetU, ta2.scaleV + ta2.offsetV), GL20.GL_TRIANGLES, material);
        } else {
            builder.part("1", createQuad(), GL20.GL_TRIANGLES, material);
        }
        final Model model = builder.end();
        return model;
    }

    public Model createBox(final TextureRegion textureRegion, final Material material, final int i) {
        final ModelBuilder builder = new ModelBuilder();
        builder.begin();
        builder.part("1", createQuad(textureRegion), GL20.GL_TRIANGLES, material);
        final Model model = builder.end();
        return model;
    }

    public Mesh createQuad() {
        final Mesh quad = new Mesh(true, 24, 36, VertexAttribute.Position(), VertexAttribute.Normal());

        final float[] vertices = new float[]{-0.500000f, -0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, // 0
                -0.500000f, 0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, // 1
                0.500000f, 0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, // 2
                0.500000f, -0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, // 3
                -0.500000f, 0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, // 4
                -0.500000f, -0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, // 5
                0.500000f, -0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, // 6
                0.500000f, 0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, // 7
                -0.500000f, -0.500000f, 0.500000f, 0.000000f, -1.000000f, 0.000000f, // 8
                -0.500000f, -0.500000f, -0.500000f, 0.000000f, -1.000000f, 0.000000f, // 9
                0.500000f, -0.500000f, -0.500000f, 0.000000f, -1.000000f, 0.000000f, // 10
                0.500000f, -0.500000f, 0.500000f, 0.000000f, -1.000000f, 0.000000f, // 11
                -0.500000f, 0.500000f, -0.500000f, -0.000000f, 1.000000f, -0.000000f, // 12
                -0.500000f, 0.500000f, 0.500000f, -0.000000f, 1.000000f, -0.000000f, // 13
                0.500000f, 0.500000f, 0.500000f, -0.000000f, 1.000000f, -0.000000f, // 14
                0.500000f, 0.500000f, -0.500000f, -0.000000f, 1.000000f, -0.000000f, // 15
                -0.500000f, -0.500000f, 0.500000f, -1.000000f, 0.000000f, 0.000000f, // 16
                -0.500000f, 0.500000f, 0.500000f, -1.000000f, 0.000000f, 0.000000f, // 17
                -0.500000f, 0.500000f, -0.500000f, -1.000000f, 0.000000f, 0.000000f, // 18
                -0.500000f, -0.500000f, -0.500000f, -1.000000f, 0.000000f, 0.000000f, // 19
                0.500000f, -0.500000f, -0.500000f, 1.000000f, -0.000000f, -0.000000f, // 20
                0.500000f, 0.500000f, -0.500000f, 1.000000f, -0.000000f, -0.000000f, // 21
                0.500000f, 0.500000f, 0.500000f, 1.000000f, -0.000000f, -0.000000f, // 22
                0.500000f, -0.500000f, 0.500000f, 1.000000f, -0.000000f, -0.000000f, // 23
        };
        quad.setVertices(vertices);

        final short[] indixes = new short[]{0, 1, 2, // 0
                2, 3, 0, // 1
                4, 5, 6, // 2
                6, 7, 4, // 3
                8, 9, 10, // 4
                10, 11, 8, // 5
                12, 13, 14, // 6
                14, 15, 12, // 7
                16, 17, 18, // 8
                18, 19, 16, // 9
                20, 21, 22, // 10
                22, 23, 20,// 11
        };
        quad.setIndices(indixes);

        return quad;
    }

    public Mesh createQuad(final float u1, final float v1, final float u21, final float v21, final float u2, final float v2, final float u22, final float v22) {
        final Mesh quad = new Mesh(true, 24, 36, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0), VertexAttribute.TexCoords(0));

        //		float[] vertices = new float[] { -0.500000f, -0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, u, v2, // 0
        //				-0.500000f, 0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, u2, v2, // 1
        //				0.500000f, 0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, u2, v, // 2
        //				0.500000f, -0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, u, v, // 3
        //				-0.500000f, 0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, u, v2, // 4
        //				-0.500000f, -0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, u2, v2, // 5
        //				0.500000f, -0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, u2, v, // 6
        //				0.500000f, 0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, u, v, // 7
        //				-0.500000f, -0.500000f, 0.500000f, 0.000000f, -1.000000f, 0.000000f, u, v2, // 8
        //				-0.500000f, -0.500000f, -0.500000f, 0.000000f, -1.000000f, 0.000000f, u2, v2, // 9
        //				0.500000f, -0.500000f, -0.500000f, 0.000000f, -1.000000f, 0.000000f, u2, v, // 10
        //				0.500000f, -0.500000f, 0.500000f, 0.000000f, -1.000000f, 0.000000f, u, v, // 11
        //				-0.500000f, 0.500000f, -0.500000f, -0.000000f, 1.000000f, -0.000000f, u, v2, // 12
        //				-0.500000f, 0.500000f, 0.500000f, -0.000000f, 1.000000f, -0.000000f, u2, v2, // 13
        //				0.500000f, 0.500000f, 0.500000f, -0.000000f, 1.000000f, -0.000000f, u2, v, // 14
        //				0.500000f, 0.500000f, -0.500000f, -0.000000f, 1.000000f, -0.000000f, u, v, // 15
        //				-0.500000f, -0.500000f, 0.500000f, -1.000000f, 0.000000f, 0.000000f, u, v2, // 16
        //				-0.500000f, 0.500000f, 0.500000f, -1.000000f, 0.000000f, 0.000000f, u2, v2, // 17
        //				-0.500000f, 0.500000f, -0.500000f, -1.000000f, 0.000000f, 0.000000f, u2, v, // 18
        //				-0.500000f, -0.500000f, -0.500000f, -1.000000f, 0.000000f, 0.000000f, u, v, // 19
        //				0.500000f, -0.500000f, -0.500000f, 1.000000f, -0.000000f, -0.000000f, u, v2, // 20
        //				0.500000f, 0.500000f, -0.500000f, 1.000000f, -0.000000f, -0.000000f, u2, v2, // 21
        //				0.500000f, 0.500000f, 0.500000f, 1.000000f, -0.000000f, -0.000000f, u2, v, // 22
        //				0.500000f, -0.500000f, 0.500000f, 1.000000f, -0.000000f, -0.000000f, u, v,// 23
        //		};
        final float[] vertices = new float[]{-0.500000f, -0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, u1, v21, u2, v22, // 0
                -0.500000f, 0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, u21, v21, u22, v22, // 1
                0.500000f, 0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, u21, v1, u22, v2, // 2
                0.500000f, -0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, u1, v1, u2, v2, // 3
                -0.500000f, 0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, u1, v21, u2, v22, // 4
                -0.500000f, -0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, u21, v21, u22, v22, // 5
                0.500000f, -0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, u21, v1, u22, v2, // 6
                0.500000f, 0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, u1, v1, u2, v2, // 7
                -0.500000f, -0.500000f, 0.500000f, 0.000000f, -1.000000f, 0.000000f, u1, v21, u2, v22, // 8
                -0.500000f, -0.500000f, -0.500000f, 0.000000f, -1.000000f, 0.000000f, u21, v21, u22, v22, // 9
                0.500000f, -0.500000f, -0.500000f, 0.000000f, -1.000000f, 0.000000f, u21, v1, u22, v2, // 10
                0.500000f, -0.500000f, 0.500000f, 0.000000f, -1.000000f, 0.000000f, u1, v1, u2, v2, // 11
                -0.500000f, 0.500000f, -0.500000f, -0.000000f, 1.000000f, -0.000000f, u1, v21, u2, v22, // 12
                -0.500000f, 0.500000f, 0.500000f, -0.000000f, 1.000000f, -0.000000f, u21, v21, u22, v22, // 13
                0.500000f, 0.500000f, 0.500000f, -0.000000f, 1.000000f, -0.000000f, u21, v1, u22, v2, // 14
                0.500000f, 0.500000f, -0.500000f, -0.000000f, 1.000000f, -0.000000f, u1, v1, u2, v2, // 15
                -0.500000f, -0.500000f, 0.500000f, -1.000000f, 0.000000f, 0.000000f, u1, v21, u2, v22, // 16
                -0.500000f, 0.500000f, 0.500000f, -1.000000f, 0.000000f, 0.000000f, u21, v21, u22, v22, // 17
                -0.500000f, 0.500000f, -0.500000f, -1.000000f, 0.000000f, 0.000000f, u21, v1, u22, v2, // 18
                -0.500000f, -0.500000f, -0.500000f, -1.000000f, 0.000000f, 0.000000f, u1, v1, u2, v2, // 19
                0.500000f, -0.500000f, -0.500000f, 1.000000f, -0.000000f, -0.000000f, u1, v21, u2, v22, // 20
                0.500000f, 0.500000f, -0.500000f, 1.000000f, -0.000000f, -0.000000f, u21, v21, u22, v22, // 21
                0.500000f, 0.500000f, 0.500000f, 1.000000f, -0.000000f, -0.000000f, u21, v1, u22, v2, // 22
                0.500000f, -0.500000f, 0.500000f, 1.000000f, -0.000000f, -0.000000f, u1, v1, u2, v2,// 23
        };
        quad.setVertices(vertices);

        final short[] indixes = new short[]{0, 1, 2, // 0
                2, 3, 0, // 1
                4, 5, 6, // 2
                6, 7, 4, // 3
                8, 9, 10, // 4
                10, 11, 8, // 5
                12, 13, 14, // 6
                14, 15, 12, // 7
                16, 17, 18, // 8
                18, 19, 16, // 9
                20, 21, 22, // 10
                22, 23, 20,// 11
        };
        quad.setIndices(indixes);

        return quad;
    }

    public Mesh createQuad(final TextureRegion region) {
        final Mesh quad = new Mesh(true, 24, 36, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0));

        final float[] vertices = new float[]{-0.500000f, -0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, region.getU(), region.getV2(), // 0
                -0.500000f, 0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, region.getU2(), region.getV2(), // 1
                0.500000f, 0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, region.getU2(), region.getV(), // 2
                0.500000f, -0.500000f, -0.500000f, 0.000000f, 0.000000f, -1.000000f, region.getU(), region.getV(), // 3
                -0.500000f, 0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, region.getU(), region.getV2(), // 4
                -0.500000f, -0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, region.getU2(), region.getV2(), // 5
                0.500000f, -0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, region.getU2(), region.getV(), // 6
                0.500000f, 0.500000f, 0.500000f, -0.000000f, -0.000000f, 1.000000f, region.getU(), region.getV(), // 7
                -0.500000f, -0.500000f, 0.500000f, 0.000000f, -1.000000f, 0.000000f, region.getU(), region.getV2(), // 8
                -0.500000f, -0.500000f, -0.500000f, 0.000000f, -1.000000f, 0.000000f, region.getU2(), region.getV2(), // 9
                0.500000f, -0.500000f, -0.500000f, 0.000000f, -1.000000f, 0.000000f, region.getU2(), region.getV(), // 10
                0.500000f, -0.500000f, 0.500000f, 0.000000f, -1.000000f, 0.000000f, region.getU(), region.getV(), // 11
                -0.500000f, 0.500000f, -0.500000f, -0.000000f, 1.000000f, -0.000000f, region.getU(), region.getV2(), // 12
                -0.500000f, 0.500000f, 0.500000f, -0.000000f, 1.000000f, -0.000000f, region.getU2(), region.getV2(), // 13
                0.500000f, 0.500000f, 0.500000f, -0.000000f, 1.000000f, -0.000000f, region.getU2(), region.getV(), // 14
                0.500000f, 0.500000f, -0.500000f, -0.000000f, 1.000000f, -0.000000f, region.getU(), region.getV(), // 15
                -0.500000f, -0.500000f, 0.500000f, -1.000000f, 0.000000f, 0.000000f, region.getU(), region.getV2(), // 16
                -0.500000f, 0.500000f, 0.500000f, -1.000000f, 0.000000f, 0.000000f, region.getU2(), region.getV2(), // 17
                -0.500000f, 0.500000f, -0.500000f, -1.000000f, 0.000000f, 0.000000f, region.getU2(), region.getV(), // 18
                -0.500000f, -0.500000f, -0.500000f, -1.000000f, 0.000000f, 0.000000f, region.getU(), region.getV(), // 19
                0.500000f, -0.500000f, -0.500000f, 1.000000f, -0.000000f, -0.000000f, region.getU(), region.getV2(), // 20
                0.500000f, 0.500000f, -0.500000f, 1.000000f, -0.000000f, -0.000000f, region.getU2(), region.getV2(), // 21
                0.500000f, 0.500000f, 0.500000f, 1.000000f, -0.000000f, -0.000000f, region.getU2(), region.getV(), // 22
                0.500000f, -0.500000f, 0.500000f, 1.000000f, -0.000000f, -0.000000f, region.getU(), region.getV(),// 23
        };
        quad.setVertices(vertices);

        final short[] indixes = new short[]{0, 1, 2, // 0
                2, 3, 0, // 1
                4, 5, 6, // 2
                6, 7, 4, // 3
                8, 9, 10, // 4
                10, 11, 8, // 5
                12, 13, 14, // 6
                14, 15, 12, // 7
                16, 17, 18, // 8
                18, 19, 16, // 9
                20, 21, 22, // 10
                22, 23, 20,// 11
        };
        quad.setIndices(indixes);

        return quad;
    }

    public void print(final Model model) {

        for (final Mesh mesh : model.meshes) {
            final int maxVertices = mesh.getMaxVertices();
            final int maxIndices  = mesh.getMaxIndices();
            System.out.printf("maxVertices=%d\n", maxVertices);
            System.out.printf("maxIndices=%d\n", maxIndices);
            {
                final int numVertices = mesh.getNumVertices();
                final int vertexSize  = mesh.getVertexSize();

                System.out.printf("numVertices=%d\n", numVertices);
                System.out.printf("vertexSize=%d\n", vertexSize);

                final float[] vertices = new float[numVertices * vertexSize / 4];
                mesh.getVertices(vertices);
                int index       = 0;
                int vertixCount = 0;
                for (int vertex = 0; vertex < vertices.length; vertex++) {
                    if (index == (vertexSize / 4) - 2) {
                        final float u = vertices[vertex];
                        if (u == 0.0f) {
                            System.out.printf(" %s,", "region.getU()");
                        } else if (u == 1.0f) {
                            System.out.printf(" %s,", "region.getU2()");
                        }
                    } else if (index == (vertexSize / 4) - 1) {
                        final float v = vertices[vertex];
                        if (v == 0.0f) {
                            System.out.printf(" %s,", "region.getV()");
                        } else if (v == 1.0f) {
                            System.out.printf(" %s,", "region.getV2()");
                        }
                    } else {
                        System.out.printf(" %ff,", vertices[vertex]);
                    }
                    if (++index == vertexSize / 4) {
                        System.out.printf("//%d\n", vertixCount++);
                        index = 0;
                    }
                }
            }
            {
                final int numIndices = mesh.getNumIndices();
                System.out.printf("numIndices=%d\n", numIndices);
                final short[] indexes = new short[numIndices];
                mesh.getIndices(indexes);
                int index      = 0;
                int indexCount = 0;
                for (int i = 0; i < indexes.length; i++) {
                    System.out.printf(" %d,", indexes[i]);
                    if (++index == 3) {
                        System.out.printf("//%d\n", indexCount++);
                        index = 0;
                    }
                }
            }
        }

        for (final MeshPart meshPart : model.meshParts) {
        }
    }

}
