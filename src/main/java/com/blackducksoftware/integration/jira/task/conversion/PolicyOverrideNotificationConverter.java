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
import java.util.Set;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleViewV2;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.hub.service.bucket.HubBucketService;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;
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

public class PolicyOverrideNotificationConverter extends AbstractPolicyNotificationConverter {
    private final static HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(PolicyOverrideNotificationConverter.class.getName()));

    private final HubBucketService hubBucketService;

    public PolicyOverrideNotificationConverter(final Set<NotificationEvent> cache, final HubProjectMappings mappings, final HubJiraFieldCopyConfigSerializable fieldCopyConfig, final JiraServices jiraServices, final JiraContext jiraContext,
            final JiraSettingsService jiraSettingsService, final HubService hubService, final HubBucketService hubBucketService) throws ConfigurationException {
        super(cache, mappings, jiraServices, jiraContext, jiraSettingsService, HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE, fieldCopyConfig, hubService, logger);
        this.hubBucketService = hubBucketService;
    }

    @Override
    protected List<NotificationEvent> handleNotificationPerJiraProject(final CommonNotificationState commonNotificationState, final JiraProject jiraProject) throws EventDataBuilderException, IntegrationException {
        final List<NotificationEvent> events = new ArrayList<>();

        final HubEventAction action = HubEventAction.RESOLVE;
        final HubBucket hubBucket = new HubBucket();
        for (final NotificationContentDetail detail : commonNotificationState.getContent().getNotificationContentDetails()) {
            hubBucketService.addToTheBucket(hubBucket, detail.getPresentLinks());

            String licensesString;
            ComponentVersionView componentVersion = null;
            if (detail.getComponentVersion().isPresent()) {
                componentVersion = hubBucket.get(detail.getComponentVersion().get());
                licensesString = getComponentLicensesStringPlainText(componentVersion);
                final String componentName = detail.getComponentName().orElse("");
                final String componentVersionName = detail.getComponentVersionName().orElse("");
                logger.debug("Component " + componentName + " (version: " + componentVersionName + "): License: " + licensesString);
            } else {
                licensesString = "";
            }

            final EventDataBuilder eventDataBuilder = new EventDataBuilder(EventCategory.POLICY);
            eventDataBuilder.setAction(action);
            eventDataBuilder.setPropertiesFromJiraContext(getJiraContext());
            eventDataBuilder.setPropertiesFromJiraProject(jiraProject);

            eventDataBuilder.setJiraIssueTypeId(getIssueTypeId());
            eventDataBuilder.setJiraFieldCopyMappings(getFieldCopyConfig().getProjectFieldCopyMappings());

            eventDataBuilder.setIssueCommentPropertiesFromNotificationType(commonNotificationState.getType(), commonNotificationState.getContent());

            if (detail.isPolicy()) {
                final UriSingleResponse<PolicyRuleViewV2> policyRuleLink = detail.getPolicy().get();
                final PolicyRuleViewV2 rule = hubBucket.get(policyRuleLink);
                eventDataBuilder.setHubRuleName(rule.name);
                eventDataBuilder.setHubRuleUrl(policyRuleLink.uri);

                final IssuePropertiesGenerator issuePropertiesGenerator = new PolicyIssuePropertiesGenerator(detail, rule.name);
                eventDataBuilder.setJiraIssuePropertiesGenerator(issuePropertiesGenerator);
                eventDataBuilder.setJiraIssueSummary(getIssueSummary(detail, rule));
                eventDataBuilder.setJiraIssueDescription(getIssueDescription(detail, rule, hubBucket));
            }
            eventDataBuilder.setHubLicenseNames(licensesString);

            eventDataBuilder.setPropertiesFromNotificationContentDetail(detail);

            eventDataBuilder.setHubComponentUsage(getComponentUsage(detail, hubBucket));
            eventDataBuilder.setHubProjectVersionNickname(getProjectVersionNickname(detail, hubBucket));

            populateEventDataBuilder(eventDataBuilder, detail, hubBucket);

            final EventData eventData = eventDataBuilder.build();

            final Map<String, Object> eventDataSet = new HashMap<>(1);
            eventDataSet.put(HubJiraConstants.EVENT_DATA_SET_KEY_JIRA_EVENT_DATA, eventData);
            final String key = generateEventKey(eventData.getDataSet());
            final NotificationEvent event = new NotificationEvent(key, commonNotificationState.getType(), eventDataSet);
            events.add(event);
        }

        return events;
    }
}
