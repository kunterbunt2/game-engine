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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import de.bushnaq.abdalla.engine.audio.synthesis.util.CircularCubeActor;
import de.bushnaq.abdalla.engine.audio.synthesis.util.TranslationUtil;
import de.bushnaq.abdalla.engine.util.ExtendedGLProfiler;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RenderTest extends TranslationUtil {
    public static final  String               VISIBLE_DYNAMIC_GAME_OBJECTS     = "visibleDynamicGameObjects";
    private static final String               CALLS                            = "calls";
    private static final String               DRAW_CALLS                       = "drawCalls";
    private static final String               DYNAMIC_TEXT_3_D                 = "dynamicText3D";
    private static final String               FPS                              = "fps";
    private static final int                  NUMBER_OF_SOURCES                = 10;
    private static final String               SHADER_SWITCHES                  = "shaderSwitches";
    private static final String               STATIC_TEXT_3_D                  = "staticText3D";
    private static final String               TEXTURE_BINDINGS                 = "textureBindings";
    private static final String               TEXTURE_GET_NUM_MANAGED_TEXTURES = "Texture.getNumManagedTextures()";
    private static final String               VISIBLE_STATIC_GAME_OBJECTS      = "visibleStaticGameObjects";
    private final        Logger               logger                           = LoggerFactory.getLogger(this.getClass());
    private final        Map<String, Integer> performanceCounters              = new HashMap<>();
    private final        long                 started                          = System.currentTimeMillis();
    CircularCubeActor[] ccaa = new CircularCubeActor[NUMBER_OF_SOURCES];

    private void assesPerformanceCounters() {
        if (getRenderEngine().isShowGraphs()) {
            assertEquals(49, performanceCounters.get(TEXTURE_BINDINGS));
            assertEquals(69, performanceCounters.get(DRAW_CALLS));
            assertEquals(9, performanceCounters.get(SHADER_SWITCHES));
            assertEquals(1210, performanceCounters.get(CALLS));
            assertEquals(4, performanceCounters.get(TEXTURE_GET_NUM_MANAGED_TEXTURES));
            assertEquals(120, performanceCounters.get(FPS));
        } else {
            assertEquals(45, performanceCounters.get(TEXTURE_BINDINGS));
            assertEquals(65, performanceCounters.get(DRAW_CALLS));
            assertEquals(7, performanceCounters.get(SHADER_SWITCHES));
            assertEquals(1140, performanceCounters.get(CALLS));
            assertEquals(4, performanceCounters.get(TEXTURE_GET_NUM_MANAGED_TEXTURES));
            assertEquals(120, performanceCounters.get(FPS));
            assertEquals(0, performanceCounters.get(DYNAMIC_TEXT_3_D));
            assertEquals(0, performanceCounters.get(STATIC_TEXT_3_D));
            assertEquals(10, performanceCounters.get(VISIBLE_DYNAMIC_GAME_OBJECTS));
            assertEquals(0, performanceCounters.get(VISIBLE_STATIC_GAME_OBJECTS));
        }
    }

    @Test
    public void circularTranslatingSources() {
        runFor = 10000;
        startLwjgl();
    }

    @Override
    public void create() {
        super.create();
        for (int i = 0; i < NUMBER_OF_SOURCES; i++) {
            ccaa[i] = new CircularCubeActor(i, 0);
            ccaa[i].get3DRenderer().create(getRenderEngine());
        }
    }

    @Override
    public void dispose() {
        printPerformanceCounters();
        assesPerformanceCounters();
        super.dispose();
    }

    @Override
    protected void update() throws Exception {
        for (int i = 0; i < NUMBER_OF_SOURCES; i++) {
            ccaa[i].get3DRenderer().update(getRenderEngine(), 0, 0, 0, false);
        }
        super.update();
    }

    private void printPerformanceCounters() {
        for (String counter : performanceCounters.keySet()) {
            logger.info(String.format("%s = %d", counter, performanceCounters.get(counter)));
        }
    }

    private void updateCounter(String counter, int value) {
        performanceCounters.merge(counter, value, (a, b) -> Math.max(b, a));
    }

    void updateCounters(ExtendedGLProfiler profiler) {
        if (System.currentTimeMillis() - started > 2000) {
            updateCounter(TEXTURE_BINDINGS, profiler.getTextureBindings());
            updateCounter(DRAW_CALLS, profiler.getDrawCalls());
            updateCounter(SHADER_SWITCHES, profiler.getShaderSwitches());
            updateCounter(CALLS, profiler.getCalls());
            updateCounter(TEXTURE_GET_NUM_MANAGED_TEXTURES, Texture.getNumManagedTextures());
            updateCounter(FPS, Gdx.graphics.getFramesPerSecond());
            updateCounter(DYNAMIC_TEXT_3_D, profiler.getDynamicText3D());
            updateCounter(STATIC_TEXT_3_D, profiler.getStaticText3D());
            updateCounter(VISIBLE_DYNAMIC_GAME_OBJECTS, profiler.getVisibleDynamicGameObjects());
            updateCounter(VISIBLE_STATIC_GAME_OBJECTS, profiler.getVisibleStaticGameObjects());
        }
    }

    //	private void printStatistics() throws Exception {
    //		if (profiler.isEnabled()) {
    //			//			setMaxFramesPerSecond(Math.max(getMaxFramesPerSecond(), Gdx.graphics.getFramesPerSecond()));
    //			// once a second
    //			if (debugTimer.getTime() > 1000) {
    //				// for ( String statisticName : universe.timeStatisticManager.getSet() )
    //				// {
    //				// TimeStatistic statistic = universe.timeStatisticManager.getStatistic(
    //				// statisticName );
    //				// System.out.println( String.format( "%s %dms %dms %dms %dms", statisticName,
    //				// statistic.lastTime, statistic.minTime, statistic.averageTime,
    //				// statistic.maxTime ) );
    //				// }
    //				System.out.printf("----------------------------------------------------\n");
    //				System.out.printf("profiler.textureBindings %d\n", profiler.getTextureBindings());// expensive, minimize
    //																									// with atlas
    //				System.out.printf("profiler.drawCalls %d\n", profiler.getDrawCalls());
    //				System.out.printf("profiler.shaderSwitches %d\n", profiler.getShaderSwitches());
    //				System.out.printf("profiler.vertexCount.min %.0f\n", profiler.getVertexCount().min);
    //				System.out.printf("profiler.vertexCount.average %.0f\n", profiler.getVertexCount().average);
    //				System.out.printf("profiler.vertexCount.max %.0f\n", profiler.getVertexCount().max);
    //				System.out.printf("profiler.calls %d\n", profiler.getCalls());
    //				System.out.printf("Texture.getNumManagedTextures() %d\n", Texture.getNumManagedTextures());
    //				System.out.printf("Gdx.graphics.getDeltaTime() %f\n", Gdx.graphics.getDeltaTime());
    //				//				System.out.printf("batch.renderCalls %d\n", renderMaster.sceneClusterManager.modelBatch.);
    //				System.out.printf(Gdx.graphics.getFramesPerSecond() + " fps\n");
    //				System.out.printf("----------------------------------------------------\n");
    //			}
    //		}
    //	}
}