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
import com.blackducksoftware.integration.jira.hub.property.PolicyViolationIssueProperties;
import com.blackducksoftware.integration.jira.hub.property.VulnerabilityIssueProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opensymphony.workflow.loader.ActionDescriptor;

public class JiraIssueHandler {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final TicketGeneratorInfo ticketGenInfo;

	public JiraIssueHandler(final TicketGeneratorInfo ticketGenInfo) {
		this.ticketGenInfo = ticketGenInfo;
	}

	public void addIssuePropertyPolicyViolation(final Long issueId, final String key, final PolicyViolationIssueProperties value) {

		final Gson gson = new GsonBuilder().create();

		final String jsonValue = gson.toJson(value);
		addIssuePropertyJson(issueId, key, jsonValue);
	}

	// TODO : shouldn't need two different methods: rule/vuln
	public void addIssuePropertyVulnerability(final Long issueId, final String key,
			final VulnerabilityIssueProperties value) {

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

	public Issue findIssue(final FilteredNotificationResult notificationResult) {
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
		final Gson gson = new GsonBuilder().create();

		// TODO: Push this subtype-dependent code down into each IssueProperty
		// subtype class
		final IssueProperties propertyValue;
		if (notificationResult instanceof FilteredNotificationResultRule) {
			logger.debug("Converting JSON to VulnerabilityIssueProperties: " + property.getValue());
			propertyValue = gson.fromJson(property.getValue(), PolicyViolationIssueProperties.class);
		} else { // Vulnerability
			logger.debug("Converting JSON to VulnerabilityIssueProperties: " + property.getValue());
			propertyValue = gson.fromJson(property.getValue(), VulnerabilityIssueProperties.class);
		}
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

	public Issue createIssue(final IssueInputParameters issueInputParameters) {
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

	public Issue updateIssue(final Issue issueToUpdate, final IssueInputParameters issueInputParameters) {
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

	public Issue transitionIssue(final Issue oldIssue, final String stepName) {
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

	public void printIssueInfo(final Issue issue) {
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
