/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.data.accessor.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.data.accessor.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.web.action.AccessConfigActions;
import com.blackducksoftware.integration.jira.web.model.BlackDuckAdminConfigSerializable;
import com.synopsys.integration.rest.HttpMethod;

@Path("/config/access")
public class AccessConfigController extends ConfigController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    final AccessConfigActions accessConfigActions;
    final GroupPickerSearchService groupPickerSearchService;

    AccessConfigController(final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate, final UserManager userManager, final GroupPickerSearchService groupPickerSearchService) {
        super(pluginSettingsFactory, transactionTemplate, userManager);
        this.groupPickerSearchService = groupPickerSearchService;

        final JiraSettingsAccessor jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettingsFactory.createGlobalSettings());
        this.accessConfigActions = new AccessConfigActions(new GlobalConfigurationAccessor(jiraSettingsAccessor), getAuthorizationChecker(), groupPickerSearchService);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPluginAdminConfiguration(@Context final HttpServletRequest request) {
        final Object adminConfig;
        try {
            final boolean validAuthentication = isAuthorized(request);
            if (!validAuthentication) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            adminConfig = executeAsTransaction(() -> accessConfigActions.getConfigWithJiraGroups(request));
        } catch (final Exception e) {
            return createErrorResponse(HttpMethod.GET, e);
        }
        return Response.ok(adminConfig).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateBlackDuckAdminConfiguration(final BlackDuckAdminConfigSerializable adminConfig, @Context final HttpServletRequest request) {
        final Object responseObject;
        try {
            final boolean userAvailable = getAuthorizationChecker().isUserAvailable(request);
            if (!userAvailable) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            responseObject = executeAsTransaction(() -> accessConfigActions.updateConfigWithJiraGroups(request, adminConfig.getHubJiraGroups()));
        } catch (final Exception e) {
            return createErrorResponse(HttpMethod.PUT, e);
        }
        if (responseObject != null) {
            return Response.ok(responseObject).status(Response.Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

    private Response createErrorResponse(final HttpMethod httpMethod, final Throwable e) {
        final String msg = String.format("Exception performing %s : %s", httpMethod.name(), e.getMessage());
        logger.error(msg, e);
        final BlackDuckAdminConfigSerializable errorResponseObject = new BlackDuckAdminConfigSerializable();
        errorResponseObject.setHubJiraGroupsError(msg);
        return Response.ok(errorResponseObject).status(Response.Status.BAD_REQUEST).build();
    }

}
