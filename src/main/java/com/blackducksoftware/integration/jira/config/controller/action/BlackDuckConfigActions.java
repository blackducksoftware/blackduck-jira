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
package com.blackducksoftware.integration.jira.config.controller.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.BlackDuckProjectMappings;
import com.blackducksoftware.integration.jira.common.blackduck.BlackDuckConnectionHelper;
import com.blackducksoftware.integration.jira.common.model.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.common.settings.GlobalConfigurationAccessor;
import com.blackducksoftware.integration.jira.common.settings.JiraSettingsAccessor;
import com.blackducksoftware.integration.jira.common.settings.model.PluginBlackDuckServerConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.config.JiraConfigErrorStrings;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.config.model.BlackDuckServerConfigSerializable;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class BlackDuckConfigActions {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final GlobalConfigurationAccessor globalConfigurationAccessor;
    private final BlackDuckConnectionHelper blackDuckConnectionHelper;

    public BlackDuckConfigActions(final JiraSettingsAccessor jiraSettingsAccessor, final BlackDuckConnectionHelper blackDuckConnectionHelper) {
        this.globalConfigurationAccessor = jiraSettingsAccessor.createGlobalConfigurationAccessor();
        this.blackDuckConnectionHelper = blackDuckConnectionHelper;
    }

    public BlackDuckServerConfigSerializable getStoredBlackDuckConfig() {
        final BlackDuckServerConfigSerializable config = new BlackDuckServerConfigSerializable();
        final PluginBlackDuckServerConfigModel blackDuckServerConfig = globalConfigurationAccessor.getBlackDuckServerConfig();

        final String blackDuckUrl = blackDuckServerConfig.getUrl();
        logger.debug(String.format("Returning Black Duck details for %s", blackDuckUrl));

        config.setHubUrl(blackDuckUrl);
        if (StringUtils.isNotBlank(blackDuckServerConfig.getApiToken())) {
            config.setApiTokenLength(blackDuckServerConfig.getApiToken().length());
            config.setApiToken(config.getMaskedApiToken());
        }

        blackDuckServerConfig.getTimeoutInSeconds().map(Objects::toString).ifPresent(config::setTimeout);
        config.setTrustCert(blackDuckServerConfig.getTrustCert());

        config.setHubProxyHost(blackDuckServerConfig.getProxyHost());
        blackDuckServerConfig.getProxyPort().map(String::valueOf).ifPresent(config::setHubProxyPort);
        config.setHubProxyUser(blackDuckServerConfig.getProxyUsername());
        if (StringUtils.isNotBlank(blackDuckServerConfig.getProxyPassword())) {
            config.setHubProxyPasswordLength(blackDuckServerConfig.getProxyPassword().length());
            config.setHubProxyPassword(config.getMaskedProxyPassword());
        }
        validateAndUpdateErrorsOnConfig(config);
        return config;
    }

    public BlackDuckServerConfigSerializable updateBlackDuckConfig(final BlackDuckServerConfigSerializable config) {
        final BlackDuckServerConfigSerializable newConfig = new BlackDuckServerConfigSerializable(config);

        final Integer intTimeout = StringUtils.isNotBlank(newConfig.getTimeout()) ? Integer.parseInt(newConfig.getTimeout()) : null;
        final Integer proxyPort = StringUtils.isNotBlank(newConfig.getHubProxyPort()) ? Integer.parseInt(newConfig.getHubProxyPort()) : null;
        final Boolean trustCert = Boolean.parseBoolean(newConfig.getTrustCert());
        final PluginBlackDuckServerConfigModel pluginBlackDuckServerConfigModel =
            new PluginBlackDuckServerConfigModel(newConfig.getHubUrl(), newConfig.getApiToken(), intTimeout, trustCert, newConfig.getHubProxyHost(), proxyPort, newConfig.getHubProxyUser(), newConfig.getHubProxyPassword());

        logger.debug(String.format("Saving connection to %s...", newConfig.getHubUrl()));
        globalConfigurationAccessor.setBlackDuckServerConfig(pluginBlackDuckServerConfigModel);

        validateAndUpdateErrorsOnConfig(newConfig);
        return newConfig;
    }

    public Object getBlackDuckProjects() {
        try {
            final PluginBlackDuckServerConfigModel blackDuckServerConfig = globalConfigurationAccessor.getBlackDuckServerConfig();
            final BlackDuckServicesFactory blackDuckServicesFactory = blackDuckConnectionHelper.createBlackDuckServicesFactory(logger, blackDuckServerConfig.createBlackDuckServerConfigBuilder());
            final List<String> blackDuckProjects = blackDuckConnectionHelper.getBlackDuckProjects(blackDuckServicesFactory);
            blackDuckProjects.add(0, BlackDuckProjectMappings.MAP_ALL_PROJECTS);

            if (blackDuckProjects.size() == 0) {
                return JiraConfigErrorStrings.NO_BLACKDUCK_PROJECTS_FOUND;
            }
            return blackDuckProjects;
        } catch (final IntegrationException e) {
            return e.getMessage();
        }
    }

    public BlackDuckServerConfigSerializable testConnection(final BlackDuckServerConfigSerializable config) {
        BlackDuckServerConfigSerializable newConfig = new BlackDuckServerConfigSerializable(config);
        validateAndUpdateErrorsOnConfig(newConfig);

        if (newConfig.hasErrors()) {
            return newConfig;
        } else {
            newConfig = getUnMaskedConfig(newConfig);
            final BlackDuckServerConfigBuilder serverConfigBuilder = new BlackDuckServerConfigBuilder();
            serverConfigBuilder.setLogger(logger);
            serverConfigBuilder.setUrl(newConfig.getHubUrl());
            serverConfigBuilder.setTimeoutInSeconds(newConfig.getTimeout());
            serverConfigBuilder.setApiToken(newConfig.getApiToken());
            serverConfigBuilder.setTrustCert(newConfig.getTrustCert());

            serverConfigBuilder.setProxyHost(newConfig.getHubProxyHost());
            serverConfigBuilder.setProxyPort(newConfig.getHubProxyPort());
            serverConfigBuilder.setProxyUsername(newConfig.getHubProxyUser());
            serverConfigBuilder.setProxyPassword(newConfig.getHubProxyPassword());

            try {
                serverConfigBuilder.build();
            } catch (final IllegalStateException e) {
                newConfig.setTestConnectionError(e.getMessage());
            }

            return newConfig;
        }
    }

    public BlackDuckJiraConfigSerializable getBlackDuckPolicies() {
        final PluginIssueCreationConfigModel issueCreationConfig = globalConfigurationAccessor.getIssueCreationConfig();
        final String policyRulesJson = issueCreationConfig.getTicketCriteria().getPolicyRulesJson();
        final BlackDuckJiraConfigSerializable txConfig = new BlackDuckJiraConfigSerializable();

        if (StringUtils.isNotBlank(policyRulesJson)) {
            txConfig.setPolicyRulesJson(policyRulesJson);
        } else {
            txConfig.setPolicyRules(new ArrayList<>(0));
        }

        final BlackDuckServicesFactory blackDuckServicesFactory;
        try {
            final PluginBlackDuckServerConfigModel blackDuckServerConfig = globalConfigurationAccessor.getBlackDuckServerConfig();
            blackDuckServicesFactory = blackDuckConnectionHelper.createBlackDuckServicesFactory(logger, blackDuckServerConfig.createBlackDuckServerConfigBuilder());
            setBlackDuckPolicyRules(blackDuckServicesFactory, txConfig);
        } catch (final IntegrationException e) {
            txConfig.setErrorMessage(e.getMessage());
        }
        return txConfig;
    }

    private BlackDuckServerConfigSerializable getUnMaskedConfig(final BlackDuckServerConfigSerializable currentConfig) {
        final PluginBlackDuckServerConfigModel blackDuckServerConfig = globalConfigurationAccessor.getBlackDuckServerConfig();
        final BlackDuckServerConfigSerializable newConfig = new BlackDuckServerConfigSerializable(currentConfig);

        if (StringUtils.isNotBlank(newConfig.getApiToken()) && newConfig.isApiTokenMasked()) {
            newConfig.setApiToken(blackDuckServerConfig.getApiToken());
        }
        if (StringUtils.isNotBlank(newConfig.getHubProxyPassword()) && newConfig.isProxyPasswordMasked()) {
            final String proxyPassword = blackDuckServerConfig.getProxyPassword();
            if (null != proxyPassword) {
                newConfig.setHubProxyPassword(proxyPassword);
                newConfig.setHubProxyPasswordLength(proxyPassword.length());
            }
        }

        return newConfig;
    }

    // TODO create a BlackDuck class that handles most BlackDuck functionality and clean this method up
    private void setBlackDuckPolicyRules(final BlackDuckServicesFactory blackDuckServicesFactory, final BlackDuckJiraConfigSerializable config) {
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
            config.setPolicyRulesError(org.apache.commons.lang3.StringUtils.joinWith(" : ", config.getPolicyRulesError(), JiraConfigErrorStrings.NO_POLICY_RULES_FOUND_ERROR));
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

    // This method must be "package protected" to avoid synthetic access
    void validateAndUpdateErrorsOnConfig(final BlackDuckServerConfigSerializable config) {
        blackDuckConnectionHelper.validateBlackDuckUrl(config.getHubUrl()).ifPresent(config::setHubUrlError);
        blackDuckConnectionHelper.validateBlackDuckTimeout(config.getTimeout()).ifPresent(config::setTimeoutError);
        blackDuckConnectionHelper.validateBlackDuckApiToken(config.getApiToken()).ifPresent(config::setApiTokenError);
        blackDuckConnectionHelper.validateProxy(config);
    }

}
