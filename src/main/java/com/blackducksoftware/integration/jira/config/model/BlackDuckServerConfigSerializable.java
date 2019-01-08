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
package com.blackducksoftware.integration.jira.config.model;

import java.io.Serializable;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.util.Stringable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BlackDuckServerConfigSerializable extends Stringable implements Serializable {
    private static final long serialVersionUID = -8136935284749275566L;

    @XmlElement
    private String testConnectionError;

    @XmlElement
    private String hubUrl;

    @XmlElement
    private String hubUrlError;

    @XmlElement
    private String timeout;

    @XmlElement
    private String timeoutError;

    @XmlElement
    private String apiToken;

    private int apiTokenLength;

    @XmlElement
    private String apiTokenError;

    @XmlElement
    private String username;

    @XmlElement
    private String usernameError;

    @XmlElement
    private String password;

    @XmlElement
    private String passwordError;

    private int passwordLength;

    @XmlElement
    private Boolean trustCert;

    @XmlElement
    private String trustCertError;

    @XmlElement
    private String hubProxyHost;

    @XmlElement
    private String hubProxyHostError;

    @XmlElement
    private String hubProxyPort;

    @XmlElement
    private String hubProxyPortError;

    @XmlElement
    private String hubNoProxyHosts;

    @XmlElement
    private String hubNoProxyHostsError;

    @XmlElement
    private String hubProxyUser;

    @XmlElement
    private String hubProxyUserError;

    @XmlElement
    private String hubProxyPassword;

    @XmlElement
    private String hubProxyPasswordError;

    private int hubProxyPasswordLength;

    public BlackDuckServerConfigSerializable() {

    }

    public BlackDuckServerConfigSerializable(final BlackDuckServerConfigSerializable blackDuckServerConfigSerializable) {
        this.hubUrl = blackDuckServerConfigSerializable.getHubUrl();
        this.timeout = blackDuckServerConfigSerializable.getTimeout();
        this.apiToken = blackDuckServerConfigSerializable.getApiToken();
        this.apiTokenLength = blackDuckServerConfigSerializable.getApiTokenLength();
        this.username = blackDuckServerConfigSerializable.getUsername();
        this.password = blackDuckServerConfigSerializable.getPassword();
        this.passwordLength = blackDuckServerConfigSerializable.getPasswordLength();
        this.trustCert = Boolean.valueOf(blackDuckServerConfigSerializable.getTrustCert());
        this.hubProxyHost = blackDuckServerConfigSerializable.getHubProxyHost();
        this.hubProxyPort = blackDuckServerConfigSerializable.getHubProxyPort();
        this.hubNoProxyHosts = blackDuckServerConfigSerializable.getHubNoProxyHosts();
        this.hubProxyUser = blackDuckServerConfigSerializable.getHubProxyUser();
        this.hubProxyPassword = blackDuckServerConfigSerializable.getHubProxyPassword();
        this.hubProxyPasswordLength = blackDuckServerConfigSerializable.getHubProxyPasswordLength();
    }

    public boolean hasErrors() {
        boolean hasErrors = false;
        if (StringUtils.isNotBlank(getHubUrlError())) {
            hasErrors = true;
        } else if (StringUtils.isNotBlank(getTimeoutError())) {
            hasErrors = true;
        } else if (StringUtils.isNotBlank(getTrustCertError())) {
            hasErrors = true;
        } else if (StringUtils.isNotBlank(getApiTokenError())) {
            hasErrors = true;
        } else if (StringUtils.isNotBlank(getUsernameError())) {
            hasErrors = true;
        } else if (StringUtils.isNotBlank(getPasswordError())) {
            hasErrors = true;
        } else if (StringUtils.isNotBlank(getTestConnectionError())) {
            hasErrors = true;
        } else if (StringUtils.isNotBlank(getHubProxyHostError())) {
            hasErrors = true;
        } else if (StringUtils.isNotBlank(getHubProxyPortError())) {
            hasErrors = true;
        } else if (StringUtils.isNotBlank(getHubNoProxyHostsError())) {
            hasErrors = true;
        } else if (StringUtils.isNotBlank(getHubProxyUserError())) {
            hasErrors = true;
        } else if (StringUtils.isNotBlank(getHubProxyPasswordError())) {
            hasErrors = true;
        }

        return hasErrors;
    }

    public String getMaskedPassword() {
        return getMaskedString(passwordLength);
    }

    public boolean isPasswordMasked() {
        return isStringMasked(password);
    }

    public String getMaskedProxyPassword() {
        return getMaskedString(hubProxyPasswordLength);
    }

    public boolean isProxyPasswordMasked() {
        return isStringMasked(hubProxyPassword);
    }

    public String getMaskedApiToken() {
        return getMaskedString(apiTokenLength);
    }

    public boolean isApiTokenMasked() {
        return isStringMasked(apiToken);
    }

    public static String getMaskedString(final int length) {
        if (length == 0) {
            return null;
        }
        final char[] array = new char[length];
        Arrays.fill(array, '*');
        return new String(array);
    }

    private boolean isStringMasked(final String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        final String masked = getMaskedString(value.length());
        if (value.equals(masked)) {
            return true;
        }
        return false;
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public void setHubUrl(final String hubUrl) {
        this.hubUrl = hubUrl;
    }

    public String getHubUrlError() {
        return hubUrlError;
    }

    public void setHubUrlError(final String hubUrlError) {
        this.hubUrlError = hubUrlError;
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(final String timeout) {
        this.timeout = timeout;
    }

    public String getTimeoutError() {
        return timeoutError;
    }

    public void setTimeoutError(final String timeoutError) {
        this.timeoutError = timeoutError;
    }

    public String getTrustCert() {
        return trustCert != null ? trustCert.toString() : Boolean.FALSE.toString();
    }

    public void setTrustCert(final String trustCert) {
        if (trustCert != null) {
            setTrustCert(Boolean.parseBoolean(trustCert));
        } else {
            setTrustCert(Boolean.FALSE);
        }
    }

    public void setTrustCert(final Boolean trustCert) {
        this.trustCert = trustCert;
    }

    public String getTrustCertError() {
        return trustCertError;
    }

    public void setTrustCertError(final String trustCertError) {
        this.trustCertError = trustCertError;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(final String apiToken) {
        this.apiToken = apiToken;
    }

    public int getApiTokenLength() {
        return apiTokenLength;
    }

    public void setApiTokenLength(final int apiTokenLength) {
        this.apiTokenLength = apiTokenLength;
    }

    public String getApiTokenError() {
        return apiTokenError;
    }

    public void setApiTokenError(final String apiTokenError) {
        this.apiTokenError = apiTokenError;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getUsernameError() {
        return usernameError;
    }

    public void setUsernameError(final String usernameError) {
        this.usernameError = usernameError;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getPasswordError() {
        return passwordError;
    }

    public void setPasswordError(final String passwordError) {
        this.passwordError = passwordError;
    }

    public int getPasswordLength() {
        return passwordLength;
    }

    public void setPasswordLength(final int passwordLength) {
        this.passwordLength = passwordLength;
    }

    public String getHubProxyHost() {
        return hubProxyHost;
    }

    public void setHubProxyHost(final String hubProxyHost) {
        this.hubProxyHost = hubProxyHost;
    }

    public String getHubProxyHostError() {
        return hubProxyHostError;
    }

    public void setHubProxyHostError(final String hubProxyHostError) {
        this.hubProxyHostError = hubProxyHostError;
    }

    public String getHubProxyPort() {
        return hubProxyPort;
    }

    public void setHubProxyPort(final String hubProxyPort) {
        this.hubProxyPort = hubProxyPort;
    }

    public String getHubProxyPortError() {
        return hubProxyPortError;
    }

    public void setHubProxyPortError(final String hubProxyPortError) {
        this.hubProxyPortError = hubProxyPortError;
    }

    public String getHubNoProxyHosts() {
        return hubNoProxyHosts;
    }

    public void setHubNoProxyHosts(final String hubNoProxyHosts) {
        this.hubNoProxyHosts = hubNoProxyHosts;
    }

    public String getHubNoProxyHostsError() {
        return hubNoProxyHostsError;
    }

    public void setHubNoProxyHostsError(final String hubNoProxyHostsError) {
        this.hubNoProxyHostsError = hubNoProxyHostsError;
    }

    public String getHubProxyUser() {
        return hubProxyUser;
    }

    public void setHubProxyUser(final String hubProxyUser) {
        this.hubProxyUser = hubProxyUser;
    }

    public String getHubProxyUserError() {
        return hubProxyUserError;
    }

    public void setHubProxyUserError(final String hubProxyUserError) {
        this.hubProxyUserError = hubProxyUserError;
    }

    public String getHubProxyPassword() {
        return hubProxyPassword;
    }

    public void setHubProxyPassword(final String hubProxyPassword) {
        this.hubProxyPassword = hubProxyPassword;
    }

    public String getHubProxyPasswordError() {
        return hubProxyPasswordError;
    }

    public void setHubProxyPasswordError(final String hubProxyPasswordError) {
        this.hubProxyPasswordError = hubProxyPasswordError;
    }

    public int getHubProxyPasswordLength() {
        return hubProxyPasswordLength;
    }

    public void setHubProxyPasswordLength(final int hubProxyPasswordLength) {
        this.hubProxyPasswordLength = hubProxyPasswordLength;
    }

    public String getTestConnectionError() {
        return testConnectionError;
    }

    public void setTestConnectionError(final String testConnectionError) {
        this.testConnectionError = testConnectionError;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("HubServerConfigSerializable [testConnectionError=");
        builder.append(testConnectionError);
        builder.append(", hubUrl=");
        builder.append(hubUrl);
        builder.append(", hubUrlError=");
        builder.append(hubUrlError);
        builder.append(", timeout=");
        builder.append(timeout);
        builder.append(", timeoutError=");
        builder.append(timeoutError);
        builder.append(", trustCert=");
        builder.append(trustCert);
        builder.append(", trustCertError=");
        builder.append(trustCertError);
        builder.append(", apiToken=");
        builder.append(apiToken);
        builder.append(", apiTokenError=");
        builder.append(apiTokenError);
        builder.append(", username=");
        builder.append(username);
        builder.append(", usernameError=");
        builder.append(usernameError);
        builder.append(", password=");
        builder.append(password);
        builder.append(", passwordError=");
        builder.append(passwordError);
        builder.append(", passwordLength=");
        builder.append(passwordLength);
        builder.append(", hubProxyHost=");
        builder.append(hubProxyHost);
        builder.append(", hubProxyHostError=");
        builder.append(hubProxyHostError);
        builder.append(", hubProxyPort=");
        builder.append(hubProxyPort);
        builder.append(", hubProxyPortError=");
        builder.append(hubProxyPortError);
        builder.append(", hubNoProxyHosts=");
        builder.append(hubNoProxyHosts);
        builder.append(", hubNoProxyHostsError=");
        builder.append(hubNoProxyHostsError);
        builder.append(", hubProxyUser=");
        builder.append(hubProxyUser);
        builder.append(", hubProxyUserError=");
        builder.append(hubProxyUserError);
        builder.append(", hubProxyPassword=");
        builder.append(hubProxyPassword);
        builder.append(", hubProxyPasswordError=");
        builder.append(hubProxyPasswordError);
        builder.append(", hubProxyPasswordLength=");
        builder.append(hubProxyPasswordLength);
        builder.append("]");
        return builder.toString();
    }

}
