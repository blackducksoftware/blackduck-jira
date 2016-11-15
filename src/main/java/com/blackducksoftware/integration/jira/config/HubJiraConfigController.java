/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.joda.time.DateTime;
import org.joda.time.Days;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.atlassian.utils.HubConfigKeys;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.api.item.HubItemFilterUtil;
import com.blackducksoftware.integration.hub.api.policy.PolicyRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyRule;
import com.blackducksoftware.integration.hub.api.project.ProjectItem;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.capabilities.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.jira.common.HubJiraConfigKeys;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.common.HubProject;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.PluginField;
import com.blackducksoftware.integration.jira.common.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.task.HubMonitor;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

@Path("/")
public class HubJiraConfigController {
    private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final UserManager userManager;

    private final PluginSettingsFactory pluginSettingsFactory;

    private final TransactionTemplate transactionTemplate;

    private final ProjectManager projectManager;

    private final HubMonitor hubMonitor;

    private final GroupPickerSearchService groupPickerSearchService;

    private final Gson gson = new GsonBuilder().create();

    private final JsonParser jsonParser = new JsonParser();

    public HubJiraConfigController(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory,
            final TransactionTemplate transactionTemplate, final ProjectManager projectManager,
            final HubMonitor hubMonitor,
            final GroupPickerSearchService groupPickerSearchService) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
        this.projectManager = projectManager;
        this.hubMonitor = hubMonitor;
        this.groupPickerSearchService = groupPickerSearchService;
    }

    private Response checkUserPermissions(final HttpServletRequest request, final PluginSettings settings) {
        final String username = userManager.getRemoteUsername(request);
        if (username == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        if (userManager.isSystemAdmin(username)) {
            return null;
        }
        final String hubJiraGroupsString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_GROUPS);
        if (StringUtils.isNotBlank(hubJiraGroupsString)) {
            final String[] hubJiraGroups = hubJiraGroupsString.split(",");
            boolean userIsInGroups = false;
            for (final String hubJiraGroup : hubJiraGroups) {
                if (userManager.isUserInGroup(username, hubJiraGroup)) {
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

    @Path("/admin")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHubJiraAdminConfiguration(@Context final HttpServletRequest request) {
        final String username = userManager.getRemoteUsername(request);
        if (username == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        final boolean userIsSysAdmin = userManager.isSystemAdmin(username);
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final String hubJiraGroupsString = getStringValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_GROUPS);
        if (!userIsSysAdmin) {
            if (StringUtils.isBlank(hubJiraGroupsString)) {
                return Response.status(Status.UNAUTHORIZED).build();
            } else {
                final String[] hubJiraGroups = hubJiraGroupsString.split(",");
                boolean userIsInGroups = false;
                for (final String hubJiraGroup : hubJiraGroups) {
                    if (userManager.isUserInGroup(username, hubJiraGroup)) {
                        userIsInGroups = true;
                        break;
                    }
                }
                if (!userIsInGroups) {
                    return Response.status(Status.UNAUTHORIZED).build();
                }
            }
        }

        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {
                final HubAdminConfigSerializable adminConfig = new HubAdminConfigSerializable();
                adminConfig.setHubJiraGroups(hubJiraGroupsString);
                if (userIsSysAdmin) {
                    final List<String> jiraGroups = new ArrayList<>();

                    final Collection<Group> jiraGroupCollection = groupPickerSearchService.findGroups("");
                    if (jiraGroupCollection != null && !jiraGroupCollection.isEmpty()) {
                        for (final Group group : jiraGroupCollection) {
                            jiraGroups.add(group.getName());
                        }
                    }
                    adminConfig.setJiraGroups(jiraGroups);
                }
                return adminConfig;
            }
        });

        return Response.ok(obj).build();
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

                final Map<String, String> ticketErrors = expireOldErrors(settings);
                if (ticketErrors != null) {
                    final Set<TicketCreationError> displayTicketErrors = new HashSet<>();
                    for (final Entry<String, String> error : ticketErrors.entrySet()) {
                        final String errorKey = error.getKey();
                        final TicketCreationError ticketCreationError = new TicketCreationError();
                        ticketCreationError.setStackTrace(errorKey);
                        ticketCreationError.setTimeStamp(error.getValue());
                        displayTicketErrors.add(ticketCreationError);
                    }
                    creationError.setHubJiraTicketErrors(displayTicketErrors);
                    System.err.println("Errors to UI : " + creationError.getHubJiraTicketErrors().size());
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
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }
        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {
                final HubJiraConfigSerializable config = new HubJiraConfigSerializable();

                final String intervalBetweenChecks = getStringValue(settings,
                        HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS);

                config.setIntervalBetweenChecks(intervalBetweenChecks);

                checkIntervalErrors(config);
                return config;
            }
        });

        return Response.ok(obj).build();
    }

    @Path("/jiraProjects")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJiraProjects(@Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }
        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {
                final List<JiraProject> jiraProjects = getJiraProjects(projectManager.getProjectObjects());

                final HubJiraConfigSerializable config = new HubJiraConfigSerializable();
                config.setJiraProjects(jiraProjects);

                if (jiraProjects.size() == 0) {
                    config.setJiraProjectsError(JiraConfigErrors.NO_JIRA_PROJECTS_FOUND);
                }
                return config;
            }
        });
        return Response.ok(obj).build();
    }

    @Path("/hubProjects")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHubProjects(@Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }
        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {
                final HubJiraConfigSerializable config = new HubJiraConfigSerializable();

                final HubIntRestService restService = getHubRestService(settings, config);

                final List<HubProject> hubProjects = getHubProjects(restService, config);
                config.setHubProjects(hubProjects);

                if (hubProjects.size() == 0) {
                    config.setHubProjectsError(JiraConfigErrors.NO_HUB_PROJECTS_FOUND);
                }
                return config;
            }
        });
        return Response.ok(obj).build();
    }

    @Path("/sourceFields")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSourceFields(@Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }

        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {
                final Fields sourceFields = new Fields();
                logger.debug("Adding source fields");
                sourceFields.add(PluginField.HUB_CUSTOM_FIELD_PROJECT.getId(), PluginField.HUB_CUSTOM_FIELD_PROJECT.getName());
                sourceFields.add(PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION.getId(), PluginField.HUB_CUSTOM_FIELD_PROJECT_VERSION.getName());
                sourceFields.add(PluginField.HUB_CUSTOM_FIELD_COMPONENT.getId(), PluginField.HUB_CUSTOM_FIELD_COMPONENT.getName());
                sourceFields.add(PluginField.HUB_CUSTOM_FIELD_COMPONENT_VERSION.getId(), PluginField.HUB_CUSTOM_FIELD_COMPONENT_VERSION.getName());
                sourceFields.add(PluginField.HUB_CUSTOM_FIELD_POLICY_RULE.getId(), PluginField.HUB_CUSTOM_FIELD_POLICY_RULE.getName());
                return sourceFields;
            }

        });

        return Response.ok(obj).build();
    }

    @Path("/targetFields")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTargetFields(@Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }

        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {
                final Fields targetFields = new Fields();
                targetFields.add("customfield_10001", "Custom Project Version");
                targetFields.add("customfield_10000", "Custom Project");
                logger.debug("targetFields: " + targetFields);
                return targetFields;
            }
        });

        return Response.ok(obj).build();
    }

    @Path("/hubPolicies")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHubPolicies(@Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }
        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {

                final String policyRulesJson = getStringValue(settings,
                        HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON);

                final HubJiraConfigSerializable config = new HubJiraConfigSerializable();

                final HubIntRestService restService = getHubRestService(settings, config);

                if (StringUtils.isNotBlank(policyRulesJson)) {
                    config.setPolicyRulesJson(policyRulesJson);
                }
                setHubPolicyRules(restService, config);
                return config;
            }
        });
        return Response.ok(obj).build();
    }

    @Path("/mappings")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMappings(@Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }
        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {

                final HubJiraConfigSerializable config = new HubJiraConfigSerializable();

                final String hubProjectMappingsJson = getStringValue(settings,
                        HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);

                config.setHubProjectMappingsJson(hubProjectMappingsJson);

                checkMappingErrors(config);
                return config;
            }
        });
        return Response.ok(obj).build();
    }

    @Path("/fieldCopyMappings")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFieldCopyMappings(@Context final HttpServletRequest request) {
        logger.debug("Get /fieldCopyMappings");
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }
        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {

                final HubJiraFieldCopyConfigSerializable config = new HubJiraFieldCopyConfigSerializable();

                final String hubFieldCopyMappingsJson = getStringValue(settings,
                        HubJiraConfigKeys.HUB_CONFIG_FIELD_COPY_MAPPINGS_JSON);

                logger.debug("Get /fieldCopyMappings returning JSON: " + hubFieldCopyMappingsJson);
                config.setJson(hubFieldCopyMappingsJson);
                logger.debug("HubJiraFieldCopyConfigSerializable.getJson(): " + config.getJson());
                // TODO: checkMappingErrors(config);
                return config;
            }
        });
        HubJiraFieldCopyConfigSerializable returnValue = (HubJiraFieldCopyConfigSerializable) obj;
        logger.debug("returnValue: " + returnValue);
        return Response.ok(obj).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final HubJiraConfigSerializable config, @Context final HttpServletRequest request) {
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

                final HubIntRestService restService = getHubRestService(settings, config);
                final List<HubProject> hubProjects = getHubProjects(restService, config);
                config.setHubProjects(hubProjects);
                config.setJiraProjects(jiraProjects);
                checkIntervalErrors(config);
                checkMappingErrors(config);
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
                setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_POLICY_RULES_JSON, config.getPolicyRulesJson());
                setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_PROJECT_MAPPINGS_JSON,
                        config.getHubProjectMappingsJson());
                setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_USER, username);
                updateHubTaskInterval(previousInterval, config.getIntervalBetweenChecks());
                return null;
            }
        });
        if (config.hasErrors()) {
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
                final HashMap<String, String> ticketErrors;
                if (errorObject != null) {
                    ticketErrors = (HashMap<String, String>) errorObject;
                } else {
                    ticketErrors = new HashMap<>();
                }
                if (errorsToDelete.getHubJiraTicketErrors() != null
                        && !errorsToDelete.getHubJiraTicketErrors().isEmpty()) {
                    for (final TicketCreationError creationError : errorsToDelete.getHubJiraTicketErrors()) {
                        try {
                            final String errorMessage = URLDecoder.decode(creationError.getStackTrace(), "UTF-8");
                            final String val = ticketErrors.remove(errorMessage);
                            if (val == null) {
                                final TicketCreationErrorSerializable serializableError = new TicketCreationErrorSerializable();
                                serializableError.setConfigError(
                                        "Could not find the Error selected for removal in the persisted list.");
                                return serializableError;
                            }
                        } catch (final UnsupportedEncodingException e) {

                        }
                    }
                }
                setValue(settings, HubJiraConstants.HUB_JIRA_ERROR, ticketErrors);
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
        final String username = userManager.getRemoteUsername(request);
        if (username == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        final boolean userIsSysAdmin = userManager.isSystemAdmin(username);

        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {
                final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();

                final HubAdminConfigSerializable responseObject = new HubAdminConfigSerializable();

                if (!userIsSysAdmin) {
                    responseObject.setHubJiraGroupsError(JiraConfigErrors.NON_SYSTEM_ADMINS_CANT_CHANGE_GROUPS);
                    return responseObject;
                } else {
                    setValue(settings, HubJiraConfigKeys.HUB_CONFIG_JIRA_GROUPS, adminConfig.getHubJiraGroups());
                }
                return null;
            }
        });

        if (obj != null) {
            return Response.ok(obj).status(Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

    @Path("/fieldCopy")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateFieldCopyConfiguration(final HubJiraFieldCopyConfigSerializable fieldCopyConfig,
            @Context final HttpServletRequest request) {

        logger.debug("fieldCopyConfig.getProjectFieldCopyMappings(): " + fieldCopyConfig.getProjectFieldCopyMappings());
        for (ProjectFieldCopyMapping projectFieldCopyMapping : fieldCopyConfig.getProjectFieldCopyMappings()) {
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
                // TODO validation??
                setValue(settings, HubJiraConfigKeys.HUB_CONFIG_FIELD_COPY_MAPPINGS_JSON,
                        fieldCopyConfig.getJson());
                return null;
            }
        });
        if (fieldCopyConfig.hasErrors()) {
            return Response.ok(fieldCopyConfig).status(Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

    @Path("/reset")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resetHubJiraKeys(final Object object, @Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }

        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {
                try {
                    final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
                    final Date runDate = new Date();

                    final SimpleDateFormat dateFormatter = new SimpleDateFormat(RestConnection.JSON_DATE_FORMAT);
                    dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("Zulu"));

                    setValue(settings, HubJiraConfigKeys.HUB_CONFIG_LAST_RUN_DATE, dateFormatter.format(runDate));
                    setValue(settings, HubJiraConstants.HUB_JIRA_ERROR, null);
                } catch (final Exception e) {
                    return e.getMessage();
                }
                return null;
            }
        });

        if (obj != null) {
            return Response.ok(obj).status(Status.BAD_REQUEST).build();
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
            // the new interval was not an integer
        }
    }

    private void checkIntervalErrors(final HubJiraConfigSerializable config) {
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

    private void checkMappingErrors(final HubJiraConfigSerializable config) {
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

    public HubIntRestService getHubRestService(final PluginSettings settings, final ErrorTracking config) {
        final String hubUrl = getStringValue(settings, HubConfigKeys.CONFIG_HUB_URL);
        final String hubUser = getStringValue(settings, HubConfigKeys.CONFIG_HUB_USER);
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

        HubIntRestService hubRestService = null;
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

            final HubServerConfig serverConfig = configBuilder.build();
            if (configBuilder.buildResults().hasErrors()) {
                config.setErrorMessage(JiraConfigErrors.CHECK_HUB_SERVER_CONFIGURATION);
                return null;
            }

            final RestConnection restConnection = new CredentialsRestConnection(logger, serverConfig);
            restConnection.setTimeout(serverConfig.getTimeout());
            restConnection.setProxyProperties(serverConfig.getProxyInfo());
            restConnection.setCookies(serverConfig.getGlobalCredentials().getUsername(),
                    serverConfig.getGlobalCredentials().getDecryptedPassword());

            hubRestService = new HubIntRestService(restConnection);
        } catch (IllegalArgumentException | URISyntaxException | BDRestException | EncryptionException e) {
            config.setErrorMessage(JiraConfigErrors.CHECK_HUB_SERVER_CONFIGURATION + " :: " + e.getMessage());
            return null;
        }
        return hubRestService;
    }

    private List<HubProject> getHubProjects(final HubIntRestService hubRestService,
            final ErrorTracking config) {
        final List<HubProject> hubProjects = new ArrayList<>();
        if (hubRestService != null) {
            List<ProjectItem> hubProjectItems = null;
            try {
                hubProjectItems = hubRestService.getProjectMatches(null);
            } catch (IOException | BDRestException | URISyntaxException e) {
                config.setErrorMessage(concatErrorMessage(config.getErrorMessage(), e.getMessage()));
            }

            final HubItemFilterUtil<ProjectItem> filter = new HubItemFilterUtil<>();
            hubProjectItems = filter.getAccessibleItems(hubProjectItems);

            if (hubProjectItems != null && !hubProjectItems.isEmpty()) {
                for (final ProjectItem project : hubProjectItems) {
                    final HubProject newHubProject = new HubProject();
                    newHubProject.setProjectName(project.getName());
                    newHubProject.setProjectUrl(project.getMeta().getHref());
                    hubProjects.add(newHubProject);
                }
            }
        }
        return hubProjects;
    }

    private void setHubPolicyRules(final HubIntRestService restService, final HubJiraConfigSerializable config) {

        final List<PolicyRuleSerializable> newPolicyRules = new ArrayList<>();
        if (restService != null) {
            final HubSupportHelper supportHelper = new HubSupportHelper();
            try {
                supportHelper.checkHubSupport(restService, null);

                if (supportHelper.hasCapability(HubCapabilitiesEnum.POLICY_API)) {

                    final PolicyRestService policyService = getPolicyService(restService.getRestConnection());

                    List<PolicyRule> policyRules = null;
                    try {
                        policyRules = policyService.getAllPolicyRules();
                    } catch (IOException | URISyntaxException | BDRestException e) {
                        config.setPolicyRulesError(e.getMessage());
                    }

                    if (policyRules != null && !policyRules.isEmpty()) {
                        for (final PolicyRule rule : policyRules) {
                            logger.debug("Rule: " + rule);
                            final PolicyRuleSerializable newRule = new PolicyRuleSerializable();
                            String description = rule.getDescription();
                            if (description == null) {
                                description = "";
                            }
                            newRule.setDescription(description.trim());
                            newRule.setName(rule.getName().trim());
                            newRule.setPolicyUrl(rule.getMeta().getHref());
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
            } catch (IOException | URISyntaxException e) {
                config.setPolicyRulesError(e.getMessage());
            }
        }
        config.setPolicyRules(newPolicyRules);
        if (config.getPolicyRules().isEmpty()) {
            config.setPolicyRulesError(
                    concatErrorMessage(config.getPolicyRulesError(), JiraConfigErrors.NO_POLICY_RULES_FOUND_ERROR));
        }

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

    public PolicyRestService getPolicyService(final RestConnection restConnection) {
        return new PolicyRestService(restConnection, gson, jsonParser);
    }

    private final Map<String, String> expireOldErrors(final PluginSettings pluginSettings) {
        final Object errorObject = getValue(pluginSettings, HubJiraConstants.HUB_JIRA_ERROR);
        if (errorObject != null) {
            final HashMap<String, String> ticketErrors = (HashMap<String, String>) errorObject;

            if (ticketErrors != null && !ticketErrors.isEmpty()) {
                final DateTime currentTime = DateTime.now();
                final Iterator<Entry<String, String>> s = ticketErrors.entrySet().iterator();
                while (s.hasNext()) {
                    final Entry<String, String> ticketError = s.next();
                    final DateTime errorTime = DateTime.parse(ticketError.getValue(),
                            JiraSettingsService.ERROR_TIME_FORMAT);
                    if (Days.daysBetween(errorTime, currentTime).isGreaterThan(Days.days(30))) {
                        s.remove();
                    }
                }
                setValue(pluginSettings, HubJiraConstants.HUB_JIRA_ERROR, ticketErrors);
                return ticketErrors;
            }
        }
        return null;
    }
}
