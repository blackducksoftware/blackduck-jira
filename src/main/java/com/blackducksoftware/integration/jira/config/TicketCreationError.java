package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class TicketCreationError implements Serializable {

	private static final long serialVersionUID = 8705688400750977007L;

	@XmlElement
	private String stackTrace;

	@XmlElement
	private String timeStamp;

	public TicketCreationError() {
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(final String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(final String timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("TicketCreationError [stackTrace=");
		builder.append(stackTrace);
		builder.append(", timeStamp=");
		builder.append(timeStamp);
		builder.append("]");
		return builder.toString();
	}
}
