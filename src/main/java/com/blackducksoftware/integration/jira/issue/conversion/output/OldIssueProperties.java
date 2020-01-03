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
package com.blackducksoftware.integration.jira.issue.conversion.output;

import com.blackducksoftware.integration.jira.issue.model.BlackDuckIssueFieldTemplate;
import com.blackducksoftware.integration.jira.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.issue.model.IssueCategory;

public class OldIssueProperties extends IssueProperties {
    private final String projectName;
    private final String projectVersionName;
    private final String componentName;
    private final String componentVersionName;

    public OldIssueProperties(final String projectName, final String projectVersionName, final String componentName, final String componentVersionName, final String ruleName, final IssueCategory type, final String bomComponentUri,
        final Long jiraIssueId) {
        super(type, bomComponentUri, ruleName, jiraIssueId);
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.componentName = componentName;
        this.componentVersionName = componentVersionName;
    }
    // @formatter:on

    // @formatter:off
    public static OldIssueProperties fromBlackDuckIssueWrapper(final BlackDuckIssueModel blackDuckIssueModel) {
        final BlackDuckIssueFieldTemplate template = blackDuckIssueModel.getBlackDuckIssueTemplate();
        return new OldIssueProperties(
                 template.getProjectName()
                ,template.getProjectVersionName()
                ,template.getComponentName()
                ,template.getComponentVersionName()
                ,template.getPolicyRuleName()
                ,template.getIssueCategory()
                ,blackDuckIssueModel.getBomComponentUri()
                ,blackDuckIssueModel.getJiraIssueId());
    }
    // @formatter:on

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
