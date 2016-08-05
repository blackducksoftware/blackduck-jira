package com.blackducksoftware.integration.jira.config;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.blackducksoftware.integration.jira.common.HubProject;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class HubProjectMappingsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		final JiraServices jiraServices = Mockito.mock(JiraServices.class);

		final Collection<IssueType> issueTypes = new ArrayList<>();
		final IssueType issueType = Mockito.mock(IssueType.class);
		Mockito.when(issueType.getName()).thenReturn("Issue");
		Mockito.when(issueType.getId()).thenReturn("issueTypeId");
		issueTypes.add(issueType);

		final JiraContext jiraContext = Mockito.mock(JiraContext.class);
		final ProjectManager jiraProjectManager = Mockito.mock(ProjectManager.class);
		Mockito.when(jiraServices.getJiraProjectManager()).thenReturn(jiraProjectManager);
		// ticketGenInfo.getJiraIssueTypeName()
		Mockito.when(jiraContext.getJiraIssueTypeName()).thenReturn("Issue");

		for (int i = 0; i < 10; i++) {
			final Project mockAtlassianJiraProject = Mockito.mock(Project.class);
			Mockito.when(mockAtlassianJiraProject.getKey()).thenReturn("projectKey" + i);
			Mockito.when(mockAtlassianJiraProject.getName()).thenReturn("projectName" + i);

			Mockito.when(mockAtlassianJiraProject.getIssueTypes()).thenReturn(issueTypes);

			Mockito.when(jiraProjectManager.getProjectObj((long) i)).thenReturn(mockAtlassianJiraProject);
		}

		final Set<HubProjectMapping> underlyingMappings = new HashSet<>();
		for (int i = 0; i < 10; i++) {
			final HubProjectMapping mapping = new HubProjectMapping();
			final HubProject hubProject = new HubProject();
			hubProject.setProjectName("projectName" + i);
			hubProject.setProjectUrl("projectUrl" + i);
			mapping.setHubProject(hubProject);
			final JiraProject jiraProject = new JiraProject();
			jiraProject.setIssueTypeId("issueTypeId");
			jiraProject.setProjectError("projectError");
			jiraProject.setProjectId((long) i);
			jiraProject.setProjectKey("projectKey" + i);
			jiraProject.setProjectName("projectName" + i);
			mapping.setJiraProject(jiraProject);
			underlyingMappings.add(mapping);
		}

		final HubProjectMappings mappings = new HubProjectMappings(jiraServices, jiraContext, underlyingMappings);

		final List<JiraProject> mappedJiraProjects = mappings.getJiraProjects("projectUrl7");
		assertEquals(1, mappedJiraProjects.size());
		final JiraProject mappedJiraProject = mappedJiraProjects.get(0);

		System.out.println(mappedJiraProject);
		assertEquals(Long.valueOf(7L), mappedJiraProject.getProjectId());
	}

}
