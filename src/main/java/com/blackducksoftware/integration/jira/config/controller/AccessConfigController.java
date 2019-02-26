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

import java.util.ArrayList;
import java.util.Collection;
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.config.JiraConfigErrorStrings;
import com.blackducksoftware.integration.jira.config.PluginConfigKeys;
import com.blackducksoftware.integration.jira.config.model.BlackDuckAdminConfigSerializable;

@Path("/config/access")
public class AccessConfigController extends ConfigController {
    // This must be "package protected" to avoid synthetic access
    final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));
    final GroupPickerSearchService groupPickerSearchService;

    AccessConfigController(final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate, final UserManager userManager, final GroupPickerSearchService groupPickerSearchService) {
        super(pluginSettingsFactory, transactionTemplate, userManager);
        this.groupPickerSearchService = groupPickerSearchService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPluginAdminConfiguration(@Context final HttpServletRequest request) {
        final Object adminConfig;
        try {
            final String username = getUserManager().getRemoteUsername(request);
            if (username == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            final boolean userIsSysAdmin = getUserManager().isSystemAdmin(username);
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final String blackDuckJiraGroupsString = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_GROUPS);

            if (!userIsSysAdmin) {
                if (StringUtils.isBlank(blackDuckJiraGroupsString)) {
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                } else {
                    final String[] blackDuckJiraGroups = blackDuckJiraGroupsString.split(",");
                    boolean userIsInGroups = false;
                    for (final String blackDuckJiraGroup : blackDuckJiraGroups) {
                        if (getUserManager().isUserInGroup(username, blackDuckJiraGroup.trim())) {
                            userIsInGroups = true;
                            break;
                        }
                    }
                    if (!userIsInGroups) {
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                }
            }

            adminConfig = getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final BlackDuckAdminConfigSerializable txAdminConfig = new BlackDuckAdminConfigSerializable();
                    txAdminConfig.setHubJiraGroups(blackDuckJiraGroupsString);
                    if (userIsSysAdmin) {
                        final List<String> jiraGroups = new ArrayList<>();

                        final Collection<Group> jiraGroupCollection = groupPickerSearchService.findGroups("");
                        if (jiraGroupCollection != null && !jiraGroupCollection.isEmpty()) {
                            for (final Group group : jiraGroupCollection) {
                                jiraGroups.add(group.getName());
                            }
                        }
                        txAdminConfig.setJiraGroups(jiraGroups);
                    }
                    return txAdminConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckAdminConfigSerializable errorAdminConfig = new BlackDuckAdminConfigSerializable();
            final String msg = "Error getting admin config: " + e.getMessage();
            logger.error(msg, e);
            errorAdminConfig.setHubJiraGroupsError(msg);
            return Response.ok(errorAdminConfig).build();
        }
        return Response.ok(adminConfig).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateBlackDuckAdminConfiguration(final BlackDuckAdminConfigSerializable adminConfig, @Context final HttpServletRequest request) {
        final Object responseObject;
        try {
            final String username = getUserManager().getRemoteUsername(request);
            if (username == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            final boolean userIsSysAdmin = getUserManager().isSystemAdmin(username);

            responseObject = getTransactionTemplate().execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
                    final BlackDuckAdminConfigSerializable txResponseObject = new BlackDuckAdminConfigSerializable();

                    if (!userIsSysAdmin) {
                        txResponseObject.setHubJiraGroupsError(JiraConfigErrorStrings.NON_SYSTEM_ADMINS_CANT_CHANGE_GROUPS);
                        return txResponseObject;
                    } else {
                        setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_GROUPS, adminConfig.getHubJiraGroups());
                    }
                    return null;
                }
            });
        } catch (final Exception e) {
            final String msg = "Exception during admin save: " + e.getMessage();
            logger.error(msg, e);
            final BlackDuckAdminConfigSerializable errorResponseObject = new BlackDuckAdminConfigSerializable();
            errorResponseObject.setHubJiraGroupsError(msg);
            return Response.ok(errorResponseObject).status(Response.Status.BAD_REQUEST).build();
        }
        if (responseObject != null) {
            return Response.ok(responseObject).status(Response.Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }
}
