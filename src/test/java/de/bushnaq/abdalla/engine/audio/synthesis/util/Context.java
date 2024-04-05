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
import com.badlogic.gdx.files.FileHandle;
import de.bushnaq.abdalla.engine.IContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

enum OperatingSystem {
    android, applet, headlessDesktop, ios, iosSimulator, linux, osx, unknonw, webgl, windows
}

/**
 * @author kunterbunt
 */
public abstract class Context extends ApplicationProperties implements IContext {
    protected final static Logger          logger           = LoggerFactory.getLogger(Context.class);
    private static         String          appFolderName    = "app";
    private static         String          configFolderName = "app/config";
    private static         String          homeFolderName;
    public                 long            currentTime      = 8L * 10000;
    public                 boolean         restart          = false;
    public                 Object          selected         = null;
    public                 long            timeDelta        = 0L;
    private                String          appVersion       = "0.0.0";
    private                boolean         enableTime       = true;
    private                String          installationFolder;
    private                long            lastTime         = 0;
    private                OperatingSystem operatingSystem;

    public Context() {
        homeFolderName  = "app/";
        operatingSystem = getOeratingSystemType();
        switch (operatingSystem) {
            case windows:
            default:
                if (isRunningInEclipse()) {
                    logger.info("Detected 'Windows' system and we are running inside of 'IDE'.");
                    installationFolder = cleanupPath(getInstallationFolder() + "/../..");
                    appFolderName      = installationFolder + "/app";
                    configFolderName   = homeFolderName;
                } else {
                    logger.info("Detected 'Windows' system.");
                    installationFolder = cleanupPath(getInstallationFolder() + "/../..");
                    appFolderName      = installationFolder + "/app";
                    configFolderName   = homeFolderName;
                }
                break;
            case linux:
                if (isRunningInEclipse()) {
                    logger.info("Detected 'Linux' system and we are running inside of 'Eclipse'.");
                    installationFolder = cleanupPath(getInstallationFolder() + "/../../..");
                    appFolderName      = installationFolder + "/app";
                    configFolderName   = homeFolderName;
                } else {
                    logger.info("Detected 'Linux' system.");
                    installationFolder = cleanupPath(getInstallationFolder() + "/../../../bin");
                    appFolderName      = cleanupPath(installationFolder + "/../lib/app");
                    configFolderName   = homeFolderName;
                }
                break;
            case osx:
                if (isRunningInEclipse()) {
                    logger.info("Detected 'macOS' system and we are running inside of 'Eclipse'.");
                    installationFolder = cleanupPath(getInstallationFolder() + "/../../..");
                    appFolderName      = installationFolder + "/app";
                    configFolderName   = homeFolderName;
                } else {
                    logger.info("Detected 'macOS' system.");
                    installationFolder = cleanupPath(getInstallationFolder() + "/../../MacOS");
                    appFolderName      = cleanupPath(installationFolder + "/../app");
                    configFolderName   = homeFolderName;
                }
                break;
            case iosSimulator: {
                logger.info("Detected 'iOS' system and we are running inside of 'simulator'.");
                homeFolderName     = ".";
                installationFolder = ".";
                appFolderName      = installationFolder;
                configFolderName   = homeFolderName;
            }
            break;
            case ios: {
                logger.info("Detected 'iOS' system.");
                homeFolderName     = ".";
                installationFolder = ".";
                appFolderName      = installationFolder;
                configFolderName   = homeFolderName;
            }
            break;

        }
        logger.info("Detected 'home' folder = " + homeFolderName);
        logger.info("Detected 'installation' folder = " + installationFolder);
        logger.info("Detected 'app' folder = " + appFolderName);
        logger.info("Detected 'configuration' folder = " + configFolderName);
        createFolder(homeFolderName);
        createFolder(configFolderName);

    }

    protected static String cleanupPath(String path) {
        try {
            path = new File(path).getCanonicalPath();// get rid of all the /..
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return path;
    }

    public static String getAppFolderName() {
        return appFolderName;
    }

    public static String getConfigFolderName() {
        return configFolderName;
    }

    public static String getHomeFolderName() {
        return homeFolderName;
    }

    public static OperatingSystem getOeratingSystemType() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return OperatingSystem.windows;
        } else if (os.contains("mac")) {
            return OperatingSystem.osx;
        } else if (os.contains("nix") || os.contains("nux")) {
            return OperatingSystem.linux;
        } else if (os.contains("ios simulator")) {
            return OperatingSystem.iosSimulator;
        } else if (os.contains("ios")) {
            return OperatingSystem.ios;
        } else
            return OperatingSystem.unknonw;
    }

    public static boolean isIos() {
        return getOeratingSystemType().equals(OperatingSystem.ios) || getOeratingSystemType().equals(OperatingSystem.iosSimulator);
    }

    public static boolean isRunningInEclipse() {
        String  path      = System.getProperty("java.class.path").toLowerCase();
        boolean isEclipse = path.contains(".m2");
        return isEclipse;
    }

    private void createFolder(String folderName) {
        FileHandle local = Gdx.files.external(folderName);
        if (!local.exists()) {
            local.mkdirs(); // If you require it to make the entire directory path including parents, use directory.mkdirs(); here instead.
        }
    }

    public void dispose() {
    }

    public String getAppVersion() {
        return appVersion;
    }


    protected abstract String getInstallationFolder();

    public boolean isEnableTime() {
        return enableTime;
    }

    public void setSelected(final Object selected, final boolean setDirty) throws Exception {
        this.selected = selected;
    }


}
