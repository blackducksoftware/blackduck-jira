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

import java.util.Properties;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.common.WorkflowHelper;
import com.blackducksoftware.integration.jira.data.accessor.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.web.action.IssueFieldMappingConfigActions;
import com.blackducksoftware.integration.jira.web.action.ProjectMappingConfigActions;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.web.model.Fields;
import com.blackducksoftware.integration.jira.web.model.ProjectFieldCopyMapping;

@Path("/config/issue/field/mapping")
public class IssueFieldMappingConfigController extends ConfigController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    final FieldManager fieldManager;
    private final IssueFieldMappingConfigActions issueFieldMappingConfigActions;
    private final ProjectMappingConfigActions projectMappingConfigActions;

    public IssueFieldMappingConfigController(final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate, final UserManager userManager, final FieldManager fieldManager,
        final WorkflowManager workflowManager, final WorkflowSchemeManager workflowSchemeManager, final ProjectManager projectManager, final Properties i18nProperties) {
        super(pluginSettingsFactory, transactionTemplate, userManager);
        this.fieldManager = fieldManager;

        final JiraSettingsAccessor jiraSettingsAccessor = new JiraSettingsAccessor(pluginSettingsFactory.createGlobalSettings());
        this.issueFieldMappingConfigActions = new IssueFieldMappingConfigActions(jiraSettingsAccessor, i18nProperties, fieldManager);
        this.projectMappingConfigActions = new ProjectMappingConfigActions(jiraSettingsAccessor, new WorkflowHelper(workflowManager, workflowSchemeManager, projectManager));
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    // FIXME move this to a ProjectMappingConfigController in the next major version
    public Response getMappings(@Context final HttpServletRequest request) {
        final Object config;
        try {
            final boolean validAuthentication = isAuthorized(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            config = executeAsTransaction(() -> projectMappingConfigActions.getMappings());
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting project mappings: " + e.getMessage();
            logger.error(msg, e);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @Path("/sources")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSourceFields(@Context final HttpServletRequest request) {
        final Object sourceFields;
        try {
            final boolean validAuthentication = isAuthorized(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }

            sourceFields = executeAsTransaction(() -> issueFieldMappingConfigActions.getSourceFields());
        } catch (final Exception e) {
            final Fields errorSourceFields = new Fields();
            final String msg = "Error getting source fields: " + e.getMessage();
            logger.error(msg, e);
            errorSourceFields.setErrorMessage(msg);
            return Response.ok(errorSourceFields).build();
        }
        return Response.ok(sourceFields).build();
    }

    @Path("/targets")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTargetFields(@Context final HttpServletRequest request) {
        final Object targetFields;
        try {
            final boolean validAuthentication = isAuthorized(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }

            targetFields = executeAsTransaction(() -> issueFieldMappingConfigActions.getTargetFields());
        } catch (final Exception e) {
            final Fields errorTargetFields = new Fields();
            final String msg = "Error getting target fields: " + e.getMessage();
            logger.error(msg, e);
            errorTargetFields.setErrorMessage(msg);
            return Response.ok(errorTargetFields).build();
        }
        return Response.ok(targetFields).build();
    }

    @Path("/copies")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFieldCopyMappings(@Context final HttpServletRequest request) {
        final Object config;
        try {
            logger.debug("Get /copies");
            final boolean validAuthentication = isAuthorized(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            config = executeAsTransaction(() -> issueFieldMappingConfigActions.getFieldCopyMappings());
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting field mappings: " + e.getMessage();
            logger.error(msg, e);
            return Response.ok(errorConfig).build();
        }

        final BlackDuckJiraFieldCopyConfigSerializable returnValue = (BlackDuckJiraFieldCopyConfigSerializable) config;
        logger.debug("returnValue: " + returnValue);
        return Response.ok(config).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateFieldCopyConfiguration(final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig, @Context final HttpServletRequest request) {
        try {
            logger.debug("updateFieldCopyConfiguration() received " + fieldCopyConfig.getProjectFieldCopyMappings().size() + " rows.");
            logger.debug("fieldCopyConfig.getProjectFieldCopyMappings(): " + fieldCopyConfig.getProjectFieldCopyMappings());
            for (final ProjectFieldCopyMapping projectFieldCopyMapping : fieldCopyConfig.getProjectFieldCopyMappings()) {
                logger.debug("projectFieldCopyMapping: " + projectFieldCopyMapping);
            }

            final boolean validAuthentication = isAuthorized(request);
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            executeAsTransaction(() -> issueFieldMappingConfigActions.updateFieldCopyMappings(fieldCopyConfig));
        } catch (final Exception e) {
            final String msg = "Exception during admin save: " + e.getMessage();
            logger.error(msg, e);
            fieldCopyConfig.setErrorMessage(msg);
        }
        if (fieldCopyConfig.hasErrors()) {
            return Response.ok(fieldCopyConfig).status(Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

}
