package com.blackducksoftware.integration.jira.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;

public class HubIssueTypeSetup {

	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

	private final JiraSettingsService settingService;

	private final Collection<IssueType> issueTypes;

	public HubIssueTypeSetup(final JiraSettingsService settingService, final Collection<IssueType> issueTypes) {
		this.settingService = settingService;
		this.issueTypes = issueTypes;
	}

	// TODO create our issueTypes AND add them to each Projects workflow
	// scheme before we try addWorkflowToProjectsWorkflowScheme

	public List<IssueType> addIssueTypesToJira() {
		final List<IssueType> issueTypes = new ArrayList<>();

		for (final IssueType issueType : issueTypes) {
			if (issueType.getName().equals(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE)
					|| issueType.getName().equals(HubJiraConstants.HUB_VULNERABILITY_ISSUE)) {
				issueTypes.add(issueType);
			}
		}
		if (!issueTypes.isEmpty()) {
			// TODO could not find our issue types so lets add them
		}

		return issueTypes;
	}

	public void addIssueTypesToProject(final Project jiraProject, final List<IssueType> hubIssueTypes) {
		// TODO
	}

}
