package com.blackducksoftware.integration.jira.common.settings.model;

public class ProjectMappingConfigModel {
    private String mappingsJson;

    public ProjectMappingConfigModel(final String mappingsJson) {
        this.mappingsJson = mappingsJson;
    }

    public String getMappingsJson() {
        return mappingsJson;
    }

}
