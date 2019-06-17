/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
 * https://www.synopsys.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
