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
package com.blackducksoftware.integration.jira.task.conversion.output.old;

import java.util.Optional;

import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventData;
import com.synopsys.integration.util.Stringable;

// TODO update this for BOM_EDIT
public class IssueProperties extends Stringable {
    private final String projectName;
    private final String projectVersionName;
    private final String componentName;
    private final String componentVersionName;
    private final String ruleName;

    private final String bomComponentUri;

    private final Long jiraIssueId;

    // @formatter:off
    public static IssueProperties fromEventData(final EventData eventData, final Long jiraIssueId) {
        return new IssueProperties(
                 eventData.getBlackDuckProjectName()
                ,eventData.getBlackDuckProjectVersionName()
                ,eventData.getBlackDuckComponentName()
                ,eventData.getBlackDuckComponentVersionName()
                ,eventData.getBlackDuckRuleName()
                ,eventData.getBlackDuckBomComponentUri()
                ,jiraIssueId);
    }
    // @formatter:on

    public IssueProperties(final String projectName, final String projectVersionName, final String componentName, final String componentVersionName, final String ruleName, final String bomComponentUri, final Long jiraIssueId) {
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.componentName = componentName;
        this.componentVersionName = componentVersionName;
        this.ruleName = ruleName;

        this.bomComponentUri = bomComponentUri;

        this.jiraIssueId = jiraIssueId;
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

    public Optional<String> getRuleName() {
        return Optional.ofNullable(ruleName);
    }

    public String getBomComponentUri() {
        return bomComponentUri;
    }

    public Long getJiraIssueId() {
        return jiraIssueId;
    }

}
