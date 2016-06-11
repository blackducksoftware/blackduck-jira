package com.blackducksoftware.integration.jira.hub;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.jira.config.HubProject;
import com.blackducksoftware.integration.jira.config.HubProjectMapping;
import com.blackducksoftware.integration.jira.config.JiraProject;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
import com.blackducksoftware.integration.jira.hub.model.notification.NotificationType;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationContent;
import com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
import com.atlassian.jira.project.ProjectManager;

public class JiraNotificationFilterTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		ProjectManager mockJiraProjectManager = Mockito.mock(ProjectManager.class);
		Project mockJiraProject = Mockito.mock(Project.class);
		Mockito.when(mockJiraProject.getKey()).thenReturn("TEST0a");
		Mockito.when(mockJiraProject.getName()).thenReturn("test JIRA Project0a");
		Mockito.when(mockJiraProject.getId()).thenReturn(123L);
		Mockito.when(mockJiraProjectManager.getProjectObj(Mockito.anyLong())).thenReturn(mockJiraProject);

		List<HubProjectMapping> mappings = new ArrayList<>();

		for (int i = 0; i < 5; i++) {
			HubProjectMapping mapping = new HubProjectMapping();
			HubProject hubProject = new HubProject();
			hubProject.setProjectExists(true);
			hubProject.setProjectName("Test Hub Project" + i);
			hubProject.setProjectUrl("http://test.project.url" + i);
			mapping.setHubProject(hubProject);
			JiraProject jiraProject = new JiraProject();
			jiraProject.setProjectExists(true);
			jiraProject.setProjectId(122L + i);
			jiraProject.setProjectName("Test JIRA Project" + i);
			mapping.setJiraProject(jiraProject);

			System.out.println("Mapping: " + mapping);
			mappings.add(mapping);
		}

		JiraNotificationFilter filter = new JiraNotificationFilter(mockJiraProjectManager, mappings);
		List<NotificationItem> notifications = new ArrayList<>();
		for (int i = 2; i >= 0; i--) {
			RuleViolationNotificationItem notif = new RuleViolationNotificationItem();
			notif.setContentType("test content type");
			notif.setCreatedAt(new Date());
			MetaInformation meta = new MetaInformation(null, "http://test.project.url" + i, null);
			notif.setMeta(meta);
			notif.setType(NotificationType.RULE_VIOLATION);
			RuleViolationNotificationContent content = new RuleViolationNotificationContent();
			content.setProjectName("test Hub Project" + i);
			notif.setContent(content);
			System.out.println("Notif: " + notif);
			notifications.add(notif);
		}
		List<JiraReadyNotification> jiraReadyNotifs = filter.extractJiraReadyNotifications(notifications);

		System.out.println("JIRA-ready notifications:");
		for (JiraReadyNotification notif : jiraReadyNotifs) {
			System.out.println(notif);
		}
		assertEquals(3, jiraReadyNotifs.size());
	}

}