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

/**
 * @author kunterbunt
 */
public interface IApplicationProperties {

    int MAX_GRAPHICS_QUALITY = 4;

    boolean getAmbientAudioProperty();

    int getAmbientAudioVolumenProperty();

    int getAudioVolumenProperty();

    boolean getDebugModeProperty();

    int getForegroundFPSProperty();

    boolean getFullscreenModeProperty();

    int getGraphicsQuality();

    int getMSAASamples();

    int getMaxPointLights();

    int getMaxSceneObjects();

    int getMonitorProperty();

    int getNumberOfMonitors();

    boolean getPbrModeProperty();

    int getShadowMapSizeProperty();

    boolean getShowFpsProperty();

    boolean getShowGraphsProperty();

    boolean getVsyncProperty();

    boolean isDebugMode();

    boolean isDebugModeSupported();

    boolean isForegroundFpsSupported();

    boolean isFullscreenModeSupported();

    boolean isMSAASamplesSupported();

    boolean isMonitorSupported();

    boolean isPbrModeSupported();

    boolean isRestartSuported();

    boolean isShowGraphs();

    boolean isVsyncSupported();

    void setAmbientAudio(boolean checked);

    void setAmbientAudioVolumen(int value);

    void setAudioVolumen(int value);

    void setDebugMode(boolean checked);

    void setForegroundFps(int value);

    void setFullScreenMode(boolean checked);

    void setGraphicsQuality(int value);

    void setMaxPointLights(int value);

    void setMaxSceneObjects(int value);

    void setMonitor(int value);

    void setMsaaSamples(int value);

    void setPbr(boolean checked);

    void setShadowMapSize(int value);

    void setShowFps(boolean checked);

    void setShowGraphs(boolean checked);

    void setVsync(boolean checked);

    void write();
}