/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
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
package com.blackducksoftware.integration.jira.issue.tracker;

import com.google.gson.annotations.SerializedName;

public class IssueTrackerProperties {
    // TODO this might break backwards compatibility with the issue tracker
    @SerializedName("hubIssueUrl")
    private final String blackDuckIssueUrl;
    private final Long jiraIssueId;

    public IssueTrackerProperties(final String blackDuckIssueUrl, final Long jiraIssueId) {
        this.blackDuckIssueUrl = blackDuckIssueUrl;
        this.jiraIssueId = jiraIssueId;
    }

    public Long getJiraIssueId() {
        return jiraIssueId;
    }

    public String getBlackDuckIssueUrl() {
        return blackDuckIssueUrl;
    }

}
