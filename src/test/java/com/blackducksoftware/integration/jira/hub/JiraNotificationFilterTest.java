// package com.blackducksoftware.integration.jira.hub;
//
// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertTrue;
//
// import java.util.ArrayList;
// import java.util.Date;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Set;
//
// import org.junit.AfterClass;
// import org.junit.BeforeClass;
// import org.junit.Test;
// import org.mockito.Mockito;
//
// import com.atlassian.jira.project.Project;
// import com.atlassian.jira.project.ProjectManager;
// import
// com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
// import com.blackducksoftware.integration.hub.meta.MetaInformation;
// import com.blackducksoftware.integration.hub.meta.MetaLink;
// import com.blackducksoftware.integration.hub.policy.api.PolicyRule;
// import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
// import com.blackducksoftware.integration.jira.config.HubProject;
// import com.blackducksoftware.integration.jira.config.HubProjectMapping;
// import com.blackducksoftware.integration.jira.config.JiraProject;
// import
// com.blackducksoftware.integration.jira.hub.model.component.BomComponentVersionPolicyStatus;
// import
// com.blackducksoftware.integration.jira.hub.model.component.ComponentVersion;
// import
// com.blackducksoftware.integration.jira.hub.model.component.ComponentVersionStatus;
// import
// com.blackducksoftware.integration.jira.hub.model.notification.NotificationItem;
// import
// com.blackducksoftware.integration.jira.hub.model.notification.NotificationType;
// import
// com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationContent;
// import
// com.blackducksoftware.integration.jira.hub.model.notification.RuleViolationNotificationItem;
// import com.blackducksoftware.integration.jira.issue.Issue;
// import com.blackducksoftware.integration.jira.issue.IssueLevel;
// import com.blackducksoftware.integration.jira.service.JiraService;
//
// public class JiraNotificationFilterTest {
//
// private static final String TEST_PROJECT_VERSION_PREFIX = "testVersionName";
// private static final String HUB_COMPONENT_NAME_PREFIX = "test Hub Component";
// private static final String HUB_PROJECT_NAME_PREFIX = "test Hub Project";
// private static final String NOTIF_URL_PREFIX = "http://test.notif.url";
// private static final String JIRA_PROJECT_PREFIX = "Test JIRA Project";
// private static final long JIRA_PROJECT_ID_BASE = 122L;
// private static final String PROJECT_URL_PREFIX = "http://test.project.url";
// private static final String JIRA_ISSUE_TYPE = "Bug";
// private static final String CURRENT_JIRA_PROJECT_NAME = "test JIRA
// Project0a";
// private static final String CURRENT_JIRA_PROJECT_KEY = "TEST0a";
// private static final String BOM_COMPONENT_VERSION_POLICY_STATUS_LINK_PREFIX =
// "bomComponentVersionPolicyStatusLink";
// private static final String COMPONENT_VERSION_LINK_PREFIX =
// "componentVersionLink";
// private static final String VERSION_NAME_PREFIX = "versionName";
// private static final String PROJECTVERSION_URL_PREFIX =
// "http://test.projectversion.url";
// private static final String RULE_URL_PREFIX = "ruleUrl";
// private static final String RULE_NAME_PREFIX = "ruleName";
// private static final String RULE_LINK_NAME = "policy-rule";
//
// @BeforeClass
// public static void setUpBeforeClass() throws Exception {
// }
//
// @AfterClass
// public static void tearDownAfterClass() throws Exception {
// }
//
// @Test
// public void testWithRuleListWithMatches() throws
// HubNotificationServiceException, UnexpectedHubResponseException {
// final HubNotificationService mockHubNotificationService =
// createMockHubNotificationService(true);
// final ProjectManager mockJiraProjectManager = createMockJiraProjectManager();
// final JiraService jiraService = new JiraService(mockJiraProjectManager,
// "http://bds00829:2990/jira",
// JIRA_ISSUE_TYPE);
//
// final Set<HubProjectMapping> mappings = createMappings(true);
//
//
// final List<NotificationItem> notifications = createNotifications();
// final List<String> rulesToMonitor = new ArrayList<>();
// rulesToMonitor.add("ruleUrl0");
// rulesToMonitor.add("ruleUrl1");
//
// final JiraNotificationFilter filter = new
// JiraNotificationFilter(mockHubNotificationService, jiraService,
// mappings, rulesToMonitor);
// final List<Issue> issues =
// filter.extractJiraReadyNotifications(notifications);
//
// System.out.println("Issues:");
// for (final Issue issue : issues) {
// System.out.println(issue);
// assertEquals(IssueLevel.COMPONENT, issue.getLevel());
// assertTrue(issue.getHubProject().getName().startsWith(HUB_PROJECT_NAME_PREFIX));
// assertTrue(issue.getHubProject().getVersion().startsWith(TEST_PROJECT_VERSION_PREFIX));
// assertTrue(issue.getHubComponent().getName().startsWith(HUB_COMPONENT_NAME_PREFIX));
// assertTrue(issue.getHubComponent().getVersion().startsWith(VERSION_NAME_PREFIX));
// assertEquals(CURRENT_JIRA_PROJECT_KEY, issue.getJiraProjectKey());
// assertTrue(issue.getRuleUrl().startsWith(RULE_URL_PREFIX));
// }
// assertEquals(6, issues.size());
// }
//
// @Test
// public void testWithRuleListNoMatch() throws HubNotificationServiceException,
// UnexpectedHubResponseException {
// final HubNotificationService mockHubNotificationService =
// createMockHubNotificationService(false);
// final ProjectManager mockJiraProjectManager = createMockJiraProjectManager();
// final JiraService jiraService = new JiraService(mockJiraProjectManager,
// "http://bds00829:2990/jira",
// JIRA_ISSUE_TYPE);
//
// final Set<HubProjectMapping> mappings = createMappings(true);
//
// final List<NotificationItem> notifications = createNotifications();
//
// final List<String> rulesToMonitor = new ArrayList<>();
// rulesToMonitor.add("rule0");
// rulesToMonitor.add("rule1");
// final JiraNotificationFilter filter = new
// JiraNotificationFilter(mockHubNotificationService, jiraService,
// mappings, rulesToMonitor);
// final List<Issue> issues =
// filter.extractJiraReadyNotifications(notifications);
//
// System.out.println("Issues: " + issues);
//
// assertEquals(0, issues.size());
// }
//
// @Test
// public void testNoMappingMatch() throws HubNotificationServiceException,
// UnexpectedHubResponseException {
// final HubNotificationService mockHubNotificationService =
// createMockHubNotificationService(true);
// final ProjectManager mockJiraProjectManager = createMockJiraProjectManager();
// final JiraService jiraService = new JiraService(mockJiraProjectManager,
// "http://bds00829:2990/jira",
// JIRA_ISSUE_TYPE);
//
// final Set<HubProjectMapping> mappings = createMappings(false);
//
// final List<NotificationItem> notifications = createNotifications();
//
// final JiraNotificationFilter filter = new
// JiraNotificationFilter(mockHubNotificationService, jiraService,
// mappings, null);
// final List<Issue> issues =
// filter.extractJiraReadyNotifications(notifications);
//
// System.out.println("Issues: " + issues);
//
// assertEquals(0, issues.size());
// }
//
// @Test
// public void testWithoutMappings() throws HubNotificationServiceException,
// UnexpectedHubResponseException {
// final HubNotificationService mockHubNotificationService =
// createMockHubNotificationService(true);
// final ProjectManager mockJiraProjectManager = createMockJiraProjectManager();
// final JiraService jiraService = new JiraService(mockJiraProjectManager,
// "http://bds00829:2990/jira",
// JIRA_ISSUE_TYPE);
//
// final Set<HubProjectMapping> mappings = null;
//
// final List<NotificationItem> notifications = createNotifications();
//
// final JiraNotificationFilter filter = new
// JiraNotificationFilter(mockHubNotificationService, jiraService,
// mappings, null);
// final List<Issue> issues =
// filter.extractJiraReadyNotifications(notifications);
//
// System.out.println("Issues:");
// assertEquals(0, issues.size());
// }
//
// private List<NotificationItem> createNotifications() {
// final List<NotificationItem> notifications = new ArrayList<>();
// for (int i = 2; i >= 0; i--) {
// final MetaInformation meta = new MetaInformation(null, NOTIF_URL_PREFIX + i,
// null);
// final RuleViolationNotificationItem notif = new
// RuleViolationNotificationItem(meta);
// notif.setCreatedAt(new Date());
// notif.setType(NotificationType.RULE_VIOLATION);
// final RuleViolationNotificationContent content = new
// RuleViolationNotificationContent();
// content.setProjectName(HUB_PROJECT_NAME_PREFIX + i);
// content.setProjectVersionLink(PROJECTVERSION_URL_PREFIX + i);
// final List<ComponentVersionStatus> componentVersionStatuses = new
// ArrayList<>();
// final ComponentVersionStatus compVerStatus = new ComponentVersionStatus();
// compVerStatus.setComponentName(HUB_COMPONENT_NAME_PREFIX + i);
// compVerStatus.setComponentVersionLink(COMPONENT_VERSION_LINK_PREFIX + i);
// compVerStatus.setBomComponentVersionPolicyStatusLink(BOM_COMPONENT_VERSION_POLICY_STATUS_LINK_PREFIX
// + i);
// componentVersionStatuses.add(compVerStatus);
// content.setComponentVersionStatuses(componentVersionStatuses);
// notif.setContent(content);
// System.out.println("Notif: " + notif);
// notifications.add(notif);
// }
// return notifications;
// }
//
// private Set<HubProjectMapping> createMappings(final boolean match) {
// String suffix;
// if (match) {
// suffix = "";
// } else {
// suffix = "XX";
// }
// final Set<HubProjectMapping> mappings = new HashSet<>();
//
// for (int i = 0; i < 5; i++) {
// final HubProjectMapping mapping = new HubProjectMapping();
// final HubProject hubProject = new HubProject();
// hubProject.setProjectName(HUB_PROJECT_NAME_PREFIX + i);
// hubProject.setProjectUrl(PROJECT_URL_PREFIX + i + suffix);
// mapping.setHubProject(hubProject);
// final JiraProject jiraProject = new JiraProject();
// jiraProject.setProjectId(JIRA_PROJECT_ID_BASE + i);
// jiraProject.setProjectName(JIRA_PROJECT_PREFIX + i);
// mapping.setJiraProject(jiraProject);
//
// System.out.println("Mapping: " + mapping);
// mappings.add(mapping);
// }
// return mappings;
// }
//
// private ProjectManager createMockJiraProjectManager() {
// final ProjectManager mockJiraProjectManager =
// Mockito.mock(ProjectManager.class);
// final Project mockJiraProject = Mockito.mock(Project.class);
// Mockito.when(mockJiraProject.getKey()).thenReturn(CURRENT_JIRA_PROJECT_KEY);
// Mockito.when(mockJiraProject.getName()).thenReturn(CURRENT_JIRA_PROJECT_NAME);
// Mockito.when(mockJiraProject.getId()).thenReturn(123L);
// Mockito.when(mockJiraProjectManager.getProjectObj(Mockito.anyLong())).thenReturn(mockJiraProject);
// return mockJiraProjectManager;
// }
//
// private HubNotificationService createMockHubNotificationService(final boolean
// ruleMatches)
// throws HubNotificationServiceException,
// UnexpectedHubResponseException {
// String suffix;
// if (ruleMatches) {
// suffix = "";
// } else {
// suffix = "XX";
// }
// final HubNotificationService mockHubNotificationService =
// Mockito.mock(HubNotificationService.class);
// for (int i = 0; i < 3; i++) {
// final ReleaseItem releaseItem = getReleaseItem(i);
// Mockito.when(
// mockHubNotificationService
// .getProjectReleaseItemFromProjectReleaseUrl(PROJECTVERSION_URL_PREFIX + i))
// .thenReturn(releaseItem);
// List<MetaLink> links = new ArrayList<>();
// MetaInformation meta = new MetaInformation(null, null, links);
// final ComponentVersion componentVersion = new ComponentVersion(meta);
// componentVersion.setVersionName(VERSION_NAME_PREFIX + i);
// Mockito.when(mockHubNotificationService.getComponentVersion(COMPONENT_VERSION_LINK_PREFIX
// + i))
// .thenReturn(componentVersion);
//
// links = new ArrayList<>();
// for (int j = 0; j < 3; j++) {
// links.add(new MetaLink(RULE_LINK_NAME, RULE_URL_PREFIX + j + suffix));
//
// final PolicyRule rule = new PolicyRule(null, RULE_NAME_PREFIX + j,
// "description", true, true, "createdAt",
// "createdBy", "updatedAt", "updatedBy");
// Mockito.when(mockHubNotificationService.getPolicyRule(RULE_URL_PREFIX +
// j)).thenReturn(rule);
// }
// meta = new MetaInformation(null, null, links);
// final BomComponentVersionPolicyStatus status = new
// BomComponentVersionPolicyStatus(meta);
// Mockito.when(mockHubNotificationService.getPolicyStatus(BOM_COMPONENT_VERSION_POLICY_STATUS_LINK_PREFIX
// + i))
// .thenReturn(status);
// }
// return mockHubNotificationService;
// }
//
//
//
// private ReleaseItem getReleaseItem(final int i) {
// final List<MetaLink> links = new ArrayList<>();
// final MetaLink link = new MetaLink("project", PROJECT_URL_PREFIX + i);
// links.add(link);
// final MetaInformation _meta = new MetaInformation(null, null, links);
// final ReleaseItem releaseItem = new ReleaseItem(TEST_PROJECT_VERSION_PREFIX +
// i, "testPhase",
// "testDistribution",
// "testSource", _meta);
// return releaseItem;
// }
//
//
// }