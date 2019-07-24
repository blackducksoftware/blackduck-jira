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
package com.blackducksoftware.integration.jira.web.controller;

import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.data.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.data.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.data.model.PluginGroupsConfigModel;

public class ConfigController {
    // This must be "package protected" to avoid synthetic access
    final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;
    private final AuthorizationChecker authorizationChecker;
    private final JiraSettingsAccessor jiraSettingsAccessor;

    public ConfigController(final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate, final UserManager userManager) {
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
        authorizationChecker = new AuthorizationChecker(userManager);
        jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettingsFactory.createGlobalSettings());
    }

    public PluginSettingsFactory getPluginSettingsFactory() {
        return pluginSettingsFactory;
    }

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    public AuthorizationChecker getAuthorizationChecker() {
        return authorizationChecker;
    }

    public JiraSettingsAccessor getJiraSettingsAccessor() {
        return jiraSettingsAccessor;
    }

    protected <T> T executeAsTransaction(final Supplier<T> supplier) {
        return getTransactionTemplate().execute(() -> supplier.get());
    }

    protected boolean isAuthorized(final HttpServletRequest request) {
        final GlobalConfigurationAccessor globalConfigurationAccessor = jiraSettingsAccessor.createGlobalConfigurationAccessor();
        final PluginGroupsConfigModel groupsConfig = globalConfigurationAccessor.getGroupsConfig();
        return getAuthorizationChecker().isValidAuthorization(request, groupsConfig.getGroups());
    }

}
