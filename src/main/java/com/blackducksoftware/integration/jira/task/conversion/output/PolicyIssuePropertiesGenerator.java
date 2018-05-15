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
package com.blackducksoftware.integration.jira.task.conversion.output;

import com.blackducksoftware.integration.hub.notification.content.NotificationContentDetail;

public class PolicyIssuePropertiesGenerator implements IssuePropertiesGenerator {
    private final String projectName;
    private final String projectVersionName;
    private final String componentName;
    private final String componentVersionName;
    private final String ruleName;

    public PolicyIssuePropertiesGenerator(final NotificationContentDetail detail, final String ruleName) {
        this.projectName = detail.getProjectName();
        this.projectVersionName = detail.getProjectVersionName();
        this.componentName = detail.getComponentName().orElse("?");
        this.componentVersionName = detail.getComponentVersionName().orElse("?");
        this.ruleName = ruleName;
    }

    @Override
    public IssueProperties createIssueProperties(final Long issueId) {
        final IssueProperties properties = new PolicyViolationIssueProperties(projectName, projectVersionName, componentName, componentVersionName, issueId, ruleName);
        return properties;
    }

}
