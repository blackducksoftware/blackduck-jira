/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.jira.blackduck.BlackDuckDataHelper;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.dal.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.dal.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.dal.model.TicketCriteriaConfigModel;
import com.blackducksoftware.integration.jira.issue.handler.DataFormatHelper;
import com.blackducksoftware.integration.jira.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;
import com.blackducksoftware.integration.jira.mocks.UserManagerMock;
import com.blackducksoftware.integration.jira.web.JiraServices;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.workflow.notification.NotificationContentDetail;
import com.blackducksoftware.integration.jira.workflow.notification.NotificationDetailResult;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.api.manual.component.RuleViolationNotificationContent;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;

public class BomNotificationToIssueModelConverterTest {
    private static final String TEST_PROJECT = "test project";
    private static final String POLICY_RULE_LINK = "test policy";
    private static final String DIFFERENT_POLICY_RULE_LINK = "test policy";

    public static final Gson GSON = new Gson();

    @Test
    public void ifNoPolicyRulesSelectedNoPolicyIssuesCreatedTest() throws IntegrationException {
        final BlackDuckProjectMapping mapping = new BlackDuckProjectMapping();
        final JiraProject jiraProject = new JiraProject();
        mapping.setJiraProject(jiraProject);
        mapping.setBlackDuckProjectName(TEST_PROJECT);

        final PolicyInfo policyInfo = new PolicyInfo();
        policyInfo.setPolicy(DIFFERENT_POLICY_RULE_LINK);

        final RuleViolationNotificationContent content = new RuleViolationNotificationContent();
        content.setProjectName(TEST_PROJECT);
        content.setPolicyInfos(Collections.singletonList(policyInfo));

        final String notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_POLICY;
        final String bomComponentLink = "bom component";
        final NotificationContentDetail notificationContentDetail =
            NotificationContentDetail.createDetail(notificationGroup, Optional.of(TEST_PROJECT), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.of(DIFFERENT_POLICY_RULE_LINK), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(bomComponentLink));

        final NotificationDetailResult notificationDetailResult =
            new NotificationDetailResult(content, "application/json", new Date(), NotificationType.RULE_VIOLATION, notificationGroup, Optional.empty(), Collections.singletonList(notificationContentDetail));

        final BomNotificationToIssueModelConverter converter = createMockBomNotificationToIssueModelConverter(Collections.singleton(mapping), Collections.singletonList(DIFFERENT_POLICY_RULE_LINK));
        final Collection<BlackDuckIssueModel> resultingModels = converter.convertToModel(notificationDetailResult, new Date());
        assertEquals(0, resultingModels.size());
    }

    private BomNotificationToIssueModelConverter createMockBomNotificationToIssueModelConverter(final Set<BlackDuckProjectMapping> mappings, final List<String> linksOfRulesToMonitor) throws IntegrationException {
        return createMockBomNotificationToIssueModelConverter(mappings, linksOfRulesToMonitor, new BlackDuckJiraFieldCopyConfigSerializable());
    }

    private BomNotificationToIssueModelConverter createMockBomNotificationToIssueModelConverter(final Set<BlackDuckProjectMapping> mappings, final List<String> linksOfRulesToMonitor,
        final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig) throws IntegrationException {
        final BlackDuckJiraLogger blackDuckJiraLogger = new BlackDuckJiraLogger(Logger.getLogger(BomNotificationToIssueModelConverterTest.class));
        final PluginSettingsMock pluginSettingsMock = new PluginSettingsMock();

        final JiraServices jiraServices = createMockJiraServices();
        final JiraUserContext jiraUserContext = Mockito.mock(JiraUserContext.class);
        final PluginErrorAccessor pluginErrorAccessor = new PluginErrorAccessor(new JiraSettingsAccessor(pluginSettingsMock));
        final BlackDuckProjectMappings blackDuckProjectMappings = createMockBlackDuckProjectMappings(mappings);
        final BlackDuckDataHelper blackDuckDataHelper = createMockBlackDuckDataHelper();
        final DataFormatHelper dataFormatHelper = new DataFormatHelper(blackDuckDataHelper);
        final TicketCriteriaConfigModel ticketCriteriaConfig = new TicketCriteriaConfigModel(GSON.toJson(linksOfRulesToMonitor), true, true);

        return new BomNotificationToIssueModelConverter(jiraServices, jiraUserContext, pluginErrorAccessor, blackDuckProjectMappings, fieldCopyConfig, dataFormatHelper, linksOfRulesToMonitor, blackDuckDataHelper,
            blackDuckJiraLogger, ticketCriteriaConfig);
    }

    private JiraServices createMockJiraServices() {
        final JiraServices jiraServices = Mockito.mock(JiraServices.class);

        final UserManagerMock userManagerMock = new UserManagerMock();
        Mockito.when(jiraServices.getUserManager()).thenReturn(userManagerMock);

        return jiraServices;
    }

    private BlackDuckProjectMappings createMockBlackDuckProjectMappings(final Set<BlackDuckProjectMapping> mappings) {
        BlackDuckProjectMappings blackDuckProjectMappings = Mockito.mock(BlackDuckProjectMappings.class);
        blackDuckProjectMappings = Mockito.spy(blackDuckProjectMappings);

        for (final BlackDuckProjectMapping mapping : mappings) {
            Mockito.when(blackDuckProjectMappings.getJiraProjects(mapping.getBlackDuckProjectName())).thenReturn(Collections.singletonList(mapping.getJiraProject()));
        }

        return blackDuckProjectMappings;
    }

    private BlackDuckDataHelper createMockBlackDuckDataHelper() throws IntegrationException {
        final BlackDuckDataHelper dataFormatHelper = Mockito.mock(BlackDuckDataHelper.class);
        final ProjectVersionWrapper projectVersionWrapper = new ProjectVersionWrapper();

        final ProjectView projectView = new ProjectView();
        projectView.setName(TEST_PROJECT);
        projectVersionWrapper.setProjectView(projectView);

        final ProjectVersionView projectVersionView = new ProjectVersionView();
        projectVersionWrapper.setProjectVersionView(projectVersionView);

        final VersionBomComponentView versionBomComponentView = new VersionBomComponentView();

        final UriSingleResponse policyResponse = new UriSingleResponse(POLICY_RULE_LINK, PolicyRuleView.class);
        final PolicyRuleView policyRuleView = Mockito.mock(PolicyRuleView.class);
        Mockito.when(policyRuleView.getHref()).thenReturn(Optional.of(POLICY_RULE_LINK));

        Mockito.when(dataFormatHelper.getProjectVersionWrapper(Mockito.any(NotificationContentDetail.class))).thenReturn(projectVersionWrapper);
        Mockito.when(dataFormatHelper.getBomComponent(Mockito.any())).thenReturn(versionBomComponentView);
        Mockito.when(dataFormatHelper.getResponse(Mockito.eq(policyResponse))).thenReturn(policyRuleView);

        return dataFormatHelper;
    }

}
