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

import java.util.Date;
import java.util.HashMap;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.blackducksoftware.integration.jira.common.HubJiraConfigKeys;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;

public class HubMonitor implements NotificationMonitor, LifecycleAware, DisposableBean {

    private static final long DEFAULT_INTERVAL_MILLISEC = 1000L;

    /* package */static final String KEY_INSTANCE = HubMonitor.class.getName() + ":instance";

    public static final String KEY_SETTINGS = HubMonitor.class.getName() + ":settings";

    private static final String JOB_NAME = HubMonitor.class.getName() + ":job";

    private static final String V1_JOB_NAME = "com.blackducksoftware.integration.jira.impl.HubMonitor:job";

    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final PluginScheduler pluginScheduler; // provided by SAL

    private final PluginSettingsFactory pluginSettingsFactory;

    @Inject
    public HubMonitor(final PluginScheduler pluginScheduler, final PluginSettingsFactory pluginSettingsFactory) {
        logger.debug("HubMonitor ctor called.");
        this.pluginScheduler = pluginScheduler;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public void onStart() {
        logger.debug("HubMonitor onStart() called.");
        reschedule(0L);
    }

    public void changeInterval() {
        logger.debug("HubMonitor changeInterval() called.");
        reschedule(0L);
    }

    @Override
    public void reschedule(final long intervalIgnored) {
        logger.debug("HubMonitor reschedule() called.");

        final long actualInterval = getIntervalMillisec();

        try {
            pluginScheduler.unscheduleJob(V1_JOB_NAME);
            logger.debug("Unscheduled job " + V1_JOB_NAME);
        } catch (final Exception e) {
            logger.debug("Job " + V1_JOB_NAME + " wasn't scheduled");
        }
        try {
            pluginScheduler.unscheduleJob(JOB_NAME);
            logger.debug("Unscheduled job " + JOB_NAME);
        } catch (final Exception e) {
            logger.debug("Job " + JOB_NAME + " wasn't scheduled");
        }
        final HashMap<String, Object> classProperties = new HashMap<>();
        classProperties.put(KEY_INSTANCE, HubMonitor.this);
        classProperties.put(KEY_SETTINGS, pluginSettingsFactory.createGlobalSettings());
        pluginScheduler.scheduleJob(JOB_NAME, // unique name of the job
                JiraTask.class, // class of the job
                classProperties, // data that needs to be passed to the job
                new Date(), // the time the job is to start
                actualInterval); // interval between repeats, in milliseconds
        logger.info(String.format("Hub Notification check task scheduled to run every %dms", actualInterval));
    }

    public String getName() {
        logger.debug("HubMonitor.getName() called");
        if (pluginScheduler != null) {
            return "hubMonitor with pluginScheduler:" + pluginScheduler.toString();
        }

        return "hubMonitor";
    }

    private long getIntervalMillisec() {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        if (settings == null) {
            logger.error("Unable to get plugin settings");
            return DEFAULT_INTERVAL_MILLISEC;
        }
        final String intervalString = (String) settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
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
        // Lop off 30 seconds to give the task room to run. Otherwise, the
        // runtime
        // of the task pushes the next scheduled runtime out beyond the targeted
        // once-a-minute opportunity to run
        final long intervalSeconds = (intervalMinutes * 60) - 30l;
        final long intervalMillisec = intervalSeconds * 1000;
        return intervalMillisec;
    }

    @Override
    public void destroy() throws Exception {
        logger.info("destroy() called; Unscheduling " + JOB_NAME);
        pluginScheduler.unscheduleJob(JOB_NAME);
    }
}
