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
package com.blackducksoftware.integration.jira.task.issue.model;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.synopsys.integration.util.Stringable;

public class BlackDuckIssueFieldTemplate extends Stringable {
    private final ApplicationUser projectOwner;
    private final String projectName;
    private final String projectVersionName;
    private final String projectVersionUri;
    private final String projectVersionNickname;

    private final ApplicationUser componentReviewer;
    private final String componentName;
    private final String componentUri;
    private final String componentVersionName;
    private final String componentVersionUri;

    private final String licenseString;
    private final String licenseLink;
    private final String usagesString;
    private final String updatedTimeString;

    private final IssueCategory issueCategory;

    // Vulnerability
    private String componentVersionOriginName;
    private String componentVersionOriginId;

    // Policy
    private String policyRuleName;
    private String policyRuleUri;
    private String policyRuleOverridable;
    private String policyRuleDescription;
    private String policyRuleSeverity;

    // @formatter:off
    public BlackDuckIssueFieldTemplate(
             final ApplicationUser projectOwner
            ,final String projectName
            ,final String projectVersionName
            ,final String projectVersionUri
            ,final String projectVersionNickname
            ,final ApplicationUser componentReviewer
            ,final String componentName
            ,final String componentUri
            ,final String componentVersionName
            ,final String componentVersionUri
            ,final String licenseString
            ,final String licenseLink
            ,final String usagesString
            ,final String updatedTimeString
            ,final IssueCategory issueCategory
            ) {
        this.projectOwner = projectOwner;
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.projectVersionUri = projectVersionUri;
        this.projectVersionNickname = projectVersionNickname;
        this.componentReviewer = componentReviewer;
        this.componentName = componentName;
        this.componentUri = componentUri;
        this.componentVersionName = componentVersionName;
        this.componentVersionUri = componentVersionUri;
        this.licenseString = licenseString;
        this.licenseLink = licenseLink;
        this.usagesString = usagesString;
        this.updatedTimeString = updatedTimeString;
        this.issueCategory = issueCategory;
    }
    // @formatter:on

    // @formatter:off
    public static BlackDuckIssueFieldTemplate createPolicyIssueFieldTemplate(
             final ApplicationUser projectOwner
            ,final String projectName
            ,final String projectVersionName
            ,final String projectVersionUri
            ,final String projectVersionNickname
            ,final ApplicationUser componentReviewer
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
            ,final String policyRuleSeverity
            ) {
        final BlackDuckIssueFieldTemplate newTemplate = new BlackDuckIssueFieldTemplate(projectOwner, projectName, projectVersionName, projectVersionUri, projectVersionNickname, componentReviewer, componentName, componentUri, componentVersionName, componentVersionUri, licenseString, licenseLink, usagesString, updatedTimeString, IssueCategory.POLICY);
        newTemplate.policyRuleName = policyRuleName;
        newTemplate.policyRuleUri = policyRuleUri;
        newTemplate.policyRuleOverridable = policyRuleOverridable;
        newTemplate.policyRuleDescription = policyRuleDescription;
        newTemplate.policyRuleSeverity = policyRuleSeverity;
        return newTemplate;
    }
    // @formatter:on

    // @formatter:off
    public static BlackDuckIssueFieldTemplate createVulnerabilityIssueFieldTemplate(
             final ApplicationUser projectOwner
            ,final String projectName
            ,final String projectVersionName
            ,final String projectVersionUri
            ,final String projectVersionNickname
            ,final ApplicationUser componentReviewer
            ,final String componentName
            ,final String componentVersionName
            ,final String componentVersionUri
            ,final String licenseString
            ,final String licenseLink
            ,final String usagesString
            ,final String updatedTimeString
            ,final String componentVersionOriginName
            ,final String componentVersionOriginId
            ) {
        final BlackDuckIssueFieldTemplate newTemplate = new BlackDuckIssueFieldTemplate(projectOwner, projectName, projectVersionName, projectVersionUri, projectVersionNickname, componentReviewer, componentName, null, componentVersionName, componentVersionUri, licenseString, licenseLink, usagesString, updatedTimeString, IssueCategory.VULNERABILITY);
        newTemplate.componentVersionOriginName = componentVersionOriginName;
        newTemplate.componentVersionOriginId = componentVersionOriginId;
        return newTemplate;
    }
    // @formatter:on

    public ApplicationUser getProjectOwner() {
        return projectOwner;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public String getProjectVersionUri() {
        return projectVersionUri;
    }

    public String getProjectVersionNickname() {
        return projectVersionNickname;
    }

    public ApplicationUser getComponentReviewer() {
        return componentReviewer;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentUri() {
        return componentUri;
    }

    public String getComponentVersionName() {
        return componentVersionName;
    }

    public String getComponentVersionUri() {
        return componentVersionUri;
    }

    public String getLicenseString() {
        return licenseString;
    }

    public String getLicenseLink() {
        return licenseLink;
    }

    public String getUsagesString() {
        return usagesString;
    }

    public String getUpdatedTimeString() {
        return updatedTimeString;
    }

    public String getComponentVersionOriginName() {
        return componentVersionOriginName;
    }

    public String getComponentVersionOriginId() {
        return componentVersionOriginId;
    }

    public String getPolicyRuleName() {
        return policyRuleName;
    }

    public String getPolicyRuleUri() {
        return policyRuleUri;
    }

    public String getPolicyRuleOverridable() {
        return policyRuleOverridable;
    }

    public String getPolicyRuleDescription() {
        return policyRuleDescription;
    }

    public String getPolicyRuleSeverity() {
        return policyRuleSeverity;
    }

    public IssueCategory getIssueCategory() {
        return issueCategory;
    }

    public final Map<Long, String> createBlackDuckFieldMappings(final Map<PluginField, CustomField> customFields) {
        final Map<Long, String> blackDuckFieldMappings = new HashMap<>();
        if (null != projectOwner) {
            addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_OWNER, projectOwner.getUsername());
        }
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT, projectName);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION, projectVersionName);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_URL, projectVersionUri);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME, projectVersionNickname);

        if (null != componentReviewer) {
            addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_REVIEWER, componentReviewer.getUsername());
        }
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT, componentName);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_URL, componentUri);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION, componentVersionName);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION_URL, componentVersionUri);

        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_LICENSE_NAMES, licenseString);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_LICENSE_URL, licenseLink);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_USAGE, usagesString);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED, updatedTimeString);

        // Vulnerability
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN, componentVersionOriginName);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID, componentVersionOriginId);

        // Policy
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE, policyRuleName);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE, policyRuleOverridable);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_DESCRIPTION, policyRuleDescription);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_SEVERITY, policyRuleSeverity);
        // TODO use this when Black Duck supports policy redirect:
        // addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_URL, policyRuleUri);
        addCustomField(customFields, blackDuckFieldMappings, PluginField.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_URL, extractBlackDuckBaseUrl() + "/ui/policy-management");

        return blackDuckFieldMappings;
    }

    // TODO remove this once policy redirect is supported in the Black Duck UI
    private String extractBlackDuckBaseUrl() {
        final String projectVersionUriString = getProjectVersionUri();
        if (StringUtils.isNotBlank(projectVersionUriString)) {
            final String searchString = "/api";
            final int end = projectVersionUriString.indexOf(searchString);
            if (end > 0) {
                // No need to subtract 1 since we included the slash in the search String
                return projectVersionUriString.substring(0, end);
            }
        }
        return "";
    }

    private void addCustomField(final Map<PluginField, CustomField> customFields, final Map<Long, String> blackDuckFieldMappings, final PluginField pluginField, final String fieldValue) {
        final CustomField customField = customFields.get(pluginField);
        if (customField != null) {
            blackDuckFieldMappings.put(customField.getIdAsLong(), fieldValue);
        }
    }
}
