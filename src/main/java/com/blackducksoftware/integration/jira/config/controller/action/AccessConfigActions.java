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
package com.blackducksoftware.integration.jira.config.controller.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.blackducksoftware.integration.jira.common.settings.PluginSettingsWrapper;
import com.blackducksoftware.integration.jira.config.JiraConfigErrorStrings;
import com.blackducksoftware.integration.jira.config.controller.AuthorizationChecker;
import com.blackducksoftware.integration.jira.config.model.BlackDuckAdminConfigSerializable;

public class AccessConfigActions {
    private final PluginSettingsWrapper pluginSettingsWrapper;
    private final AuthorizationChecker authorizationChecker;
    private final GroupPickerSearchService groupPickerSearchService;

    public AccessConfigActions(final PluginSettingsFactory pluginSettingsFactory, final AuthorizationChecker authorizationChecker, final GroupPickerSearchService groupPickerSearchService) {
        this.pluginSettingsWrapper = new PluginSettingsWrapper(pluginSettingsFactory.createGlobalSettings());
        this.authorizationChecker = authorizationChecker;
        this.groupPickerSearchService = groupPickerSearchService;
    }

    public BlackDuckAdminConfigSerializable getConfigWithJiraGroups(final HttpServletRequest request) {
        final BlackDuckAdminConfigSerializable txAdminConfig = new BlackDuckAdminConfigSerializable();
        final String blackDuckConfigGroups = pluginSettingsWrapper.getBlackDuckConfigGroups();
        txAdminConfig.setHubJiraGroups(blackDuckConfigGroups);
        if (authorizationChecker.isUserSystemAdmin(request)) {
            final List<String> jiraGroups = new ArrayList<>();

            final Collection<Group> jiraGroupCollection = groupPickerSearchService.findGroups("");
            if (jiraGroupCollection != null && !jiraGroupCollection.isEmpty()) {
                for (final Group group : jiraGroupCollection) {
                    jiraGroups.add(group.getName());
                }
            }
            txAdminConfig.setJiraGroups(jiraGroups);
        }
        return txAdminConfig;
    }

    public BlackDuckAdminConfigSerializable updateConfigWithJiraGroups(final HttpServletRequest request, final String blackDuckJiraGroups) {
        final BlackDuckAdminConfigSerializable txResponseObject = new BlackDuckAdminConfigSerializable();

        final boolean userSystemAdmin = authorizationChecker.isUserSystemAdmin(request);
        if (!userSystemAdmin) {
            txResponseObject.setHubJiraGroupsError(JiraConfigErrorStrings.NON_SYSTEM_ADMINS_CANT_CHANGE_GROUPS);
            return txResponseObject;
        } else {
            pluginSettingsWrapper.setBlackDuckConfigGroups(blackDuckJiraGroups);
        }
        return null;
    }

}
