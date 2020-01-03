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
package com.blackducksoftware.integration.jira.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.blackducksoftware.integration.jira.data.accessor.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.task.thread.PluginExecutorService;
import com.blackducksoftware.integration.jira.web.BlackDuckPluginVersion;
import com.blackducksoftware.integration.jira.web.JiraServices;

public class BlackDuckJobRunner implements JobRunner {
    public static final String HUMAN_READABLE_TASK_NAME = "Black Duck notification check task";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final JiraSettingsAccessor jiraSettingsAccessor;
    private final PluginExecutorService executorService;

    public BlackDuckJobRunner(final PluginSettings pluginSettings, final PluginExecutorService executorService) {
        this.jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettings);
        this.executorService = executorService;
    }

    @Override
    public JobRunnerResponse runJob(final JobRunnerRequest request) {
        final JiraTaskTimed jiraTaskTimed = new JiraTaskTimed(jiraSettingsAccessor, new JiraServices());

        logger.info("blackduck-jira plugin version: " + BlackDuckPluginVersion.getVersion());
        final BlackDuckJobRunnerUtil blackDuckJobRunnerUtil = new BlackDuckJobRunnerUtil(executorService, "periodic");
        return blackDuckJobRunnerUtil.runJob(request, jiraTaskTimed);
    }

}
