package com.blackducksoftware.integration.jira.config;

import com.blackducksoftware.integration.jira.common.PluginField;

public class ProjectFieldCopyMapping {
    private final String jiraProjectName;

    private final String hubProjectName;

    private final PluginField pluginField;

    private final String targetFieldName;

    public ProjectFieldCopyMapping(String jiraProjectName, String hubProjectName, PluginField pluginField, String targetFieldName) {
        super();
        this.jiraProjectName = jiraProjectName;
        this.hubProjectName = hubProjectName;
        this.pluginField = pluginField;
        this.targetFieldName = targetFieldName;
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

    public String getTargetFieldName() {
        return targetFieldName;
    }

}
