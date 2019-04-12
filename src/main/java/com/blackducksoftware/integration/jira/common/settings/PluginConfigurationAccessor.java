package com.blackducksoftware.integration.jira.common.settings;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;

public class PluginConfigurationAccessor {
    private JiraSettingsAccessor jiraSettingsAccessor;

    public PluginConfigurationAccessor(final JiraSettingsAccessor jiraSettingsAccessor) {
        this.jiraSettingsAccessor = jiraSettingsAccessor;
    }

    public String getFirstTimeSave() {
        return jiraSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME);
    }

    public void setFirstTimeSave(final String firstTimeSave) {
        jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME, firstTimeSave);
    }

    public String getLastRunDate() {
        return jiraSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_LAST_RUN_DATE);
    }

    public void setLastRunDate(final String lastRunDate) {
        jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_LAST_RUN_DATE, lastRunDate);
    }

    public String getJiraAdminUser() {
        return jiraSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ADMIN_USER);
    }

    public void setJiraAdminUser(final String adminUser) {
        jiraSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ADMIN_USER, adminUser);
    }

    public Object getPluginError() {
        return jiraSettingsAccessor.getObjectValue(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR);
    }

    public void setPluginError(final Object pluginError) {
        jiraSettingsAccessor.setValue(BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR, pluginError);
    }

}
