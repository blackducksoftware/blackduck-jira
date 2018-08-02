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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConfigKeys;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.rest.RestConstants;

public class BlackDuckMonitor implements NotificationMonitor, LifecycleAware, DisposableBean {
    private static final long DEFAULT_INTERVAL_MILLISEC = 1000L;
    /* package */static final String KEY_INSTANCE = BlackDuckMonitor.class.getName() + ":instance";
    public static final String KEY_SETTINGS = BlackDuckMonitor.class.getName() + ":settings";
    public static final String KEY_EXECUTOR = BlackDuckMonitor.class.getName() + ":executor";
    public static final String KEY_SCHEDULED_TASK_LIST = BlackDuckMonitor.class.getName() + ":scheduledTasks";
    private static final String JOB_NAME = BlackDuckMonitor.class.getName() + ":job";
    private static final String V1_JOB_NAME = "com.blackducksoftware.integration.jira.impl.HubMonitor:job";
    private static final String V3_JOB_NAME = "com.blackducksoftware.integration.jira.task.HubMonitor:job";

    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    private final PluginScheduler pluginScheduler; // provided by SAL
    private final PluginSettings pluginSettings;
    private ExecutorService executor;

    @Inject
    public BlackDuckMonitor(final PluginScheduler pluginScheduler, final PluginSettingsFactory pluginSettingsFactory) {
        logger.trace("BlackDuckMonitor ctor called.");
        this.pluginScheduler = pluginScheduler;
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
    }

    @Override
    public void onStart() {
        logger.trace("BlackDuckMonitor onStart() called.");
        this.executor = Executors.newSingleThreadExecutor();
        updateInstallDate();
        reschedule(0L);
    }

    public void changeInterval() {
        logger.trace("BlackDuckMonitor changeInterval() called.");
        reschedule(0L);
    }

    @Override
    public void reschedule(final long intervalIgnored) {
        logger.trace("BlackDuckMonitor reschedule() called.");

        final long actualInterval = getIntervalMillisec();

        try {
            pluginScheduler.unscheduleJob(V1_JOB_NAME);
            logger.debug("Unscheduled job " + V1_JOB_NAME);
        } catch (final Exception e) {
            logger.debug("Job " + V1_JOB_NAME + " wasn't scheduled");
        }
        try {
            pluginScheduler.unscheduleJob(V3_JOB_NAME);
            logger.debug("Unscheduled job " + V3_JOB_NAME);
        } catch (final Exception e) {
            logger.debug("Job " + V3_JOB_NAME + " wasn't scheduled");
        }
        try {
            pluginScheduler.unscheduleJob(JOB_NAME);
            logger.debug("Unscheduled job " + JOB_NAME);
        } catch (final Exception e) {
            logger.debug("Job " + JOB_NAME + " wasn't scheduled");
        }
        final HashMap<String, Object> classProperties = new HashMap<>();
        classProperties.put(KEY_INSTANCE, BlackDuckMonitor.this);
        classProperties.put(KEY_SETTINGS, pluginSettings);
        classProperties.put(KEY_EXECUTOR, executor);
        classProperties.put(KEY_SCHEDULED_TASK_LIST, new ArrayList<Future<String>>());
        pluginScheduler.scheduleJob(JOB_NAME, // unique name of the job
                JiraTask.class, // class of the job
                classProperties, // data that needs to be passed to the job
                new Date(), // the time the job is to start
                actualInterval); // interval between repeats, in milliseconds
        logger.info(String.format("Hub Notification check task scheduled to run every %dms", actualInterval));
    }

    public String getName() {
        logger.trace("BlackDuckMonitor.getName() called");
        if (pluginScheduler != null) {
            return "blackDuckMonitor with pluginScheduler:" + pluginScheduler.toString();
        }

        return "blackDuckMonitor";
    }

    private long getIntervalMillisec() {
        if (pluginSettings == null) {
            logger.error("Unable to get plugin settings");
            return DEFAULT_INTERVAL_MILLISEC;
        }
        final String intervalString = (String) pluginSettings.get(BlackDuckJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
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

    @Override
    public void destroy() throws Exception {
        logger.debug("destroy() called; Unscheduling " + JOB_NAME);
        pluginScheduler.unscheduleJob(JOB_NAME);

        final String installDate = (String) pluginSettings.get(BlackDuckJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME);
        logger.debug("Install date was: " + installDate);
        logger.debug("Removing install date...");
        final Object removedSetting = pluginSettings.remove(BlackDuckJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME);
        if (removedSetting != null) {
            logger.debug("Successfully removed install date.");
        } else {
            logger.debug("Failed to remove install date.");
        }
        try {
            logger.debug("Shutting down executor service.");
            executor.shutdown();
        } catch (final SecurityException e) {
            logger.error(e);
        }
    }

    private void updateInstallDate() {
        final SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConstants.JSON_DATE_FORMAT);
        dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
        final String installDate = dateFormatter.format(new Date());

        logger.debug("Updating install date...");
        final String oldInstallDate = (String) pluginSettings.put(BlackDuckJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME, installDate);
        logger.debug("The previous install date was: " + oldInstallDate);

        final String newInstallDate = (String) pluginSettings.get(BlackDuckJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME);
        logger.debug("The new install date is: " + newInstallDate);
    }

}
