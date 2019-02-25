/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.jira.common.notification;

import java.util.Arrays;
import java.util.List;

import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;
import com.synopsys.integration.blackduck.service.model.ProjectVersionDescription;

public class RuleViolationClearedNotificationContent extends NotificationContent {
    public String projectName;
    public String projectVersionName;
    public String projectVersion;
    public int componentVersionsCleared;
    public List<ComponentVersionStatus> componentVersionStatuses;
    public List<PolicyInfo> policyInfos;

    @Override
    public boolean providesPolicyDetails() {
        return true;
    }

    @Override
    public boolean providesVulnerabilityDetails() {
        return false;
    }

    @Override
    public boolean providesProjectComponentDetails() {
        return true;
    }

    @Override
    public boolean providesLicenseDetails() {
        return false;
    }

    @Override
    public List<ProjectVersionDescription> getAffectedProjectVersionDescriptions() {
        final ProjectVersionDescription projectVersionDescription = new ProjectVersionDescription(projectName, projectVersionName, projectVersion);
        return Arrays.asList(projectVersionDescription);
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public String getProjectVersion() {
        return projectVersion;
    }

    public int getComponentVersionsCleared() {
        return componentVersionsCleared;
    }

    public List<ComponentVersionStatus> getComponentVersionStatuses() {
        return componentVersionStatuses;
    }

    public List<PolicyInfo> getPolicyInfos() {
        return policyInfos;
    }
}