package de.bushnaq.abdalla.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

/**
 * Simple solution: Custom Label that draws background manually.
 * Uses existing system texture from AtlasManager.
 * Most lightweight approach - no additional textures needed.
 */
public class RichLabel extends Label {
    private final Color         backgroundColor = new Color(0, 0, 0, 0.8f);
    private final TextureRegion backgroundTexture;
    private final GlyphLayout   layout   = new GlyphLayout();
    private       float         paddingH = 6f;
    private float         paddingV        = 3f;

    public RichLabel(CharSequence text, LabelStyle style, TextureRegion backgroundTexture) {
        super(text, style);
        this.backgroundTexture = backgroundTexture;
        updateSize();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Draw background first
        if (backgroundTexture != null) {
            Color originalColor = batch.getColor();
            batch.setColor(backgroundColor.r, backgroundColor.g, backgroundColor.b,
                    backgroundColor.a * parentAlpha);
            batch.draw(backgroundTexture, getX(), getY(), getWidth(), getHeight());
            batch.setColor(originalColor);
        }

        // Then draw the text
        super.draw(batch, parentAlpha);
    }

    public void setBackgroundAlpha(float alpha) {
        this.backgroundColor.a = alpha;
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor.set(color);
    }

    public void setPadding(float horizontal, float vertical) {
        this.paddingH = horizontal;
        this.paddingV = vertical;
        updateSize();
    }

    @Override
    public void setText(CharSequence newText) {
        super.setText(newText);
        updateSize();
    }

    private void updateSize() {
        if (getStyle() != null && getStyle().font != null) {
            layout.setText(getStyle().font, getText());
            setSize(layout.width + paddingH * 2, layout.height + paddingV * 2);
        }
    }
}
