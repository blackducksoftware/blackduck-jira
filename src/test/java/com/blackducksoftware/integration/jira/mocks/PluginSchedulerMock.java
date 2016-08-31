package com.blackducksoftware.integration.jira.mocks;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.sal.api.scheduling.PluginJob;
import com.atlassian.sal.api.scheduling.PluginScheduler;

public class PluginSchedulerMock implements PluginScheduler {

	private final Map<String, Object> jobMap = new HashMap<>();

	private boolean jobScheduled;
	private boolean jobUnScheduled;

	@Override
	public void scheduleJob(final String name, final Class<? extends PluginJob> job, final Map<String, Object> jobDataMap, final Date startTime,
			final long repeatInterval) {
		jobMap.put(name, job);
		jobScheduled = true;
	}

	@Override
	public void unscheduleJob(final String name) {
		jobMap.remove(name);
		jobUnScheduled = true;
	}

	public boolean isJobScheduled() {
		return jobScheduled;
	}

	public boolean isJobUnScheduled() {
		return jobUnScheduled;
	}

}
