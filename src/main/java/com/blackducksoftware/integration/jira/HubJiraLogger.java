package com.blackducksoftware.integration.jira;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

public class HubJiraLogger implements IntLogger {
	private final Logger jiraLogger;
	private LogLevel logLevel = LogLevel.DEBUG;

	public HubJiraLogger(final Logger logger) {
		this.jiraLogger = logger;
	}

	public Logger getJiraLogger() {
		return jiraLogger;
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}

	@Override
	public void setLogLevel(final LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public void info(final String txt) {
		if (LogLevel.isLoggable(logLevel, LogLevel.INFO)) {
			logMessageInfo(txt);
		}
	}

	@Override
	public void error(final Throwable t) {
		if (LogLevel.isLoggable(logLevel, LogLevel.ERROR)) {
			logThrowable(t);
		}
	}

	@Override
	public void error(final String txt, final Throwable t) {
		if (LogLevel.isLoggable(logLevel, LogLevel.ERROR)) {
			logThrowable(txt, t);
		}
	}

	@Override
	public void error(final String txt) {
		if (LogLevel.isLoggable(logLevel, LogLevel.ERROR)) {
			logErrorMessage(txt);
		}
	}

	@Override
	public void warn(final String txt) {
		if (LogLevel.isLoggable(logLevel, LogLevel.WARN)) {
			logMessageWarn(txt);
		}
	}

	@Override
	public void trace(final String txt) {
		if (LogLevel.isLoggable(logLevel, LogLevel.TRACE)) {
			logMessageTrace(txt);
		}
	}

	@Override
	public void trace(final String txt, final Throwable t) {
		if (LogLevel.isLoggable(logLevel, LogLevel.TRACE)) {
			logThrowable(txt, t);
		}
	}

	@Override
	public void debug(final String txt) {
		if (LogLevel.isLoggable(logLevel, LogLevel.DEBUG)) {
			logMessageDebug(txt);
		}
	}

	@Override
	public void debug(final String txt, final Throwable t) {
		if (LogLevel.isLoggable(logLevel, LogLevel.DEBUG)) {
			logThrowable(txt, t);
		}
	}

	private void logMessageInfo(final String txt) {
		if (txt != null) {
			if (jiraLogger != null) {
				jiraLogger.info(txt);
			} else {
				System.out.println(txt);
			}
		}
	}

	private void logMessageDebug(final String txt) {
		if (txt != null) {
			if (jiraLogger != null) {
				jiraLogger.debug(txt);
			} else {
				System.out.println(txt);
			}
		}
	}

	private void logMessageWarn(final String txt) {
		if (txt != null) {
			if (jiraLogger != null) {
				jiraLogger.warn(txt);
			} else {
				System.out.println(txt);
			}
		}
	}

	private void logMessageTrace(final String txt) {
		if (txt != null) {
			if (jiraLogger != null) {
				jiraLogger.trace(txt);
			} else {
				System.out.println(txt);
			}
		}
	}

	private void logThrowable(final Throwable throwable) {
		logThrowable("An error occurred caused by ", throwable);
	}

	private void logThrowable(final String txt, final Throwable throwable) {
		if (txt != null) {
			if (jiraLogger != null) {
				jiraLogger.error(txt, throwable);
			} else {
				final StringWriter sw = new StringWriter();
				throwable.printStackTrace(new PrintWriter(sw));
				System.err.println(sw.toString());
			}
		}
	}

	private void logErrorMessage(final String txt) {
		if (txt != null) {
			if (jiraLogger != null) {
				jiraLogger.error(txt);
			} else {
				System.out.println(txt);
			}
		}
	}
}
