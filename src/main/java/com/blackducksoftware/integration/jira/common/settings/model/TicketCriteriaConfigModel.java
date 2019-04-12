package com.blackducksoftware.integration.jira.common.settings.model;

public class TicketCriteriaConfigModel {
    private String policyRulesJson;
    private Boolean commentOnIssueUpdates;
    private Boolean addComponentReviewerToTickets;

    public TicketCriteriaConfigModel(final String policyRulesJson, final Boolean commentOnIssueUpdates, final Boolean addComponentReviewerToTickets) {
        this.policyRulesJson = policyRulesJson;
        this.commentOnIssueUpdates = commentOnIssueUpdates;
        this.addComponentReviewerToTickets = addComponentReviewerToTickets;
    }

    public String getPolicyRulesJson() {
        return policyRulesJson;
    }

    public Boolean getCommentOnIssueUpdates() {
        return commentOnIssueUpdates;
    }

    public Boolean getAddComponentReviewerToTickets() {
        return addComponentReviewerToTickets;
    }

}
