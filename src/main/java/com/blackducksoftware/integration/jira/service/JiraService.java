package com.blackducksoftware.integration.jira.service;

import java.net.CookieHandler;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.atlassian.jira.project.ProjectManager;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.JiraReadyNotification;
import com.blackducksoftware.integration.jira.hub.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.VulnerabilityNotificationItem;

/**
 * Generates JIRA tickets.
 * 
 * @author sbillings
 * 
 */
public class JiraService {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final ProjectManager jiraProjectManager;

	public JiraService(ProjectManager jiraProjectManager) {
		this.jiraProjectManager = jiraProjectManager;
	}

	public JiraProject getProject(long jiraProjectId) throws JiraServiceException {
		if (jiraProjectManager == null) {
			throw new JiraServiceException("The JIRA projectManager has not been set");
		}
		com.atlassian.jira.project.Project atlassianJiraProject = jiraProjectManager.getProjectObj(jiraProjectId);
		if (atlassianJiraProject == null) {
			throw new JiraServiceException("Error: JIRA Project with ID " + jiraProjectId + " not found");
		}
		String jiraProjectKey = atlassianJiraProject.getKey();
		String jiraProjectName = atlassianJiraProject.getName();
		JiraProject bdsJiraProject = new JiraProject();
		bdsJiraProject.setProjectExists(true);
		bdsJiraProject.setProjectId(jiraProjectId);
		bdsJiraProject.setProjectKey(jiraProjectKey);
		bdsJiraProject.setProjectName(jiraProjectName);
		return bdsJiraProject;
	}

	public int generateTickets(List<JiraReadyNotification> notifs) throws JiraServiceException {

		logger.info("Generating tickets for " + notifs.size() + " JIRA-ready notifications");
		int ticketCount = 0;
		for (JiraReadyNotification notif : notifs) {
			logger.debug("Generating ticket for: " + notif);
			String hubProjectName = "<unknown>";
			String notificationTypeString = "<null>";
			if (notif.getNotificationItem() instanceof VulnerabilityNotificationItem) {
				notificationTypeString = "Vulnerability";
				logger.debug("This is a vulnerability notification; skipping it.");
				continue;
			} else if (notif.getNotificationItem() instanceof RuleViolationNotificationItem) {
				notificationTypeString = "RuleViolation";
				RuleViolationNotificationItem ruleViolationNotificationItem = (RuleViolationNotificationItem) notif
						.getNotificationItem();
				hubProjectName = ruleViolationNotificationItem.getContent().getProjectName();
			} else if (notif.getNotificationItem() instanceof PolicyOverrideNotificationItem) {
				notificationTypeString = "PolicyOverride";
				PolicyOverrideNotificationItem policyOverrideNotificationItem = (PolicyOverrideNotificationItem) notif
						.getNotificationItem();
				hubProjectName = policyOverrideNotificationItem.getContent().getProjectName();
			}

			if (notif.getNotificationItem().getType() != null) {
				notificationTypeString = notif.getNotificationItem().getType().toString();
			}

			makeJiraIssue(notif.getJiraProjectKey(), "Black Duck issue: Type: " + notificationTypeString
					+ " on Hub Project '" + hubProjectName + "'", "Created at: "
					+ notif.getNotificationItem().getCreatedAt().toString());
			ticketCount++;
		}
		logger.info("Generated " + ticketCount + " tickets.");
		return ticketCount;
	}

	private void makeJiraIssue(String projectKey, String issueSummary, String issueDescription)
			throws JiraServiceException {
		String data = generateBody(projectKey, issueSummary, issueDescription);
		try {
			httpPostString("http://localhost:2990/jira/rest/api/2/issue", data);
		} catch (JiraServiceException e) {
			throw new JiraServiceException("Error generating JIRA ticket for JIRA project with key '" + projectKey
					+ "'", e);
		}
	}

	// TODO make this a TON better
	private String generateBody(String projectKey, String issueSummary, String issueDescription) {
		String body = "{ \"fields\": { \"project\": { \"key\": \"" + projectKey + "\" }, \"summary\": \""
				+ issueSummary + "\", \"description\": \"" + issueDescription
				+ "\", \"issuetype\": { \"name\": \"Bug\"        }    } }";
		return body;
	}

	private ClientResource httpPostString(String url, String data) throws JiraServiceException {
		logger.debug("Posting to URL: " + url + "; Data: " + data);
		ClientResource resource = new ClientResource(url);
		resource.setMethod(Method.POST);
		resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");

		final StringRepresentation stringRep = new StringRepresentation(data);
		stringRep.setCharacterSet(CharacterSet.UTF_8);
		stringRep.setMediaType(MediaType.APPLICATION_JSON);
		resource.getRequest().setEntity(stringRep);

		handleRequest(resource);

		logger.debug("Response: " + resource.getResponse());

		int statusCode = resource.getResponse().getStatus().getCode();
		if (statusCode != 201) {
			throw new JiraServiceException("Error on POST to '" + url + "' with data '" + data + "': "
					+ resource.getResponse().toString());
		}
		return resource;
	}

	private void handleRequest(final ClientResource resource) throws JiraServiceException {

		final CookieHandler originalCookieHandler = CookieHandler.getDefault();
		try {
			if (originalCookieHandler != null) {
				CookieHandler.setDefault(null);
			}
			resource.handle();
		} catch (final ResourceException e) {
			throw new JiraServiceException("Problem connecting to the Hub server provided.", e);
		} finally {
			if (originalCookieHandler != null) {

				CookieHandler.setDefault(originalCookieHandler);
			}
		}
		logger.debug("Status Code : " + resource.getResponse().getStatus().getCode());
	}
}
