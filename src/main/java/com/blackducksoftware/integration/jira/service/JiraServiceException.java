package com.blackducksoftware.integration.jira.service;

public class JiraServiceException extends Exception {

	public JiraServiceException() {
	}

	public JiraServiceException(String message) {
		super(message);
	}

	public JiraServiceException(Throwable cause) {
		super(cause);
	}

	public JiraServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public JiraServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
