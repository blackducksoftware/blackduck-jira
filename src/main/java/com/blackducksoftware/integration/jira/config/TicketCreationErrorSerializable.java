package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class TicketCreationErrorSerializable implements Serializable {

	private static final long serialVersionUID = -5335895094076488435L;

	@XmlElement
	private Set<TicketCreationError> hubJiraTicketErrors;

	public Set<TicketCreationError> getHubJiraTicketErrors() {
		return hubJiraTicketErrors;
	}

	public void setHubJiraTicketErrors(final Set<TicketCreationError> hubJiraTicketErrors) {
		this.hubJiraTicketErrors = hubJiraTicketErrors;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hubJiraTicketErrors == null) ? 0 : hubJiraTicketErrors.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TicketCreationErrorSerializable)) {
			return false;
		}
		final TicketCreationErrorSerializable other = (TicketCreationErrorSerializable) obj;
		if (hubJiraTicketErrors == null) {
			if (other.hubJiraTicketErrors != null) {
				return false;
			}
		} else if (!hubJiraTicketErrors.equals(other.hubJiraTicketErrors)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("TicketCreationErrorSerializable [hubJiraTicketErrors=");
		builder.append(hubJiraTicketErrors);
		builder.append("]");
		return builder.toString();
	}

}
