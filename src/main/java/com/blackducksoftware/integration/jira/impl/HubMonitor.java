package com.blackducksoftware.integration.jira.impl;

import java.util.Date;
import java.util.HashMap;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.api.NotificationMonitor;
import com.blackducksoftware.integration.jira.task.JiraTask;

public class HubMonitor implements NotificationMonitor, LifecycleAware {

	/* package */static final String KEY_INSTANCE = HubMonitor.class.getName() + ":instance";
	public static final String KEY_SETTINGS = HubMonitor.class.getName() + ":settings";
	public static final String KEY_PROJECT_MANAGER = HubMonitor.class.getName() + ":projectManager";

	private static final String JOB_NAME = HubMonitor.class.getName() + ":job";

	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

	private final PluginScheduler pluginScheduler; // provided by SAL
	private final PluginSettingsFactory pluginSettingsFactory;
	private final ProjectManager projectManager;

	private long interval = 5000L; // default job interval (5 sec)
	private String serverName = "initialServerName";
	private Date lastRun = null; // time when the last search returned

	@Inject
	public HubMonitor(final PluginScheduler pluginScheduler, final PluginSettingsFactory pluginSettingsFactory,
			final ProjectManager projectManager) {
		logger.debug("HubMonitor ctor called.");
		this.pluginScheduler = pluginScheduler;
		this.pluginSettingsFactory = pluginSettingsFactory;
		this.projectManager = projectManager;
	}

	@Override
	public void onStart() {
		logger.debug("HubMonitor onStart() called.");
		reschedule(serverName, interval);
	}

	@Override
	public void reschedule(final String serverName, final long interval) {
		logger.debug("HubMonitor reschedule() called.");
		logger.debug("pluginSettingsFactory: " + pluginSettingsFactory);

		this.interval = interval;
		this.serverName = serverName;

		pluginScheduler.scheduleJob(JOB_NAME, // unique name of the job
				JiraTask.class, // class of the job
				new HashMap<String, Object>() {
					{
						put(KEY_INSTANCE, HubMonitor.this);
						put(KEY_SETTINGS, pluginSettingsFactory.createGlobalSettings());
						put(KEY_PROJECT_MANAGER, projectManager);
					}
				}, // data that needs to be passed to the job
				new Date(), // the time the job is to start
				interval); // interval between repeats, in milliseconds
		logger.info(String.format("Hub Notification check task scheduled to run every %dms", interval));
	}

	/* package */void setLastRun(final Date lastRun) {
		logger.debug("HubMonitor setLastRun() called.");
		this.lastRun = lastRun;
	}

	public String getName() {
		logger.debug("HubMonitor.getName() called");
		if (null != pluginScheduler) {
			return "hubMonitor with pluginScheduler:" + pluginScheduler.toString();
		}

		return "hubMonitor";
	}
}
