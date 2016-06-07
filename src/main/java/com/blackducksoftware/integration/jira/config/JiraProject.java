package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JiraProject implements Serializable {

	private static final long serialVersionUID = 8278577774809760400L;

	@XmlElement
	private String projectName;

	@XmlElement
	private Long projectId;

	@XmlElement
	private Boolean projectExists;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(final Long projectId) {
		this.projectId = projectId;
	}

	public Boolean getProjectExists() {
		return projectExists;
	}

	public void setProjectExists(final Boolean projectExists) {
		this.projectExists = projectExists;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((projectExists == null) ? 0 : projectExists.hashCode());
		result = prime * result + ((projectId == null) ? 0 : projectId.hashCode());
		result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
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
		if (!(obj instanceof JiraProject)) {
			return false;
		}
		final JiraProject other = (JiraProject) obj;
		if (projectExists == null) {
			if (other.projectExists != null) {
				return false;
			}
		} else if (!projectExists.equals(other.projectExists)) {
			return false;
		}
		if (projectId == null) {
			if (other.projectId != null) {
				return false;
			}
		} else if (!projectId.equals(other.projectId)) {
			return false;
		}
		if (projectName == null) {
			if (other.projectName != null) {
				return false;
			}
		} else if (!projectName.equals(other.projectName)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("JiraProject [projectName=");
		builder.append(projectName);
		builder.append(", projectId=");
		builder.append(projectId);
		builder.append(", projectExists=");
		builder.append(projectExists);
		builder.append("]");
		return builder.toString();
	}

}
