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
package com.blackducksoftware.integration.jira.config.controller.action;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blackducksoftware.integration.jira.common.BlackDuckWorkflowStatus;
import com.blackducksoftware.integration.jira.common.WorkflowHelper;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.common.settings.PluginSettingsWrapper;
import com.blackducksoftware.integration.jira.config.JiraConfigErrorStrings;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;

public class ProjectMappingConfigActions {
    private final PluginSettingsWrapper pluginSettingsWrapper;
    private final WorkflowHelper workflowHelper;

    public ProjectMappingConfigActions(final PluginSettingsFactory pluginSettingsFactory, final WorkflowHelper workflowHelper) {
        this.pluginSettingsWrapper = new PluginSettingsWrapper(pluginSettingsFactory.createGlobalSettings());
        this.workflowHelper = workflowHelper;
    }

    public BlackDuckJiraConfigSerializable getMappings() {
        final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();
        final String blackDuckProjectMappingsJson = pluginSettingsWrapper.getProjectMappingsJson();
        txConfig.setHubProjectMappingsJson(blackDuckProjectMappingsJson);

        addWorkflowStatusToMappings(txConfig);
        validateMapping(txConfig);
        return txConfig;
    }

    public void validateMapping(final BlackDuckJiraConfigSerializable config) {
        if (config.getHubProjectMappings() != null && !config.getHubProjectMappings().isEmpty()) {
            boolean hasEmptyMapping = false;
            for (final BlackDuckProjectMapping mapping : config.getHubProjectMappings()) {
                boolean jiraProjectBlank = true;
                boolean blackDuckProjectBlank = true;
                if (mapping.getJiraProject() != null) {
                    if (mapping.getJiraProject().getProjectId() != null) {
                        jiraProjectBlank = false;
                    }
                }
                if (StringUtils.isNotBlank(mapping.getBlackDuckProjectName())) {
                    blackDuckProjectBlank = false;
                }
                if (jiraProjectBlank || blackDuckProjectBlank) {
                    hasEmptyMapping = true;
                }
            }
            if (hasEmptyMapping) {
                config.setHubProjectMappingError(StringUtils.joinWith(" : ", config.getHubProjectMappingError(), JiraConfigErrorStrings.MAPPING_HAS_EMPTY_ERROR));
            }
        }
    }

    private void addWorkflowStatusToMappings(final BlackDuckJiraConfigSerializable config) {
        final Set<BlackDuckProjectMapping> projectMappings = config.getHubProjectMappings();
        for (final BlackDuckProjectMapping mapping : projectMappings) {
            final JiraProject jiraProject = mapping.getJiraProject();

            final BlackDuckWorkflowStatus workflowStatus = workflowHelper.getBlackDuckWorkflowStatus(jiraProject.getProjectId());
            jiraProject.setWorkflowStatus(workflowStatus.getPrettyPrintName());
        }
    }

}
