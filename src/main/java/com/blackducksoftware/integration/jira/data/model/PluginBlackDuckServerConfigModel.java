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
package com.blackducksoftware.integration.jira.data.model;

import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.util.Stringable;

public class PluginBlackDuckServerConfigModel extends Stringable {
    private final String url;
    private final String apiToken;
    private final Integer timeoutInSeconds;
    private final Boolean trustCert;

    private final String proxyHost;
    private final Integer proxyPort;
    private final String proxyUsername;
    private final String proxyPassword;

    public PluginBlackDuckServerConfigModel(final String url, final String apiToken, final Integer timeoutInSeconds, final Boolean trustCert, final String proxyHost, final Integer proxyPort, final String proxyUsername,
        final String proxyPassword) {
        this.url = url;
        this.apiToken = apiToken;
        this.timeoutInSeconds = timeoutInSeconds;
        this.trustCert = trustCert;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
    }

    public String getUrl() {
        return url;
    }

    public String getApiToken() {
        return apiToken;
    }

    public Optional<Integer> getTimeoutInSeconds() {
        return Optional.ofNullable(timeoutInSeconds);
    }

    public Boolean getTrustCert() {
        return trustCert;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public Optional<Integer> getProxyPort() {
        return Optional.ofNullable(proxyPort);
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public BlackDuckServerConfigBuilder createBlackDuckServerConfigBuilder() {
        final BlackDuckServerConfigBuilder builder = BlackDuckServerConfig.newBuilder();
        setBuilderStringValue(builder::setUrl, url);
        setBuilderStringValue(builder::setApiToken, apiToken);
        if (null != timeoutInSeconds) {
            builder.setTimeoutInSeconds(timeoutInSeconds);
        }
        if (null != trustCert) {
            builder.setTrustCert(trustCert);
        }

        builder.setProxyHost(proxyHost);
        if (null != proxyPort) {
            builder.setProxyPort(proxyPort);
        }

        setBuilderStringValue(builder::setProxyUsername, proxyUsername);
        setBuilderStringValue(builder::setProxyPassword, proxyPassword);
        return builder;
    }

    private void setBuilderStringValue(final Function<String, BlackDuckServerConfigBuilder> builderFunction, final String value) {
        if (StringUtils.isNotBlank(value)) {
            builderFunction.apply(value);
        }
    }

}
