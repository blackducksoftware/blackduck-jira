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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.common.PluginSettingsWrapper;
import com.blackducksoftware.integration.jira.config.controller.action.IssueCreationConfigActions;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.task.BlackDuckMonitor;

@Path("/config/issue/creator")
public class IssueCreationConfigController extends ConfigController {

    final ProjectManager projectManager;
    private final IssueCreationConfigActions issueCreationConfigActions;

    public IssueCreationConfigController(final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate, final UserManager userManager, final ProjectManager projectManager,
        final BlackDuckMonitor blackDuckMonitor) {
        super(pluginSettingsFactory, transactionTemplate, userManager);
        this.projectManager = projectManager;
        issueCreationConfigActions = new IssueCreationConfigActions(pluginSettingsFactory, getAuthenticationChecker(), projectManager, blackDuckMonitor);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreator(@Context final HttpServletRequest request) {
        final Object config;
        try {
            final boolean validAuthentication = isValidAuthentication(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            config = executeAsTransaction(() -> issueCreationConfigActions.getCreator());
        } catch (final Exception e) {
            return createGeneralError("creator", e);
        }
        return Response.ok(config).build();
    }

    @Path("/candidates")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreatorCandidates(@Context final HttpServletRequest request) {
        logger.debug("getCreatorCandidates()");
        final Object projectsConfig;
        try {
            final boolean validAuthentication = isValidAuthentication(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            projectsConfig = executeAsTransaction(() -> issueCreationConfigActions.getCreatorCandidates());
        } catch (final Exception e) {
            return createGeneralError("issue creator candidates", e);
        }
        return Response.ok(projectsConfig).build();
    }

    @Path("/jira/projects")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJiraProjects(@Context final HttpServletRequest request) {
        final Object projectsConfig;
        try {
            final boolean validAuthentication = isValidAuthentication(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            projectsConfig = executeAsTransaction(() -> issueCreationConfigActions.getJiraProjects());
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting JIRA projects config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setJiraProjectsError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(projectsConfig).build();
    }

    @Path("/vulnerability/ticketchoice")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreateVulnerabilityTicketsChoice(@Context final HttpServletRequest request) {
        logger.debug("GET /vulnerability/ticketchoice");
        final Object config;
        try {
            final boolean validAuthentication = isValidAuthentication(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            config = executeAsTransaction(() -> issueCreationConfigActions.getCreateVulnerabilityTickets());
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting 'create vulnerability issues' choice: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setCreateVulnerabilityIssuesError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @Path("/comment/updatechoice")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCommentOnIssueUpdatesChoice(@Context final HttpServletRequest request) {
        logger.debug("GET /comment/updatechoice");
        final Object config;
        try {
            final boolean validAuthentication = isValidAuthentication(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            config = executeAsTransaction(() -> issueCreationConfigActions.getCommentOnIssueUpdates());
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting 'comment on issue updates' choice: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setCommentOnIssueUpdatesChoiceError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @Path("/interval")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInterval(@Context final HttpServletRequest request) {
        final Object config;
        try {
            final boolean validAuthentication = isValidAuthentication(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            config = executeAsTransaction(() -> issueCreationConfigActions.getInterval());
        } catch (final Exception e) {
            return createGeneralError("interval", e);
        }
        return Response.ok(config).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final BlackDuckJiraConfigSerializable config, @Context final HttpServletRequest request) {
        try {
            final boolean validAuthentication = isValidAuthentication(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            executeAsTransaction(() -> issueCreationConfigActions.updateConfig(config, request));
        } catch (final Exception e) {
            final String msg = "Exception during save: " + e.getMessage();
            logger.error(msg, e);
            config.setErrorMessage(msg);
        }
        if (config.hasErrors()) {
            logger.error("There are one or more errors in the configuration: " + config.getConsolidatedErrorMessage());
            config.enhanceMappingErrorMessage();
            return Response.ok(config).status(Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

    private Response createGeneralError(final String fieldName, final Throwable e) {
        final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
        final String msg = String.format("Error getting {} config: ", fieldName, e.getMessage());
        logger.error(msg, e);
        errorConfig.setGeneralSettingsError(msg);
        return Response.ok(errorConfig).build();
    }

    private boolean isValidAuthentication(final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final PluginSettingsWrapper pluginSettingsWrapper = new PluginSettingsWrapper(settings);
        return getAuthenticationChecker().isValidAuthentication(request, pluginSettingsWrapper.getParsedBlackDuckConfigGroups());
    }

}
