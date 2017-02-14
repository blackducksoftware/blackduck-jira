/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import com.blackducksoftware.integration.hub.api.bom.BomRequestService;
import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.notification.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.api.vulnerablebomcomponent.VulnerableBomComponentRequestService;
import com.blackducksoftware.integration.hub.dataservice.model.ProjectVersion;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.ListProcessorCache;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
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
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.IssueProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.IssuePropertiesGenerator;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.util.ObjectFactory;

public class NotificationConverterTest {
    private static final long JIRA_ISSUE_ID = 456L;

    private static final String OVERRIDER_LAST_NAME = "lastName";

    private static final String OVERRIDER_FIRST_NAME = "firstName";

    private static final String PROJECT_VERSION_COMPONENTS_URL = "http://int-hub01.dc1.lan:8080/api/projects/projectId/versions/versionId/components";

    private static final String RULE_URL = "http://int-hub01.dc1.lan:8080/api/rules/ruleId";

    private static final String VULNERABLE_COMPONENTS_URL = "http://int-hub01.dc1.lan:8080/api/projects/x/versions/y/vulnerable-components";

    private static final String VULNERABLE_COMPONENTS_LINK_NAME = "vulnerable-components";

    private static final String RULE_NAME = "Test Rule";

    private static final String POLICY_EXPECTED_PROPERTY_KEY = "t=p|jp=123|hpv=-32224582|hc=-973294316|hcv=1816144506|hr=1736320804";

    private static final String POLICY_CLEARED_EXPECTED_COMMENT_IF_EXISTS = "This Policy Violation was cleared in the Hub.";

    private static final String POLICY_CLEARED_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = "This Policy Violation was cleared in the Hub.";

    private static final String POLICY_VIOLATION_EXPECTED_DESCRIPTION = "The Black Duck Hub has detected a policy violation on " +
            "Hub project ['hubProjectName' / 'projectVersionName'|" + PROJECT_VERSION_COMPONENTS_URL
            + "], component 'componentName' / 'componentVersion'. The rule violated is: '"
            +
            RULE_NAME + "'. Rule overridable : true" +
            "\nComponent license(s): ";

    private static final String POLICY_CLEARED_EXPECTED_DESCRIPTION = POLICY_VIOLATION_EXPECTED_DESCRIPTION;

    private static final String POLICY_CLEARED_EXPECTED_SUMMARY = "Black Duck policy violation detected on Hub project 'hubProjectName' / 'projectVersionName', component 'componentName' / 'componentVersion' [Rule: 'Test Rule']";

    private static final String POLICY_CLEARED_EXPECTED_REOPEN_COMMENT = "Automatically re-opened in response to a new Black Duck Hub Policy Violation on this project / component / rule";

    private static final String POLICY_CLEARED_EXPECTED_RESOLVE_COMMENT = "Automatically resolved in response to a Black Duck Hub Policy Violation Cleared event on this project / component / rule";

    private static final String POLICY_OVERRIDE_EXPECTED_COMMENT_IF_EXISTS = "This Policy Violation was overridden in the Hub.";

    private static final String POLICY_OVERRIDE_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = "This Policy Violation was overridden in the Hub.";

    private static final String POLICY_OVERRIDE_EXPECTED_DESCRIPTION = POLICY_VIOLATION_EXPECTED_DESCRIPTION;

    private static final String POLICY_OVERRIDE_EXPECTED_SUMMARY = "Black Duck policy violation detected on Hub project 'hubProjectName' / 'projectVersionName', component 'componentName' / 'componentVersion' [Rule: 'Test Rule']";

    private static final String POLICY_OVERRIDE_EXPECTED_REOPEN_COMMENT = "Automatically re-opened in response to a new Black Duck Hub Policy Violation on this project / component / rule";

    private static final String POLICY_OVERRIDE_EXPECTED_RESOLVE_COMMENT = "Automatically resolved in response to a Black Duck Hub Policy Override on this project / component / rule";

    private static final String POLICY_VIOLATION_EXPECTED_RESOLVE_COMMENT = "Automatically resolved in response to a Black Duck Hub Policy Override on this project / component / rule";

    private static final String POLICY_VIOLATION_EXPECTED_REOPEN_COMMENT = "Automatically re-opened in response to a new Black Duck Hub Policy Violation on this project / component / rule";

    private static final String POLICY_VIOLATION_EXPECTED_SUMMARY = "Black Duck policy violation detected on Hub project 'hubProjectName' / 'projectVersionName', component 'componentName' / 'componentVersion' [Rule: '"
            + RULE_NAME + "']";

    private static final String POLICY_EXPECTED_COMMENT_IF_EXISTS = "This Policy Violation was detected again by the Hub.";

    private static final String POLICY_VIOLATION_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = POLICY_EXPECTED_COMMENT_IF_EXISTS;

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

    private final static String VULN_EXPECTED_COMMENT = "(Black Duck Hub JIRA plugin auto-generated comment)\n" +
            "Vulnerabilities added: http://int-hub01.dc1.lan:8080/api/components/componentId/versions/versionId (NVD)\n" +
            "Vulnerabilities updated: None\n" +
            "Vulnerabilities deleted: None\n";

    private final static String VULN_EXPECTED_COMMENT_IF_EXISTS = VULN_EXPECTED_COMMENT;

    private final static String VULN_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = VULN_EXPECTED_COMMENT;

    private final static String VULN_EXPECTED_DESCRIPTION = "This issue tracks vulnerability status changes on " +
            "Hub project ['hubProjectName' / 'projectVersionName'|" + PROJECT_VERSION_COMPONENTS_URL + "], component 'componentName' / 'componentVersion'. " +
            "For details, see the comments below, or the project's [vulnerabilities|" + VULNERABLE_COMPONENTS_URL + "]" + " in the Hub." +
            "\nComponent license(s): ";

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
                POLICY_VIOLATION_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE,
                POLICY_VIOLATION_EXPECTED_DESCRIPTION,
                POLICY_VIOLATION_EXPECTED_SUMMARY,
                POLICY_ISSUE_TYPE_ID,
                POLICY_VIOLATION_EXPECTED_REOPEN_COMMENT,
                POLICY_VIOLATION_EXPECTED_RESOLVE_COMMENT,
                POLICY_EXPECTED_PROPERTY_KEY);
    }

    @Test
    public void testPolicyOverride() throws ConfigurationException, URISyntaxException, IntegrationException {
        test(NotifType.POLICY_VIOLATION_OVERRIDE, HubEventAction.RESOLVE, null, POLICY_OVERRIDE_EXPECTED_COMMENT_IF_EXISTS,
                POLICY_OVERRIDE_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE,
                POLICY_OVERRIDE_EXPECTED_DESCRIPTION,
                POLICY_OVERRIDE_EXPECTED_SUMMARY,
                POLICY_ISSUE_TYPE_ID,
                POLICY_OVERRIDE_EXPECTED_REOPEN_COMMENT,
                POLICY_OVERRIDE_EXPECTED_RESOLVE_COMMENT,
                POLICY_EXPECTED_PROPERTY_KEY);
    }

    @Test
    public void testPolicyCleared() throws ConfigurationException, URISyntaxException, IntegrationException {
        test(NotifType.POLICY_VIOLATION_CLEARED, HubEventAction.RESOLVE, null, POLICY_CLEARED_EXPECTED_COMMENT_IF_EXISTS,
                POLICY_CLEARED_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE,
                POLICY_CLEARED_EXPECTED_DESCRIPTION,
                POLICY_CLEARED_EXPECTED_SUMMARY,
                POLICY_ISSUE_TYPE_ID,
                POLICY_CLEARED_EXPECTED_REOPEN_COMMENT,
                POLICY_CLEARED_EXPECTED_RESOLVE_COMMENT,
                POLICY_EXPECTED_PROPERTY_KEY);
    }

    private void test(final NotifType notifType, final HubEventAction expectedHubEventAction,
            final String expectedComment,
            final String expectedCommentIfExists,
            final String expectedCommentInLieuOfStateChange,
            final String expectedDescription,
            final String expectedSummary,
            final String issueTypeId,
            final String expectedReOpenComment,
            final String expectedResolveComment,
            final String expectedPropertyKey) throws ConfigurationException, URISyntaxException, IntegrationException {
        final Date now = new Date();

        // Mock the objects that the Converter needs
        final JiraServices jiraServices = Mockito.mock(JiraServices.class);
        final Set<HubProjectMapping> mappings = new HashSet<>();
        final HubProjectMapping mapping = new HubProjectMapping();
        final HubProject hubProject = createHubProject();
        mapping.setHubProject(hubProject);
        final JiraProject jiraProject = createJiraProject();
        mapping.setJiraProject(jiraProject);
        mappings.add(mapping);
        final HubProjectMappings mappingObject = new HubProjectMappings(jiraServices,
                mappings);
        final HubJiraFieldCopyConfigSerializable fieldCopyConfig = createFieldCopyMappings();

        final ConstantsManager constantsManager = Mockito.mock(ConstantsManager.class);
        final List<IssueType> issueTypes = new ArrayList<>();
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

        final ApplicationUser jiraUser = Mockito.mock(ApplicationUser.class);
        Mockito.when(jiraUser.getName()).thenReturn(JIRA_USER_NAME);
        Mockito.when(jiraUser.getKey()).thenReturn(JIRA_USER_KEY);
        final JiraContext jiraContext = new JiraContext(jiraUser);
        final JiraSettingsService jiraSettingsService = null;
        final HubServicesFactory hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final VulnerableBomComponentRequestService vulnBomCompReqSvc = Mockito.mock(VulnerableBomComponentRequestService.class);
        final HubRequestService hubRequestService = Mockito.mock(HubRequestService.class);
        final ProjectVersion projectVersion = createProjectVersion();
        Mockito.when(hubServicesFactory.createHubRequestService()).thenReturn(hubRequestService);
        Mockito.when(hubServicesFactory.createVulnerableBomComponentRequestService()).thenReturn(vulnBomCompReqSvc);
        final MetaService metaService = Mockito.mock(MetaService.class);
        Mockito.when(metaService.getHref(Mockito.any(HubItem.class))).thenReturn(PROJECT_VERSION_COMPONENTS_URL);
        Mockito.when(hubServicesFactory.createMetaService(Mockito.any(IntLogger.class))).thenReturn(metaService);

        final BomRequestService bomRequestService = Mockito.mock(BomRequestService.class);
        final List<VersionBomComponentView> bom = new ArrayList<>();
        final VersionBomComponentView bomComp = Mockito.mock(VersionBomComponentView.class);
        Mockito.when(bomComp.getComponentName()).thenReturn("componentName");
        Mockito.when(bomComp.getComponentVersionName()).thenReturn("componentVersion");
        Mockito.when(bomComp.getComponentVersion()).thenReturn(PROJECT_VERSION_COMPONENTS_URL);
        bom.add(bomComp);
        Mockito.when(bomRequestService.getBom(PROJECT_VERSION_COMPONENTS_URL)).thenReturn(bom);
        Mockito.when(hubServicesFactory.createBomRequestService()).thenReturn(bomRequestService);

        // Construct the notification and the converter
        NotificationToEventConverter conv;
        NotificationContentItem notif;
        final ListProcessorCache cache = new ListProcessorCache();
        switch (notifType) {
        case VULNERABILITY:
            notif = createVulnerabilityNotif(metaService, projectVersion, now);
            conv = new VulnerabilityNotificationConverter(cache,
                    mappingObject,
                    fieldCopyConfig,
                    jiraServices,
                    jiraContext, jiraSettingsService,
                    hubServicesFactory);
            break;
        case POLICY_VIOLATION:
            notif = createPolicyViolationNotif(metaService, projectVersion, now);
            conv = new PolicyViolationNotificationConverter(cache,
                    mappingObject,
                    fieldCopyConfig,
                    jiraServices,
                    jiraContext, jiraSettingsService,
                    hubServicesFactory);
            break;
        case POLICY_VIOLATION_OVERRIDE:
            notif = createPolicyOverrideNotif(metaService, now);
            conv = new PolicyOverrideNotificationConverter(cache,
                    mappingObject,
                    fieldCopyConfig,
                    jiraServices,
                    jiraContext, jiraSettingsService,
                    hubServicesFactory);
            break;
        case POLICY_VIOLATION_CLEARED:
            notif = createPolicyClearedNotif(metaService, now);
            conv = new PolicyViolationClearedNotificationConverter(cache,
                    mappingObject,
                    fieldCopyConfig,
                    jiraServices,
                    jiraContext, jiraSettingsService,
                    hubServicesFactory);
            break;
        default:
            throw new IllegalArgumentException("Unrecognized notification type");
        }

        // Run the converter
        conv.process(notif);
        final List<NotificationEvent> events = new ArrayList<>(cache.getEvents());

        // Verify the generated event
        verifyGeneratedEvents(events, issueTypeId, expectedHubEventAction, expectedComment, expectedCommentIfExists, expectedCommentInLieuOfStateChange,
                expectedDescription, expectedSummary, expectedReOpenComment, expectedResolveComment, expectedPropertyKey);
    }

    private HubProject createHubProject() {
        final HubProject hubProject = new HubProject();
        hubProject.setProjectName(HUB_PROJECT_NAME);
        hubProject.setProjectUrl(HUB_PROJECT_URL);
        return hubProject;
    }

    private JiraProject createJiraProject() {
        final JiraProject jiraProject = new JiraProject();
        jiraProject.setProjectName(JIRA_PROJECT_NAME);
        jiraProject.setProjectId(JIRA_PROJECT_ID);
        jiraProject.setAssigneeUserId(ASSIGNEE_USER_ID);
        return jiraProject;
    }

    private HubJiraFieldCopyConfigSerializable createFieldCopyMappings() {
        final HubJiraFieldCopyConfigSerializable fieldCopyConfig = new HubJiraFieldCopyConfigSerializable();
        final Set<ProjectFieldCopyMapping> projectFieldCopyMappings = new HashSet<>();
        final ProjectFieldCopyMapping projectFieldCopyMapping = new ProjectFieldCopyMapping();
        projectFieldCopyMapping.setHubProjectName(WILDCARD_STRING);
        projectFieldCopyMapping.setJiraProjectName(WILDCARD_STRING);
        projectFieldCopyMapping.setSourceFieldId(SOURCE_FIELD_ID);
        projectFieldCopyMapping.setSourceFieldName(SOURCE_FIELD_NAME);
        projectFieldCopyMapping.setTargetFieldId(TARGET_FIELD_ID);
        projectFieldCopyMapping.setTargetFieldName(TARGET_FIELD_NAME);
        projectFieldCopyMappings.add(projectFieldCopyMapping);
        fieldCopyConfig.setProjectFieldCopyMappings(projectFieldCopyMappings);
        return fieldCopyConfig;
    }

    private void verifyGeneratedEvents(final List<NotificationEvent> events, final String issueTypeId, final HubEventAction expectedHubEventAction,
            final String expectedComment,
            final String expectedCommentIfExists, final String expectedCommentInLieuOfStateChange, final String expectedDescription,
            final String expectedSummary,
            final String expectedReOpenComment, final String expectedResolveComment, final String expectedPropertyKey)
            throws HubIntegrationException, URISyntaxException {
        assertEquals(EXPECTED_EVENT_COUNT, events.size());
        final NotificationEvent event = events.get(0);
        // HubEvent<VulnerabilityContentItem> event = events.get(0);
        final Map<String, Object> dataSet = event.getDataSet();
        assertEquals(expectedHubEventAction, dataSet.get(EventDataSetKeys.ACTION));
        assertEquals(expectedComment, dataSet.get(EventDataSetKeys.JIRA_ISSUE_COMMENT));
        assertEquals(expectedCommentIfExists, dataSet.get(EventDataSetKeys.JIRA_ISSUE_COMMENT_FOR_EXISTING_ISSUE));
        assertEquals(expectedCommentInLieuOfStateChange, dataSet.get(EventDataSetKeys.JIRA_ISSUE_COMMENT_IN_LIEU_OF_STATE_CHANGE));
        assertEquals(ASSIGNEE_USER_ID, dataSet.get(EventDataSetKeys.JIRA_ISSUE_ASSIGNEE_USER_ID));
        assertEquals(expectedDescription, dataSet.get(EventDataSetKeys.JIRA_ISSUE_DESCRIPTION));
        assertEquals(expectedSummary, dataSet.get(EventDataSetKeys.JIRA_ISSUE_SUMMARY));
        assertEquals(issueTypeId, dataSet.get(EventDataSetKeys.JIRA_ISSUE_TYPE_ID));

        assertEquals(Long.valueOf(JIRA_PROJECT_ID), dataSet.get(EventDataSetKeys.JIRA_PROJECT_ID));
        assertEquals(JIRA_PROJECT_NAME, dataSet.get(EventDataSetKeys.JIRA_PROJECT_NAME));
        assertEquals(JIRA_USER_KEY, dataSet.get(EventDataSetKeys.JIRA_USER_KEY));
        assertEquals(JIRA_USER_NAME, dataSet.get(EventDataSetKeys.JIRA_USER_NAME));
        final Set<ProjectFieldCopyMapping> fieldMappings = (Set<ProjectFieldCopyMapping>) dataSet.get(EventDataSetKeys.JIRA_FIELD_COPY_MAPPINGS);
        assertEquals(1, fieldMappings.size());
        final Iterator<ProjectFieldCopyMapping> iter = fieldMappings.iterator();
        final ProjectFieldCopyMapping actualProjectFieldCopyMapping = iter.next();
        assertEquals(WILDCARD_STRING, actualProjectFieldCopyMapping.getHubProjectName());
        assertEquals(WILDCARD_STRING, actualProjectFieldCopyMapping.getJiraProjectName());
        assertEquals(SOURCE_FIELD_ID, actualProjectFieldCopyMapping.getSourceFieldId());
        assertEquals(SOURCE_FIELD_NAME, actualProjectFieldCopyMapping.getSourceFieldName());
        assertEquals(TARGET_FIELD_ID, actualProjectFieldCopyMapping.getTargetFieldId());
        assertEquals(TARGET_FIELD_NAME, actualProjectFieldCopyMapping.getTargetFieldName());

        assertEquals(expectedReOpenComment, dataSet.get(EventDataSetKeys.JIRA_ISSUE_REOPEN_COMMENT));
        assertEquals(expectedResolveComment, dataSet.get(EventDataSetKeys.JIRA_ISSUE_RESOLVE_COMMENT));
        final IssuePropertiesGenerator issuePropertiesGenerator = (IssuePropertiesGenerator) dataSet
                .get(EventDataSetKeys.JIRA_ISSUE_PROPERTIES_GENERATOR);
        // TODO check the property key
        // assertEquals(expectedPropertyKey, dataSet.get(EventDataSetKeys.)
        final IssueProperties issueProperties = issuePropertiesGenerator.createIssueProperties(Long.valueOf(JIRA_ISSUE_ID));
        assertEquals(HUB_PROJECT_NAME, issueProperties.getProjectName());
        assertEquals(PROJECT_VERSION_NAME, issueProperties.getProjectVersion());
        assertEquals(COMPONENT_NAME, issueProperties.getComponentName());
        assertEquals(COMPONENT_VERSION, issueProperties.getComponentVersion());
        assertEquals(Long.valueOf(456L), issueProperties.getJiraIssueId());
        // assertEquals(expectedPropertyKey, issuePropertiesGenerator.createIssueProperties(Long.valueOf(1L)));

    }

    private NotificationContentItem createVulnerabilityNotif(final MetaService metaService, final ProjectVersion projectVersion,
            final Date createdAt) throws URISyntaxException, HubIntegrationException {
        final VulnerabilitySourceQualifiedId vuln = new VulnerabilitySourceQualifiedId(VULN_SOURCE, COMPONENT_VERSION_URL);
        final List<VulnerabilitySourceQualifiedId> addedVulnList = new ArrayList<>();
        final List<VulnerabilitySourceQualifiedId> updatedVulnList = new ArrayList<>();
        final List<VulnerabilitySourceQualifiedId> deletedVulnList = new ArrayList<>();
        addedVulnList.add(vuln);
        final NotificationContentItem notif = new VulnerabilityContentItem(createdAt, projectVersion,
                COMPONENT_NAME,
                createComponentVersionMock(COMPONENT_VERSION),
                COMPONENT_VERSION_URL,
                addedVulnList,
                updatedVulnList,
                deletedVulnList);

        return notif;
    }

    private ComponentVersion createComponentVersionMock(final String componentVersion) {
        ComponentVersion fullComponentVersion;
        fullComponentVersion = Mockito.mock(ComponentVersion.class);
        Mockito.when(fullComponentVersion.getVersionName()).thenReturn(componentVersion);
        return fullComponentVersion;
    }

    private NotificationContentItem createPolicyViolationNotif(final MetaService metaService, final ProjectVersion projectVersion,
            final Date createdAt)
            throws URISyntaxException, IntegrationException {

        final List<PolicyRule> policyRuleList = new ArrayList<>();
        final PolicyRule rule = createRule();
        policyRuleList.add(rule);
        final NotificationContentItem notif = new PolicyViolationContentItem(createdAt, projectVersion,
                COMPONENT_NAME,
                createComponentVersionMock(COMPONENT_VERSION), COMPONENT_URL,
                COMPONENT_VERSION_URL,
                policyRuleList);
        Mockito.when(metaService.getHref(rule)).thenReturn(RULE_URL);

        return notif;
    }

    private ProjectVersion createProjectVersion() {
        final ProjectVersion projectVersion = new ProjectVersion();
        projectVersion.setProjectName(HUB_PROJECT_NAME);
        projectVersion.setProjectVersionName(PROJECT_VERSION_NAME);
        projectVersion.setUrl(PROJECT_VERSION_URL);
        projectVersion.setVulnerableComponentsLink(VULNERABLE_COMPONENTS_URL);
        projectVersion.setComponentsLink(PROJECT_VERSION_COMPONENTS_URL);
        return projectVersion;
    }

    private NotificationContentItem createPolicyClearedNotif(final MetaService metaService, final Date createdAt)
            throws URISyntaxException, IntegrationException {
        final ProjectVersion projectVersion = createProjectVersion();
        final List<PolicyRule> policyRuleList = new ArrayList<>();

        // Create rule
        final PolicyRule rule = createRule();

        policyRuleList.add(rule);
        final NotificationContentItem notif = new PolicyViolationClearedContentItem(createdAt, projectVersion,
                COMPONENT_NAME,
                createComponentVersionMock(COMPONENT_VERSION), COMPONENT_URL,
                COMPONENT_VERSION_URL,
                policyRuleList);

        Mockito.when(metaService.getHref(rule)).thenReturn(RULE_URL);

        return notif;
    }

    private PolicyRule createRule() throws IntegrationException {
        final Map<String, Object> objectProperties = new HashMap<>();
        objectProperties.put("name", RULE_NAME);
        objectProperties.put("description", RULE_NAME);
        objectProperties.put("enabled", Boolean.TRUE);
        objectProperties.put("overridable", Boolean.TRUE);
        final PolicyRule rule = ObjectFactory.INSTANCE.createPopulatedInstance(PolicyRule.class, objectProperties);
        return rule;
    }

    private NotificationContentItem createPolicyOverrideNotif(final MetaService metaService, final Date createdAt)
            throws URISyntaxException, IntegrationException {
        final ProjectVersion projectVersion = createProjectVersion();
        final List<PolicyRule> policyRuleList = new ArrayList<>();

        final PolicyRule rule = createRule();

        policyRuleList.add(rule);
        final NotificationContentItem notif = new PolicyOverrideContentItem(createdAt, projectVersion,
                COMPONENT_NAME,
                createComponentVersionMock(COMPONENT_VERSION), COMPONENT_URL,
                COMPONENT_VERSION_URL,
                policyRuleList, OVERRIDER_FIRST_NAME, OVERRIDER_LAST_NAME);

        Mockito.when(metaService.getHref(rule)).thenReturn(RULE_URL);

        return notif;
    }
}
