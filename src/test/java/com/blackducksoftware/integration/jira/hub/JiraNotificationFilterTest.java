package com.blackducksoftware.integration.jira.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import com.blackducksoftware.integration.jira.service.JiraService;

public class JiraNotificationFilterTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testWithoutRuleList() throws HubNotificationServiceException {
		final HubNotificationService mockHubNotificationService = Mockito.mock(HubNotificationService.class);
		for (int i = 0; i < 3; i++) {
			Mockito.when(
					mockHubNotificationService.getProjectUrlFromProjectReleaseUrl("http://test.projectversion.url" + i))
			.thenReturn("http://test.project.url" + i);
		}

		final List<String> violatedRules = new ArrayList<>();
		violatedRules.add("rule1");
		violatedRules.add("rule2");
		violatedRules.add("rule3");
		Mockito.when(
				mockHubNotificationService.getLinksOfRulesViolated(Mockito.any(RuleViolationNotificationItem.class)))
		.thenReturn(violatedRules);

		final ProjectManager mockJiraProjectManager = Mockito.mock(ProjectManager.class);
		final Project mockJiraProject = Mockito.mock(Project.class);
		Mockito.when(mockJiraProject.getKey()).thenReturn("TEST0a");
		Mockito.when(mockJiraProject.getName()).thenReturn("test JIRA Project0a");
		Mockito.when(mockJiraProject.getId()).thenReturn(123L);
		Mockito.when(mockJiraProjectManager.getProjectObj(Mockito.anyLong())).thenReturn(mockJiraProject);

		final JiraService jiraService = new JiraService(mockJiraProjectManager, "Bug");

		final Set<HubProjectMapping> mappings = new HashSet<>();

		for (int i = 0; i < 5; i++) {
			final HubProjectMapping mapping = new HubProjectMapping();
			final HubProject hubProject = new HubProject();
			hubProject.setProjectExists(true);
			hubProject.setProjectName("Test Hub Project" + i);
			hubProject.setProjectUrl("http://test.project.url" + i);
			mapping.setHubProject(hubProject);
			final JiraProject jiraProject = new JiraProject();
			jiraProject.setProjectExists(true);
			jiraProject.setProjectId(122L + i);
			jiraProject.setProjectName("Test JIRA Project" + i);
			mapping.setJiraProject(jiraProject);

			System.out.println("Mapping: " + mapping);
			mappings.add(mapping);
		}

		final JiraNotificationFilter filter = new JiraNotificationFilter(mockHubNotificationService, jiraService,
				mappings, null);
		final List<NotificationItem> notifications = new ArrayList<>();
		for (int i = 2; i >= 0; i--) {
			final MetaInformation meta = new MetaInformation(null, "http://test.notif.url" + i, null);
			final RuleViolationNotificationItem notif = new RuleViolationNotificationItem(meta);
			notif.setContentType("test content type");
			notif.setCreatedAt(new Date());
			notif.setType(NotificationType.RULE_VIOLATION);
			final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
			content.setProjectName("test Hub Project" + i);
			content.setProjectVersionLink("http://test.projectversion.url" + i);
			notif.setContent(content);
			System.out.println("Notif: " + notif);
			notifications.add(notif);
		}
		final List<JiraReadyNotification> jiraReadyNotifs = filter.extractJiraReadyNotifications(notifications);

		System.out.println("JIRA-ready notifications:");
		for (final JiraReadyNotification notif : jiraReadyNotifs) {
			System.out.println(notif);
			final RuleViolationNotificationItem ruleViolationNotif = (RuleViolationNotificationItem) notif
					.getNotificationItem();
			assertTrue(ruleViolationNotif.getContent().getProjectVersionLink()
					.startsWith("http://test.projectversion.url"));

		}
		assertEquals(3, jiraReadyNotifs.size());
	}

	@Test
	public void testWithRuleListNoMatch() throws HubNotificationServiceException {
		final HubNotificationService mockHubNotificationService = Mockito.mock(HubNotificationService.class);
		for (int i = 0; i < 3; i++) {
			Mockito.when(
					mockHubNotificationService.getProjectUrlFromProjectReleaseUrl("http://test.projectversion.url" + i))
			.thenReturn("http://test.project.url" + i);
		}

		final List<String> violatedRules = new ArrayList<>();
		violatedRules.add("rule1");
		violatedRules.add("rule2");
		violatedRules.add("rule3");
		Mockito.when(
				mockHubNotificationService.getLinksOfRulesViolated(Mockito.any(RuleViolationNotificationItem.class)))
		.thenReturn(violatedRules);

		final ProjectManager mockJiraProjectManager = Mockito.mock(ProjectManager.class);
		final Project mockJiraProject = Mockito.mock(Project.class);
		Mockito.when(mockJiraProject.getKey()).thenReturn("TEST0a");
		Mockito.when(mockJiraProject.getName()).thenReturn("test JIRA Project0a");
		Mockito.when(mockJiraProject.getId()).thenReturn(123L);
		Mockito.when(mockJiraProjectManager.getProjectObj(Mockito.anyLong())).thenReturn(mockJiraProject);

		final JiraService jiraService = new JiraService(mockJiraProjectManager, "Bug");

		final Set<HubProjectMapping> mappings = new HashSet<>();

		for (int i = 0; i < 5; i++) {
			final HubProjectMapping mapping = new HubProjectMapping();
			final HubProject hubProject = new HubProject();
			hubProject.setProjectExists(true);
			hubProject.setProjectName("Test Hub Project" + i);
			hubProject.setProjectUrl("http://test.project.url" + i);
			mapping.setHubProject(hubProject);
			final JiraProject jiraProject = new JiraProject();
			jiraProject.setProjectExists(true);
			jiraProject.setProjectId(122L + i);
			jiraProject.setProjectName("Test JIRA Project" + i);
			mapping.setJiraProject(jiraProject);

			System.out.println("Mapping: " + mapping);
			mappings.add(mapping);
		}

		final List<String> linksOfRulesToMonitor = new ArrayList<>();
		linksOfRulesToMonitor.add("testRule");

		final JiraNotificationFilter filter = new JiraNotificationFilter(mockHubNotificationService, jiraService,
				mappings, linksOfRulesToMonitor);
		final List<NotificationItem> notifications = new ArrayList<>();
		for (int i = 2; i >= 0; i--) {
			final MetaInformation meta = new MetaInformation(null, "http://test.notif.url" + i, null);
			final RuleViolationNotificationItem notif = new RuleViolationNotificationItem(meta);
			notif.setContentType("test content type");
			notif.setCreatedAt(new Date());
			notif.setType(NotificationType.RULE_VIOLATION);
			final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
			content.setProjectName("test Hub Project" + i);
			content.setProjectVersionLink("http://test.projectversion.url" + i);
			notif.setContent(content);
			System.out.println("Notif: " + notif);
			notifications.add(notif);
		}
		final List<JiraReadyNotification> jiraReadyNotifs = filter.extractJiraReadyNotifications(notifications);

		System.out.println("JIRA-ready notifications:");
		for (final JiraReadyNotification notif : jiraReadyNotifs) {
			System.out.println(notif);
			final RuleViolationNotificationItem ruleViolationNotif = (RuleViolationNotificationItem) notif
					.getNotificationItem();
			assertTrue(ruleViolationNotif.getContent().getProjectVersionLink()
					.startsWith("http://test.projectversion.url"));

		}
		assertEquals(0, jiraReadyNotifs.size());
	}

	@Test
	public void testWithRuleListWithMatch() throws HubNotificationServiceException {
		final HubNotificationService mockHubNotificationService = Mockito.mock(HubNotificationService.class);
		for (int i = 0; i < 3; i++) {
			Mockito.when(
					mockHubNotificationService.getProjectUrlFromProjectReleaseUrl("http://test.projectversion.url" + i))
			.thenReturn("http://test.project.url" + i);
		}

		final List<String> violatedRules = new ArrayList<>();
		violatedRules.add("rule1");
		violatedRules.add("testRule");
		violatedRules.add("rule3");
		Mockito.when(
				mockHubNotificationService.getLinksOfRulesViolated(Mockito.any(RuleViolationNotificationItem.class)))
		.thenReturn(violatedRules);

		final ProjectManager mockJiraProjectManager = Mockito.mock(ProjectManager.class);
		final Project mockJiraProject = Mockito.mock(Project.class);
		Mockito.when(mockJiraProject.getKey()).thenReturn("TEST0a");
		Mockito.when(mockJiraProject.getName()).thenReturn("test JIRA Project0a");
		Mockito.when(mockJiraProject.getId()).thenReturn(123L);
		Mockito.when(mockJiraProjectManager.getProjectObj(Mockito.anyLong())).thenReturn(mockJiraProject);

		final JiraService jiraService = new JiraService(mockJiraProjectManager, "Bug");

		final Set<HubProjectMapping> mappings = new HashSet<>();

		for (int i = 0; i < 5; i++) {
			final HubProjectMapping mapping = new HubProjectMapping();
			final HubProject hubProject = new HubProject();
			hubProject.setProjectExists(true);
			hubProject.setProjectName("Test Hub Project" + i);
			hubProject.setProjectUrl("http://test.project.url" + i);
			mapping.setHubProject(hubProject);
			final JiraProject jiraProject = new JiraProject();
			jiraProject.setProjectExists(true);
			jiraProject.setProjectId(122L + i);
			jiraProject.setProjectName("Test JIRA Project" + i);
			mapping.setJiraProject(jiraProject);

			System.out.println("Mapping: " + mapping);
			mappings.add(mapping);
		}

		final List<String> linksOfRulesToMonitor = new ArrayList<>();
		linksOfRulesToMonitor.add("testRule");

		final JiraNotificationFilter filter = new JiraNotificationFilter(mockHubNotificationService, jiraService,
				mappings, linksOfRulesToMonitor);
		final List<NotificationItem> notifications = new ArrayList<>();
		for (int i = 2; i >= 0; i--) {
			final MetaInformation meta = new MetaInformation(null, "http://test.notif.url" + i, null);
			final RuleViolationNotificationItem notif = new RuleViolationNotificationItem(meta);
			notif.setContentType("test content type");
			notif.setCreatedAt(new Date());
			notif.setType(NotificationType.RULE_VIOLATION);
			final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
			content.setProjectName("test Hub Project" + i);
			content.setProjectVersionLink("http://test.projectversion.url" + i);
			notif.setContent(content);
			System.out.println("Notif: " + notif);
			notifications.add(notif);
		}
		final List<JiraReadyNotification> jiraReadyNotifs = filter.extractJiraReadyNotifications(notifications);

		System.out.println("JIRA-ready notifications:");
		for (final JiraReadyNotification notif : jiraReadyNotifs) {
			System.out.println(notif);
			final RuleViolationNotificationItem ruleViolationNotif = (RuleViolationNotificationItem) notif
					.getNotificationItem();
			assertTrue(ruleViolationNotif.getContent().getProjectVersionLink()
					.startsWith("http://test.projectversion.url"));

		}
		assertEquals(3, jiraReadyNotifs.size());
	}
}