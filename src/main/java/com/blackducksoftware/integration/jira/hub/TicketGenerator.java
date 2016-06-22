package com.blackducksoftware.integration.jira.hub;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;

/**
 * Collects recent notifications from the Hub, and generates JIRA tickets for
 * them.
 *
 * @author sbillings
 *
 */
public class TicketGenerator {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final HubNotificationService notificationService;
	private final ProjectManager jiraProjectManager;
	private final IssueService issueService;
	private final ApplicationUser jiraUser;
	private final String jiraIssueTypeName;
	private final JiraAuthenticationContext authContext;


	public TicketGenerator(final RestConnection restConnection, final HubIntRestService hub,
			final HubItemsService<NotificationItem> hubItemsService,
			final ProjectManager jiraProjectManager,
			final IssueService issueService, final JiraAuthenticationContext authContext,
			final ApplicationUser jiraUser, final String jiraIssueTypeName) {
		notificationService = new HubNotificationService(restConnection, hub, hubItemsService);
		this.jiraProjectManager = jiraProjectManager;
		this.issueService = issueService;
		this.authContext = authContext;
		this.jiraUser = jiraUser;
		this.jiraIssueTypeName = jiraIssueTypeName;
	}

	public int generateTicketsForRecentNotifications(final Set<HubProjectMapping> hubProjectMappings,
			final List<String> linksOfRulesToMonitor,
			final NotificationDateRange notificationDateRange) throws HubNotificationServiceException {

		final List<NotificationItem> notifs = notificationService.fetchNotifications(notificationDateRange);
		final JiraNotificationFilter filter = new JiraNotificationFilter(notificationService,
				hubProjectMappings, linksOfRulesToMonitor, jiraProjectManager, issueService, jiraIssueTypeName,
				jiraUser.getName());

		final List<IssueInputParameters> issueParametersList = filter.extractJiraReadyNotifications(notifs);
		int ticketCount = 0;
		for(final IssueInputParameters issueParameters: issueParametersList){
			logger.trace("Setting logged in User : " + jiraUser.getDisplayName());
			logger.trace("User active : " + jiraUser.isActive());

			authContext.setLoggedInUser(jiraUser);
			final CreateValidationResult validationResult = issueService.validateCreate(jiraUser, issueParameters);
			ErrorCollection errors = null;

			if (!validationResult.isValid()) {
				errors = validationResult.getErrorCollection();
				if(errors.hasAnyErrors()){
					for (final Entry<String, String> error : errors.getErrors().entrySet()) {
						logger.error(error.getKey() + " :: " + error.getValue());
					}
					for (final String error : errors.getErrorMessages()) {
						logger.error(error);
					}
				}
			} else {
				final IssueResult result = issueService.create(jiraUser, validationResult);
				errors = result.getErrorCollection();
				if (errors.hasAnyErrors()) {
					for (final Entry<String, String> error : errors.getErrors().entrySet()) {
						logger.error(error.getKey() + " :: " + error.getValue());
					}
					for (final String error : errors.getErrorMessages()) {
						logger.error(error);
					}
				}
				final MutableIssue issue = result.getIssue();
				if (issue != null) {
					logger.trace("Created ticket  with ID : " + issue.getId());
					logger.trace("Summary : " + issue.getSummary());
					logger.trace("Description : " + issue.getDescription());
					logger.trace("Issue Type : " + issue.getIssueTypeObject().getName());
					logger.trace("For Project : " + issue.getProjectObject().getName());
				}
			}
			ticketCount++;
		}
		return ticketCount;
	}

}
