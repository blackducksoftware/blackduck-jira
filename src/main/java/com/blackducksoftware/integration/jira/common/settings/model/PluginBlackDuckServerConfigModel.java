package com.blackducksoftware.integration.jira.common.settings.model;

import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfigBuilder;
import com.synopsys.integration.util.Stringable;

public class PluginBlackDuckServerConfigModel extends Stringable {
    private String url;
    private String apiToken;
    private Integer timeoutInSeconds;
    private Boolean trustCert;

    private String proxyHost;
    private Integer proxyPort;
    private String proxyUsername;
    private String proxyPassword;

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

    public Integer getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    public Boolean getTrustCert() {
        return trustCert;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public BlackDuckServerConfigBuilder createBlackDuckServerConfigBuilder() {
        final BlackDuckServerConfigBuilder builder = BlackDuckServerConfig.newBuilder();
        builder.setUrl(url);
        builder.setApiToken(apiToken);
        builder.setTimeoutInSeconds(timeoutInSeconds);
        builder.setTrustCert(trustCert);
        builder.setProxyHost(proxyHost);
        builder.setProxyPort(proxyPort);
        builder.setProxyUsername(proxyUsername);
        builder.setProxyPassword(proxyPassword);
        return builder;
    }

}
