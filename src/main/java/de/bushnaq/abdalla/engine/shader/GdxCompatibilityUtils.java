package de.bushnaq.abdalla.engine.shader;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import com.badlogic.gdx.graphics.glutils.VertexData;

/**
 * @author kunterbunt
 *
 */
public class GdxCompatibilityUtils {
	public static VertexData createVertexBuffer(final boolean isStatic, final int numVertices, final VertexAttributes attributes) {
		if (Gdx.gl30 != null)
			return new VertexBufferObjectWithVAO(isStatic, numVertices, attributes);
		else
			return new VertexBufferObject(isStatic, numVertices, attributes);
	}

	public static String getShaderVersionCode() {
		// To support OpenGL, work with this:
		// config.useGL30 = true;
		// ShaderProgram.prependVertexCode = "#version 140\n#define varying out\n#define attribute in\n";
		// ShaderProgram.prependFragmentCode = "#version 140\n#define varying in\n#define texture2D texture\n#define gl_FragColor fragColor\nout vec4 fragColor;\n";
		if (Gdx.app.getType() == Application.ApplicationType.Desktop)
			return "#version 120\n";
		else
			return "#version 100\n";
	}
}
