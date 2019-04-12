package com.blackducksoftware.integration.jira.common.settings.model;

public class PluginIssueFieldConfigModel {
    private String fieldMappingJson;

    public PluginIssueFieldConfigModel(final String fieldMappingJson) {
        this.fieldMappingJson = fieldMappingJson;
    }

    public String getFieldMappingJson() {
        return fieldMappingJson;
    }

}
