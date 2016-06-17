package com.blackducksoftware.integration.jira.issue;

import com.blackducksoftware.integration.jira.hub.model.NameVersion;

public class Issue {
	private final String issueTypeDescription;
	private final IssueLevel level;
	private final NameVersion hubProject;
	private final NameVersion hubComponent;
	private final String ruleUrl;
	private final String ruleName;
	private final String jiraProjectKey;

	public Issue(final String issueTypeDescription, final IssueLevel level, final NameVersion hubProject,
			final NameVersion hubComponent, final String ruleUrl, final String ruleName, final String jiraProjectKey) {
		super();
		this.issueTypeDescription = issueTypeDescription;
		this.level = level;
		this.hubProject = hubProject;
		this.hubComponent = hubComponent;
		this.ruleUrl = ruleUrl;
		this.ruleName = ruleName;
		this.jiraProjectKey = jiraProjectKey;
	}

	public String getIssueTypeDescription() {
		return issueTypeDescription;
	}
	public IssueLevel getLevel() {
		return level;
	}
	public NameVersion getHubProject() {
		return hubProject;
	}
	public NameVersion getHubComponent() {
		return hubComponent;
	}
	public String getRuleUrl() {
		return ruleUrl;
	}
	public String getRuleName() {
		return ruleName;
	}
	public String getJiraProjectKey() {
		return jiraProjectKey;
	}
	@Override
	public String toString() {
		return "Issue [issueTypeDescription=" + issueTypeDescription + ", level=" + level + ", hubProject="
				+ hubProject + ", hubComponent=" + hubComponent + ", ruleUrl=" + ruleUrl + ", ruleName=" + ruleName
				+ ", jiraProjectKey=" + jiraProjectKey + "]";
	}



}
