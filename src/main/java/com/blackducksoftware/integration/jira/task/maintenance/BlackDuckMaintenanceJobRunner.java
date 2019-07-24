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
package com.blackducksoftware.integration.jira.task.maintenance;

import org.apache.log4j.Logger;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.dal.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.task.BlackDuckJobRunnerUtil;
import com.blackducksoftware.integration.jira.task.thread.PluginExecutorService;

public class BlackDuckMaintenanceJobRunner implements JobRunner {
    public static final String HUMAN_READABLE_TASK_NAME = "Black Duck maintenance task";
    public static final String DEFAULT_ATLASSIAN_CRON_EXPRESSION = "0 0 0 * * ? *";

    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final JiraSettingsAccessor jiraSettingsAccessor;

    public BlackDuckMaintenanceJobRunner(final PluginSettings pluginSettings) {
        this.jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettings);
    }

    @Override
    public JobRunnerResponse runJob(final JobRunnerRequest request) {
        PluginExecutorService pluginExecutorService = null;
        try {
            final CleanUpOrphanedTicketsTask cleanUpTask = new CleanUpOrphanedTicketsTask(jiraSettingsAccessor);

            pluginExecutorService = PluginExecutorService.restricted(1);
            final BlackDuckJobRunnerUtil blackDuckJobRunnerUtil = new BlackDuckJobRunnerUtil(logger, pluginExecutorService, "maintenance");
            return blackDuckJobRunnerUtil.runJob(request, cleanUpTask);
        } finally {
            if (null != pluginExecutorService) {
                pluginExecutorService.shutdownNow();
            }
        }
    }

}
