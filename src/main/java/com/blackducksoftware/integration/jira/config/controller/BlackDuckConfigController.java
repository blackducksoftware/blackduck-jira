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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

import org.apache.commons.lang3.StringUtils;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.common.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.common.PluginSettingsWrapper;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.model.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.config.JiraConfigErrorStrings;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;
import com.blackducksoftware.integration.jira.config.PluginConfigKeys;
import com.blackducksoftware.integration.jira.config.TicketCreationError;
import com.blackducksoftware.integration.jira.config.controller.action.BlackDuckConfigActions;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.config.model.BlackDuckServerConfigSerializable;
import com.blackducksoftware.integration.jira.config.model.TicketCreationErrorSerializable;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.rest.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.ProjectService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.util.IntEnvironmentVariables;

@Path("/config/blackduck")
public class BlackDuckConfigController extends ConfigController {
    private final BlackDuckConfigActions blackDuckConfigActions;

    public BlackDuckConfigController(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate) {
        super(pluginSettingsFactory, transactionTemplate, userManager);
        this.blackDuckConfigActions = new BlackDuckConfigActions();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context final HttpServletRequest request) {
        final PluginSettingsWrapper pluginSettingsWrapper = createSettingsWrapper();
        final boolean validAuthentication = getAuthenticationChecker().isValidAuthentication(request, pluginSettingsWrapper.getParsedBlackDuckConfigGroups());
        if (!validAuthentication) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        final BlackDuckServerConfigSerializable config = executeAsTransaction(() -> blackDuckConfigActions.getStoredBlackDuckConfig(pluginSettingsWrapper));
        return Response.ok(config).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final BlackDuckServerConfigSerializable config, @Context final HttpServletRequest request) {
        final PluginSettingsWrapper pluginSettingsWrapper = createSettingsWrapper();
        final boolean validAuthentication = getAuthenticationChecker().isValidAuthentication(request, pluginSettingsWrapper.getParsedBlackDuckConfigGroups());
        if (!validAuthentication) {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        final BlackDuckServerConfigSerializable modifiedConfig = executeAsTransaction(() -> blackDuckConfigActions.updateBlackDuckConfig(config, pluginSettingsWrapper));
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
            final PluginSettingsWrapper pluginSettingsWrapper = createSettingsWrapper();
            final boolean validAuthentication = getAuthenticationChecker().isValidAuthentication(request, pluginSettingsWrapper.getParsedBlackDuckConfigGroups());
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }

            final BlackDuckServerConfigSerializable modifiedConfig = executeAsTransaction(() -> blackDuckConfigActions.testConnection(config, pluginSettingsWrapper));
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
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final PluginSettingsWrapper pluginSettingsWrapper = new PluginSettingsWrapper(settings);
            final boolean validAuthentication = getAuthenticationChecker().isValidAuthentication(request, pluginSettingsWrapper.getParsedBlackDuckConfigGroups());
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            projectsConfig = executeAsTransaction(() -> {
                final BlackDuckServicesFactory blackDuckServicesFactory;
                try {
                    blackDuckServicesFactory = createBlackDuckServicesFactory(settings);
                    final List<String> blackDuckProjects = getBlackDuckProjects(blackDuckServicesFactory);

                    if (blackDuckProjects.size() == 0) {
                        return JiraConfigErrorStrings.NO_BLACKDUCK_PROJECTS_FOUND;
                    }
                    return blackDuckProjects;
                } catch (final ConfigurationException e) {
                    return e.getMessage();
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

    @Path("/policies")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlackDuckPolicies(@Context final HttpServletRequest request) {
        final Object config;
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final PluginSettingsWrapper pluginSettingsWrapper = new PluginSettingsWrapper(settings);
            final boolean validAuthentication = getAuthenticationChecker().isValidAuthentication(request, pluginSettingsWrapper.getParsedBlackDuckConfigGroups());
            if (!validAuthentication) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
            config = executeAsTransaction(() -> {
                final String policyRulesJson = pluginSettingsWrapper.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_POLICY_RULES_JSON);
                final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();

                if (StringUtils.isNotBlank(policyRulesJson)) {
                    txConfig.setPolicyRulesJson(policyRulesJson);
                } else {
                    txConfig.setPolicyRules(new ArrayList<>(0));
                }

                final BlackDuckServicesFactory blackDuckServicesFactory;
                try {
                    blackDuckServicesFactory = createBlackDuckServicesFactory(settings);
                    setBlackDuckPolicyRules(blackDuckServicesFactory, txConfig);
                } catch (final ConfigurationException e) {
                    txConfig.setErrorMessage(e.getMessage());
                }
                return txConfig;
            });
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
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final PluginSettingsWrapper pluginSettingsWrapper = new PluginSettingsWrapper(settings);
        final boolean validAuthentication = getAuthenticationChecker().isValidAuthentication(request, pluginSettingsWrapper.getParsedBlackDuckConfigGroups());
        if (!validAuthentication) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        final Object obj = executeAsTransaction(() -> {
            final TicketCreationErrorSerializable creationError = new TicketCreationErrorSerializable();

            final List<TicketCreationError> ticketErrors = JiraSettingsService.expireOldErrors(settings);
            if (ticketErrors != null) {
                Collections.sort(ticketErrors);
                creationError.setHubJiraTicketErrors(ticketErrors);
                logger.debug("Errors to UI : " + creationError.getHubJiraTicketErrors().size());
            }
            return creationError;
        });

        return Response.ok(obj).build();
    }

    // This must be "package protected" to avoid synthetic access
    BlackDuckServicesFactory createBlackDuckServicesFactory(final PluginSettings settings) throws ConfigurationException {
        final BlackDuckHttpClient restConnection = createRestConnection(new PluginSettingsWrapper(settings));
        final BlackDuckServicesFactory blackDuckServicesFactory = new BlackDuckServicesFactory(new IntEnvironmentVariables(), BlackDuckServicesFactory.createDefaultGson(), BlackDuckServicesFactory.createDefaultObjectMapper(),
            restConnection, logger);
        return blackDuckServicesFactory;
    }

    private BlackDuckHttpClient createRestConnection(final PluginSettingsWrapper settings) throws ConfigurationException {
        final String blackDuckUrl = settings.getBlackDuckUrl();
        final String blackDuckApiToken = settings.getBlackDuckApiToken();
        final Optional<Integer> blackDuckTimeout = settings.getBlackDuckTimeout();
        final Boolean blackDuckTrustCert = settings.getBlackDuckAlwaysTrust();

        if (StringUtils.isBlank(blackDuckApiToken)) {
            throw new ConfigurationException(JiraConfigErrorStrings.BLACKDUCK_SERVER_MISCONFIGURATION + " " + JiraConfigErrorStrings.CHECK_BLACKDUCK_SERVER_CONFIGURATION);
        }

        final String blackDuckProxyHost = settings.getBlackDuckProxyHost();
        final Optional<Integer> blackDuckProxyPort = settings.getBlackDuckProxyPort();
        final String blackDuckProxyUser = settings.getBlackDuckProxyUser();
        final String encBlackDuckProxyPassword = settings.getBlackDuckProxyPassword();

        final BlackDuckHttpClient restConnection;
        try {
            final BlackDuckServerConfigBuilder configBuilder = new BlackDuckServerConfigBuilder();
            configBuilder.setUrl(blackDuckUrl);
            configBuilder.setApiToken(blackDuckApiToken);
            configBuilder.setTimeout(blackDuckTimeout.orElse(300));
            configBuilder.setTrustCert(blackDuckTrustCert);
            configBuilder.setProxyHost(blackDuckProxyHost);
            blackDuckProxyPort.ifPresent(configBuilder::setProxyPort);
            configBuilder.setProxyUsername(blackDuckProxyUser);
            configBuilder.setProxyPassword(encBlackDuckProxyPassword);

            final BlackDuckServerConfig serverConfig;
            try {
                serverConfig = configBuilder.build();
            } catch (final IllegalStateException e) {
                logger.error("Error in Black Duck server configuration: " + e.getMessage());
                throw new ConfigurationException(JiraConfigErrorStrings.CHECK_BLACKDUCK_SERVER_CONFIGURATION);
            }

            restConnection = serverConfig.createBlackDuckHttpClient(logger);
        } catch (final IllegalArgumentException e) {
            throw new ConfigurationException(JiraConfigErrorStrings.CHECK_BLACKDUCK_SERVER_CONFIGURATION + " :: " + e.getMessage());
        }
        return restConnection;
    }

    // This must be "package protected" to avoid synthetic access
    List<String> getBlackDuckProjects(final BlackDuckServicesFactory blackDuckServicesFactory) throws ConfigurationException {
        final List<String> blackDuckProjects = new ArrayList<>();
        blackDuckProjects.add(BlackDuckProjectMappings.MAP_ALL_PROJECTS);

        final ProjectService projectRequestService = blackDuckServicesFactory.createProjectService();
        final List<ProjectView> blackDuckProjectItems;
        try {
            blackDuckProjectItems = projectRequestService.getAllProjectMatches(null);
        } catch (final IntegrationException e) {
            throw new ConfigurationException(e.getMessage());
        }

        if (blackDuckProjectItems != null && !blackDuckProjectItems.isEmpty()) {
            for (final ProjectView project : blackDuckProjectItems) {
                final List<String> allowedMethods = project.getAllowedMethods();
                if (allowedMethods != null && !allowedMethods.isEmpty() && allowedMethods.contains("GET") && allowedMethods.contains("PUT")) {
                    blackDuckProjects.add(project.getName());
                }
            }
        }
        return blackDuckProjects;
    }

    // This must be "package protected" to avoid synthetic access
    void setBlackDuckPolicyRules(final BlackDuckServicesFactory blackDuckServicesFactory, final BlackDuckJiraConfigSerializable config) {
        final List<PolicyRuleSerializable> newPolicyRules = new ArrayList<>();
        if (blackDuckServicesFactory != null) {
            final BlackDuckService blackDuckService = blackDuckServicesFactory.createBlackDuckService();
            try {
                List<PolicyRuleView> policyRules = null;
                try {
                    policyRules = blackDuckService.getAllResponses(ApiDiscovery.POLICY_RULES_LINK_RESPONSE);
                } catch (final BlackDuckIntegrationException e) {
                    config.setPolicyRulesError(e.getMessage());
                } catch (final IntegrationRestException ire) {
                    if (ire.getHttpStatusCode() == 402) {
                        config.setPolicyRulesError(JiraConfigErrorStrings.NO_POLICY_LICENSE_FOUND);
                    } else {
                        config.setPolicyRulesError(ire.getMessage());
                    }
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

                        final Optional<String> ruleHref = rule.getHref();
                        if (ruleHref.isPresent()) {
                            newRule.setPolicyUrl(ruleHref.get());
                        } else {
                            logger.error("URL for policy rule" + rule.getName() + " does not exist.");
                            config.setPolicyRulesError(JiraConfigErrorStrings.POLICY_RULE_URL_ERROR);
                            continue;
                        }

                        newRule.setEnabled(rule.getEnabled());
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
            config.setPolicyRulesError(StringUtils.joinWith(" : ", config.getPolicyRulesError(), JiraConfigErrorStrings.NO_POLICY_RULES_FOUND_ERROR));
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

    private PluginSettingsWrapper createSettingsWrapper() {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        return new PluginSettingsWrapper(settings);
    }
}
