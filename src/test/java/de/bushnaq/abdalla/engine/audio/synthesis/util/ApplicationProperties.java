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
import de.bushnaq.abdalla.engine.IApplicationProperties;
import de.bushnaq.abdalla.engine.util.logger.Logger;
import de.bushnaq.abdalla.engine.util.logger.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author kunterbunt
 */
public abstract class ApplicationProperties implements IApplicationProperties {
    protected static final String     PLUVIA_SHADOW_MAP_SIZE       = "pluvia.shadowMapSize";
    private static final   String     PLUVIA_AMBIENT_AUDIO         = "pluvia.ambientAudio";
    private static final   String     PLUVIA_AMBIENT_AUDIO_VOLUMEN = "pluvia.ambientAudioVolumen";
    private static final   String     PLUVIA_AUDIO_VOLUMEN         = "pluvia.audioVolumen";
    private static final   String     PLUVIA_DEBUG_MODE            = "pluvia.debugMode";
    private static final   String     PLUVIA_FOREGROUND_FPS        = "pluvia.foregroundFPS";
    private static final   String     PLUVIA_FULLSCREEN_MODE       = "pluvia.fullscreenMode";
    private static final   String     PLUVIA_GRAPHICS_QUALITY      = "pluvia.graphicsQuality";
    private static final   String     PLUVIA_MAX_POINT_LIGHTS      = "pluvia.maxPointLights";
    private static final   String     PLUVIA_MAX_SCENE_OBJECTS     = "pluvia.maxSceneObjects";
    private static final   String     PLUVIA_MSAA_SAMPLES          = "pluvia.msaaSamples";
    private static final   String     PLUVIA_PBR_MODE              = "pluvia.pbr";
    private static final   String     PLUVIA_SHOW_FPS              = "pluvia.showFps";
    private static final   String     PLUVIA_SHOW_GRAPHS           = "pluvia.showGraphs";
    private static final   String     PLUVIA_VSYNC                 = "pluvia.vsync";
    private static         String     propertyFileName;
    //	private int						maxSceneObjects;
    public                 int        predefinedMaxPointLights[]   = {0, 5, 10, 20};
    public                 int        predefinedMaxSceneObjects[]  = {0, 25, 50, 100};
    public                 int        predefinedMssaSamples[]      = {0, 4, 8, 16};
    public                 int        predefinedShadowMapSize[]    = {1024, 2048, 4096, 8192};
    public                 Properties properties                   = new Properties();
    Map<String, Boolean> booleanPropertiesMap = new HashMap<>();
    Map<String, Integer> integerPropertiesMap = new HashMap<>();
    //	private boolean					debugMode;																	// debug mode is allowed
    private Logger logger = LoggerFactory.getLogger(this.getClass());

//	private boolean					showGraphs;

    public ApplicationProperties() {
    }

    @Override
    public boolean getAmbientAudioProperty() {
        return readBooleanProperty(PLUVIA_AMBIENT_AUDIO, true);
    }

    @Override
    public int getAmbientAudioVolumenProperty() {
        return readIntegerProperty(PLUVIA_AMBIENT_AUDIO_VOLUMEN, 10, 1, 100);
    }

    @Override
    public int getAudioVolumenProperty() {
        return readIntegerProperty(PLUVIA_AUDIO_VOLUMEN, 50, 1, 100);
    }

    @Override
    public boolean getDebugModeProperty() {
        return readBooleanProperty(PLUVIA_DEBUG_MODE, false);
    }

    @Override
    public int getForegroundFPSProperty() {
        return readIntegerProperty(PLUVIA_FOREGROUND_FPS, 60, 25, 10000);
    }

    @Override
    public boolean getFullscreenModeProperty() {
        return readBooleanProperty(PLUVIA_FULLSCREEN_MODE, true);
    }

    @Override
    public int getGraphicsQuality() {
        return readIntegerProperty(PLUVIA_GRAPHICS_QUALITY, 2, 0, MAX_GRAPHICS_QUALITY);
    }

    @Override
    public int getMSAASamples() {
        return readIntegerProperty(PLUVIA_MSAA_SAMPLES, 16, 0, 32);
    }

    @Override
    public int getMaxPointLights() {
        return readIntegerProperty(PLUVIA_MAX_POINT_LIGHTS, 20, 0, 500);
    }

    @Override
    public int getMaxSceneObjects() {
        return readIntegerProperty(PLUVIA_MAX_SCENE_OBJECTS, 10, 0, 500);
    }

    @Override
    public boolean getPbrModeProperty() {
        return readBooleanProperty(PLUVIA_PBR_MODE, true);
    }

    @Override
    public int getShadowMapSizeProperty() {
        return readMultipleOf2Property(PLUVIA_SHADOW_MAP_SIZE, 4096, 128, 8192);
    }

    @Override
    public boolean getShowFpsProperty() {
        return readBooleanProperty(PLUVIA_SHOW_FPS, true);
    }

    @Override
    public boolean getShowGraphsProperty() {
        return readBooleanProperty(PLUVIA_SHOW_GRAPHS, false);
    }

    @Override
    public boolean getVsyncProperty() {
        return readBooleanProperty(PLUVIA_VSYNC, true);
    }

    @Override
    public boolean isDebugMode() {
        return getDebugModeProperty();
    }

    @Override
    public boolean isShowGraphs() {
        return getShowGraphsProperty();
    }

    @Override
    public void setAmbientAudio(boolean checked) {
        properties.setProperty(PLUVIA_AMBIENT_AUDIO, "" + checked);
    }

    @Override
    public void setAmbientAudioVolumen(int value) {
        properties.setProperty(PLUVIA_AMBIENT_AUDIO_VOLUMEN, "" + value);
    }

    @Override
    public void setAudioVolumen(int value) {
        properties.setProperty(PLUVIA_AUDIO_VOLUMEN, "" + value);
    }

    @Override
    public void setDebugMode(boolean checked) {
        properties.setProperty(PLUVIA_DEBUG_MODE, "" + checked);
    }

    @Override
    public void setForegroundFps(int value) {
        properties.setProperty(PLUVIA_FOREGROUND_FPS, "" + value);
    }

    @Override
    public void setFullScreenMode(boolean checked) {
        if (checked)
            properties.setProperty(PLUVIA_FULLSCREEN_MODE, "true");
        else
            properties.setProperty(PLUVIA_FULLSCREEN_MODE, "false");
    }

    @Override
    public void setGraphicsQuality(int value) {
        properties.setProperty(PLUVIA_GRAPHICS_QUALITY, "" + value);
//		updateGrphicsQuality();
    }

    @Override
    public void setMaxPointLights(int value) {
        properties.setProperty(PLUVIA_MAX_POINT_LIGHTS, "" + value);
    }

    @Override
    public void setMaxSceneObjects(int value) {
        properties.setProperty(PLUVIA_MAX_SCENE_OBJECTS, "" + value);
    }

    @Override
    public void setMsaaSamples(int value) {
        properties.setProperty(PLUVIA_MSAA_SAMPLES, "" + value);
    }

    @Override
    public void setPbr(boolean checked) {
        if (checked)
            properties.setProperty(PLUVIA_PBR_MODE, "true");
        else
            properties.setProperty(PLUVIA_PBR_MODE, "false");
    }

    @Override
    public void setShadowMapSize(int value) {
        properties.setProperty(PLUVIA_SHADOW_MAP_SIZE, "" + value);
    }

    @Override
    public void setShowFps(boolean checked) {
        if (checked)
            properties.setProperty(PLUVIA_SHOW_FPS, "true");
        else
            properties.setProperty(PLUVIA_SHOW_FPS, "false");
    }

    @Override
    public void setShowGraphs(boolean checked) {
        properties.setProperty(PLUVIA_SHOW_GRAPHS, "" + checked);
    }

    @Override
    public void setVsync(boolean checked) {
        if (checked)
            properties.setProperty(PLUVIA_VSYNC, "true");
        else
            properties.setProperty(PLUVIA_VSYNC, "false");
    }

    @Override
    public void write() {
        try {
            OutputStream inStream = Gdx.files.external(propertyFileName).write(false);
            properties.store(inStream, "test");
            inStream.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void init() {
        propertyFileName = Context.getConfigFolderName() + "/mercator.properties";
        read();
        updateGrphicsQuality();
//		debugMode = getDebugModeProperty();
//		showGraphs = getShowGraphsProperty();
    }

    private boolean isMultipleOf2(int propertyValue, int propertyMinValue, int propertyMaxValue) {
        for (int allowedValues = propertyMinValue; allowedValues <= propertyMaxValue; allowedValues *= 2) {
            if (propertyValue == allowedValues)
                return true;
        }
        return false;
    }

    private void read() {
        try {
            logger.info("----------------------------------------------------------------------------------");
            logger.info(new File(propertyFileName).getAbsolutePath());
            logger.info("----------------------------------------------------------------------------------");
//			InputStream inStream = Gdx.files.external(propertyFileName).read();
            InputStream inStream = new FileInputStream(propertyFileName);
            properties.load(inStream);
            inStream.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private boolean readBooleanProperty(String propertyName, boolean propertyDefaultValue) {
        boolean propertyValue       = propertyDefaultValue;
        Boolean cachedPropertyValue = booleanPropertiesMap.get(propertyName);
        if (cachedPropertyValue == null) {
            String propertyStringValue = properties.getProperty(propertyName);
            if (propertyStringValue != null) {
                try {
                    propertyValue = Boolean.parseBoolean(propertyStringValue);
                } catch (NumberFormatException e) {
                    logger.error(String.format("%s=%s must be a boolean value.", propertyName, propertyStringValue));
                }
            }
            booleanPropertiesMap.put(propertyStringValue, propertyValue);
        }
        return propertyValue;
    }

    protected int readIntegerProperty(String propertyName, int propertyDefaultValue, int propertyMinValue, int propertyMaxValue) {
        int     propertyValue       = propertyDefaultValue;
        Integer cachedPropertyValue = integerPropertiesMap.get(propertyName);
        if (cachedPropertyValue == null) {
            String propertyStringValue = properties.getProperty(propertyName);
            if (propertyStringValue != null) {
                try {
                    propertyValue = Integer.parseInt(propertyStringValue);
                    if (propertyValue < propertyMinValue || propertyValue > propertyMaxValue) {
                        propertyValue = propertyDefaultValue;
                        logger.error(String.format("%s=%s must be a number bigger or equal %d and smaller or equal %d.", propertyName, propertyStringValue, propertyMinValue, propertyMaxValue));
                    }
                } catch (NumberFormatException e) {
                    propertyValue = propertyDefaultValue;
                    logger.error(String.format("%s=%s must be a number bigger or equal %d and smaller or equal %d.", propertyName, propertyStringValue, propertyMinValue, propertyMaxValue));
                }
            }
            integerPropertiesMap.put(propertyStringValue, propertyValue);
        }
        return propertyValue;
    }

    private int readMultipleOf2Property(String propertyName, int propertyDefaultValue, int propertyMinValue, int propertyMaxValue) {
        int     propertyValue       = propertyDefaultValue;
        Integer cachedPropertyValue = integerPropertiesMap.get(propertyName);
        if (cachedPropertyValue == null) {
            String propertyStringValue = properties.getProperty(propertyName);
            if (propertyStringValue != null) {
                try {
                    propertyValue = Integer.parseInt(propertyStringValue);
                    if (propertyValue < propertyMinValue || propertyValue > propertyMaxValue) {
                        propertyValue = propertyDefaultValue;
                        logger.error(String.format("%s=%s must be a number bigger or equal %d and smaller or equal %d.", propertyName, propertyStringValue, propertyMinValue, propertyMaxValue));
                    } else {
                        if (!isMultipleOf2(propertyValue, propertyMinValue, propertyMaxValue)) {
                            propertyValue = propertyDefaultValue;
                            logger.error(String.format("%s=%s must be a nultiple of 2.", propertyName, propertyStringValue));
                        }
                    }
                } catch (NumberFormatException e) {
                    propertyValue = propertyDefaultValue;
                    logger.error(String.format("%s=%s must be a number bigger or equal %d and smaller or equal %d.", propertyName, propertyStringValue));
                }
            }
            integerPropertiesMap.put(propertyStringValue, propertyValue);
        }
        return propertyValue;
    }

    private void updateGrphicsQuality() {
        if (getGraphicsQuality() < MAX_GRAPHICS_QUALITY) {
            properties.setProperty(PLUVIA_MAX_POINT_LIGHTS, "" + predefinedMaxPointLights[getGraphicsQuality()]);
            properties.setProperty(PLUVIA_SHADOW_MAP_SIZE, "" + predefinedShadowMapSize[getGraphicsQuality()]);
            properties.setProperty(PLUVIA_MSAA_SAMPLES, "" + predefinedMssaSamples[getGraphicsQuality()]);
            properties.setProperty(PLUVIA_MAX_SCENE_OBJECTS, "" + predefinedMaxSceneObjects[getGraphicsQuality()]);
        }
        logger.info("--- read following properties ---");
        for (String property : properties.stringPropertyNames()) {
            logger.info(String.format("%s=%s", property, properties.get(property)));
        }
        logger.info("---");
    }

}
