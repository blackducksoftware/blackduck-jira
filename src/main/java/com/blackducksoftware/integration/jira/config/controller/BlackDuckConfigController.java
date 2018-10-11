/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.config.BlackDuckConfigKeys;
import com.blackducksoftware.integration.jira.config.model.BlackDuckServerConfigSerializable;

@Path("/blackDuckDetails")
public class BlackDuckConfigController {
    private final UserManager userManager;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;
    private final BlackDuckConfigActions blackDuckConfigActions;

    public BlackDuckConfigController(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
        this.blackDuckConfigActions = new BlackDuckConfigActions();
    }

    private Response checkUserPermissions(final HttpServletRequest request, final PluginSettings settings) {
        final String username = userManager.getRemoteUsername(request);
        if (username == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        if (userManager.isSystemAdmin(username)) {
            return null;
        }

        final String blackDuckJiraGroupsString = (String) settings.get(BlackDuckConfigKeys.BLACKDUCK_CONFIG_GROUPS);

        if (StringUtils.isNotBlank(blackDuckJiraGroupsString)) {
            final String[] blackDuckJiraGroups = blackDuckJiraGroupsString.split(",");
            boolean userIsInGroups = false;
            for (final String blackDuckJiraGroup : blackDuckJiraGroups) {
                if (userManager.isUserInGroup(username, blackDuckJiraGroup.trim())) {
                    userIsInGroups = true;
                    break;
                }
            }
            if (userIsInGroups) {
                return null;
            }
        }
        return Response.status(Status.UNAUTHORIZED).build();
    }

    @Path("/read")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }

        final BlackDuckServerConfigSerializable config = (BlackDuckServerConfigSerializable) transactionTemplate.execute(() -> blackDuckConfigActions.getStoredBlackDuckConfig(settings));
        return Response.ok(config).build();
    }

    @Path("/save")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final BlackDuckServerConfigSerializable config, @Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }

        final BlackDuckServerConfigSerializable modifiedConfig = (BlackDuckServerConfigSerializable) transactionTemplate.execute(() -> blackDuckConfigActions.updateBlackDuckConfig(config, settings));
        if (modifiedConfig.hasErrors()) {
            return Response.ok(modifiedConfig).status(Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

    @Path("/testConnection")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response testConnection(final BlackDuckServerConfigSerializable config, @Context final HttpServletRequest request) {
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }

            final BlackDuckServerConfigSerializable modifiedConfig = (BlackDuckServerConfigSerializable) transactionTemplate.execute(() -> blackDuckConfigActions.testConnection(config, settings));
            if (modifiedConfig.hasErrors()) {
                return Response.ok(modifiedConfig).status(Status.BAD_REQUEST).build();
            }
            return Response.noContent().build();
        } catch (final Throwable t) {
            final StringBuilder sb = new StringBuilder();
            sb.append(t.getMessage());
            if (t.getCause() != null) {
                sb.append("; Caused by: ");
                sb.append(t.getCause().getMessage());
            }
            final String lowerCaseMessage = t.getMessage().toLowerCase();
            if (lowerCaseMessage.contains("ssl") || lowerCaseMessage.contains("pkix")) {
                config.setTrustCertError("There was an issue handling the certificate: " + sb.toString());
            } else {
                sb.insert(0, "Unexpected exception caught in testConnection(): ");
                config.setHubUrlError(sb.toString());
            }
            return Response.ok(config).status(Status.BAD_REQUEST).build();
        }
    }

}
