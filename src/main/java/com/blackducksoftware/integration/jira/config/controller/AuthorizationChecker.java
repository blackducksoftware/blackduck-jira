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
package com.blackducksoftware.integration.jira.config.controller;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.sal.api.user.UserManager;

public class AuthorizationChecker {
    private final UserManager userManager;

    public AuthorizationChecker(final UserManager userManager) {
        this.userManager = userManager;
    }

    public boolean isValidAuthorization(final HttpServletRequest request, final String[] blackDuckJiraGroups) {
        return isUserSystemAdmin(request) || isGroupAuthorized(request, blackDuckJiraGroups);
    }

    public boolean isValidAuthorization(final String username, final String[] blackDuckJiraGroups) {
        return isUserSystemAdmin(username) && isGroupAuthorized(username, blackDuckJiraGroups);
    }

    public String getUsername(final HttpServletRequest request) {
        return userManager.getRemoteUsername(request);
    }

    public boolean isUserAvailable(final HttpServletRequest request) {
        final String username = getUsername(request);
        return username != null;
    }

    public boolean isUserSystemAdmin(final HttpServletRequest request) {
        final String username = getUsername(request);
        return isUserSystemAdmin(username);
    }

    public boolean isUserSystemAdmin(final String username) {
        if (username == null) {
            return false;
        }
        return userManager.isSystemAdmin(username);
    }

    public boolean isGroupAuthorized(final HttpServletRequest request, final String[] blackDuckJiraGroups) {
        final String username = userManager.getRemoteUsername(request);
        return isGroupAuthorized(username, blackDuckJiraGroups);
    }

    public boolean isGroupAuthorized(final String username, final String[] blackDuckJiraGroups) {
        for (final String blackDuckJiraGroup : blackDuckJiraGroups) {
            if (userManager.isUserInGroup(username, blackDuckJiraGroup.trim())) {
                return true;
            }
        }
        return false;
    }

}
