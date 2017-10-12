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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.dataservice.notification.model.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.model.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.model.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.model.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.notification.processor.SubProcessorCache;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
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

public class PolicyViolationNotificationConverter extends AbstractPolicyNotificationConverter {
    private final static HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(PolicyViolationNotificationConverter.class.getName()));

    public PolicyViolationNotificationConverter(final SubProcessorCache cache, final HubProjectMappings mappings, final HubJiraFieldCopyConfigSerializable fieldCopyConfig, final JiraServices jiraServices, final JiraContext jiraContext,
            final JiraSettingsService jiraSettingsService, final HubServicesFactory hubServicesFactory) throws ConfigurationException {
        super(cache, mappings, jiraServices, jiraContext, jiraSettingsService, HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE, fieldCopyConfig, hubServicesFactory, logger);
    }

    @Override
    protected List<NotificationEvent> handleNotificationPerJiraProject(final NotificationContentItem notif, final JiraProject jiraProject) throws EventDataBuilderException, IntegrationException {
        final List<NotificationEvent> events = new ArrayList<>();

        final HubEventAction action = HubEventAction.OPEN;
        final PolicyViolationContentItem notification = (PolicyViolationContentItem) notif;
        for (final PolicyRuleView rule : notification.getPolicyRuleList()) {
            final IssuePropertiesGenerator issuePropertiesGenerator = new PolicyIssuePropertiesGenerator(notification, rule.name);
            final ComponentVersionView compVer = notification.getComponentVersion();
            final String compVerName;
            if (compVer == null) {
                compVerName = "";
            } else {
                compVerName = compVer.versionName;
            }
            final String licensesString = getComponentLicensesStringPlainText(notification);
            logger.debug("Component " + notification.getComponentName() + " (version: " + compVerName + "): License: " + licensesString);

            final VersionBomComponentView bomComp = getBomComponent(notification);
            final EventDataBuilder eventDataBuilder = new EventDataBuilder(EventCategory.POLICY);
            final EventData eventData = eventDataBuilder.setAction(action).setJiraAdminUserName(getJiraContext().getJiraAdminUser().getName()).setJiraAdminUserKey(getJiraContext().getJiraAdminUser().getKey())
                    .setJiraIssueCreatorUserName(getJiraContext().getJiraIssueCreatorUser().getName()).setJiraIssueCreatorUserKey(getJiraContext().getJiraIssueCreatorUser().getKey())
                    .setJiraIssueAssigneeUserId(jiraProject.getAssigneeUserId()).setJiraIssueTypeId(getIssueTypeId()).setJiraProjectName(jiraProject.getProjectName()).setJiraProjectId(jiraProject.getProjectId())
                    .setJiraFieldCopyMappings(getFieldCopyConfig().getProjectFieldCopyMappings()).setHubProjectName(notification.getProjectVersion().getProjectName())
                    .setHubProjectVersion(notification.getProjectVersion().getProjectVersionName()).setHubProjectVersionUrl(notification.getProjectVersion().getUrl()).setHubComponentName(notification.getComponentName())
                    .setHubComponentUrl(notification.getComponentUrl()).setHubComponentVersion(compVerName).setHubComponentVersionUrl(notification.getComponentVersionUrl()).setHubLicenseNames(licensesString)
                    .setHubComponentUsage(getComponentUsage(notification, bomComp)).setHubComponentOrigin(getComponentOrigin(notification)).setHubComponentOriginId(getComponentOriginId(notification))
                    .setHubProjectVersionNickname(getProjectVersionNickname(notification)).setJiraIssueSummary(getIssueSummary(notification, rule)).setJiraIssueDescription(getIssueDescription(notification, rule)).setJiraIssueComment(null)
                    .setJiraIssueReOpenComment(HubJiraConstants.HUB_POLICY_VIOLATION_REOPEN).setJiraIssueCommentForExistingIssue(HubJiraConstants.HUB_POLICY_VIOLATION_DETECTED_AGAIN_COMMENT)
                    .setJiraIssueResolveComment(HubJiraConstants.HUB_POLICY_VIOLATION_RESOLVE).setJiraIssueCommentInLieuOfStateChange(HubJiraConstants.HUB_POLICY_VIOLATION_DETECTED_AGAIN_COMMENT)
                    .setJiraIssuePropertiesGenerator(issuePropertiesGenerator).setHubRuleName(rule.name).setHubRuleUrl(getHubServicesFactory().createMetaService().getHref(rule)).setComponentIssueUrl(notif.getComponentIssueLink()).build();

            final Map<String, Object> eventDataSet = new HashMap<>(1);
            eventDataSet.put(HubJiraConstants.EVENT_DATA_SET_KEY_JIRA_EVENT_DATA, eventData);
            final String key = generateEventKey(eventData.getDataSet());
            final NotificationEvent event = new NotificationEvent(key, NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE, eventDataSet);
            events.add(event);
        }

        return events;
    }
}
