package com.blackducksoftware.integration.jira.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.CookieHandler;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.Response;
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
import com.blackducksoftware.integration.jira.issue.Issue;

/**
 * Generates JIRA tickets.
 *
 * @author sbillings
 *
 */
public class JiraService {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final ProjectManager jiraProjectManager;
	private final String jiraIssueTypeName;

	public JiraService(final ProjectManager jiraProjectManager, final String jiraIssueTypeName) {
		this.jiraProjectManager = jiraProjectManager;
		this.jiraIssueTypeName = jiraIssueTypeName;
	}

	public JiraProject getProject(final long jiraProjectId) throws JiraServiceException {
		if (jiraProjectManager == null) {
			throw new JiraServiceException("The JIRA projectManager has not been set");
		}
		final com.atlassian.jira.project.Project atlassianJiraProject = jiraProjectManager.getProjectObj(jiraProjectId);
		if (atlassianJiraProject == null) {
			throw new JiraServiceException("Error: JIRA Project with ID " + jiraProjectId + " not found");
		}
		final String jiraProjectKey = atlassianJiraProject.getKey();
		final String jiraProjectName = atlassianJiraProject.getName();
		final JiraProject bdsJiraProject = new JiraProject();
		bdsJiraProject.setProjectExists(true);
		bdsJiraProject.setProjectId(jiraProjectId);
		bdsJiraProject.setProjectKey(jiraProjectKey);
		bdsJiraProject.setProjectName(jiraProjectName);
		return bdsJiraProject;
	}

	public int generateTickets(final List<Issue> issues) throws JiraServiceException {

		logger.info("Generating tickets for " + issues.size() + " JIRA-ready notifications");
		int ticketCount = 0;
		for (final Issue issue : issues) {
			logger.debug("Generating ticket for: " + issue);
			final String hubProjectName = issue.getHubProject().getName();
			final String hubProjectVersionName = issue.getHubProject().getVersion();
			final String notificationTypeString = issue.getIssueTypeDescription();

			final String issueSummary = "Black Duck " + notificationTypeString + " detected on Hub Project '"
					+ hubProjectName
					+ "' / '" + hubProjectVersionName + "', component '" + issue.getHubComponent().getName() + "' / '"
					+ issue.getHubComponent().getVersion() + "' [Rule: '" + issue.getRuleName() + "']";
			final String issueDescription = "The Black Duck Hub has detected a " + notificationTypeString
					+ " on Hub Project '" + hubProjectName + "', component '" + issue.getHubComponent().getName()
					+ "' / '" + issue.getHubComponent().getVersion() + "'. The rule violated is: '"
					+ issue.getRuleName() + "'";

			makeJiraIssue(issue.getJiraProjectKey(), issueSummary, issueDescription);
			ticketCount++;
		}
		logger.info("Generated " + ticketCount + " tickets.");
		return ticketCount;
	}

	private void makeJiraIssue(final String projectKey, final String issueSummary, final String issueDescription)
			throws JiraServiceException {
		final String data = generateBody(projectKey, issueSummary, issueDescription);
		try {
			// TODO Get actual jira URL
			httpPostString("http://localhost:2990/jira/rest/api/2/issue", data);
		} catch (final JiraServiceException e) {
			throw new JiraServiceException("Error generating JIRA ticket for JIRA project with key '" + projectKey
					+ "'", e);
		}
	}


	private String generateBody(final String projectKey, final String issueSummary, final String issueDescription) {

		final StringBuilder sb = new StringBuilder();
		sb.append("{ \"fields\": { \"project\": { \"key\": \"");
		sb.append(projectKey);
		sb.append("\" }, \"summary\": \"");
		sb.append(issueSummary);
		sb.append("\", \"description\": \"");
		sb.append(issueDescription);
		sb.append("\", \"issuetype\": { \"name\": \"");
		sb.append(jiraIssueTypeName);
		sb.append("\"        }    } }");

		return sb.toString();
	}

	private ClientResource httpPostString(final String url, final String data) throws JiraServiceException {
		logger.debug("Posting to URL: " + url + "; Data: " + data);
		final ClientResource resource = new ClientResource(url);
		resource.setMethod(Method.POST);
		// TODO fix this
		resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, "admin", "admin");

		final StringRepresentation stringRep = new StringRepresentation(data);
		stringRep.setCharacterSet(CharacterSet.UTF_8);
		stringRep.setMediaType(MediaType.APPLICATION_JSON);
		resource.getRequest().setEntity(stringRep);

		handleRequest(resource);

		logger.debug("Response: " + resource.getResponse());
		try {
			logger.debug("Response data: " + readResponseAsString(resource.getResponse()));
		} catch (final IOException e) {
			logger.debug("Error reading response data");
		}

		final int statusCode = resource.getResponse().getStatus().getCode();
		if (statusCode != 201) {
			throw new JiraServiceException("Error on POST to '" + url + "' with data '" + data + "': "
					+ resource.getResponse().toString());
		}
		return resource;
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
		logger.debug("Status Code : " + resource.getResponse().getStatus().getCode());
	}
}
