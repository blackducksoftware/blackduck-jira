package com.blackducksoftware.integration.jira.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.hub.HubNotificationServiceException;
import com.blackducksoftware.integration.jira.hub.TicketGeneratorInfo;

public class HubProjectMappings {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final TicketGeneratorInfo ticketGenInfo;
	private final Set<HubProjectMapping> mappings;

	public HubProjectMappings(final TicketGeneratorInfo ticketGenInfo, final Set<HubProjectMapping> mappings) {
		this.ticketGenInfo = ticketGenInfo;
		this.mappings = mappings;
	}

	public List<JiraProject> getJiraProjects(final String hubProjectUrl) {
		final List<JiraProject> matchingJiraProjects = new ArrayList<>();

		if (mappings == null || mappings.isEmpty()) {
			logger.debug("There are no configured project mapping");
			return matchingJiraProjects;
		}

		for (final HubProjectMapping mapping : mappings) {
			final JiraProject mappingJiraProject = mapping.getJiraProject();
			final JiraProject jiraProject;
			try {
				jiraProject = getJiraProject(mappingJiraProject.getProjectId());
			} catch (final HubNotificationServiceException e) {
				logger.warn("Mapped project '" + mappingJiraProject.getProjectName() + "' with ID "
						+ mappingJiraProject.getProjectId() + " not found in JIRA; skipping this notification");
				continue;
			}
			if (StringUtils.isNotBlank(jiraProject.getProjectError())) {
				logger.error(jiraProject.getProjectError());
				continue;
			}

			logger.debug("JIRA Project: " + jiraProject);

			final HubProject hubProject = mapping.getHubProject();

			logger.debug("hubProject.getProjectUrl() (from config mapping): " + hubProject.getProjectUrl());
			logger.debug("hubProjectUrl (from notification content)       : " + hubProjectUrl);
			if ((!StringUtils.isBlank(hubProject.getProjectUrl()) && (hubProject.getProjectUrl().equals(hubProjectUrl)))) {
				logger.debug("Match!");
				matchingJiraProjects.add(jiraProject);
			}
		}
		logger.debug("Number of matches found: " + matchingJiraProjects.size());
		return matchingJiraProjects;
	}

	private JiraProject getJiraProject(final long jiraProjectId)
			throws HubNotificationServiceException {
		if (ticketGenInfo.getJiraProjectManager() == null) {
			throw new HubNotificationServiceException("The JIRA projectManager has not been set");
		}
		final com.atlassian.jira.project.Project atlassianJiraProject = ticketGenInfo.getJiraProjectManager()
				.getProjectObj(jiraProjectId);
		if (atlassianJiraProject == null) {
			throw new HubNotificationServiceException("Error: JIRA Project with ID " + jiraProjectId + " not found");
		}
		final String jiraProjectKey = atlassianJiraProject.getKey();
		final String jiraProjectName = atlassianJiraProject.getName();
		final JiraProject bdsJiraProject = new JiraProject();
		bdsJiraProject.setProjectId(jiraProjectId);
		bdsJiraProject.setProjectKey(jiraProjectKey);
		bdsJiraProject.setProjectName(jiraProjectName);

		if (atlassianJiraProject.getIssueTypes() == null || atlassianJiraProject.getIssueTypes().isEmpty()) {
			bdsJiraProject.setProjectError("The Jira project : " + bdsJiraProject.getProjectName()
					+ " does not have any issue types, we will not be able to create tickets for this project.");
		} else {
			boolean projectHasIssueType = false;
			if (atlassianJiraProject.getIssueTypes() != null && !atlassianJiraProject.getIssueTypes().isEmpty()) {
				for (final IssueType issueType : atlassianJiraProject.getIssueTypes()) {
					if (issueType.getName().equals(ticketGenInfo.getJiraIssueTypeName())) {
						bdsJiraProject.setIssueTypeId(issueType.getId());
						projectHasIssueType = true;
					}
				}
			}
			if (!projectHasIssueType) {
				bdsJiraProject.setProjectError("The Jira project is missing the "
						+ ticketGenInfo.getJiraIssueTypeName() + " issue type.");
			}
		}
		return bdsJiraProject;
	}

	public int size() {
		if (mappings == null) {
			return 0;
		}
		return mappings.size();
	}
}
