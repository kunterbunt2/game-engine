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

package de.bushnaq.abdalla.engine.shader.effect.scheduled;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import de.bushnaq.abdalla.engine.IGameEngine;
import de.bushnaq.abdalla.engine.Text2D;
import de.bushnaq.abdalla.engine.util.ColorUtil;

import static de.bushnaq.abdalla.engine.shader.effect.scheduled.SchedulePhase.EXECUTE;
import static de.bushnaq.abdalla.engine.shader.effect.scheduled.SchedulePhase.START;

public class TextTask<T extends IGameEngine> extends ScheduledTask<T> {

    private final long          end   = 500;
    private final float         height;
    private       SchedulePhase mode  = START;
    private final long          start = 500;
    private final TextFormat    text;
    private       Text2D        text2D;
    private final float         width;

    public TextTask(T gameEngine, TextFormat text, float duration) {
        super(gameEngine, duration);
        this.text = text;
        final GlyphLayout layout = new GlyphLayout();
        layout.setText(text.font, text.text);
        width  = layout.width;// contains the width of the current set text
        height = layout.height;// contains the width of the current set text
    }

    public boolean execute(float deltaTime) {
        boolean returnValue = false;
        switch (mode) {
            case EXECUTE -> {
                subexecute(deltaTime);
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
                text2D = new Text2D(text.text, x, y, Color.BLACK, text.font);
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
    public void subexecute(float deltaTime) {
//        logger.info(String.format("%dms", (System.currentTimeMillis() - taskStartTime)));
//        final GlyphLayout lastLayout = text.font.draw(gameEngine.getRenderEngine().renderEngine2D.batch, text.text, x, y, width, Align.left, true);
        //starting
        if (System.currentTimeMillis() < taskStartTime + start) {
            //before start
            float f = ((float) (System.currentTimeMillis() - taskStartTime)) / start;
//            logger.info(String.format("before start %f", f));
            Color color = ColorUtil.mix(text.color, Color.BLACK, f);//fade-in
            text2D.setColor(color);
        } else if (System.currentTimeMillis() > taskStartTime + durationMs - end) {
            //before end
            float f = ((float) (System.currentTimeMillis() - (taskStartTime + durationMs - end))) / end;
//            logger.info(String.format("before end %f", f));
            Color color = ColorUtil.mix(Color.BLACK, text.color, f);//fade-out
            text2D.setColor(color);
        } else {
//            logger.info(String.format("show %f", 1f));
            text2D.setColor(text.color);
        }
    }

}
