package com.blackducksoftware.integration.jira.task.issue.model;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.config.model.ProjectFieldCopyMapping;
import com.blackducksoftware.integration.jira.task.conversion.output.BlackDuckEventAction;
import com.blackducksoftware.integration.jira.task.conversion.output.eventdata.EventDataFormatHelper;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleViewV2;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.bucket.HubBucket;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.Stringable;

public class BlackDuckIssueBuilder extends Stringable {
    private final HubService blackDuckService;
    private final HubBucket blackDuckBucket;
    private final EventDataFormatHelper dataFormatHelper;

    private BlackDuckEventAction issueAction;
    private Set<ProjectFieldCopyMapping> projectFieldCopyMappings;
    private String bomComponentUri;
    private String componentIssueUrl;

    // Jira Issue Fields
    private Long jiraProjectId;
    private String jiraProjectName;
    private String jiraIssueTypeId;
    private String jiraIssueSummary;
    private String issueCreatorUsername;
    private String issueDescription;
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

    public BlackDuckIssueBuilder(final HubService blackDuckService, final HubBucket blackDuckBucket, final EventDataFormatHelper dataFormatHelper) {
        this.blackDuckService = blackDuckService;
        this.blackDuckBucket = blackDuckBucket;
        this.dataFormatHelper = dataFormatHelper;
    }

    public void setIssueAction(final BlackDuckEventAction issueAction) {
        this.issueAction = issueAction;
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

    // TODO SECTION

    public void setJiraProjectId(final Long jiraProjectId) {
        this.jiraProjectId = jiraProjectId;
    }

    public void setJiraProjectName(final String jiraProjectName) {
        this.jiraProjectName = jiraProjectName;
    }

    public void setJiraIssueTypeId(final String jiraIssueTypeId) {
        this.jiraIssueTypeId = jiraIssueTypeId;
    }

    public void setJiraIssueSummary(final String jiraIssueSummary) {
        this.jiraIssueSummary = jiraIssueSummary;
    }

    public void setIssueCreatorUsername(final String issueCreatorUsername) {
        this.issueCreatorUsername = issueCreatorUsername;
    }

    public void setIssueDescription(final String issueDescription) {
        this.issueDescription = issueDescription;
    }

    public void setAssigneeId(final String assigneeId) {
        this.assigneeId = assigneeId;
    }

    // TODO SECTION

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

    // TODO SECTION

    public BlackDuckIssueBuilder setBlackDuckFields(final ProjectVersionWrapper projectVersionWrapper, final VersionBomComponentView versionBomComponent) throws IntegrationException {
        final ProjectView project = projectVersionWrapper.getProjectView();
        final ProjectVersionView projectVersion = projectVersionWrapper.getProjectVersionView();

        this.projectOwner = null; // FIXME getJiraProjectOwner(project.projectOwner, blackDuckBucket);
        this.projectName = project.name;
        this.projectVersionName = projectVersion.versionName;
        this.projectVersionUri = blackDuckService.getHref(projectVersion);
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

    public BlackDuckIssueBuilder setPolicyFields(final PolicyRuleViewV2 policyRule) throws IntegrationException {
        this.policyRuleUrl = blackDuckService.getHref(policyRule);
        this.policyRuleName = policyRule.name;
        this.policyDescription = policyRule.description;
        this.policyOverridable = policyRule.overridable;
        this.policySeverity = policyRule.severity;
        return this;
    }

    // TODO throw exception if missing required fields
    public BlackDuckIssueWrapper build() {
        final JiraIssueFieldTemplate jiraIssueFieldTemplate = new JiraIssueFieldTemplate(jiraProjectId, jiraProjectName, jiraIssueTypeId, jiraIssueSummary, issueCreatorUsername, issueDescription, assigneeId);

        if (issueAction == null) {
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

        final BlackDuckIssueWrapper wrapper = new BlackDuckIssueWrapper(issueAction, jiraIssueFieldTemplate, blackDuckIssueFieldTemplate, projectFieldCopyMappings, bomComponentUri, componentIssueUrl);
        addComments(wrapper);
        return wrapper;
    }

    private void addComments(final BlackDuckIssueWrapper wrapper) {
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
