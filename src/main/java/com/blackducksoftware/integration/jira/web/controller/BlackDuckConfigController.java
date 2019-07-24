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

import java.util.Collections;
import java.util.List;

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

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.blackduck.BlackDuckConnectionHelper;
import com.blackducksoftware.integration.jira.data.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.data.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.web.TicketCreationError;
import com.blackducksoftware.integration.jira.web.action.BlackDuckConfigActions;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.web.model.BlackDuckServerConfigSerializable;
import com.blackducksoftware.integration.jira.web.model.TicketCreationErrorSerializable;

@Path("/config/blackduck")
public class BlackDuckConfigController extends ConfigController {
    private final JiraSettingsAccessor jiraSettingsAccessor;
    private final BlackDuckConfigActions blackDuckConfigActions;

    public BlackDuckConfigController(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate) {
        super(pluginSettingsFactory, transactionTemplate, userManager);
        this.jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettingsFactory.createGlobalSettings());
        this.blackDuckConfigActions = new BlackDuckConfigActions(jiraSettingsAccessor, new BlackDuckConnectionHelper());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context final HttpServletRequest request) {
        final boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        final BlackDuckServerConfigSerializable config = executeAsTransaction(() -> blackDuckConfigActions.getStoredBlackDuckConfig());
        return Response.ok(config).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final BlackDuckServerConfigSerializable config, @Context final HttpServletRequest request) {
        final boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        final BlackDuckServerConfigSerializable modifiedConfig = executeAsTransaction(() -> blackDuckConfigActions.updateBlackDuckConfig(config));
        if (modifiedConfig.hasErrors()) {
            return Response.ok(modifiedConfig).status(Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

    @Path("/test")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response testConnection(final BlackDuckServerConfigSerializable config, @Context final HttpServletRequest request) {
        try {
            final boolean validAuthentication = isAuthorized(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }

            final BlackDuckServerConfigSerializable modifiedConfig = executeAsTransaction(() -> blackDuckConfigActions.testConnection(config));
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

    @Path("/projects")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlackDuckProjects(@Context final HttpServletRequest request) {
        logger.debug("getBlackDuckProjects()");
        final Object projectsConfig;
        try {
            final boolean validAuthentication = isAuthorized(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            projectsConfig = executeAsTransaction(() -> blackDuckConfigActions.getBlackDuckProjects());
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting Black Duck projects config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setHubProjectsError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(projectsConfig).build();
    }

    @Path("/policies")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlackDuckPolicies(@Context final HttpServletRequest request) {
        final Object config;
        try {
            final boolean validAuthentication = isAuthorized(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            config = executeAsTransaction(() -> blackDuckConfigActions.getBlackDuckPolicies());
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting policies: " + e.getMessage();
            logger.error(msg, e);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @Path("/ticket/errors")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlackDuckJiraTicketErrors(@Context final HttpServletRequest request) {
        final boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        final Object obj = executeAsTransaction(() -> {
            final TicketCreationErrorSerializable creationError = new TicketCreationErrorSerializable();

            final PluginSettings settings = getPluginSettingsFactory().createGlobalSettings();
            final List<TicketCreationError> ticketErrors = PluginErrorAccessor.expireOldErrors(jiraSettingsAccessor);
            if (ticketErrors != null) {
                Collections.sort(ticketErrors);
                creationError.setHubJiraTicketErrors(ticketErrors);
                logger.debug("Errors to UI : " + creationError.getHubJiraTicketErrors().size());
            }
            return creationError;
        });

        return Response.ok(obj).build();
    }

}
