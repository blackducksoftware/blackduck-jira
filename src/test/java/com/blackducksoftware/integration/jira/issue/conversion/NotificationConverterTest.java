/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
 * https://www.synopsys.com/
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
package com.blackducksoftware.integration.jira.issue.conversion;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.blackducksoftware.integration.jira.blackduck.BlackDuckDataHelper;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.data.accessor.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.data.accessor.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.issue.conversion.output.BlackDuckIssueAction;
import com.blackducksoftware.integration.jira.issue.conversion.output.OldIssueProperties;
import com.blackducksoftware.integration.jira.issue.handler.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.issue.handler.DataFormatHelper;
import com.blackducksoftware.integration.jira.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.issue.model.TicketCriteriaConfigModel;
import com.blackducksoftware.integration.jira.mocks.ApplicationUserMock;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.UserManagerMock;
import com.blackducksoftware.integration.jira.web.JiraServices;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.web.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.web.model.JiraProject;
import com.blackducksoftware.integration.jira.web.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.workflow.notification.NotificationContentDetail;
import com.blackducksoftware.integration.jira.workflow.notification.NotificationDetailResult;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionSetView;
import com.synopsys.integration.blackduck.api.generated.component.PolicyRuleExpressionView;
import com.synopsys.integration.blackduck.api.generated.component.ResourceLink;
import com.synopsys.integration.blackduck.api.generated.component.ResourceMetadata;
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
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.manual.component.AffectedProjectVersion;
import com.synopsys.integration.blackduck.api.manual.component.BomEditNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.PolicyOverrideNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationClearedNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.VulnerabilityNotificationContent;
import com.synopsys.integration.blackduck.api.manual.component.VulnerabilitySourceQualifiedId;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.rest.CredentialsBlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.bucket.BlackDuckBucket;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class NotificationConverterTest {
    private static final Logger logger = LoggerFactory.getLogger(NotificationConverterTest.class);

    private static final long JIRA_ISSUE_ID = 456L;
    private static final String OVERRIDER_LAST_NAME = "lastName";
    private static final String OVERRIDER_FIRST_NAME = "firstName";
    private static final String RULE_URL = "http://localhost:8080/api/rules/ruleId";
    private static final String COMPONENT_NAME = "componentName";
    private static final String COMPONENT_VERSION_NAME = "componentVersion";
    private static final String VULNERABLE_COMPONENTS_URL = "http://localhost:8080/api/projects/x/versions/y/vulnerable-components";
    private static final String VULNERABLE_COMPONENTS_QUERY_PARAMS = "?q=componentName:" + COMPONENT_NAME;
    private static final String RULE_NAME = "Test Rule";
    private static final String POLICY_EXPECTED_PROPERTY_KEY = "t=p|jp=123|hpv=-32224582|hc=|hcv=1816144506|hr=1736320804";
    private static final String POLICY_CLEARED_EXPECTED_COMMENT_IF_EXISTS = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_CLEARED_COMMENT;
    private static final String POLICY_CLEARED_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_CLEARED_COMMENT;
    private static final String POLICY_VIOLATION_EXPECTED_DESCRIPTION = "Black Duck has detected a policy violation.  \n\n";
    private static final String POLICY_CLEARED_EXPECTED_DESCRIPTION = POLICY_VIOLATION_EXPECTED_DESCRIPTION;
    private static final String POLICY_CLEARED_EXPECTED_SUMMARY = "Policy Violation: Project 'hubProjectName' / 'projectVersionName', Component '" + COMPONENT_NAME + "' / '" + COMPONENT_VERSION_NAME + "', Rule 'Test Rule'";
    private static final String POLICY_CLEARED_EXPECTED_REOPEN_COMMENT = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_REOPEN;
    private static final String POLICY_CLEARED_EXPECTED_RESOLVE_COMMENT = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_CLEARED_RESOLVE;
    private static final String POLICY_OVERRIDE_EXPECTED_COMMENT_IF_EXISTS = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_OVERRIDDEN_COMMENT;
    private static final String POLICY_OVERRIDE_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_OVERRIDDEN_COMMENT;
    private static final String POLICY_OVERRIDE_EXPECTED_DESCRIPTION = POLICY_VIOLATION_EXPECTED_DESCRIPTION;
    private static final String POLICY_OVERRIDE_EXPECTED_SUMMARY = "Policy Violation: Project 'hubProjectName' / 'projectVersionName', Component '" + COMPONENT_NAME + "' / '" + COMPONENT_VERSION_NAME + "', Rule 'Test Rule'";
    private static final String POLICY_OVERRIDE_EXPECTED_REOPEN_COMMENT = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_REOPEN;
    private static final String POLICY_OVERRIDE_EXPECTED_RESOLVE_COMMENT = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_RESOLVE;
    private static final String POLICY_VIOLATION_EXPECTED_RESOLVE_COMMENT = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_RESOLVE;
    private static final String POLICY_VIOLATION_EXPECTED_REOPEN_COMMENT = BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_REOPEN;
    private static final String POLICY_VIOLATION_EXPECTED_SUMMARY = "Policy Violation: Project 'hubProjectName' / 'projectVersionName', Component '" + COMPONENT_NAME + "' / '" + COMPONENT_VERSION_NAME + "', Rule '" + RULE_NAME + "'";
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
    private static final String PROJECT_VERSION_NAME = "projectVersionName";
    private static final String PROJECT_VERSION_URL = "http://localhost:8080/api/projects/projectId/versions/versionId";
    private static final String PROJECT_VERSION_URL_COMPONENT_QUERY = PROJECT_VERSION_URL + "?q=componentName:" + COMPONENT_NAME;
    private static final String COMPONENT_VERSION_URL = "http://localhost:8080/api/components/componentId/versions/versionId";
    private static final String COMPONENT_URL = "http://localhost:8080/api/components/componentId";
    private static final String BOM_COMPONENT_URI = "http://localhost:8080/api/projects/projectId/versions/versionId/components/componentId";
    private static final String BOM_EDIT_COMMENT_VULN = "(Black Duck plugin auto-generated comment)\nVulnerabilities _added_: None\nVulnerabilities _updated_: None\nVulnerabilities _deleted_: None\n";
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
                                                            + "Vulnerabilities _updated_: None\n" + "Vulnerabilities _deleted_: None\n" + "\nTotal Vulnerabilities:\n"
                                                            + "High: 1";
    private final static String VULN_EXPECTED_COMMENT_IF_EXISTS = VULN_EXPECTED_COMMENT;
    private final static String VULN_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE = VULN_EXPECTED_COMMENT;
    private final static String VULN_EXPECTED_DESCRIPTION =
        "Black Duck has detected vulnerabilities. For details, see the comments below, or the project's [vulnerabilities|" + VULNERABLE_COMPONENTS_URL + VULNERABLE_COMPONENTS_QUERY_PARAMS + "] in Black Duck.  \n\n";
    private final static String VULN_EXPECTED_SUMMARY = "Vulnerability: Project " + "'hubProjectName' / 'projectVersionName', Component 'componentName' / 'componentVersion'";

    private static final Gson gson = new Gson();
    private static JiraServices jiraServices;
    private static PluginErrorAccessor pluginErrorAccessor;
    private static JiraUserContext jiraContext;
    private static BlackDuckService mockBlackDuckSerivce;
    private static BlackDuckBucket mockBlackDuckBucket;
    private static BlackDuckProjectMappings projectMappingObject;
    private static BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig;
    private static BlackDuckDataHelper blackDuckDataHelper;
    private static DataFormatHelper dataFormatHelper;

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
        mapping.setBlackDuckProjectName(BLACKDUCK_PROJECT_NAME);
        final JiraProject jiraProject = createJiraProject();
        mapping.setJiraProject(jiraProject);
        mappings.add(mapping);

        Mockito.when(jiraServices.getJiraProject(JIRA_PROJECT_ID)).thenReturn(jiraProject);

        // Jira Settings Service
        final JiraSettingsAccessor jiraSettingsAccessor = new JiraSettingsAccessor(new PluginSettingsMock());
        pluginErrorAccessor = new PluginErrorAccessor(jiraSettingsAccessor);

        // Jira Context
        final ApplicationUserMock jiraAdminUser = new ApplicationUserMock();
        jiraAdminUser.setName(JIRA_ADMIN_USERNAME);
        jiraAdminUser.setUsername(JIRA_ADMIN_USERNAME);
        jiraAdminUser.setKey(JIRA_ADMIN_USER_KEY);
        final ApplicationUserMock jiraIssueCreatorUser = new ApplicationUserMock();
        jiraIssueCreatorUser.setName(JIRA_ISSUE_CREATOR_USERNAME);
        jiraIssueCreatorUser.setUsername(JIRA_ISSUE_CREATOR_USERNAME);
        jiraIssueCreatorUser.setKey(JIRA_ISSUE_CREATOR_USER_KEY);
        jiraContext = new JiraUserContext(jiraAdminUser, jiraIssueCreatorUser);

        final UserManagerMock userManagerMock = new UserManagerMock();
        userManagerMock.setMockApplicationUser(jiraIssueCreatorUser);
        Mockito.when(jiraServices.getUserManager()).thenReturn(userManagerMock);

        // Black Duck Services
        mockBlackDuckSerivce = Mockito.mock(BlackDuckService.class);
        mockHubServiceResponses(mockBlackDuckSerivce);
        mockBlackDuckBucket = Mockito.mock(BlackDuckBucket.class);
        mockBlackDuckBucketResponses(mockBlackDuckBucket);

        // Project Mappings
        projectMappingObject = new BlackDuckProjectMappings(jiraServices, mappings);
        fieldCopyConfig = createFieldCopyMappings();

        // EventData Format Helper
        blackDuckDataHelper = new BlackDuckDataHelper(logger, mockBlackDuckSerivce, mockBlackDuckBucket);
        Mockito.when(blackDuckDataHelper.getResponseNullable(COMPONENT_VERSION_URL, ComponentVersionView.class)).thenReturn(createComponentVersionView());
        dataFormatHelper = new DataFormatHelper(blackDuckDataHelper);
    }

    @AfterClass
    public static void tearDownAfterClass() {
    }

    private static void mockHubServiceResponses(final BlackDuckService mockBlackDuckService) throws IntegrationException {
        final String blackDuckBaseUrl = "https://localhost:8080";

        final BlackDuckHttpClient mockRestConnection = new CredentialsBlackDuckHttpClient(new Slf4jIntLogger(logger), 120, true, ProxyInfo.NO_PROXY_INFO, blackDuckBaseUrl, null, Credentials.NO_CREDENTIALS);
        Mockito.when(mockBlackDuckService.getBlackDuckHttpClient()).thenReturn(mockRestConnection);
        Mockito.when(mockBlackDuckService.getBlackDuckBaseUrl()).thenReturn(blackDuckBaseUrl);

        final VersionRiskProfileView riskProfile = new VersionRiskProfileView();
        riskProfile.setBomLastUpdatedAt(new Date());
        Mockito.when(mockBlackDuckService.getResponse(Mockito.any(), Mockito.eq(ProjectVersionView.RISKPROFILE_LINK_RESPONSE))).thenReturn(Optional.of(riskProfile));

        final ProjectView project = new ProjectView();
        project.setProjectOwner("Shmario Bear");
        project.setName(BLACKDUCK_PROJECT_NAME);
        Mockito.when(mockBlackDuckService.getResponse(Mockito.any(), Mockito.eq(ProjectVersionView.PROJECT_LINK_RESPONSE))).thenReturn(Optional.of(project));
        Mockito.when(mockBlackDuckService.getResponse(PROJECT_VERSION_URL, ProjectVersionView.class)).thenReturn(createProjectVersionView());

        Mockito.when(mockBlackDuckService.getAllResponses(Mockito.any(VersionBomComponentView.class), Mockito.eq(VersionBomComponentView.POLICY_RULES_LINK_RESPONSE)))
            .thenReturn(Arrays.asList(createPolicyRule(new Date(), POLICY_VIOLATION_EXPECTED_DESCRIPTION)));
    }

    private static void mockBlackDuckBucketResponses(final BlackDuckBucket mockBlackDuckBucket) {
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

    private static JiraProject createJiraProject() {
        final JiraProject jiraProject = new JiraProject();
        jiraProject.setProjectName(JIRA_PROJECT_NAME);
        jiraProject.setProjectId(JIRA_PROJECT_ID);
        jiraProject.setAssigneeUserId(ASSIGNEE_USER_ID);
        jiraProject.setConfiguredForVulnerabilities(true);
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
        final ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(PROJECT_VERSION_URL);
        final ResourceLink componentLink = new ResourceLink();
        componentLink.setHref(COMPONENT_URL);
        componentLink.setRel(ProjectVersionView.COMPONENTS_LINK);
        final ResourceLink vulnerableLink = new ResourceLink();
        vulnerableLink.setHref(VULNERABLE_COMPONENTS_URL);
        vulnerableLink.setRel(ProjectVersionView.VULNERABLE_COMPONENTS_LINK);
        resourceMetadata.setLinks(Arrays.asList(componentLink, vulnerableLink));
        projectVersion.setMeta(resourceMetadata);
        projectVersion.setNickname("???");
        projectVersion.setPhase(ProjectVersionPhaseType.PLANNING);
        projectVersion.setReleaseComments("???");
        projectVersion.setReleasedOn(new Date());
        projectVersion.setSource(OriginSourceType.KB);
        projectVersion.setVersionName(PROJECT_VERSION_NAME);
        return projectVersion;
    }

    private static ComponentView createComponentView() {
        final ComponentView component = new ComponentView();
        component.setDescription("???");
        component.setName(COMPONENT_NAME);
        component.setSource(OriginSourceType.KB);
        return component;
    }

    private static ComponentVersionView createComponentVersionView() {
        final ComponentVersionView componentVersion = new ComponentVersionView();
        componentVersion.setLicense(new ComplexLicenseView());
        componentVersion.setReleasedOn(new Date());
        componentVersion.setSource(OriginSourceType.KB);
        componentVersion.setVersionName(COMPONENT_VERSION_NAME);
        componentVersion.setMeta(new ResourceMetadata());
        componentVersion.getMeta().setHref(COMPONENT_VERSION_URL);
        return componentVersion;
    }

    private static VersionBomComponentView createVersionBomComponentView() {
        final VersionBomComponentView versionBomComponent = new VersionBomComponentView();
        final ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(BOM_COMPONENT_URI);
        versionBomComponent.setMeta(resourceMetadata);
        versionBomComponent.setComponentName(COMPONENT_NAME);
        versionBomComponent.setComponent(COMPONENT_URL);
        versionBomComponent.setComponentVersionName(COMPONENT_VERSION_NAME);
        versionBomComponent.setComponentVersion(COMPONENT_VERSION_URL);
        versionBomComponent.setOrigins(Collections.emptyList());
        versionBomComponent.setLicenses(Arrays.asList(new VersionBomLicenseView()));
        versionBomComponent.setSecurityRiskProfile(createSecurityRiskProfile());
        versionBomComponent.setPolicyStatus(PolicySummaryStatusType.IN_VIOLATION);

        return versionBomComponent;
    }

    private static RiskProfileView createSecurityRiskProfile() {
        final RiskProfileView riskProfile = new RiskProfileView();

        final RiskCountView riskCount = new RiskCountView();
        riskCount.setCount(1);
        riskCount.setCountType(RiskCountType.HIGH);
        riskProfile.setCounts(Arrays.asList(riskCount));

        return riskProfile;
    }

    private static PolicyRuleView createPolicyRule(final Date createdAt, final String description) {
        final PolicyRuleView policyRule = new PolicyRuleView();
        final ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(RULE_URL);
        policyRule.setMeta(resourceMetadata);
        policyRule.setCreatedAt(createdAt);
        policyRule.setCreatedBy("Shmario");
        policyRule.setCreatedByUser("Bear");
        policyRule.setDescription(description);
        policyRule.setEnabled(Boolean.TRUE);
        policyRule.setExpression(new PolicyRuleExpressionSetView());
        policyRule.setName(RULE_NAME);
        policyRule.setOverridable(Boolean.TRUE);
        policyRule.setSeverity("Who Cares?");
        policyRule.setUpdatedAt(createdAt);
        policyRule.setUpdatedBy("Shmario");
        policyRule.setUpdatedByUser("Bear");
        policyRule.setExpression(createPolicyRuleExpressionSet());
        policyRule.setJsonElement(gson.toJsonTree(policyRule));
        return policyRule;
    }

    private static PolicyRuleExpressionSetView createPolicyRuleExpressionSet() {
        final PolicyRuleExpressionSetView policyRuleExpressionSetView = new PolicyRuleExpressionSetView();
        policyRuleExpressionSetView.setExpressions(Arrays.asList(createPolicyRuleExpression()));
        return policyRuleExpressionSetView;
    }

    private static PolicyRuleExpressionView createPolicyRuleExpression() {
        final PolicyRuleExpressionView policyRuleExpressionView = new PolicyRuleExpressionView();
        policyRuleExpressionView.setName("policy");
        return policyRuleExpressionView;
    }

    @Test
    public void testVulnerability() throws URISyntaxException, IntegrationException {
        test(NotificationType.VULNERABILITY, BlackDuckIssueAction.ADD_COMMENT, VULN_EXPECTED_COMMENT, VULN_EXPECTED_COMMENT_IF_EXISTS, VULN_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE, VULN_EXPECTED_DESCRIPTION, VULN_EXPECTED_SUMMARY,
            VULNERABILITY_ISSUE_TYPE_ID, VULN_EXPECTED_REOPEN_COMMENT, VULN_EXPECTED_RESOLVED_COMMENT, EXPECTED_EVENT_COUNT);
    }

    @Test
    public void testBomEdit() throws URISyntaxException, IntegrationException {
        test(NotificationType.BOM_EDIT, BlackDuckIssueAction.UPDATE_OR_OPEN, BOM_EDIT_COMMENT_VULN, BOM_EDIT_COMMENT_VULN, BOM_EDIT_COMMENT_VULN, VULN_EXPECTED_DESCRIPTION, VULN_EXPECTED_SUMMARY,
            VULNERABILITY_ISSUE_TYPE_ID, VULN_EXPECTED_REOPEN_COMMENT, VULN_EXPECTED_RESOLVED_COMMENT, 2);
    }

    @Test
    public void testRuleViolation() throws URISyntaxException, IntegrationException {
        test(NotificationType.RULE_VIOLATION, BlackDuckIssueAction.OPEN, null, POLICY_EXPECTED_COMMENT_IF_EXISTS, POLICY_VIOLATION_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE, POLICY_VIOLATION_EXPECTED_DESCRIPTION,
            POLICY_VIOLATION_EXPECTED_SUMMARY, POLICY_ISSUE_TYPE_ID, POLICY_VIOLATION_EXPECTED_REOPEN_COMMENT, POLICY_VIOLATION_EXPECTED_RESOLVE_COMMENT, EXPECTED_EVENT_COUNT);
    }

    @Test
    public void testPolicyOverride() throws URISyntaxException, IntegrationException {
        test(NotificationType.POLICY_OVERRIDE, BlackDuckIssueAction.RESOLVE, null, POLICY_OVERRIDE_EXPECTED_COMMENT_IF_EXISTS, POLICY_OVERRIDE_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE, POLICY_OVERRIDE_EXPECTED_DESCRIPTION,
            POLICY_OVERRIDE_EXPECTED_SUMMARY, POLICY_ISSUE_TYPE_ID, POLICY_OVERRIDE_EXPECTED_REOPEN_COMMENT, POLICY_OVERRIDE_EXPECTED_RESOLVE_COMMENT, EXPECTED_EVENT_COUNT);
    }

    @Test
    public void testRuleViolationCleared() throws URISyntaxException, IntegrationException {
        test(NotificationType.RULE_VIOLATION_CLEARED, BlackDuckIssueAction.RESOLVE, null, POLICY_CLEARED_EXPECTED_COMMENT_IF_EXISTS, POLICY_CLEARED_EXPECTED_COMMENT_IN_LIEU_OF_STATE_CHANGE, POLICY_CLEARED_EXPECTED_DESCRIPTION,
            POLICY_CLEARED_EXPECTED_SUMMARY, POLICY_ISSUE_TYPE_ID, POLICY_CLEARED_EXPECTED_REOPEN_COMMENT, POLICY_CLEARED_EXPECTED_RESOLVE_COMMENT, EXPECTED_EVENT_COUNT);
    }

    private void test(final NotificationType notifType, final BlackDuckIssueAction expectedBlackDuckIssueAction, final String expectedComment, final String expectedCommentIfExists, final String expectedCommentInLieuOfStateChange,
        final String expectedDescription, final String expectedSummary, final String issueTypeId, final String expectedReOpenComment, final String expectedResolveComment, final int expectedCount)
        throws URISyntaxException, IntegrationException {

        final Date startDate = new Date();
        final NotificationDetailResult notificationDetailResults = createNotification(mockBlackDuckBucket, notifType, startDate);

        final BomNotificationToIssueModelConverter notificationConverter = new BomNotificationToIssueModelConverter(jiraServices, jiraContext, pluginErrorAccessor, projectMappingObject, fieldCopyConfig, dataFormatHelper,
            Arrays.asList(RULE_URL), blackDuckDataHelper, new TicketCriteriaConfigModel("{}", true, true));

        final Collection<BlackDuckIssueModel> issueModels = notificationConverter.convertToModel(notificationDetailResults, startDate);
        assertEquals(expectedCount, issueModels.size());
        for (final BlackDuckIssueModel model : issueModels) {
            verifyGeneratedModels(model, expectedBlackDuckIssueAction);
            if (expectedCount == 1) {
                assertEquals(expectedDescription, model.getJiraIssueFieldTemplate().getIssueDescription());
                assertEquals(expectedSummary, model.getJiraIssueFieldTemplate().getSummary());
                assertEquals(issueTypeId, model.getJiraIssueFieldTemplate().getIssueTypeId());
                verifyGeneratedComments(model, expectedComment, expectedCommentIfExists, expectedReOpenComment, expectedResolveComment, expectedCommentInLieuOfStateChange);
            }
        }
    }

    private NotificationDetailResult createNotification(final BlackDuckBucket mockBlackDuckBucket, final NotificationType notifType, final Date notifDate) throws URISyntaxException, IntegrationException {
        if (NotificationType.VULNERABILITY.equals(notifType)) {
            return createVulnerabilityNotif(notifDate);
        } else if (NotificationType.RULE_VIOLATION.equals(notifType)) {
            return createRuleViolationNotif(mockBlackDuckBucket, notifDate);
        } else if (NotificationType.POLICY_OVERRIDE.equals(notifType)) {
            return createPolicyOverrideNotif(mockBlackDuckBucket, notifDate);
        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(notifType)) {
            return createRuleViolationClearedNotif(mockBlackDuckBucket, notifDate);
        } else if (NotificationType.BOM_EDIT.equals(notifType)) {
            return createVulnerabilityBomEditNotif(notifDate);
        } else {
            throw new IllegalArgumentException("Unrecognized notification type");
        }
    }

    private void verifyGeneratedModels(final BlackDuckIssueModel model, final BlackDuckIssueAction expectedHubEventAction) {
        assertEquals(BOM_COMPONENT_URI, model.getBomComponentUri());
        assertEquals(expectedHubEventAction, model.getIssueAction());
        assertEquals(ASSIGNEE_USER_ID, model.getJiraIssueFieldTemplate().getAssigneeId());

        assertEquals(Long.valueOf(JIRA_PROJECT_ID), model.getJiraIssueFieldTemplate().getProjectId());
        assertEquals(JIRA_PROJECT_NAME, model.getJiraIssueFieldTemplate().getProjectName());
        assertEquals(JIRA_ISSUE_CREATOR_USERNAME, model.getJiraIssueFieldTemplate().getIssueCreator().getUsername());
        final Set<ProjectFieldCopyMapping> fieldMappings = model.getProjectFieldCopyMappings();
        assertEquals(1, fieldMappings.size());
        final Iterator<ProjectFieldCopyMapping> iter = fieldMappings.iterator();
        final ProjectFieldCopyMapping actualProjectFieldCopyMapping = iter.next();
        assertEquals(WILDCARD_STRING, actualProjectFieldCopyMapping.getHubProjectName());
        assertEquals(WILDCARD_STRING, actualProjectFieldCopyMapping.getJiraProjectName());
        assertEquals(SOURCE_FIELD_ID, actualProjectFieldCopyMapping.getSourceFieldId());
        assertEquals(SOURCE_FIELD_NAME, actualProjectFieldCopyMapping.getSourceFieldName());
        assertEquals(TARGET_FIELD_ID, actualProjectFieldCopyMapping.getTargetFieldId());
        assertEquals(TARGET_FIELD_NAME, actualProjectFieldCopyMapping.getTargetFieldName());

        model.setJiraIssueId(JIRA_ISSUE_ID);
        final OldIssueProperties issueProperties = OldIssueProperties.fromBlackDuckIssueWrapper(model);
        assertEquals(BLACKDUCK_PROJECT_NAME, issueProperties.getProjectName());
        assertEquals(PROJECT_VERSION_NAME, issueProperties.getProjectVersion());
        assertEquals(COMPONENT_NAME, issueProperties.getComponentName());
        assertEquals(COMPONENT_VERSION_NAME, issueProperties.getComponentVersion());
        assertEquals(Long.valueOf(JIRA_ISSUE_ID), issueProperties.getJiraIssueId());
    }

    private void verifyGeneratedComments(final BlackDuckIssueModel model,
        final String expectedComment, final String expectedCommentIfExists, final String expectedReOpenComment, final String expectedResolveComment, final String expectedCommentInLieuOfStateChange) {
        assertEquals(expectedComment, model.getJiraIssueComment());
        assertEquals(expectedCommentIfExists, model.getJiraIssueCommentForExistingIssue());
        assertEquals(expectedCommentInLieuOfStateChange, model.getJiraIssueCommentInLieuOfStateChange());
        assertEquals(expectedReOpenComment, model.getJiraIssueReOpenComment());
        assertEquals(expectedResolveComment, model.getJiraIssueResolveComment());
    }

    private NotificationDetailResult createVulnerabilityNotif(final Date createdAt) {
        final VulnerabilitySourceQualifiedId vuln = new VulnerabilitySourceQualifiedId();
        vuln.setSource(VULN_SOURCE);
        vuln.setVulnerabilityId(COMPONENT_VERSION_URL);
        final List<VulnerabilitySourceQualifiedId> addedVulnList = new ArrayList<>();
        final List<VulnerabilitySourceQualifiedId> updatedVulnList = new ArrayList<>();
        final List<VulnerabilitySourceQualifiedId> deletedVulnList = new ArrayList<>();
        addedVulnList.add(vuln);

        final VulnerabilityNotificationContent content = new VulnerabilityNotificationContent();
        content.setComponentName(COMPONENT_NAME);
        content.setComponentVersion(COMPONENT_VERSION_URL);
        content.setVersionName(COMPONENT_VERSION_NAME);
        content.setNewVulnerabilityIds(addedVulnList);
        content.setNewVulnerabilityCount(addedVulnList.size());
        content.setUpdatedVulnerabilityIds(updatedVulnList);
        content.setUpdatedVulnerabilityCount(updatedVulnList.size());
        content.setDeletedVulnerabilityIds(deletedVulnList);
        content.setDeletedVulnerabilityCount(deletedVulnList.size());

        final AffectedProjectVersion affected = new AffectedProjectVersion();
        affected.setProjectName(BLACKDUCK_PROJECT_NAME);
        affected.setProjectVersion(PROJECT_VERSION_URL);
        affected.setProjectVersionName(PROJECT_VERSION_NAME);
        affected.setBomComponent(BOM_COMPONENT_URI);
        content.setAffectedProjectVersions(Arrays.asList(affected));

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

    private NotificationDetailResult createRuleViolationNotif(final BlackDuckBucket mockBlackDuckBucket, final Date createdAt) {
        final PolicyRuleView policyRule = createPolicyRule(createdAt, POLICY_VIOLATION_EXPECTED_DESCRIPTION);
        Mockito.when(mockBlackDuckBucket.get(mockUriSingleResponsePolicyRuleViewV2())).thenReturn(policyRule);

        final PolicyInfo policyInfo = createPolicyInfo();
        final ComponentVersionStatus componentVersionStatus = createComponentVersionStatus();

        final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
        content.setComponentVersionStatuses(Arrays.asList(componentVersionStatus));
        content.setComponentVersionsInViolation(content.getComponentVersionStatuses().size());
        content.setPolicyInfos(Arrays.asList(policyInfo));
        content.setProjectName(BLACKDUCK_PROJECT_NAME);
        content.setProjectVersion(PROJECT_VERSION_URL);
        content.setProjectVersionName(PROJECT_VERSION_NAME);

        final Optional<String> projectName = Optional.of(BLACKDUCK_PROJECT_NAME);
        final Optional<String> projectVersionName = Optional.of(PROJECT_VERSION_NAME);
        final Optional<String> projectVersionUri = Optional.of(PROJECT_VERSION_URL);

        final Optional<String> componentName = Optional.of(COMPONENT_NAME);
        final Optional<String> componentVersionName = Optional.of(COMPONENT_VERSION_NAME);
        final Optional<String> componentVersionUri = Optional.of(COMPONENT_VERSION_URL);
        final Optional<String> componentVersionOriginId = Optional.of("compVerOriginId");
        final Optional<String> componentVersionOriginName = Optional.of("compVerOriginName");
        final Optional<String> policyName = Optional.of(policyInfo.getPolicyName());
        final Optional<String> policyUri = Optional.of(policyInfo.getPolicy());

        final NotificationContentDetail detail = NotificationContentDetail.createDetail(NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, projectName, projectVersionName, projectVersionUri, componentName, Optional.empty(),
            componentVersionName, componentVersionUri, policyName, policyUri, componentVersionOriginName, Optional.empty(), componentVersionOriginId, Optional.of(BOM_COMPONENT_URI));

        return new NotificationDetailResult(content, "application/json", createdAt, NotificationType.RULE_VIOLATION, NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, Optional.empty(), Arrays.asList(detail));
    }

    private NotificationDetailResult createRuleViolationClearedNotif(final BlackDuckBucket mockBlackDuckBucket, final Date createdAt) throws URISyntaxException, IntegrationException {
        final PolicyRuleView policyRule = createPolicyRule(createdAt, POLICY_CLEARED_EXPECTED_DESCRIPTION);
        Mockito.when(mockBlackDuckBucket.get(mockUriSingleResponsePolicyRuleViewV2())).thenReturn(policyRule);

        final PolicyInfo policyInfo = createPolicyInfo();
        final ComponentVersionStatus componentVersionStatus = createComponentVersionStatus();

        final RuleViolationClearedNotificationContent content = new RuleViolationClearedNotificationContent();
        content.setComponentVersionStatuses(Arrays.asList(componentVersionStatus));
        content.setComponentVersionsCleared(content.getComponentVersionStatuses().size());
        content.setPolicyInfos(Arrays.asList(policyInfo));
        content.setProjectName(BLACKDUCK_PROJECT_NAME);
        content.setProjectVersion(PROJECT_VERSION_URL);
        content.setProjectVersionName(PROJECT_VERSION_NAME);

        final Optional<String> projectName = Optional.of(BLACKDUCK_PROJECT_NAME);
        final Optional<String> projectVersionName = Optional.of(PROJECT_VERSION_NAME);
        final Optional<String> projectVersionUri = Optional.of(PROJECT_VERSION_URL);

        final Optional<String> componentName = Optional.of(COMPONENT_NAME);
        final Optional<String> componentVersionName = Optional.of(COMPONENT_VERSION_NAME);
        final Optional<String> componentVersionUri = Optional.of(COMPONENT_VERSION_URL);
        final Optional<String> componentVersionOriginId = Optional.of("compVerOriginId");
        final Optional<String> componentVersionOriginName = Optional.of("compVerOriginName");
        final Optional<String> policyName = Optional.of(policyInfo.getPolicyName());
        final Optional<String> policyUri = Optional.of(policyInfo.getPolicy());

        final NotificationContentDetail detail = NotificationContentDetail.createDetail(NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, projectName, projectVersionName, projectVersionUri, componentName, Optional.empty(),
            componentVersionName, componentVersionUri, policyName, policyUri, componentVersionOriginName, Optional.empty(), componentVersionOriginId, Optional.of(BOM_COMPONENT_URI));

        return new NotificationDetailResult(content, "application/json", createdAt, NotificationType.RULE_VIOLATION_CLEARED, NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, Optional.empty(), Arrays.asList(detail));
    }

    private NotificationDetailResult createPolicyOverrideNotif(final BlackDuckBucket mockBlackDuckBucket, final Date createdAt) throws URISyntaxException, IntegrationException {
        final PolicyRuleView policyRule = createPolicyRule(createdAt, POLICY_OVERRIDE_EXPECTED_DESCRIPTION);
        Mockito.when(mockBlackDuckBucket.get(mockUriSingleResponsePolicyRuleViewV2())).thenReturn(policyRule);

        final PolicyInfo policyInfo = createPolicyInfo();

        final PolicyOverrideNotificationContent content = new PolicyOverrideNotificationContent();
        content.setBomComponentVersionPolicyStatus("???");
        content.setComponent(COMPONENT_URL);
        content.setComponentName(COMPONENT_NAME);
        content.setComponentVersion(COMPONENT_VERSION_URL);
        content.setComponentVersionName(COMPONENT_VERSION_NAME);
        content.setFirstName(OVERRIDER_FIRST_NAME);
        content.setLastName(OVERRIDER_LAST_NAME);
        content.setPolicies(Arrays.asList(policyInfo.getPolicyName()));
        content.setPolicyInfos(Arrays.asList(policyInfo));
        content.setProjectName(BLACKDUCK_PROJECT_NAME);
        content.setProjectVersion(PROJECT_VERSION_URL);
        content.setProjectVersionName(PROJECT_VERSION_NAME);

        final Optional<String> projectName = Optional.of(BLACKDUCK_PROJECT_NAME);
        final Optional<String> projectVersionName = Optional.of(PROJECT_VERSION_NAME);
        final Optional<String> projectVersionUri = Optional.of(PROJECT_VERSION_URL);

        final Optional<String> componentName = Optional.of(COMPONENT_NAME);
        final Optional<String> componentVersionName = Optional.of(COMPONENT_VERSION_NAME);
        final Optional<String> componentVersionUri = Optional.of(COMPONENT_VERSION_URL);
        final Optional<String> policyName = Optional.of(policyInfo.getPolicyName());
        final Optional<String> policyUri = Optional.of(policyInfo.getPolicy());

        final NotificationContentDetail detail = NotificationContentDetail.createDetail(NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, projectName, projectVersionName, projectVersionUri, componentName, Optional.empty(),
            componentVersionName, componentVersionUri, policyName, policyUri, Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(BOM_COMPONENT_URI));

        return new NotificationDetailResult(content, "application/json", createdAt, NotificationType.POLICY_OVERRIDE, NotificationContentDetail.CONTENT_KEY_GROUP_POLICY, Optional.empty(), Arrays.asList(detail));
    }

    private NotificationDetailResult createVulnerabilityBomEditNotif(final Date createdAt) {
        final BomEditNotificationContent content = new BomEditNotificationContent();
        content.setBomComponent(BOM_COMPONENT_URI);

        final NotificationContentDetail detail = NotificationContentDetail.createDetail(NotificationContentDetail.CONTENT_KEY_GROUP_BOM_EDIT, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(BOM_COMPONENT_URI));

        return new NotificationDetailResult(content, "application/json", createdAt, NotificationType.BOM_EDIT, NotificationContentDetail.CONTENT_KEY_GROUP_BOM_EDIT, Optional.empty(), Arrays.asList(detail));
    }

    private ComponentVersionStatus createComponentVersionStatus() {
        final ComponentVersionStatus componentVersionStatus = new ComponentVersionStatus();
        componentVersionStatus.setBomComponentVersionPolicyStatus(PolicySummaryStatusType.IN_VIOLATION.name());
        componentVersionStatus.setComponent(COMPONENT_URL);
        componentVersionStatus.setComponentIssueLink("???");
        componentVersionStatus.setComponentName(COMPONENT_NAME);
        componentVersionStatus.setComponentVersion(COMPONENT_VERSION_URL);
        componentVersionStatus.setComponentVersionName(COMPONENT_VERSION_NAME);
        componentVersionStatus.setPolicies(Arrays.asList(RULE_URL));
        componentVersionStatus.setBomComponent(BOM_COMPONENT_URI);
        return componentVersionStatus;
    }

    private PolicyInfo createPolicyInfo() {
        final PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.setPolicyName(RULE_NAME);
        policyInfo.setPolicy(RULE_URL);
        return policyInfo;
    }

    private UriSingleResponse<PolicyRuleView> mockUriSingleResponsePolicyRuleViewV2() {
        return new UriSingleResponse<>(RULE_URL, PolicyRuleView.class);
    }
}
