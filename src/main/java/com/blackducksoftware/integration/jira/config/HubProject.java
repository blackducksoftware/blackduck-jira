package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class HubProject implements Serializable {

	private static final long serialVersionUID = 7694431556001276668L;

	@XmlElement
	private String projectName;

	@XmlElement
	private String projectUrl;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	public String getProjectUrl() {
		return projectUrl;
	}

	public void setProjectUrl(final String projectUrl) {
		this.projectUrl = projectUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
		result = prime * result + ((projectUrl == null) ? 0 : projectUrl.hashCode());
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
		if (!(obj instanceof HubProject)) {
			return false;
		}
		final HubProject other = (HubProject) obj;
		if (projectName == null) {
			if (other.projectName != null) {
				return false;
			}
		} else if (!projectName.equals(other.projectName)) {
			return false;
		}
		if (projectUrl == null) {
			if (other.projectUrl != null) {
				return false;
			}
		} else if (!projectUrl.equals(other.projectUrl)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("HubProject [projectName=");
		builder.append(projectName);
		builder.append(", projectUrl=");
		builder.append(projectUrl);
		builder.append("]");
		return builder.toString();
	}

}
