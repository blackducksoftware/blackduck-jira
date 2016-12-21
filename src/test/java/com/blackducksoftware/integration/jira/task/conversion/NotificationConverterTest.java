/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.jira.task.conversion;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectVersion;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionItem;
import com.blackducksoftware.integration.hub.api.vulnerablebomcomponent.VulnerableBomComponentRequestService;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.service.HubRequestService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.jira.common.HubProject;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEvent;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;
import com.blackducksoftware.integration.util.ObjectFactory;

public class NotificationConverterTest {
    private static final String RULE_URL = "http://int-hub01.dc1.lan:8080/api/rules/ruleId";

    private static final String VULNERABLE_COMPONENTS_LINK_NAME = "vulnerable-components";

    private static final String RULE_NAME = "Test Rule";

    private static final String POLICY_EXPECTED_PROPERTY_KEY = "t=p|jp=123|hpv=-32224582|hc=-973294316|hcv=1816144506|hr=1736320804";

    private static final String POLICY_EXPECTED_RESOLVE_COMMENT = "Automatically resolved in response to a Black Duck Hub Policy Override on this project / component / rule";

    private static final String POLICY_EXPECTED_REOPEN_COMMENT = "Automatically re-opened in response to a new Black Duck Hub Policy Violation on this project / component / rule";

    private static final String POLICY_EXPECTED_SUMMARY = "Black Duck policy violation detected on Hub project 'hubProjectName' / 'projectVersionName', component 'componentName' / 'componentVersion' [Rule: '"
            + RULE_NAME + "']";

    private static final String POLICY_EXPECTED_DESCRIPTION = "The Black Duck Hub has detected a policy violation on Hub project 'hubProjectName' / 'projectVersionName', component 'componentName' / 'componentVersion'. The rule violated is: '"
            +
            RULE_NAME + "'. Rule overridable : true";

    private static final String POLICY_EXPECTED_COMMENT_IF_EXISTS = "This Policy Violation was detected again by the Hub.";

    private static final String POLICY_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = POLICY_EXPECTED_COMMENT_IF_EXISTS;

    private static final String VULNERABILITY_ISSUE_TYPE_ID = "Hub Security Vulnerability ID";

    private static final String VULNERABILITY_ISSUE_TYPE_NAME = "Hub Security Vulnerability";

    private static final String POLICY_ISSUE_TYPE_ID = "Hub Policy Violation ID";

    private static final String POLICY_ISSUE_TYPE_NAME = "Hub Policy Violation";

    private static final String TARGET_FIELD_NAME = "targetFieldName";

    private static final String TARGET_FIELD_ID = "targetFieldId";

    private static final String SOURCE_FIELD_NAME = "sourceFieldName";

    private static final String SOURCE_FIELD_ID = "sourceFieldId";

    private static final String WILDCARD_STRING = "*";

    private static final String HUB_PROJECT_URL = "hubProjectUrl";

    private static final String PROJECT_VERSION_NAME = "projectVersionName";

    private static final String PROJECT_VERSION_URL = "http://int-hub01.dc1.lan:8080/api/projects/projectId/versions/versionId";

    private static final String COMPONENT_VERSION_URL = "http://int-hub01.dc1.lan:8080/api/components/componentId/versions/versionId";

    private static final String COMPONENT_URL = "http://int-hub01.dc1.lan:8080/api/components/componentId";

    private static final String COMPONENT_VERSION = "componentVersion";

    private static final String COMPONENT_NAME = "componentName";

    private static final String ASSIGNEE_USER_ID = "assigneeUserId";

    private static final String HUB_PROJECT_NAME = "hubProjectName";

    private static final long JIRA_PROJECT_ID = 123L;

    private static final int EXPECTED_EVENT_COUNT = 1;

    private static final String JIRA_USER_NAME = "jiraUserName";

    private static final String JIRA_USER_KEY = "jiraUserKey";

    private static final String JIRA_PROJECT_NAME = "jiraProjectName";

    private static final String VULN_SOURCE = "NVD";

    private static final String VULN_EXPECTED_PROPERTY_KEY = "t=v|jp=123|hpv=-32224582|hc=|hcv=1816144506";

    private static final String VULN_EXPECTED_RESOLVED_COMMENT = "Automatically resolved; the Black Duck Hub reports no remaining vulnerabilities on this project from this component";

    private static final String VULN_EXPECTED_REOPEN_COMMENT = "Automatically re-opened in response to new Black Duck Hub vulnerabilities on this project from this component";

    private final static String VULN_EXPECTED_COMMENT = "(Black Duck Hub JIRA plugin-generated comment)\n" +
            "Vulnerabilities added: http://int-hub01.dc1.lan:8080/api/components/componentId/versions/versionId (NVD)\n" +
            "Vulnerabilities updated: None\n" +
            "Vulnerabilities deleted: None\n";

    private final static String VULN_EXPECTED_COMMENT_IF_EXISTS = VULN_EXPECTED_COMMENT;

    private final static String VULN_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = VULN_EXPECTED_COMMENT;

    private final static String VULN_EXPECTED_DESCRIPTION = "This issue tracks vulnerability status changes on " +
            "Hub project 'hubProjectName' / 'projectVersionName', component 'componentName' / 'componentVersion'. " +
            "For details, see the comments below, or the project's vulnerabilities view in the Hub:\n" +
            RULE_URL;

    private final static String VULN_EXPECTED_SUMMARY = "Black Duck vulnerability status changes on Hub project " +
            "'hubProjectName' / 'projectVersionName', component 'componentName' / 'componentVersion'";

    private enum NotifType {
        VULNERABILITY, POLICY_VIOLATION, POLICY_VIOLATION_OVERRIDE, POLICY_VIOLATION_CLEARED
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testVulnerability() throws ConfigurationException, URISyntaxException, IntegrationException {
        test(NotifType.VULNERABILITY, HubEventAction.ADD_COMMENT, VULN_EXPECTED_COMMENT, VULN_EXPECTED_COMMENT_IF_EXISTS,
                VULN_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE,
                VULN_EXPECTED_DESCRIPTION,
                VULN_EXPECTED_SUMMARY, VULNERABILITY_ISSUE_TYPE_ID, VULN_EXPECTED_REOPEN_COMMENT,
                VULN_EXPECTED_RESOLVED_COMMENT,
                VULN_EXPECTED_PROPERTY_KEY);
    }

    @Test
    public void testPolicyViolation() throws ConfigurationException, URISyntaxException, IntegrationException {
        test(NotifType.POLICY_VIOLATION, HubEventAction.OPEN, null, POLICY_EXPECTED_COMMENT_IF_EXISTS,
                POLICY_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE,
                POLICY_EXPECTED_DESCRIPTION,
                POLICY_EXPECTED_SUMMARY,
                POLICY_ISSUE_TYPE_ID,
                POLICY_EXPECTED_REOPEN_COMMENT,
                POLICY_EXPECTED_RESOLVE_COMMENT,
                POLICY_EXPECTED_PROPERTY_KEY);
    }

    private void test(NotifType notifType, HubEventAction expectedHubEventAction,
            String expectedComment,
            String expectedCommentIfExists,
            String expectedCommentInLieuOfStateChange,
            String expectedDescription,
            String expectedSummary,
            String issueTypeId,
            String expectedReOpenComment,
            String expectedResolveComment,
            String expectedPropertyKey) throws ConfigurationException, URISyntaxException, IntegrationException {
        Date now = new Date();

        // Mock the objects that the Converter needs
        final JiraServices jiraServices = Mockito.mock(JiraServices.class);
        final Set<HubProjectMapping> mappings = new HashSet<>();
        HubProjectMapping mapping = new HubProjectMapping();
        HubProject hubProject = new HubProject();
        hubProject.setProjectName(HUB_PROJECT_NAME);
        hubProject.setProjectUrl(HUB_PROJECT_URL);
        mapping.setHubProject(hubProject);
        JiraProject jiraProject = new JiraProject();
        jiraProject.setProjectName(JIRA_PROJECT_NAME);
        jiraProject.setProjectId(JIRA_PROJECT_ID);
        jiraProject.setAssigneeUserId(ASSIGNEE_USER_ID);
        mapping.setJiraProject(jiraProject);
        mappings.add(mapping);
        final HubProjectMappings mappingObject = new HubProjectMappings(jiraServices,
                mappings);
        final HubJiraFieldCopyConfigSerializable fieldCopyConfig = new HubJiraFieldCopyConfigSerializable();
        Set<ProjectFieldCopyMapping> projectFieldCopyMappings = new HashSet<>();
        ProjectFieldCopyMapping projectFieldCopyMapping = new ProjectFieldCopyMapping();
        projectFieldCopyMapping.setHubProjectName(WILDCARD_STRING);
        projectFieldCopyMapping.setJiraProjectName(WILDCARD_STRING);
        projectFieldCopyMapping.setSourceFieldId(SOURCE_FIELD_ID);
        projectFieldCopyMapping.setSourceFieldName(SOURCE_FIELD_NAME);
        projectFieldCopyMapping.setTargetFieldId(TARGET_FIELD_ID);
        projectFieldCopyMapping.setTargetFieldName(TARGET_FIELD_NAME);
        projectFieldCopyMappings.add(projectFieldCopyMapping);
        fieldCopyConfig.setProjectFieldCopyMappings(projectFieldCopyMappings);

        ConstantsManager constantsManager = Mockito.mock(ConstantsManager.class);
        List<IssueType> issueTypes = new ArrayList<>();
        IssueType issueType = Mockito.mock(IssueType.class);
        Mockito.when(issueType.getName()).thenReturn(VULNERABILITY_ISSUE_TYPE_NAME);
        Mockito.when(issueType.getId()).thenReturn(VULNERABILITY_ISSUE_TYPE_ID);
        issueTypes.add(issueType);

        issueType = Mockito.mock(IssueType.class);
        Mockito.when(issueType.getName()).thenReturn(POLICY_ISSUE_TYPE_NAME);
        Mockito.when(issueType.getId()).thenReturn(POLICY_ISSUE_TYPE_ID);
        issueTypes.add(issueType);

        Mockito.when(constantsManager.getAllIssueTypeObjects()).thenReturn(issueTypes);
        Mockito.when(jiraServices.getConstantsManager()).thenReturn(constantsManager);
        Mockito.when(jiraServices.getJiraProject(JIRA_PROJECT_ID)).thenReturn(jiraProject);

        ApplicationUser jiraUser = Mockito.mock(ApplicationUser.class);
        Mockito.when(jiraUser.getName()).thenReturn(JIRA_USER_NAME);
        Mockito.when(jiraUser.getKey()).thenReturn(JIRA_USER_KEY);
        final JiraContext jiraContext = new JiraContext(jiraUser);
        final JiraSettingsService jiraSettingsService = null;
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        VulnerableBomComponentRequestService vulnBomCompReqSvc = Mockito.mock(VulnerableBomComponentRequestService.class);
        HubRequestService hubRequestService = Mockito.mock(HubRequestService.class);
        ProjectVersionItem projectVersionItem = Mockito.mock(ProjectVersionItem.class);
        Mockito.when(projectVersionItem.getVersionName()).thenReturn(PROJECT_VERSION_NAME);

        Mockito.when(hubRequestService.getItem(PROJECT_VERSION_URL, ProjectVersionItem.class))
                .thenReturn(projectVersionItem);
        Mockito.when(hubServicesFactory.createHubRequestService()).thenReturn(hubRequestService);
        Mockito.when(hubServicesFactory.createVulnerableBomComponentRequestService()).thenReturn(vulnBomCompReqSvc);
        final MetaService metaService = Mockito.mock(MetaService.class);

        // Construct the converter
        NotificationToEventConverter conv;

        // Construct the notification
        NotificationContentItem notif;
        switch (notifType) {
        case VULNERABILITY:
            notif = createVulnerabilityNotif(metaService, projectVersionItem, now);
            conv = new VulnerabilityNotificationConverter(
                    mappingObject,
                    fieldCopyConfig,
                    jiraServices,
                    jiraContext, jiraSettingsService,
                    hubServicesFactory, metaService);
            break;
        case POLICY_VIOLATION:
            notif = createPolicyViolationNotif(metaService, now);
            conv = new PolicyViolationNotificationConverter(
                    mappingObject,
                    fieldCopyConfig,
                    jiraServices,
                    jiraContext, jiraSettingsService,
                    metaService);
            break;
        default:
            throw new IllegalArgumentException("Unrecognized notification type");
        }

        // Run the converter
        List<HubEvent> events = conv.generateEvents(notif);

        // Verify the generated event
        assertEquals(EXPECTED_EVENT_COUNT, events.size());
        HubEvent<VulnerabilityContentItem> event = events.get(0);
        assertEquals(expectedHubEventAction, event.getAction());
        assertEquals(expectedComment, event.getComment());
        assertEquals(expectedCommentIfExists, event.getCommentIfExists());
        assertEquals(expectedCommentInLieuOfStateChange, event.getCommentInLieuOfStateChange());
        assertEquals(ASSIGNEE_USER_ID, event.getIssueAssigneeId());
        assertEquals(expectedDescription, event.getIssueDescription());
        assertEquals(expectedSummary, event.getIssueSummary());
        assertEquals(issueTypeId, event.getJiraIssueTypeId());

        assertEquals(Long.valueOf(JIRA_PROJECT_ID), event.getJiraProjectId());
        assertEquals(JIRA_PROJECT_NAME, event.getJiraProjectName());
        assertEquals(JIRA_USER_KEY, event.getJiraUserId());
        assertEquals(JIRA_USER_NAME, event.getJiraUserName());
        // VulnerabilityContentItem vulnContentItem = event.getNotif();
        // assertEquals(VULN_SOURCE, vulnContentItem.getAddedVulnList().get(0).getSource());
        assertEquals(1, event.getProjectFieldCopyMappings().size());
        Iterator<ProjectFieldCopyMapping> iter = event.getProjectFieldCopyMappings().iterator();
        ProjectFieldCopyMapping actualProjectFieldCopyMapping = iter.next();
        assertEquals(WILDCARD_STRING, actualProjectFieldCopyMapping.getHubProjectName());
        assertEquals(WILDCARD_STRING, actualProjectFieldCopyMapping.getJiraProjectName());
        assertEquals(SOURCE_FIELD_ID, actualProjectFieldCopyMapping.getSourceFieldId());
        assertEquals(SOURCE_FIELD_NAME, actualProjectFieldCopyMapping.getSourceFieldName());
        assertEquals(TARGET_FIELD_ID, actualProjectFieldCopyMapping.getTargetFieldId());
        assertEquals(TARGET_FIELD_NAME, actualProjectFieldCopyMapping.getTargetFieldName());

        assertEquals(expectedReOpenComment, event.getReopenComment());
        assertEquals(expectedResolveComment, event.getResolveComment());
        assertEquals(expectedPropertyKey, event.getUniquePropertyKey());
    }

    private NotificationContentItem createVulnerabilityNotif(final MetaService metaService, ProjectVersionItem projectReleaseItem,
            final Date createdAt) throws URISyntaxException, HubIntegrationException {
        final ProjectVersion projectVersion = new ProjectVersion();
        projectVersion.setProjectName(HUB_PROJECT_NAME);
        projectVersion.setProjectVersionName(PROJECT_VERSION_NAME);
        projectVersion.setUrl(PROJECT_VERSION_URL);
        VulnerabilitySourceQualifiedId vuln = new VulnerabilitySourceQualifiedId(VULN_SOURCE, COMPONENT_VERSION_URL);
        final List<VulnerabilitySourceQualifiedId> addedVulnList = new ArrayList<>();
        final List<VulnerabilitySourceQualifiedId> updatedVulnList = new ArrayList<>();
        final List<VulnerabilitySourceQualifiedId> deletedVulnList = new ArrayList<>();
        addedVulnList.add(vuln);
        NotificationContentItem notif = new VulnerabilityContentItem(createdAt, projectVersion,
                COMPONENT_NAME,
                COMPONENT_VERSION,
                COMPONENT_VERSION_URL,
                addedVulnList,
                updatedVulnList,
                deletedVulnList);

        Mockito.when(metaService.getLink(projectReleaseItem, VULNERABLE_COMPONENTS_LINK_NAME)).thenReturn(RULE_URL);

        return notif;
    }

    private NotificationContentItem createPolicyViolationNotif(final MetaService metaService, final Date createdAt)
            throws URISyntaxException, IntegrationException {
        final ProjectVersion projectVersion = new ProjectVersion();
        projectVersion.setProjectName(HUB_PROJECT_NAME);
        projectVersion.setProjectVersionName(PROJECT_VERSION_NAME);
        projectVersion.setUrl(PROJECT_VERSION_URL);
        final List<PolicyRule> policyRuleList = new ArrayList<>();

        // Create rule
        Map<String, Object> objectProperties = new HashMap<>();
        objectProperties.put("name", RULE_NAME);
        objectProperties.put("description", RULE_NAME);
        objectProperties.put("enabled", Boolean.TRUE);
        objectProperties.put("overridable", Boolean.TRUE);
        PolicyRule rule = ObjectFactory.INSTANCE.createPopulatedInstance(PolicyRule.class, objectProperties);

        policyRuleList.add(rule);
        NotificationContentItem notif = new PolicyViolationContentItem(createdAt, projectVersion,
                COMPONENT_NAME,
                COMPONENT_VERSION, COMPONENT_URL,
                COMPONENT_VERSION_URL,
                policyRuleList);

        Mockito.when(metaService.getHref(rule)).thenReturn(RULE_URL);

        return notif;
    }

}
