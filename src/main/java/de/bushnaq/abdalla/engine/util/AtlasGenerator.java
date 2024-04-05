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

package de.bushnaq.abdalla.engine.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;

public class AtlasGenerator {
    private static final int        MAX_ITERATIONS        = 64;
    protected final      Logger     logger                = LoggerFactory.getLogger(this.getClass());
    private              String     aggregatedInputFolder = "app/assets/raw/";
    private              FontData[] fontData;
    private              File[]     inputFolders;
    private              String     outputFolder          = "app/assets/atlas/";
    private              String     packFileName          = "atlas";

    private int calculatePageSize(final int i) {
        return 64 * i;
    }

    private boolean foundMissingImage(TextureAtlas atlas, File srcDir) {
        Collection<File> files = FileUtils.listFiles(srcDir, new String[]{"png"}, false);
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(".9.png")) fileName = fileName.substring(0, fileName.indexOf(".9.png")) + ".png";
            TextureAtlas.AtlasRegion atlasRegion = atlas.findRegion(removeFileExtension(fileName));
            if (atlasRegion == null) {
                return true;
            }
        }
        return false;
    }

    private void generateAtlas(File atlasFile, String atlasAtlasFileName, String generationReason) throws Exception {
        File aggregatedInputFolderFile = new File(aggregatedInputFolder);
        logger.info("----------------------------------------------------------------------------------");
        logger.info(generationReason);
        if (!aggregatedInputFolderFile.exists())
            aggregatedInputFolderFile.mkdir();
        else
            FileUtils.cleanDirectory(aggregatedInputFolderFile);
        atlasFile.delete();
        (new File(atlasAtlasFileName)).delete();
        generateFonts();
        for (File f : inputFolders) {
            FileUtils.copyDirectory(f, aggregatedInputFolderFile);
        }
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.maxWidth  = 4096;
        settings.maxHeight = 4096;
        TexturePacker.process(settings, aggregatedInputFolder, outputFolder, packFileName);
        logger.info("----------------------------------------------------------------------------------");
    }

    private void generateFonts() throws Exception {
        for (final FontData fontData : fontData) {
            File fontFile = new File(aggregatedInputFolder + "/" + fontData.name + ".png");
            if (!fontFile.exists()) {
                //font was never generated
                int i = 1;
                int pageSize;
                for (; i < MAX_ITERATIONS; i++) {
                    pageSize = calculatePageSize(i);
                    try {

                        final PixmapPacker packer = new PixmapPacker(pageSize, pageSize, Pixmap.Format.RGBA8888, 1, false);
                        {
                            final FreeTypeFontGenerator                       generator = new FreeTypeFontGenerator(Gdx.files.internal(fontData.file));
                            final FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
                            parameter.size   = (fontData.fontSize);
                            parameter.packer = packer;
                            generator.generateData(parameter);
                            generator.dispose(); // don't forget to dispose to avoid memory leaks!
                        }
                        final Array<PixmapPacker.Page> pages = packer.getPages();
                        if (pages.size == 1) {
                            final PixmapPacker.Page p      = pages.get(0);
                            final Pixmap            pixmap = p.getPixmap();
                            logger.info("Generating font '" + fontData.name + ".png'.");
                            final FileHandle fh = new FileHandle(aggregatedInputFolder + "/" + fontData.name + ".png");
                            PixmapIO.writePNG(fh, pixmap);
                            pixmap.dispose();
                            break;
                        }
                    } catch (final GdxRuntimeException e) {
                        if (e.getMessage().equals("Page size too small for pixmap.")) {
                            //ignore
                        } else {
                            throw e;
                        }
                    }
                }
                if (i == MAX_ITERATIONS)
                    throw new Exception(String.format("Page size of %d too small for font: %s", calculatePageSize(MAX_ITERATIONS), fontData.name));
            }
        }
    }

    public void generateIfNeeded() throws Exception {
        String atlasAtlasFileName = outputFolder + packFileName + ".atlas";
        String atlasImageFileName = outputFolder + packFileName + ".png";
        File   atlasFile          = new File(atlasImageFileName);
        if (!atlasFile.exists()) {
            //atlas does not exist
            generateAtlas(atlasFile, atlasAtlasFileName, "Atlas missing, generating atlas.");
            return;
        }
        {
            for (final FontData fontData : fontData) {
                File fontFile = new File(aggregatedInputFolder + "/" + fontData.name + ".png");
                if (!fontFile.exists()) {
                    generateAtlas(atlasFile, atlasAtlasFileName, "At least one font file is missing, generating atlas.");
                    return;
                }
            }
        }
        {
            long lastAtlasGeneration = atlasFile.lastModified();
            for (File f : inputFolders) {
                long lastPngGeneration = getNewestFile(f);
                if (lastAtlasGeneration < lastPngGeneration) {
                    //there is at least on png file newer than the atlas
                    generateAtlas(atlasFile, atlasAtlasFileName, "At least one png file is newer than atlas, generating atlas.");
                    return;
                }
            }
        }
        {
            TextureAtlas atlas = new TextureAtlas(Gdx.files.internal(atlasAtlasFileName));
            try {
                for (File f : inputFolders) {
                    if (foundMissingImage(atlas, f)) {
                        //there is at least one file missing in the current atlas
                        generateAtlas(atlasFile, atlasAtlasFileName, "At least one png file not found in atlas, generating atlas.");
                        return;
                    }

                }
            } finally {
                atlas.dispose();
            }
        }
    }

    private long getNewestFile(File srcDir) {
        long             lastPngGeneration = 0;
        Collection<File> files             = FileUtils.listFiles(srcDir, null, false);
        for (File file : files) {
            lastPngGeneration = Math.max(lastPngGeneration, file.lastModified());
        }
        return lastPngGeneration;
    }

    private String removeFileExtension(String name) {
        return name.substring(0, name.lastIndexOf('.'));
    }

    public void setFontData(FontData[] fontData) {
        this.fontData = fontData;
    }

    public void setInputFolders(File[] inputFolders) {
        this.inputFolders = inputFolders;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public void setPackFileName(String packFileName) {
        this.packFileName = packFileName;
    }
}
