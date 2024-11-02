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

package de.bushnaq.abdalla.engine.chronos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import de.bushnaq.abdalla.engine.IGameEngine;
import de.bushnaq.abdalla.engine.Text2D;
import de.bushnaq.abdalla.engine.util.ColorUtil;

import static de.bushnaq.abdalla.engine.chronos.ChronosPhase.EXECUTE;
import static de.bushnaq.abdalla.engine.chronos.ChronosPhase.START;

/**
 * Fades in {@link TextData textData} screen centered in fadeInSecond, waits durationSeconds, then fades out {@link TextData textData} in fadeOutSeconds.
 *
 * @param <T> GameEngine that implements IGameEngine
 */
public class ShowText<T extends IGameEngine> extends Task<T> {

    private final Color        backgroundColor;
    private       long         fadeInMs  = 500;
    private       long         fadeOutMs = 500;
    private final float        height;
    private       ChronosPhase mode      = START;
    private       Text2D       text2D;
    private final TextData     textData;
    private final float        width;

    public ShowText(T gameEngine, Color backgroundColor, TextData textData, float durationSeconds) {
        this(gameEngine, backgroundColor, textData, durationSeconds, .5f, .5f);
    }

    public ShowText(T gameEngine, Color backgroundColor, TextData textData, float durationSeconds, float fadeInSecond, float fadeOutSeconds) {
        super(gameEngine, durationSeconds);
        this.backgroundColor = backgroundColor;
        this.textData        = textData;
        this.fadeInMs        = (long) (fadeInSecond * 1000);
        this.fadeOutMs       = (long) (fadeOutSeconds * 1000);
        final GlyphLayout layout = new GlyphLayout();
        layout.setText(textData.font, textData.text);
        width  = layout.width;// contains the width of the current set textData
        height = layout.height;// contains the width of the current set textData
    }

    public boolean execute(float deltaTime) {
        boolean returnValue = false;
        switch (mode) {
            case EXECUTE -> {
                subExecute(deltaTime);
                if (System.currentTimeMillis() > taskStartTime + durationMs) {
                    logger.info("stop TextTask");
                    gameEngine.getRenderEngine().remove(text2D);
                    returnValue = true;
                }
            }
            case START -> {
                logger.info("start TextTask");
                mode          = EXECUTE;
                taskStartTime = System.currentTimeMillis();
                float x = Gdx.graphics.getWidth() / 2f - width / 2;
                float y = Gdx.graphics.getHeight() / 2f - height / 2;
                text2D = new Text2D(textData.text, x, y, backgroundColor, textData.font);
                gameEngine.getRenderEngine().add(text2D);
            }
        }
        return returnValue;
    }

    @Override
    public long secondToRun() {
        return taskStartTime + durationMs - System.currentTimeMillis();
    }

    @Override
    public void subExecute(float deltaTime) {
//        logger.info(String.format("%dms", (System.currentTimeMillis() - taskStartTime)));
//        final GlyphLayout lastLayout = text.font.draw(gameEngine.getRenderEngine().renderEngine2D.batch, text.text, x, y, width, Align.left, true);
        //starting
        if (System.currentTimeMillis() < taskStartTime + fadeInMs) {
            //fade in
            float f = ((float) (System.currentTimeMillis() - taskStartTime)) / fadeInMs;
            Color color = ColorUtil.mix(textData.color, backgroundColor, f);//fade-in
//            logger.info(String.format("fade in %f", f));
            text2D.setColor(color);
        } else if (System.currentTimeMillis() > taskStartTime + durationMs - fadeOutMs) {
            //fade out
            float f = ((float) (System.currentTimeMillis() - (taskStartTime + durationMs - fadeOutMs))) / fadeOutMs;
//            logger.info(String.format("fade out %f", f));
            Color color = ColorUtil.mix(backgroundColor, textData.color, f);//fade-out
            text2D.setColor(color);
        } else {
//            logger.info(String.format("show %f", 1f));
            text2D.setColor(textData.color);
        }
    }

}
