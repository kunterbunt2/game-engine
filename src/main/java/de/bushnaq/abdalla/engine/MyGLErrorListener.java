package de.bushnaq.abdalla.engine;

import static com.badlogic.gdx.graphics.profiling.GLInterceptor.resolveErrorNumber;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;

/**
 * @author kunterbunt
 *
 */
public class MyGLErrorListener implements GLErrorListener {

	public MyGLErrorListener() {
	}

	@Override
	public void onError(final int error) {
		String place = null;
		Thread.dumpStack();
		try {
			final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			for (int i = 0; i < stack.length; i++) {
				if ("check".equals(stack[i].getMethodName())) {
					if (i + 1 < stack.length) {
						final StackTraceElement glMethod = stack[i + 1];
						place = glMethod.getMethodName();
					}
					break;
				}
			}
		} catch (final Exception ignored) {
		}

		if (place != null) {
			Gdx.app.error("GLProfiler", "Error " + resolveErrorNumber(error) + " from " + place);
		} else {
			Gdx.app.error("GLProfiler", "Error " + resolveErrorNumber(error) + " at: ", new Exception());
			// This will capture current stack trace for logging, if possible
		}
		// System.exit(error);
	}
}