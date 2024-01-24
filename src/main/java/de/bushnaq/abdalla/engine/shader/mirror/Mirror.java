package de.bushnaq.abdalla.engine.shader.mirror;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FrameBufferBuilder;

/**
 * @author kunterbunt
 *
 */
public class Mirror {
	private boolean		present			= false;
	private FrameBuffer	reflectionFbo;

	private float		reflectivity	= 0.5f;

	public Mirror() {

	}
	public void createFrameBuffer() {
		{
			final FrameBufferBuilder frameBufferBuilder = new FrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			frameBufferBuilder.addColorTextureAttachment(GL30.GL_RGBA8, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE);
			frameBufferBuilder.addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT24);
			reflectionFbo = frameBufferBuilder.build();
		}
	}

	public void dispose() {
		reflectionFbo.dispose();
	}

	public FrameBuffer getReflectionFbo() {
		return reflectionFbo;
	}

	public float getReflectivity() {
		return reflectivity;
	}

	public boolean isPresent() {
		return present;
	}

	public void setPresent(boolean present) {
		this.present = present;
	}

	public void setReflectivity(float reflectivity) {
		this.reflectivity = reflectivity;
	}

}
