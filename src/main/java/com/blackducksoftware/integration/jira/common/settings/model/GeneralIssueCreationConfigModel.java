package com.blackducksoftware.integration.jira.common.settings.model;

public class GeneralIssueCreationConfigModel {
    private Integer interval;
    private String defaultIssueCreator;

    public GeneralIssueCreationConfigModel(final Integer interval, final String defaultIssueCreator) {
        this.interval = interval;
        this.defaultIssueCreator = defaultIssueCreator;
    }

    public Integer getInterval() {
        return interval;
    }

    public String getDefaultIssueCreator() {
        return defaultIssueCreator;
    }

}
