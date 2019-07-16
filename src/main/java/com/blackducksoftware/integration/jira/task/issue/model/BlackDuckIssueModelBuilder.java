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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.blackduck.BlackDuckDataHelper;
import com.blackducksoftware.integration.jira.common.exception.IssueModelBuilderException;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckIssueAction;
import com.blackducksoftware.integration.jira.task.issue.handler.DataFormatHelper;
import com.synopsys.integration.blackduck.api.generated.component.VersionBomOriginView;
import com.synopsys.integration.blackduck.api.generated.enumeration.MatchedFileUsagesType;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.Stringable;

public class BlackDuckIssueModelBuilder extends Stringable implements Cloneable {
    private final BlackDuckDataHelper blackDuckDataHelper;
    private final DataFormatHelper dataFormatHelper;

    private BlackDuckIssueAction action;
    private IssueCategory issueCategory;
    private Set<ProjectFieldCopyMapping> projectFieldCopyMappings;
    private String bomComponentUri;
    private String componentIssueUrl;
    private Date lastBatchStartDate;

    // Jira Issue Fields
    private Long jiraProjectId;
    private String jiraProjectName;
    private String jiraIssueTypeId;
    private ApplicationUser issueCreator;
    private String assigneeId;

    // BlackDuckFields
    private ApplicationUser projectOwner;
    private ApplicationUser componentReviewer;
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
    private boolean includeRemediationInfo = false;

    // Comments
    private String jiraIssueComment;
    private String jiraIssueReOpenComment;
    private String jiraIssueCommentForExistingIssue;
    private String jiraIssueResolveComment;
    private String jiraIssueCommentInLieuOfStateChange;

    public BlackDuckIssueModelBuilder(final BlackDuckDataHelper blackDuckDataHelper, final DataFormatHelper dataFormatHelper) {
        this.blackDuckDataHelper = blackDuckDataHelper;
        this.dataFormatHelper = dataFormatHelper;
    }

    public BlackDuckIssueModelBuilder setAction(final BlackDuckIssueAction action) {
        this.action = action;
        return this;
    }

    public BlackDuckIssueModelBuilder setIssueCategory(final IssueCategory issueCategory) {
        this.issueCategory = issueCategory;
        return this;
    }

    public BlackDuckIssueModelBuilder setProjectFieldCopyMappings(final Set<ProjectFieldCopyMapping> projectFieldCopyMappings) {
        this.projectFieldCopyMappings = projectFieldCopyMappings;
        return this;
    }

    public BlackDuckIssueModelBuilder setBomComponentUri(final String bomComponentUri) {
        this.bomComponentUri = bomComponentUri;
        return this;
    }

    public BlackDuckIssueModelBuilder setLastBatchStartDate(final Date lastBatchStartDate) {
        this.lastBatchStartDate = lastBatchStartDate;
        return this;
    }

    public BlackDuckIssueModelBuilder setJiraProject(final JiraProject jiraProject) {
        this.jiraProjectId = jiraProject.getProjectId();
        this.jiraProjectName = jiraProject.getProjectName();
        this.assigneeId = jiraProject.getAssigneeUserId();
        return this;
    }

    public BlackDuckIssueModelBuilder setJiraIssueTypeId(final String jiraIssueTypeId) {
        this.jiraIssueTypeId = jiraIssueTypeId;
        return this;
    }

    public BlackDuckIssueModelBuilder setIssueCreator(final ApplicationUser issueCreator) {
        this.issueCreator = issueCreator;
        return this;
    }

    public BlackDuckIssueModelBuilder setJiraIssueComment(final String jiraIssueComment) {
        this.jiraIssueComment = jiraIssueComment;
        return this;
    }

    public BlackDuckIssueModelBuilder setJiraIssueReOpenComment(final String jiraIssueReOpenComment) {
        this.jiraIssueReOpenComment = jiraIssueReOpenComment;
        return this;
    }

    public BlackDuckIssueModelBuilder setJiraIssueCommentForExistingIssue(final String jiraIssueCommentForExistingIssue) {
        this.jiraIssueCommentForExistingIssue = jiraIssueCommentForExistingIssue;
        return this;
    }

    public BlackDuckIssueModelBuilder setJiraIssueResolveComment(final String jiraIssueResolveComment) {
        this.jiraIssueResolveComment = jiraIssueResolveComment;
        return this;
    }

    public BlackDuckIssueModelBuilder setJiraIssueCommentInLieuOfStateChange(final String jiraIssueCommentInLieuOfStateChange) {
        this.jiraIssueCommentInLieuOfStateChange = jiraIssueCommentInLieuOfStateChange;
        return this;
    }

    public BlackDuckIssueModelBuilder setAllIssueComments(final String comment) {
        this.jiraIssueComment = comment;
        this.jiraIssueReOpenComment = comment;
        this.jiraIssueResolveComment = comment;
        this.jiraIssueCommentForExistingIssue = comment;
        this.jiraIssueCommentInLieuOfStateChange = comment;
        return this;
    }

    public BlackDuckIssueModelBuilder setVulnerabilityComments(final String comment) {
        setJiraIssueComment(comment);
        setJiraIssueCommentForExistingIssue(comment);
        setJiraIssueReOpenComment(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_REOPEN);
        setJiraIssueResolveComment(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_RESOLVE);
        setJiraIssueCommentInLieuOfStateChange(comment);
        return this;
    }

    public BlackDuckIssueModelBuilder setPolicyComments(final NotificationType notificationType) {
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

    public BlackDuckIssueModelBuilder setBlackDuckFields(final ApplicationUser projectOwner, final ApplicationUser componentReviewer, final ProjectVersionWrapper projectVersionWrapper, final VersionBomComponentView versionBomComponent) {
        final ProjectView project = projectVersionWrapper.getProjectView();
        final ProjectVersionView projectVersion = projectVersionWrapper.getProjectVersionView();

        this.projectOwner = projectOwner;
        this.componentReviewer = componentReviewer;
        this.projectName = project.getName();
        this.projectVersionName = projectVersion.getVersionName();
        this.projectVersionUri = blackDuckDataHelper.getHrefNullable(projectVersion);
        this.projectVersionNickname = projectVersion.getNickname();

        this.componentName = versionBomComponent.getComponentName();
        this.componentUri = versionBomComponent.getComponent();
        this.componentVersionName = versionBomComponent.getComponentVersionName();
        this.componentVersionUri = versionBomComponent.getComponentVersion();

        this.originsString = createCommaSeparatedString(versionBomComponent.getOrigins(), VersionBomOriginView::getName);
        this.originIdsString = createCommaSeparatedString(versionBomComponent.getOrigins(), VersionBomOriginView::getExternalId);
        this.licenseString = dataFormatHelper.getComponentLicensesStringPlainText(versionBomComponent.getLicenses());
        this.licenseLink = dataFormatHelper.getLicenseTextLink(versionBomComponent.getLicenses(), this.licenseString);
        this.usagesString = createCommaSeparatedString(versionBomComponent.getUsages(), MatchedFileUsagesType::prettyPrint);
        this.updatedTimeString = dataFormatHelper.getBomLastUpdated(projectVersion);

        this.bomComponentUri = blackDuckDataHelper.getHrefNullable(versionBomComponent);
        this.componentIssueUrl = blackDuckDataHelper.getFirstLinkSafely(versionBomComponent, VersionBomComponentView.COMPONENT_ISSUES_LINK);

        return this;
    }

    public BlackDuckIssueModelBuilder setPolicyFields(final PolicyRuleView policyRule) {
        this.policyRuleUrl = blackDuckDataHelper.getHrefNullable(policyRule);
        this.policyRuleName = policyRule.getName();
        this.policyDescription = StringUtils.defaultString(policyRule.getDescription(), "No description");
        this.policyOverridable = policyRule.getOverridable();
        this.policySeverity = StringUtils.capitalize(StringUtils.lowerCase(policyRule.getSeverity()));
        return this;
    }

    public BlackDuckIssueModelBuilder includeRemediationInfo(final boolean includeRemediationInfo) {
        this.includeRemediationInfo = includeRemediationInfo;
        return this;
    }

    // TODO throw exception if missing required fields
    public BlackDuckIssueModel build() throws IntegrationException {
        if (action == null) {
            throw new IssueModelBuilderException("The required field defining a BlackDuckIssueAction is missing.");
        }
        if (bomComponentUri == null) {
            throw new IssueModelBuilderException("The required field 'bomComponentUri' is missing.");
        }

        final BlackDuckIssueFieldTemplate blackDuckIssueFieldTemplate;
        final Optional<String> projectVersionComponentQueryLink = dataFormatHelper.createUrlWithComponentNameQuery(this.projectVersionUri, this.componentName);
        final String componentUiLink = projectVersionComponentQueryLink.orElse(componentUri);
        final String componentVersionUiLink = projectVersionComponentQueryLink.orElse(componentVersionUri);

        if (IssueCategory.POLICY.equals(issueCategory) || IssueCategory.SECURITY_POLICY.equals(issueCategory)) {
            if (policyRuleUrl != null) {
                blackDuckIssueFieldTemplate = BlackDuckIssueFieldTemplate.createPolicyIssueFieldTemplate(
                    projectOwner, projectName, projectVersionName, projectVersionUri, projectVersionNickname, componentReviewer, componentName, componentUiLink, componentVersionName, componentVersionUiLink, licenseString, licenseLink,
                    usagesString, updatedTimeString, policyRuleName, policyRuleUrl, policyOverridable.toString(), policyDescription, policySeverity);
            } else {
                throw new IssueModelBuilderException("The field 'policyRuleUrl' is required for policy notifications.");
            }
        } else if (IssueCategory.VULNERABILITY.equals(issueCategory)) {
            blackDuckIssueFieldTemplate = BlackDuckIssueFieldTemplate.createVulnerabilityIssueFieldTemplate(
                projectOwner, projectName, projectVersionName, projectVersionUri, projectVersionNickname, componentReviewer, componentName, componentVersionName, componentVersionUiLink, licenseString, licenseLink, usagesString,
                updatedTimeString, originsString, originIdsString);
        } else {
            issueCategory = IssueCategory.SPECIAL;
            blackDuckIssueFieldTemplate = new BlackDuckIssueFieldTemplate(projectOwner, projectName, projectVersionName, projectVersionUri, projectVersionNickname,
                componentReviewer, componentName, componentUiLink, componentVersionName, componentVersionUiLink, licenseString, licenseLink, usagesString, updatedTimeString, issueCategory);
        }

        String jiraIssueSummary = null;
        String issueDescription = null;
        if (!IssueCategory.SPECIAL.equals(issueCategory)) {
            jiraIssueSummary = dataFormatHelper.createIssueSummary(issueCategory, projectName, projectVersionName, componentName, componentVersionName, policyRuleName);
            issueDescription = dataFormatHelper.getIssueDescription(issueCategory, projectVersionUri, componentName, componentVersionUri, includeRemediationInfo);
        }
        final JiraIssueFieldTemplate jiraIssueFieldTemplate = new JiraIssueFieldTemplate(jiraProjectId, jiraProjectName, jiraIssueTypeId, jiraIssueSummary, issueCreator, issueDescription, assigneeId);

        final BlackDuckIssueModel model = new BlackDuckIssueModel(action, jiraIssueFieldTemplate, blackDuckIssueFieldTemplate, projectFieldCopyMappings, bomComponentUri, componentIssueUrl, lastBatchStartDate);
        addComments(model);
        return model;
    }

    // TODO make sure all of these fields are correctly updated
    public BlackDuckIssueModelBuilder copy() {
        final BlackDuckIssueModelBuilder newBuilder = new BlackDuckIssueModelBuilder(blackDuckDataHelper, dataFormatHelper);
        newBuilder.action = action;
        newBuilder.projectFieldCopyMappings = new HashSet<>(projectFieldCopyMappings);
        newBuilder.bomComponentUri = bomComponentUri;
        newBuilder.componentIssueUrl = componentIssueUrl;
        newBuilder.lastBatchStartDate = lastBatchStartDate;
        newBuilder.includeRemediationInfo = includeRemediationInfo;

        newBuilder.jiraProjectId = jiraProjectId;
        newBuilder.jiraProjectName = jiraProjectName;
        newBuilder.jiraIssueTypeId = jiraIssueTypeId;
        newBuilder.issueCreator = issueCreator;
        newBuilder.assigneeId = assigneeId;

        newBuilder.projectOwner = projectOwner;
        newBuilder.projectName = projectName;
        newBuilder.projectVersionName = projectVersionName;
        newBuilder.projectVersionUri = projectVersionUri;
        newBuilder.projectVersionNickname = projectVersionNickname;
        newBuilder.componentReviewer = componentReviewer;
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

    private <T> String createCommaSeparatedString(final List<T> list, final Function<T, String> reductionFunction) {
        if (list != null && !list.isEmpty()) {
            return list.stream().map(reductionFunction).collect(Collectors.joining(", "));
        }
        return null;
    }

}
