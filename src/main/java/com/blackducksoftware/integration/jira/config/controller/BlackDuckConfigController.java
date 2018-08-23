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
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.transaction.TransactionCallback;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.atlassian.sal.api.user.UserManager;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.config.BlackDuckConfigKeys;
import com.blackducksoftware.integration.jira.config.model.BlackDuckServerConfigSerializable;
import com.synopsys.integration.blackduck.ApiTokenField;
import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.configuration.HubServerConfigBuilder;
import com.synopsys.integration.blackduck.configuration.HubServerConfigFieldEnum;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.encryption.PasswordEncrypter;
import com.synopsys.integration.exception.EncryptionException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.credentials.CredentialsField;
import com.synopsys.integration.rest.proxy.ProxyInfoField;
import com.synopsys.integration.validator.AbstractValidator;
import com.synopsys.integration.validator.ValidationResults;

@Path("/blackDuckDetails")
public class BlackDuckConfigController {
    // This variable must be "package protected" to avoid synthetic access
    final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final UserManager userManager;
    private final PluginSettingsFactory pluginSettingsFactory;
    private final TransactionTemplate transactionTemplate;

    public BlackDuckConfigController(final UserManager userManager, final PluginSettingsFactory pluginSettingsFactory, final TransactionTemplate transactionTemplate) {
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
        this.transactionTemplate = transactionTemplate;
    }

    private Response checkUserPermissions(final HttpServletRequest request, final PluginSettings settings) {
        final String username = userManager.getRemoteUsername(request);
        if (username == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        if (userManager.isSystemAdmin(username)) {
            return null;
        }

        final String blackDuckJiraGroupsString = getValue(settings, BlackDuckConfigKeys.BLACKDUCK_CONFIG_GROUPS);

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
                return null;
            }
        }
        return Response.status(Status.UNAUTHORIZED).build();
    }

    @Path("/read")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }

        final Object obj = transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {
                final String blackDuckUrl = getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_URL);
                logger.debug(String.format("Returning Black Duck details for %s", blackDuckUrl));
                final String apiToken = getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN);
                final String username = getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_USER);
                final String password = getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_PASS);
                final String passwordLength = getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_PASS_LENGTH);
                final String timeout = getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_TIMEOUT);
                final String trustCert = getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_TRUST_CERT);
                final String proxyHost = getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_HOST);
                final String proxyPort = getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PORT);
                final String noProxyHosts = getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_NO_HOST);
                final String proxyUser = getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_USER);
                final String proxyPassword = getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS);
                final String proxyPasswordLength = getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH);

                final BlackDuckServerConfigSerializable config = new BlackDuckServerConfigSerializable();

                final HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
                serverConfigBuilder.setUrl(blackDuckUrl);
                serverConfigBuilder.setTimeout(timeout);
                serverConfigBuilder.setTrustCert(trustCert);
                serverConfigBuilder.setApiToken(apiToken);
                serverConfigBuilder.setUsername(username);
                serverConfigBuilder.setPassword(password);
                serverConfigBuilder.setPasswordLength(NumberUtils.toInt(passwordLength));
                serverConfigBuilder.setProxyHost(proxyHost);
                serverConfigBuilder.setProxyPort(proxyPort);
                serverConfigBuilder.setProxyIgnoredHosts(noProxyHosts);
                serverConfigBuilder.setProxyUsername(proxyUser);
                serverConfigBuilder.setProxyPassword(proxyPassword);
                serverConfigBuilder.setProxyPasswordLength(NumberUtils.toInt(proxyPasswordLength));

                setConfigFromResult(config, serverConfigBuilder.createValidator());

                config.setHubUrl(blackDuckUrl);
                if (StringUtils.isNotBlank(apiToken)) {
                    config.setApiTokenLength(apiToken.length());
                    config.setApiToken(config.getMaskedApiToken());
                }
                config.setUsername(username);
                if (StringUtils.isNotBlank(password)) {
                    final int passwordLengthInt = getIntFromObject(passwordLength);
                    if (passwordLengthInt > 0) {
                        config.setPasswordLength(passwordLengthInt);
                        config.setPassword(config.getMaskedPassword());
                    }
                }
                config.setTimeout(timeout);
                config.setTrustCert(trustCert);
                config.setHubProxyHost(proxyHost);
                config.setHubProxyPort(proxyPort);
                config.setHubNoProxyHosts(noProxyHosts);
                config.setHubProxyUser(proxyUser);
                if (StringUtils.isNotBlank(proxyPassword)) {
                    final int blackDuckProxyPasswordLength = getIntFromObject(proxyPasswordLength);
                    if (blackDuckProxyPasswordLength > 0) {
                        config.setHubProxyPasswordLength(blackDuckProxyPasswordLength);
                        config.setHubProxyPassword(config.getMaskedProxyPassword());
                    }
                }
                return config;
            }
        });

        return Response.ok(obj).build();
    }

    // This method must be "package protected" to avoid synthetic access
    int getIntFromObject(final String value) {
        if (StringUtils.isNotBlank(value)) {
            return NumberUtils.toInt(value);
        }
        return 0;
    }

    @Path("/save")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(final BlackDuckServerConfigSerializable config, @Context final HttpServletRequest request) {
        final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
        final Response response = checkUserPermissions(request, settings);
        if (response != null) {
            return response;
        }

        transactionTemplate.execute(new TransactionCallback() {
            @Override
            public Object doInTransaction() {
                final HubServerConfigBuilder serverConfigBuilder = setConfigBuilderFromSerializableConfig(config, settings);

                setConfigFromResult(config, serverConfigBuilder.createValidator());

                logger.debug(String.format("Saving connection to %s...", config.getHubUrl()));
                setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_URL, config.getHubUrl());
                final String apiToken = config.getApiToken();
                if (!config.isApiTokenMasked()) {
                    if (StringUtils.isNotBlank(apiToken)) {
                        setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN, apiToken);
                    } else {
                        setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN, null);
                    }
                }
                setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_USER, config.getUsername());

                final String password = config.getPassword();
                if (StringUtils.isNotBlank(password) && !config.isPasswordMasked()) {
                    // only update the stored password if it is not the masked
                    // password used for display
                    try {
                        final String encPassword = PasswordEncrypter.encrypt(password);
                        setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_PASS, encPassword);
                        setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_PASS_LENGTH, String.valueOf(password.length()));
                    } catch (IllegalArgumentException | EncryptionException e) {
                        // This error was swallowed; not sure why. Adding a log message
                        logger.error("Error encrypting password: " + e.getMessage());
                    }
                } else if (StringUtils.isBlank(password)) {
                    setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_PASS, null);
                    setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_PASS_LENGTH, null);
                }
                setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_TIMEOUT, config.getTimeout());
                setValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_TRUST_CERT, config.getTrustCert());
                setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_HOST, config.getHubProxyHost());
                setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PORT, config.getHubProxyPort());
                setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_NO_HOST, config.getHubNoProxyHosts());
                setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_USER, config.getHubProxyUser());

                final String proxyPassword = config.getHubProxyPassword();
                if (StringUtils.isNotBlank(proxyPassword) && !config.isProxyPasswordMasked()) {
                    // only update the stored password if it is not the masked password used for display
                    try {
                        final String encryptedProxyPassword = PasswordEncrypter.encrypt(proxyPassword);
                        setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS, encryptedProxyPassword);
                        setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH,
                                String.valueOf(proxyPassword.length()));
                    } catch (IllegalArgumentException | EncryptionException e) {
                    }
                } else if (StringUtils.isBlank(proxyPassword)) {
                    setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS, null);
                    setValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH, null);
                }
                return null;
            }
        });

        if (config.hasErrors()) {
            return Response.ok(config).status(Status.BAD_REQUEST).build();
        }
        return Response.noContent().build();
    }

    @Path("/testConnection")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response testConnection(final BlackDuckServerConfigSerializable config, @Context final HttpServletRequest request) {
        try {
            final PluginSettings settings = pluginSettingsFactory.createGlobalSettings();
            final Response response = checkUserPermissions(request, settings);
            if (response != null) {
                return response;
            }

            transactionTemplate.execute(new TransactionCallback() {
                @Override
                public Object doInTransaction() {
                    final HubServerConfigBuilder serverConfigBuilder = setConfigBuilderFromSerializableConfig(config, settings);

                    setConfigFromResult(config, serverConfigBuilder.createValidator());

                    if (config.hasErrors()) {
                        return config;
                    } else {
                        final HubServerConfig serverConfig = serverConfigBuilder.build();
                        try (final BlackduckRestConnection restConnection = serverConfig.createRestConnection(logger)) {
                            restConnection.connect();
                        } catch (final IntegrationException | IOException e) {
                            if (e.getMessage().toLowerCase().contains("unauthorized")) {
                                config.setApiTokenError("Invalid credential(s) for: " + serverConfig.getHubUrl());
                            } else {
                                config.setTestConnectionError(e.toString());
                            }
                        }
                        return config;
                    }
                }
            });
            if (config.hasErrors()) {
                return Response.ok(config).status(Status.BAD_REQUEST).build();
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

    // This method must be "package protected" to avoid synthetic access
    HubServerConfigBuilder setConfigBuilderFromSerializableConfig(final BlackDuckServerConfigSerializable config, final PluginSettings settings) {
        final HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();
        serverConfigBuilder.setUrl(config.getHubUrl());
        serverConfigBuilder.setTimeout(config.getTimeout());
        serverConfigBuilder.setTrustCert(config.getTrustCert());

        final String apiToken = config.getApiToken();
        if (StringUtils.isNotBlank(apiToken)) {
            if (config.isApiTokenMasked()) {
                serverConfigBuilder.setApiToken(getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN));
            } else {
                serverConfigBuilder.setApiToken(apiToken);
            }
        } else {
            serverConfigBuilder.setUsername(config.getUsername());

            if (StringUtils.isBlank(config.getPassword())) {
                serverConfigBuilder.setPassword(null);
                serverConfigBuilder.setPasswordLength(0);
            } else if (StringUtils.isNotBlank(config.getPassword()) && !config.isPasswordMasked()) {
                serverConfigBuilder.setPassword(config.getPassword());
                serverConfigBuilder.setPasswordLength(0);
            } else {
                serverConfigBuilder.setPassword(getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_PASS));
                serverConfigBuilder.setPasswordLength(NumberUtils.toInt(getValue(settings, BlackDuckConfigKeys.CONFIG_BLACKDUCK_PASS_LENGTH)));
            }
        }
        serverConfigBuilder.setProxyHost(config.getHubProxyHost());
        serverConfigBuilder.setProxyPort(config.getHubProxyPort());
        serverConfigBuilder.setProxyIgnoredHosts(config.getHubNoProxyHosts());
        serverConfigBuilder.setProxyUsername(config.getHubProxyUser());

        if (StringUtils.isBlank(config.getHubProxyPassword())) {
            serverConfigBuilder.setProxyPassword(null);
            serverConfigBuilder.setProxyPasswordLength(0);
        } else if (StringUtils.isNotBlank(config.getHubProxyPassword()) && !config.isProxyPasswordMasked()) {
            // only update the stored password if it is not the masked password used for display
            serverConfigBuilder.setProxyPassword(config.getHubProxyPassword());
            serverConfigBuilder.setProxyPasswordLength(0);
        } else {
            serverConfigBuilder.setProxyPassword(getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS));
            serverConfigBuilder.setProxyPasswordLength(
                    NumberUtils.toInt(getValue(settings, BlackDuckConfigKeys.CONFIG_PROXY_PASS_LENGTH)));
        }
        return serverConfigBuilder;
    }

    // This method must be "package protected" to avoid synthetic access
    void setConfigFromResult(final BlackDuckServerConfigSerializable config, final AbstractValidator validator) {
        final ValidationResults serverConfigResults = validator.assertValid();
        if (serverConfigResults.hasErrors()) {
            if (serverConfigResults.getResultString(HubServerConfigFieldEnum.HUBURL) != null) {
                config.setHubUrlError(serverConfigResults.getResultString(HubServerConfigFieldEnum.HUBURL));
            }
            if (serverConfigResults.getResultString(HubServerConfigFieldEnum.HUBTIMEOUT) != null) {
                config.setTimeoutError(serverConfigResults.getResultString(HubServerConfigFieldEnum.HUBTIMEOUT));
            }
            if (serverConfigResults.getResultString(ApiTokenField.API_TOKEN) != null) {
                config.setApiTokenError(serverConfigResults.getResultString(ApiTokenField.API_TOKEN));
            }
            if (serverConfigResults.getResultString(CredentialsField.USERNAME) != null) {
                config.setUsernameError(serverConfigResults.getResultString(CredentialsField.USERNAME));
            }
            if (serverConfigResults.getResultString(CredentialsField.PASSWORD) != null) {
                config.setPasswordError(serverConfigResults.getResultString(CredentialsField.PASSWORD));
            }
            if (serverConfigResults.getResultString(ProxyInfoField.PROXYHOST) != null) {
                config.setHubProxyHostError(serverConfigResults.getResultString(ProxyInfoField.PROXYHOST));
            }
            if (serverConfigResults.getResultString(ProxyInfoField.NOPROXYHOSTS) != null) {
                config.setHubNoProxyHostsError(serverConfigResults.getResultString(ProxyInfoField.NOPROXYHOSTS));
            }
            if (serverConfigResults.getResultString(ProxyInfoField.PROXYPORT) != null) {
                config.setHubProxyPortError(serverConfigResults.getResultString(ProxyInfoField.PROXYPORT));
            }
            if (serverConfigResults.getResultString(ProxyInfoField.PROXYUSERNAME) != null) {
                config.setHubProxyUserError(serverConfigResults.getResultString(ProxyInfoField.PROXYUSERNAME));
            }
            if (serverConfigResults.getResultString(ProxyInfoField.PROXYPASSWORD) != null) {
                config.setHubProxyPasswordError(serverConfigResults.getResultString(ProxyInfoField.PROXYPASSWORD));
            }
        }
    }

    // This method must be "package protected" to avoid synthetic access
    String getValue(final PluginSettings settings, final String key) {
        return (String) settings.get(key);
    }

    // This method must be "package protected" to avoid synthetic access
    void setValue(final PluginSettings settings, final String key, final Object value) {
        if (value == null) {
            settings.remove(key);
        } else {
            settings.put(key, String.valueOf(value));
        }
    }

}
