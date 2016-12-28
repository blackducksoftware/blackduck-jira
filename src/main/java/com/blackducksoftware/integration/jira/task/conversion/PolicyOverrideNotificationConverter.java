/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.task.conversion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.item.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubProjectMappings;
import com.blackducksoftware.integration.jira.common.JiraContext;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.config.HubJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.conversion.output.HubEventAction;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class PolicyOverrideNotificationConverter extends AbstractPolicyNotificationConverter {

    public PolicyOverrideNotificationConverter(final HubProjectMappings mappings,
            final HubJiraFieldCopyConfigSerializable fieldCopyConfig,
            final JiraServices jiraServices,
            final JiraContext jiraContext, final JiraSettingsService jiraSettingsService,
            final MetaService metaService)
            throws ConfigurationException {
        super(mappings, jiraServices, jiraContext, jiraSettingsService, HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE,
                metaService, fieldCopyConfig);
    }

    @Override
    protected List<NotificationEvent> handleNotificationPerJiraProject(final NotificationContentItem notif,
            final JiraProject jiraProject) throws HubIntegrationException {
        final List<NotificationEvent> events = new ArrayList<>();

        final HubEventAction action = HubEventAction.RESOLVE;
        final PolicyOverrideContentItem notification = (PolicyOverrideContentItem) notif;
        for (final PolicyRule rule : notification.getPolicyRuleList()) {

            Map<String, Object> dataSet = createDataSet(notif, action, getJiraContext(), jiraProject,
                    null, HubJiraConstants.HUB_POLICY_VIOLATION_OVERRIDDEN_COMMENT,
                    HubJiraConstants.HUB_POLICY_VIOLATION_RESOLVE,
                    rule.getName());
            String key = getUniquePropertyKeyForPolicyIssue(notification, jiraProject.getProjectId(),
                    getMetaService().getHref(rule));
            final NotificationEvent event = new NotificationEvent(key, NotificationCategoryEnum.POLICY_VIOLATION_OVERRIDE, dataSet);

            events.add(event);
        }

        return events;
    }
}
