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
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import de.bushnaq.abdalla.engine.util.AtlasGenerator;
import de.bushnaq.abdalla.engine.util.FontData;

import java.io.File;

public class BasicAtlasManager {
    private static String       assetsFolderName;
    public         TextureAtlas atlas;
    public         FontData[]   fontData = {new FontData("menu-font", Context.getAppFolderName() + "/assets/fonts/Roboto-Regular.ttf", 12),//
            new FontData("menu-font-bold", Context.getAppFolderName() + "/assets/fonts/Roboto-bold.ttf", 12),//
            new FontData("model-font", Context.getAppFolderName() + "/assets/fonts/Roboto-Bold.ttf", 64),//
    };
    public         BitmapFont   menuBoldFont;
    public         BitmapFont   menuFont;
    public         BitmapFont   modelFont;
    public         AtlasRegion  systemTextureRegion;

    public BasicAtlasManager() {
    }

    public static String getAssetsFolderName() {
        return assetsFolderName;
    }

    public void dispose() {
        for (final FontData fontData : fontData) {
            fontData.font.dispose();
        }
        atlas.dispose();
    }

    public void init() throws Exception {
        assetsFolderName = Context.getAppFolderName() + "/assets/";
        initTextures();
        initFonts();
    }

    private void initFonts() {
        for (int index = 0; index < fontData.length; index++) {
            final FontData    fontData    = this.fontData[index];
            final AtlasRegion atlasRegion = atlas.findRegion(fontData.name);
            atlasRegion.getRegionWidth();
            atlasRegion.getRegionHeight();
            final PixmapPacker          packer    = new PixmapPacker(atlasRegion.getRegionWidth(), atlasRegion.getRegionHeight(), Format.RGBA8888, 1, false);
            final FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontData.file));
            final FreeTypeFontParameter parameter = new FreeTypeFontParameter();
            parameter.size   = (fontData.fontSize);
            parameter.packer = packer;
            final BitmapFont generateFont = generator.generateFont(parameter);
            generator.dispose(); // don't forget to dispose to avoid memory leaks!
            fontData.font = new BitmapFont(generateFont.getData(), atlas.findRegion(fontData.name), true);
            packer.dispose();
            fontData.font.setUseIntegerPositions(false);
        }
        menuFont     = fontData[0].font;
        menuBoldFont = fontData[1].font;
        modelFont    = fontData[2].font;
    }

    private void initTextures() throws Exception {
        AtlasGenerator atlasGenerator = new AtlasGenerator();
        atlasGenerator.setOutputFolder(getAssetsFolderName() + "atlas/");
        atlasGenerator.setInputFolders(new File[]{new File(getAssetsFolderName() + "textures/")});
        atlasGenerator.setFontData(fontData);
        atlasGenerator.generateIfNeeded();
        atlas               = new TextureAtlas(Gdx.files.internal(Context.getAppFolderName() + "/assets/atlas/atlas.atlas"));
        systemTextureRegion = atlas.findRegion("system");
        Colors.put("BOLD", new Color(0x1BA1E2FF));
    }
}
