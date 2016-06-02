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

public class HubNotificationCheckTask implements PluginJob {

	private final Logger logger = Logger.getLogger(HubNotificationCheckTask.class);

	@Override
	public void execute(Map<String, Object> jobDataMap) {
		System.out.println("HubNotificationCheckTask.execute() called 4.");
		log("HubNotificationCheckTask.execute() called 4.");
	}

	private void log(String msg) {
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
