/**
 * Black Duck JIRA Plugin
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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProject;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.OldIssueProperties;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataFormatHelper;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.component.AffectedProjectVersion;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView;
import com.synopsys.integration.blackduck.api.generated.component.RiskCountView;
import com.synopsys.integration.blackduck.api.generated.component.VersionBomLicenseView;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.enumeration.OriginSourceType;
import com.synopsys.integration.blackduck.api.generated.enumeration.PolicySummaryStatusType;
import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionPhaseType;
import com.synopsys.integration.blackduck.api.generated.enumeration.RiskCountType;
import com.synopsys.integration.blackduck.api.generated.response.VersionRiskProfileView;
import com.synopsys.integration.blackduck.api.generated.view.ComplexLicenseView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleViewV2;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.notification.NotificationDetailResult;
import com.synopsys.integration.blackduck.notification.content.BomEditContent;
import com.synopsys.integration.blackduck.notification.content.ComponentVersionStatus;
import com.synopsys.integration.blackduck.notification.content.PolicyInfo;
import com.synopsys.integration.blackduck.notification.content.PolicyOverrideNotificationContent;
import com.synopsys.integration.blackduck.notification.content.RuleViolationClearedNotificationContent;
import com.synopsys.integration.blackduck.notification.content.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.notification.content.VulnerabilityNotificationContent;
import com.synopsys.integration.blackduck.notification.content.VulnerabilitySourceQualifiedId;
import com.synopsys.integration.blackduck.notification.content.detail.NotificationContentDetail;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.rest.CredentialsRestConnection;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.bucket.HubBucket;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class NotificationConverterTest {
    private static final long JIRA_ISSUE_ID = 456L;
    private static final String OVERRIDER_LAST_NAME = "lastName";
    private static final String OVERRIDER_FIRST_NAME = "firstName";
    private static final String RULE_URL = "http://localhost:8080/api/rules/ruleId";
    private static final String VULNERABLE_COMPONENTS_URL = "http://localhost:8080/api/projects/x/versions/y/vulnerable-components";
    private static final String RULE_NAME = "Test Rule";
    private static final String POLICY_EXPECTED_PROPERTY_KEY = "t=p|jp=123|hpv=-32224582|hc=|hcv=1816144506|hr=1736320804";
    private static final String POLICY_CLEARED_EXPECTED_COMMENT_IF_EXISTS = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_CLEARED_COMMENT;
    private static final String POLICY_CLEARED_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_CLEARED_COMMENT;
    private static final String POLICY_VIOLATION_EXPECTED_DESCRIPTION = "Black Duck has detected a policy violation.  \n\n";
    private static final String POLICY_CLEARED_EXPECTED_DESCRIPTION = POLICY_VIOLATION_EXPECTED_DESCRIPTION;
    private static final String POLICY_CLEARED_EXPECTED_SUMMARY = "Black Duck Policy Violation: Project 'hubProjectName' / 'projectVersionName', Component 'componentName' / 'componentVersion' [Rule: 'Test Rule']";
    private static final String POLICY_CLEARED_EXPECTED_REOPEN_COMMENT = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_REOPEN;
    private static final String POLICY_CLEARED_EXPECTED_RESOLVE_COMMENT = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_CLEARED_RESOLVE;
    private static final String POLICY_OVERRIDE_EXPECTED_COMMENT_IF_EXISTS = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_OVERRIDDEN_COMMENT;
    private static final String POLICY_OVERRIDE_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_OVERRIDDEN_COMMENT;
    private static final String POLICY_OVERRIDE_EXPECTED_DESCRIPTION = POLICY_VIOLATION_EXPECTED_DESCRIPTION;
    private static final String POLICY_OVERRIDE_EXPECTED_SUMMARY = "Black Duck Policy Violation: Project 'hubProjectName' / 'projectVersionName', Component 'componentName' / 'componentVersion' [Rule: 'Test Rule']";
    private static final String POLICY_OVERRIDE_EXPECTED_REOPEN_COMMENT = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_REOPEN;
    private static final String POLICY_OVERRIDE_EXPECTED_RESOLVE_COMMENT = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_RESOLVE;
    private static final String POLICY_VIOLATION_EXPECTED_RESOLVE_COMMENT = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_RESOLVE;
    private static final String POLICY_VIOLATION_EXPECTED_REOPEN_COMMENT = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_REOPEN;
    private static final String POLICY_VIOLATION_EXPECTED_SUMMARY = "Black Duck Policy Violation: Project 'hubProjectName' / 'projectVersionName', Component 'componentName' / 'componentVersion' [Rule: '" + RULE_NAME + "']";
    private static final String POLICY_EXPECTED_COMMENT_IF_EXISTS = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_DETECTED_AGAIN_COMMENT;
    private static final String POLICY_VIOLATION_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = POLICY_EXPECTED_COMMENT_IF_EXISTS;
    private static final String VULNERABILITY_ISSUE_TYPE_ID = "Black Duck Security Vulnerability ID";
    private static final String VULNERABILITY_ISSUE_TYPE_NAME = "Black Duck Security Vulnerability";
    private static final String POLICY_ISSUE_TYPE_ID = "Black Duck Policy Violation ID";
    private static final String POLICY_ISSUE_TYPE_NAME = "Black Duck Policy Violation";
    private static final String TARGET_FIELD_NAME = "targetFieldName";
    private static final String TARGET_FIELD_ID = "targetFieldId";
    private static final String SOURCE_FIELD_NAME = "sourceFieldName";
    private static final String SOURCE_FIELD_ID = "sourceFieldId";
    private static final String WILDCARD_STRING = "*";
    private static final String BLACKDUCK_PROJECT_URL = "hubProjectUrl";
    private static final String PROJECT_VERSION_NAME = "projectVersionName";
    private static final String PROJECT_VERSION_URL = "http://localhost:8080/api/projects/projectId/versions/versionId";
    private static final String COMPONENT_VERSION_URL = "http://localhost:8080/api/components/componentId/versions/versionId";
    private static final String COMPONENT_URL = "http://localhost:8080/api/components/componentId";
    private static final String COMPONENT_VERSION_NAME = "componentVersion";
    private static final String COMPONENT_NAME = "componentName";
    private static final String BOM_COMPONENT_URI = "http://localhost:8080/api/projects/projectId/versions/versionId/components/componentId";
    private static final String ASSIGNEE_USER_ID = "assigneeUserId";
    private static final String BLACKDUCK_PROJECT_NAME = "hubProjectName";
    private static final long JIRA_PROJECT_ID = 123L;
    private static final int EXPECTED_EVENT_COUNT = 1;
    private static final String JIRA_ADMIN_USERNAME = "jiraAdminUsername";
    private static final String JIRA_ISSUE_CREATOR_USERNAME = "jiraIssueCreatorUsername";
    private static final String JIRA_ADMIN_USER_KEY = "jiraAdminUserKey";
    private static final String JIRA_ISSUE_CREATOR_USER_KEY = "jiraIssueCreatorUserKey";
    private static final String JIRA_PROJECT_NAME = "jiraProjectName";
    private static final String VULN_SOURCE = "NVD";
    private static final String VULN_EXPECTED_PROPERTY_KEY = "t=v|jp=123|hpv=-32224582|hc=|hcv=1816144506";
    private static final String VULN_EXPECTED_RESOLVED_COMMENT = BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_RESOLVE;
    private static final String VULN_EXPECTED_REOPEN_COMMENT = BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_REOPEN;
    private final static String VULN_EXPECTED_COMMENT = "(Black Duck plugin auto-generated comment)\n" + "Vulnerabilities *added*: http://localhost:8080/api/components/componentId/versions/versionId (NVD)\n"
            + "Vulnerabilities _updated_: None\n" + "Vulnerabilities _deleted_: None\n";
    private final static String BOM_EDIT_VULN_EXPECTED_COMMENT = "(Black Duck plugin auto-generated comment)\nVulnerabilities _added_: None\nVulnerabilities _updated_: None\nVulnerabilities _deleted_: None\n";
    private final static String VULN_EXPECTED_COMMENT_IF_EXISTS = VULN_EXPECTED_COMMENT;
    private final static String VULN_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = VULN_EXPECTED_COMMENT;
    private final static String VULN_EXPECTED_DESCRIPTION = "Black Duck has detected vulnerabilities. For details, see the comments below, or the project's [vulnerabilities|" + VULNERABLE_COMPONENTS_URL + "] in Black Duck.  \n\n";
    private final static String VULN_EXPECTED_SUMMARY = BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE + ": Project " + "'hubProjectName' / 'projectVersionName', Component 'componentName' / 'componentVersion'";
    private static JiraServices jiraServices;
    private static JiraSettingsService jiraSettingsService;
    private static JiraUserContext jiraContext;
    private static HubService mockBlackDuckSerivce;
    private static HubBucket mockBlackDuckBucket;
    private static BlackDuckJiraLogger mockLogger;
    private static BlackDuckProjectMappings projectMappingObject;
    private static BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig;
    private static EventDataFormatHelper eventDataFormatHelper;

    @BeforeClass
    // Mock the objects that the Converter needs
    public static void setUpBeforeClass() throws Exception {
        // Jira Services
        jiraServices = Mockito.mock(JiraServices.class);
        final List<IssueType> issueTypes = new ArrayList<>();
        IssueType issueType = Mockito.mock(IssueType.class);
        Mockito.when(issueType.getName()).thenReturn(VULNERABILITY_ISSUE_TYPE_NAME);
        Mockito.when(issueType.getId()).thenReturn(VULNERABILITY_ISSUE_TYPE_ID);
        issueTypes.add(issueType);

        issueType = Mockito.mock(IssueType.class);
        Mockito.when(issueType.getName()).thenReturn(POLICY_ISSUE_TYPE_NAME);
        Mockito.when(issueType.getId()).thenReturn(POLICY_ISSUE_TYPE_ID);
        issueTypes.add(issueType);

        final ConstantsManager constantsManager = Mockito.mock(ConstantsManager.class);
        Mockito.when(constantsManager.getAllIssueTypeObjects()).thenReturn(issueTypes);
        Mockito.when(jiraServices.getConstantsManager()).thenReturn(constantsManager);

        final Set<BlackDuckProjectMapping> mappings = new HashSet<>();
        final BlackDuckProjectMapping mapping = new BlackDuckProjectMapping();
        final BlackDuckProject blackDuckProject = createBlackDuckProject();
        mapping.setHubProject(blackDuckProject);
        final JiraProject jiraProject = createJiraProject();
        mapping.setJiraProject(jiraProject);
        mappings.add(mapping);

        Mockito.when(jiraServices.getJiraProject(JIRA_PROJECT_ID)).thenReturn(jiraProject);

        // Jira Settings Service
        jiraSettingsService = new JiraSettingsService(new PluginSettingsMock());

        // Jira Context
        final ApplicationUserMock jiraAdminUser = new ApplicationUserMock();
        jiraAdminUser.setName(JIRA_ADMIN_USERNAME);
        jiraAdminUser.setKey(JIRA_ADMIN_USER_KEY);
        final ApplicationUserMock jiraIssueCreatorUser = new ApplicationUserMock();
        jiraIssueCreatorUser.setName(JIRA_ISSUE_CREATOR_USERNAME);
        jiraIssueCreatorUser.setKey(JIRA_ISSUE_CREATOR_USER_KEY);
        jiraContext = new JiraUserContext(jiraAdminUser, jiraIssueCreatorUser);

        // Black Duck Services
        mockBlackDuckSerivce = Mockito.mock(HubService.class);
        mockHubServiceResponses(mockBlackDuckSerivce);
        mockBlackDuckBucket = Mockito.mock(HubBucket.class);
        mockBlackDuckBucketResponses(mockBlackDuckBucket);
        mockLogger = new BlackDuckJiraLogger(Logger.getLogger(NotificationConverterTest.class));

        // Project Mappings
        projectMappingObject = new BlackDuckProjectMappings(jiraServices, mappings);
        fieldCopyConfig = createFieldCopyMappings();

        // EventData Format Helper
        eventDataFormatHelper = new EventDataFormatHelper(mockBlackDuckSerivce);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testVulnerability() throws ConfigurationException, URISyntaxException, IntegrationException {
        test(NotificationType.VULNERABILITY, BlackDuckEventAction.ADD_COMMENT, VULN_EXPECTED_COMMENT, VULN_EXPECTED_COMMENT_IF_EXISTS, VULN_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE, VULN_EXPECTED_DESCRIPTION, VULN_EXPECTED_SUMMARY,
                VULNERABILITY_ISSUE_TYPE_ID, VULN_EXPECTED_REOPEN_COMMENT, VULN_EXPECTED_RESOLVED_COMMENT, VULN_EXPECTED_PROPERTY_KEY);
    }

    @Test
    public void testBomEdit() throws ConfigurationException, URISyntaxException, IntegrationException {
        test(NotificationType.BOM_EDIT, BlackDuckEventAction.UPDATE_IF_EXISTS, BOM_EDIT_VULN_EXPECTED_COMMENT, BOM_EDIT_VULN_EXPECTED_COMMENT, BOM_EDIT_VULN_EXPECTED_COMMENT, VULN_EXPECTED_DESCRIPTION, VULN_EXPECTED_SUMMARY,
                VULNERABILITY_ISSUE_TYPE_ID, VULN_EXPECTED_REOPEN_COMMENT, VULN_EXPECTED_RESOLVED_COMMENT, VULN_EXPECTED_PROPERTY_KEY);
    }

    @Test
    public void testRuleViolation() throws ConfigurationException, URISyntaxException, IntegrationException {
        test(NotificationType.RULE_VIOLATION, BlackDuckEventAction.OPEN, null, POLICY_EXPECTED_COMMENT_IF_EXISTS, POLICY_VIOLATION_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE, POLICY_VIOLATION_EXPECTED_DESCRIPTION,
                POLICY_VIOLATION_EXPECTED_SUMMARY, POLICY_ISSUE_TYPE_ID, POLICY_VIOLATION_EXPECTED_REOPEN_COMMENT, POLICY_VIOLATION_EXPECTED_RESOLVE_COMMENT, POLICY_EXPECTED_PROPERTY_KEY);
    }

    @Test
    public void testPolicyOverride() throws ConfigurationException, URISyntaxException, IntegrationException {
        test(NotificationType.POLICY_OVERRIDE, BlackDuckEventAction.RESOLVE, null, POLICY_OVERRIDE_EXPECTED_COMMENT_IF_EXISTS, POLICY_OVERRIDE_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE, POLICY_OVERRIDE_EXPECTED_DESCRIPTION,
                POLICY_OVERRIDE_EXPECTED_SUMMARY, POLICY_ISSUE_TYPE_ID, POLICY_OVERRIDE_EXPECTED_REOPEN_COMMENT, POLICY_OVERRIDE_EXPECTED_RESOLVE_COMMENT, POLICY_EXPECTED_PROPERTY_KEY);
    }

    @Test
    public void testRuleViolationCleared() throws ConfigurationException, URISyntaxException, IntegrationException {
        test(NotificationType.RULE_VIOLATION_CLEARED, BlackDuckEventAction.RESOLVE, null, POLICY_CLEARED_EXPECTED_COMMENT_IF_EXISTS, POLICY_CLEARED_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE, POLICY_CLEARED_EXPECTED_DESCRIPTION,
                POLICY_CLEARED_EXPECTED_SUMMARY, POLICY_ISSUE_TYPE_ID, POLICY_CLEARED_EXPECTED_REOPEN_COMMENT, POLICY_CLEARED_EXPECTED_RESOLVE_COMMENT, POLICY_EXPECTED_PROPERTY_KEY);
    }

    @Test
    public void testFindCompInBom() throws ConfigurationException, HubIntegrationException {
        final OldNotificationToEventConverter conv = new OldNotificationToEventConverter(jiraServices, jiraContext, jiraSettingsService, projectMappingObject, fieldCopyConfig, eventDataFormatHelper, Arrays.asList(RULE_URL),
                mockBlackDuckSerivce, mockLogger);
        final String compVer1Url = "comp1version1Url";
        final String compVer2Url = "comp2version1Url";
        final String comp3Url = "comp3Url";

        final List<VersionBomComponentView> bomComps = new ArrayList<>();
        addComp(bomComps, "comp1", null, compVer1Url);
        addComp(bomComps, "comp2", null, compVer2Url);
        addComp(bomComps, "comp3", comp3Url, null);

        final ComponentVersionView actualCompVer1 = new ComponentVersionView();
        final ComponentVersionView actualCompVer2 = new ComponentVersionView();
        final ComponentView actualComp3 = new ComponentView();

        // Needed to override stateful equals
        actualCompVer1.versionName = compVer1Url;
        actualCompVer2.versionName = compVer2Url;
        actualComp3.name = comp3Url;

        Mockito.when(mockBlackDuckSerivce.getHref(actualCompVer1)).thenReturn(compVer1Url);
        Mockito.when(mockBlackDuckSerivce.getHref(actualCompVer2)).thenReturn(compVer2Url);
        Mockito.when(mockBlackDuckSerivce.getHref(actualComp3)).thenReturn(comp3Url);

        assertEquals("comp1", conv.findCompInBom(bomComps, null, actualCompVer1).componentName);
        assertEquals("comp2", conv.findCompInBom(bomComps, null, actualCompVer2).componentName);
        assertEquals("comp3", conv.findCompInBom(bomComps, actualComp3, null).componentName);

        final ComponentVersionView nonCompVer1 = new ComponentVersionView();
        nonCompVer1.versionName = "x";
        final ComponentView nonComp1 = new ComponentView();
        nonComp1.name = "y";

        Mockito.when(mockBlackDuckSerivce.getHref(nonCompVer1)).thenReturn("x");
        Mockito.when(mockBlackDuckSerivce.getHref(nonComp1)).thenReturn("y");

        assertEquals(null, conv.findCompInBom(bomComps, null, nonCompVer1));
        assertEquals(null, conv.findCompInBom(bomComps, nonComp1, null));
    }

    private void addComp(final List<VersionBomComponentView> bomComps, final String componentName, final String componentUrl, final String componentVersionUrl) {
        final VersionBomComponentView bomComp = new VersionBomComponentView();
        bomComp.componentName = componentName;
        bomComp.component = componentUrl;
        bomComp.componentVersion = componentVersionUrl;

        bomComps.add(bomComp);
    }

    private void test(final NotificationType notifType, final BlackDuckEventAction expectedBlackDuckEventAction, final String expectedComment, final String expectedCommentIfExists, final String expectedCommentInLieuOfStateChange,
            final String expectedDescription, final String expectedSummary, final String issueTypeId, final String expectedReOpenComment, final String expectedResolveComment, final String expectedPropertyKey)
            throws URISyntaxException, IntegrationException, ConfigurationException {

        final Date startDate = new Date();
        final NotificationDetailResult notificationDetailResults = createNotification(mockBlackDuckBucket, notifType, startDate);
        // Old Converter
        if (!NotificationType.BOM_EDIT.equals(notifType)) {
            final OldNotificationToEventConverter oldConverter = new OldNotificationToEventConverter(jiraServices, jiraContext, jiraSettingsService, projectMappingObject, fieldCopyConfig, eventDataFormatHelper, Arrays.asList(RULE_URL),
                    mockBlackDuckSerivce, mockLogger);
            final Collection<EventData> oldEvents = oldConverter.createEventDataForNotificationDetailResult(notificationDetailResults, mockBlackDuckBucket, startDate);
            verifyGeneratedEvents(oldEvents, issueTypeId, expectedBlackDuckEventAction, expectedComment, expectedCommentIfExists, expectedCommentInLieuOfStateChange, expectedDescription, expectedSummary, expectedReOpenComment,
                    expectedResolveComment, expectedPropertyKey);
        }

        // New Converter
        final BomNotificationToEventConverter newConverter = new BomNotificationToEventConverter(jiraServices, jiraContext, jiraSettingsService, projectMappingObject, fieldCopyConfig, eventDataFormatHelper, Arrays.asList(RULE_URL),
                mockBlackDuckSerivce, mockLogger);
        final Collection<EventData> newEvents = newConverter.convertToEventData(notificationDetailResults, mockBlackDuckBucket, startDate);
        verifyGeneratedEvents(newEvents, issueTypeId, expectedBlackDuckEventAction, expectedComment, expectedCommentIfExists, expectedCommentInLieuOfStateChange, expectedDescription, expectedSummary, expectedReOpenComment,
                expectedResolveComment, expectedPropertyKey);
    }

    private static void mockHubServiceResponses(final HubService mockBlackDuckService) throws IntegrationException, MalformedURLException {
        final URL blackDuckBaseUrl = new URL("https://localhost:8080");

        final BlackduckRestConnection mockRestConnection = new CredentialsRestConnection(Mockito.mock(BlackDuckJiraLogger.class), blackDuckBaseUrl, "", "", 120, ProxyInfo.NO_PROXY_INFO);
        Mockito.when(mockBlackDuckService.getRestConnection()).thenReturn(mockRestConnection);
        Mockito.when(mockBlackDuckService.getHubBaseUrl()).thenReturn(blackDuckBaseUrl);

        final VersionRiskProfileView riskProfile = new VersionRiskProfileView();
        riskProfile.bomLastUpdatedAt = new Date();
        Mockito.when(mockBlackDuckService.getResponse(Mockito.any(), Mockito.eq(ProjectVersionView.RISKPROFILE_LINK_RESPONSE))).thenReturn(riskProfile);

        final ProjectView project = new ProjectView();
        project.projectOwner = "Shmario Bear";
        project.name = BLACKDUCK_PROJECT_NAME;
        project._meta = new ResourceMetadata();
        project._meta.href = BLACKDUCK_PROJECT_URL;
        Mockito.when(mockBlackDuckService.getResponse(Mockito.any(), Mockito.eq(ProjectVersionView.PROJECT_LINK_RESPONSE))).thenReturn(project);
        Mockito.when(mockBlackDuckService.getResponse(PROJECT_VERSION_URL, ProjectVersionView.class)).thenReturn(createProjectVersionView());

        Mockito.when(mockBlackDuckService.getFirstLinkSafely(Mockito.any(), Mockito.eq(ProjectVersionView.COMPONENTS_LINK))).thenReturn(COMPONENT_URL);
        Mockito.when(mockBlackDuckService.getFirstLinkSafely(Mockito.any(), Mockito.eq(ProjectVersionView.VULNERABLE_COMPONENTS_LINK))).thenReturn(VULNERABLE_COMPONENTS_URL);

        Mockito.when(mockBlackDuckService.getHref(Mockito.any(PolicyRuleViewV2.class))).thenReturn(RULE_URL);
        Mockito.when(mockBlackDuckService.getHref(Mockito.any(ProjectVersionView.class))).thenReturn(PROJECT_VERSION_URL);
        Mockito.when(mockBlackDuckService.getHref(Mockito.any(VersionBomComponentView.class))).thenReturn(BOM_COMPONENT_URI);
    }

    private static void mockBlackDuckBucketResponses(final HubBucket mockBlackDuckBucket) {
        final UriSingleResponse<ProjectVersionView> mockUriSingleResponseProjectVersionView = new UriSingleResponse<>(PROJECT_VERSION_URL, ProjectVersionView.class);
        Mockito.when(mockBlackDuckBucket.get(mockUriSingleResponseProjectVersionView)).thenReturn(createProjectVersionView());
        Mockito.when(mockBlackDuckBucket.get(PROJECT_VERSION_URL, ProjectVersionView.class)).thenReturn(createProjectVersionView());

        final UriSingleResponse<ComponentView> mockUriSingleResponseComponentView = new UriSingleResponse<>(COMPONENT_URL, ComponentView.class);
        Mockito.when(mockBlackDuckBucket.get(mockUriSingleResponseComponentView)).thenReturn(createComponentView());

        final UriSingleResponse<ComponentVersionView> mockUriSingleResponseComponentVersionView = new UriSingleResponse<>(COMPONENT_VERSION_URL, ComponentVersionView.class);
        Mockito.when(mockBlackDuckBucket.get(mockUriSingleResponseComponentVersionView)).thenReturn(createComponentVersionView());
        Mockito.when(mockBlackDuckBucket.get(COMPONENT_VERSION_URL, ComponentVersionView.class)).thenReturn(createComponentVersionView());

        final UriSingleResponse<VersionBomComponentView> mockUriSingleResponseVersionBomComponentView = new UriSingleResponse<>(BOM_COMPONENT_URI, VersionBomComponentView.class);
        Mockito.when(mockBlackDuckBucket.get(mockUriSingleResponseVersionBomComponentView)).thenReturn(createVersionBomComponentView());
        Mockito.when(mockBlackDuckBucket.get(BOM_COMPONENT_URI, VersionBomComponentView.class)).thenReturn(createVersionBomComponentView());
    }

    private NotificationDetailResult createNotification(final HubBucket mockBlackDuckBucket, final NotificationType notifType, final Date now) throws URISyntaxException, HubIntegrationException, IntegrationException {
        if (NotificationType.VULNERABILITY.equals(notifType)) {
            return createVulnerabilityNotif(now);
        } else if (NotificationType.RULE_VIOLATION.equals(notifType)) {
            return createRuleViolationNotif(mockBlackDuckBucket, now);
        } else if (NotificationType.POLICY_OVERRIDE.equals(notifType)) {
            return createPolicyOverrideNotif(mockBlackDuckBucket, now);
        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(notifType)) {
            return createRuleViolationClearedNotif(mockBlackDuckBucket, now);
        } else if (NotificationType.BOM_EDIT.equals(notifType)) {
            return createBomEditNotif(now);
        } else {
            throw new IllegalArgumentException("Unrecognized notification type");
        }
    }

    private void verifyGeneratedEvents(final Collection<EventData> events, final String issueTypeId, final BlackDuckEventAction expectedHubEventAction, final String expectedComment, final String expectedCommentIfExists,
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

        final OldIssueProperties issueProperties = OldIssueProperties.fromEventData(eventData, Long.valueOf(JIRA_ISSUE_ID));
        assertEquals(BLACKDUCK_PROJECT_NAME, issueProperties.getProjectName());
        assertEquals(PROJECT_VERSION_NAME, issueProperties.getProjectVersion());
        assertEquals(COMPONENT_NAME, issueProperties.getComponentName());
        assertEquals(COMPONENT_VERSION_NAME, issueProperties.getComponentVersion());
        assertEquals(Long.valueOf(456L), issueProperties.getJiraIssueId());
        assertEquals(expectedPropertyKey, eventData.getEventKey());
    }

    private NotificationDetailResult createVulnerabilityNotif(final Date createdAt) throws URISyntaxException, HubIntegrationException {
        final VulnerabilitySourceQualifiedId vuln = new VulnerabilitySourceQualifiedId();
        vuln.source = VULN_SOURCE;
        vuln.vulnerabilityId = COMPONENT_VERSION_URL;
        final List<VulnerabilitySourceQualifiedId> addedVulnList = new ArrayList<>();
        final List<VulnerabilitySourceQualifiedId> updatedVulnList = new ArrayList<>();
        final List<VulnerabilitySourceQualifiedId> deletedVulnList = new ArrayList<>();
        addedVulnList.add(vuln);

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
        affected.projectName = BLACKDUCK_PROJECT_NAME;
        affected.projectVersion = PROJECT_VERSION_URL;
        affected.projectVersionName = PROJECT_VERSION_NAME;
        affected.bomComponent = BOM_COMPONENT_URI;
        content.affectedProjectVersions = Arrays.asList(affected);

        final Optional<String> projectName = Optional.of(BLACKDUCK_PROJECT_NAME);
        final Optional<String> projectVersionName = Optional.of(PROJECT_VERSION_NAME);
        final Optional<String> projectVersionUri = Optional.of(PROJECT_VERSION_URL);

        final Optional<String> componentName = Optional.of(COMPONENT_NAME);
        final Optional<String> componentVersionName = Optional.of(COMPONENT_VERSION_NAME);
        final Optional<String> componentVersionUri = Optional.of(COMPONENT_VERSION_URL);
        final Optional<String> componentVersionOriginId = Optional.of("compVerOriginId");
        final Optional<String> componentVersionOriginName = Optional.of("compVerOriginName");
        final Optional<String> bomComponent = Optional.of(BOM_COMPONENT_URI);

        final NotificationContentDetail detail = NotificationContentDetail.createDetail(NotificationContentDetail.CONTENT_KEY_GROUP_VULNERABILITY, projectName, projectVersionName, projectVersionUri, componentName, Optional.empty(),
                componentVersionName, componentVersionUri, Optional.empty(), Optional.empty(), componentVersionOriginName, Optional.empty(), componentVersionOriginId, bomComponent);

        return new NotificationDetailResult(content, "application/json", createdAt, NotificationType.VULNERABILITY, NotificationContentDetail.CONTENT_KEY_GROUP_VULNERABILITY, Optional.empty(), Arrays.asList(detail));
    }

    private NotificationDetailResult createRuleViolationNotif(final HubBucket mockBlackDuckBucket, final Date createdAt) throws URISyntaxException, IntegrationException {
        final PolicyRuleViewV2 policyRule = createPolicyRuleV2(createdAt, POLICY_VIOLATION_EXPECTED_DESCRIPTION);
        Mockito.when(mockBlackDuckBucket.get(mockUriSingleResponsePolicyRuleViewV2())).thenReturn(policyRule);

        final PolicyInfo policyInfo = createPolicyInfo();
        final ComponentVersionStatus componentVersionStatus = createComponentVersionStatus();

        final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
        content.componentVersionStatuses = Arrays.asList(componentVersionStatus);
        content.componentVersionsInViolation = content.componentVersionStatuses.size();
        content.policyInfos = Arrays.asList(policyInfo);
        content.projectName = BLACKDUCK_PROJECT_NAME;
        content.projectVersion = PROJECT_VERSION_URL;
        content.projectVersionName = PROJECT_VERSION_NAME;

        final Optional<String> projectName = Optional.of(BLACKDUCK_PROJECT_NAME);
        final Optional<String> projectVersionName = Optional.of(PROJECT_VERSION_NAME);
        final Optional<String> projectVersionUri = Optional.of(PROJECT_VERSION_URL);

        final Optional<String> componentName = Optional.of(COMPONENT_NAME);
        final Optional<String> componentVersionName = Optional.of(COMPONENT_VERSION_NAME);
        final Optional<String> componentVersionUri = Optional.of(COMPONENT_VERSION_URL);
        final Optional<String> componentVersionOriginId = Optional.of("compVerOriginId");
        final Optional<String> componentVersionOriginName = Optional.of("compVerOriginName");
        final Optional<String> policyName = Optional.of(policyInfo.policyName);
        final Optional<String> policyUri = Optional.of(policyInfo.policy);

        final NotificationContentDetail detail = NotificationContentDetail.createDetail(NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, projectName, projectVersionName, projectVersionUri, componentName, Optional.empty(),
                componentVersionName, componentVersionUri, policyName, policyUri, componentVersionOriginName, Optional.empty(), componentVersionOriginId, Optional.of(BOM_COMPONENT_URI));

        return new NotificationDetailResult(content, "application/json", createdAt, NotificationType.RULE_VIOLATION, NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, Optional.empty(), Arrays.asList(detail));
    }

    private NotificationDetailResult createRuleViolationClearedNotif(final HubBucket mockBlackDuckBucket, final Date createdAt) throws URISyntaxException, IntegrationException {
        final PolicyRuleViewV2 policyRule = createPolicyRuleV2(createdAt, POLICY_CLEARED_EXPECTED_DESCRIPTION);
        Mockito.when(mockBlackDuckBucket.get(mockUriSingleResponsePolicyRuleViewV2())).thenReturn(policyRule);

        final PolicyInfo policyInfo = createPolicyInfo();
        final ComponentVersionStatus componentVersionStatus = createComponentVersionStatus();

        final RuleViolationClearedNotificationContent content = new RuleViolationClearedNotificationContent();
        content.componentVersionStatuses = Arrays.asList(componentVersionStatus);
        content.componentVersionsCleared = content.componentVersionStatuses.size();
        content.policyInfos = Arrays.asList(policyInfo);
        content.projectName = BLACKDUCK_PROJECT_NAME;
        content.projectVersion = PROJECT_VERSION_URL;
        content.projectVersionName = PROJECT_VERSION_NAME;

        final Optional<String> projectName = Optional.of(BLACKDUCK_PROJECT_NAME);
        final Optional<String> projectVersionName = Optional.of(PROJECT_VERSION_NAME);
        final Optional<String> projectVersionUri = Optional.of(PROJECT_VERSION_URL);

        final Optional<String> componentName = Optional.of(COMPONENT_NAME);
        final Optional<String> componentVersionName = Optional.of(COMPONENT_VERSION_NAME);
        final Optional<String> componentVersionUri = Optional.of(COMPONENT_VERSION_URL);
        final Optional<String> componentVersionOriginId = Optional.of("compVerOriginId");
        final Optional<String> componentVersionOriginName = Optional.of("compVerOriginName");
        final Optional<String> policyName = Optional.of(policyInfo.policyName);
        final Optional<String> policyUri = Optional.of(policyInfo.policy);

        final NotificationContentDetail detail = NotificationContentDetail.createDetail(NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, projectName, projectVersionName, projectVersionUri, componentName, Optional.empty(),
                componentVersionName, componentVersionUri, policyName, policyUri, componentVersionOriginName, Optional.empty(), componentVersionOriginId, Optional.of(BOM_COMPONENT_URI));

        return new NotificationDetailResult(content, "application/json", createdAt, NotificationType.RULE_VIOLATION_CLEARED, NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, Optional.empty(), Arrays.asList(detail));
    }

    private NotificationDetailResult createPolicyOverrideNotif(final HubBucket mockBlackDuckBucket, final Date createdAt) throws URISyntaxException, IntegrationException {
        final PolicyRuleViewV2 policyRule = createPolicyRuleV2(createdAt, POLICY_OVERRIDE_EXPECTED_DESCRIPTION);
        Mockito.when(mockBlackDuckBucket.get(mockUriSingleResponsePolicyRuleViewV2())).thenReturn(policyRule);

        final PolicyInfo policyInfo = createPolicyInfo();

        final PolicyOverrideNotificationContent content = new PolicyOverrideNotificationContent();
        content.bomComponentVersionPolicyStatus = "???";
        content.component = COMPONENT_URL;
        content.componentName = COMPONENT_NAME;
        content.componentVersion = COMPONENT_VERSION_URL;
        content.componentVersionName = COMPONENT_VERSION_NAME;
        content.firstName = OVERRIDER_FIRST_NAME;
        content.lastName = OVERRIDER_LAST_NAME;
        content.policies = Arrays.asList(policyInfo.policyName);
        content.policyInfos = Arrays.asList(policyInfo);
        content.projectName = BLACKDUCK_PROJECT_NAME;
        content.projectVersion = PROJECT_VERSION_URL;
        content.projectVersionName = PROJECT_VERSION_NAME;

        final Optional<String> projectName = Optional.of(BLACKDUCK_PROJECT_NAME);
        final Optional<String> projectVersionName = Optional.of(PROJECT_VERSION_NAME);
        final Optional<String> projectVersionUri = Optional.of(PROJECT_VERSION_URL);

        final Optional<String> componentName = Optional.of(COMPONENT_NAME);
        final Optional<String> componentVersionName = Optional.of(COMPONENT_VERSION_NAME);
        final Optional<String> componentVersionUri = Optional.of(COMPONENT_VERSION_URL);
        final Optional<String> policyName = Optional.of(policyInfo.policyName);
        final Optional<String> policyUri = Optional.of(policyInfo.policy);

        final NotificationContentDetail detail = NotificationContentDetail.createDetail(NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, projectName, projectVersionName, projectVersionUri, componentName, Optional.empty(),
                componentVersionName, componentVersionUri, policyName, policyUri, Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(BOM_COMPONENT_URI));

        return new NotificationDetailResult(content, "application/json", createdAt, NotificationType.POLICY_OVERRIDE, NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, Optional.empty(), Arrays.asList(detail));
    }

    private NotificationDetailResult createBomEditNotif(final Date createdAt) {
        final BomEditContent content = new BomEditContent();
        content.bomComponent = BOM_COMPONENT_URI;

        final NotificationContentDetail detail = NotificationContentDetail.createDetail(NotificationContentDetail.CONTENT_KEY_GROUP_BOM_EDIT, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(BOM_COMPONENT_URI));

        return new NotificationDetailResult(content, "application/json", createdAt, NotificationType.BOM_EDIT, NotificationContentDetail.CONTENT_KEY_GROUP_BOM_EDIT, Optional.empty(), Arrays.asList(detail));
    }

    private static BlackDuckProject createBlackDuckProject() {
        final BlackDuckProject blackDuckProject = new BlackDuckProject();
        blackDuckProject.setProjectName(BLACKDUCK_PROJECT_NAME);
        blackDuckProject.setProjectUrl(BLACKDUCK_PROJECT_URL);
        return blackDuckProject;
    }

    private static JiraProject createJiraProject() {
        final JiraProject jiraProject = new JiraProject();
        jiraProject.setProjectName(JIRA_PROJECT_NAME);
        jiraProject.setProjectId(JIRA_PROJECT_ID);
        jiraProject.setAssigneeUserId(ASSIGNEE_USER_ID);
        return jiraProject;
    }

    private static BlackDuckJiraFieldCopyConfigSerializable createFieldCopyMappings() {
        final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig = new BlackDuckJiraFieldCopyConfigSerializable();
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

    private static ProjectVersionView createProjectVersionView() {
        final ProjectVersionView projectVersion = new ProjectVersionView();
        projectVersion._meta = new ResourceMetadata();
        projectVersion._meta.href = PROJECT_VERSION_URL;
        projectVersion.nickname = "???";
        projectVersion.phase = ProjectVersionPhaseType.PLANNING;
        projectVersion.releaseComments = "???";
        projectVersion.releasedOn = new Date();
        projectVersion.source = OriginSourceType.KB;
        projectVersion.versionName = PROJECT_VERSION_NAME;
        return projectVersion;
    }

    private static ComponentView createComponentView() {
        final ComponentView component = new ComponentView();
        component.description = "???";
        component.name = COMPONENT_NAME;
        component.source = OriginSourceType.KB;
        return component;
    }

    private static ComponentVersionView createComponentVersionView() {
        final ComponentVersionView componentVersion = new ComponentVersionView();
        componentVersion.license = new ComplexLicenseView();
        componentVersion.releasedOn = new Date();
        componentVersion.source = OriginSourceType.KB;
        componentVersion.versionName = COMPONENT_VERSION_NAME;
        return componentVersion;
    }

    private static VersionBomComponentView createVersionBomComponentView() {
        final VersionBomComponentView versionBomComponent = new VersionBomComponentView();
        versionBomComponent._meta = new ResourceMetadata();
        versionBomComponent._meta.href = BOM_COMPONENT_URI;
        versionBomComponent.componentName = COMPONENT_NAME;
        versionBomComponent.component = COMPONENT_URL;
        versionBomComponent.componentVersionName = COMPONENT_VERSION_NAME;
        versionBomComponent.componentVersion = COMPONENT_VERSION_URL;
        versionBomComponent.origins = Collections.emptyList();
        versionBomComponent.licenses = Arrays.asList(new VersionBomLicenseView());
        versionBomComponent.securityRiskProfile = createSecurityRiskProfile();

        return versionBomComponent;
    }

    private static RiskProfileView createSecurityRiskProfile() {
        final RiskProfileView riskProfile = new RiskProfileView();

        final RiskCountView riskCount = new RiskCountView();
        riskCount.count = 1;
        riskCount.countType = RiskCountType.HIGH;
        riskProfile.counts = Arrays.asList(riskCount);

        return riskProfile;
    }

    private ComponentVersionStatus createComponentVersionStatus() {
        final ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
        componentVersionStatus.bomComponentVersionPolicyStatus = PolicySummaryStatusType.IN_VIOLATION.name();
        componentVersionStatus.component = COMPONENT_URL;
        componentVersionStatus.componentIssueLink = "???";
        componentVersionStatus.componentName = COMPONENT_NAME;
        componentVersionStatus.componentVersion = COMPONENT_VERSION_URL;
        componentVersionStatus.componentVersionName = COMPONENT_VERSION_NAME;
        componentVersionStatus.policies = Arrays.asList(RULE_URL);
        componentVersionStatus.bomComponent = BOM_COMPONENT_URI;
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
        policyRule.createdBy = "Shmario";
        policyRule.createdByUser = "Bear";
        policyRule.description = description;
        policyRule.enabled = Boolean.TRUE;
        policyRule.expression = new PolicyRuleExpressionSetView();
        policyRule.name = RULE_NAME;
        policyRule.overridable = Boolean.TRUE;
        policyRule.severity = "Who Cares?";
        policyRule.updatedAt = createdAt;
        policyRule.updatedBy = "Shmario";
        policyRule.updatedByUser = "Bear";
        return policyRule;
    }

    private UriSingleResponse<PolicyRuleViewV2> mockUriSingleResponsePolicyRuleViewV2() {
        return new UriSingleResponse<>(RULE_URL, PolicyRuleViewV2.class);
    }
}
