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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.common.BlackDuckPluginDateFormatter;
import com.blackducksoftware.integration.jira.data.accessor.PluginConfigurationAccessor;
import com.blackducksoftware.integration.jira.task.BlackDuckMonitor;
import com.blackducksoftware.integration.jira.web.BlackDuckPluginVersion;
import com.blackducksoftware.integration.jira.web.TicketCreationError;
import com.blackducksoftware.integration.jira.web.model.TicketCreationErrorSerializable;

@Path("/")
public class BlackDuckJiraConfigController extends ConfigController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    // These must be "package protected" to avoid synthetic access
    final ProjectManager projectManager;
    final GroupPickerSearchService groupPickerSearchService;
    final FieldManager fieldManager;

    public BlackDuckJiraConfigController(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate, final ProjectManager projectManager,
        final BlackDuckMonitor blackDuckMonitor, final GroupPickerSearchService groupPickerSearchService, final FieldManager fieldManager) {
        super(pluginSettingsFactory, transactionTemplate, userManager);
        this.projectManager = projectManager;
        this.groupPickerSearchService = groupPickerSearchService;
        this.fieldManager = fieldManager;
    }

    @Path("config/pluginInfo")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPluginVersion(@Context final HttpServletRequest request) {
        final Object pluginInfo;
        try {
            final boolean validAuthentication = isAuthorized(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            pluginInfo = executeAsTransaction(() -> {
                logger.debug("Getting plugin version string");
                final String pluginVersion = BlackDuckPluginVersion.getVersion();
                logger.debug("pluginVersion: " + pluginVersion);
                return pluginVersion;
            });
        } catch (final Exception e) {
            final String msg = "Error getting Plugin info: " + e.getMessage();
            logger.error(msg, e);
            return Response.ok("<unknown>").build();
        }
        return Response.ok(pluginInfo).build();
    }

    @Path("/config/errors")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeErrors(final TicketCreationErrorSerializable errorsToDelete, @Context final HttpServletRequest request) {
        final boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        final Object obj = executeAsTransaction(() -> {
            final PluginConfigurationAccessor pluginConfigurationAccessor = getJiraSettingsAccessor().createPluginConfigurationAccessor();
            final Object errorObject = pluginConfigurationAccessor.getPluginError();

            List<TicketCreationError> ticketErrors = null;
            if (errorObject != null) {
                final String errorString = (String) errorObject;
                try {
                    ticketErrors = TicketCreationError.fromJson(errorString);
                } catch (final Exception e) {
                    ticketErrors = new ArrayList<>();
                }
            } else {
                ticketErrors = new ArrayList<>();
            }

            if (errorsToDelete.getHubJiraTicketErrors() != null && !errorsToDelete.getHubJiraTicketErrors().isEmpty()) {
                for (final TicketCreationError creationError : errorsToDelete.getHubJiraTicketErrors()) {
                    try {
                        final String errorMessage = URLDecoder.decode(creationError.getStackTrace(), "UTF-8");
                        final Iterator<TicketCreationError> iterator = ticketErrors.iterator();
                        while (iterator.hasNext()) {
                            final TicketCreationError error = iterator.next();
                            if (errorMessage.equals(error.getStackTrace())) {
                                iterator.remove();
                                break;
                            }
                        }
                    } catch (final UnsupportedEncodingException e) {

                    }
                }
            }
            pluginConfigurationAccessor.setPluginError(TicketCreationError.toJson(ticketErrors));
            return null;
        });
        if (obj != null) {
            return Response.ok(obj).status(Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

    @Path("config/reset")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resetPluginKeys(final Object object, @Context final HttpServletRequest request) {
        logger.debug("Reset called with parameter: " + object);
        final Object responseString;
        try {
            final boolean validAuthentication = isAuthorized(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }

            responseString = executeAsTransaction(() -> {
                final PluginConfigurationAccessor pluginConfigurationAccessor = getJiraSettingsAccessor().createPluginConfigurationAccessor();
                try {
                    final Date now = new Date();
                    final String oldLastRunDateString = pluginConfigurationAccessor.getLastRunDate();
                    final String newLastRunDateString = new BlackDuckPluginDateFormatter().format(now);
                    logger.warn("Resetting last run date from " + oldLastRunDateString + " to " + newLastRunDateString + "; this will skip over any notifications generated between those times");

                    pluginConfigurationAccessor.setLastRunDate(newLastRunDateString);
                    pluginConfigurationAccessor.setPluginError(null);
                } catch (final Exception e) {
                    return e.getMessage();
                }
                return null;
            });
        } catch (final Exception e) {
            final String msg = "Exception during reset: " + e.getMessage();
            logger.error(msg, e);
            return Response.ok(msg).status(Status.BAD_REQUEST).build();
        }
        if (responseString != null) {
            return Response.ok(responseString).status(Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

}
