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
package com.blackducksoftware.integration.jira.config.controller;

import static javax.ws.rs.core.Response.Status;
import static javax.ws.rs.core.Response.noContent;
import static javax.ws.rs.core.Response.serverError;
import static javax.ws.rs.core.Response.status;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.controller.action.ManageOldIssues;

@Path("/config/management")
public class ManagementController extends ConfigController {
    private final ManageOldIssues manageOldIssues;

    public ManagementController(final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate, final UserManager userManager, final com.atlassian.jira.user.util.UserManager jiraUserManager) {
        super(pluginSettingsFactory, transactionTemplate, userManager);
        this.manageOldIssues = new ManageOldIssues(new JiraServices(), jiraUserManager);
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    public Response put(final String oldUrl, @Context final HttpServletRequest request) {
        final boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return status(Status.UNAUTHORIZED).build();
        }

        try {
            final Optional<UserKey> userKey = getAuthorizationChecker().getUserKey(request);
            if (userKey.isPresent()) {
                manageOldIssues.closeAllIssues(userKey.get(), oldUrl);
            }
        } catch (final JiraIssueException e) {
            return serverError().build();
        }

        return noContent().build();
    }
}
