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
import com.blackducksoftware.integration.hub.api.UriSingleResponse;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleViewV2;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomComponentView;
import com.blackducksoftware.integration.hub.api.view.CommonNotificationState;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.hub.service.bucket.HubBucketService;
import com.blackducksoftware.integration.hub.throwaway.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.throwaway.NotificationEvent;
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

public class PolicyOverrideNotificationConverter extends AbstractPolicyNotificationConverter {
    private final static HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(PolicyOverrideNotificationConverter.class.getName()));

    // FIXME these should be passed in
    private final HubBucketService bucketService = new HubBucketService(null);
    private final HubBucket hubBucket = new HubBucket();

    public PolicyOverrideNotificationConverter(final SubProcessorCache cache, final HubProjectMappings mappings, final HubJiraFieldCopyConfigSerializable fieldCopyConfig, final JiraServices jiraServices, final JiraContext jiraContext,
            final JiraSettingsService jiraSettingsService, final HubServicesFactory hubServicesFactory, final MetaHandler metaHandler) throws ConfigurationException {
        super(cache, mappings, jiraServices, jiraContext, jiraSettingsService, HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE, fieldCopyConfig, hubServicesFactory, metaHandler, logger);
    }

    @Override
    protected List<NotificationEvent> handleNotificationPerJiraProject(final CommonNotificationState commonNotificationState, final JiraProject jiraProject) throws EventDataBuilderException, IntegrationException {
        final List<NotificationEvent> events = new ArrayList<>();

        final HubEventAction action = HubEventAction.RESOLVE;
        for (final NotificationContentDetail detail : commonNotificationState.getContent().getNotificationContentDetails()) {
            bucketService.addToTheBucket(hubBucket, detail.getPresentLinks());
            UriSingleResponse<PolicyRuleViewV2> policyRuleLink = null;
            if (detail.isPolicy()) {
                policyRuleLink = detail.getPolicy().get();
            }
            final PolicyRuleViewV2 rule = hubBucket.get(policyRuleLink);
            final IssuePropertiesGenerator issuePropertiesGenerator = new PolicyIssuePropertiesGenerator(detail, rule.name);

            String licensesString;
            ComponentVersionView componentVersionView = null;
            if (detail.getComponentVersion().isPresent()) {
                componentVersionView = hubBucket.get(detail.getComponentVersion().get());
                licensesString = getComponentLicensesStringPlainText(componentVersionView);
                final String componentName = detail.getComponentName().orElse("");
                final String componentVersionName = detail.getComponentVersionName().orElse("");
                logger.debug("Component " + componentName + " (version: " + componentVersionName + "): License: " + licensesString);
            } else {
                licensesString = "";
            }

            final VersionBomComponentView bomComp = getBomComponent(componentVersionView);
            final EventDataBuilder eventDataBuilder = new EventDataBuilder(EventCategory.POLICY);
            eventDataBuilder.setAction(action);
            eventDataBuilder.setPropertiesFromJiraContext(getJiraContext());
            eventDataBuilder.setPropertiesFromJiraProject(jiraProject);

            eventDataBuilder.setJiraIssueTypeId(getIssueTypeId());
            eventDataBuilder.setJiraFieldCopyMappings(getFieldCopyConfig().getProjectFieldCopyMappings());
            eventDataBuilder.setJiraIssueReOpenComment(HubJiraConstants.HUB_POLICY_VIOLATION_REOPEN);
            eventDataBuilder.setJiraIssueCommentForExistingIssue(HubJiraConstants.HUB_POLICY_VIOLATION_OVERRIDDEN_COMMENT);
            eventDataBuilder.setJiraIssueResolveComment(HubJiraConstants.HUB_POLICY_VIOLATION_RESOLVE);
            eventDataBuilder.setJiraIssueCommentInLieuOfStateChange(HubJiraConstants.HUB_POLICY_VIOLATION_OVERRIDDEN_COMMENT);
            eventDataBuilder.setJiraIssuePropertiesGenerator(issuePropertiesGenerator);
            eventDataBuilder.setHubRuleName(rule.name);
            eventDataBuilder.setHubRuleUrl(policyRuleLink.uri);
            eventDataBuilder.setHubLicenseNames(licensesString);

            eventDataBuilder.setPropertiesFromNotificationContentDetail(detail);

            eventDataBuilder.setHubComponentUsage(getComponentUsage(detail, bomComp));
            eventDataBuilder.setHubProjectVersionNickname(getProjectVersionNickname(detail));
            eventDataBuilder.setJiraIssueSummary(getIssueSummary(detail, rule));
            eventDataBuilder.setJiraIssueDescription(getIssueDescription(detail, rule));

            populateEventDataBuilder(eventDataBuilder, detail);

            final EventData eventData = eventDataBuilder.build();

            final Map<String, Object> eventDataSet = new HashMap<>(1);
            eventDataSet.put(HubJiraConstants.EVENT_DATA_SET_KEY_JIRA_EVENT_DATA, eventData);
            final String key = generateEventKey(eventData.getDataSet());
            final NotificationEvent event = new NotificationEvent(key, NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE, eventDataSet);
            events.add(event);
        }

        return events;
    }
}
