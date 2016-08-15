package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class TicketCreationError implements Serializable {

	private static final long serialVersionUID = -2288742710417545610L;

	@XmlElement
	private final String message;

	@XmlElement
	private final String uiStackTrace;

	public TicketCreationError(final String message, final String uiStackTrace) {
		this.message = message;
		this.uiStackTrace = uiStackTrace;
	}

	public String getMessage() {
		return message;
	}

	public String getUiStackTrace() {
		return uiStackTrace;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("TicketCreationError [message=");
		builder.append(message);
		builder.append(", uiStackTrace=");
		builder.append(uiStackTrace);
		builder.append("]");
		return builder.toString();
	}

}
