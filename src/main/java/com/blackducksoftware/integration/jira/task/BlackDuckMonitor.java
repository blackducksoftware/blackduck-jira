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
import com.atlassian.scheduler.config.RunMode;
import com.atlassian.scheduler.config.Schedule;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckPluginDateFormatter;
import com.blackducksoftware.integration.jira.common.settings.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.common.settings.PluginConfigKeys;
import com.blackducksoftware.integration.jira.common.settings.model.GeneralIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.task.setup.UpgradeSteps;
import com.blackducksoftware.integration.jira.task.thread.PluginExecutorService;

public class BlackDuckMonitor implements NotificationMonitor, LifecycleAware {
    public static final String KEY_CONFIGURED_INTERVAL_MINUTES = BlackDuckMonitor.class.getName() + ":configuredIntervalMinutes";

    private static final int DEFAULT_INTERVAL_MINUTES = 1;
    private static final String CURRENT_JOB_NAME = BlackDuckMonitor.class.getName() + ":job";

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

        schedulerService.registerJobRunner(BlackDuckJobRunner.JOB_RUNNER_KEY, new BlackDuckJobRunner(pluginSettings, executorService));
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
        logger.debug(BlackDuckMonitor.class.getName() + ".onStop() called; Unscheduling " + CURRENT_JOB_NAME);
        schedulerService.unscheduleJob(JobId.of(CURRENT_JOB_NAME));

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

        final Number actualIntervalInMinutes = getIntervalInMinutes();
        final Number actualIntervalInMilliseconds = getIntervalMillisec(actualIntervalInMinutes);

        final Schedule schedule;
        try {
            final String installDateString = UpgradeSteps.getInstallDateString(pluginSettings);
            schedule = Schedule.forInterval(actualIntervalInMilliseconds.longValue(), BlackDuckPluginDateFormatter.parse(installDateString));
        } catch (final Exception e) {
            logger.error("Could not get the install date. Please disable, and then reenable, this plugin or restart Jira.", e);
            return;
        }

        final HashMap<String, Serializable> blackDuckJobRunnerProperties = new HashMap<>();
        blackDuckJobRunnerProperties.put(KEY_CONFIGURED_INTERVAL_MINUTES, actualIntervalInMinutes);

        final JobConfig jobConfig = JobConfig
                                        .forJobRunnerKey(BlackDuckJobRunner.JOB_RUNNER_KEY)
                                        .withRunMode(RunMode.RUN_LOCALLY)
                                        .withParameters(blackDuckJobRunnerProperties)
                                        .withSchedule(schedule);

        try {
            schedulerService.scheduleJob(JobId.of(CURRENT_JOB_NAME), jobConfig);
            logger.info(String.format("%s scheduled to run every %dms", BlackDuckJobRunner.HUMAN_READABLE_TASK_NAME, actualIntervalInMilliseconds));
        } catch (final SchedulerServiceException e) {
            logger.error(String.format("Could not schedule %s." + BlackDuckJobRunner.HUMAN_READABLE_TASK_NAME), e);
        }
    }

    public String getName() {
        logger.trace(BlackDuckMonitor.class.getName() + ".getName() called");
        return "blackDuckMonitor";
    }

    private void unscheduleOldJobs() {
        try {
            schedulerService.unscheduleJob(JobId.of(CURRENT_JOB_NAME));
        } catch (final Exception e) {
            logger.debug("Job " + CURRENT_JOB_NAME + " wasn't scheduled");
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
        final UpgradeSteps upgradeSteps = new UpgradeSteps(logger, pluginSettings);
        upgradeSteps.updateInstallDate(installDate);
        upgradeSteps.updateOldMappingsIfNeeded();
        upgradeSteps.upgradeToV6FromAny();
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
