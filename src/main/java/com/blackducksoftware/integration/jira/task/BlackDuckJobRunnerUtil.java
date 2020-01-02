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
package com.blackducksoftware.integration.jira.task;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.scheduler.JobRunnerRequest;
import com.atlassian.scheduler.JobRunnerResponse;
import com.atlassian.scheduler.config.JobConfig;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.task.thread.PluginExecutorService;

public class BlackDuckJobRunnerUtil {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final PluginExecutorService executorService;
    private final String taskName;

    public BlackDuckJobRunnerUtil(PluginExecutorService executorService, String taskName) {
        this.executorService = executorService;
        this.taskName = taskName;
    }

    public JobRunnerResponse runJob(JobRunnerRequest request, Callable<String> actualTask) {
        Instant jobStartTime = Instant.now();
        try {
            logger.info("Running the blackduck-jira " + taskName + " task.");
            JobConfig jobConfig = request.getJobConfig();
            JobRunnerResponse result = executeTimedTask(jobConfig, actualTask);
            logger.info("The blackduck-jira " + taskName + " task has completed.");
            return result;
        } catch (Exception e) {
            logger.error("An error occurred in the Black Duck Job Runner. Task: " + taskName, e);
            return JobRunnerResponse.failed(e);
        } finally {
            Instant jobEndTime = Instant.now();
            Duration jobRunTime = Duration.between(jobStartTime, jobEndTime);

            String formattedDuration = DurationFormatUtils.formatDurationHMS(jobRunTime.toMillis());
            logger.debug("blackduck-jira " + taskName + " task took " + formattedDuration);
        }
    }

    private JobRunnerResponse executeTimedTask(JobConfig jobConfig, Callable<String> actualTask) {
        Map<String, Serializable> parameterMap = jobConfig.getParameters();

        Number configuredIntervalMinutes = (Number) parameterMap.get(BlackDuckMonitor.KEY_CONFIGURED_INTERVAL_MINUTES);
        if (configuredIntervalMinutes != null) {
            int taskIntervalMinutes = configuredIntervalMinutes.intValue();
            logger.debug("Task interval (minutes): " + taskIntervalMinutes);
            if (taskIntervalMinutes < 1) {
                logger.info("The blackduck-jira " + taskName + " task has not been configured, or has a run interval < 1 minute");
                return JobRunnerResponse.aborted("The plugin has not been configured correctly.");
            }

            int taskTimeoutMinutes = BlackDuckJiraConstants.PERIODIC_TASK_TIMEOUT_AS_MULTIPLE_OF_INTERVAL * taskIntervalMinutes;
            logger.debug("Task timeout (minutes): " + taskTimeoutMinutes);

            if (executorService.canAcceptNewTasks()) {
                return scheduleTask(actualTask, taskTimeoutMinutes);
            }
        } else {
            logger.warn("No task interval was configured.");
        }
        return JobRunnerResponse.aborted("Too many tasks are currently scheduled.");
    }

    private JobRunnerResponse scheduleTask(Callable<String> actualTask, int taskTimeoutMinutes) {
        String reasonForFailure = null;
        Exception failureException = null;
        PluginExecutorService.PluginFuture future = null;
        try {
            future = executorService.submit(actualTask);
            String result = future.get(taskTimeoutMinutes);
            logger.info("The blackduck-jira " + taskName + " task completed with result: " + result);
        } catch (ExecutionException e) {
            reasonForFailure = "The blackduck-jira " + taskName + " task threw an error: " + e.getMessage();
            failureException = e;
        } catch (TimeoutException e) {
            reasonForFailure = "The blackduck-jira " + taskName + " task timed out";
        } catch (InterruptedException e) {
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
                logger.error("An error occurred while executing a job", failureException);
            }
            return JobRunnerResponse.failed(reasonForFailure);
        }
        return JobRunnerResponse.success();
    }

}
