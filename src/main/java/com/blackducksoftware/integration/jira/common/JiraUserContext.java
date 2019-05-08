/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.jira.common;

import java.util.Optional;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

public class JiraUserContext {
    private final ApplicationUser jiraAdminUser;
    private final ApplicationUser defaultJiraIssueCreatorUser;

    public static Optional<JiraUserContext> create(final BlackDuckJiraLogger logger, final String jiraAdminUsername, String jiraIssueCreatorUsername, final UserManager userManager) {
        logger.debug(String.format("Checking JIRA users: Admin: %s; Issue creator: %s", jiraAdminUsername, jiraIssueCreatorUsername));
        if (jiraIssueCreatorUsername == null) {
            logger.warn(String.format("The JIRA Issue Creator user has not been configured, using the admin user (%s) to create issues. This can be changed via the Issue Creation configuration", jiraAdminUsername));
            jiraIssueCreatorUsername = jiraAdminUsername;
        }
        final Optional<ApplicationUser> jiraAdminUser = getJiraUser(logger, jiraAdminUsername, userManager);
        final Optional<ApplicationUser> jiraIssueCreatorUser = getJiraUser(logger, jiraIssueCreatorUsername, userManager);
        if (!jiraAdminUser.isPresent() || !jiraIssueCreatorUser.isPresent()) {
            return Optional.empty();
        }
        final JiraUserContext jiraContext = new JiraUserContext(jiraAdminUser.get(), jiraIssueCreatorUser.get());
        return Optional.of(jiraContext);
    }

    private static Optional<ApplicationUser> getJiraUser(final BlackDuckJiraLogger logger, final String jiraUsername, final UserManager userManager) {
        final ApplicationUser jiraUser = userManager.getUserByName(jiraUsername);
        if (jiraUser == null) {
            logger.error(String.format("Could not find the JIRA user %s", jiraUsername));
        }
        return Optional.ofNullable(jiraUser);
    }

    public JiraUserContext(final ApplicationUser jiraAdminUser, final ApplicationUser defaultJiraIssueCreatorUser) {
        this.jiraAdminUser = jiraAdminUser;
        this.defaultJiraIssueCreatorUser = defaultJiraIssueCreatorUser;
    }

    public ApplicationUser getJiraAdminUser() {
        return jiraAdminUser;
    }

    public ApplicationUser getDefaultJiraIssueCreatorUser() {
        return defaultJiraIssueCreatorUser;
    }

}
