package com.blackducksoftware.integration.jira.hub;

import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.util.ErrorCollection;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.issue.Issue;
import com.blackducksoftware.integration.jira.service.JiraService;
import com.blackducksoftware.integration.jira.service.JiraServiceException;

/**
 * Collects recent notifications from the Hub, and generates JIRA tickets for
 * them.
 *
 * @author sbillings
 *
 */
public class TicketGenerator {
	private final HubNotificationService notificationService;
	private final JiraService jiraService;
	private final IssueService issueService;
	private final User jiraUser;

	private final IntLogger logger;

	public TicketGenerator(final RestConnection restConnection, final HubIntRestService hub,
			final HubItemsService<NotificationItem> hubItemsService, final JiraService jiraService,
			final IssueService issueService, final User jiraUser, final IntLogger logger) {
		notificationService = new HubNotificationService(restConnection, hub, hubItemsService);
		this.jiraService = jiraService;
		this.issueService = issueService;
		this.jiraUser = jiraUser;
		this.logger = logger;
	}

	public int generateTicketsForRecentNotifications(final Set<HubProjectMapping> hubProjectMappings,
			final List<String> linksOfRulesToMonitor,
			final NotificationDateRange notificationDateRange) throws HubNotificationServiceException, JiraServiceException {

		final List<NotificationItem> notifs = notificationService.fetchNotifications(notificationDateRange);
		final JiraNotificationFilter filter = new JiraNotificationFilter(notificationService, jiraService,
				hubProjectMappings, linksOfRulesToMonitor);
		final List<IssueInputParameters> issueParameters = filter.extractJiraReadyNotifications(notifs);
		final CreateValidationResult validationResult = issueService.validateCreate(jiraUser, null);
		final ErrorCollection errors = validationResult.getErrorCollection();
		if(errors.hasAnyErrors()){
			for(final String message : errors.getErrorMessages()){
				logger.error(message);
			}
		}

		final MutableIssue issue = validationResult.getIssue();
		issue.get



		return jiraService.generateTickets(issues);
	}
}
