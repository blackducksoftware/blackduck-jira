package com.blackducksoftware.integration.jira.impl;

import java.util.Date;
import java.util.HashMap;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.scheduling.PluginScheduler;
import com.blackducksoftware.integration.jira.HubJiraLogger;
import com.blackducksoftware.integration.jira.api.NotificationMonitor;
import com.blackducksoftware.integration.jira.task.JiraTask;
import com.blackducksoftware.integration.jira.utils.HubJiraConfigKeys;

public class HubMonitor implements NotificationMonitor, LifecycleAware {

	private static final long DEFAULT_INTERVAL_MILLISEC = 1000L;
	/* package */static final String KEY_INSTANCE = HubMonitor.class.getName() + ":instance";
	public static final String KEY_SETTINGS = HubMonitor.class.getName() + ":settings";
	public static final String KEY_ISSUE_SERVICE = HubMonitor.class.getName() + ":issueService";
	public static final String KEY_PROJECT_MANAGER = HubMonitor.class.getName() + ":projectManager";
	public static final String KEY_USER_MANAGER = HubMonitor.class.getName() + ":userManager";
	public static final String KEY_AUTH_CONTEXT = HubMonitor.class.getName() + ":authContext";
	public static final String KEY_PROPTERY_SERVICE = HubMonitor.class.getName() + ":propertyService";
	public static final String KEY_WORKFLOW_MANAGER = HubMonitor.class.getName() + ":workflowManager";
	public static final String KEY_JSON_ENTITY_PROPERTY_MANAGER = HubMonitor.class.getName()
			+ ":jsonEntityPropertyManager";

	private static final String JOB_NAME = HubMonitor.class.getName() + ":job";

	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

	private final PluginScheduler pluginScheduler; // provided by SAL
	private final PluginSettingsFactory pluginSettingsFactory;
	private final ProjectManager projectManager;

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
		reschedule(serverName, 0L);
	}

	public void changeInterval() {
		logger.debug("HubMonitor changeInterval() called.");
		reschedule(serverName, 0L);
	}

	@Override
	public void reschedule(final String serverName, final long intervalIgnored) {
		logger.debug("HubMonitor reschedule() called.");
		logger.debug("pluginSettingsFactory: " + pluginSettingsFactory);

		final IssueService issueService = ComponentAccessor.getIssueService();
		logger.debug("issueService: " + issueService);

		final UserManager userManager = ComponentAccessor.getUserManager();
		logger.debug("userManager: " + userManager);

		final JiraAuthenticationContext authContext = ComponentAccessor.getJiraAuthenticationContext();
		logger.debug("authContext: " + authContext);

		final IssuePropertyService propertyService = ComponentAccessor.getComponentOfType(IssuePropertyService.class);
		logger.debug("propertyService: " + propertyService);

		final WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
		logger.debug("workflowManager: " + workflowManager);

		final JsonEntityPropertyManager jsonEntityPropertyManager = ComponentAccessor
				.getComponentOfType(JsonEntityPropertyManager.class);
		logger.debug("jsonEntityPropertyManager: " + jsonEntityPropertyManager);

		final long actualInterval = getIntervalMillisec();

		this.serverName = serverName;

		pluginScheduler.scheduleJob(JOB_NAME, // unique name of the job
				JiraTask.class, // class of the job
				new HashMap<String, Object>() {
			{
				put(KEY_INSTANCE, HubMonitor.this);
				put(KEY_SETTINGS, pluginSettingsFactory.createGlobalSettings());
				put(KEY_ISSUE_SERVICE, issueService);
				put(KEY_PROJECT_MANAGER, projectManager);
				put(KEY_USER_MANAGER, userManager);
				put(KEY_AUTH_CONTEXT, authContext);
				put(KEY_PROPTERY_SERVICE, propertyService);
				put(KEY_WORKFLOW_MANAGER, workflowManager);
				put(KEY_JSON_ENTITY_PROPERTY_MANAGER, jsonEntityPropertyManager);
			}
		}, // data that needs to be passed to the job
				new Date(), // the time the job is to start
				actualInterval); // interval between repeats, in milliseconds
		logger.info(String.format("Hub Notification check task scheduled to run every %dms", actualInterval));
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

	private long getIntervalMillisec() {
		final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
		if (settings == null) {
			logger.error("Unable to get plugin settings");
			;
			return DEFAULT_INTERVAL_MILLISEC;
		}
		final String intervalString = (String) settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
		if (intervalString == null) {
			logger.error("Unable to get interval from plugin settings");
			;
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
		final long intervalSeconds = (intervalMinutes * 60) - 30;
		final long intervalMillisec = intervalSeconds * 1000;
		return intervalMillisec;
	}
}
