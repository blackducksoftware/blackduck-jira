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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.scheduler.config.JobConfig;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.task.thread.PluginExecutorService;

public class BlackDuckJobRunnerUtil {
    private final BlackDuckJiraLogger logger;
    private final PluginExecutorService executorService;
    private final String taskName;

    public BlackDuckJobRunnerUtil(final BlackDuckJiraLogger logger, final PluginExecutorService executorService, final String taskName) {
        this.logger = logger;
        this.executorService = executorService;
        this.taskName = taskName;
    }

    public JobRunnerResponse runJob(final JobRunnerRequest request, final Callable<String> actualTask) {
        try {
            logger.info("Running the blackduck-jira " + taskName + " task.");
            final JobConfig jobConfig = request.getJobConfig();
            final JobRunnerResponse result = executeTimedTask(jobConfig, actualTask);
            logger.info("The blackduck-jira " + taskName + " task has completed.");
            return result;
        } catch (final Exception e) {
            logger.error("An error occurred in the Black Duck Job Runner", e);
            return JobRunnerResponse.failed(e);
        }
    }

    private JobRunnerResponse executeTimedTask(final JobConfig jobConfig, final Callable<String> actualTask) {
        final Map<String, Serializable> parameterMap = jobConfig.getParameters();

        final Number configuredIntervalMinutes = (Number) parameterMap.get(BlackDuckMonitor.KEY_CONFIGURED_INTERVAL_MINUTES);
        if (configuredIntervalMinutes != null) {
            final int taskIntervalMinutes = configuredIntervalMinutes.intValue();
            logger.debug("Task interval (minutes): " + taskIntervalMinutes);
            if (taskIntervalMinutes < 1) {
                logger.info("The blackduck-jira " + taskName + " task has not been configured, or has a run interval < 1 minute");
                return JobRunnerResponse.aborted("The plugin has not been configured correctly.");
            }

            final int taskTimeoutMinutes = BlackDuckJiraConstants.PERIODIC_TASK_TIMEOUT_AS_MULTIPLE_OF_INTERVAL * taskIntervalMinutes;
            logger.debug("Task timeout (minutes): " + taskTimeoutMinutes);

            if (executorService.canAcceptNewTasks()) {
                return scheduleTask(logger, actualTask, taskTimeoutMinutes);
            }
        } else {
            logger.warn("No task interval was configured.");
        }
        return JobRunnerResponse.aborted("Too many tasks are currently scheduled.");
    }

    private JobRunnerResponse scheduleTask(final BlackDuckJiraLogger logger, final Callable<String> actualTask, final int taskTimeoutMinutes) {
        String reasonForFailure = null;
        Exception failureException = null;
        PluginExecutorService.PluginFuture future = null;
        try {
            future = executorService.submit(actualTask);
            final String result = future.get(taskTimeoutMinutes);
            logger.info("The blackduck-jira " + taskName + " task completed with result: " + result);
        } catch (final ExecutionException e) {
            reasonForFailure = "The blackduck-jira " + taskName + " task threw an error: " + e.getMessage();
            failureException = e;
        } catch (final TimeoutException e) {
            reasonForFailure = "The blackduck-jira " + taskName + " task timed out";
        } catch (final InterruptedException e) {
            // It is bad practice to catch an InterruptedException without interrupting the Thread.
            logger.error("The blackduck-jira " + taskName + " task was interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            if (future != null && !future.isDone()) {
                future.cancel();
            }
        }

        if (reasonForFailure != null) {
            logger.error(reasonForFailure);
            if (failureException != null) {
                logger.error(failureException);
            }
            return JobRunnerResponse.failed(reasonForFailure);
        }
        return JobRunnerResponse.success();
    }

}
