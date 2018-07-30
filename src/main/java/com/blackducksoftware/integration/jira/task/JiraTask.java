/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.PluginVersion;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.PluginConfigurationDetails;

/**
 * A scheduled JIRA task that collects recent notifications from the Hub, and generates JIRA tickets for them.
 *
 * @author sbillings
 *
 */
public class JiraTask implements PluginJob {
    public static final int MAX_QUEUED_TASKS = HubJiraConstants.PERIODIC_TASK_TIMEOUT_AS_MULTIPLE_OF_INTERVAL + 1;
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    public JiraTask() {
    }

    @Override
    public void execute(final Map<String, Object> jobDataMap) {
        logger.info("Running the Hub JIRA task.");
        logger.info("hub-jira plugin version: " + PluginVersion.getVersion());
        final PluginSettings settings = (PluginSettings) jobDataMap.get(HubMonitor.KEY_SETTINGS);
        final ExecutorService executor = (ExecutorService) jobDataMap.get(HubMonitor.KEY_EXECUTOR);
        final List<Future<String>> scheduledTasks = (List<Future<String>>) jobDataMap.get(HubMonitor.KEY_SCHEDULED_TASK_LIST);
        final PluginConfigurationDetails configDetails = new PluginConfigurationDetails(settings);
        final JiraSettingsService jiraSettingsService = new JiraSettingsService(settings);

        final int taskIntervalMinutes = configDetails.getIntervalMinutes();
        logger.debug("Task interval (minutes): " + taskIntervalMinutes);
        if (taskIntervalMinutes < 1) {
            logger.info("hub-jira periodic task has not been configured, or has a run interval < 1 minute");
            return;
        }
        final int taskTimeoutMinutes = HubJiraConstants.PERIODIC_TASK_TIMEOUT_AS_MULTIPLE_OF_INTERVAL * taskIntervalMinutes;

        logger.debug("Task timeout (minutes): " + taskTimeoutMinutes);

        if (scheduledTasks.size() < MAX_QUEUED_TASKS) {
            final Future<String> future = executor.submit(new JiraTaskTimed(settings, jiraSettingsService, new JiraServices()));
            scheduledTasks.add(future);
            try {
                final String result = future.get(taskTimeoutMinutes, TimeUnit.MINUTES);
                logger.info("The timed task completed with result: " + result);
            } catch (final ExecutionException e) {
                logger.error("The timed task threw an error: " + e.getMessage(), e);
            } catch (final TimeoutException e) {
                logger.error("The timed task timed out");
            } catch (final InterruptedException e) {
                // Since the exception was caught, the thread hasn't been interrupted yet; it will be manually interrupted (by future.cancel(true)) in the finally block.
                logger.error("The timed task was interrupted");
            } finally {
                scheduledTasks.remove(future);
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }
        }
        logger.info("hub-jira periodic task has completed");
    }

}
