package com.blackducksoftware.integration.jira.issue;

import java.util.List;
import java.util.Map.Entry;

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
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.hub.FilteredNotificationResult;
import com.blackducksoftware.integration.jira.hub.FilteredNotificationResultRule;
import com.blackducksoftware.integration.jira.hub.TicketGeneratorInfo;
import com.blackducksoftware.integration.jira.hub.property.IssueProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class JiraIssueHandler {
	public static final String DONE_STATUS = "Done";
	public static final String REOPEN_STATUS = "Reopen";
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final TicketGeneratorInfo ticketGenInfo;

	public JiraIssueHandler(final TicketGeneratorInfo ticketGenInfo) {
		this.ticketGenInfo = ticketGenInfo;
	}

	private void addIssueProperty(final Long issueId, final String key, final IssueProperties value) {

		final Gson gson = new GsonBuilder().create();

		final String jsonValue = gson.toJson(value);
		addIssuePropertyJson(issueId, key, jsonValue);
	}

	private void addIssuePropertyJson(final Long issueId, final String key, final String jsonValue) {
		logger.debug("addIssuePropertyJson(): issueId: " + issueId + "; key: " + key + "; json: " + jsonValue);
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
		logger.debug("findIssue(): notificationResult: " + notificationResult);
		logger.debug("findIssue(): key: " + notificationResult.getUniquePropertyKey());
		final EntityPropertyQuery<?> query = ticketGenInfo.getJsonEntityPropertyManager().query();
		final EntityPropertyQuery.ExecutableQuery executableQuery = query
				.key(notificationResult.getUniquePropertyKey());
		final List<EntityProperty> props = executableQuery.maxResults(1).find();
		if (props.size() == 0) {
			logger.debug("No property found with that key");
			return null;
		}
		final EntityProperty property = props.get(0);
		final IssueProperties propertyValue = notificationResult.createIssuePropertiesFromJson(property.getValue());
		logger.debug("findIssue(): propertyValue (converted from JSON): " + propertyValue);
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

	private Issue createIssue(final FilteredNotificationResult notificationResult) {

		final IssueInputParameters issueInputParameters = ticketGenInfo.getIssueService().newIssueInputParameters();
		issueInputParameters.setProjectId(notificationResult.getJiraProjectId())
		.setIssueTypeId(notificationResult.getJiraIssueTypeId())
		.setSummary(notificationResult.getIssueSummary()).setReporterId(notificationResult.getJiraUserName())
		.setDescription(notificationResult.getIssueDescription());

		final CreateValidationResult validationResult = ticketGenInfo.getIssueService()
				.validateCreate(ticketGenInfo.getJiraUser(), issueInputParameters);
		ErrorCollection errors = null;
		logger.debug("createIssue(): issueInputParameters: " + issueInputParameters);
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

	private Issue transitionIssue(final Issue oldIssue, final String stepName) {
		final Status currentStatus = oldIssue.getStatusObject();
		logger.debug("Current status : " + currentStatus.getName());
		final JiraWorkflow workflow = ticketGenInfo.getWorkflowManager().getWorkflow(oldIssue);

		ActionDescriptor transitionAction = null;
		// https://answers.atlassian.com/questions/6985/how-do-i-change-status-of-issue
		final List<ActionDescriptor> actions = workflow.getLinkedStep(currentStatus).getActions();
		logger.debug("Found this many actions : " + actions.size());
		if (actions.size() == 0) {
			logger.warn("Can not transition this issue : " + oldIssue.getKey() + ", from status : "
					+ currentStatus.getName() + ". There are no steps from this status to any other status.");
		}
		for (final ActionDescriptor descriptor : actions) {
			if (descriptor.getName() != null && descriptor.getName().equals(stepName)) {
				logger.info("Found Action descriptor : " + descriptor.getName());
				transitionAction = descriptor;
				break;
			}
		}
		if (transitionAction == null) {
			logger.warn("Can not transition this issue : " + oldIssue.getKey() + ", from status : "
					+ currentStatus.getName() + ". We could not find the step : " + stepName);
		}
		if (transitionAction != null) {
			final IssueInputParameters parameters = ticketGenInfo.getIssueService().newIssueInputParameters();
			parameters.setRetainExistingValuesWhenParameterNotProvided(true);
			final TransitionValidationResult validationResult = ticketGenInfo.getIssueService().validateTransition(
					ticketGenInfo.getJiraUser(), oldIssue.getId(), transitionAction.getId(), parameters);

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
			logger.error("Could not find the action : " + stepName + " to transition this issue: " + oldIssue.getKey());
		}
		return null;
	}

	public void createOrReOpenIssue(final FilteredNotificationResult notificationResult) {
		logger.debug("Setting logged in User : " + ticketGenInfo.getJiraUser().getDisplayName());
		logger.debug("User active : " + ticketGenInfo.getJiraUser().isActive());
		ticketGenInfo.getAuthContext().setLoggedInUser(ticketGenInfo.getJiraUser());
		final Issue oldIssue = findIssue(notificationResult);
		if (oldIssue == null) {

			final Issue issue = createIssue(notificationResult);
			if (issue != null) {
				logger.info("Created new Issue.");
				printIssueInfo(issue);

				final IssueProperties properties = notificationResult.createIssueProperties(issue);
				logger.debug("Adding properties to created issue: " + properties);
				addIssueProperty(issue.getId(), notificationResult.getUniquePropertyKey(), properties);
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

	public void closeIssue(final FilteredNotificationResult notificationResult) {
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
			if (notificationResult instanceof FilteredNotificationResultRule) {
				final FilteredNotificationResultRule notificationResultRule = (FilteredNotificationResultRule) notificationResult;
				logger.debug("Hub Rule Name : " + notificationResultRule.getRule().getName());
			}
		}
	}

	private void printIssueInfo(final Issue issue) {
		logger.debug("Issue Key : " + issue.getKey());
		logger.debug("Issue ID : " + issue.getId());
		logger.debug("Summary : " + issue.getSummary());
		logger.debug("Description : " + issue.getDescription());
		logger.debug("Issue Type : " + issue.getIssueTypeObject().getName());
		logger.debug("Status : " + issue.getStatusObject().getName());
		logger.debug("For Project : " + issue.getProjectObject().getName());
		logger.debug("For Project Id : " + issue.getProjectObject().getId());
	}

}
