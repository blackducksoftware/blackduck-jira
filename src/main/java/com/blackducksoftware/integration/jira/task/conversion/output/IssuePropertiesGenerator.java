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
package com.blackducksoftware.integration.jira.task.conversion.output;

import java.util.Optional;

import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventCategory;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataBuilder;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleViewV2;
import com.synopsys.integration.blackduck.notification.content.detail.NotificationContentDetail;

public class IssuePropertiesGenerator {
    private static final String UNKNOWN = "<unknown>";

    private final boolean isPolicy;
    private final String projectName;
    private final String projectVersionName;
    private final String componentName;
    private final String componentVersionName;
    private final String ruleName;

    public IssuePropertiesGenerator(final NotificationContentDetail notificationContentDetail, final Optional<PolicyRuleViewV2> optionalPolicyRule) {
        this.isPolicy = notificationContentDetail.isPolicy();
        this.projectName = notificationContentDetail.getProjectName().orElse(UNKNOWN);
        this.projectVersionName = notificationContentDetail.getProjectVersionName().orElse(UNKNOWN);
        this.componentName = notificationContentDetail.getComponentName().orElse(UNKNOWN);
        this.componentVersionName = notificationContentDetail.getComponentVersionName().orElse(UNKNOWN);
        this.ruleName = optionalPolicyRule.isPresent() ? optionalPolicyRule.get().name : UNKNOWN;
    }

    public IssuePropertiesGenerator(final EventDataBuilder eventDataBuilder) {
        this.isPolicy = EventCategory.POLICY.equals(eventDataBuilder.getEventCategory());
        this.projectName = eventDataBuilder.getBlackDuckProjectName();
        this.projectVersionName = eventDataBuilder.getBlackDuckProjectVersionName();
        this.componentName = eventDataBuilder.getBlackDuckComponentName();
        this.componentVersionName = eventDataBuilder.getBlackDuckComponentVersionName();
        this.ruleName = eventDataBuilder.getBlackDuckRuleName();
    }

    public IssueProperties createIssueProperties(final Long issueId) {
        if (isPolicy) {
            return new PolicyViolationIssueProperties(projectName, projectVersionName, componentName, getComponentVersionName(), issueId, ruleName);
        }
        return new VulnerabilityIssueProperties(projectName, projectVersionName, componentName, getComponentVersionName(), issueId);
    }

    private String getComponentVersionName() {
        if (componentVersionName != null) {
            return componentVersionName;
        }
        return UNKNOWN;
    }

}
