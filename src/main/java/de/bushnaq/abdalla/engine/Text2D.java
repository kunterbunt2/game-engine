package de.bushnaq.abdalla.engine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.utils.Align;

/**
 * @author kunterbunt
 * 2D Text layered on top of 3D scene.
 * TODO example
 *
 */
public class Text2D {
	private Color	color;
	BitmapFont		font;
	String			text;
	int				x;
	int				y;

	public Text2D(final String text, int x, int y, Color color, final BitmapFont font) {
		this.text = text;
		this.x = x;
		this.y = y;
		this.color = color;
		this.font = font;
	}

	public void draw(PolygonSpriteBatch batch2d) {
		final GlyphLayout layout = new GlyphLayout();
		layout.setText(font, text);
		final float width = layout.width;// contains the width of the current set text
		font.setColor(color);
		font.draw(batch2d, text, x, y, width, Align.left, false);
	}

	public Color getColor() {
		return color;
	}

	public String getText() {
		return text;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}
}