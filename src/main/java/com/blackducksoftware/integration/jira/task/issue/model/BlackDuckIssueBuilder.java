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
package com.blackducksoftware.integration.jira.task.issue.model;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.BlackDuckDataHelper;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.UrlParser;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.DataFormatHelper;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventCategory;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleViewV2;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.Stringable;

public class BlackDuckIssueBuilder extends Stringable {
    private final BlackDuckDataHelper blackDuckDataHelper;
    private final DataFormatHelper dataFormatHelper;

    private BlackDuckEventAction action;
    private EventCategory eventCategory;
    private Set<ProjectFieldCopyMapping> projectFieldCopyMappings;
    private String bomComponentUri;
    private String componentIssueUrl;
    private Date lastBatchStartDate;

    // Jira Issue Fields
    private Long jiraProjectId;
    private String jiraProjectName;
    private String jiraIssueTypeId;
    private String issueCreatorUsername;
    private String assigneeId;

    // BlackDuckFields
    private ApplicationUser projectOwner;
    private String projectName;
    private String projectVersionName;
    private String projectVersionUri;
    private String projectVersionNickname;

    private String componentName;
    private String componentUri;
    private String componentVersionName;
    private String componentVersionUri;

    private String licenseString;
    private String licenseLink;
    private String originsString;
    private String originIdsString;
    private String usagesString;
    private String updatedTimeString;

    private String policyRuleName;
    private String policyRuleUrl;
    private String policyDescription;
    private Boolean policyOverridable;
    private String policySeverity;

    // Comments
    private String jiraIssueComment;
    private String jiraIssueReOpenComment;
    private String jiraIssueCommentForExistingIssue;
    private String jiraIssueResolveComment;
    private String jiraIssueCommentInLieuOfStateChange;

    public BlackDuckIssueBuilder(final BlackDuckDataHelper blackDuckDataHelper, final DataFormatHelper dataFormatHelper) {
        this.blackDuckDataHelper = blackDuckDataHelper;
        this.dataFormatHelper = dataFormatHelper;
    }

    public void setAction(final BlackDuckEventAction action) {
        this.action = action;
    }

    public void setEventCategory(final EventCategory eventCategory) {
        this.eventCategory = eventCategory;
    }

    public void setProjectFieldCopyMappings(final Set<ProjectFieldCopyMapping> projectFieldCopyMappings) {
        this.projectFieldCopyMappings = projectFieldCopyMappings;
    }

    public void setBomComponentUri(final String bomComponentUri) {
        this.bomComponentUri = bomComponentUri;
    }

    public void setComponentIssueUrl(final String componentIssueUrl) {
        this.componentIssueUrl = componentIssueUrl;
    }

    public void setLastBatchStartDate(final Date lastBatchStartDate) {
        this.lastBatchStartDate = lastBatchStartDate;
    }

    public void setJiraProject(final JiraProject jiraProject) {
        this.jiraProjectId = jiraProject.getProjectId();
        this.jiraProjectName = jiraProject.getProjectName();
        this.assigneeId = jiraProject.getAssigneeUserId();
    }

    public void setJiraIssueTypeId(final String jiraIssueTypeId) {
        this.jiraIssueTypeId = jiraIssueTypeId;
    }

    public void setIssueCreatorUsername(final String issueCreatorUsername) {
        this.issueCreatorUsername = issueCreatorUsername;
    }

    public void setJiraIssueComment(final String jiraIssueComment) {
        this.jiraIssueComment = jiraIssueComment;
    }

    public void setJiraIssueReOpenComment(final String jiraIssueReOpenComment) {
        this.jiraIssueReOpenComment = jiraIssueReOpenComment;
    }

    public void setJiraIssueCommentForExistingIssue(final String jiraIssueCommentForExistingIssue) {
        this.jiraIssueCommentForExistingIssue = jiraIssueCommentForExistingIssue;
    }

    public void setJiraIssueResolveComment(final String jiraIssueResolveComment) {
        this.jiraIssueResolveComment = jiraIssueResolveComment;
    }

    public void setJiraIssueCommentInLieuOfStateChange(final String jiraIssueCommentInLieuOfStateChange) {
        this.jiraIssueCommentInLieuOfStateChange = jiraIssueCommentInLieuOfStateChange;
    }

    public void setAllIssueComments(final String comment) {
        this.jiraIssueComment = comment;
        this.jiraIssueReOpenComment = comment;
        this.jiraIssueResolveComment = comment;
        this.jiraIssueCommentForExistingIssue = comment;
        this.jiraIssueCommentInLieuOfStateChange = comment;
    }

    public BlackDuckIssueBuilder setVulnerabilityComments(final String comment) {
        setJiraIssueComment(comment);
        setJiraIssueCommentForExistingIssue(comment);
        setJiraIssueReOpenComment(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_REOPEN);
        setJiraIssueResolveComment(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_RESOLVE);
        setJiraIssueCommentInLieuOfStateChange(comment);
        return this;
    }

    public BlackDuckIssueBuilder setPolicyComments(final NotificationType notificationType) {
        if (NotificationType.POLICY_OVERRIDE.equals(notificationType)) {
            setJiraIssueReOpenComment(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_REOPEN);
            setJiraIssueCommentForExistingIssue(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_OVERRIDDEN_COMMENT);
            setJiraIssueResolveComment(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_RESOLVE);
            setJiraIssueCommentInLieuOfStateChange(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_OVERRIDDEN_COMMENT);
        } else if (NotificationType.RULE_VIOLATION.equals(notificationType)) {
            setJiraIssueReOpenComment(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_REOPEN);
            setJiraIssueCommentForExistingIssue(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_DETECTED_AGAIN_COMMENT);
            setJiraIssueResolveComment(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_RESOLVE);
            setJiraIssueCommentInLieuOfStateChange(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_DETECTED_AGAIN_COMMENT);
        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(notificationType)) {
            setJiraIssueReOpenComment(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_REOPEN);
            setJiraIssueCommentForExistingIssue(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_CLEARED_COMMENT);
            setJiraIssueResolveComment(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_CLEARED_RESOLVE);
            setJiraIssueCommentInLieuOfStateChange(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_CLEARED_COMMENT);
        } else if (NotificationType.BOM_EDIT.equals(notificationType)) {
            final String noComment = "";
            setJiraIssueReOpenComment(noComment);
            setJiraIssueCommentForExistingIssue(noComment);
            setJiraIssueResolveComment(noComment);
            setJiraIssueCommentInLieuOfStateChange(noComment);
        }
        return this;
    }

    // TODO SECTION

    public BlackDuckIssueBuilder setBlackDuckFields(final ApplicationUser projectOwner, final ProjectVersionWrapper projectVersionWrapper, final VersionBomComponentView versionBomComponent) {
        final ProjectView project = projectVersionWrapper.getProjectView();
        final ProjectVersionView projectVersion = projectVersionWrapper.getProjectVersionView();

        this.projectOwner = projectOwner;
        this.projectName = project.name;
        this.projectVersionName = projectVersion.versionName;
        this.projectVersionUri = blackDuckDataHelper.getHrefNullable(projectVersion);
        this.projectVersionNickname = projectVersion.nickname;

        this.componentName = versionBomComponent.componentName;
        this.componentUri = versionBomComponent.component;
        this.componentVersionName = versionBomComponent.componentVersionName;
        this.componentVersionUri = versionBomComponent.componentVersion;

        this.originsString = createCommaSeparatedString(versionBomComponent.origins, origin -> origin.name);
        this.originIdsString = createCommaSeparatedString(versionBomComponent.origins, origin -> origin.externalId);
        this.licenseString = dataFormatHelper.getComponentLicensesStringPlainText(versionBomComponent.licenses);
        this.licenseLink = dataFormatHelper.getLicenseTextLink(versionBomComponent.licenses, this.licenseString);
        this.usagesString = createCommaSeparatedString(versionBomComponent.usages, usage -> usage.prettyPrint());
        this.updatedTimeString = dataFormatHelper.getBomLastUpdated(projectVersion);

        return this;
    }

    public BlackDuckIssueBuilder setPolicyFields(final PolicyRuleViewV2 policyRule) {
        this.policyRuleUrl = blackDuckDataHelper.getHrefNullable(policyRule);
        this.policyRuleName = policyRule.name;
        this.policyDescription = policyRule.description;
        this.policyOverridable = policyRule.overridable;
        this.policySeverity = policyRule.severity;
        return this;
    }

    // TODO throw exception if missing required fields
    public BlackDuckIssueModel build() throws IntegrationException {
        if (action == null) {
            // TODO throw exception
        }
        if (bomComponentUri == null) {
            // TODO throw exception
        }

        final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate;
        if (policyRuleUrl != null) {
            blackDuckIssueFieldTemplate = new PolicyIssueFieldTempate(projectOwner, projectName, projectVersionName, projectVersionUri, projectVersionNickname, componentName, componentUri, componentVersionName, componentVersionUri,
                licenseString, licenseLink, usagesString, updatedTimeString, policyRuleName, policyRuleUrl, policyOverridable.toString(), policyDescription, policySeverity);
        } else {
            blackDuckIssueFieldTemplate = new VulnerabilityIssueFieldTemplate(projectOwner, projectName, projectVersionName, projectVersionUri, projectVersionNickname, componentName, componentVersionName, componentVersionUri, originsString,
                originIdsString, licenseString, licenseLink, usagesString, updatedTimeString);
        }

        final String jiraIssueSummary = dataFormatHelper.createIssueSummary(eventCategory, projectName, projectVersionName, componentName, componentVersionName, policyRuleName);
        final String issueDescription = dataFormatHelper.getIssueDescription(eventCategory, projectVersionUri, componentVersionUri);
        final JiraIssueFieldTemplate jiraIssueFieldTemplate = new JiraIssueFieldTemplate(jiraProjectId, jiraProjectName, jiraIssueTypeId, jiraIssueSummary, issueCreatorUsername, issueDescription, assigneeId);

        final BlackDuckIssueModel wrapper = new BlackDuckIssueModel(action, jiraIssueFieldTemplate, blackDuckIssueFieldTemplate, projectFieldCopyMappings, bomComponentUri, componentIssueUrl, lastBatchStartDate);
        addComments(wrapper);
        wrapper.setEventKey(generateEventKey());
        return wrapper;
    }

    // FIXME make sure all of these fields are correctly updated
    public BlackDuckIssueBuilder copy() {
        final BlackDuckIssueBuilder newBuilder = new BlackDuckIssueBuilder(blackDuckDataHelper, dataFormatHelper);
        newBuilder.action = action;
        newBuilder.projectFieldCopyMappings = projectFieldCopyMappings;
        newBuilder.bomComponentUri = bomComponentUri;
        newBuilder.componentIssueUrl = componentIssueUrl;
        newBuilder.lastBatchStartDate = lastBatchStartDate;

        newBuilder.jiraProjectId = jiraProjectId;
        newBuilder.jiraProjectName = jiraProjectName;
        newBuilder.jiraIssueTypeId = jiraIssueTypeId;
        newBuilder.issueCreatorUsername = issueCreatorUsername;
        newBuilder.assigneeId = assigneeId;

        newBuilder.projectOwner = projectOwner;
        newBuilder.projectName = projectName;
        newBuilder.projectVersionName = projectVersionName;
        newBuilder.projectVersionUri = projectVersionUri;
        newBuilder.projectVersionNickname = projectVersionNickname;

        newBuilder.componentName = componentName;
        newBuilder.componentUri = componentUri;
        newBuilder.componentVersionName = componentVersionName;
        newBuilder.componentVersionUri = componentVersionUri;

        newBuilder.licenseString = licenseString;
        newBuilder.licenseLink = licenseLink;
        newBuilder.originsString = originsString;
        newBuilder.originIdsString = originIdsString;
        newBuilder.usagesString = usagesString;
        newBuilder.updatedTimeString = updatedTimeString;

        newBuilder.policyRuleName = policyRuleName;
        newBuilder.policyRuleUrl = policyRuleUrl;
        newBuilder.policyDescription = policyDescription;
        newBuilder.policyOverridable = policyOverridable;
        newBuilder.policySeverity = policySeverity;

        newBuilder.jiraIssueComment = jiraIssueComment;
        newBuilder.jiraIssueReOpenComment = jiraIssueReOpenComment;
        newBuilder.jiraIssueCommentForExistingIssue = jiraIssueCommentForExistingIssue;
        newBuilder.jiraIssueResolveComment = jiraIssueResolveComment;
        newBuilder.jiraIssueCommentInLieuOfStateChange = jiraIssueCommentInLieuOfStateChange;

        return newBuilder;
    }

    private void addComments(final BlackDuckIssueModel wrapper) {
        wrapper.setJiraIssueComment(jiraIssueComment);
        wrapper.setJiraIssueCommentForExistingIssue(jiraIssueCommentForExistingIssue);
        wrapper.setJiraIssueCommentInLieuOfStateChange(jiraIssueCommentInLieuOfStateChange);
        wrapper.setJiraIssueReOpenComment(jiraIssueReOpenComment);
        wrapper.setJiraIssueResolveComment(jiraIssueResolveComment);
    }

    // This must remain consistent among non-major versions
    private String generateEventKey() throws IntegrationException {
        final String blackDuckProjectVersionUrl = projectVersionUri;
        final String blackDuckComponentVersionUrl = componentVersionUri;
        final String blackDuckComponentUrl = componentUri;
        final StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_NAME);
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        if (EventCategory.POLICY.equals(eventCategory)) {
            keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_VALUE_POLICY);
        } else {
            keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_ISSUE_TYPE_VALUE_VULNERABILITY);
        }
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_JIRA_PROJECT_ID_NAME);
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(jiraProjectId);
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_BLACKDUCK_PROJECT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(UrlParser.getRelativeUrl(blackDuckProjectVersionUrl)));
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_BLACKDUCK_COMPONENT_REL_URL_HASHED_NAME);
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        if (blackDuckComponentVersionUrl == null && EventCategory.POLICY.equals(eventCategory)) {
            keyBuilder.append(hashString(UrlParser.getRelativeUrl(blackDuckComponentUrl)));
        } else {
            // Vulnerabilities do not have a component URL
        }
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);

        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_BLACKDUCK_COMPONENT_VERSION_REL_URL_HASHED_NAME);
        keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
        keyBuilder.append(hashString(UrlParser.getRelativeUrl(blackDuckComponentVersionUrl)));

        if (EventCategory.POLICY.equals(eventCategory)) {
            if (policyRuleUrl == null) {
                throw new HubIntegrationException("Policy Rule URL is null");
            }
            keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR);
            keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_BLACKDUCK_POLICY_RULE_REL_URL_HASHED_NAME);
            keyBuilder.append(BlackDuckJiraConstants.ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR);
            keyBuilder.append(hashString(UrlParser.getRelativeUrl(policyRuleUrl)));
        }

        final String key = keyBuilder.toString();
        return key;
    }

    public final String hashString(final String origString) {
        String hashString;
        if (origString == null) {
            hashString = "";
        } else {
            hashString = String.valueOf(origString.hashCode());
        }
        return hashString;
    }

    private <T> String createCommaSeparatedString(final List<T> list, final Function<T, String> reductionFunction) {
        if (list != null && !list.isEmpty()) {
            return list.stream().map(reductionFunction).collect(Collectors.joining(", "));
        }
        return null;
    }
}
