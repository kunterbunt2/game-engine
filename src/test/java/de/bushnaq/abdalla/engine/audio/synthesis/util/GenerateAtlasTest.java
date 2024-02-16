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

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.backends.lwjgl.LwjglNativesLoader;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.PixmapPacker.Page;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import de.bushnaq.abdalla.engine.util.FontData;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class GenerateAtlasTest implements ApplicationListener {
    private static final int MAX_ITERATIONS = 20;

    private int calculatePageSize(final int i) {
        return 64 * i;
    }

    @Override
    public void create() {
        final BasicAtlasManager atlasManager = new BasicAtlasManager();
        generateFonts(atlasManager.fontDataList);
        createAtlas();
        System.out.println("End");
        Gdx.app.exit();
        System.exit(0);
    }

    @Override
    public void resize(final int width, final int height) {
    }

    @Override
    public void render() {
        try {
            Thread.sleep(100);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

    private void createAtlas() {
        (new File("app/assets/atlas/atlas.png")).delete();
        (new File("app/assets/atlas/atlas.atlas")).delete();
        TexturePacker.process("app/assets/raw", "app/assets/atlas/", "atlas");
    }

    @Test
    public void generateAtlas() throws Exception {
        Gdx.files = new LwjglFiles();
        LwjglNativesLoader.load();
        final Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        new Lwjgl3Application(this, config);
    }

    private void generateFonts(final FontData[] fontDataList) {
        for (final FontData fontData : fontDataList) {
            int i = 1;
            int pageSize;
            for (; i < MAX_ITERATIONS; i++) {
                pageSize = calculatePageSize(i);
                try {

                    final PixmapPacker packer = new PixmapPacker(pageSize, pageSize, Format.RGBA8888, 1, false);
                    {
                        final FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontData.file));
                        final FreeTypeFontParameter parameter = new FreeTypeFontParameter();
                        parameter.size   = (fontData.fontSize);
                        parameter.packer = packer;
                        generator.generateData(parameter);
                        generator.dispose(); // don't forget to dispose to avoid memory leaks!
                    }
                    final Array<Page> pages = packer.getPages();
                    if (pages.size == 1) {
                        final Page   p      = pages.get(0);
                        final Pixmap pixmap = p.getPixmap();
                        System.out.println("Generating font '" + fontData.name + ".png'.");
                        final FileHandle fh = new FileHandle("app/assets/raw/" + fontData.name + ".png");
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
            assertNotEquals(calculatePageSize(i), calculatePageSize(MAX_ITERATIONS), "Page too small for font: " + fontData.name);
        }
    }
}
