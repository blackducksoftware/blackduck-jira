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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.BlackDuckPluginVersion;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckPluginDateFormatter;
import com.blackducksoftware.integration.jira.common.exception.JiraException;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProject;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.common.model.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.config.BlackDuckConfigKeys;
import com.blackducksoftware.integration.jira.config.ErrorTracking;
import com.blackducksoftware.integration.jira.config.IdToNameMappingByNameComparator;
import com.blackducksoftware.integration.jira.config.JiraConfigErrorStrings;
import com.blackducksoftware.integration.jira.config.JiraServices;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.PluginConfigKeys;
import com.blackducksoftware.integration.jira.config.TicketCreationError;
import com.blackducksoftware.integration.jira.config.model.BlackDuckAdminConfigSerializable;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.config.model.Fields;
import com.blackducksoftware.integration.jira.config.model.IdToNameMapping;
import com.blackducksoftware.integration.jira.config.model.PluginInfoSerializable;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.config.model.TicketCreationErrorSerializable;
import com.blackducksoftware.integration.jira.task.BlackDuckMonitor;
import com.blackducksoftware.integration.jira.task.issue.ui.JiraFieldUtils;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleViewV2;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.view.HubViewFilter;
import com.synopsys.integration.blackduck.api.view.MetaHandler;
import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.configuration.HubServerConfigBuilder;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@Path("/")
public class BlackDuckJiraConfigController {
    // This must be "package protected" to avoid synthetic access
    final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    // These must be "package protected" to avoid synthetic access
    final PluginSettingsFactory pluginSettingsFactory;
    final ProjectManager projectManager;
    final GroupPickerSearchService groupPickerSearchService;
    final FieldManager fieldManager;

    private final UserManager userManager;
    private final TransactionTemplate transactionTemplate;
    private final BlackDuckMonitor blackDuckMonitor;
    private final Properties i18nProperties;

    public BlackDuckJiraConfigController(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate, final ProjectManager projectManager,
        final BlackDuckMonitor blackDuckMonitor, final GroupPickerSearchService groupPickerSearchService, final FieldManager fieldManager) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
        this.projectManager = projectManager;
        this.blackDuckMonitor = blackDuckMonitor;
        this.groupPickerSearchService = groupPickerSearchService;
        this.fieldManager = fieldManager;

        i18nProperties = new Properties();
        populateI18nProperties();
    }

    private void populateI18nProperties() {
        try (final InputStream stream = ClassLoaderUtils.getResourceAsStream(BlackDuckJiraConstants.PROPERTY_FILENAME, this.getClass())) {
            if (stream != null) {
                i18nProperties.load(stream);
            } else {
                logger.warn("Error opening property file: " + BlackDuckJiraConstants.PROPERTY_FILENAME);
            }
        } catch (final IOException e) {
            logger.warn("Error reading property file: " + BlackDuckJiraConstants.PROPERTY_FILENAME);
        }
        logger.debug("i18nProperties: " + i18nProperties);
    }

    // This must be "package protected" to avoid synthetic access
    String getI18nProperty(final String key) {
        if (i18nProperties == null) {
            return key;
        }
        final String value = i18nProperties.getProperty(key);
        if (value == null) {
            return key;
        }
        return value;
    }

    private Response checkUserPermissions(final HttpServletRequest request, final PluginSettings settings) {
        final String username = userManager.getRemoteUsername(request);
        if (isUserAuthorizedForPlugin(settings, username)) {
            return null;
        }
        return Response.status(Status.UNAUTHORIZED).build();
    }

    private boolean isUserAuthorizedForPlugin(final PluginSettings settings, final String username) {
        if (username == null) {
            return false;
        }
        if (userManager.isSystemAdmin(username)) {
            return true;
        }
        final String blackDuckJiraGroupsString = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_GROUPS);
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
                return true;
            }
        }
        return false;
    }

    @Path("/admin")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPluginAdminConfiguration(@Context final HttpServletRequest request) {
        final Object adminConfig;
        try {
            final String username = userManager.getRemoteUsername(request);
            if (username == null) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            final boolean userIsSysAdmin = userManager.isSystemAdmin(username);
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final String blackDuckJiraGroupsString = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_GROUPS);

            if (!userIsSysAdmin) {
                if (StringUtils.isBlank(blackDuckJiraGroupsString)) {
                    return Response.status(Status.UNAUTHORIZED).build();
                } else {
                    final String[] blackDuckJiraGroups = blackDuckJiraGroupsString.split(",");
                    boolean userIsInGroups = false;
                    for (final String blackDuckJiraGroup : blackDuckJiraGroups) {
                        if (userManager.isUserInGroup(username, blackDuckJiraGroup.trim())) {
                            userIsInGroups = true;
                            break;
                        }
                    }
                    if (!userIsInGroups) {
                        return Response.status(Status.UNAUTHORIZED).build();
                    }
                }
            }

            adminConfig = transactionTemplate.execute(new TransactionCallback() {
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

    @Path("/blackDuckJiraTicketErrors")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlackDuckJiraTicketErrors(@Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }
        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {
                final TicketCreationErrorSerializable creationError = new TicketCreationErrorSerializable();

                final List<TicketCreationError> ticketErrors = JiraSettingsService.expireOldErrors(settings);
                if (ticketErrors != null) {
                    Collections.sort(ticketErrors);
                    creationError.setHubJiraTicketErrors(ticketErrors);
                    logger.debug("Errors to UI : " + creationError.getHubJiraTicketErrors().size());
                }
                return creationError;
            }
        });

        return Response.ok(obj).build();
    }

    @Path("/interval")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInterval(@Context final HttpServletRequest request) {
        final Object config;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            config = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();

                    final String intervalBetweenChecks = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);

                    txConfig.setIntervalBetweenChecks(intervalBetweenChecks);

                    validateInterval(txConfig);
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting interval config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setGeneralSettingsError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @Path("/creator")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreator(@Context final HttpServletRequest request) {
        final Object config;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            config = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();
                    final String creator = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ISSUE_CREATOR_USER);
                    txConfig.setCreator(creator);
                    validateCreator(txConfig);
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting creator config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setGeneralSettingsError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @Path("/jiraProjects")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJiraProjects(@Context final HttpServletRequest request) {
        final Object projectsConfig;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            projectsConfig = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final List<JiraProject> jiraProjects = getJiraProjects(projectManager.getProjectObjects());

                    final BlackDuckJiraConfigSerializable txProjectsConfig = new BlackDuckJiraConfigSerializable();
                    txProjectsConfig.setJiraProjects(jiraProjects);

                    if (jiraProjects.size() == 0) {
                        txProjectsConfig.setJiraProjectsError(JiraConfigErrorStrings.NO_JIRA_PROJECTS_FOUND);
                    }
                    return txProjectsConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting JIRA projects config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setJiraProjectsError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(projectsConfig).build();
    }

    @Path("/blackDuckProjects")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlackDuckProjects(@Context final HttpServletRequest request) {
        logger.debug("getBlackDuckProjects()");
        final Object projectsConfig;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            projectsConfig = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
                    config.setHubProjects(new ArrayList<>(0));

                    final HubServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(settings, config);
                    if (blackDuckServicesFactory == null) {
                        return config;
                    }

                    final List<BlackDuckProject> blackDuckProjects = getBlackDuckProjects(blackDuckServicesFactory, config);
                    config.setHubProjects(blackDuckProjects);

                    if (blackDuckProjects.size() == 0) {
                        config.setHubProjectsError(JiraConfigErrorStrings.NO_BLACKDUCK_PROJECTS_FOUND);
                    }
                    closeRestConnection(blackDuckServicesFactory.getRestConnection());
                    return config;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting Black Duck projects config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setHubProjectsError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(projectsConfig).build();
    }

    @Path("/pluginInfo")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPluginVersion(@Context final HttpServletRequest request) {
        final Object pluginInfo;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            pluginInfo = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final PluginInfoSerializable txPluginInfo = new PluginInfoSerializable();

                    logger.debug("Getting plugin version string");
                    final String pluginVersion = BlackDuckPluginVersion.getVersion();
                    logger.debug("pluginVersion: " + pluginVersion);
                    txPluginInfo.setPluginVersion(pluginVersion);
                    return txPluginInfo;
                }
            });
        } catch (final Exception e) {
            final PluginInfoSerializable errorPluginInfo = new PluginInfoSerializable();
            final String msg = "Error getting Plugin info: " + e.getMessage();
            logger.error(msg, e);
            errorPluginInfo.setPluginVersion("<unknown>");
            return Response.ok(errorPluginInfo).build();
        }
        return Response.ok(pluginInfo).build();
    }

    @Path("/sourceFields")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSourceFields(@Context final HttpServletRequest request) {
        final Object sourceFields;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }

            sourceFields = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final Fields txSourceFields = new Fields();
                    logger.debug("Adding source fields");
                    txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_LICENSE_NAMES.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_LICENSE_NAMES.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_USAGE.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_USAGE.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_OWNER.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_OWNER.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME.getId(), getI18nProperty(PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME.getLongNameProperty())));
                    Collections.sort(txSourceFields.getIdToNameMappings(), new IdToNameMappingByNameComparator());
                    logger.debug("sourceFields: " + txSourceFields);
                    return txSourceFields;
                }
            });
        } catch (final Exception e) {
            final Fields errorSourceFields = new Fields();
            final String msg = "Error getting source fields: " + e.getMessage();
            logger.error(msg, e);
            errorSourceFields.setErrorMessage(msg);
            return Response.ok(errorSourceFields).build();
        }
        return Response.ok(sourceFields).build();
    }

    @Path("/targetFields")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTargetFields(@Context final HttpServletRequest request) {
        final Object targetFields;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }

            targetFields = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    Fields txTargetFields;
                    try {
                        txTargetFields = JiraFieldUtils.getTargetFields(logger, fieldManager);
                    } catch (final JiraException e) {
                        txTargetFields = new Fields();
                        txTargetFields.setErrorMessage("Error getting target field list: " + e.getMessage());
                        return txTargetFields;
                    }
                    Collections.sort(txTargetFields.getIdToNameMappings(), new IdToNameMappingByNameComparator());
                    logger.debug("targetFields: " + txTargetFields);
                    return txTargetFields;
                }
            });
        } catch (final Exception e) {
            final Fields errorTargetFields = new Fields();
            final String msg = "Error getting target fields: " + e.getMessage();
            logger.error(msg, e);
            errorTargetFields.setErrorMessage(msg);
            return Response.ok(errorTargetFields).build();
        }
        return Response.ok(targetFields).build();
    }

    @Path("/blackDuckPolicies")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlackDuckPolicies(@Context final HttpServletRequest request) {
        final Object config;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            config = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final String policyRulesJson = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_POLICY_RULES_JSON);
                    final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();

                    if (StringUtils.isNotBlank(policyRulesJson)) {
                        txConfig.setPolicyRulesJson(policyRulesJson);
                    } else {
                        txConfig.setPolicyRules(new ArrayList<>(0));
                    }

                    final HubServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(settings, txConfig);
                    if (blackDuckServicesFactory == null) {
                        return txConfig;
                    }
                    setBlackDuckPolicyRules(blackDuckServicesFactory, txConfig);
                    closeRestConnection(blackDuckServicesFactory.getRestConnection());
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting policies: " + e.getMessage();
            logger.error(msg, e);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @Path("/createVulnerabilityTicketsChoice")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreateVulnerabilityTicketsChoice(@Context final HttpServletRequest request) {
        logger.debug("GET createVulnerabilityTicketsChoice");
        final Object config;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            config = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    logger.debug("GET createVulnerabilityTicketsChoice transaction");
                    final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();
                    final String createVulnIssuesChoiceString = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_CREATE_VULN_ISSUES_CHOICE);
                    logger.debug("createVulnIssuesChoiceString: " + createVulnIssuesChoiceString);
                    boolean choice = true;
                    if ("false".equalsIgnoreCase(createVulnIssuesChoiceString)) {
                        choice = false;
                    }
                    logger.debug("choice: " + choice);
                    txConfig.setCreateVulnerabilityIssues(choice);
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting 'create vulnerability issues' choice: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setCreateVulnerabilityIssuesError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @Path("/mappings")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappings(@Context final HttpServletRequest request) {
        final Object config;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            config = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();
                    final String blackDuckProjectMappingsJson = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);

                    txConfig.setHubProjectMappingsJson(blackDuckProjectMappingsJson);

                    validateMapping(txConfig);
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting project mappings: " + e.getMessage();
            logger.error(msg, e);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(config).build();
    }

    @Path("/fieldCopyMappings")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFieldCopyMappings(@Context final HttpServletRequest request) {
        Object config = null;
        try {
            logger.debug("Get /fieldCopyMappings");
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            config = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final BlackDuckJiraFieldCopyConfigSerializable txConfig = new BlackDuckJiraFieldCopyConfigSerializable();
                    final String blackDuckFieldCopyMappingsJson = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_FIELD_COPY_MAPPINGS_JSON);

                    logger.debug("Get /fieldCopyMappings returning JSON: " + blackDuckFieldCopyMappingsJson);
                    txConfig.setJson(blackDuckFieldCopyMappingsJson);
                    logger.debug("BlackDuckJiraFieldCopyConfigSerializable.getJson(): " + txConfig.getJson());
                    return txConfig;
                }
            });
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

    @Path("/creatorCandidates")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreatorCandidates(@Context final HttpServletRequest request) {
        logger.debug("getCreatorCandidates()");
        final Object projectsConfig;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            projectsConfig = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
                    config.setCreatorCandidates(new TreeSet<String>());

                    final SortedSet<String> creatorCandidates = getIssueCreatorCandidates(settings);
                    config.setCreatorCandidates(creatorCandidates);

                    if (creatorCandidates.size() == 0) {
                        config.setGeneralSettingsError(JiraConfigErrorStrings.NO_CREATOR_CANDIDATES_FOUND);
                    }
                    return config;
                }
            });
        } catch (final Exception e) {
            final BlackDuckJiraConfigSerializable errorConfig = new BlackDuckJiraConfigSerializable();
            final String msg = "Error getting issue creator candidates config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setGeneralSettingsError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(projectsConfig).build();
    }

    // This must be "package protected" to avoid synthetic access
    SortedSet<String> getIssueCreatorCandidates(final PluginSettings settings) {
        final SortedSet<String> jiraUsernames = new TreeSet<>();
        final String groupList = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_GROUPS);
        if (!StringUtils.isBlank(groupList)) {
            final String[] groupNames = groupList.split(",");
            for (final String groupName : groupNames) {
                jiraUsernames.addAll(new JiraServices().getGroupManager().getUserNamesInGroup(groupName));
            }
        }
        logger.debug("getJiraUsernames(): returning: " + jiraUsernames);
        return jiraUsernames;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final BlackDuckJiraConfigSerializable config, @Context final HttpServletRequest request) {
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final String username = userManager.getRemoteUsername(request);
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final List<JiraProject> jiraProjects = getJiraProjects(projectManager.getProjectObjects());
                    config.setJiraProjects(jiraProjects);
                    final HubServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactory(settings, config);
                    if (blackDuckServicesFactory == null) {
                        return config;
                    }
                    final List<BlackDuckProject> blackDuckProjects = getBlackDuckProjects(blackDuckServicesFactory, config);
                    closeRestConnection(blackDuckServicesFactory.getRestConnection());

                    config.setHubProjects(blackDuckProjects);
                    validateInterval(config);
                    validateCreator(config, settings);
                    validateMapping(config);
                    if (getValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME) == null) {
                        setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME, BlackDuckPluginDateFormatter.format(new Date()));
                    }
                    final String previousInterval = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, config.getIntervalBetweenChecks());
                    final String issueCreatorJiraUser = config.getCreator();
                    logger.debug("Setting issue creator jira user to: " + issueCreatorJiraUser);
                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ISSUE_CREATOR_USER, issueCreatorJiraUser);
                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_POLICY_RULES_JSON, config.getPolicyRulesJson());
                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, config.getHubProjectMappingsJson());
                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ADMIN_USER, username);
                    updatePluginTaskInterval(previousInterval, config.getIntervalBetweenChecks());
                    logger.debug("User input: createVulnerabilityIssues: " + config.isCreateVulnerabilityIssues());
                    final Boolean createVulnerabilityIssuesChoice = config.isCreateVulnerabilityIssues();
                    logger.debug("Setting createVulnerabilityIssuesChoice to " + createVulnerabilityIssuesChoice.toString());
                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_CREATE_VULN_ISSUES_CHOICE, createVulnerabilityIssuesChoice.toString());

                    return null;
                }
            });
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

    @Path("/removeErrors")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeErrors(final TicketCreationErrorSerializable errorsToDelete, @Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }
        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {

                final Object errorObject = getValue(settings, BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR);

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
                setValue(settings, BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR, TicketCreationError.toJson(ticketErrors));
                return null;
            }
        });
        if (obj != null) {
            return Response.ok(obj).status(Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

    @Path("/admin")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateBlackDuckAdminConfiguration(final BlackDuckAdminConfigSerializable adminConfig, @Context final HttpServletRequest request) {
        final Object responseObject;
        try {
            final String username = userManager.getRemoteUsername(request);
            if (username == null) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            final boolean userIsSysAdmin = userManager.isSystemAdmin(username);

            responseObject = transactionTemplate.execute(new TransactionCallback() {
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
            return Response.ok(errorResponseObject).status(Status.BAD_REQUEST).build();
        }
        if (responseObject != null) {
            return Response.ok(responseObject).status(Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

    @Path("/updateFieldCopyMappings")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateFieldCopyConfiguration(final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig, @Context final HttpServletRequest request) {
        try {
            logger.debug("updateFieldCopyConfiguration() received " + fieldCopyConfig.getProjectFieldCopyMappings().size() + " rows.");
            logger.debug("fieldCopyConfig.getProjectFieldCopyMappings(): " + fieldCopyConfig.getProjectFieldCopyMappings());
            for (final ProjectFieldCopyMapping projectFieldCopyMapping : fieldCopyConfig.getProjectFieldCopyMappings()) {
                logger.debug("projectFieldCopyMapping: " + projectFieldCopyMapping);
            }

            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }
            transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    if (!isValid(fieldCopyConfig)) {
                        return null;
                    }

                    setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_FIELD_COPY_MAPPINGS_JSON, fieldCopyConfig.getJson());
                    return null;
                }
            });
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

    // This must be "package protected" to avoid synthetic access
    boolean isValid(final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig) {
        if (fieldCopyConfig.getProjectFieldCopyMappings().size() == 0) {
            fieldCopyConfig.setErrorMessage(JiraConfigErrorStrings.NO_VALID_FIELD_CONFIGURATIONS);
            return false;
        }

        for (final ProjectFieldCopyMapping projectFieldCopyMapping : fieldCopyConfig.getProjectFieldCopyMappings()) {
            if (StringUtils.isBlank(projectFieldCopyMapping.getSourceFieldId())) {
                fieldCopyConfig.setErrorMessage(JiraConfigErrorStrings.FIELD_CONFIGURATION_INVALID_SOURCE_FIELD);
                return false;
            }
            if (StringUtils.isBlank(projectFieldCopyMapping.getTargetFieldId())) {
                fieldCopyConfig.setErrorMessage(JiraConfigErrorStrings.FIELD_CONFIGURATION_INVALID_TARGET_FIELD);
                return false;
            }
            if (StringUtils.isBlank(projectFieldCopyMapping.getSourceFieldName())) {
                fieldCopyConfig.setErrorMessage(JiraConfigErrorStrings.FIELD_CONFIGURATION_INVALID_SOURCE_FIELD);
                return false;
            }
            if (StringUtils.isBlank(projectFieldCopyMapping.getTargetFieldName())) {
                fieldCopyConfig.setErrorMessage(JiraConfigErrorStrings.FIELD_CONFIGURATION_INVALID_TARGET_FIELD);
                return false;
            }
        }
        return true;
    }

    @Path("/reset")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resetPluginKeys(final Object object, @Context final HttpServletRequest request) {
        logger.debug("Reset called with parameter: " + object);
        final Object responseString;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }

            responseString = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    try {
                        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
                        final Date now = new Date();
                        final String oldLastRunDateString = getStringValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_LAST_RUN_DATE);
                        final String newLastRunDateString = BlackDuckPluginDateFormatter.format(now);
                        logger.warn("Resetting last run date from " + oldLastRunDateString + " to " + newLastRunDateString + "; this will skip over any notifications generated between those times");
                        setValue(settings, PluginConfigKeys.BLACKDUCK_CONFIG_LAST_RUN_DATE, newLastRunDateString);
                        setValue(settings, BlackDuckJiraConstants.BLACKDUCK_JIRA_ERROR, null);
                    } catch (final Exception e) {
                        return e.getMessage();
                    }
                    return null;
                }
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

    // This must be "package protected" to avoid synthetic access
    void updatePluginTaskInterval(final String previousIntervalString, final String newIntervalString) {
        final int previousInterval = NumberUtils.toInt(previousIntervalString);
        final int newInterval;
        try {
            newInterval = stringToInteger(newIntervalString);
            if (newInterval > 0 && newInterval != previousInterval) {
                blackDuckMonitor.changeInterval();
            }
        } catch (final IllegalArgumentException e) {
            logger.error("The specified interval is not an integer.");
        }
    }

    // This must be "package protected" to avoid synthetic access
    void validateInterval(final BlackDuckJiraConfigSerializable config) {
        if (StringUtils.isBlank(config.getIntervalBetweenChecks())) {
            config.setGeneralSettingsError(JiraConfigErrorStrings.NO_INTERVAL_FOUND_ERROR);
        } else {
            try {
                final int interval = stringToInteger(config.getIntervalBetweenChecks());
                if (interval <= 0) {
                    config.setGeneralSettingsError(JiraConfigErrorStrings.INVALID_INTERVAL_FOUND_ERROR);
                }
            } catch (final IllegalArgumentException e) {
                config.setGeneralSettingsError(e.getMessage());
            }
        }
    }

    // This must be "package protected" to avoid synthetic access
    void validateCreator(final BlackDuckJiraConfigSerializable config) {
        if (StringUtils.isBlank(config.getCreator())) {
            config.setGeneralSettingsError(JiraConfigErrorStrings.NO_CREATOR_SPECIFIED_ERROR);
        }
    }

    // This must be "package protected" to avoid synthetic access
    void validateCreator(final BlackDuckJiraConfigSerializable config, final PluginSettings settings) {
        if (StringUtils.isBlank(config.getCreator())) {
            config.setGeneralSettingsError(JiraConfigErrorStrings.NO_CREATOR_SPECIFIED_ERROR);
        }
        if (isUserAuthorizedForPlugin(settings, config.getCreator())) {
            return;
        } else {
            config.setGeneralSettingsError(JiraConfigErrorStrings.UNAUTHORIZED_CREATOR_ERROR);
        }
    }

    // This must be "package protected" to avoid synthetic access
    void validateMapping(final BlackDuckJiraConfigSerializable config) {
        if (config.getHubProjectMappings() != null && !config.getHubProjectMappings().isEmpty()) {
            boolean hasEmptyMapping = false;
            for (final BlackDuckProjectMapping mapping : config.getHubProjectMappings()) {
                boolean jiraProjectBlank = true;
                boolean blackDuckProjectBlank = true;
                if (mapping.getJiraProject() != null) {
                    if (mapping.getJiraProject().getProjectId() != null) {
                        jiraProjectBlank = false;
                    }
                }
                if (mapping.getHubProject() != null) {
                    if (StringUtils.isNotBlank(mapping.getHubProject().getProjectUrl())) {
                        blackDuckProjectBlank = false;
                    } else if (StringUtils.isNotBlank(mapping.getHubProject().getProjectName())) {
                        blackDuckProjectBlank = false;
                    }
                }
                if (jiraProjectBlank || blackDuckProjectBlank) {
                    hasEmptyMapping = true;
                }
            }
            if (hasEmptyMapping) {
                config.setHubProjectMappingError(concatErrorMessage(config.getHubProjectMappingError(), JiraConfigErrorStrings.MAPPING_HAS_EMPTY_ERROR));
            }
        }
    }

    // This must be "package protected" to avoid synthetic access
    Object getValue(final PluginSettings settings, final String key) {
        return settings.get(key);
    }

    // This must be "package protected" to avoid synthetic access
    String getStringValue(final PluginSettings settings, final String key) {
        return (String) getValue(settings, key);
    }

    // This must be "package protected" to avoid synthetic access
    void setValue(final PluginSettings settings, final String key, final Object value) {
        if (value == null) {
            settings.remove(key);
        } else {
            settings.put(key, value);
        }
    }

    private int stringToInteger(final String integer) throws IllegalArgumentException {
        try {
            return Integer.valueOf(integer);
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("The String : " + integer + " , is not an Integer.", e);
        }
    }

    // This must be "package protected" to avoid synthetic access
    List<JiraProject> getJiraProjects(final List<Project> jiraProjects) {
        final List<JiraProject> newJiraProjects = new ArrayList<>();
        if (jiraProjects != null && !jiraProjects.isEmpty()) {
            for (final Project oldProject : jiraProjects) {
                final JiraProject newProject = new JiraProject();
                newProject.setProjectName(oldProject.getName());
                newProject.setProjectId(oldProject.getId());

                newJiraProjects.add(newProject);
            }
        }
        return newJiraProjects;
    }

    // This must be "package protected" to avoid synthetic access
    HubServicesFactory createBlackDuckServicesFactory(final PluginSettings settings, final BlackDuckJiraConfigSerializable config) {
        final BlackduckRestConnection restConnection = createRestConnection(settings, config);
        if (config.hasErrors()) {
            return null;
        }
        final HubServicesFactory blackDuckServicesFactory = new HubServicesFactory(HubServicesFactory.createDefaultGson(), HubServicesFactory.createDefaultJsonParser(), restConnection, logger);
        return blackDuckServicesFactory;
    }

    void closeRestConnection(final RestConnection restConnection) {
        try {
            restConnection.close();
        } catch (final IOException e) {
            logger.error("There was a problem trying to close the connection to the Black Duck server.", e);
        }
    }

    private BlackduckRestConnection createRestConnection(final PluginSettings settings, final BlackDuckJiraConfigSerializable config) {
        final String blackDuckUrl = getStringValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_URL);
        final String blackDuckApiToken = getStringValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN);
        final String blackDuckTimeout = getStringValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_TIMEOUT);
        final String blackDuckTrustCert = getStringValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_TRUST_CERT);

        String blackDuckUser = null;
        String encBlackDuckPassword = null;
        String encBlackDuckPasswordLength = null;

        if (blackDuckApiToken == null) {
            blackDuckUser = getStringValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_USER);
            logger.debug(String.format("Establishing connection to Black Duck server: %s...", blackDuckUrl));
            encBlackDuckPassword = getStringValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_PASS);
            encBlackDuckPasswordLength = getStringValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_PASS_LENGTH);

            if (StringUtils.isBlank(blackDuckUrl) && StringUtils.isBlank(blackDuckUser) && StringUtils.isBlank(encBlackDuckPassword) && StringUtils.isBlank(blackDuckTimeout)) {
                config.setErrorMessage(JiraConfigErrorStrings.BLACKDUCK_CONFIG_PLUGIN_MISSING);
                return null;
            } else if (StringUtils.isBlank(blackDuckUrl) || StringUtils.isBlank(blackDuckUser) || StringUtils.isBlank(encBlackDuckPassword) || StringUtils.isBlank(blackDuckTimeout)) {
                config.setErrorMessage(JiraConfigErrorStrings.BLACKDUCK_SERVER_MISCONFIGURATION + JiraConfigErrorStrings.CHECK_BLACKDUCK_SERVER_CONFIGURATION);
                return null;
            }
        } else if (StringUtils.isBlank(blackDuckUrl) || StringUtils.isBlank(blackDuckApiToken) || StringUtils.isBlank(blackDuckTimeout)) {
            config.setErrorMessage(JiraConfigErrorStrings.BLACKDUCK_SERVER_MISCONFIGURATION + " " + JiraConfigErrorStrings.CHECK_BLACKDUCK_SERVER_CONFIGURATION);
            return null;
        }

        final String blackDuckProxyHost = getStringValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_HOST);
        final String blackDuckProxyPort = getStringValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PORT);
        final String blackDuckNoProxyHost = getStringValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_NO_HOST);
        final String blackDuckProxyUser = getStringValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_USER);
        final String encBlackDuckProxyPassword = getStringValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS);
        final String blackDuckProxyPasswordLength = getStringValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH);

        BlackduckRestConnection restConnection = null;
        try {
            final HubServerConfigBuilder configBuilder = new HubServerConfigBuilder();
            configBuilder.setUrl(blackDuckUrl);
            configBuilder.setApiToken(blackDuckApiToken);
            configBuilder.setUsername(blackDuckUser);
            configBuilder.setPassword(encBlackDuckPassword);
            configBuilder.setPasswordLength(NumberUtils.toInt(encBlackDuckPasswordLength));
            configBuilder.setTimeout(blackDuckTimeout);
            configBuilder.setTrustCert(blackDuckTrustCert);
            configBuilder.setProxyHost(blackDuckProxyHost);
            configBuilder.setProxyPort(blackDuckProxyPort);
            configBuilder.setProxyIgnoredHosts(blackDuckNoProxyHost);
            configBuilder.setProxyUsername(blackDuckProxyUser);
            configBuilder.setProxyPassword(encBlackDuckProxyPassword);
            configBuilder.setProxyPasswordLength(NumberUtils.toInt(blackDuckProxyPasswordLength));

            final HubServerConfig serverConfig;
            try {
                serverConfig = configBuilder.build();
            } catch (final IllegalStateException e) {
                logger.error("Error in Black Duck server configuration: " + e.getMessage());
                config.setErrorMessage(JiraConfigErrorStrings.CHECK_BLACKDUCK_SERVER_CONFIGURATION);
                return null;
            }

            restConnection = serverConfig.createRestConnection(logger);
            restConnection.connect();
        } catch (final IllegalArgumentException | IntegrationException e) {
            config.setErrorMessage(JiraConfigErrorStrings.CHECK_BLACKDUCK_SERVER_CONFIGURATION + " :: " + e.getMessage());
            return null;
        }
        return restConnection;
    }

    // This must be "package protected" to avoid synthetic access
    List<BlackDuckProject> getBlackDuckProjects(final HubServicesFactory blackDuckServicesFactory, final ErrorTracking config) {
        final List<BlackDuckProject> blackDuckProjects = new ArrayList<>();
        final ProjectService projectRequestService = blackDuckServicesFactory.createProjectService();
        List<ProjectView> blackDuckProjectItems = null;
        try {
            blackDuckProjectItems = projectRequestService.getAllProjectMatches(null);
        } catch (final IntegrationException e) {
            config.setErrorMessage(concatErrorMessage(config.getErrorMessage(), e.getMessage()));
            return blackDuckProjects;
        }

        final HubViewFilter<ProjectView> filter = new HubViewFilter<>();
        final MetaHandler metaHandler = new MetaHandler(logger);
        try {
            blackDuckProjectItems = filter.getAccessibleItems(metaHandler, blackDuckProjectItems);
        } catch (final HubIntegrationException e1) {
            config.setErrorMessage(concatErrorMessage(config.getErrorMessage(), e1.getMessage()));
            return blackDuckProjects;
        }

        if (blackDuckProjectItems != null && !blackDuckProjectItems.isEmpty()) {
            for (final ProjectView project : blackDuckProjectItems) {
                final BlackDuckProject newBlackDuckProject = new BlackDuckProject();
                newBlackDuckProject.setProjectName(project.name);
                try {
                    newBlackDuckProject.setProjectUrl(metaHandler.getHref(project));
                } catch (final HubIntegrationException e) {
                    config.setErrorMessage(concatErrorMessage(config.getErrorMessage(), e.getMessage()));
                    continue;
                }
                blackDuckProjects.add(newBlackDuckProject);
            }
        }
        return blackDuckProjects;
    }

    // This must be "package protected" to avoid synthetic access
    void setBlackDuckPolicyRules(final HubServicesFactory blackDuckServicesFactory, final BlackDuckJiraConfigSerializable config) {
        final List<PolicyRuleSerializable> newPolicyRules = new ArrayList<>();
        if (blackDuckServicesFactory != null) {
            final HubService blackDuckService = blackDuckServicesFactory.createHubService();
            try {
                List<PolicyRuleViewV2> policyRules = null;
                try {
                    policyRules = blackDuckService.getAllResponses(ApiDiscovery.POLICY_RULES_LINK_RESPONSE);
                } catch (final HubIntegrationException e) {
                    config.setPolicyRulesError(e.getMessage());
                } catch (final IntegrationRestException ire) {
                    if (ire.getHttpStatusCode() == 402) {
                        config.setPolicyRulesError(JiraConfigErrorStrings.NO_POLICY_LICENSE_FOUND);
                    } else {
                        config.setPolicyRulesError(ire.getMessage());
                    }
                }

                if (policyRules != null && !policyRules.isEmpty()) {
                    for (final PolicyRuleViewV2 rule : policyRules) {
                        final PolicyRuleSerializable newRule = new PolicyRuleSerializable();
                        String description = rule.description;
                        if (description == null) {
                            description = "";
                        }
                        newRule.setDescription(cleanDescription(description));
                        newRule.setName(rule.name.trim());

                        final MetaHandler metaHandler = new MetaHandler(logger);
                        try {
                            newRule.setPolicyUrl(metaHandler.getHref(rule));
                        } catch (final HubIntegrationException e) {
                            logger.error("Error getting URL for policy rule " + rule.name + ": " + e.getMessage());
                            config.setPolicyRulesError(JiraConfigErrorStrings.POLICY_RULE_URL_ERROR);
                            continue;
                        }
                        newRule.setEnabled(rule.enabled);
                        newPolicyRules.add(newRule);
                    }
                }
                if (config.getPolicyRules() != null) {
                    for (final PolicyRuleSerializable oldRule : config.getPolicyRules()) {
                        for (final PolicyRuleSerializable newRule : newPolicyRules) {
                            if (oldRule.getPolicyUrl().equals(newRule.getPolicyUrl())) {
                                newRule.setChecked(oldRule.getChecked());
                                break;
                            }
                        }
                    }
                }
            } catch (final Exception e) {
                config.setPolicyRulesError(e.getMessage());
            }
        }
        config.setPolicyRules(newPolicyRules);
        if (config.getPolicyRules().isEmpty()) {
            config.setPolicyRulesError(concatErrorMessage(config.getPolicyRulesError(), JiraConfigErrorStrings.NO_POLICY_RULES_FOUND_ERROR));
        }
    }

    private String cleanDescription(final String origString) {
        return removeCharsFromString(origString.trim(), "\n\r\t");
    }

    private String removeCharsFromString(final String origString, final String charsToRemoveString) {
        String cleanerString = origString;
        final char[] charsToRemove = charsToRemoveString.toCharArray();
        for (final char c : charsToRemove) {
            cleanerString = cleanerString.replace(c, ' ');
        }
        return cleanerString;
    }

    private String concatErrorMessage(final String originalMessage, final String newMessage) {
        String errorMsg = "";
        if (StringUtils.isNotBlank(originalMessage)) {
            errorMsg = originalMessage;
            errorMsg += " : ";
        }
        errorMsg += newMessage;
        return errorMsg;
    }
}
