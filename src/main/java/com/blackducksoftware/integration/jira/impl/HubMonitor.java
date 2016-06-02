package com.blackducksoftware.integration.jira.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.blackducksoftware.integration.jira.api.NotificationMonitor;
import com.blackducksoftware.integration.jira.task.HubNotificationCheckTask;

@ExportAsService({ NotificationMonitor.class })
@Named("notificationMonitor")
public class HubMonitor implements NotificationMonitor, LifecycleAware {

	/* package */static final String KEY = HubMonitor.class.getName() + ":instance";
	private static final String JOB_NAME = HubMonitor.class.getName() + ":job";

	private final Logger logger = Logger.getLogger(HubMonitor.class);

	// @ComponentImport
	private final PluginScheduler pluginScheduler; // provided by SAL

	private long interval = 5000L; // default job interval (5 sec)
	private String serverName = "initialServerName";
	private Date lastRun = null; // time when the last search returned

	@Inject
	public HubMonitor(@ComponentImport PluginScheduler pluginScheduler) {
		log("HubMonitor ctor called.");
		this.pluginScheduler = pluginScheduler;
	}

	@Override
	public void onStart() {
		log("HubMonitor onStart() called.");
		reschedule(serverName, interval);
	}

	public void reschedule(String serverName, long interval) {
		log("HubMonitor reschedule() called.");
		this.interval = interval;
		this.serverName = serverName;

		pluginScheduler.scheduleJob(JOB_NAME, // unique name of the job
				HubNotificationCheckTask.class, // class of the job
				new HashMap<String, Object>() {
					{
						put(KEY, HubMonitor.this);
					}
				}, // data that needs to be passed to the job
				new Date(), // the time the job is to start
				interval); // interval between repeats, in milliseconds
		logger.info(String.format("Twitter search task scheduled to run every %dms", interval));
	}

	/* package */void setLastRun(Date lastRun) {
		log("HubMonitor setLastRun() called.");
		this.lastRun = lastRun;
	}

	// @Override
	public void onStop() {
		log("HubMonitor onStop() called.");
	}

	public String getName() {
		log("HubMonitor.getName() called");
		if (null != pluginScheduler) {
			return "hubMonitor with pluginScheduler:" + pluginScheduler.toString();
		}

		return "hubMonitor";
	}

	private void log(String msg) {
		String filename = "/tmp/HubMonitor_log.txt";
		msg = "[INFO] " + (new Date()).toString() + ": " + msg + "\n";
		try {
			File file = new File(filename);
			if (!file.exists()) {
				file.createNewFile();
			}

			Files.write(Paths.get(filename), msg.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			throw new IllegalArgumentException("IO error in log(): " + e.getMessage());
		}
	}
}
