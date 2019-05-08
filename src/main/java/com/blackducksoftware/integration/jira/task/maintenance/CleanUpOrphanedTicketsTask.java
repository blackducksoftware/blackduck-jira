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
package com.blackducksoftware.integration.jira.task.maintenance;

import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.JiraUserContext;
import com.blackducksoftware.integration.jira.common.settings.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.common.settings.PluginConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.model.GeneralIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.task.issue.handler.JiraIssueServiceWrapper;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;

public class CleanUpOrphanedTicketsTask implements Callable<String> {
    private static final Long MAX_BATCH_SIZE = 100L;
    private static final Long MAX_BATCHES_PER_RUN = 10L;
    private static final String DEFAULT_STATUS_MESSAGE = "COMPLETED";
    private static final String ERROR_STATUS_MESSAGE = "ERROR";

    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final JiraSettingsAccessor jiraSettingsAccessor;
    private final JiraServices jiraServices;

    public CleanUpOrphanedTicketsTask(final JiraSettingsAccessor jiraSettingsAccessor) {
        this.jiraSettingsAccessor = jiraSettingsAccessor;
        this.jiraServices = new JiraServices();
    }

    @Override
    public String call() throws Exception {
        final PluginConfigurationAccessor pluginConfigurationAccessor = jiraSettingsAccessor.createPluginConfigurationAccessor();
        final GlobalConfigurationAccessor globalConfigurationAccessor = jiraSettingsAccessor.createGlobalConfigurationAccessor();
        final GeneralIssueCreationConfigModel generalIssueConfig = globalConfigurationAccessor.getIssueCreationConfig().getGeneral();
        final Optional<JiraUserContext> optionalJiraUserContext = JiraUserContext.create(logger, pluginConfigurationAccessor.getJiraAdminUser(), generalIssueConfig.getDefaultIssueCreator(), jiraServices.getUserManager());

        final JiraUserContext jiraUserContext;
        if (optionalJiraUserContext.isPresent()) {
            jiraUserContext = optionalJiraUserContext.get();
        } else {
            logger.error("No (valid) user in configuration data; The plugin has likely not yet been configured; The task cannot run (yet)");
            return ERROR_STATUS_MESSAGE;
        }

        final JiraIssueServiceWrapper issueServiceWrapper = JiraIssueServiceWrapper.createIssueServiceWrapperFromJiraServices(jiraServices, jiraUserContext, new Gson(), ImmutableMap.of());

        // TODO Implement the actual logic

        return DEFAULT_STATUS_MESSAGE;
    }

}
