package com.blackducksoftware.integration.jira.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class HubProjectMapping {

	@XmlElement
	private String jiraProjectDisplayName;

	@XmlElement
	private String jiraProjectKey;

	@XmlElement
	private boolean jiraProjectExists;

	@XmlElement
	private String hubProjectDisplayName;

	@XmlElement
	private String hubProjectKey;

	@XmlElement
	private boolean hubProjectExists;

	public String getJiraProjectDisplayName() {
		return jiraProjectDisplayName;
	}

	public void setJiraProjectDisplayName(final String jiraProjectDisplayName) {
		this.jiraProjectDisplayName = jiraProjectDisplayName;
	}

	public String getJiraProjectKey() {
		return jiraProjectKey;
	}

	public void setJiraProjectKey(final String jiraProjectKey) {
		this.jiraProjectKey = jiraProjectKey;
	}

	public String getHubProjectDisplayName() {
		return hubProjectDisplayName;
	}

	public void setHubProjectDisplayName(final String hubProjectDisplayName) {
		this.hubProjectDisplayName = hubProjectDisplayName;
	}

	public String getHubProjectKey() {
		return hubProjectKey;
	}

	public void setHubProjectKey(final String hubProjectKey) {
		this.hubProjectKey = hubProjectKey;
	}

	public boolean isJiraProjectExists() {
		return jiraProjectExists;
	}

	public void setJiraProjectExists(final boolean jiraProjectExists) {
		this.jiraProjectExists = jiraProjectExists;
	}

	public boolean isHubProjectExists() {
		return hubProjectExists;
	}

	public void setHubProjectExists(final boolean hubProjectExists) {
		this.hubProjectExists = hubProjectExists;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hubProjectDisplayName == null) ? 0 : hubProjectDisplayName.hashCode());
		result = prime * result + (hubProjectExists ? 1231 : 1237);
		result = prime * result + ((hubProjectKey == null) ? 0 : hubProjectKey.hashCode());
		result = prime * result + ((jiraProjectDisplayName == null) ? 0 : jiraProjectDisplayName.hashCode());
		result = prime * result + (jiraProjectExists ? 1231 : 1237);
		result = prime * result + ((jiraProjectKey == null) ? 0 : jiraProjectKey.hashCode());
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
		if (!(obj instanceof HubProjectMapping)) {
			return false;
		}
		final HubProjectMapping other = (HubProjectMapping) obj;
		if (hubProjectDisplayName == null) {
			if (other.hubProjectDisplayName != null) {
				return false;
			}
		} else if (!hubProjectDisplayName.equals(other.hubProjectDisplayName)) {
			return false;
		}
		if (hubProjectExists != other.hubProjectExists) {
			return false;
		}
		if (hubProjectKey == null) {
			if (other.hubProjectKey != null) {
				return false;
			}
		} else if (!hubProjectKey.equals(other.hubProjectKey)) {
			return false;
		}
		if (jiraProjectDisplayName == null) {
			if (other.jiraProjectDisplayName != null) {
				return false;
			}
		} else if (!jiraProjectDisplayName.equals(other.jiraProjectDisplayName)) {
			return false;
		}
		if (jiraProjectExists != other.jiraProjectExists) {
			return false;
		}
		if (jiraProjectKey == null) {
			if (other.jiraProjectKey != null) {
				return false;
			}
		} else if (!jiraProjectKey.equals(other.jiraProjectKey)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("HubProjectMapping [jiraProjectDisplayName=");
		builder.append(jiraProjectDisplayName);
		builder.append(", jiraProjectKey=");
		builder.append(jiraProjectKey);
		builder.append(", jiraProjectExists=");
		builder.append(jiraProjectExists);
		builder.append(", hubProjectDisplayName=");
		builder.append(hubProjectDisplayName);
		builder.append(", hubProjectKey=");
		builder.append(hubProjectKey);
		builder.append(", hubProjectExists=");
		builder.append(hubProjectExists);
		builder.append("]");
		return builder.toString();
	}

}
