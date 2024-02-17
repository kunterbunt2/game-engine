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
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.lwjgl.opengl.GL30C;

import java.io.File;
import java.net.URISyntaxException;

/**
 * @author kunterbunt
 */
public class BasicDesktopContext extends Context {
    private static final String PLUVIA_MONITOR = "pluvia.monitor";

    @Override
    public void disableClipping() {
        if (!Context.isIos())
            Gdx.gl.glDisable(GL30C.GL_CLIP_DISTANCE0);
    }

    @Override
    public void enableClipping() {
        if (!Context.isIos())
            Gdx.gl.glEnable(GL30C.GL_CLIP_DISTANCE0);

    }

    @Override
    public float getWaterLevel() {
        return -10f;
    }

    @Override
    protected String getInstallationFolder() {
        try {
            String path = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            if (path.endsWith(".jar")) {
                logger.info("path = " + path);
                logger.info("last index = " + path.lastIndexOf(File.separator));
                path = path.substring(0, path.lastIndexOf(File.separator) - 1);
            }
            path = cleanupPath(path);
            return path;
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    @Override
    public int getMonitorProperty() {
        final Monitor[] monitors = Lwjgl3ApplicationConfiguration.getMonitors();
        int             monitor  = readIntegerProperty(PLUVIA_MONITOR, 0, 0, monitors.length);
        if (monitor < 0 || monitor >= monitors.length) {
            monitor = 0;
            logger.error(String.format("pluvia.monitiro=%d cannot be negative or higher than the number of monitors %d.", monitor, monitors.length));
        }
        return monitor;
    }

    @Override
    public int getNumberOfMonitors() {
        final Monitor[] monitors = Lwjgl3ApplicationConfiguration.getMonitors();
        return monitors.length;
    }

    @Override
    public boolean isDebugModeSupported() {
        return true;
    }

    @Override
    public boolean isForegroundFpsSupported() {
        return true;
    }

    @Override
    public boolean isFullscreenModeSupported() {
        return true;
    }

    @Override
    public boolean isMSAASamplesSupported() {
        return true;
    }

    @Override
    public boolean isMonitorSupported() {
        return true;
    }

    @Override
    public boolean isPbrModeSupported() {
        return false;
    }

    @Override
    public boolean isRestartSuported() {
        return true;
    }

    @Override
    public boolean isVsyncSupported() {
        return false;
    }

    @Override
    public void setMonitor(int value) {
        properties.setProperty(PLUVIA_MONITOR, "" + value);
    }

}
