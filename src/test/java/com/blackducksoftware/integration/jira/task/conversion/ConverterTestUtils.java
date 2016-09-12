package com.blackducksoftware.integration.jira.task.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mockito.Mockito;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.ProjectManager;
import com.blackducksoftware.integration.hub.exception.NotificationServiceException;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.mocks.ProjectManagerMock;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class ConverterTestUtils {

	public static final String JIRA_PROJECT_PREFIX = "Test JIRA Project";
	public static final long JIRA_PROJECT_ID_BASE = 153L;

	static JiraServices mockJiraServices() throws NotificationServiceException {

		final ConstantsManager constantsManager = Mockito.mock(ConstantsManager.class);
		final Collection<IssueType> issueTypes = new ArrayList<>();
		final IssueType policyIssueType = Mockito.mock(IssueType.class);
		Mockito.when(policyIssueType.getName()).thenReturn(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE);
		Mockito.when(policyIssueType.getId()).thenReturn("policyIssueTypeId");
		issueTypes.add(policyIssueType);
		final IssueType vulnerabilityIssueType = Mockito.mock(IssueType.class);
		Mockito.when(vulnerabilityIssueType.getName()).thenReturn(HubJiraConstants.HUB_VULNERABILITY_ISSUE);
		Mockito.when(vulnerabilityIssueType.getId()).thenReturn("vulnerabilityIssueTypeId");
		issueTypes.add(vulnerabilityIssueType);

		Mockito.when(constantsManager.getAllIssueTypeObjects()).thenReturn(issueTypes);

		final JiraServices jiraServices = Mockito.mock(JiraServices.class);
		Mockito.when(jiraServices.getConstantsManager()).thenReturn(constantsManager);
		final ProjectManager jiraProjectManager = createJiraProjectManager();
		Mockito.when(jiraServices.getJiraProjectManager()).thenReturn(jiraProjectManager);

		final List<JiraProject> mockJiraProjects = ConverterTestUtils.getTestJiraProjectObjects("assigneeUserId");
		for (final JiraProject mockJiraProject : mockJiraProjects) {
			Mockito.when(jiraServices.getJiraProject(mockJiraProject.getProjectId())).thenReturn(mockJiraProject);
		}

		return jiraServices;
	}

	static List<JiraProject> getTestJiraProjectObjects(final String assigneeUserId) {
		final List<JiraProject> jiraProjects = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			final JiraProject jiraProject = new JiraProject();
			jiraProject.setAssigneeUserId(assigneeUserId);
			jiraProject.setProjectId(JIRA_PROJECT_ID_BASE + i);
			jiraProject.setProjectKey("KEY");
			jiraProject.setProjectName(JIRA_PROJECT_PREFIX + i);
			jiraProjects.add(jiraProject);
		}

		return jiraProjects;
	}

	private static ProjectManager createJiraProjectManager() {
		final ProjectManagerMock projectManager = new ProjectManagerMock();
		projectManager.setProjectObjects(ProjectManagerMock.getTestProjectObjectsWithTaskIssueType());
		return projectManager;
	}
}
