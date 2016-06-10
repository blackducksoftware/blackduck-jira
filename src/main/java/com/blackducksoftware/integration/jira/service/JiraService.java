package com.blackducksoftware.integration.jira.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.CookieHandler;
import java.net.URI;
import java.util.List;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.logging.LogLevel;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
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

	public int generateTickets(List<NotificationItem> notifs) throws JiraServiceException {

		System.out.println("Generating tickets for " + notifs.size() + " notifications");
		int ticketCount = 0;
		for (NotificationItem notif : notifs) {
			System.out.println("Generating ticket for: " + notif);
			String hubProjectName = "<unknown>";
			String notificationTypeString = "<null>";
			if (notif instanceof VulnerabilityNotificationItem) {
				notificationTypeString = "Vulnerability";
				System.out.println("This is a vulnerability notification; skipping it.");
				continue;
			} else if (notif instanceof RuleViolationNotificationItem) {
				notificationTypeString = "RuleViolation";
				RuleViolationNotificationItem ruleViolationNotificationItem = (RuleViolationNotificationItem) notif;
				hubProjectName = ruleViolationNotificationItem.getContent().getProjectName();
			} else if (notif instanceof PolicyOverrideNotificationItem) {
				notificationTypeString = "PolicyOverride";
				PolicyOverrideNotificationItem policyOverrideNotificationItem = (PolicyOverrideNotificationItem) notif;
				hubProjectName = policyOverrideNotificationItem.getContent().getProjectName();
			}

			if (notif.getType() != null) {
				notificationTypeString = notif.getType().toString();
			}
			String projectKey = "DEMO";
			makeJiraIssue(projectKey, "Black Duck issue: Type: " + notificationTypeString + " on Hub Project '"
					+ hubProjectName + "'", "Created at: " + notif.getCreatedAt().toString());
			ticketCount++;
		}
		System.out.println("Generated " + ticketCount + " tickets.");
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
		System.out.println("Posting to URL: " + url + "; Data: " + data);
		ClientResource resource = new ClientResource(url);
		resource.setMethod(Method.POST);
		resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");

		final StringRepresentation stringRep = new StringRepresentation(data);
		stringRep.setCharacterSet(CharacterSet.UTF_8);
		stringRep.setMediaType(MediaType.APPLICATION_JSON);
		resource.getRequest().setEntity(stringRep);

		handleRequest(resource);

		System.out.println("Response: " + resource.getResponse());

		int statusCode = resource.getResponse().getStatus().getCode();
		if (statusCode != 201) {
			throw new JiraServiceException("Error on POST to '" + url + "' with data '" + data + "': "
					+ resource.getResponse().toString());
		}
		return resource;
	}

	private String httpGetString(String url) throws JiraServiceException {
		System.out.println("Getting from URL: " + url);
		ClientResource resource = new ClientResource(url);
		resource.setMethod(Method.GET);
		resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");
		handleRequest(resource);
		System.out.println("Response: " + resource.getResponse());
		// TODO test status code
		String responseString;
		try {
			responseString = readResponseAsString(resource.getResponse());
		} catch (IOException e) {
			throw new JiraServiceException(e);
		}
		return responseString;
	}

	private String readResponseAsString(final Response response) throws IOException {
		final StringBuilder sb = new StringBuilder();
		final Reader reader = response.getEntity().getReader();
		final BufferedReader bufReader = new BufferedReader(reader);
		try {
			String line;
			while ((line = bufReader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} finally {
			bufReader.close();
		}
		return sb.toString();
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
		System.out.println("Status Code : " + resource.getResponse().getStatus().getCode());
	}
}
