package com.blackducksoftware.integration.jira.hub;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.IssueService.CreateValidationResult;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
import com.atlassian.jira.bc.issue.IssueService.UpdateValidationResult;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyQuery;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.entity.property.EntityPropertyService.PropertyResult;
import com.atlassian.jira.entity.property.EntityPropertyService.SetPropertyValidationResult;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.item.HubItemsService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.property.PolicyViolationIssueProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opensymphony.workflow.loader.ActionDescriptor;

/**
 * Collects recent notifications from the Hub, and generates JIRA tickets for
 * them.
 *
 * @author sbillings
 *
 */
public class TicketGenerator {
	public static final String DONE_STATUS = "Done";

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

		final FilteredNotificationResults notificationResults = filter.extractJiraReadyNotifications(notifs);

		int ticketCount = 0;
		for (final FilteredNotificationResult notificationResult : notificationResults.getPolicyViolationResults()) {
			createPolicyViolationIssue(notificationResult);
			ticketCount++;
		}
		for (final FilteredNotificationResult notificationResult : notificationResults
				.getPolicyViolationOverrideResults()) {
			closePolicyViolationIssue(notificationResult);
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
		.setReporterId(notificationResult.getJiraUser().getName())
		.setDescription(issueDescription);

		final Issue oldIssue = findIssue(notificationResult);
		if (oldIssue == null) {
			final Issue issue = createIssue(issueInputParameters);
			if (issue != null) {
				logger.info("Created issue with ID : " + issue.getId());
				logger.debug("Summary : " + issue.getSummary());
				logger.debug("Description : " + issue.getDescription());
				logger.debug("Issue Type : " + issue.getIssueTypeObject().getName());
				logger.debug("For Project : " + issue.getProjectObject().getName());

				final PolicyViolationIssueProperties properties = new PolicyViolationIssueProperties(
						notificationResult.getHubProjectName(), notificationResult.getHubProjectVersion(),
						notificationResult.getHubComponentName(), notificationResult.getHubComponentVersion(),
						notificationResult.getRuleName(), issue.getId());

				addIssueProperty(issue.getId(), notificationResult.getUniquePropertyKey(),
						properties);
			}
		} else {
			logger.info("This issue already exists.");
			logger.debug("Issue ID : " + oldIssue.getId());
			logger.debug("Summary : " + oldIssue.getSummary());
			logger.debug("Description : " + oldIssue.getDescription());
			logger.debug("Issue Type : " + oldIssue.getIssueTypeObject().getName());
			logger.debug("Status Object : " + oldIssue.getStatusObject().getName());
			logger.debug("Status Id : " + oldIssue.getStatusObject().getId());
			logger.debug("Status Description : " + oldIssue.getStatusObject().getDescription());
			logger.debug("For Project : " + oldIssue.getProjectObject().getName());
		}
	}

	private void closePolicyViolationIssue(final FilteredNotificationResult notificationResult) {
		final Issue oldIssue = findIssue(notificationResult);
		if (oldIssue != null) {
			final Issue updatedIssue = closeIssue(oldIssue);
			if (updatedIssue != null) {
				logger.info("Closed the issue based on an override.");
				logger.debug("Issue ID : " + updatedIssue.getId());
				logger.debug("Summary : " + updatedIssue.getSummary());
				logger.debug("Description : " + updatedIssue.getDescription());
				logger.debug("Issue Type : " + updatedIssue.getIssueTypeObject().getName());
				logger.debug("Status Object : " + updatedIssue.getStatusObject().getName());
				logger.debug("Status Id : " + updatedIssue.getStatusObject().getId());
				logger.debug("Status Description : " + updatedIssue.getStatusObject().getDescription());
				logger.debug("For Project : " + updatedIssue.getProjectObject().getName());
			}
		} else {
			logger.info("Could not find an existing issue to close for this override.");
			logger.debug("Hub Project Name : " + notificationResult.getHubProjectName());
			logger.debug("Hub Project Version : " + notificationResult.getHubProjectVersion());
			logger.debug("Hub Component Name : " + notificationResult.getHubComponentName());
			logger.debug("Hub Component Version : " + notificationResult.getHubComponentVersion());
			logger.debug("Hub Rule Name : " + notificationResult.getRuleName());

		}
	}

	private void addIssueProperty(final Long issueId, final String key, final PolicyViolationIssueProperties value) {

		final Gson gson = new GsonBuilder().create();

		final String jsonValue = gson.toJson(value);
		final EntityPropertyService.PropertyInput propertyInput = new EntityPropertyService.PropertyInput(jsonValue,
				key);

		final SetPropertyValidationResult validationResult = ticketGenInfo.getPropertyService()
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
			final PropertyResult result = ticketGenInfo.getPropertyService().setProperty(ticketGenInfo.getJiraUser(),
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
		}
	}

	private Issue findIssue(final FilteredNotificationResult notificationResult) {
		final EntityPropertyQuery<?> query = ticketGenInfo.getJsonEntityPropertyManager().query();
		final EntityPropertyQuery.ExecutableQuery executableQuery = query
				.key(notificationResult.getUniquePropertyKey());
		final List<EntityProperty> props = executableQuery.maxResults(1).find();
		if (props.size() == 0) {
			return null;
		}
		final EntityProperty property = props.get(0);
		final Gson gson = new GsonBuilder().create();

		final PolicyViolationIssueProperties propertyValue = gson.fromJson(property.getValue(),
				PolicyViolationIssueProperties.class);

		final IssueResult result = ticketGenInfo.getIssueService().getIssue(ticketGenInfo.getJiraUser(),
				propertyValue.getJiraIssueId());
		final ErrorCollection errors = result.getErrorCollection();
		if (!result.isValid()) {
			if (errors.hasAnyErrors()) {
				for (final Entry<String, String> error : errors.getErrors().entrySet()) {
					logger.error(error.getKey() + " :: " + error.getValue());
				}
				for (final String error : errors.getErrorMessages()) {
					logger.error(error);
				}
			}
		} else {
			return result.getIssue();
		}
		return null;
	}

	private Issue createIssue(final IssueInputParameters issueInputParameters) {
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
			} else {
				return result.getIssue();
			}
		}
		return null;
	}

	private Issue updateIssue(final Issue issueToUpdate, final IssueInputParameters issueInputParameters) {
		issueInputParameters.setRetainExistingValuesWhenParameterNotProvided(true);
		final UpdateValidationResult validationResult = ticketGenInfo.getIssueService()
				.validateUpdate(ticketGenInfo.getJiraUser(), issueToUpdate.getId(), issueInputParameters);
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
			final IssueResult result = ticketGenInfo.getIssueService().update(ticketGenInfo.getJiraUser(),
					validationResult);
			errors = result.getErrorCollection();
			if (errors.hasAnyErrors()) {
				for (final Entry<String, String> error : errors.getErrors().entrySet()) {
					logger.error(error.getKey() + " :: " + error.getValue());
				}
				for (final String error : errors.getErrorMessages()) {
					logger.error(error);
				}
			} else {
				return result.getIssue();
			}
		}
		return null;
	}

	private Issue closeIssue(final Issue oldIssue) {
		final Status currentStatus = oldIssue.getStatusObject();
		logger.debug("Current status : " + currentStatus.getName());
		final JiraWorkflow workflow = ticketGenInfo.getWorkflowManager().getWorkflow(oldIssue);
		final List<ActionDescriptor> actions = workflow.getLinkedStep(currentStatus).getActions();
		ActionDescriptor doneAction = null;
		for (final ActionDescriptor descriptor : actions) {
			if (descriptor.getName() != null && descriptor.getName().equals(DONE_STATUS)) {
				logger.info("Found Action descriptor : " + descriptor.getUnconditionalResult().getStatus());
				doneAction = descriptor;
				break;
			}
		}
		if (doneAction != null) {
			final IssueInputParameters parameters = ticketGenInfo.getIssueService().newIssueInputParameters();
			parameters.setRetainExistingValuesWhenParameterNotProvided(true);
			final TransitionValidationResult validationResult = ticketGenInfo.getIssueService()
					.validateTransition(ticketGenInfo.getJiraUser(), oldIssue.getId(), doneAction.getId(), parameters);

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
				final IssueResult result = ticketGenInfo.getIssueService().transition(ticketGenInfo.getJiraUser(),
						validationResult);
				errors = result.getErrorCollection();
				if (errors.hasAnyErrors()) {
					for (final Entry<String, String> error : errors.getErrors().entrySet()) {
						logger.error(error.getKey() + " :: " + error.getValue());
					}
					for (final String error : errors.getErrorMessages()) {
						logger.error(error);
					}
				} else {
					return result.getIssue();
				}
			}
		} else {
			logger.error("Could not find the status : " + DONE_STATUS + " to close this issue : " + oldIssue.getId());
		}
		return null;
	}


}
