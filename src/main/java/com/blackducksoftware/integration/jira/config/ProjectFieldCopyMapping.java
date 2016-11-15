package com.blackducksoftware.integration.jira.config;

public class ProjectFieldCopyMapping {
    private String jiraProjectName;

    private String hubProjectName;

    private String sourceFieldId;

    private String sourceFieldName;

    private String targetFieldId;

    private String targetFieldName;

    public ProjectFieldCopyMapping() {
    }

    public ProjectFieldCopyMapping(String jiraProjectName, String hubProjectName, String sourceFieldId, String sourceFieldName, String targetFieldId,
            String targetFieldName) {
        super();
        this.jiraProjectName = jiraProjectName;
        this.hubProjectName = hubProjectName;
        this.sourceFieldId = sourceFieldId;
        this.sourceFieldName = sourceFieldName;
        this.targetFieldId = targetFieldId;
    }

    public String getJiraProjectName() {
        return jiraProjectName;
    }

    public String getHubProjectName() {
        return hubProjectName;
    }

    public String getSourceFieldId() {
        return sourceFieldId;
    }

    public String getSourceFieldName() {
        return sourceFieldName;
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

    public void setSourceFieldId(String sourceFieldId) {
        this.sourceFieldId = sourceFieldId;
    }

    public void setSourceFieldName(String sourceFieldName) {
        this.sourceFieldName = sourceFieldName;
    }

    public void setTargetFieldId(String targetFieldId) {
        this.targetFieldId = targetFieldId;
    }

    public void setTargetFieldName(String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    @Override
    public String toString() {
        return "ProjectFieldCopyMapping [jiraProjectName=" + jiraProjectName + ", hubProjectName=" + hubProjectName + ", sourceFieldId=" + sourceFieldId
                + ", sourceFieldName=" + sourceFieldName + ", targetFieldId=" + targetFieldId + ", targetFieldName=" + targetFieldName + "]";
    }
}
