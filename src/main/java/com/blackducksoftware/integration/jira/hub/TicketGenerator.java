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
	public static final String REOPEN_STATUS = "Reopen";

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

		final StringBuilder issueSummary = new StringBuilder();
		issueSummary.append("Black Duck ");
		issueSummary.append(notificationResult.getNotificationType().getDisplayName());
		issueSummary.append(" detected on Hub Project '");
		issueSummary.append(notificationResult.getHubProjectName());
		issueSummary.append("' / '");
		issueSummary.append(notificationResult.getHubProjectVersion());
		issueSummary.append("', component '");
		issueSummary.append(notificationResult.getHubComponentName());
		issueSummary.append("' / '");
		issueSummary.append(notificationResult.getHubComponentVersion());
		issueSummary.append("' [Rule: '");
		issueSummary.append(notificationResult.getRule().getName());
		issueSummary.append("']");

		final StringBuilder issueDescription = new StringBuilder();
		issueDescription.append("The Black Duck Hub has detected a ");
		issueDescription.append(notificationResult.getNotificationType().getDisplayName());
		issueDescription.append(" on Hub Project '");
		issueDescription.append(notificationResult.getHubProjectName());
		issueDescription.append("', component '");
		issueDescription.append(notificationResult.getHubComponentName());
		issueDescription.append("' / '");
		issueDescription.append(notificationResult.getHubComponentVersion());
		issueDescription.append("'. The rule violated is: '");
		issueDescription.append(notificationResult.getRule().getName());
		issueDescription.append("' Rule overridable : ");
		issueDescription.append(notificationResult.getRule().getOverridable());

		final IssueInputParameters issueInputParameters =
				ticketGenInfo.getIssueService()
				.newIssueInputParameters();
		issueInputParameters.setProjectId(notificationResult.getJiraProjectId())
				.setIssueTypeId(notificationResult.getJiraIssueTypeId()).setSummary(issueSummary.toString())
		.setReporterId(notificationResult.getJiraUser().getName())
				.setDescription(issueDescription.toString());

		final Issue oldIssue = findIssue(notificationResult);
		if (oldIssue == null) {
			final Issue issue = createIssue(issueInputParameters);
			if (issue != null) {
				logger.info("Created new Issue.");
				printIssueInfo(issue);

				final PolicyViolationIssueProperties properties = new PolicyViolationIssueProperties(
						notificationResult.getHubProjectName(), notificationResult.getHubProjectVersion(),
						notificationResult.getHubComponentName(), notificationResult.getHubComponentVersion(),
						notificationResult.getRule().getName(), issue.getId());

				addIssueProperty(issue.getId(), notificationResult.getUniquePropertyKey(),
						properties);
			}
		} else {
			if (oldIssue.getStatusObject().getName().equals(DONE_STATUS)) {
				transitionIssue(oldIssue, REOPEN_STATUS);
				logger.info("Re-opened the already exisiting issue.");
				printIssueInfo(oldIssue);
			} else {
				logger.info("This issue already exists.");
				printIssueInfo(oldIssue);
			}
		}
	}

	private void closePolicyViolationIssue(final FilteredNotificationResult notificationResult) {
		final Issue oldIssue = findIssue(notificationResult);
		if (oldIssue != null) {
			final Issue updatedIssue = transitionIssue(oldIssue, DONE_STATUS);
			if (updatedIssue != null) {
				logger.info("Closed the issue based on an override.");
				printIssueInfo(updatedIssue);
			}
		} else {
			logger.info("Could not find an existing issue to close for this override.");
			logger.debug("Hub Project Name : " + notificationResult.getHubProjectName());
			logger.debug("Hub Project Version : " + notificationResult.getHubProjectVersion());
			logger.debug("Hub Component Name : " + notificationResult.getHubComponentName());
			logger.debug("Hub Component Version : " + notificationResult.getHubComponentVersion());
			logger.debug("Hub Rule Name : " + notificationResult.getRule().getName());

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

	private Issue transitionIssue(final Issue oldIssue, final String transitionName) {
		final Status currentStatus = oldIssue.getStatusObject();
		logger.debug("Current status : " + currentStatus.getName());
		final JiraWorkflow workflow = ticketGenInfo.getWorkflowManager().getWorkflow(oldIssue);
		final List<ActionDescriptor> actions = workflow.getLinkedStep(currentStatus).getActions();
		ActionDescriptor transitionAction = null;
		for (final ActionDescriptor descriptor : actions) {
			if (descriptor.getName() != null && descriptor.getName().equals(transitionName)) {
				logger.info("Found Action descriptor : " + descriptor.getName());
				transitionAction = descriptor;
				break;
			}
		}
		if (transitionAction != null) {
			final IssueInputParameters parameters = ticketGenInfo.getIssueService().newIssueInputParameters();
			parameters.setRetainExistingValuesWhenParameterNotProvided(true);
			final TransitionValidationResult validationResult = ticketGenInfo.getIssueService()
					.validateTransition(ticketGenInfo.getJiraUser(), oldIssue.getId(), transitionAction.getId(), parameters);

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
			logger.error("Could not find the status : " + transitionName + " to transition this issue: "
					+ oldIssue.getKey());
		}
		return null;
	}

	private void printIssueInfo(final Issue issue) {
		logger.debug("Issue Key : " + issue.getKey());
		logger.debug("Issue ID : " + issue.getId());
		logger.debug("Summary : " + issue.getSummary());
		logger.debug("Description : " + issue.getDescription());
		logger.debug("Issue Type : " + issue.getIssueTypeObject().getName());
		logger.debug("Status : " + issue.getStatusObject().getName());
		logger.debug("For Project : " + issue.getProjectObject().getName());
	}

}
