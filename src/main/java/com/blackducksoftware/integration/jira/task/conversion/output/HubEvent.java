/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.jira.task.conversion.output;

import java.net.URISyntaxException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.dataservice.notification.item.NotificationContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.config.ProjectFieldCopyMapping;

/**
 * An event is one of the following: Policy violation by a specific component on
 * a specific project, policy override (on a ...), vulnerability added to a
 * specific component on a specific project, vulnerability removed (from a ...),
 * vulnerability updated (on a ...).
 *
 * @author sbillings
 *
 */
public abstract class HubEvent<T extends NotificationContentItem> {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final HubEventAction action;

    private final String jiraUserName;

    private final String jiraUserId;

    // if issueAssigneeId is null: leave it unassigned
    private final String issueAssigneeId;

    private final String jiraIssueTypeId;

    private final Long jiraProjectId;

    private final String jiraProjectName;

    private final T notif;

    private final Set<ProjectFieldCopyMapping> projectFieldCopyMappings;

    // TODO it seems unfortunate that this class now needs a hub service
    final MetaService metaService;

    public HubEvent(final HubEventAction action, final String jiraUserName, final String jiraUserId,
            final String issueAssigneeId, final String jiraIssueTypeId, final Long jiraProjectId,
            final String jiraProjectName, final Set<ProjectFieldCopyMapping> projectFieldCopyMappings,
            final T notif, final MetaService metaService) {
        this.action = action;
        this.jiraUserName = jiraUserName;
        this.jiraUserId = jiraUserId;
        this.issueAssigneeId = issueAssigneeId;
        this.jiraIssueTypeId = jiraIssueTypeId;
        this.jiraProjectId = jiraProjectId;
        this.jiraProjectName = jiraProjectName;
        this.projectFieldCopyMappings = projectFieldCopyMappings;
        this.notif = notif;
        this.metaService = metaService;
    }

    public HubEventAction getAction() {
        return action;
    }

    public T getNotif() {
        return notif;
    }

    public String getJiraUserName() {
        return jiraUserName;
    }

    public String getJiraUserId() {
        return jiraUserId;
    }

    public String getIssueAssigneeId() {
        return issueAssigneeId;
    }

    public String getJiraIssueTypeId() {
        return jiraIssueTypeId;
    }

    public Long getJiraProjectId() {
        return jiraProjectId;
    }

    public String getJiraProjectName() {
        return jiraProjectName;
    }

    public String getReopenComment() {
        return null;
    }

    public String getComment() {
        return null;
    }

    public String getCommentIfExists() {
        return null;
    }

    public String getCommentInLieuOfStateChange() {
        return null;
    }

    public String getResolveComment() {
        return null;
    }

    public Set<ProjectFieldCopyMapping> getProjectFieldCopyMappings() {
        return projectFieldCopyMappings;
    }

    public abstract String getUniquePropertyKey() throws HubIntegrationException, URISyntaxException;

    public abstract String getIssueSummary();

    public abstract String getIssueDescription();

    public abstract IssueProperties createIssuePropertiesFromJson(final String json);

    public abstract IssueProperties createIssueProperties(final Issue issue);

    protected String hashString(final String origString) {
        String hashString;
        if (origString == null) {
            hashString = "";
        } else {
            hashString = String.valueOf(origString.hashCode());
        }
        logger.debug("Hash string for '" + origString + "': " + hashString);
        return hashString;
    }

    protected MetaService getMetaService() {
        return metaService;
    }
}
