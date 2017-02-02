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
package com.blackducksoftware.integration.jira.task.conversion.output;

import com.blackducksoftware.integration.hub.api.component.version.ComponentVersion;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyContentItem;

public class PolicyIssuePropertiesGenerator implements IssuePropertiesGenerator {
    private final String projectName;

    private final String projectVersion;

    private final String componentName;

    private final String componentVersion;

    private final String ruleName;

    public PolicyIssuePropertiesGenerator(final PolicyContentItem notifContentItem,
            final String ruleName) {
        this.projectName = notifContentItem.getProjectVersion().getProjectName();
        this.projectVersion = notifContentItem.getProjectVersion().getProjectVersionName();
        this.componentName = notifContentItem.getComponentName();
        final ComponentVersion compVer = notifContentItem.getComponentVersion();
        if (compVer == null) {
            this.componentVersion = "";
        } else {
            this.componentVersion = compVer.getVersionName();
        }
        this.ruleName = ruleName;
    }

    @Override
    public IssueProperties createIssueProperties(final Long issueId) {
        final IssueProperties properties = new PolicyViolationIssueProperties(
                projectName,
                projectVersion,
                componentName, componentVersion,
                issueId, ruleName);
        return properties;
    }

}
