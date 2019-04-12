package com.blackducksoftware.integration.jira.common.settings;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.jira.common.settings.model.GeneralIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginBlackDuckServerConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginGroupsConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.PluginIssueFieldConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.ProjectMappingConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.TicketCriteriaConfigModel;

public class GlobalConfigurationAccessor {
    public static final String BLACK_DUCK_GROUPS_LIST_DELIMETER = ",";

    private JiraSettingsAccessor pluginSettingsAccessor;

    public GlobalConfigurationAccessor(final JiraSettingsAccessor pluginSettingsAccessor) {
        this.pluginSettingsAccessor = pluginSettingsAccessor;
    }

    public PluginGroupsConfigModel getGroupsConfig() {
        final String blackDuckConfigGroupsString = pluginSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_GROUPS);
        if (StringUtils.isNotBlank(blackDuckConfigGroupsString)) {
            final String[] groups = blackDuckConfigGroupsString.split(BLACK_DUCK_GROUPS_LIST_DELIMETER);
            return PluginGroupsConfigModel.of(groups);
        }
        return PluginGroupsConfigModel.none();
    }

    public void setGroupsConfig(final PluginGroupsConfigModel groupsModel) {
        final Collection<String> groups = groupsModel.getGroups();
        final String groupsString = StringUtils.join(groups, BLACK_DUCK_GROUPS_LIST_DELIMETER);
        pluginSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_GROUPS, groupsString);
    }

    public PluginBlackDuckServerConfigModel getBlackDuckServerConfig() {
        final String url = pluginSettingsAccessor.getStringValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_URL);
        final String apiToken = pluginSettingsAccessor.getStringValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN);
        final Integer timeout = pluginSettingsAccessor.getIntegerValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_TIMEOUT).orElse(300);
        final Boolean trustCert = pluginSettingsAccessor.getBooleanValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_TRUST_CERT);

        final String proxyHost = pluginSettingsAccessor.getStringValue(BlackDuckConfigKeys.CONFIG_PROXY_HOST);
        final Integer proxyPort = pluginSettingsAccessor.getIntegerValue(BlackDuckConfigKeys.CONFIG_PROXY_PORT).orElse(0);
        final String poxyUser = pluginSettingsAccessor.getStringValue(BlackDuckConfigKeys.CONFIG_PROXY_USER);
        final String proxyPassword = getBlackDuckProxyPassword();

        return new PluginBlackDuckServerConfigModel(url, apiToken, timeout, trustCert, proxyHost, proxyPort, poxyUser, proxyPassword);
    }

    public void setBlackDuckServerConfig(final PluginBlackDuckServerConfigModel blackDuckServerConfigModel) {
        pluginSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_URL, blackDuckServerConfigModel.getUrl());
        pluginSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_API_TOKEN, blackDuckServerConfigModel.getApiToken());
        pluginSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_TIMEOUT, blackDuckServerConfigModel.getTimeoutInSeconds());
        pluginSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_BLACKDUCK_TRUST_CERT, blackDuckServerConfigModel.getTrustCert());

        pluginSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_PROXY_HOST, blackDuckServerConfigModel.getProxyHost());
        pluginSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_PROXY_PORT, blackDuckServerConfigModel.getProxyPort());
        pluginSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_PROXY_USER, blackDuckServerConfigModel.getProxyUsername());
        setBlackDuckProxyPassword(blackDuckServerConfigModel.getProxyPassword());
    }

    public PluginIssueCreationConfigModel getIssueCreationConfig() {
        final Integer interval = pluginSettingsAccessor.getIntegerValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS).orElse(10);
        final String defaultIssueCreator = pluginSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ISSUE_CREATOR_USER);
        final GeneralIssueCreationConfigModel general = new GeneralIssueCreationConfigModel(interval, defaultIssueCreator);

        final String mappingsJson = pluginSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON);
        final ProjectMappingConfigModel projectMapping = new ProjectMappingConfigModel(mappingsJson);

        final String policyRulesJson = pluginSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_POLICY_RULES_JSON);
        final Boolean commentOnIssueUpdates = pluginSettingsAccessor.getBooleanValue(PluginConfigKeys.BLACKDUCK_CONFIG_COMMENT_ON_ISSUE_UPDATES_CHOICE, false);
        final Boolean addComponentReviewerToTickets = pluginSettingsAccessor.getBooleanValue(PluginConfigKeys.BLACKDUCK_CONFIG_PROJECT_REVIEWER_NOTIFICATIONS_CHOICE, false);
        final TicketCriteriaConfigModel ticketCriteriaConfigModel = new TicketCriteriaConfigModel(policyRulesJson, commentOnIssueUpdates, addComponentReviewerToTickets);

        return new PluginIssueCreationConfigModel(general, projectMapping, ticketCriteriaConfigModel);
    }

    public void setIssueCreationConfig(final PluginIssueCreationConfigModel pluginIssueCreationConfigModel) {
        final GeneralIssueCreationConfigModel general = pluginIssueCreationConfigModel.getGeneral();
        pluginSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS, general.getInterval());
        pluginSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_ISSUE_CREATOR_USER, general.getDefaultIssueCreator());

        final ProjectMappingConfigModel projectMapping = pluginIssueCreationConfigModel.getProjectMapping();
        pluginSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON, projectMapping.getMappingsJson());

        final TicketCriteriaConfigModel ticketCriteriaConfigModel = pluginIssueCreationConfigModel.getTicketCriteria();
        pluginSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_POLICY_RULES_JSON, ticketCriteriaConfigModel.getPolicyRulesJson());
        pluginSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_COMMENT_ON_ISSUE_UPDATES_CHOICE, ticketCriteriaConfigModel.getCommentOnIssueUpdates());
        pluginSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_PROJECT_REVIEWER_NOTIFICATIONS_CHOICE, ticketCriteriaConfigModel.getAddComponentReviewerToTickets());
    }

    public PluginIssueFieldConfigModel getFieldMappingConfig() {
        final String fieldMappings = pluginSettingsAccessor.getStringValue(PluginConfigKeys.BLACKDUCK_CONFIG_FIELD_COPY_MAPPINGS_JSON);
        return new PluginIssueFieldConfigModel(fieldMappings);
    }

    public void setFieldMappingConfig(final PluginIssueFieldConfigModel fieldMappingConfig) {
        pluginSettingsAccessor.setValue(PluginConfigKeys.BLACKDUCK_CONFIG_FIELD_COPY_MAPPINGS_JSON, fieldMappingConfig.getFieldMappingJson());
    }

    private String getBlackDuckProxyPassword() {
        final String stringValue = pluginSettingsAccessor.getStringValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS);
        if (StringUtils.isBlank(stringValue)) {
            return stringValue;
        }
        final Base64.Decoder decoder = Base64.getDecoder();
        final byte[] decode = decoder.decode(stringValue);
        return new String(decode, StandardCharsets.UTF_8);
    }

    private void setBlackDuckProxyPassword(final String password) {
        if (StringUtils.isBlank(password)) {
            pluginSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS, null);
            return;
        }
        final Base64.Encoder encoder = Base64.getEncoder();
        final String encodedPassword = encoder.encodeToString(password.getBytes());
        pluginSettingsAccessor.setValue(BlackDuckConfigKeys.CONFIG_PROXY_PASS, encodedPassword);
    }

}
