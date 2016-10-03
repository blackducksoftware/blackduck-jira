package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class HubAdminConfigSerializable implements Serializable {

	private static final long serialVersionUID = -5925523949026662425L;

	@XmlElement
	private String hubJiraGroups;

	@XmlElement
	private List<String> jiraGroups;

	@XmlElement
	private String hubJiraGroupsError;

	public HubAdminConfigSerializable() {
	}

	public String getHubJiraGroups() {
		return hubJiraGroups;
	}

	public void setHubJiraGroups(final String hubJiraGroups) {
		this.hubJiraGroups = hubJiraGroups;
	}

	public List<String> getJiraGroups() {
		return jiraGroups;
	}

	public void setJiraGroups(final List<String> jiraGroups) {
		this.jiraGroups = jiraGroups;
	}

	public String getHubJiraGroupsError() {
		return hubJiraGroupsError;
	}

	public void setHubJiraGroupsError(final String hubJiraGroupsError) {
		this.hubJiraGroupsError = hubJiraGroupsError;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hubJiraGroups == null) ? 0 : hubJiraGroups.hashCode());
		result = prime * result + ((hubJiraGroupsError == null) ? 0 : hubJiraGroupsError.hashCode());
		result = prime * result + ((jiraGroups == null) ? 0 : jiraGroups.hashCode());
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
		if (!(obj instanceof HubAdminConfigSerializable)) {
			return false;
		}
		final HubAdminConfigSerializable other = (HubAdminConfigSerializable) obj;
		if (hubJiraGroups == null) {
			if (other.hubJiraGroups != null) {
				return false;
			}
		} else if (!hubJiraGroups.equals(other.hubJiraGroups)) {
			return false;
		}
		if (hubJiraGroupsError == null) {
			if (other.hubJiraGroupsError != null) {
				return false;
			}
		} else if (!hubJiraGroupsError.equals(other.hubJiraGroupsError)) {
			return false;
		}
		if (jiraGroups == null) {
			if (other.jiraGroups != null) {
				return false;
			}
		} else if (!jiraGroups.equals(other.jiraGroups)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("HubAdminConfigSerializable [hubJiraGroups=");
		builder.append(hubJiraGroups);
		builder.append(", jiraGroups=");
		builder.append(jiraGroups);
		builder.append(", hubJiraGroupsError=");
		builder.append(hubJiraGroupsError);
		builder.append("]");
		return builder.toString();
	}

}
