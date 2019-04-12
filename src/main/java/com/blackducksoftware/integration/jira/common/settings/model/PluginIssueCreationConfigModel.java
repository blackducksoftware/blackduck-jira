package com.blackducksoftware.integration.jira.common.settings.model;

public class PluginIssueCreationConfigModel {
    private GeneralIssueCreationConfigModel general;
    private ProjectMappingConfigModel projectMapping;
    private TicketCriteriaConfigModel ticketCriteria;

    public PluginIssueCreationConfigModel(final GeneralIssueCreationConfigModel general, final ProjectMappingConfigModel projectMapping, final TicketCriteriaConfigModel ticketCriteria) {
        this.general = general;
        this.projectMapping = projectMapping;
        this.ticketCriteria = ticketCriteria;
    }

    public GeneralIssueCreationConfigModel getGeneral() {
        return general;
    }

    public ProjectMappingConfigModel getProjectMapping() {
        return projectMapping;
    }

    public TicketCriteriaConfigModel getTicketCriteria() {
        return ticketCriteria;
    }

}
