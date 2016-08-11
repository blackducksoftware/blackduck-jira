package com.blackducksoftware.integration.jira.hub;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersionStatus;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationType;

public abstract class NotificationFilter {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	public static final String PROJECT_LINK = "project";
	private final Set<HubProjectMapping> matchingMappings;
	private final TicketGeneratorInfo ticketGenInfo;

	public NotificationFilter(final Set<HubProjectMapping> mappings, final TicketGeneratorInfo ticketGenInfo) {
		this.matchingMappings = getMatchingMappings();
		this.ticketGenInfo = ticketGenInfo;
	}

	public Set<HubProjectMapping> getMatchingMappings() {
		return matchingMappings;
	}

	public TicketGeneratorInfo getTicketGenInfo() {
		return ticketGenInfo;
	}

	public FilteredNotificationResults handleNotification(final NotificationType notificationType,
			final String projectName, final String projectVersionName,
			final List<ComponentVersionStatus> compVerStatuses, final ReleaseItem notifHubProjectReleaseItem)
					throws UnexpectedHubResponseException, HubNotificationServiceException {
		final FilteredNotificationResults notifResults = new FilteredNotificationResults();

		if (getMatchingMappings() == null || getMatchingMappings().isEmpty()) {
			logger.debug("No configured project mapping matching this notification found; skipping this notification");
			return null;
		}
		for (final HubProjectMapping mapping : getMatchingMappings()) {
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

			final FilteredNotificationResults oneProjectsResults = handleNotificationPerJiraProject(notificationType,
					projectName, projectVersionName, compVerStatuses,
					notifHubProjectReleaseItem, jiraProject);
			if (oneProjectsResults != null) {
				notifResults.addAllResults(oneProjectsResults);
			}
		}
		return notifResults;
	}

	public abstract FilteredNotificationResults handleNotificationPerJiraProject(
			final NotificationType notificationType, final String projectName, final String projectVersionName,
			final List<ComponentVersionStatus> compVerStatuses, final ReleaseItem notifHubProjectReleaseItem,
			JiraProject jiraProject)
					throws UnexpectedHubResponseException, HubNotificationServiceException;


	public JiraProject getJiraProject(final long jiraProjectId) throws HubNotificationServiceException {
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
				bdsJiraProject.setProjectError(
						"The Jira project is missing the " + ticketGenInfo.getJiraIssueTypeName() + " issue type.");
			}
		}
		return bdsJiraProject;
	}

}
