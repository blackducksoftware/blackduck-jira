package com.blackducksoftware.integration.jira.config;

public class HubProjectMapping {

	private final String jiraProject;
	private final String hubProjectLink;

	public HubProjectMapping(final String jiraProject, final String hubProjectLink) {
		this.jiraProject = jiraProject;
		this.hubProjectLink = hubProjectLink;
	}

	public String getJiraProject() {
		return jiraProject;
	}

	public String getHubProjectLink() {
		return hubProjectLink;
	}


}
