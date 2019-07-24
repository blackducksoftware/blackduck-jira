/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
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
package com.blackducksoftware.integration.jira.web.action;

import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckWorkflowStatus;
import com.blackducksoftware.integration.jira.common.WorkflowHelper;
import com.blackducksoftware.integration.jira.common.exception.JiraException;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.data.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.data.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.data.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.web.JiraConfigErrorStrings;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraConfigSerializable;

public class ProjectMappingConfigActions {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    final GlobalConfigurationAccessor globalConfigurationAccessor;
    private final WorkflowHelper workflowHelper;

    public ProjectMappingConfigActions(final JiraSettingsAccessor jiraSettingsAccessor, final WorkflowHelper workflowHelper) {
        this.globalConfigurationAccessor = jiraSettingsAccessor.createGlobalConfigurationAccessor();
        this.workflowHelper = workflowHelper;
    }

    public BlackDuckJiraConfigSerializable getMappings() {
        final PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();

        final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();
        final String blackDuckProjectMappingsJson = issueCreationConfig.getProjectMapping().getMappingsJson();
        txConfig.setHubProjectMappingsJson(blackDuckProjectMappingsJson);

        addWorkflowStatusToMappings(txConfig);
        validateMapping(txConfig);
        return txConfig;
    }

    public void validateMapping(final BlackDuckJiraConfigSerializable config) {
        if (config.getHubProjectMappings() != null && !config.getHubProjectMappings().isEmpty()) {
            boolean hasEmptyMapping = false;
            boolean isPatternValid = true;
            for (final BlackDuckProjectMapping mapping : config.getHubProjectMappings()) {
                boolean jiraProjectBlank = true;
                boolean blackDuckProjectBlank = true;
                if (mapping.getJiraProject() != null) {
                    if (mapping.getJiraProject().getProjectId() != null) {
                        jiraProjectBlank = false;
                    }
                }
                final String blackDuckProjectName = mapping.getBlackDuckProjectName();
                if (StringUtils.isNotBlank(blackDuckProjectName)) {
                    blackDuckProjectBlank = false;
                }
                if (jiraProjectBlank || blackDuckProjectBlank) {
                    hasEmptyMapping = true;
                }
                if (isPatternValid && mapping.isProjectPattern()) {
                    isPatternValid = isPatternValid(blackDuckProjectName);
                }
            }
            if (hasEmptyMapping) {
                addError(config, JiraConfigErrorStrings.MAPPING_HAS_EMPTY_ERROR);
            }
            if (!isPatternValid) {
                addError(config, JiraConfigErrorStrings.BLACKDUCK_PROJECT_PATTERN_INVALID);
            }
        }
    }

    private void addWorkflowStatusToMappings(final BlackDuckJiraConfigSerializable config) {
        final Set<BlackDuckProjectMapping> projectMappings = config.getHubProjectMappings();
        if (projectMappings != null) {
            for (final BlackDuckProjectMapping mapping : projectMappings) {
                final JiraProject jiraProject = mapping.getJiraProject();

                final EnumSet<BlackDuckWorkflowStatus> projectWorkflowStatus = workflowHelper.getBlackDuckWorkflowStatus(jiraProject.getProjectId());
                try {
                    final String status = BlackDuckWorkflowStatus.getPrettyListNames(projectWorkflowStatus);
                    jiraProject.setWorkflowStatus(status);
                } catch (final JiraException e) {
                    logger.error(e.getMessage());
                    jiraProject.setWorkflowStatus("ERROR");
                }
            }
        }
    }

    private void addError(final BlackDuckJiraConfigSerializable config, final String messages) {
        config.setHubProjectMappingError(StringUtils.joinWith(" : ", config.getHubProjectMappingError(), messages));
    }

    private boolean isPatternValid(final String pattern) {
        if (StringUtils.isNotBlank(pattern)) {
            try {
                Pattern.compile(pattern);
                return true;
            } catch (final PatternSyntaxException e) {
            }
        }
        return false;
    }

}
