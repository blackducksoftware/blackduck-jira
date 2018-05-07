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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.throwaway.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.throwaway.NotificationContentItem;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;
import com.blackducksoftware.integration.hub.throwaway.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.throwaway.SubProcessorCache;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.exception.EventDataBuilderException;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.IssuePropertiesGenerator;
import com.blackducksoftware.integration.jira.task.conversion.output.PolicyIssuePropertiesGenerator;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventCategory;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataBuilder;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class PolicyViolationClearedNotificationConverter extends AbstractPolicyNotificationConverter {
    private final static HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(PolicyViolationClearedNotificationConverter.class.getName()));

    public PolicyViolationClearedNotificationConverter(final SubProcessorCache cache, final HubProjectMappings mappings, final HubJiraFieldCopyConfigSerializable fieldCopyConfig, final JiraServices jiraServices,
            final JiraContext jiraContext, final JiraSettingsService jiraSettingsService, final HubServicesFactory hubServicesFactory, final MetaHandler metaHandler) throws ConfigurationException {
        super(cache, mappings, jiraServices, jiraContext, jiraSettingsService, HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE, fieldCopyConfig, hubServicesFactory, metaHandler, logger);
    }

    @Override
    protected List<NotificationEvent> handleNotificationPerJiraProject(final NotificationContentItem notificationContentItem, final JiraProject jiraProject) throws EventDataBuilderException, IntegrationException {
        final List<NotificationEvent> notificationEvents = new ArrayList<>();

        final HubEventAction hubEventAction = HubEventAction.RESOLVE;
        final PolicyViolationClearedContentItem policyViolationClearedContentItem = (PolicyViolationClearedContentItem) notificationContentItem;
        logger.debug("handleNotificationPerJiraProject(): notification: " + policyViolationClearedContentItem);
        for (final PolicyRuleView policyRuleView : policyViolationClearedContentItem.getPolicyRuleList()) {
            final IssuePropertiesGenerator issuePropertiesGenerator = new PolicyIssuePropertiesGenerator(policyViolationClearedContentItem, policyRuleView.name);
            final ComponentVersionView componentVersionView = policyViolationClearedContentItem.getComponentVersion();
            final String componentVersionName = componentVersionView == null ? "" : componentVersionView.versionName;
            final String licensesString = getComponentLicensesStringPlainText(policyViolationClearedContentItem);
            logger.debug("Component " + policyViolationClearedContentItem.getComponentName() + ": License: " + licensesString);

            final VersionBomComponentView bomComp = getBomComponent(policyViolationClearedContentItem);
            final EventDataBuilder eventDataBuilder = new EventDataBuilder(EventCategory.POLICY);
            eventDataBuilder.setAction(hubEventAction);
            eventDataBuilder.setPropertiesFromJiraContext(getJiraContext());
            eventDataBuilder.setPropertiesFromJiraProject(jiraProject);

            eventDataBuilder.setJiraIssueTypeId(getIssueTypeId());
            eventDataBuilder.setJiraFieldCopyMappings(getFieldCopyConfig().getProjectFieldCopyMappings());
            eventDataBuilder.setJiraIssueReOpenComment(HubJiraConstants.HUB_POLICY_VIOLATION_REOPEN);
            eventDataBuilder.setJiraIssueCommentForExistingIssue(HubJiraConstants.HUB_POLICY_VIOLATION_CLEARED_COMMENT);
            eventDataBuilder.setJiraIssueResolveComment(HubJiraConstants.HUB_POLICY_VIOLATION_CLEARED_RESOLVE);
            eventDataBuilder.setJiraIssueCommentInLieuOfStateChange(HubJiraConstants.HUB_POLICY_VIOLATION_CLEARED_COMMENT);
            eventDataBuilder.setJiraIssuePropertiesGenerator(issuePropertiesGenerator);
            eventDataBuilder.setHubComponentVersion(componentVersionName);
            eventDataBuilder.setHubLicenseNames(licensesString);
            eventDataBuilder.setHubRuleName(policyRuleView.name);
            eventDataBuilder.setHubRuleUrl(getMetaHandler().getHref(policyRuleView));

            eventDataBuilder.setHubProjectName(policyViolationClearedContentItem.getProjectVersion().getProjectName());
            eventDataBuilder.setHubProjectVersion(policyViolationClearedContentItem.getProjectVersion().getProjectVersionName());
            eventDataBuilder.setHubProjectVersionUrl(policyViolationClearedContentItem.getProjectVersion().getUrl());
            eventDataBuilder.setHubComponentName(policyViolationClearedContentItem.getComponentName());
            eventDataBuilder.setHubComponentUrl(policyViolationClearedContentItem.getComponentUrl());
            eventDataBuilder.setHubComponentVersionUrl(policyViolationClearedContentItem.getComponentVersionUrl());
            eventDataBuilder.setHubComponentUsage(getComponentUsage(policyViolationClearedContentItem, bomComp));
            eventDataBuilder.setHubProjectVersionNickname(getProjectVersionNickname(policyViolationClearedContentItem));
            eventDataBuilder.setJiraIssueSummary(getIssueSummary(policyViolationClearedContentItem, policyRuleView));
            eventDataBuilder.setJiraIssueDescription(getIssueDescription(policyViolationClearedContentItem, policyRuleView));
            eventDataBuilder.setComponentIssueUrl(policyViolationClearedContentItem.getComponentIssueLink());

            populateEventDataBuilder(eventDataBuilder, policyViolationClearedContentItem);

            final EventData eventData = eventDataBuilder.build();

            final Map<String, Object> eventDataSet = new HashMap<>(1);
            eventDataSet.put(HubJiraConstants.EVENT_DATA_SET_KEY_JIRA_EVENT_DATA, eventData);
            final String key = generateEventKey(eventData.getDataSet());
            final NotificationEvent event = new NotificationEvent(key, NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE, eventDataSet);
            logger.debug("handleNotificationPerJiraProject(): adding event: " + event);
            notificationEvents.add(event);
        }

        return notificationEvents;
    }

}
