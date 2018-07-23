/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.jira.task.issue.model;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.model.PluginField;

public class PolicyIssueFieldTempate extends BlackDuckIssueFieldTemplate {
    private final String policyRuleName;
    private final String policyRuleUri;
    private final String policyRuleOverridable;
    private final String policyRuleDescription;

    // @formatter:off
    public PolicyIssueFieldTempate(
             final ApplicationUser projectOwner
            ,final String projectName
            ,final String projectVersionName
            ,final String projectVersionUri
            ,final String projectVersionNickname
            ,final String componentName
            ,final String componentUri
            ,final String componentVersionName
            ,final String componentVersionUri
            ,final String licenseString
            ,final String licenseLink
            ,final String usagesString
            ,final String updatedTimeString
            ,final String policyRuleName
            ,final String policyRuleUri
            ,final String policyRuleOverridable
            ,final String policyRuleDescription
            ) {
        super(projectOwner, projectName, projectVersionName, projectVersionUri, projectVersionNickname, componentName, componentUri, componentVersionName, componentVersionUri, licenseString, licenseLink, usagesString, updatedTimeString);
        this.policyRuleName = policyRuleName;
        this.policyRuleUri = policyRuleUri;
        this.policyRuleOverridable = policyRuleOverridable;
        this.policyRuleDescription = policyRuleDescription;
    }
    // @formatter:on

    // @formatter:off
    public static PolicyIssueFieldTempate withComponent(
             final ApplicationUser projectOwner
            ,final String projectName
            ,final String projectVersionName
            ,final String projectVersionUri
            ,final String projectVersionNickname
            ,final String componentName
            ,final String componentUri
            ,final String licenseString
            ,final String licenseLink
            ,final String usagesString
            ,final String updatedTimeString
            ,final String policyRuleName
            ,final String policyRuleUri
            ,final String policyRuleOverridable
            ,final String policyRuleDescription
            ) {
        return new PolicyIssueFieldTempate(projectOwner, projectName, projectVersionName, projectVersionUri, projectVersionNickname, componentName, componentUri, null, null, licenseString, licenseLink, usagesString, updatedTimeString, policyRuleName, policyRuleUri, policyRuleOverridable, policyRuleDescription);
    }
    // @formatter:on

    // @formatter:off
    public static PolicyIssueFieldTempate withComponentVersion(
             final ApplicationUser projectOwner
            ,final String projectName
            ,final String projectVersionName
            ,final String projectVersionUri
            ,final String projectVersionNickname
            ,final String componentName
            ,final String componentVersionName
            ,final String componentVersionUri
            ,final String licenseString
            ,final String licenseLink
            ,final String usagesString
            ,final String updatedTimeString
            ,final String policyRuleName
            ,final String policyRuleUri
            ,final String policyRuleOverridable
            ,final String policyRuleDescription
            ) {
        return new PolicyIssueFieldTempate(projectOwner, projectName, projectVersionName, projectVersionUri, projectVersionNickname, componentName, null, componentVersionName, componentVersionUri, licenseString, licenseLink, usagesString, updatedTimeString, policyRuleName, policyRuleUri, policyRuleOverridable, policyRuleDescription);
    }
    // @formatter:on

    public String getPolicyRuleName() {
        return policyRuleName;
    }

    public String getPolicyRuleUri() {
        return policyRuleUri;
    }

    public String getPolicyRuleOverridable() {
        return policyRuleOverridable;
    }

    @Override
    protected Map<Long, String> getAddtionalHubFieldMappings(final Map<PluginField, CustomField> customFields) {
        final Map<Long, String> policyFieldMappings = new HashMap<>();
        addCustomField(customFields, policyFieldMappings, PluginField.HUB_CUSTOM_FIELD_POLICY_RULE, policyRuleName);
        addCustomField(customFields, policyFieldMappings, PluginField.HUB_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE, policyRuleOverridable);
        addCustomField(customFields, policyFieldMappings, PluginField.HUB_CUSTOM_FIELD_POLICY_RULE_DESCRIPTION, policyRuleDescription);
        // TODO use: <hubBaseUrl> + "/ui/policy-management"
        addCustomField(customFields, policyFieldMappings, PluginField.HUB_CUSTOM_FIELD_POLICY_RULE_URL, policyRuleUri);
        return policyFieldMappings;
    }

}
