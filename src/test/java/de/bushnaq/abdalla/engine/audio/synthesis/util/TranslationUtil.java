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
import de.bushnaq.abdalla.engine.audio.synthesis.Synthesizer;
import de.bushnaq.abdalla.engine.util.ModelCreator;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class TranslationUtil<T extends Translation> extends BasicGameEngine {
    public static final    int                               MAX_ENGINE_SPEED  = 20;
    public static final    int                               MIN_ENGINE_SPEED  = 2;
    protected static final float                             CUBE_SIZE         = 64;
    private static final   Color                             CUBE_NAME_COLOR   = Color.WHITE;
    private static final   float                             MAX_CITY_SIZE     = 3000f + CUBE_SIZE;
    private static         Color                             DIAMON_BLUE_COLOR = new Color(0x006ab6ff);
    private static         Color                             GRAY_COLOR        = new Color(0x404853ff);
    private static         Color                             POST_GREEN_COLOR  = new Color(0x00614eff);
    private static         Color                             SCARLET_COLOR     = new Color(0xb00233ff);
    protected final        List<GameObject<BasicGameEngine>> gameObjects       = new ArrayList<>();
    protected final        List<T>                           translation       = new ArrayList<>();
    //    private final          boolean                           bassBoost         = true;
    private final          Logger                            logger            = LoggerFactory.getLogger(this.getClass());
    private final          boolean                           simulatePauses    = true;
    private final          List<Synthesizer>                 synths            = new ArrayList<>();
    protected              long                              runFor            = 30000;//ms
    private                GameObject<BasicGameEngine>       buildingGameObject;
    private                Model                             buildingModel;
    private                GameObject<BasicGameEngine>       cityGameObject;
    private                Model                             cityModel;
    private                long                              last              = 0;
    private                boolean                           play              = false;
    private                long                              time1;

    public static Color getColor(final int index) {
        switch (index % 4) {
            case 0:
                return POST_GREEN_COLOR;
            case 1:
                return SCARLET_COLOR;
            case 2:
                return DIAMON_BLUE_COLOR;
            case 3:
                return GRAY_COLOR;
            case -1:
                return Color.WHITE;//we are not transporting any good
            default:
                return Color.WHITE;
        }
    }

    public void create(final int numberOfSources) {
        super.create();
        try {
            createCube();
            buildingGameObject = new GameObject<>(new ModelInstanceHack(buildingModel), null);
            buildingGameObject.instance.transform.setToTranslationAndScaling(0, CUBE_SIZE / 2, 0, CUBE_SIZE, CUBE_SIZE, CUBE_SIZE);
            final PointLight light = new PointLight().set(Color.WHITE, 0, CUBE_SIZE * 2, 0, 10000f);
            getRenderEngine().add(light, true);
            getRenderEngine().addStatic(buildingGameObject);
            cityGameObject = new GameObject<>(new ModelInstanceHack(cityModel), null);
            cityGameObject.instance.transform.setToTranslationAndScaling(0, -CUBE_SIZE / 2, 0, MAX_CITY_SIZE, CUBE_SIZE, MAX_CITY_SIZE);
            getRenderEngine().addStatic(cityGameObject);
            for (int l = 0; l < numberOfSources; l++) {
                final GameObject<BasicGameEngine> go = new GameObject<>(new ModelInstanceHack(createCube(getColor(l))), null);
                gameObjects.add(go);
                getRenderEngine().addDynamic(go);
                final Synthesizer synth = getAudioEngine().createAudioProducer(ExampleSynthesizer.class);
                synth.play();
                synths.add(synth);
            }
            time1 = System.currentTimeMillis();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
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

    private Model createCube(final Color c) {
        final ModelCreator modelCreator = new ModelCreator();
        final Attribute    color        = new PBRColorAttribute(PBRColorAttribute.BaseColorFactor, c);
        final Attribute    metallic     = PBRFloatAttribute.createMetallic(0.5f);
        final Attribute    roughness    = PBRFloatAttribute.createRoughness(0.2f);
        final Material     material     = new Material(metallic, roughness, color);
        return modelCreator.createBox(material);
    }

    @Override
    public void dispose() {
        //		try {
        //			for (final Synthesizer synth : synths) {
        //				synth.dispose();
        //			}
        //		} catch (final Exception e) {
        //			logger.error(e.getMessage(), e);
        //		}
        super.dispose();
    }

    @Override
    protected void renderText() {
        for (int i = 0; i < gameObjects.size(); i++) {
            final Translation t = translation.get(i);
            renderTextOnTop(t.position.x, t.position.y, t.position.z, 0, 0, "" + i, CUBE_SIZE);
            renderTextOnTop(t.position.x, t.position.y, t.position.z, -(CUBE_SIZE / 2 - CUBE_SIZE / 8), -(CUBE_SIZE / 2 - CUBE_SIZE / 16), "" + (int) t.velocity.x, CUBE_SIZE / 4);
            renderTextOnTop(t.position.x, t.position.y, t.position.z, (CUBE_SIZE / 2 - CUBE_SIZE / 8), -(CUBE_SIZE / 2 - CUBE_SIZE / 16), "" + (int) t.velocity.z, CUBE_SIZE / 4);
            {
                //				float gain = ((float) ((int) (synths.get(i).getGain() * 10))) / 10;
                //				renderTextOnTop(t.position.x, t.position.y, t.position.z, 0, -(CUBE_SIZE / 2 - CUBE_SIZE / 16), "" + Float.toString(gain), CUBE_SIZE / 4);
            }
            {
                final float bassGain       = 1 - (t.velocity.len() - MIN_ENGINE_SPEED) / (MAX_ENGINE_SPEED - MIN_ENGINE_SPEED);
                float       actualBassGain = bassGain * (48 + 24);
                actualBassGain = ((float) ((int) (actualBassGain * 10))) / 10;
                renderTextOnTop(t.position.x, t.position.y, t.position.z, 0, -(CUBE_SIZE / 2 - CUBE_SIZE / 16), "" + Float.toString(actualBassGain), CUBE_SIZE / 4);
            }
        }
    }

    //	public void renderText(final float aX, final float aY, final float aZ, final int index) {
    //		final float size = 48;
    //		final float x = aX;
    //		final float y = aY;
    //		final float z = aZ;
    //		final String text = "" + index;
    //		//draw text
    //		final PolygonSpriteBatch batch = sceneManager.batch2D;
    //		final BitmapFont font = sceneManager.getAtlasManager().modelFont;
    //		{
    //			final Matrix4 m = new Matrix4();
    //			final float fontSize = font.getLineHeight();
    //			final GlyphLayout layout = new GlyphLayout();
    //			layout.setText(font, text);
    //			final float width = layout.width;// contains the width of the current set text
    //			final float height = layout.height; // contains the height of the current set text
    //
    //			final float scaling = size / fontSize;
    //			final Vector3 xVector = new Vector3(1, 0, 0);
    //			final Vector3 yVector = new Vector3(0, 1, 0);
    //			//			final Vector3 zVector = new Vector3(0, 0, 1);
    //			//			m.setToTranslationAndScaling(x - width * scaling / 2, y + height * scaling / 2, z + CUBE_SIZE / 2 + 1, scaling, scaling, 1f);
    //			m.setToTranslation(x - height * scaling / 2, y + CUBE_SIZE / 2 + 1, z + width * scaling / 2);
    //			//			m.setToTranslation(x - size / 3, y, z + size / 5);
    //			m.rotate(yVector, 90);
    //			m.rotate(xVector, -90);
    //			m.scale(scaling, scaling, 1f);
    //			batch.setTransformMatrix(m);
    //			font.setColor(CUBE_NAME_COLOR);
    //			font.draw(batch, text, 0, 0);
    //		}
    //	}

    @Override
    protected void update() throws Exception {
        updateTranslation();
        for (int i = 0; i < gameObjects.size(); i++) {
            final Translation                 t        = translation.get(i);
            final GameObject<BasicGameEngine> go       = gameObjects.get(i);
            final Synthesizer                 synth    = synths.get(i);
            final float[]                     position = new float[]{t.position.x, t.position.y, t.position.z};
            final float[]                     velocity = new float[]{t.velocity.x, 0, t.velocity.z};
            synth.setPositionAndVelocity(position, velocity);
            if (simulatePauses) {
                if (System.currentTimeMillis() - last > 2000) {
//                    play = !play;
                    if (play) {
//                        logger.info("play");
                        synth.play();
                    } else {
//                        logger.info("pause");
                        synth.pause();
                    }
                    last = System.currentTimeMillis();
                }
            }
            if (isSimulateBassBoost()) {
                synth.setBassBoost(isSimulateBassBoost());
            } else {
                synth.setBassBoost(isSimulateBassBoost());
            }
            //				if (System.currentTimeMillis() - last > 2000) {
            //					bassBoost = !bassBoost;
            //					if (bassBoost) {
            //						logger.info("bassBoost on");
            //		synth.setBassBoost(bassBoost);
            //					} else {
            //						logger.info("bassBoost off");
            //						synth.setBassBoost(bassBoost);
            //					}
            //					last = System.currentTimeMillis();
            //				}
            go.instance.transform.setToTranslationAndScaling(t.position.x, t.position.y, t.position.z, CUBE_SIZE, CUBE_SIZE, CUBE_SIZE);

        }
        if (System.currentTimeMillis() - time1 > runFor) Gdx.app.exit();

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

    protected abstract void updateTranslation();

}