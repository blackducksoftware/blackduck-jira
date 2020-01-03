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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.scheduler.SchedulerService;
import com.atlassian.scheduler.SchedulerServiceException;
import com.blackducksoftware.integration.jira.data.accessor.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.data.accessor.MigrationAccessor;
import com.blackducksoftware.integration.jira.task.maintenance.AlertMigrationRunner;
import com.blackducksoftware.integration.jira.web.action.MigrationActions;
import com.blackducksoftware.integration.jira.web.model.MigrationDetails;

@Path("/blackduck/migration")
public class MigrationController extends ConfigController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MigrationActions migrationActions;

    public MigrationController(PluginSettingsFactory pluginSettingsFactory, TransactionTemplate transactionTemplate, UserManager userManager, SchedulerService schedulerService) {
        super(pluginSettingsFactory, transactionTemplate, userManager);
        JiraSettingsAccessor jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettingsFactory.createGlobalSettings());
        MigrationAccessor migrationAccessor = new MigrationAccessor(jiraSettingsAccessor);
        this.migrationActions = new MigrationActions(schedulerService, jiraSettingsAccessor, migrationAccessor);
    }

    @Path("/details")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMigrationDetails(@Context HttpServletRequest request) {
        boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        MigrationDetails migrationDetails = migrationActions.getMigrationDetails();
        return Response.ok(migrationDetails).build();
    }

    @Path("/project")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeMigratedProject(String projectToDelete, @Context HttpServletRequest request) {
        boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        migrationActions.removeProjectsFromCompletedList(projectToDelete);

        return Response.ok().build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response startMigration(@Context HttpServletRequest request) {
        boolean validAuthentication = isAuthorized(request);
        if (!validAuthentication) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        try {
            migrationActions.startMigration();
        } catch (SchedulerServiceException e) {
            logger.error(String.format("Could not start %s.", AlertMigrationRunner.HUMAN_READABLE_TASK_NAME), e);
            return Response.ok(String.format("Could not start %s. Error: %s", AlertMigrationRunner.HUMAN_READABLE_TASK_NAME, e.getMessage())).status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok().build();
    }

}
