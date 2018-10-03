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

import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventCategory;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueFieldTemplate;
import com.blackducksoftware.integration.jira.task.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.task.issue.model.PolicyIssueFieldTempate;

public class OldIssueProperties extends IssueProperties {
    private final String projectName;
    private final String projectVersionName;
    private final String componentName;
    private final String componentVersionName;

    public OldIssueProperties(final String projectName, final String projectVersionName, final String componentName, final String componentVersionName, final String ruleName, final EventCategory type, final String bomComponentUri,
        final Long jiraIssueId) {
        super(type, bomComponentUri, ruleName, jiraIssueId);
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.componentName = componentName;
        this.componentVersionName = componentVersionName;
    }
    // @formatter:on

    // @formatter:off
    public static OldIssueProperties fromEventData(final EventData eventData, final Long jiraIssueId) {
        return new OldIssueProperties(
                 eventData.getBlackDuckProjectName()
                ,eventData.getBlackDuckProjectVersionName()
                ,eventData.getBlackDuckComponentName()
                ,eventData.getBlackDuckComponentVersionName()
                ,eventData.getBlackDuckRuleName()
                ,eventData.getCategory()
                ,eventData.getBlackDuckBomComponentUri()
                ,jiraIssueId);
    }
    // @formatter:on

    // @formatter:off
    public static OldIssueProperties fromBlackDuckIssueWrapper(final BlackDuckIssueModel blackDuckIssueModel, final Long jiraIssueId) {
        final BlackDuckIssueFieldTemplate template = blackDuckIssueModel.getBlackDuckIssueTemplate();

        String ruleName = null;
        final EventCategory category = blackDuckIssueModel.getEventCategoryFromFieldTemplate();
        if (EventCategory.POLICY.equals(category)) {
            ruleName = ((PolicyIssueFieldTempate) template).getPolicyRuleName();
        }
        return new OldIssueProperties(
                 template.getProjectName()
                ,template.getProjectVersionName()
                ,template.getComponentName()
                ,template.getComponentVersionName()
                ,ruleName
                ,category
                ,blackDuckIssueModel.getBomComponentUri()
                ,jiraIssueId);
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersion() {
        return projectVersionName;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersion() {
        return componentVersionName;
    }

}
