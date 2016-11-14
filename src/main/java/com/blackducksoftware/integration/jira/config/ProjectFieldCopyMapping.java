package com.blackducksoftware.integration.jira.config;

import com.blackducksoftware.integration.jira.common.PluginField;

public class ProjectFieldCopyMapping {
    private String jiraProjectName;

    private String hubProjectName;

    private PluginField pluginField;

    private String targetFieldId;

    private String targetFieldName;

    public ProjectFieldCopyMapping() {
    }

    public ProjectFieldCopyMapping(String jiraProjectName, String hubProjectName, PluginField pluginField, String targetFieldId, String targetFieldName) {
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

    public String getTargetFieldName() {
        return targetFieldName;
    }

    public void setJiraProjectName(String jiraProjectName) {
        this.jiraProjectName = jiraProjectName;
    }

    public void setHubProjectName(String hubProjectName) {
        this.hubProjectName = hubProjectName;
    }

    public void setPluginField(PluginField pluginField) {
        this.pluginField = pluginField;
    }

    public void setTargetFieldId(String targetFieldId) {
        this.targetFieldId = targetFieldId;
    }

    public void setTargetFieldName(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    @Override
    public String toString() {
        return "ProjectFieldCopyMapping [jiraProjectName=" + jiraProjectName + ", hubProjectName=" + hubProjectName + ", pluginField=" + pluginField
                + ", targetFieldId=" + targetFieldId + ", targetFieldName=" + targetFieldName + "]";
    }
}
