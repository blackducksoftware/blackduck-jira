package com.blackducksoftware.integration.jira.hub;

public class HubNotificationServiceException extends Exception {

	private static final long serialVersionUID = -5409459979943538039L;

	public HubNotificationServiceException() {
		super();
	}

	public HubNotificationServiceException(String message) {
		super(message);
	}

	public HubNotificationServiceException(Throwable cause) {
		super(cause);
	}

	public HubNotificationServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public HubNotificationServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
