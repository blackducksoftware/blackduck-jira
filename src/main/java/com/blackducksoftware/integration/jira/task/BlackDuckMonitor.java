/**
 * Black Duck JIRA Plugin
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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckPluginDateFormatter;
import com.blackducksoftware.integration.jira.config.PluginConfigKeys;
import com.blackducksoftware.integration.jira.config.PluginConfigurationDetails;
import com.blackducksoftware.integration.jira.task.thread.PluginExecutorService;

public class BlackDuckMonitor implements NotificationMonitor, LifecycleAware, DisposableBean {
    public static final String KEY_CONFIGURED_INTERVAL_MINUTES = BlackDuckMonitor.class.getName() + ":configuredIntervalMinutes";

    private static final long DEFAULT_INTERVAL_MILLISEC = 1000L;
    private static final String CURRENT_JOB_NAME = BlackDuckMonitor.class.getName() + ":job";
    private static final String V1_JOB_NAME = "com.blackducksoftware.integration.jira.impl.HubMonitor:job";
    private static final String V3_JOB_NAME = "com.blackducksoftware.integration.jira.task.HubMonitor:job";

    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final SchedulerService schedulerService;
    private final PluginScheduler pluginSchedulerDeprecated;
    private final PluginSettings pluginSettings;
    private final PluginExecutorService executorService;

    @Inject
    public BlackDuckMonitor(final SchedulerService schedulerService, final PluginScheduler pluginSchedulerDeprecated, final PluginSettingsFactory pluginSettingsFactory, final PluginExecutorService executorService) {
        logger.trace(BlackDuckMonitor.class.getName() + " ctor called.");
        this.schedulerService = schedulerService;
        this.pluginSchedulerDeprecated = pluginSchedulerDeprecated;
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
        this.executorService = executorService;

        schedulerService.registerJobRunner(BlackDuckJobRunner.JOB_RUNNER_KEY, new BlackDuckJobRunner(pluginSettings, executorService));
    }

    @Override
    public void onStart() {
        logger.trace(BlackDuckMonitor.class.getName() + " onStart() called.");
        updateInstallDate(new Date());
        if (executorService.isShutdown()) {
            executorService.restart();
        }
        reschedule(0L);
    }

    @Override
    public void onStop() {
        cleanup();
    }

    public void changeInterval() {
        logger.trace(BlackDuckMonitor.class.getName() + " changeInterval() called.");
        reschedule(0L);
    }

    @Override
    public void reschedule(final long intervalIgnored) {
        logger.trace(BlackDuckMonitor.class.getName() + " reschedule() called.");

        unscheduleOldJobs();

        final long actualInterval = getIntervalMillisec();
        final Schedule schedule;
        try {
            final String installDateString = getInstallDateString();
            schedule = Schedule.forInterval(actualInterval, BlackDuckPluginDateFormatter.parse(installDateString));
        } catch (final Exception e) {
            logger.error("Could not get the install date. Please disable, and then reenable, this plugin or restart Jira.", e);
            return;
        }

        final PluginConfigurationDetails configDetails = new PluginConfigurationDetails(pluginSettings);
        final int configuredIntervalMinutes = configDetails.getIntervalMinutes();

        final HashMap<String, Serializable> blackDuckJobRunnerProperties = new HashMap<>();
        blackDuckJobRunnerProperties.put(KEY_CONFIGURED_INTERVAL_MINUTES, new Integer(configuredIntervalMinutes));

        final JobConfig jobConfig = JobConfig
                                        .forJobRunnerKey(BlackDuckJobRunner.JOB_RUNNER_KEY)
                                        .withRunMode(RunMode.RUN_LOCALLY)
                                        .withParameters(blackDuckJobRunnerProperties)
                                        .withSchedule(schedule);

        try {
            schedulerService.scheduleJob(JobId.of(CURRENT_JOB_NAME), jobConfig);
            logger.info(String.format("%s scheduled to run every %dms", BlackDuckJobRunner.HUMAN_READABLE_TASK_NAME, actualInterval));
        } catch (final SchedulerServiceException e) {
            logger.error(String.format("Could not schedule %s." + BlackDuckJobRunner.HUMAN_READABLE_TASK_NAME), e);
        }
    }

    public String getName() {
        logger.trace(BlackDuckMonitor.class.getName() + ".getName() called");
        if (pluginSchedulerDeprecated != null) {
            return "blackDuckMonitor with pluginScheduler:" + pluginSchedulerDeprecated.toString();
        }

        return "blackDuckMonitor";
    }

    @Override
    public void destroy() throws Exception {
        cleanup();
    }

    private void cleanup() {
        logger.debug(BlackDuckMonitor.class.getName() + ".cleanup() called; Unscheduling " + CURRENT_JOB_NAME);
        schedulerService.unscheduleJob(JobId.of(CURRENT_JOB_NAME));

        final String installDate = getInstallDateString();
        logger.debug("Install date was: " + installDate);
        logger.debug("Removing install date...");
        final Object removedSetting = pluginSettings.remove(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME);
        if (removedSetting != null) {
            logger.debug("Successfully removed install date.");
        } else {
            logger.debug("Failed to remove install date.");
        }
        try {
            executorService.shutdown();
        } catch (final Exception e) {
            logger.warn("Failed to properly shutdown the Black Duck threadManager: " + e.getMessage());
        }
    }

    private void unscheduleOldJobs() {
        try {
            pluginSchedulerDeprecated.unscheduleJob(V1_JOB_NAME);
            logger.debug("Unscheduled job " + V1_JOB_NAME);
        } catch (final Exception e) {
            logger.debug("Job " + V1_JOB_NAME + " wasn't scheduled");
        }
        try {
            pluginSchedulerDeprecated.unscheduleJob(V3_JOB_NAME);
            logger.debug("Unscheduled job " + V3_JOB_NAME);
        } catch (final Exception e) {
            logger.debug("Job " + V3_JOB_NAME + " wasn't scheduled");
        }
        try {
            schedulerService.unscheduleJob(JobId.of(CURRENT_JOB_NAME));
        } catch (final Exception e) {
            logger.debug("Job " + CURRENT_JOB_NAME + " wasn't scheduled");
        }
    }

    private long getIntervalMillisec() {
        if (pluginSettings == null) {
            logger.error("Unable to get plugin settings");
            return DEFAULT_INTERVAL_MILLISEC;
        }
        final String intervalString = (String) pluginSettings.get(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
        if (intervalString == null) {
            logger.error("Unable to get interval from plugin settings");
            return DEFAULT_INTERVAL_MILLISEC;
        }
        int intervalMinutes;
        try {
            intervalMinutes = Integer.parseInt(intervalString);
        } catch (final NumberFormatException e) {
            logger.error("Unable to convert interval string '" + intervalString + "' to an integer");
            return DEFAULT_INTERVAL_MILLISEC;
        }
        if (intervalMinutes < 1) {
            logger.warn("Invalid interval string; setting interval to 1 minute");
            intervalMinutes = 1;
        }
        logger.info("Interval in minutes: " + intervalMinutes);
        // Lop off 30 seconds to give the task room to run. Otherwise, the runtime of the task pushes
        // the next scheduled runtime out beyond the targeted once-a-minute opportunity to run
        final long intervalSeconds = (intervalMinutes * 60) - 30l;
        final long intervalMillisec = intervalSeconds * 1000;
        return intervalMillisec;
    }

    private void updateInstallDate(final Date installDate) {
        final String installDateString = BlackDuckPluginDateFormatter.format(installDate);

        logger.debug("Updating install date...");
        final String oldInstallDate = (String) pluginSettings.put(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME, installDateString);
        logger.debug("The previous install date was: " + oldInstallDate);

        final String newInstallDate = getInstallDateString();
        logger.debug("The new install date is: " + newInstallDate);
    }

    private String getInstallDateString() {
        return (String) pluginSettings.get(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME);
    }

}
