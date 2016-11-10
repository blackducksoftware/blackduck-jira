package com.blackducksoftware.integration.jira.config;

import com.blackducksoftware.integration.jira.common.PluginField;

public class ProjectFieldCopyMapping {
    private final String jiraProjectName;

    private final String hubProjectName;

    private final PluginField pluginField;

    private final String targetFieldId;

    public ProjectFieldCopyMapping(String jiraProjectName, String hubProjectName, PluginField pluginField, String targetFieldId) {
        super();
        this.jiraProjectName = jiraProjectName;
        this.hubProjectName = hubProjectName;
        this.pluginField = pluginField;
        this.targetFieldId = targetFieldId;
    }

    public String getJiraProjectName() {
        return jiraProjectName;
    }

    public String getHubProjectName() {
        return hubProjectName;
    }

    public PluginField getPluginField() {
        return pluginField;
    }

    public String getTargetFieldId() {
        return targetFieldId;
    }

    @Override
    public String toString() {
        return "ProjectFieldCopyMapping [jiraProjectName=" + jiraProjectName + ", hubProjectName=" + hubProjectName + ", pluginField=" + pluginField
                + ", targetFieldId=" + targetFieldId + "]";
    }

}
