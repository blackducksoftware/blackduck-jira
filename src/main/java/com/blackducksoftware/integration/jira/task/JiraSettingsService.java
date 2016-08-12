package com.blackducksoftware.integration.jira.task;

import java.util.HashMap;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;

public class JiraSettingsService {

	private final PluginSettings settings;

	public JiraSettingsService(final PluginSettings settings) {
		this.settings = settings;
	};


	public void addHubError(final String errorMessage) {
		final Object errorMapObject = settings.get(HubJiraConstants.HUB_JIRA_ERROR);
		final HashMap<String, String> errorMap;
		if(errorMapObject == null){
			errorMap = new HashMap<String, String>();
		} else {
			errorMap = (HashMap<String, String>) errorMapObject;
		}
		errorMap.put(errorMessage, errorMessage);
		settings.put(HubJiraConstants.HUB_JIRA_ERROR, errorMap);
	}
}
