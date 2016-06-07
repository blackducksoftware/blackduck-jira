package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class HubProjectMapping implements Serializable {

	private static final long serialVersionUID = 5092202477431243180L;

	@XmlElement
	private JiraProject jiraProject;

	@XmlElement
	private HubProject hubProject;

	public JiraProject getJiraProject() {
		return jiraProject;
	}

	public void setJiraProject(final JiraProject jiraProject) {
		this.jiraProject = jiraProject;
	}

	public HubProject getHubProject() {
		return hubProject;
	}

	public void setHubProject(final HubProject hubProject) {
		this.hubProject = hubProject;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hubProject == null) ? 0 : hubProject.hashCode());
		result = prime * result + ((jiraProject == null) ? 0 : jiraProject.hashCode());
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
		if (getClass() != obj.getClass()) {
			return false;
		}
		final HubProjectMapping other = (HubProjectMapping) obj;
		if (hubProject == null) {
			if (other.hubProject != null) {
				return false;
			}
		} else if (!hubProject.equals(other.hubProject)) {
			return false;
		}
		if (jiraProject == null) {
			if (other.jiraProject != null) {
				return false;
			}
		} else if (!jiraProject.equals(other.jiraProject)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("HubProjectMapping [jiraProject=");
		builder.append(jiraProject);
		builder.append(", hubProject=");
		builder.append(hubProject);
		builder.append("]");
		return builder.toString();
	}


}
