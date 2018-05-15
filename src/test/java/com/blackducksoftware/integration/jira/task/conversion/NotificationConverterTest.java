/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.component.AffectedProjectVersion;
import com.blackducksoftware.integration.hub.api.generated.component.PolicyRuleExpressionSetView;
import com.blackducksoftware.integration.hub.api.generated.enumeration.MatchedFileUsagesType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationStateRequestStateType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.OriginSourceType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ProjectVersionPhaseType;
import com.blackducksoftware.integration.hub.api.generated.response.VersionRiskProfileView;
import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationUserView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleViewV2;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.UserView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.content.ComponentVersionStatus;
import com.blackducksoftware.integration.hub.notification.content.PolicyInfo;
import com.blackducksoftware.integration.hub.notification.content.PolicyOverrideNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationClearedNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.RuleViolationNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.service.ComponentService;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProject;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.hub.ProjectResponse;
import com.blackducksoftware.integration.jira.hub.VersionRiskProfileResponse;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.IssueProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.IssuePropertiesGenerator;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataFormatHelper;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class NotificationConverterTest {
    private static final long JIRA_ISSUE_ID = 456L;
    private static final String OVERRIDER_LAST_NAME = "lastName";
    private static final String OVERRIDER_FIRST_NAME = "firstName";
    private static final String PROJECT_VERSION_COMPONENTS_URL = "http://localhost:8080/api/components/componentId";
    private static final String RISK_PROFILE_LINK = "project_version_risk_profile_link";
    private static final String PROJECT_RESPONSE_LINK = "project_response_link";
    private static final String PROJECT_OWNER_LINK = "project_version_project_owner_link";
    private static final String RULE_URL = "http://localhost:8080/api/rules/ruleId";
    private static final String VULNERABLE_COMPONENTS_URL = "http://localhost:8080/api/projects/x/versions/y/vulnerable-components";
    private static final String RULE_NAME = "Test Rule";
    private static final String POLICY_EXPECTED_PROPERTY_KEY = "t=p|jp=123|hpv=-32224582|hc=|hcv=1816144506|hr=1736320804"; // TODO remove: "t=p|jp=123|hpv=-32224582|hc=-973294316|hcv=1816144506|hr=1736320804";
    private static final String POLICY_CLEARED_EXPECTED_COMMENT_IF_EXISTS = "This Policy Violation was cleared in the Hub.";
    private static final String POLICY_CLEARED_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = "This Policy Violation was cleared in the Hub.";
    private static final String POLICY_VIOLATION_EXPECTED_DESCRIPTION = "The Black Duck Hub has detected a policy violation on " + "Hub project ['hubProjectName' / 'projectVersionName'|" + PROJECT_VERSION_COMPONENTS_URL
            + "], component 'componentName' / 'componentVersion'. The rule violated is: '" + RULE_NAME + "'. Rule overridable: true" + "\nComponent license(s): ";
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
    private static final String POLICY_VIOLATION_EXPECTED_SUMMARY = "Black Duck policy violation detected on Hub project 'hubProjectName' / 'projectVersionName', component 'componentName' / 'componentVersion' [Rule: '" + RULE_NAME + "']";
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
    private static final String PROJECT_VERSION_URL = "http://localhost:8080/api/projects/projectId/versions/versionId";
    private static final String COMPONENT_VERSION_URL = "http://localhost:8080/api/components/componentId/versions/versionId";
    private static final String COMPONENT_URL = "http://localhost:8080/api/components/componentId";
    private static final String COMPONENT_VERSION_NAME = "componentVersion";
    private static final String COMPONENT_NAME = "componentName";
    private static final String ASSIGNEE_USER_ID = "assigneeUserId";
    private static final String HUB_PROJECT_NAME = "hubProjectName";
    private static final long JIRA_PROJECT_ID = 123L;
    private static final int EXPECTED_EVENT_COUNT = 1;
    private static final String JIRA_ADMIN_USERNAME = "jiraAdminUsername";
    private static final String JIRA_ISSUE_CREATOR_USERNAME = "jiraIssueCreatorUsername";
    private static final String JIRA_ADMIN_USER_KEY = "jiraAdminUserKey";
    private static final String JIRA_ISSUE_CREATOR_USER_KEY = "jiraIssueCreatorUserKey";
    private static final String JIRA_PROJECT_NAME = "jiraProjectName";
    private static final String VULN_SOURCE = "NVD";
    private static final String VULN_EXPECTED_PROPERTY_KEY = "t=v|jp=123|hpv=-32224582|hc=|hcv=1816144506";
    private static final String VULN_EXPECTED_RESOLVED_COMMENT = "Automatically resolved; the Black Duck Hub reports no remaining vulnerabilities on this project from this component";
    private static final String VULN_EXPECTED_REOPEN_COMMENT = "Automatically re-opened in response to new Black Duck Hub vulnerabilities on this project from this component";
    private final static String VULN_EXPECTED_COMMENT = "(Black Duck Hub JIRA plugin auto-generated comment)\n" + "Vulnerabilities added: http://localhost:8080/api/components/componentId/versions/versionId (NVD)\n"
            + "Vulnerabilities updated: None\n" + "Vulnerabilities deleted: None\n";
    private final static String VULN_EXPECTED_COMMENT_IF_EXISTS = VULN_EXPECTED_COMMENT;
    private final static String VULN_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = VULN_EXPECTED_COMMENT;
    private final static String VULN_EXPECTED_DESCRIPTION = "This issue tracks vulnerability status changes on " + "Hub project ['hubProjectName' / 'projectVersionName'|" + PROJECT_VERSION_COMPONENTS_URL
            + "], component 'componentName' / 'componentVersion'. " + "For details, see the comments below, or the project's [vulnerabilities|" + VULNERABLE_COMPONENTS_URL + "]" + " in the Hub." + "\nComponent license(s): ";
    private final static String VULN_EXPECTED_SUMMARY = "Black Duck vulnerability status changes on Hub project " + "'hubProjectName' / 'projectVersionName', component 'componentName' / 'componentVersion'";

    private static JiraServices jiraServices;
    private static JiraSettingsService jiraSettingsService;
    private static JiraContext jiraContext;
    private static HubServicesFactory hubServicesFactory;
    private static HubProjectMappings projectMappingObject;
    private static HubJiraFieldCopyConfigSerializable fieldCopyConfig;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Mock the objects that the Converter needs
        jiraServices = Mockito.mock(JiraServices.class);
        final Set<HubProjectMapping> mappings = new HashSet<>();
        final HubProjectMapping mapping = new HubProjectMapping();
        final HubProject hubProject = createHubProject();
        mapping.setHubProject(hubProject);
        final JiraProject jiraProject = createJiraProject();
        mapping.setJiraProject(jiraProject);
        mappings.add(mapping);
        projectMappingObject = new HubProjectMappings(jiraServices, mappings);
        fieldCopyConfig = createFieldCopyMappings();

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

        final ApplicationUserMock jiraAdminUser = new ApplicationUserMock();
        jiraAdminUser.setName(JIRA_ADMIN_USERNAME);
        jiraAdminUser.setKey(JIRA_ADMIN_USER_KEY);
        final ApplicationUserMock jiraIssueCreatorUser = new ApplicationUserMock();
        jiraIssueCreatorUser.setName(JIRA_ISSUE_CREATOR_USERNAME);
        jiraIssueCreatorUser.setKey(JIRA_ISSUE_CREATOR_USER_KEY);
        jiraContext = new JiraContext(jiraAdminUser, jiraIssueCreatorUser);

        jiraSettingsService = new JiraSettingsService(new PluginSettingsMock());
        hubServicesFactory = Mockito.mock(HubServicesFactory.class);
        final ComponentService vulnBomCompReqSvc = Mockito.mock(ComponentService.class);
        final HubService hubRequestService = Mockito.mock(HubService.class);

        final VersionRiskProfileResponse versionRiskProfileResponse = new VersionRiskProfileResponse();
        versionRiskProfileResponse.bomLastUpdatedAt = "2018-04-11T19:19:38.929Z";
        Mockito.when(hubRequestService.getResponse(Mockito.eq(RISK_PROFILE_LINK), Mockito.eq(VersionRiskProfileResponse.class))).thenReturn(versionRiskProfileResponse);

        final ProjectResponse projectResponse = new ProjectResponse();
        projectResponse.projectOwner = PROJECT_OWNER_LINK;
        Mockito.when(hubRequestService.getResponse(Mockito.eq(PROJECT_RESPONSE_LINK), Mockito.eq(ProjectResponse.class))).thenReturn(projectResponse);

        final UserView projectOwnerUserView = new UserView();
        projectOwnerUserView.firstName = "Shmario";
        projectOwnerUserView.lastName = "Bear";
        Mockito.when(hubRequestService.getResponse(Mockito.eq(PROJECT_OWNER_LINK), Mockito.eq(UserView.class))).thenReturn(projectOwnerUserView);

        Mockito.when(hubServicesFactory.createHubService()).thenReturn(hubRequestService);
        Mockito.when(hubServicesFactory.createComponentService()).thenReturn(vulnBomCompReqSvc);

        final List<VersionBomComponentView> bom = new ArrayList<>();
        final VersionBomComponentView bomComp = new VersionBomComponentView();
        bomComp.componentName = "componentName";
        bomComp.componentVersionName = "componentVersion";
        bomComp.componentVersion = PROJECT_VERSION_COMPONENTS_URL;
        bomComp.usages = Arrays.asList(MatchedFileUsagesType.DYNAMICALLY_LINKED);
        bom.add(bomComp);
        // TODO add the correct link
        // Mockito.when(hubService.getResponses(VersionBomComponentView.class, ???, Mockito.anyBoolean())).thenReturn(bom);
        // Mockito.when(mockHubSerivce.getHref(Mockito.any(HubView.class))).thenReturn(PROJECT_VERSION_COMPONENTS_URL);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testVulnerability() throws ConfigurationException, URISyntaxException, IntegrationException {
        test(NotificationType.VULNERABILITY, HubEventAction.ADD_COMMENT, VULN_EXPECTED_COMMENT, VULN_EXPECTED_COMMENT_IF_EXISTS, VULN_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE, VULN_EXPECTED_DESCRIPTION, VULN_EXPECTED_SUMMARY,
                VULNERABILITY_ISSUE_TYPE_ID, VULN_EXPECTED_REOPEN_COMMENT, VULN_EXPECTED_RESOLVED_COMMENT, VULN_EXPECTED_PROPERTY_KEY);
    }

    @Test
    public void testRuleViolation() throws ConfigurationException, URISyntaxException, IntegrationException {
        test(NotificationType.RULE_VIOLATION, HubEventAction.OPEN, null, POLICY_EXPECTED_COMMENT_IF_EXISTS, POLICY_VIOLATION_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE, POLICY_VIOLATION_EXPECTED_DESCRIPTION,
                POLICY_VIOLATION_EXPECTED_SUMMARY,
                POLICY_ISSUE_TYPE_ID, POLICY_VIOLATION_EXPECTED_REOPEN_COMMENT, POLICY_VIOLATION_EXPECTED_RESOLVE_COMMENT, POLICY_EXPECTED_PROPERTY_KEY);
    }

    @Test
    public void testPolicyOverride() throws ConfigurationException, URISyntaxException, IntegrationException {
        test(NotificationType.POLICY_OVERRIDE, HubEventAction.RESOLVE, null, POLICY_OVERRIDE_EXPECTED_COMMENT_IF_EXISTS, POLICY_OVERRIDE_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE, POLICY_OVERRIDE_EXPECTED_DESCRIPTION,
                POLICY_OVERRIDE_EXPECTED_SUMMARY, POLICY_ISSUE_TYPE_ID, POLICY_OVERRIDE_EXPECTED_REOPEN_COMMENT, POLICY_OVERRIDE_EXPECTED_RESOLVE_COMMENT, POLICY_EXPECTED_PROPERTY_KEY);
    }

    @Test
    public void testRuleViolationCleared() throws ConfigurationException, URISyntaxException, IntegrationException {
        test(NotificationType.RULE_VIOLATION_CLEARED, HubEventAction.RESOLVE, null, POLICY_CLEARED_EXPECTED_COMMENT_IF_EXISTS, POLICY_CLEARED_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE, POLICY_CLEARED_EXPECTED_DESCRIPTION,
                POLICY_CLEARED_EXPECTED_SUMMARY, POLICY_ISSUE_TYPE_ID, POLICY_CLEARED_EXPECTED_REOPEN_COMMENT, POLICY_CLEARED_EXPECTED_RESOLVE_COMMENT, POLICY_EXPECTED_PROPERTY_KEY);
    }

    // TODO test this after conversion tests are fixed
    // @Test
    // public void testFindCompInBom() throws ConfigurationException {
    // final ListProcessorCache cache = new ListProcessorCache();
    // final NotificationToEventConverter conv = createConverter(jiraServices, jiraSettingsService, jiraContext, hubServicesFactory, NotifType.POLICY_VIOLATION, projectMappingObject, fieldCopyConfig, cache);
    //
    // final List<VersionBomComponentView> bomComps = new ArrayList<>();
    // addComp(bomComps, "comp1", null, "comp1version1Url");
    // addComp(bomComps, "comp2", null, "comp2version1Url");
    // addComp(bomComps, "comp3", "comp3Url", null);
    // assertEquals("comp1", conv.findCompInBom(bomComps, null, "comp1version1Url").componentName);
    // assertEquals("comp2", conv.findCompInBom(bomComps, null, "comp2version1Url").componentName);
    // assertEquals("comp3", conv.findCompInBom(bomComps, "comp3Url", null).componentName);
    // assertEquals(null, conv.findCompInBom(bomComps, null, "comp1versionXUrl"));
    // assertEquals(null, conv.findCompInBom(bomComps, "compXUrl", null));
    // }
    //
    // private void addComp(final List<VersionBomComponentView> bomComps, final String componentName, final String componentUrl, final String componentVersionUrl) {
    // final VersionBomComponentView bomComp = new VersionBomComponentView();
    // bomComp.componentName = componentName;
    // bomComp.component = componentUrl;
    // bomComp.componentVersion = componentVersionUrl;
    //
    // bomComps.add(bomComp);
    // }

    private void test(final NotificationType notifType, final HubEventAction expectedHubEventAction, final String expectedComment, final String expectedCommentIfExists, final String expectedCommentInLieuOfStateChange,
            final String expectedDescription, final String expectedSummary, final String issueTypeId, final String expectedReOpenComment, final String expectedResolveComment, final String expectedPropertyKey)
            throws ConfigurationException, URISyntaxException, IntegrationException {

        final HubService mockHubSerivce = Mockito.mock(HubService.class);
        final HubBucket mockHubBucket = Mockito.mock(HubBucket.class);
        final HubJiraLogger mockLogger = new HubJiraLogger(null);

        mockHubServiceResponses(mockHubSerivce);
        mockHubBucketResponses(mockHubBucket);

        final NotificationToEventConverter conv = new NotificationToEventConverter(jiraServices, jiraContext, jiraSettingsService, projectMappingObject, fieldCopyConfig, new EventDataFormatHelper(mockLogger, mockHubSerivce),
                Arrays.asList(RULE_URL), mockHubSerivce, mockLogger);
        final CommonNotificationState notif = createNotif(mockHubBucket, notifType, new Date());
        final Collection<EventData> events = conv.convert(notif, mockHubBucket);

        // Verify the generated event
        verifyGeneratedEvents(events, issueTypeId, expectedHubEventAction, expectedComment, expectedCommentIfExists, expectedCommentInLieuOfStateChange, expectedDescription, expectedSummary, expectedReOpenComment, expectedResolveComment,
                expectedPropertyKey);
    }

    private void mockHubServiceResponses(final HubService mockHubService) throws IntegrationException {
        final VersionRiskProfileView riskProfile = new VersionRiskProfileView();
        riskProfile.bomLastUpdatedAt = new Date();
        Mockito.when(mockHubService.getResponse(Mockito.any(), Mockito.eq(ProjectVersionView.RISKPROFILE_LINK_RESPONSE))).thenReturn(riskProfile);

        final ProjectView project = new ProjectView();
        project.projectOwner = PROJECT_OWNER_LINK; // TODO
        Mockito.when(mockHubService.getResponse(Mockito.any(), Mockito.eq(ProjectVersionView.PROJECT_LINK_RESPONSE))).thenReturn(project);

        Mockito.when(mockHubService.getFirstLinkSafely(Mockito.any(), Mockito.eq(ProjectVersionView.COMPONENTS_LINK))).thenReturn(COMPONENT_URL);
        Mockito.when(mockHubService.getFirstLinkSafely(Mockito.any(), Mockito.eq(ProjectVersionView.VULNERABLE_COMPONENTS_LINK))).thenReturn(VULNERABLE_COMPONENTS_URL);
    }

    private void mockHubBucketResponses(final HubBucket mockHubBucket) {
        final UriSingleResponse<ProjectVersionView> mockUriSingleResponseProjectVersionView = new UriSingleResponse<>(PROJECT_VERSION_URL, ProjectVersionView.class);
        Mockito.when(mockHubBucket.get(mockUriSingleResponseProjectVersionView)).thenReturn(createProjectVersionView());

        final UriSingleResponse<ComponentView> mockUriSingleResponseComponentView = new UriSingleResponse<>(COMPONENT_URL, ComponentView.class);
        Mockito.when(mockHubBucket.get(mockUriSingleResponseComponentView)).thenReturn(createComponentView());

        final UriSingleResponse<ComponentVersionView> mockUriSingleResponseComponentVersionView = new UriSingleResponse<>(COMPONENT_VERSION_URL, ComponentVersionView.class);
        Mockito.when(mockHubBucket.get(mockUriSingleResponseComponentVersionView)).thenReturn(createComponentVersionView());
    }

    private CommonNotificationState createNotif(final HubBucket mockHubBucket, final NotificationType notifType, final Date now) throws URISyntaxException, HubIntegrationException, IntegrationException {
        CommonNotificationState notif;
        if (NotificationType.VULNERABILITY.equals(notifType)) {
            notif = createVulnerabilityNotif(mockHubBucket, now);
        } else if (NotificationType.RULE_VIOLATION.equals(notifType)) {
            notif = createRuleViolationNotif(mockHubBucket, now);
        } else if (NotificationType.POLICY_OVERRIDE.equals(notifType)) {
            notif = createPolicyOverrideNotif(mockHubBucket, now);
        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(notifType)) {
            notif = createRuleViolationClearedNotif(mockHubBucket, now);
        } else {
            throw new IllegalArgumentException("Unrecognized notification type");
        }
        return notif;
    }

    private void verifyGeneratedEvents(final Collection<EventData> events, final String issueTypeId, final HubEventAction expectedHubEventAction, final String expectedComment, final String expectedCommentIfExists,
            final String expectedCommentInLieuOfStateChange, final String expectedDescription, final String expectedSummary, final String expectedReOpenComment, final String expectedResolveComment, final String expectedPropertyKey)
            throws HubIntegrationException, URISyntaxException {
        assertEquals(EXPECTED_EVENT_COUNT, events.size());
        // HubEvent<VulnerabilityContentItem> event = events.get(0);
        final EventData eventData = events.iterator().next();
        assertEquals(expectedHubEventAction, eventData.getAction());
        assertEquals(expectedComment, eventData.getJiraIssueComment());
        assertEquals(expectedCommentIfExists, eventData.getJiraIssueCommentForExistingIssue());
        assertEquals(expectedCommentInLieuOfStateChange, eventData.getJiraIssueCommentInLieuOfStateChange());
        assertEquals(ASSIGNEE_USER_ID, eventData.getJiraIssueAssigneeUserId());
        assertEquals(expectedDescription, eventData.getJiraIssueDescription());
        assertEquals(expectedSummary, eventData.getJiraIssueSummary());
        assertEquals(issueTypeId, eventData.getJiraIssueTypeId());

        assertEquals(Long.valueOf(JIRA_PROJECT_ID), eventData.getJiraProjectId());
        assertEquals(JIRA_PROJECT_NAME, eventData.getJiraProjectName());
        assertEquals(JIRA_ADMIN_USER_KEY, eventData.getJiraAdminUserKey());
        assertEquals(JIRA_ADMIN_USERNAME, eventData.getJiraAdminUsername());
        assertEquals(JIRA_ISSUE_CREATOR_USERNAME, eventData.getJiraIssueCreatorUsername());
        final Set<ProjectFieldCopyMapping> fieldMappings = eventData.getJiraFieldCopyMappings();
        assertEquals(1, fieldMappings.size());
        final Iterator<ProjectFieldCopyMapping> iter = fieldMappings.iterator();
        final ProjectFieldCopyMapping actualProjectFieldCopyMapping = iter.next();
        assertEquals(WILDCARD_STRING, actualProjectFieldCopyMapping.getHubProjectName());
        assertEquals(WILDCARD_STRING, actualProjectFieldCopyMapping.getJiraProjectName());
        assertEquals(SOURCE_FIELD_ID, actualProjectFieldCopyMapping.getSourceFieldId());
        assertEquals(SOURCE_FIELD_NAME, actualProjectFieldCopyMapping.getSourceFieldName());
        assertEquals(TARGET_FIELD_ID, actualProjectFieldCopyMapping.getTargetFieldId());
        assertEquals(TARGET_FIELD_NAME, actualProjectFieldCopyMapping.getTargetFieldName());

        assertEquals(expectedReOpenComment, eventData.getJiraIssueReOpenComment());
        assertEquals(expectedResolveComment, eventData.getJiraIssueResolveComment());
        final IssuePropertiesGenerator issuePropertiesGenerator = eventData.getJiraIssuePropertiesGenerator();

        final IssueProperties issueProperties = issuePropertiesGenerator.createIssueProperties(Long.valueOf(JIRA_ISSUE_ID));
        assertEquals(HUB_PROJECT_NAME, issueProperties.getProjectName());
        assertEquals(PROJECT_VERSION_NAME, issueProperties.getProjectVersion());
        assertEquals(COMPONENT_NAME, issueProperties.getComponentName());
        assertEquals(COMPONENT_VERSION_NAME, issueProperties.getComponentVersion());
        assertEquals(Long.valueOf(456L), issueProperties.getJiraIssueId());
        assertEquals(expectedPropertyKey, eventData.getEventKey());
    }

    private CommonNotificationState createVulnerabilityNotif(final HubBucket mockHubBucket, final Date createdAt) throws URISyntaxException, HubIntegrationException {
        // TODO Mockito.when(mockHubBucket.get().thenReturn(VULNERABILITY_ISSUE_TYPE_ID);

        final VulnerabilitySourceQualifiedId vuln = new VulnerabilitySourceQualifiedId();
        vuln.source = VULN_SOURCE;
        vuln.vulnerabilityId = COMPONENT_VERSION_URL; // TODO is this the correct value?
        final List<VulnerabilitySourceQualifiedId> addedVulnList = new ArrayList<>();
        final List<VulnerabilitySourceQualifiedId> updatedVulnList = new ArrayList<>();
        final List<VulnerabilitySourceQualifiedId> deletedVulnList = new ArrayList<>();
        addedVulnList.add(vuln);

        final NotificationUserView notificationUserView = createNotificationUserView(NotificationType.VULNERABILITY, createdAt);

        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.componentName = COMPONENT_NAME;
        content.componentVersion = COMPONENT_VERSION_URL;
        content.versionName = COMPONENT_VERSION_NAME;
        content.newVulnerabilityIds = addedVulnList;
        content.newVulnerabilityCount = addedVulnList.size();
        content.updatedVulnerabilityIds = updatedVulnList;
        content.updatedVulnerabilityCount = updatedVulnList.size();
        content.deletedVulnerabilityIds = deletedVulnList;
        content.deletedVulnerabilityCount = deletedVulnList.size();

        final AffectedProjectVersion affected = new AffectedProjectVersion();
        affected.projectName = HUB_PROJECT_NAME;
        affected.projectVersion = PROJECT_VERSION_URL;
        affected.projectVersionName = PROJECT_VERSION_NAME;
        content.affectedProjectVersions = Arrays.asList(affected);

        return new CommonNotificationState(notificationUserView, content);
    }

    private CommonNotificationState createRuleViolationNotif(final HubBucket mockHubBucket, final Date createdAt) throws URISyntaxException, IntegrationException {
        final PolicyRuleViewV2 policyRule = createPolicyRuleV2(createdAt, POLICY_VIOLATION_EXPECTED_DESCRIPTION);
        Mockito.when(mockHubBucket.get(mockUriSingleResponsePolicyRuleViewV2())).thenReturn(policyRule);

        final PolicyInfo policyInfo = createPolicyInfo();
        final ComponentVersionStatus componentVersionStatus = createComponentVersionStatus();
        final NotificationUserView notificationUserView = createNotificationUserView(NotificationType.RULE_VIOLATION, createdAt);

        final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
        content.componentVersionsInViolation = 1; // TODO
        content.componentVersionStatuses = Arrays.asList(componentVersionStatus);
        content.policyInfos = Arrays.asList(policyInfo);
        content.projectName = HUB_PROJECT_NAME;
        content.projectVersion = PROJECT_VERSION_URL;
        content.projectVersionName = PROJECT_VERSION_NAME;

        return new CommonNotificationState(notificationUserView, content);
    }

    private CommonNotificationState createRuleViolationClearedNotif(final HubBucket mockHubBucket, final Date createdAt) throws URISyntaxException, IntegrationException {
        final PolicyRuleViewV2 policyRule = createPolicyRuleV2(createdAt, POLICY_CLEARED_EXPECTED_DESCRIPTION);
        Mockito.when(mockHubBucket.get(mockUriSingleResponsePolicyRuleViewV2())).thenReturn(policyRule);

        final PolicyInfo policyInfo = createPolicyInfo();
        final ComponentVersionStatus componentVersionStatus = createComponentVersionStatus();
        final NotificationUserView notificationUserView = createNotificationUserView(NotificationType.RULE_VIOLATION_CLEARED, createdAt);

        final RuleViolationClearedNotificationContent content = new RuleViolationClearedNotificationContent();
        content.componentVersionsCleared = 1; // TODO
        content.componentVersionStatuses = Arrays.asList(componentVersionStatus);
        content.policyInfos = Arrays.asList(policyInfo);
        content.projectName = HUB_PROJECT_NAME;
        content.projectVersion = PROJECT_VERSION_URL;
        content.projectVersionName = PROJECT_VERSION_NAME;

        return new CommonNotificationState(notificationUserView, content);
    }

    private CommonNotificationState createPolicyOverrideNotif(final HubBucket mockHubBucket, final Date createdAt) throws URISyntaxException, IntegrationException {
        final PolicyRuleViewV2 policyRule = createPolicyRuleV2(createdAt, POLICY_OVERRIDE_EXPECTED_DESCRIPTION);
        Mockito.when(mockHubBucket.get(mockUriSingleResponsePolicyRuleViewV2())).thenReturn(policyRule);

        final PolicyInfo policyInfo = createPolicyInfo();
        final NotificationUserView notificationUserView = createNotificationUserView(NotificationType.POLICY_OVERRIDE, createdAt);

        final PolicyOverrideNotificationContent content = new PolicyOverrideNotificationContent();
        content.bomComponentVersionPolicyStatus = "???"; // TODO
        content.component = COMPONENT_URL;
        content.componentName = COMPONENT_NAME;
        content.componentVersion = COMPONENT_VERSION_URL;
        content.componentVersionName = COMPONENT_VERSION_NAME;
        content.firstName = OVERRIDER_FIRST_NAME;
        content.lastName = OVERRIDER_LAST_NAME;
        content.policies = Arrays.asList(policyInfo.policyName);
        content.policyInfos = Arrays.asList(policyInfo);
        content.projectName = HUB_PROJECT_NAME;
        content.projectVersion = PROJECT_VERSION_URL;
        content.projectVersionName = PROJECT_VERSION_NAME;

        return new CommonNotificationState(notificationUserView, content);
    }

    private static HubProject createHubProject() {
        final HubProject hubProject = new HubProject();
        hubProject.setProjectName(HUB_PROJECT_NAME);
        hubProject.setProjectUrl(HUB_PROJECT_URL);
        return hubProject;
    }

    private static JiraProject createJiraProject() {
        final JiraProject jiraProject = new JiraProject();
        jiraProject.setProjectName(JIRA_PROJECT_NAME);
        jiraProject.setProjectId(JIRA_PROJECT_ID);
        jiraProject.setAssigneeUserId(ASSIGNEE_USER_ID);
        return jiraProject;
    }

    private static HubJiraFieldCopyConfigSerializable createFieldCopyMappings() {
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

    private ProjectVersionView createProjectVersionView() {
        final ProjectVersionView projectVersion = new ProjectVersionView();
        projectVersion.nickname = "???";
        projectVersion.phase = ProjectVersionPhaseType.PLANNING;
        projectVersion.releaseComments = "???";
        projectVersion.releasedOn = new Date();
        projectVersion.source = OriginSourceType.KB;
        projectVersion.versionName = PROJECT_VERSION_NAME;
        return projectVersion;
    }

    private ComponentView createComponentView() {
        final ComponentView component = new ComponentView();
        component.description = "???";
        component.name = COMPONENT_NAME;
        component.source = OriginSourceType.KB;
        return component;
    }

    private ComponentVersionView createComponentVersionView() {
        final ComponentVersionView componentVersion = new ComponentVersionView();
        componentVersion.license = new ComplexLicenseView();
        componentVersion.releasedOn = new Date();
        componentVersion.source = OriginSourceType.KB;
        componentVersion.versionName = COMPONENT_VERSION_NAME;
        return componentVersion;
    }

    private NotificationUserView createNotificationUserView(final NotificationType type, final Date createdAt) {
        final NotificationUserView notificationUserView = new NotificationUserView();
        notificationUserView.createdAt = createdAt;
        notificationUserView.type = type;
        notificationUserView.notificationState = NotificationStateRequestStateType.NEW;
        return notificationUserView;
    }

    private ComponentVersionStatus createComponentVersionStatus() {
        final ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
        componentVersionStatus.bomComponentVersionPolicyStatus = "???";
        componentVersionStatus.component = COMPONENT_URL;
        componentVersionStatus.componentIssueLink = "???";
        componentVersionStatus.componentName = COMPONENT_NAME;
        componentVersionStatus.componentVersion = COMPONENT_VERSION_URL;
        componentVersionStatus.componentVersionName = COMPONENT_VERSION_NAME;
        componentVersionStatus.policies = Arrays.asList(RULE_URL);
        return componentVersionStatus;
    }

    private PolicyInfo createPolicyInfo() {
        final PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.policyName = RULE_NAME;
        policyInfo.policy = RULE_URL;
        return policyInfo;
    }

    private PolicyRuleViewV2 createPolicyRuleV2(final Date createdAt, final String description) {
        final PolicyRuleViewV2 policyRule = new PolicyRuleViewV2();
        policyRule.createdAt = createdAt;
        policyRule.createdBy = JIRA_ISSUE_CREATOR_USERNAME; // TODO
        policyRule.createdByUser = JIRA_ISSUE_CREATOR_USERNAME;
        policyRule.description = description;
        policyRule.enabled = Boolean.TRUE;
        policyRule.expression = new PolicyRuleExpressionSetView();
        policyRule.name = RULE_NAME;
        policyRule.overridable = Boolean.TRUE;
        policyRule.severity = "Blocker"; // TODO
        policyRule.updatedAt = createdAt;
        policyRule.updatedBy = JIRA_ISSUE_CREATOR_USERNAME; // TODO
        policyRule.updatedByUser = JIRA_ISSUE_CREATOR_USERNAME;
        return policyRule;
    }

    private UriSingleResponse<PolicyRuleViewV2> mockUriSingleResponsePolicyRuleViewV2() {
        return new UriSingleResponse<>(RULE_URL, PolicyRuleViewV2.class);
    }
}
