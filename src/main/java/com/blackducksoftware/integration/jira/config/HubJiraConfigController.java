/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.jira.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
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
import com.blackducksoftware.integration.atlassian.utils.HubConfigKeys;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.item.HubViewFilter;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRequestService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.capability.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.view.PolicyRuleView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.jira.common.HubJiraConfigKeys;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProject;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.PluginField;
import com.blackducksoftware.integration.jira.common.PluginVersion;
import com.blackducksoftware.integration.jira.common.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.common.exception.JiraException;
import com.blackducksoftware.integration.jira.task.HubMonitor;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.issue.JiraFieldUtils;

@Path("/")
public class HubJiraConfigController {

    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final UserManager userManager;

    private final PluginSettingsFactory pluginSettingsFactory;

    private final TransactionTemplate transactionTemplate;

    private final ProjectManager projectManager;

    private final HubMonitor hubMonitor;

    private final GroupPickerSearchService groupPickerSearchService;

    private final FieldManager fieldManager;

    private final Properties i18nProperties;

    public HubJiraConfigController(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory,
            final TransactionTemplate transactionTemplate, final ProjectManager projectManager,
            final HubMonitor hubMonitor,
            final GroupPickerSearchService groupPickerSearchService,
            final FieldManager fieldManager) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
        this.projectManager = projectManager;
        this.hubMonitor = hubMonitor;
        this.groupPickerSearchService = groupPickerSearchService;
        this.fieldManager = fieldManager;

        i18nProperties = new Properties();
        populateI18nProperties();
    }

    private void populateI18nProperties() {
        try (final InputStream stream = ClassLoaderUtils.getResourceAsStream(HubJiraConstants.PROPERTY_FILENAME, this.getClass())) {
            if (stream != null) {
                i18nProperties.load(stream);
            } else {
                logger.warn("Error opening property file: " + HubJiraConstants.PROPERTY_FILENAME);
            }
        } catch (final IOException e) {
            logger.warn("Error reading property file: " + HubJiraConstants.PROPERTY_FILENAME);
        }
        logger.debug("i18nProperties: " + i18nProperties);
    }

    private String getI18nProperty(final String key) {
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
        final String oldHubJiraGroupsString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_GROUPS);
        final String hubJiraGroupsString;
        if (StringUtils.isNotBlank(oldHubJiraGroupsString)) {
            hubJiraGroupsString = oldHubJiraGroupsString;
        } else {
            hubJiraGroupsString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_GROUPS);
        }
        if (StringUtils.isNotBlank(hubJiraGroupsString)) {
            final String[] hubJiraGroups = hubJiraGroupsString.split(",");
            boolean userIsInGroups = false;
            for (final String hubJiraGroup : hubJiraGroups) {
                if (userManager.isUserInGroup(username, hubJiraGroup.trim())) {
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
    public Response getHubJiraAdminConfiguration(@Context final HttpServletRequest request) {
        final Object adminConfig;
        try {
            final String username = userManager.getRemoteUsername(request);
            if (username == null) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            final boolean userIsSysAdmin = userManager.isSystemAdmin(username);
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final String oldHubJiraGroupsString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_GROUPS);
            final String hubJiraGroupsString;
            if (StringUtils.isNotBlank(oldHubJiraGroupsString)) {
                hubJiraGroupsString = oldHubJiraGroupsString;
                setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_GROUPS, null);
                setValue(settings, HubJiraConfigKeys.HUB_CONFIG_GROUPS, hubJiraGroupsString);
            } else {
                hubJiraGroupsString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_GROUPS);
            }
            if (!userIsSysAdmin) {
                if (StringUtils.isBlank(hubJiraGroupsString)) {
                    return Response.status(Status.UNAUTHORIZED).build();
                } else {
                    final String[] hubJiraGroups = hubJiraGroupsString.split(",");
                    boolean userIsInGroups = false;
                    for (final String hubJiraGroup : hubJiraGroups) {
                        if (userManager.isUserInGroup(username, hubJiraGroup.trim())) {
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
                    final HubAdminConfigSerializable txAdminConfig = new HubAdminConfigSerializable();
                    txAdminConfig.setHubJiraGroups(hubJiraGroupsString);
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
            final HubAdminConfigSerializable errorAdminConfig = new HubAdminConfigSerializable();
            final String msg = "Error getting admin config: " + e.getMessage();
            logger.error(msg, e);
            errorAdminConfig.setHubJiraGroupsError(msg);
            return Response.ok(errorAdminConfig).build();
        }
        return Response.ok(adminConfig).build();
    }

    @Path("/hubJiraTicketErrors")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHubJiraTicketErrors(@Context final HttpServletRequest request) {
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
                    final HubJiraConfigSerializable txConfig = new HubJiraConfigSerializable();

                    final String intervalBetweenChecks = getStringValue(settings,
                            HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);

                    txConfig.setIntervalBetweenChecks(intervalBetweenChecks);

                    validateInterval(txConfig);
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final HubJiraConfigSerializable errorConfig = new HubJiraConfigSerializable();
            final String msg = "Error getting interval config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setIntervalBetweenChecksError(msg);
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

                    final HubJiraConfigSerializable txProjectsConfig = new HubJiraConfigSerializable();
                    txProjectsConfig.setJiraProjects(jiraProjects);

                    if (jiraProjects.size() == 0) {
                        txProjectsConfig.setJiraProjectsError(JiraConfigErrors.NO_JIRA_PROJECTS_FOUND);
                    }
                    return txProjectsConfig;
                }
            });
        } catch (final Exception e) {
            final HubJiraConfigSerializable errorConfig = new HubJiraConfigSerializable();
            final String msg = "Error getting JIRA projects config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setJiraProjectsError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(projectsConfig).build();
    }

    @Path("/hubProjects")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHubProjects(@Context final HttpServletRequest request) {
        logger.debug("getHubProjects()");
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
                    final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
                    config.setHubProjects(new ArrayList<>(0));

                    final HubServicesFactory hubServicesFactory = createHubServicesFactory(settings, config);
                    if (hubServicesFactory == null) {
                        return config;
                    }

                    final List<HubProject> hubProjects = getHubProjects(hubServicesFactory, config);
                    config.setHubProjects(hubProjects);

                    if (hubProjects.size() == 0) {
                        config.setHubProjectsError(JiraConfigErrors.NO_HUB_PROJECTS_FOUND);
                    }
                    return config;
                }
            });
        } catch (final Exception e) {
            final HubJiraConfigSerializable errorConfig = new HubJiraConfigSerializable();
            final String msg = "Error getting Hub projects config: " + e.getMessage();
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
                    final String pluginVersion = PluginVersion.getVersion();
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
                    txSourceFields.add(new IdToNameMapping(PluginField.HUB_CUSTOM_FIELD_PROJECT.getId(),
                            getI18nProperty(PluginField.HUB_CUSTOM_FIELD_PROJECT.getLongNameProperty())));
                    txSourceFields
                            .add(new IdToNameMapping(PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION.getId(),
                                    getI18nProperty(PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.HUB_CUSTOM_FIELD_COMPONENT.getId(),
                            getI18nProperty(PluginField.HUB_CUSTOM_FIELD_COMPONENT.getLongNameProperty())));
                    txSourceFields.add(
                            new IdToNameMapping(PluginField.HUB_CUSTOM_FIELD_COMPONENT_VERSION.getId(),
                                    getI18nProperty(PluginField.HUB_CUSTOM_FIELD_COMPONENT_VERSION.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.HUB_CUSTOM_FIELD_POLICY_RULE.getId(),
                            getI18nProperty(PluginField.HUB_CUSTOM_FIELD_POLICY_RULE.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.HUB_CUSTOM_FIELD_LICENSE_NAMES.getId(),
                            getI18nProperty(PluginField.HUB_CUSTOM_FIELD_LICENSE_NAMES.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.HUB_CUSTOM_FIELD_COMPONENT_USAGE.getId(),
                            getI18nProperty(PluginField.HUB_CUSTOM_FIELD_COMPONENT_USAGE.getLongNameProperty())));
                    // TODO: Uncomment these when Hub starts providing Origin info
                    // txSourceFields.add(new IdToNameMapping(PluginField.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN.getId(),
                    // getI18nProperty(PluginField.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN.getLongNameProperty())));
                    // txSourceFields.add(new IdToNameMapping(PluginField.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN_ID.getId(),
                    // getI18nProperty(PluginField.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN_ID.getLongNameProperty())));
                    txSourceFields.add(new IdToNameMapping(PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME.getId(),
                            getI18nProperty(PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME.getLongNameProperty())));

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

    @Path("/hubPolicies")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHubPolicies(@Context final HttpServletRequest request) {
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

                    final String policyRulesJson = getStringValue(settings,
                            HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON);

                    final HubJiraConfigSerializable txConfig = new HubJiraConfigSerializable();

                    if (StringUtils.isNotBlank(policyRulesJson)) {
                        txConfig.setPolicyRulesJson(policyRulesJson);
                    } else {
                        txConfig.setPolicyRules(new ArrayList<>(0));
                    }

                    final HubServicesFactory hubServicesFactory = createHubServicesFactory(settings, txConfig);
                    if (hubServicesFactory == null) {
                        return txConfig;
                    }
                    setHubPolicyRules(hubServicesFactory, txConfig);
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final HubJiraConfigSerializable errorConfig = new HubJiraConfigSerializable();
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
                    final HubJiraConfigSerializable txConfig = new HubJiraConfigSerializable();
                    final String createVulnIssuesChoiceString = getStringValue(settings,
                            HubJiraConfigKeys.HUB_CONFIG_CREATE_VULN_ISSUES_CHOICE);
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
            final HubJiraConfigSerializable errorConfig = new HubJiraConfigSerializable();
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

                    final HubJiraConfigSerializable txConfig = new HubJiraConfigSerializable();

                    final String hubProjectMappingsJson = getStringValue(settings,
                            HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);

                    txConfig.setHubProjectMappingsJson(hubProjectMappingsJson);

                    validateMapping(txConfig);
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final HubJiraConfigSerializable errorConfig = new HubJiraConfigSerializable();
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

                    final HubJiraFieldCopyConfigSerializable txConfig = new HubJiraFieldCopyConfigSerializable();

                    final String hubFieldCopyMappingsJson = getStringValue(settings,
                            HubJiraConfigKeys.HUB_CONFIG_FIELD_COPY_MAPPINGS_JSON);

                    logger.debug("Get /fieldCopyMappings returning JSON: " + hubFieldCopyMappingsJson);
                    txConfig.setJson(hubFieldCopyMappingsJson);
                    logger.debug("HubJiraFieldCopyConfigSerializable.getJson(): " + txConfig.getJson());
                    return txConfig;
                }
            });
        } catch (final Exception e) {
            final HubJiraConfigSerializable errorConfig = new HubJiraConfigSerializable();
            final String msg = "Error getting field mappings: " + e.getMessage();
            logger.error(msg, e);
            return Response.ok(errorConfig).build();
        }

        final HubJiraFieldCopyConfigSerializable returnValue = (HubJiraFieldCopyConfigSerializable) config;
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
            final String currentJiraUsername = userManager.getRemoteUsername(request);
            projectsConfig = transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
                    config.setCreatorCandidates(new TreeSet<String>());

                    final SortedSet<String> creatorCandidates = getIssueCreatorCandidates(settings, currentJiraUsername);
                    config.setCreatorCandidates(creatorCandidates);

                    if (creatorCandidates.size() == 0) {
                        config.setCreatorCandidatesError(JiraConfigErrors.NO_CREATOR_CANDIDATES_FOUND);
                    }
                    return config;
                }
            });
        } catch (final Exception e) {
            final HubJiraConfigSerializable errorConfig = new HubJiraConfigSerializable();
            final String msg = "Error getting issue creator candidates config: " + e.getMessage();
            logger.error(msg, e);
            errorConfig.setCreatorCandidatesError(msg);
            return Response.ok(errorConfig).build();
        }
        return Response.ok(projectsConfig).build();
    }

    private SortedSet<String> getIssueCreatorCandidates(final PluginSettings settings, final String currentJiraUsername) {
        final SortedSet<String> jiraUsernames = new TreeSet<String>();
        jiraUsernames.add(currentJiraUsername);
        final String groupList = getStringValue(settings,
                HubJiraConfigKeys.HUB_CONFIG_GROUPS);
        if (!StringUtils.isBlank(groupList)) {
            final String[] groupNames = groupList.split(",");
            for (final String groupName : groupNames) {
                // TODO add group manager to JiraServices, and get it from there
                jiraUsernames.addAll(
                        com.atlassian.jira.component.ComponentAccessor.getGroupManager().getUserNamesInGroup(groupName));
            }
        }
        logger.debug("getJiraUsernames(): returning: " + jiraUsernames);
        return jiraUsernames;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final HubJiraConfigSerializable config, @Context final HttpServletRequest request) {
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
                    final HubServicesFactory hubServicesFactory = createHubServicesFactory(settings, config);
                    if (hubServicesFactory == null) {
                        return config;
                    }
                    final List<HubProject> hubProjects = getHubProjects(hubServicesFactory, config);
                    config.setHubProjects(hubProjects);
                    config.setJiraProjects(jiraProjects);
                    validateInterval(config);
                    validateCreator(config, settings);
                    validateMapping(config);
                    if (getValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME) == null) {
                        final SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
                        dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
                        setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_FIRST_SAVE_TIME,
                                dateFormatter.format(new Date()));
                    }
                    final String previousInterval = getStringValue(settings,
                            HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);
                    setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS,
                            config.getIntervalBetweenChecks());
                    final String issueCreatorJiraUser = config.getCreator();
                    logger.debug("Setting issue creator jira user to: " + issueCreatorJiraUser);
                    setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_ISSUE_CREATOR_USER,
                            issueCreatorJiraUser);
                    setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON, config.getPolicyRulesJson());
                    setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON,
                            config.getHubProjectMappingsJson());
                    setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_ADMIN_USER, username);
                    updateHubTaskInterval(previousInterval, config.getIntervalBetweenChecks());
                    logger.debug("User input: createVulnerabilityIssues: " + config.isCreateVulnerabilityIssues());
                    final Boolean createVulnerabilityIssuesChoice = config.isCreateVulnerabilityIssues();
                    logger.debug("Setting createVulnerabilityIssuesChoice to " + createVulnerabilityIssuesChoice.toString());
                    setValue(settings, HubJiraConfigKeys.HUB_CONFIG_CREATE_VULN_ISSUES_CHOICE, createVulnerabilityIssuesChoice.toString());
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
    public Response removeErrors(final TicketCreationErrorSerializable errorsToDelete,
            @Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }
        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {

                final Object errorObject = getValue(settings, HubJiraConstants.HUB_JIRA_ERROR);

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

                if (errorsToDelete.getHubJiraTicketErrors() != null
                        && !errorsToDelete.getHubJiraTicketErrors().isEmpty()) {
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
                setValue(settings, HubJiraConstants.HUB_JIRA_ERROR, TicketCreationError.toJson(ticketErrors));
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
    public Response updateHubAdminConfiguration(final HubAdminConfigSerializable adminConfig,
            @Context final HttpServletRequest request) {
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

                    final HubAdminConfigSerializable txResponseObject = new HubAdminConfigSerializable();

                    if (!userIsSysAdmin) {
                        txResponseObject.setHubJiraGroupsError(JiraConfigErrors.NON_SYSTEM_ADMINS_CANT_CHANGE_GROUPS);
                        return txResponseObject;
                    } else {
                        setValue(settings, HubJiraConfigKeys.HUB_CONFIG_GROUPS, adminConfig.getHubJiraGroups());
                    }
                    return null;
                }
            });
        } catch (final Exception e) {
            final String msg = "Exception during admin save: " + e.getMessage();
            logger.error(msg, e);
            final HubAdminConfigSerializable errorResponseObject = new HubAdminConfigSerializable();
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
    public Response updateFieldCopyConfiguration(final HubJiraFieldCopyConfigSerializable fieldCopyConfig,
            @Context final HttpServletRequest request) {
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

                    setValue(settings, HubJiraConfigKeys.HUB_CONFIG_FIELD_COPY_MAPPINGS_JSON,
                            fieldCopyConfig.getJson());
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

    private boolean isValid(final HubJiraFieldCopyConfigSerializable fieldCopyConfig) {
        if (fieldCopyConfig.getProjectFieldCopyMappings().size() == 0) {
            fieldCopyConfig.setErrorMessage(JiraConfigErrors.NO_VALID_FIELD_CONFIGURATIONS);
            return false;
        }

        for (final ProjectFieldCopyMapping projectFieldCopyMapping : fieldCopyConfig.getProjectFieldCopyMappings()) {
            if (StringUtils.isBlank(projectFieldCopyMapping.getSourceFieldId())) {
                fieldCopyConfig.setErrorMessage(JiraConfigErrors.FIELD_CONFIGURATION_INVALID_SOURCE_FIELD);
                return false;
            }
            if (StringUtils.isBlank(projectFieldCopyMapping.getTargetFieldId())) {
                fieldCopyConfig.setErrorMessage(JiraConfigErrors.FIELD_CONFIGURATION_INVALID_TARGET_FIELD);
                return false;
            }
            if (StringUtils.isBlank(projectFieldCopyMapping.getSourceFieldName())) {
                fieldCopyConfig.setErrorMessage(JiraConfigErrors.FIELD_CONFIGURATION_INVALID_SOURCE_FIELD);
                return false;
            }
            if (StringUtils.isBlank(projectFieldCopyMapping.getTargetFieldName())) {
                fieldCopyConfig.setErrorMessage(JiraConfigErrors.FIELD_CONFIGURATION_INVALID_TARGET_FIELD);
                return false;
            }
        }

        return true;
    }

    @Path("/reset")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resetHubJiraKeys(final Object object, @Context final HttpServletRequest request) {
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

                        final SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
                        dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));
                        final String oldLastRunDateString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE);
                        final String newLastRunDateString = dateFormatter.format(now);
                        logger.warn("Resetting last run date from " + oldLastRunDateString + " to " + newLastRunDateString
                                + "; this will skip over any notifications generated between those times");
                        setValue(settings, HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE, newLastRunDateString);
                        setValue(settings, HubJiraConstants.HUB_JIRA_ERROR, null);
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

    private void updateHubTaskInterval(final String previousIntervalString, final String newIntervalString) {
        final int previousInterval = NumberUtils.toInt(previousIntervalString);
        int newInterval;
        try {
            newInterval = stringToInteger(newIntervalString);
            if (newInterval > 0 && newInterval != previousInterval) {
                hubMonitor.changeInterval();
            }
        } catch (final IllegalArgumentException e) {
            logger.error("The specified interval is not an integer.");
        }
    }

    private void validateInterval(final HubJiraConfigSerializable config) {
        if (StringUtils.isBlank(config.getIntervalBetweenChecks())) {
            config.setIntervalBetweenChecksError(JiraConfigErrors.NO_INTERVAL_FOUND_ERROR);
        } else {
            try {
                final int interval = stringToInteger(config.getIntervalBetweenChecks());
                if (interval <= 0) {
                    config.setIntervalBetweenChecksError(JiraConfigErrors.INVALID_INTERVAL_FOUND_ERROR);
                }
            } catch (final IllegalArgumentException e) {
                config.setIntervalBetweenChecksError(e.getMessage());
            }
        }
    }

    private void validateCreator(final HubJiraConfigSerializable config, final PluginSettings settings) {
        if (StringUtils.isBlank(config.getCreator())) {
            config.setIntervalBetweenChecksError(JiraConfigErrors.NO_CREATOR_SPECIFIED_ERROR);
        }
        if (isUserAuthorizedForPlugin(settings, config.getCreator())) {
            return;
        } else {
            config.setIntervalBetweenChecksError(JiraConfigErrors.UNAUTHORIZED_CREATOR_ERROR);
        }
    }

    private void validateMapping(final HubJiraConfigSerializable config) {
        if (config.getHubProjectMappings() != null && !config.getHubProjectMappings().isEmpty()) {
            boolean hasEmptyMapping = false;
            for (final HubProjectMapping mapping : config.getHubProjectMappings()) {
                boolean jiraProjectBlank = true;
                boolean hubProjectBlank = true;
                if (mapping.getJiraProject() != null) {
                    if (mapping.getJiraProject().getProjectId() != null) {
                        jiraProjectBlank = false;
                    }
                }
                if (mapping.getHubProject() != null) {
                    if (StringUtils.isNotBlank(mapping.getHubProject().getProjectUrl())) {
                        hubProjectBlank = false;
                    } else if (StringUtils.isNotBlank(mapping.getHubProject().getProjectName())) {
                        hubProjectBlank = false;
                    }
                }
                if (jiraProjectBlank || hubProjectBlank) {
                    hasEmptyMapping = true;
                }
            }
            if (hasEmptyMapping) {
                config.setHubProjectMappingError(concatErrorMessage(config.getHubProjectMappingError(),
                        JiraConfigErrors.MAPPING_HAS_EMPTY_ERROR));
            }
        }
    }

    private Object getValue(final PluginSettings settings, final String key) {
        return settings.get(key);
    }

    private String getStringValue(final PluginSettings settings, final String key) {
        return (String) getValue(settings, key);
    }

    private void setValue(final PluginSettings settings, final String key, final Object value) {
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

    private List<JiraProject> getJiraProjects(final List<Project> jiraProjects) {
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

    private HubServicesFactory createHubServicesFactory(final PluginSettings settings, final HubJiraConfigSerializable config) {
        final RestConnection restConnection = createRestConnection(settings, config);
        if (config.hasErrors()) {
            return null;
        }
        final HubServicesFactory hubServicesFactory = new HubServicesFactory(restConnection);
        return hubServicesFactory;
    }

    private RestConnection createRestConnection(final PluginSettings settings, final HubJiraConfigSerializable config) {
        final String hubUrl = getStringValue(settings, HubConfigKeys.CONFIG_HUB_URL);
        final String hubUser = getStringValue(settings, HubConfigKeys.CONFIG_HUB_USER);
        logger.debug(String.format("Establishing connection to hub server: %s as %s", hubUrl, hubUser));
        final String encHubPassword = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS);
        final String encHubPasswordLength = getStringValue(settings, HubConfigKeys.CONFIG_HUB_PASS_LENGTH);
        final String hubTimeout = getStringValue(settings, HubConfigKeys.CONFIG_HUB_TIMEOUT);

        if (StringUtils.isBlank(hubUrl) && StringUtils.isBlank(hubUser) && StringUtils.isBlank(encHubPassword)
                && StringUtils.isBlank(hubTimeout)) {
            config.setErrorMessage(JiraConfigErrors.HUB_CONFIG_PLUGIN_MISSING);
            return null;
        } else if (StringUtils.isBlank(hubUrl) || StringUtils.isBlank(hubUser) || StringUtils.isBlank(encHubPassword)
                || StringUtils.isBlank(hubTimeout)) {
            config.setErrorMessage(
                    JiraConfigErrors.HUB_SERVER_MISCONFIGURATION + JiraConfigErrors.CHECK_HUB_SERVER_CONFIGURATION);
            return null;
        }

        final String hubProxyHost = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_HOST);
        final String hubProxyPort = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PORT);
        final String hubNoProxyHost = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_NO_HOST);
        final String hubProxyUser = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_USER);
        final String encHubProxyPassword = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PASS);
        final String hubProxyPasswordLength = getStringValue(settings, HubConfigKeys.CONFIG_PROXY_PASS_LENGTH);

        CredentialsRestConnection restConnection = null;
        try {
            final HubServerConfigBuilder configBuilder = new HubServerConfigBuilder();
            configBuilder.setHubUrl(hubUrl);
            configBuilder.setUsername(hubUser);
            configBuilder.setPassword(encHubPassword);
            configBuilder.setPasswordLength(NumberUtils.toInt(encHubPasswordLength));
            configBuilder.setTimeout(hubTimeout);
            configBuilder.setProxyHost(hubProxyHost);
            configBuilder.setProxyPort(hubProxyPort);
            configBuilder.setIgnoredProxyHosts(hubNoProxyHost);
            configBuilder.setProxyUsername(hubProxyUser);
            configBuilder.setProxyPassword(encHubProxyPassword);
            configBuilder.setProxyPasswordLength(NumberUtils.toInt(hubProxyPasswordLength));

            final HubServerConfig serverConfig;
            try {
                serverConfig = configBuilder.build();
            } catch (final IllegalStateException e) {
                logger.error("Error in Hub server configuration: " + e.getMessage());
                config.setErrorMessage(JiraConfigErrors.CHECK_HUB_SERVER_CONFIGURATION);
                return null;
            }

            restConnection = new CredentialsRestConnection(logger, serverConfig.getHubUrl(),
                    serverConfig.getGlobalCredentials().getUsername(),
                    serverConfig.getGlobalCredentials().getDecryptedPassword(),
                    serverConfig.getTimeout());
            restConnection.connect();

        } catch (IllegalArgumentException | IntegrationException e) {
            config.setErrorMessage(JiraConfigErrors.CHECK_HUB_SERVER_CONFIGURATION + " :: " + e.getMessage());
            return null;
        }
        return restConnection;
    }

    private List<HubProject> getHubProjects(final HubServicesFactory hubServicesFactory,
            final ErrorTracking config) {
        final List<HubProject> hubProjects = new ArrayList<>();
        final ProjectRequestService projectRequestService = hubServicesFactory.createProjectRequestService();
        List<ProjectView> hubProjectItems = null;
        try {
            hubProjectItems = projectRequestService.getAllProjects();
        } catch (final IntegrationException e) {
            config.setErrorMessage(concatErrorMessage(config.getErrorMessage(), e.getMessage()));
            return hubProjects;
        }

        final HubViewFilter<ProjectView> filter = new HubViewFilter<>();
        final MetaService metaService = hubServicesFactory.createMetaService(logger);
        try {
            hubProjectItems = filter.getAccessibleItems(metaService, hubProjectItems);
        } catch (final HubIntegrationException e1) {
            config.setErrorMessage(concatErrorMessage(config.getErrorMessage(), e1.getMessage()));
            return hubProjects;
        }

        if (hubProjectItems != null && !hubProjectItems.isEmpty()) {
            for (final ProjectView project : hubProjectItems) {
                final HubProject newHubProject = new HubProject();
                newHubProject.setProjectName(project.getName());
                try {
                    newHubProject.setProjectUrl(metaService.getHref(project));
                } catch (final HubIntegrationException e) {
                    config.setErrorMessage(concatErrorMessage(config.getErrorMessage(), e.getMessage()));
                    continue;
                }
                hubProjects.add(newHubProject);
            }
        }
        return hubProjects;
    }

    private void setHubPolicyRules(final HubServicesFactory hubServicesFactory, final HubJiraConfigSerializable config) {

        final List<PolicyRuleSerializable> newPolicyRules = new ArrayList<>();
        if (hubServicesFactory != null) {
            final HubSupportHelper supportHelper = new HubSupportHelper();
            try {
                final HubVersionRequestService hubVersionRequestService = hubServicesFactory.createHubVersionRequestService();
                supportHelper.checkHubSupport(hubVersionRequestService, null);

                if (supportHelper.hasCapability(HubCapabilitiesEnum.POLICY_API)) {

                    final PolicyRequestService policyService = hubServicesFactory.createPolicyRequestService();

                    List<PolicyRuleView> policyRules = null;
                    try {
                        policyRules = policyService.getAllPolicyRules();
                    } catch (final HubIntegrationException e) {
                        config.setPolicyRulesError(e.getMessage());
                    }

                    if (policyRules != null && !policyRules.isEmpty()) {
                        for (final PolicyRuleView rule : policyRules) {
                            final PolicyRuleSerializable newRule = new PolicyRuleSerializable();
                            String description = rule.getDescription();
                            if (description == null) {
                                description = "";
                            }
                            newRule.setDescription(cleanDescription(description));
                            newRule.setName(rule.getName().trim());

                            final MetaService metaService = hubServicesFactory.createMetaService(logger);
                            try {
                                newRule.setPolicyUrl(metaService.getHref(rule));
                            } catch (final HubIntegrationException e) {
                                logger.error("Error getting URL for policy rule " + rule.getName() + ": " + e.getMessage());
                                config.setPolicyRulesError(JiraConfigErrors.POLICY_RULE_URL_ERROR);
                                continue;
                            }
                            newRule.setEnabled(rule.getEnabled());
                            newPolicyRules.add(newRule);
                        }
                    }
                    if (config.getPolicyRules() != null) {
                        for (final PolicyRuleSerializable oldRule : config.getPolicyRules()) {
                            for (final PolicyRuleSerializable newRule : newPolicyRules) {
                                if (oldRule.getName().equals(newRule.getName())) {
                                    newRule.setChecked(oldRule.getChecked());
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    config.setPolicyRulesError(JiraConfigErrors.HUB_SERVER_NO_POLICY_SUPPORT_ERROR);
                }
            } catch (final Exception e) {
                config.setPolicyRulesError(e.getMessage());
            }
        }
        config.setPolicyRules(newPolicyRules);
        if (config.getPolicyRules().isEmpty()) {
            config.setPolicyRulesError(
                    concatErrorMessage(config.getPolicyRulesError(), JiraConfigErrors.NO_POLICY_RULES_FOUND_ERROR));
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
