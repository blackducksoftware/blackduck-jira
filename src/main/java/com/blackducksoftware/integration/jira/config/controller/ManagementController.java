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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.controller.action.ManageOldIssues;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@Path("/config/management")
public class ManagementController extends ConfigController {
    private final ManageOldIssues manageOldIssues;

    public ManagementController(final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate, final UserManager userManager, final com.atlassian.jira.user.util.UserManager jiraUserManager) {
        super(pluginSettingsFactory, transactionTemplate, userManager);
        this.manageOldIssues = new ManageOldIssues(new JiraServices(), jiraUserManager);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(final String oldUrl, @Context final HttpServletRequest request) {
        final boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return status(Status.UNAUTHORIZED).build();
        }

        try {
            final Optional<UserKey> userKey = getAuthorizationChecker().getUserKey(request);
            if (userKey.isPresent()) {
                final Gson gson = new GsonBuilder().create();
                final JsonObject jsonObject = gson.fromJson(oldUrl, JsonObject.class);
                final String url = jsonObject.get("oldUrl").getAsString();
                if (StringUtils.isBlank(url)) {
                    return Response.status(Status.BAD_REQUEST).build();
                }
                manageOldIssues.closeAllIssues(userKey.get(), url);
            }
        } catch (final JiraIssueException e) {
            return serverError().build();
        }

        return noContent().build();
    }
}
