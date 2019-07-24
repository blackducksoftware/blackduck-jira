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
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.atlassian.scheduler.config.JobConfig;
import com.atlassian.scheduler.config.JobId;
import com.atlassian.scheduler.config.JobRunnerKey;
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckPluginDateFormatter;
import com.blackducksoftware.integration.jira.dal.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.dal.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.dal.PluginConfigKeys;
import com.blackducksoftware.integration.jira.dal.model.GeneralIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.dal.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.task.maintenance.BlackDuckMaintenanceJobRunner;
import com.blackducksoftware.integration.jira.task.thread.PluginExecutorService;
import com.blackducksoftware.integration.jira.workflow.setup.UpgradeSteps;

public class BlackDuckMonitor implements NotificationMonitor, LifecycleAware {
    public static final String KEY_CONFIGURED_INTERVAL_MINUTES = BlackDuckMonitor.class.getName() + ":configuredIntervalMinutes";

    private static final int DEFAULT_INTERVAL_MINUTES = 1;
    private static final String PRIMARY_JOB_NAME = BlackDuckMonitor.class.getName() + ":job";
    private static final String MAINTENANCE_JOB_NAME = BlackDuckMonitor.class.getName() + ":maintenance-job";
    private static final JobRunnerKey PRIMARY_JOB_RUNNER_KEY = JobRunnerKey.of(PRIMARY_JOB_NAME);
    private static final JobRunnerKey MAINTENANCE_JOB_RUNNER_KEY = JobRunnerKey.of(MAINTENANCE_JOB_NAME);

    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final SchedulerService schedulerService;
    private final PluginSettings pluginSettings;
    private final PluginExecutorService executorService;

    @Inject
    public BlackDuckMonitor(final SchedulerService schedulerService, final PluginSettingsFactory pluginSettingsFactory, final PluginExecutorService executorService) {
        logger.trace(BlackDuckMonitor.class.getName() + " ctor called.");
        this.schedulerService = schedulerService;
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
        this.executorService = executorService;

        schedulerService.registerJobRunner(PRIMARY_JOB_RUNNER_KEY, new BlackDuckJobRunner(pluginSettings, executorService));
        schedulerService.registerJobRunner(MAINTENANCE_JOB_RUNNER_KEY, new BlackDuckMaintenanceJobRunner(pluginSettings));
    }

    @Override
    public void onStart() {
        logger.trace(BlackDuckMonitor.class.getName() + " onStart() called.");
        runUpgrade(new Date());
        if (executorService.isShutdown()) {
            executorService.restart();
        }
        reschedule(0L);
    }

    @Override
    public void onStop() {
        logger.debug(BlackDuckMonitor.class.getName() + ".onStop() called");
        unscheduleOldJobs();

        final String installDate = UpgradeSteps.getInstallDateString(pluginSettings);
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

    public void changeInterval() {
        logger.trace(BlackDuckMonitor.class.getName() + " changeInterval() called.");
        reschedule(0L);
    }

    @Override
    public void reschedule(final long intervalIgnored) {
        logger.trace(BlackDuckMonitor.class.getName() + " reschedule() called.");

        unscheduleOldJobs();

        final Number primaryTaskIntervalInMinutes = getIntervalInMinutes();
        final Number primaryTaskIntervalInMilliseconds = getIntervalMillisec(primaryTaskIntervalInMinutes);

        final Schedule primaryTaskSchedule;
        try {
            final String installDateString = UpgradeSteps.getInstallDateString(pluginSettings);
            final Date installDate = new BlackDuckPluginDateFormatter().parse(installDateString);
            primaryTaskSchedule = Schedule.forInterval(primaryTaskIntervalInMilliseconds.longValue(), installDate);
        } catch (final Exception e) {
            logger.error("Could not get the install date. Please disable, and then re-enable, this plugin or restart Jira.", e);
            return;
        }

        // Schedule primary task
        scheduleJob(PRIMARY_JOB_NAME, BlackDuckJobRunner.HUMAN_READABLE_TASK_NAME, primaryTaskSchedule, primaryTaskIntervalInMinutes, primaryTaskIntervalInMilliseconds);

        // Schedule maintenance task
        final Schedule maintenanceTaskSchedule = Schedule.forCronExpression(BlackDuckMaintenanceJobRunner.DEFAULT_ATLASSIAN_CRON_EXPRESSION);
        final Number maintenanceTaskIntervalInMinutes = 60;
        final Number maintenanceTaskIntervalInMilliseconds = getIntervalMillisec(maintenanceTaskIntervalInMinutes);

        scheduleJob(MAINTENANCE_JOB_NAME, BlackDuckMaintenanceJobRunner.HUMAN_READABLE_TASK_NAME, maintenanceTaskSchedule, maintenanceTaskIntervalInMinutes, maintenanceTaskIntervalInMilliseconds);
    }

    public String getName() {
        logger.trace(BlackDuckMonitor.class.getName() + ".getName() called");
        return "blackDuckMonitor";
    }

    private void scheduleJob(final String jobName, final String humanReadableTaskName, final Schedule schedule, final Number intervalInMinutes, final Number intervalInMilliseconds) {
        final HashMap<String, Serializable> blackDuckJobRunnerProperties = new HashMap<>();
        blackDuckJobRunnerProperties.put(KEY_CONFIGURED_INTERVAL_MINUTES, intervalInMinutes);

        final JobRunnerKey jobRunnerKey = JobRunnerKey.of(jobName);
        final JobConfig jobConfig = JobConfig
                                        .forJobRunnerKey(jobRunnerKey)
                                        .withRunMode(RunMode.RUN_LOCALLY)
                                        .withParameters(blackDuckJobRunnerProperties)
                                        .withSchedule(schedule);
        try {
            final JobId jobId = JobId.of(jobName);
            schedulerService.scheduleJob(jobId, jobConfig);
            logger.info(String.format("%s scheduled to run every %sms", humanReadableTaskName, intervalInMilliseconds));
        } catch (final SchedulerServiceException e) {
            logger.error(String.format("Could not schedule %s.", humanReadableTaskName), e);
        }
    }

    private void unscheduleOldJobs() {
        try {
            schedulerService.unscheduleJob(JobId.of(PRIMARY_JOB_NAME));
        } catch (final Exception e) {
            logger.debug("Job " + PRIMARY_JOB_NAME + " wasn't scheduled");
        }
        try {
            schedulerService.unscheduleJob(JobId.of(MAINTENANCE_JOB_NAME));
        } catch (final Exception e) {
            logger.debug("Job " + MAINTENANCE_JOB_NAME + " wasn't scheduled");
        }
    }

    private Number getIntervalMillisec(final Number intervalInMinutes) {
        if (null != intervalInMinutes) {
            int intervalMinutes = intervalInMinutes.intValue();
            if (intervalMinutes < 1) {
                logger.warn("Invalid interval string; setting interval to " + DEFAULT_INTERVAL_MINUTES + " minute(s)");
                intervalMinutes = DEFAULT_INTERVAL_MINUTES;
            }
            logger.info("Interval in minutes: " + intervalMinutes);
            // Lop off 30 seconds to give the task room to run. Otherwise, the runtime of the task pushes
            // the next scheduled runtime out beyond the targeted once-a-minute opportunity to run
            final long intervalSeconds = (intervalMinutes * 60L) - 30L;
            return intervalSeconds * 1000L;
        }
        return DEFAULT_INTERVAL_MINUTES * 60 * 1000;
    }

    private void runUpgrade(final Date installDate) {
        // Unregister old JobRunner
        final JobRunnerKey oldJobRunnerKey_6_0_0 = JobRunnerKey.of("com.blackducksoftware.integration.jira.task.BlackDuckJobRunner");
        schedulerService.unregisterJobRunner(oldJobRunnerKey_6_0_0);
        reschedule(1);

        final UpgradeSteps upgradeSteps = new UpgradeSteps(logger, pluginSettings);
        upgradeSteps.updateInstallDate(installDate);
        upgradeSteps.updateOldMappingsIfNeeded();
        upgradeSteps.upgradeToV6FromAny();
        upgradeSteps.assignUserToBlackDuckProject();
    }

    private Number getIntervalInMinutes() {
        if (pluginSettings == null) {
            logger.error("Unable to get plugin settings");
            return DEFAULT_INTERVAL_MINUTES;
        }

        final JiraSettingsAccessor jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettings);
        final GlobalConfigurationAccessor globalConfigurationAccessor = new GlobalConfigurationAccessor(jiraSettingsAccessor);
        final PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();
        final GeneralIssueCreationConfigModel generalConfig = issueCreationConfig.getGeneral();

        final Optional<Integer> optionalInterval = generalConfig.getInterval();
        if (optionalInterval.isPresent()) {
            return optionalInterval.get();
        }

        logger.error("Unable to get interval from plugin settings");
        return DEFAULT_INTERVAL_MINUTES;
    }

}
