package com.blackducksoftware.integration.jira.hub;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
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
	private final TicketGeneratorInfo ticketGenInfo;


	public TicketGenerator(final RestConnection restConnection, final HubIntRestService hub,
			final HubItemsService<NotificationItem> hubItemsService,
			final TicketGeneratorInfo ticketGenInfo) {
		notificationService = new HubNotificationService(restConnection, hub, hubItemsService);
		this.ticketGenInfo = ticketGenInfo;
	}

	public int generateTicketsForRecentNotifications(final Set<HubProjectMapping> hubProjectMappings,
			final List<String> linksOfRulesToMonitor,
			final NotificationDateRange notificationDateRange) throws HubNotificationServiceException {

		final List<NotificationItem> notifs = notificationService.fetchNotifications(notificationDateRange);
		for (final NotificationItem notification : notifs) {
			logger.debug(notification.toString());
		}
		final JiraNotificationFilter filter = new JiraNotificationFilter(notificationService,
				hubProjectMappings, linksOfRulesToMonitor, ticketGenInfo);

		final List<IssueInputParameters> issueParametersList = filter.extractJiraReadyNotifications(notifs);
		int ticketCount = 0;
		for(final IssueInputParameters issueParameters: issueParametersList){
			logger.debug("Setting logged in User : " + ticketGenInfo.getJiraUser().getDisplayName());
			logger.debug("User active : " + ticketGenInfo.getJiraUser().isActive());

			ticketGenInfo.getAuthContext().setLoggedInUser(ticketGenInfo.getJiraUser());
			final CreateValidationResult validationResult = ticketGenInfo.getIssueService()
					.validateCreate(ticketGenInfo.getJiraUser(), issueParameters);
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
				final IssueResult result = ticketGenInfo.getIssueService().create(ticketGenInfo.getJiraUser(),
						validationResult);
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
					logger.debug("Created ticket  with ID : " + issue.getId());
					logger.debug("Summary : " + issue.getSummary());
					logger.debug("Description : " + issue.getDescription());
					logger.debug("Issue Type : " + issue.getIssueTypeObject().getName());
					logger.debug("For Project : " + issue.getProjectObject().getName());
				}
			}
			ticketCount++;
		}
		return ticketCount;
	}

}
