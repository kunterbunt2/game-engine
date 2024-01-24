package de.bushnaq.abdalla.engine.util.logger;

import com.badlogic.gdx.Gdx;

/**
 * @author kunterbunt
 *
 */
public class Logger {
	private static final String	CAUSE_CAPTION	= "Caused by: ";

	private String				name;

	public Logger(String name) {
		this.name = name;
	}

	public void error(String message) {
		logError(message);
	}

	public void error(String message, Throwable e) {
		logError(message, e);
	}

	public void info(String message) {
		logInfo(message);
	}

	public void info(String message, Throwable e) {
		logInfo(message, e);
	}

	private void logError(String message) {
		if (Gdx.app == null) {
			System.out.println("[error] " + name + " - " + message);
		} else {
			Gdx.app.error(name, message);
		}
	}

	private void logError(String message, Throwable e) {
		e.printStackTrace();
		logError("------------------------------------------------------");
		logError(message);
		StackTraceElement[] trace = e.getStackTrace();
		for (StackTraceElement traceElement : trace)
			logError("\tat " + traceElement);

		// Print suppressed exceptions, if any
		// for (Throwable se : e.getSuppressed())

		// se.getgetEnclosed
		// printEnclosedStackTrace(s, trace, SUPPRESSED_CAPTION, "\t", dejaVu);

		// Print cause, if any
		// Throwable ourCause = e.getCause();
		// if (ourCause != null)
		// ourCause.printEnclosedStackTrace(s, trace, CAUSE_CAPTION, "", dejaVu);
	}

	private void logInfo(String message) {
		if (Gdx.app == null) {
			System.out.println("[info] " + name + " - " + message);
		} else {
			Gdx.app.log(name, message);
		}
	}

	private void logInfo(String message, Throwable e) {
		e.printStackTrace(System.out);
		logInfo("------------------------------------------------------");
		logInfo(message);
		StackTraceElement[] trace = e.getStackTrace();
		for (StackTraceElement traceElement : trace)
			logInfo("\tat " + traceElement);

		// Print suppressed exceptions, if any
		// for (Throwable se : e.getSuppressed())

		// se.getgetEnclosed
		// printEnclosedStackTrace(s, trace, SUPPRESSED_CAPTION, "\t", dejaVu);

		// Print cause, if any
		// Throwable ourCause = e.getCause();
		// if (ourCause != null)
		// ourCause.printEnclosedStackTrace(s, trace, CAUSE_CAPTION, "", dejaVu);
	}

	public void warn(String message) {
		logError(message);
	}

	public void warn(String message, Throwable e) {
		logError(message, e);
	}

}
