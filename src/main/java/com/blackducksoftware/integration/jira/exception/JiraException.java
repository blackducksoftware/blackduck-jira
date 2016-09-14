package com.blackducksoftware.integration.jira.exception;

public class JiraException extends Exception {

	private static final long serialVersionUID = -5115029798246254838L;

	public JiraException(final String message) {
		super(message);
	}

	public JiraException(final Throwable cause) {
		super(cause);
	}

	public JiraException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public JiraException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
