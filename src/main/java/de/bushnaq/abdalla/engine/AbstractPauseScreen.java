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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import de.bushnaq.abdalla.engine.util.ColorUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Visual keyboard overlay pause screen that shows all keyboard commands
 * with connecting lines to their descriptions.
 */
public abstract class AbstractPauseScreen {

    private static final Color                        DESCRIPTION_COLOR = new Color(0.9f, 0.9f, 0.9f, 1.0f);
    private static final Color                        KEY_COLOR         = new Color(0.2f, 0.2f, 0.2f, 0.9f);
    private static final float                        KEY_HEIGHT        = 60f;  // 2x bigger (was 30f, reduced from 120f)
    private static final Color                        OVERLAY_COLOR     = new Color(0.0f, 0.0f, 0.0f, 0.7f);
    private static final Color                        TITLE_COLOR       = Color.WHITE;
    private final        List<KeyboardKey>            allKeys;
    private final        TextureAtlas.AtlasRegion     atlasRegion;
    private final        Map<String, KeyboardCommand> commands;
    private final        IGameEngine                  gameEngine;
    private              float                        keyboardStartX;
    private              float                        keyboardStartY;
    private final        List<LegendItem>             legendItems       = new ArrayList<>();
    private final        BitmapFont                   menuBoldFont;
    private final        BitmapFont                   menuFont;
    private              RichLabel                    resumeLabel;
    private final        ShapeRenderer                shapeRenderer;
    private final        Stage                        stage;
    private              RichLabel                    titleLabel;

    public AbstractPauseScreen(IGameEngine gameEngine, TextureAtlas.AtlasRegion atlasRegion, BitmapFont menuBoldFont, BitmapFont menuFont) {
        this.gameEngine    = gameEngine;
        this.atlasRegion   = atlasRegion;
        this.menuBoldFont  = menuBoldFont;
        this.menuFont      = menuFont;
        this.stage         = new Stage();
        this.shapeRenderer = new ShapeRenderer();
        this.commands      = new HashMap<>();
        this.allKeys       = new ArrayList<>();

        initializeKeyboardCommands();
        initializeKeyboardLayout();
        initializeLegendLayout();
        createUI();
    }

    protected void addCommand(String key, String description, KeyType keyType) {
        KeyboardCommand command = new KeyboardCommand(key, description, keyType);
        commands.put(key, command);
    }

    private void addKeyFromSVG(String label, float svgX, float svgY, float svgWidth, float svgHeight, float scaleFactor) {
        float x      = svgX * scaleFactor;
        float y      = -(svgY + svgHeight) * scaleFactor; // Negative to flip Y coordinate
        float width  = svgWidth * scaleFactor;
        float height = svgHeight * scaleFactor;

        allKeys.add(new KeyboardKey(label, x, y, width, height));
    }

    private void addLegendFromSVG(String label, KeyType keyType, float svgX, float svgY, float svgWidth, float svgHeight, float scaleFactor) {
        float x      = svgX * scaleFactor;
        float y      = -(svgY + svgHeight) * scaleFactor; // Negative to flip Y coordinate
        float width  = svgWidth * scaleFactor;
        float height = svgHeight * scaleFactor;
        legendItems.add(new LegendItem(label, keyType, x, y, width, height));
    }

    private float calculateKeyboardHeight() {
        float minY = Float.MAX_VALUE;
        float maxY = Float.MIN_VALUE;

        for (KeyboardKey key : allKeys) {
            minY = Math.min(minY, key.relativeY);
            maxY = Math.max(maxY, key.relativeY);
        }
        for (LegendItem legendItem : legendItems) {
            minY = Math.min(minY, legendItem.relativeY);
            maxY = Math.max(maxY, legendItem.relativeY);
        }


        return maxY - minY + KEY_HEIGHT;
    }

    private float calculateKeyboardWidth() {
        float maxWidth        = 0;
        float currentRowWidth = 0;
        float currentY        = Float.MAX_VALUE;

        for (KeyboardKey key : allKeys) {
            if (key.relativeY != currentY) {
                maxWidth        = Math.max(maxWidth, currentRowWidth);
                currentRowWidth = key.relativeX + key.width;
                currentY        = key.relativeY;
            } else {
                currentRowWidth = Math.max(currentRowWidth, key.relativeX + key.width);
            }
        }
        maxWidth = Math.max(maxWidth, currentRowWidth);
        return maxWidth;
    }

    private void calculateLayout() {
        float screenWidth  = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Position title at top center
        titleLabel.setPosition(screenWidth / 2 - titleLabel.getWidth() / 2, screenHeight - 80);

        // Position resume instruction at bottom center
        resumeLabel.setPosition(screenWidth / 2 - resumeLabel.getWidth() / 2, 50);

        // Center the keyboard on screen
        float totalKeyboardWidth  = calculateKeyboardWidth();
        float totalKeyboardHeight = calculateKeyboardHeight();

        keyboardStartX = (screenWidth - totalKeyboardWidth) / 2;
        keyboardStartY = (screenHeight + totalKeyboardHeight) / 2 - 50; // Slight offset from center
    }

    private void createUI() {
        // Create title label
        Label.LabelStyle titleStyle = new Label.LabelStyle(menuBoldFont, TITLE_COLOR);
        titleLabel = new RichLabel("PAUSED", titleStyle, atlasRegion);
        titleLabel.setBackgroundColor(new Color(0.1f, 0.1f, 0.3f, 0.8f));

        // Create resume instruction label
        Label.LabelStyle resumeStyle = new Label.LabelStyle(menuFont, DESCRIPTION_COLOR);
        resumeLabel = new RichLabel("Press Space to Resume", resumeStyle, atlasRegion);
        resumeLabel.setBackgroundColor(new Color(0.2f, 0.2f, 0.2f, 0.8f));

        stage.addActor(titleLabel);
        stage.addActor(resumeLabel);

        calculateLayout();
    }

    public void dispose() {
        stage.dispose();
        shapeRenderer.dispose();
    }

    private void drawKeyboardLayout() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);

        // Draw filled rectangles for keys with rounded corners
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (KeyboardKey key : allKeys) {
            // Check if this key has an assigned command
            boolean         hasCommand      = commands.containsKey(key.label);
            KeyboardCommand keyboardCommand = commands.get(key.label);
            // Draw key background with rounded corners (similar to SVG rx="4" ry="4")
            shapeRenderer.setColor(hasCommand ? keyboardCommand.keyType.getColor() : KEY_COLOR);
            drawRoundedRect(keyboardStartX + key.relativeX, keyboardStartY + key.relativeY, key.width, key.height, 4f);
        }
        shapeRenderer.end();


        // Draw text on keys and descriptions
        stage.getBatch().begin();

        for (KeyboardKey key : allKeys) {
            // Draw key label in top-left corner
            menuBoldFont.setColor(Color.WHITE);
//            atlasManager.menuBoldFont.getData().setScale(1f); // Slightly smaller for better fit

            float labelX = keyboardStartX + key.relativeX + 4; // Small padding from left edge
            float labelY = keyboardStartY + key.relativeY + key.height - 8; // Near top edge

            menuBoldFont.draw(stage.getBatch(), key.label, labelX, labelY);

            // Reset font scale

            // Draw command description in bottom-left corner if this key has a command
            KeyboardCommand command = commands.get(key.label);
            if (command != null) {
                menuFont.setColor(DESCRIPTION_COLOR);
                menuFont.getData().setScale(1.0f); // Larger font for descriptions

                float descX      = keyboardStartX + key.relativeX + 4; // Same padding as label
                float lineHeight = 14f;

                // First, determine how many lines we need
                String[]      words       = command.description.split(" ");
                StringBuilder currentLine = new StringBuilder();
                int           totalLines  = 0;
                float         maxLines    = Math.max(1, (key.height - 20) / lineHeight); // Dynamic max lines based on key height

                // Count total lines needed
                for (String word : words) {
                    String testLine = !currentLine.isEmpty() ? currentLine + " " + word : word;

                    // Check if the line would be too wide for the key
                    com.badlogic.gdx.graphics.g2d.GlyphLayout testLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
                    testLayout.setText(menuFont, testLine);

                    if (testLayout.width > key.width - 8 && !currentLine.isEmpty()) {
                        totalLines++;
                        currentLine = new StringBuilder(word);
                    } else {
                        currentLine = new StringBuilder(testLine);
                    }
                }
                if (!currentLine.isEmpty()) {
                    totalLines++;
                }

                // Now draw the lines, starting from bottom for single line, moving up for multi-line
                currentLine = new StringBuilder();
                int   lineCount = 0;
                float startY;

                if (totalLines == 1) {
                    // Single line: position at bottom of key
                    startY = keyboardStartY + key.relativeY + 16;
                } else {
                    // Multi-line: start higher up to fit all lines
                    startY = keyboardStartY + key.relativeY + 16 + (totalLines - 1) * lineHeight;
                }

                for (String word : words) {
                    String testLine = !currentLine.isEmpty() ? currentLine + " " + word : word;

                    // Check if the line would be too wide for the key
                    com.badlogic.gdx.graphics.g2d.GlyphLayout testLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
                    testLayout.setText(menuFont, testLine);

                    if (testLayout.width > key.width - 8 && !currentLine.isEmpty()) {
                        // Draw current line
                        if (lineCount < maxLines) {
                            float descY = startY - (lineCount * lineHeight);
                            menuFont.draw(stage.getBatch(), currentLine.toString(), descX, descY);
                        }
                        currentLine = new StringBuilder(word);
                        lineCount++;
                    } else {
                        currentLine = new StringBuilder(testLine);
                    }
                }

                // Draw the last line
                if (!currentLine.isEmpty() && lineCount < maxLines) {
                    float descY = startY - (lineCount * lineHeight);
                    menuFont.draw(stage.getBatch(), currentLine.toString(), descX, descY);
                }

                // Reset font scale
                menuFont.getData().setScale(1.0f);
            }
        }

        stage.getBatch().end();
    }

    private void drawLegend() {
        shapeRenderer.setProjectionMatrix(stage.getCamera().combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (LegendItem legendItem : legendItems) {
            shapeRenderer.setColor(legendItem.keyType().getColor());
            shapeRenderer.rect(keyboardStartX + legendItem.relativeX, keyboardStartY + legendItem.relativeY, legendItem.width, legendItem.height);
        }
        shapeRenderer.end();

        stage.getBatch().begin();
        menuBoldFont.setColor(Color.WHITE);
        menuFont.setColor(DESCRIPTION_COLOR);
        for (LegendItem legendItem : legendItems) {
            float labelX = keyboardStartX + legendItem.relativeX + 20; // Small padding from left edge
            float labelY = keyboardStartY + legendItem.relativeY + 14; // Near top edge
            menuFont.draw(stage.getBatch(), legendItem.label, labelX, labelY);
        }
        stage.getBatch().end();
    }

    /**
     * Draw a filled rounded rectangle using the ShapeRenderer
     */
    private void drawRoundedRect(float x, float y, float width, float height, float radius) {
        width -= 1;
        height -= 1;
        // Clamp radius to not exceed half of width or height
        float maxRadius = Math.min(width / 2, height / 2);
        radius = Math.min(radius, maxRadius);

        // Main rectangle (without corners)
        shapeRenderer.rect(x + radius, y, width - 2 * radius, height);
        shapeRenderer.rect(x, y + radius, width, height - 2 * radius);

        // Corner circles
        shapeRenderer.circle(x + radius, y + radius, radius, 8); // Bottom-left
        shapeRenderer.circle(x + width - radius, y + radius, radius, 8); // Bottom-right
        shapeRenderer.circle(x + radius, y + height - radius, radius, 8); // Top-left
        shapeRenderer.circle(x + width - radius, y + height - radius, radius, 8); // Top-right
    }

    protected void initializeKeyboardCommands() {
        // ---------------------------------------------------------
        // Function keys
        // ---------------------------------------------------------
//        addCommand("ESC", "Quit", KeyType.GameControl);
//        addCommand("F1", "Gamma Correction", KeyType.Debugging);
//        addCommand("F2", "Depth of Field", KeyType.Debugging);
//        addCommand("F3", "Bokeh", KeyType.Debugging);
//        addCommand("F4", "SSAO", KeyType.Debugging);

//        addCommand("F5", "Always Day", KeyType.Debugging);
//        addCommand("F6", "Demo Modes", KeyType.GameControl);
        // addCommand("F7", "");
        // addCommand("F8", "");

//        addCommand("F9", "Graphs", KeyType.Debugging);
//        addCommand("F10", "Toggle Debug Mode", KeyType.Debugging);
        // addCommand("F11", "");
        // addCommand("F12", "");

        // ---------------------------------------------------------
        // Numerical keys
        // ---------------------------------------------------------
        // addCommand("`", "Slide Shelton");
        // addCommand("1", "Cockpit Toggle");
        // addCommand("2", "Left Look");
        // addCommand("3", "Right Look");
        // addCommand("4", "Back Look");
        // addCommand("5", "View Chase");
        // addCommand("6", "View Panning");
        // addCommand("7", "View Target");
        // addCommand("8", "View Panning Target");
        // addCommand("9", "TC -");
        // addCommand("0", "TC +");
        // addCommand("-", "Velocity Decrease");
        // addCommand("=", "Velocity Increase");
        // addCommand("Backspace", "Relative Stop");

        // ---------------------------------------------------------
        // First letter row
        // ---------------------------------------------------------
//        addCommand("Tab", "Profiler (in Info Panel)", KeyType.Debugging);
//        addCommand("Q", "Rotate Camera Left", KeyType.Camera);
//        addCommand("W", "Move Camera Forward", KeyType.Camera);
//        addCommand("E", "Rotate Camera Right", KeyType.Camera);
        // addCommand("R", "Hostile Near Next");
        // addCommand("T", "Target Next All");
        // addCommand("Y", "Mode Combat Switch");
        // addCommand("U", "Target Manmade Next");
//        addCommand("I", "Info Panel", KeyType.UI);
        // addCommand("O", "Turret AI On");
        // addCommand("P", "Boresight Target");
        // addCommand("[", "Ship Change");
        // addCommand("]", "Message Text");
        // addCommand("\\", "Max Velocity");

        // ---------------------------------------------------------
        //- Second letter row
        // ---------------------------------------------------------
        // addCommand("CapsLock", "Caps Lock");
//        addCommand("A", "Move Camera Left", KeyType.Camera);
//        addCommand("S", "Move Camera Backward", KeyType.Camera);
//        addCommand("D", "Move Camera Right", KeyType.Camera);
//        addCommand("F", "Follow selected trader", KeyType.Camera);
        // addCommand("G", "");
//        addCommand("H", "HRTF", KeyType.Debugging);
        // addCommand("J", "Jump Drive");
        // addCommand("K", "Friend Near Next");
        // addCommand("L", "Target Lock");
        // addCommand(";", "Respawn");
        // addCommand("'", "Quote");
        // addCommand("Enter", "Fire Secondary");

        // ---------------------------------------------------------
        //- Third letter row
        // ---------------------------------------------------------
        // addCommand("LShift", "Left Shift");
        // addCommand("Z", "Up Pan");
        // addCommand("X", "X Key");
        // addCommand("C", "Reset Camera");
//        addCommand("V", "VSync", KeyType.Debugging);
        // addCommand("B", "Subunit Next");
        // addCommand("N", "Target Neutral Next");
        // addCommand("M", "Left VDU Cycle");
        // addCommand(",", "Up Thrust");
        // addCommand(".", "Down Thrust");
        // addCommand("/", "Forward Slash");
        // addCommand("RShift", "Right Shift");

        // ---------------------------------------------------------
        //- Forth letter row
        // ---------------------------------------------------------
        // addCommand("LCtrl", "Left Control");
        // addCommand("LWin", "Left Windows");
        // addCommand("LAlt", "Left Alt");
//        addCommand("Space", "Pause", KeyType.GameControl);
        // addCommand("RAlt", "Right Alt");
        // addCommand("RWin", "Right Windows");
        // addCommand("Menu", "Menu Key");
        // addCommand("RCtrl", "Right Control");

        // ---------------------------------------------------------
        //-
        // ---------------------------------------------------------

//        addCommand("PRNT", "Screenshot", KeyType.GameControl);
        // addCommand("ROLL", "");
//        addCommand("PAUSE", "Pause", KeyType.GameControl);

        // addCommand("Insert", "Left Roll");
        // addCommand("Home", "Velocity Match");
        // addCommand("PageUp", "Text Scroll Up");
        // addCommand("Delete", "Right Roll");
        // addCommand("End", "Velocity Zero");
        // addCommand("PageDown", "Text Scroll Down");

//        addCommand("Up", "Move Camera Forward", KeyType.Camera);
//        addCommand("Down", "Move Camera Backward", KeyType.Camera);
//        addCommand("Left", "Move Camera Left", KeyType.Camera);
//        addCommand("Right", "Move Camera Right", KeyType.Camera);

    }

    private void initializeKeyboardLayout() {
        allKeys.clear();

        float scaleFactor = 1f;
        // Function keys row (F1-F12) - using inner rect coordinates (outer + 0.5) and dimensions (68x68)
        addKeyFromSVG("ESC", 2f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("F1", 146f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("F2", 218f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("F3", 290f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("F4", 362f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("F5", 470f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("F6", 542f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("F7", 614f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("F8", 686f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("F9", 794f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("F10", 866f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("F11", 938f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("F12", 1010f, 2f, 68f, 68f, scaleFactor);

        // System keys
        addKeyFromSVG("PRNT", 1118f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("ROLL", 1190f, 2f, 68f, 68f, scaleFactor);
        addKeyFromSVG("PAUSE", 1262f, 2f, 68f, 68f, scaleFactor);

        // Number row
        addKeyFromSVG("`", 2f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("1", 74f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("2", 146f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("3", 218f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("4", 290f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("5", 362f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("6", 434f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("7", 506f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("8", 578f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("9", 650f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("0", 722f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("-", 794f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("=", 866f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("Backspace", 938f, 110f, 140f, 68f, scaleFactor); // Width 141-1 = 140

        // QWERTY row
        addKeyFromSVG("Tab", 2f, 182f, 104f, 68f, scaleFactor); // Width 105-1 = 104
        addKeyFromSVG("Q", 110f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("W", 182f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("E", 254f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("R", 326f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("T", 398f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("Y", 470f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("U", 542f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("I", 614f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("O", 686f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("P", 758f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("[", 830f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("]", 902f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("\\", 974f, 182f, 104f, 68f, scaleFactor); // Width 105-1 = 104

        // ASDF row
        addKeyFromSVG("CapsLock", 2f, 254f, 122f, 68f, scaleFactor); // Width 123-1 = 122
        addKeyFromSVG("A", 128f, 254f, 68f, 68f, scaleFactor);
        addKeyFromSVG("S", 200f, 254f, 68f, 68f, scaleFactor);
        addKeyFromSVG("D", 272f, 254f, 68f, 68f, scaleFactor);
        addKeyFromSVG("F", 344f, 254f, 68f, 68f, scaleFactor);
        addKeyFromSVG("G", 416f, 254f, 68f, 68f, scaleFactor);
        addKeyFromSVG("H", 488f, 254f, 68f, 68f, scaleFactor);
        addKeyFromSVG("J", 560f, 254f, 68f, 68f, scaleFactor);
        addKeyFromSVG("K", 632f, 254f, 68f, 68f, scaleFactor);
        addKeyFromSVG("L", 704f, 254f, 68f, 68f, scaleFactor);
        addKeyFromSVG(";", 776f, 254f, 68f, 68f, scaleFactor);
        addKeyFromSVG("'", 848f, 254f, 68f, 68f, scaleFactor);
        addKeyFromSVG("Enter", 920f, 254f, 158f, 68f, scaleFactor); // Width 159-1 = 158

        // ZXCV row
        addKeyFromSVG("LShift", 2f, 326f, 158f, 68f, scaleFactor); // Width 159-1 = 158
        addKeyFromSVG("Z", 164f, 326f, 68f, 68f, scaleFactor);
        addKeyFromSVG("X", 236f, 326f, 68f, 68f, scaleFactor);
        addKeyFromSVG("C", 308f, 326f, 68f, 68f, scaleFactor);
        addKeyFromSVG("V", 380f, 326f, 68f, 68f, scaleFactor);
        addKeyFromSVG("B", 452f, 326f, 68f, 68f, scaleFactor);
        addKeyFromSVG("N", 524f, 326f, 68f, 68f, scaleFactor);
        addKeyFromSVG("M", 596f, 326f, 68f, 68f, scaleFactor);
        addKeyFromSVG(",", 668f, 326f, 68f, 68f, scaleFactor);
        addKeyFromSVG(".", 740f, 326f, 68f, 68f, scaleFactor);
        addKeyFromSVG("/", 812f, 326f, 68f, 68f, scaleFactor);
        addKeyFromSVG("RShift", 884f, 326f, 194f, 68f, scaleFactor); // Width 195-1 = 194

        // Bottom row
        addKeyFromSVG("LCtrl", 2f, 398f, 86f, 68f, scaleFactor); // Width 87-1 = 86
        addKeyFromSVG("LWin", 92f, 398f, 86f, 68f, scaleFactor); // Width 87-1 = 86
        addKeyFromSVG("LAlt", 182f, 398f, 86f, 68f, scaleFactor); // Width 87-1 = 86
        addKeyFromSVG("Space", 272f, 398f, 446f, 68f, scaleFactor); // Width 447-1 = 446
        addKeyFromSVG("RAlt", 722f, 398f, 86f, 68f, scaleFactor); // Width 87-1 = 86
        addKeyFromSVG("RWin", 812f, 398f, 86f, 68f, scaleFactor); // Width 87-1 = 86
        addKeyFromSVG("Menu", 902f, 398f, 86f, 68f, scaleFactor); // Width 87-1 = 86
        addKeyFromSVG("RCtrl", 992f, 398f, 86f, 68f, scaleFactor); // Width 87-1 = 86

        // Arrow keys and navigation cluster
        addKeyFromSVG("Insert", 1118f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("Home", 1190f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("PageUp", 1262f, 110f, 68f, 68f, scaleFactor);
        addKeyFromSVG("Delete", 1118f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("End", 1190f, 182f, 68f, 68f, scaleFactor);
        addKeyFromSVG("PageDown", 1262f, 182f, 68f, 68f, scaleFactor);

        // Arrow keys
        addKeyFromSVG("Up", 1190f, 326f, 68f, 68f, scaleFactor);
        addKeyFromSVG("Left", 1118f, 398f, 68f, 68f, scaleFactor);
        addKeyFromSVG("Down", 1190f, 398f, 68f, 68f, scaleFactor);
        addKeyFromSVG("Right", 1262f, 398f, 68f, 68f, scaleFactor);
    }

    private void initializeLegendLayout() {
        legendItems.clear();
        float scaleFactor = 1f;
        addLegendFromSVG("Combat", KeyType.Combat, 110f, 506f, 15, 15, scaleFactor);
        addLegendFromSVG("UI", KeyType.UI, 310f, 506f, 15, 15, scaleFactor);
        addLegendFromSVG("Targeting", KeyType.Targeting, 110f, 526f, 15, 15, scaleFactor);
        addLegendFromSVG("Camera", KeyType.Camera, 310f, 526f, 15, 15, scaleFactor);
        addLegendFromSVG("Navigation", KeyType.Navigation, 110f, 546f, 15, 15, scaleFactor);
        addLegendFromSVG("Game Control", KeyType.GameControl, 310f, 546f, 15, 15, scaleFactor);
        addLegendFromSVG("Debugging", KeyType.Debugging, 110f, 566f, 15, 15, scaleFactor);

    }

    private boolean isPaused() {
        return gameEngine.isPaused();
    }

    public void render(float deltaTime, boolean showKeyAssignments) {
        Batch batch = gameEngine.getRenderEngine().renderEngine2D.batch;
        if (!isPaused()) return;

        float screenWidth  = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        // Draw semi-transparent overlay
        batch.setColor(OVERLAY_COLOR);
        batch.draw(atlasRegion, 0, 0, screenWidth, screenHeight);
        batch.setColor(Color.WHITE);

        // Draw UI labels
        stage.draw();

        // End batch to draw shapes
        batch.end();
        if (showKeyAssignments) {
            // Draw keyboard layout and connecting lines
            drawKeyboardLayout();
            drawLegend();
        }
        // Restart batch for any additional rendering
        batch.begin();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (isPaused()) {
            calculateLayout();
        }
    }

    protected enum KeyType {
        Combat(ColorUtil.srgbToLinear(new Color(0xFC9C93FF))),
        UI(ColorUtil.srgbToLinear(new Color(0x0CC9D9FF))),
        Targeting(ColorUtil.srgbToLinear(new Color(0xCEB46CFF))),
        Camera(ColorUtil.srgbToLinear(new Color(0x80B9FFFF))),
        Navigation(ColorUtil.srgbToLinear(new Color(0x7CC690FF))),
        GameControl(ColorUtil.srgbToLinear(new Color(0xE4A0DBFF))),//f3d0ee
        Debugging(ColorUtil.srgbToLinear(new Color(0x880000FF)));
        @Getter
        private final Color color;

        KeyType(Color color) {
            this.color = color;
        }
    }

    /**
     * Represents a single keyboard command
     */
    private record KeyboardCommand(String key, String description, KeyType keyType) {
    }

    /**
     * Represents a physical key on the keyboard
     */
    private record KeyboardKey(String label, float relativeX, float relativeY, float width, float height) {
    }

    private record LegendItem(String label, KeyType keyType, float relativeX, float relativeY, float width,
                              float height) {
    }

}

