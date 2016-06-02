package com.blackducksoftware.integration.jira.config;

public class HubProjectMapping {

	private String jiraProject;
	private String hubProjectLink;

	public HubProjectMapping() {

	}

	public String getJiraProject() {
		return jiraProject;
	}

	public void setJiraProject(final String jiraProject) {
		this.jiraProject = jiraProject;
	}

	public String getHubProjectLink() {
		return hubProjectLink;
	}

	public void setHubProjectLink(final String hubProjectLink) {
		this.hubProjectLink = hubProjectLink;
	}

}
