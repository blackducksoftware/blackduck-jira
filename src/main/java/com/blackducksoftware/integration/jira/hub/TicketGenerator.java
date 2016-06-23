package com.blackducksoftware.integration.jira.hub;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.entity.property.EntityPropertyService.PropertyResult;
import com.atlassian.jira.entity.property.EntityPropertyService.SetPropertyValidationResult;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.util.ErrorCollection;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationType;

/**
 * Collects recent notifications from the Hub, and generates JIRA tickets for
 * them.
 *
 * @author sbillings
 *
 */
public class TicketGenerator {
	public static final String ISSUE_CUSTOM_FIELD_PROJECT_NAME = "Black_Duck_Project";
	public static final String ISSUE_CUSTOM_FIELD_PROJECT_VERSION = "Black_Duck_Project_Version";
	public static final String ISSUE_CUSTOM_FIELD_COMPONENT_NAME = "Black_Duck_Component";
	public static final String ISSUE_CUSTOM_FIELD_COMPONENT_VERSION = "Black_Duck_Component_Version";
	public static final String ISSUE_CUSTOM_FIELD_RULE_NAME = "Black_Duck_Rule";

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

		final List<FilteredNotificationResult> notificationResults = filter.extractJiraReadyNotifications(notifs);
		int ticketCount = 0;
		for (final FilteredNotificationResult notificationResult : notificationResults) {
			if (notificationResult.getNotificationType() == NotificationType.POLICY_VIOLATION) {
				createPolicyViolationIssue(notificationResult);
				ticketCount++;
			} else if (notificationResult.getNotificationType() == NotificationType.POLICY_OVERRIDE) {

			} else if (notificationResult.getNotificationType() == NotificationType.VULNERABILITY) {

			} else {

			}
		}
		return ticketCount;
	}

	private void createPolicyViolationIssue(final FilteredNotificationResult notificationResult){
		logger.debug("Setting logged in User : " + ticketGenInfo.getJiraUser().getDisplayName());
		logger.debug("User active : " + ticketGenInfo.getJiraUser().isActive());

		ticketGenInfo.getAuthContext().setLoggedInUser(ticketGenInfo.getJiraUser());

		final String issueSummary = "Black Duck " +
				notificationResult.getNotificationType().getDisplayName() + " detected on Hub Project '"
				+ notificationResult.getHubProjectName() + "' / '" + notificationResult.getHubProjectVersion()
				+ "', component '" + notificationResult.getHubComponentName() + "' / '"
				+ notificationResult.getHubComponentVersion() + "' [Rule: '" + notificationResult.getRuleName() + "']";

		final String issueDescription = "The Black Duck Hub has detected a "
				+ notificationResult.getNotificationType().getDisplayName() + " on Hub Project '"
				+ notificationResult.getHubProjectName() + "', component '" + notificationResult.getHubComponentName()
				+ "' / '" + notificationResult.getHubComponentVersion() + "'. The rule violated is: '"
				+ notificationResult.getRuleName() + "'";


		final IssueInputParameters issueInputParameters =
				ticketGenInfo.getIssueService()
				.newIssueInputParameters();
		issueInputParameters.setProjectId(notificationResult.getJiraProjectId())
		.setIssueTypeId(notificationResult.getJiraIssueTypeId()).setSummary(issueSummary)
		.setReporterId(notificationResult.getJiraUserName())
		.setDescription(issueDescription);

		final CreateValidationResult validationResult = ticketGenInfo.getIssueService()
				.validateCreate(ticketGenInfo.getJiraUser(), issueInputParameters);
		ErrorCollection errors = null;

		if (!validationResult.isValid()) {
			errors = validationResult.getErrorCollection();
			if (errors.hasAnyErrors()) {
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

				// TODO use 1 property and store all of our data in the value
				// link to test in browser
				// http://bds00863.bds-ad.lc:2990/jira/rest/api/2/issue/TEST-19/properties/
				addIssueProperty(issue.getId(), ISSUE_CUSTOM_FIELD_PROJECT_NAME,
						notificationResult.getHubProjectName());
				addIssueProperty(issue.getId(), ISSUE_CUSTOM_FIELD_PROJECT_VERSION,
						notificationResult.getHubProjectVersion());
				addIssueProperty(issue.getId(), ISSUE_CUSTOM_FIELD_COMPONENT_NAME,
						notificationResult.getHubComponentName());
				addIssueProperty(issue.getId(), ISSUE_CUSTOM_FIELD_COMPONENT_VERSION,
						notificationResult.getHubComponentVersion());
				addIssueProperty(issue.getId(), ISSUE_CUSTOM_FIELD_RULE_NAME, notificationResult.getRuleName());
			}
		}
	}

	private void addIssueProperty(final Long issueId, final String key, final String value) {

		final String jsonKey = key;
		final String jsonValue = "{\"" + key + "\":\"" + value + "\"}";
		logger.info("Property Key : " + jsonKey);
		logger.info("Property Value : " + jsonValue);
		final EntityPropertyService.PropertyInput propertyInput = new EntityPropertyService.PropertyInput(jsonValue,
				key);
		// TODO should not use component accessor to get the service
		final IssuePropertyService propertyService = ComponentAccessor.getComponentOfType(IssuePropertyService.class);

		final SetPropertyValidationResult validationResult = propertyService
				.validateSetProperty(ticketGenInfo.getJiraUser(), issueId, propertyInput);

		ErrorCollection errors = null;
		if (!validationResult.isValid()) {
			errors = validationResult.getErrorCollection();
			if (errors.hasAnyErrors()) {
				for (final Entry<String, String> error : errors.getErrors().entrySet()) {
					logger.error(error.getKey() + " :: " + error.getValue());
				}
				for (final String error : errors.getErrorMessages()) {
					logger.error(error);
				}
			}
		} else {
			final PropertyResult result = propertyService.setProperty(ticketGenInfo.getJiraUser(), validationResult);
			errors = result.getErrorCollection();
			if (errors.hasAnyErrors()) {
				for (final Entry<String, String> error : errors.getErrors().entrySet()) {
					logger.error(error.getKey() + " :: " + error.getValue());
				}
				for (final String error : errors.getErrorMessages()) {
					logger.error(error);
				}
			}
		}
	}

	private void closePolicyViolationIssue(final FilteredNotificationResult notificationResult){

		// TODO find issue
		// JsonEntityPropertyManager

	}

}
