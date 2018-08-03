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
package com.blackducksoftware.integration.jira.task.issue.model;

import java.util.Set;

import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;

public class JiraIssueWrapper {
    private final JiraIssueFieldTemplate jiraIssueFieldTemplate;
    private final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate;
    private final Set<ProjectFieldCopyMapping> projectFieldCopyMappings;

    public JiraIssueWrapper(final JiraIssueFieldTemplate jiraIssueFieldTemplate, final BlackDuckIssueFieldTemplate blackDuckIssueTemplate, final Set<ProjectFieldCopyMapping> projectFieldCopyMappings) {
        this.jiraIssueFieldTemplate = jiraIssueFieldTemplate;
        this.blackDuckIssueFieldTemplate = blackDuckIssueTemplate;
        this.projectFieldCopyMappings = projectFieldCopyMappings;
    }

    public JiraIssueFieldTemplate getJiraIssueFieldTemplate() {
        return jiraIssueFieldTemplate;
    }

    public BlackDuckIssueFieldTemplate getBlackDuckIssueTemplate() {
        return blackDuckIssueFieldTemplate;
    }

    public Set<ProjectFieldCopyMapping> getProjectFieldCopyMappings() {
        return projectFieldCopyMappings;
    }

    @Override
    public String toString() {
        return "[Jira Fields: " + jiraIssueFieldTemplate + " | Black Duck Fields: " + blackDuckIssueFieldTemplate + " | Copy Fields: " + projectFieldCopyMappings + "]";
    }

}
