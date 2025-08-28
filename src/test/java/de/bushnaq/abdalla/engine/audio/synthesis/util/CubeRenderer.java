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
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import de.bushnaq.abdalla.engine.GameObject;
import de.bushnaq.abdalla.engine.ObjectRenderer;
import de.bushnaq.abdalla.engine.RenderEngine3D;
import de.bushnaq.abdalla.engine.ai.PromptTags;
import de.bushnaq.abdalla.engine.audio.OggPlayer;
import de.bushnaq.abdalla.engine.audio.OpenAlException;
import de.bushnaq.abdalla.engine.audio.radio.RadioChannel;
import de.bushnaq.abdalla.engine.audio.radio.RadioMessage;
import de.bushnaq.abdalla.engine.audio.radio.RadioWave;
import de.bushnaq.abdalla.engine.audio.radio.TTSPlayer;
import de.bushnaq.abdalla.engine.audio.synthesis.Synthesizer;
import de.bushnaq.abdalla.engine.event.IEventManager;
import de.bushnaq.abdalla.engine.util.ModelCreator;
import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.bushnaq.abdalla.engine.audio.synthesis.util.TranslationUtil.CUBE_NAME_COLOR;

public class CubeRenderer extends ObjectRenderer<BasicGameEngine> implements RadioChannel {

    protected static final float                       CUBE_SIZE                   = 64;
    private static final   Color                       DIAMON_BLUE_COLOR           = new Color(0x006ab6ff);
    private static final   Color                       GRAY_COLOR                  = new Color(0x404853ff);
    public static final    int                         MAX_ENGINE_SPEED            = 20;
    protected static final float                       MAX_GRID_SIZE               = 1000f;
    public static final    int                         MIN_ENGINE_SPEED            = 2;
    private static final   Color                       POST_GREEN_COLOR            = new Color(0x00614eff);
    private static final   String                      REQUESTING_APPROVAL_TO_DOCK = "REQUESTING_APPROVAL_TO_DOCK";
    private static final   Color                       SCARLET_COLOR               = new Color(0xb00233ff);
    private final static   BasicRandomGenerator        randomGenerator             = new BasicRandomGenerator(1);
    private final          CubeActor                   cube;
    private                GameObject<BasicGameEngine> go;
    private final          float[]                     lastPositionArray           = new float[3];
    private final          float[]                     lastVelocityArray           = new float[3];
    private final          Logger                      logger                      = LoggerFactory.getLogger(this.getClass());
    protected final        int                         mode;
    private                OggPlayer                   oggPlayer;
    protected              Vector3                     origin                      = new Vector3();
    protected              Vector3                     position                    = new Vector3();
    private final          float[]                     positionArray               = new float[3];
    private                Synthesizer                 synth;
    private final          SynthType                   synthType;
    private                TTSPlayer                   ttsPlayer;
    protected              Vector3                     velocity                    = new Vector3();
    private final          float[]                     velocityArray               = new float[3];

    public CubeRenderer(CubeActor cube, int mode, SynthType synthType) {
        super();
        this.cube      = cube;
        this.mode      = mode;
        this.synthType = synthType;
    }

    @Override
    public void create(final RenderEngine3D<BasicGameEngine> renderEngine) {
        try {
            go = new GameObject<>(new ModelInstanceHack(createCube(getColor(cube.index))), null, this);
            renderEngine.addDynamic(go);
            switch (synthType) {
                case SYNTH -> {
                    synth = renderEngine.getGameEngine().getAudioEngine().createAudioProducer(ExampleSynthesizer.class, "synth");
                    synth.setGain(100.0f);
                    synth.play();
                }
                case AMBIENT_OGG -> {
                    oggPlayer = renderEngine.getGameEngine().getAudioEngine().createAudioProducer(OggPlayer.class, "ambient");
                    oggPlayer.setFile(Gdx.files.internal(BasicAtlasManager.getAssetsFolderName() + "/audio/bass-dropmp3.ogg"));
                    oggPlayer.setGain(150.0f);
                    oggPlayer.setAmbient(true);
                }
                case OGG -> {
                    oggPlayer = renderEngine.getGameEngine().getAudioEngine().createAudioProducer(OggPlayer.class, "ogg");
                    oggPlayer.setFile(Gdx.files.internal(BasicAtlasManager.getAssetsFolderName() + "/audio/bass-dropmp3.ogg"));
                    oggPlayer.setGain(1050.0f);
                    oggPlayer.setLoop(true);
                    oggPlayer.setAmbient(false);
                }
                case TTS -> {
                    ttsPlayer = renderEngine.getGameEngine().getAudioEngine().createAudioProducer(TTSPlayer.class, "tts");
                    ttsPlayer.setGain(1.0f);
                    ttsPlayer.setAmbient(true);
                    ttsPlayer.setOptIn(true);
                    PromptTags tags = new PromptTags();
                    tags.addPreTag("ship", "T-38");
                    tags.addPreTag("station", "P-81");
                    String string = RadioMessage.createMessage(renderEngine.getGameEngine().getAudioEngine().radio.resolveString(REQUESTING_APPROVAL_TO_DOCK, tags, true), tags);
                    ttsPlayer.speak(new RadioMessage(0, this, null, "REQUESTING_APPROVAL_TO_UNDOCK", string, false, tags));
                }
            }
        } catch (final Exception e) {
            logger.info(e.getMessage(), e);
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

    @Override
    public IEventManager getEventManager() {
        return null;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getName() {
        return "P-" + (int) (Math.random() * 100);
    }

    public BasicRandomGenerator getRandomGenerator() {
        return randomGenerator;
    }

    @Override
    public boolean isSelected() {
        return true;//make sure we are heard
    }

    @Override
    public void notifyFinishedTalking(RadioMessage currentRadioMessage) {

    }

    @Override
    public void notifyStartedTalking(RadioWave message) {

    }

    @Override
    public void processRadioMessage(RadioMessage rm) {

    }

    @Override
    public void renderText(final RenderEngine3D<BasicGameEngine> renderEngine, final int index, final boolean selected) {
        renderTextOnTop(renderEngine, position.x, position.y, position.z, 0, 0, "" + cube.index, CUBE_SIZE);
        renderTextOnTop(renderEngine, position.x, position.y, position.z, -(CUBE_SIZE / 2 - CUBE_SIZE / 8), -(CUBE_SIZE / 2 - CUBE_SIZE / 16), "" + (int) velocity.x, CUBE_SIZE / 4);
        renderTextOnTop(renderEngine, position.x, position.y, position.z, (CUBE_SIZE / 2 - CUBE_SIZE / 8), -(CUBE_SIZE / 2 - CUBE_SIZE / 16), "" + (int) velocity.z, CUBE_SIZE / 4);
        {
            //				float gain = ((float) ((int) (synths.get(i).getGain() * 10))) / 10;
            //				renderTextOnTop(t.position.x, t.position.y, t.position.z, 0, -(CUBE_SIZE / 2 - CUBE_SIZE / 16), "" + Float.toString(gain), CUBE_SIZE / 4);
        }
        {
            final float bassGain       = 1 - (velocity.len() - MIN_ENGINE_SPEED) / (MAX_ENGINE_SPEED - MIN_ENGINE_SPEED);
            float       actualBassGain = bassGain * (48 + 24);
            actualBassGain = ((float) ((int) (actualBassGain * 10))) / 10;
            renderTextOnTop(renderEngine, position.x, position.y, position.z, 0, -(CUBE_SIZE / 2 - CUBE_SIZE / 16), String.valueOf(actualBassGain), CUBE_SIZE / 4);
        }

    }

    private void renderTextOnTop(RenderEngine3D<BasicGameEngine> renderEngine, final float aX, final float aY, final float aZ, final float dx, final float dy, final String text, final float size) {
        final float x = aX;
        final float y = aY;
        final float z = aZ;
        //draw text
//        final PolygonSpriteBatch batch = renderEngine.renderEngine2D.batch;
        final BitmapFont font = renderEngine.getGameEngine().getAtlasManager().modelFont;
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
            renderEngine.renderEngine25D.setTransformMatrix(m);
//            font.setColor(CUBE_NAME_COLOR);
//            font.draw(batch, text, 0, 0);
            renderEngine.renderEngine25D.text(0, 0, font, Color.BLACK, CUBE_NAME_COLOR, text);
        }
    }

    @Override
    public void select() throws OpenAlException {

    }

    @Override
    public void unselect() throws OpenAlException {

    }

    @Override
    public void update(final RenderEngine3D<BasicGameEngine> renderEngine, final long currentTime, final float timeOfDay, final int index, final boolean selected) throws Exception {
        go.instance.transform.setToTranslationAndScaling(position.x, position.y, position.z, CUBE_SIZE, CUBE_SIZE, CUBE_SIZE);
        positionArray[0] = position.x;
        positionArray[1] = position.y;
        positionArray[2] = position.z;
        velocityArray[0] = velocity.x;
        velocityArray[1] = velocity.y;
        velocityArray[2] = velocity.z;
        boolean update = false;
        for (int i = 0; i < 3; i++) {
            if (Math.abs(lastVelocityArray[i] - velocityArray[i]) > 0.001f) {
                update = true;
                break;
            }
            if (Math.abs(lastPositionArray[i] - positionArray[i]) > 10f) {
                update = true;
                break;
            }
        }
        switch (synthType) {
            case SYNTH -> {
                synth.play();
                if (update) {
                    synth.setPositionAndVelocity(positionArray, velocityArray);
                }
            }
            case AMBIENT_OGG, OGG -> {
                oggPlayer.play();
                if (update) {
                    oggPlayer.setPositionAndVelocity(positionArray, velocityArray);
                }
            }
            case TTS -> {
                ttsPlayer.play();
                if (update) {
                    ttsPlayer.setPositionAndVelocity(positionArray, velocityArray);
                }
            }
        }
        if (update) {
            System.arraycopy(velocityArray, 0, lastVelocityArray, 0, 3);
            System.arraycopy(positionArray, 0, lastPositionArray, 0, 3);
        }
    }

}
