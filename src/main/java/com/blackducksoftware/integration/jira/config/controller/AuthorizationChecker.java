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

import java.util.Collection;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

public class AuthorizationChecker {
    private final UserManager userManager;

    public AuthorizationChecker(final UserManager userManager) {
        this.userManager = userManager;
    }

    public boolean isValidAuthorization(final HttpServletRequest request, final Collection<String> blackDuckJiraGroups) {
        String[] groupsArray = {};
        return isValidAuthorization(request, blackDuckJiraGroups.toArray(groupsArray));
    }

    public boolean isValidAuthorization(final HttpServletRequest request, final String[] blackDuckJiraGroups) {
        return isUserSystemAdmin(request) || isGroupAuthorized(request, blackDuckJiraGroups);
    }

    public boolean isValidAuthorization(final String username, final Collection<String> blackDuckJiraGroups) {
        String[] groupsArray = {};
        return isValidAuthorization(username, blackDuckJiraGroups.toArray(groupsArray));
    }

    public boolean isValidAuthorization(final String username, final String[] blackDuckJiraGroups) {
        return isUserSystemAdmin(username) || isGroupAuthorized(username, blackDuckJiraGroups);
    }

    public Optional<String> getUsername(final HttpServletRequest request) {
        return getUser(request).map(UserProfile::getUsername);
    }

    public Optional<UserProfile> getUser(final HttpServletRequest request) {
        return Optional.ofNullable(userManager.getRemoteUser(request));
    }

    public Optional<UserProfile> getUser(final String username) {
        return Optional.ofNullable(userManager.getUserProfile(username));
    }

    public Optional<UserKey> getUserKey(final HttpServletRequest request) {
        return getUser(request).map(UserProfile::getUserKey);
    }

    public Optional<UserKey> getUserKey(final String username) {
        return getUser(username).map(UserProfile::getUserKey);
    }

    public boolean isUserAvailable(final HttpServletRequest request) {
        return getUsername(request).isPresent();
    }

    public boolean isUserSystemAdmin(final HttpServletRequest request) {
        final Optional<UserKey> userKey = getUserKey(request);
        return userKey.isPresent() && userManager.isSystemAdmin(userKey.get());
    }

    public boolean isUserSystemAdmin(final String username) {
        final Optional<UserKey> userKey = getUserKey(username);
        return userKey.isPresent() && isUserSystemAdmin(userKey.get());
    }

    public boolean isUserSystemAdmin(final UserKey userKey) {
        return userManager.isSystemAdmin(userKey);
    }

    public boolean isGroupAuthorized(final HttpServletRequest request, final String[] blackDuckJiraGroups) {
        final Optional<UserKey> userKey = getUserKey(request);
        return userKey.isPresent() && isGroupAuthorized(userKey.get(), blackDuckJiraGroups);
    }

    public boolean isGroupAuthorized(final String username, final String[] blackDuckJiraGroups) {
        final Optional<UserKey> userKeyOptional = getUserKey(username);
        return userKeyOptional
                   .filter(userKey -> isGroupAuthorized(userKey, blackDuckJiraGroups))
                   .isPresent();
    }

    public boolean isGroupAuthorized(final UserKey userKey, final String[] blackDuckJiraGroups) {
        if (null != userKey && null != blackDuckJiraGroups) {
            for (final String blackDuckJiraGroup : blackDuckJiraGroups) {
                if (userManager.isUserInGroup(userKey, blackDuckJiraGroup.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

}
