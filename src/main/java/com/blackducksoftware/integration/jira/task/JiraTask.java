package com.blackducksoftware.integration.jira.task;

import java.util.Map;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.scheduling.PluginJob;
import com.blackducksoftware.integration.atlassian.utils.HubConfigKeys;
import com.blackducksoftware.integration.jira.impl.HubMonitor;
import com.blackducksoftware.integration.jira.utils.HubJiraConfigKeys;

/**
 * A scheduled JIRA task that collects recent notifications from the Hub, and
 * generates JIRA tickets for them.
 *
 * @author sbillings
 *
 */
public class JiraTask implements PluginJob {


	public JiraTask() {
	}

	@Override
	public void execute(final Map<String, Object> jobDataMap) {

		final ProjectManager jiraProjectManager = (ProjectManager) jobDataMap.get(HubMonitor.KEY_PROJECT_MANAGER);
		final UserManager jiraUserManager = (UserManager) jobDataMap.get(HubMonitor.KEY_USER_MANAGER);
		final IssueService jiraIssueService = (IssueService) jobDataMap.get(HubMonitor.KEY_ISSUE_SERVICE);
		final JiraAuthenticationContext authContext = (JiraAuthenticationContext) jobDataMap
				.get(HubMonitor.KEY_AUTH_CONTEXT);

		final PluginSettings settings = (PluginSettings) jobDataMap.get(HubMonitor.KEY_SETTINGS);
		final String hubUrl = getStringValue(settings, HubConfigKeys.CONFIG_HUB_URL);
		final String hubUsername = getStringValue(settings, HubConfigKeys.CONFIG_HUB_USER);
		final String hubPasswordEncrypted = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS);
		final String hubTimeoutString = getStringValue(settings, HubConfigKeys.CONFIG_HUB_TIMEOUT);
		final String intervalString = getStringValue(settings,
				HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
		final String projectMappingJson = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);
		final String policyRulesJson = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON);
		final String jiraIssueTypeName = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_ISSUE_TYPE_NAME);
		final String installDateString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME);
		final String lastRunDateString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE);

		final String jiraUser = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_USER);


		final HubJiraTask processor = new HubJiraTask(hubUrl, hubUsername, hubPasswordEncrypted,
				hubTimeoutString,
				intervalString, jiraIssueTypeName, installDateString, lastRunDateString, projectMappingJson,
				policyRulesJson, jiraProjectManager, jiraUserManager, jiraIssueService, authContext, jiraUser);
		final String runDateString = processor.execute();
		if (runDateString != null) {
			settings.put(HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE, runDateString);
		}
	}

	private Object getValue(final PluginSettings settings, final String key) {
		return settings.get(key);
	}

	private String getStringValue(final PluginSettings settings, final String key) {
		return (String) getValue(settings, key);
	}
}
