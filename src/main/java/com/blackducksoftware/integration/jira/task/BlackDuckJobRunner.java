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
package com.blackducksoftware.integration.jira.task;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.scheduler.JobRunner;
import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.blackducksoftware.integration.jira.BlackDuckPluginVersion;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.task.thread.PluginExecutorService;
import com.blackducksoftware.integration.jira.task.thread.PluginExecutorService.PluginFuture;

public class BlackDuckJobRunner implements JobRunner {
    public static final JobRunnerKey JOB_RUNNER_KEY = JobRunnerKey.of(BlackDuckJobRunner.class.getName());
    public static final String HUMAN_READABLE_TASK_NAME = "Black Duck notification check task";

    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final PluginSettings pluginSettings;
    private final PluginExecutorService executorService;

    public BlackDuckJobRunner(final PluginSettings pluginSettings, final PluginExecutorService executorService) {
        this.pluginSettings = pluginSettings;
        this.executorService = executorService;
    }

    @Override
    public JobRunnerResponse runJob(final JobRunnerRequest request) {
        try {
            logger.info("Running the blackduck-jira periodic task.");
            logger.info("blackduck-jira plugin version: " + BlackDuckPluginVersion.getVersion());
            final JobConfig jobConfig = request.getJobConfig();
            final JobRunnerResponse result = executeTimedTask(jobConfig);
            logger.info("The blackduck-jira periodic task has completed.");
            return result;
        } catch (final Exception e) {
            return JobRunnerResponse.failed(e);
        }
    }

    private JobRunnerResponse executeTimedTask(final JobConfig jobConfig) {
        final Map<String, Serializable> parameterMap = jobConfig.getParameters();

        final Integer taskIntervalMinutes = (Integer) parameterMap.get(BlackDuckMonitor.KEY_CONFIGURED_INTERVAL_MINUTES);
        if (taskIntervalMinutes != null) {
            logger.debug("Task interval (minutes): " + taskIntervalMinutes);
            if (taskIntervalMinutes < 1) {
                logger.info("blackduck-jira periodic task has not been configured, or has a run interval < 1 minute");
                return JobRunnerResponse.aborted("The plugin has not been configured correctly.");
            }
        }

        final int taskTimeoutMinutes = BlackDuckJiraConstants.PERIODIC_TASK_TIMEOUT_AS_MULTIPLE_OF_INTERVAL * taskIntervalMinutes.intValue();
        logger.debug("Task timeout (minutes): " + taskTimeoutMinutes);

        if (executorService.canAcceptNewTasks()) {
            return scheduleTask(taskIntervalMinutes, taskTimeoutMinutes);
        }
        return JobRunnerResponse.aborted("Too many tasks are currently scheduled.");
    }

    private JobRunnerResponse scheduleTask(final Integer taskIntervalMinutes, final int taskTimeoutMinutes) {
        String reasonForFailure = null;
        Exception failureException = null;
        PluginFuture future = null;
        try {
            future = executorService.submit(new JiraTaskTimed(pluginSettings, new JiraServices(), taskIntervalMinutes));
            final String result = future.get(taskTimeoutMinutes);
            logger.info("The timed task completed with result: " + result);
        } catch (final ExecutionException e) {
            reasonForFailure = "The timed task threw an error: " + e.getMessage();
            failureException = e;
        } catch (final TimeoutException e) {
            reasonForFailure = "The timed task timed out";
        } catch (final InterruptedException e) {
            // Since the exception was caught, the thread hasn't been interrupted yet; it will be manually interrupted (by future.cancel()) in the finally block.
            reasonForFailure = "The timed task was interrupted";
        } finally {
            if (future != null && !future.isDone()) {
                future.cancel();
            }
            if (reasonForFailure != null) {
                logger.error(reasonForFailure);
                if (failureException != null) {
                    logger.error(failureException);
                }
                return JobRunnerResponse.failed(reasonForFailure);
            }
        }
        return JobRunnerResponse.success();
    }

}
