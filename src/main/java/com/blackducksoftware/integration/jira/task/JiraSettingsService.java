package com.blackducksoftware.integration.jira.task;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import org.joda.time.DateTime;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;

public class JiraSettingsService {

	private final PluginSettings settings;

	public JiraSettingsService(final PluginSettings settings) {
		this.settings = settings;
	};

	public void addHubError(final Throwable throwable) {
		final StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		addHubError(sw.toString());
	}

	public void addHubError(final String errorMessage) {
		final Object errorMapObject = settings.get(HubJiraConstants.HUB_JIRA_ERROR);
		final HashMap<String, String> errorMap;
		if(errorMapObject == null){
			errorMap = new HashMap<String, String>();
		} else {
			errorMap = (HashMap<String, String>) errorMapObject;
		}
		errorMap.put(errorMessage.trim(), (new DateTime()).toString());
		settings.put(HubJiraConstants.HUB_JIRA_ERROR, errorMap);
		System.out.println("TEST Added error : " + errorMessage);
	}
}
