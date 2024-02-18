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

package de.bushnaq.abdalla.engine.audio.synthesis.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import de.bushnaq.abdalla.engine.GameObject;
import de.bushnaq.abdalla.engine.util.ModelCreator;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TranslationUtil extends BasicGameEngine {
    public static final  Color  CUBE_NAME_COLOR  = Color.WHITE;
    public static final  float  CUBE_SIZE        = 64;
    public static final  int    MAX_ENGINE_SPEED = 20;
    public static final  int    MIN_ENGINE_SPEED = 2;
    private static final float  MAX_CITY_SIZE    = 3000f + CUBE_SIZE;
    private final        Logger logger           = LoggerFactory.getLogger(this.getClass());
    protected            long   runFor           = 30000;//ms
    private              Model  buildingModel;
    private              Model  cityModel;
    private              long   time1;

    public void create() {
        super.create();
        try {
            createCube();
            GameObject<BasicGameEngine> buildingGameObject = new GameObject<>(new ModelInstanceHack(buildingModel), null);
            buildingGameObject.instance.transform.setToTranslationAndScaling(0, CUBE_SIZE / 2, 0, CUBE_SIZE, CUBE_SIZE, CUBE_SIZE);
            final PointLight light = new PointLight().set(Color.WHITE, 0, CUBE_SIZE * 2, 0, 10000f);
            getRenderEngine().add(light, true);
            getRenderEngine().addStatic(buildingGameObject);
            GameObject<BasicGameEngine> cityGameObject = new GameObject<>(new ModelInstanceHack(cityModel), null);
            cityGameObject.instance.transform.setToTranslationAndScaling(0, -CUBE_SIZE / 2, 0, MAX_CITY_SIZE, CUBE_SIZE, MAX_CITY_SIZE);
            getRenderEngine().addStatic(cityGameObject);
            time1 = System.currentTimeMillis();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    protected void update() throws Exception {
        if (System.currentTimeMillis() - time1 > runFor) Gdx.app.exit();

    }

    private void createCube() {
        final ModelCreator modelCreator = new ModelCreator();
        {
            final Attribute color     = new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, Color.BLUE);
            final Attribute metallic  = PBRFloatAttribute.createMetallic(0.1f);
            final Attribute roughness = PBRFloatAttribute.createRoughness(0.5f);
            final Material  material  = new Material(metallic, roughness, color);
            buildingModel = modelCreator.createBox(material);
        }
        {
            final Attribute color     = new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, Color.DARK_GRAY);
            final Attribute metallic  = PBRFloatAttribute.createMetallic(0.10f);
            final Attribute roughness = PBRFloatAttribute.createRoughness(0.9f);
            final Material  material  = new Material(metallic, roughness, color);
            cityModel = modelCreator.createBox(material);
        }
    }

    private void renderTextOnTop(final float aX, final float aY, final float aZ, final float dx, final float dy, final String text, final float size) {
        final float x = aX;
        final float y = aY;
        final float z = aZ;
        //draw text
        final PolygonSpriteBatch batch = getRenderEngine().renderEngine2D.batch;
        final BitmapFont         font  = getAtlasManager().modelFont;
        {
            final Matrix4     m        = new Matrix4();
            final float       fontSize = font.getLineHeight();
            final float       scaling  = size / fontSize;
            final GlyphLayout layout   = new GlyphLayout();
            layout.setText(font, text);
            final float width  = layout.width;// contains the width of the current set text
            final float height = layout.height; // contains the height of the current set text
            //on top
            {
                final Vector3 xVector = new Vector3(1, 0, 0);
                final Vector3 yVector = new Vector3(0, 1, 0);
                m.setToTranslation(x - height * scaling / 2.0f - dy, y + CUBE_SIZE / 2.0f + 0.2f, z + width * scaling / 2.0f - dx);
                m.rotate(yVector, 90);
                m.rotate(xVector, -90);
                m.scale(scaling, scaling, 1f);

            }
            batch.setTransformMatrix(m);
            font.setColor(CUBE_NAME_COLOR);
            font.draw(batch, text, 0, 0);
        }
    }

}