package com.blackducksoftware.integration.jira.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.sal.api.scheduling.PluginJob;
import com.blackducksoftware.integration.jira.hub.HubNotificationService;
import com.blackducksoftware.integration.jira.hub.HubNotificationServiceException;

public class HubNotificationCheckTask implements PluginJob {

	private final Logger logger = Logger.getLogger(HubNotificationCheckTask.class);

	@Override
	public void execute(Map<String, Object> jobDataMap) {
		log("HubNotificationCheckTask.execute() called 4.");

		// TODO should not recreate every time
		HubNotificationService svc;
		try {
			svc = new HubNotificationService("http://eng-hub-valid01.dc1.lan/", "sysadmin", "blackduck");
		} catch (HubNotificationServiceException e) {
			// TODO This will have to change (or move)
			throw new IllegalArgumentException("Error connecting to the Hub: " + e.getMessage(), e);
		}
		String hubVersion;
		try {
			hubVersion = svc.getVersion();
		} catch (HubNotificationServiceException e) {
			// TODO revisit this
			throw new IllegalArgumentException("Error getting Hub version: " + e.getMessage(), e);
		}
		// TODO
		System.out.println("Hub version: " + hubVersion);
	}

	private void log(String msg) {
		System.out.println(msg);
		logger.info(msg);
		String filename = "/tmp/HubNotificationCheckTask_log.txt";
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
