/**
 * Hub JIRA Plugin
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
package com.blackducksoftware.integration.jira.task.conversion.output.eventdata;

import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ComplexLicenseType;
import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.PolicyRuleViewV2;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.notification.content.detail.NotificationContentDetail;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.bucket.HubBucket;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;

public class EventDataFormatHelper {
    private final HubJiraLogger logger;
    private final HubService hubService;

    public EventDataFormatHelper(final HubJiraLogger logger, final HubService hubService) {
        this.logger = logger;
        this.hubService = hubService;
    }

    public String getIssueSummary(final NotificationContentDetail detail, final Optional<PolicyRuleViewV2> optionalPolicyRule) {
        final String projectName = detail.getProjectName().orElse("?");
        final String projectVersionName = detail.getProjectVersionName().orElse("?");
        if (detail.isPolicy()) {
            final String componentString = getComponentString(detail.getComponentName(), detail.getComponentVersionName());
            final String ruleName = optionalPolicyRule.isPresent() ? optionalPolicyRule.get().name : "?";
            final String issueSummaryTemplate = "Black Duck policy violation detected on Hub project '%s' / '%s', component '%s' [Rule: '%s']";
            return String.format(issueSummaryTemplate, projectName, projectVersionName, componentString, ruleName);
        } else if (detail.isVulnerability()) {
            final StringBuilder issueSummary = new StringBuilder();
            issueSummary.append("Black Duck vulnerability status changes on Hub project '");
            issueSummary.append(projectName);
            issueSummary.append("' / '");
            issueSummary.append(projectVersionName);
            issueSummary.append("', component '");
            issueSummary.append(detail.getComponentName().orElse("?"));
            issueSummary.append("' / '");
            issueSummary.append(detail.getComponentVersionName().orElse("?"));
            issueSummary.append("'");
            return issueSummary.toString();
        }
        return "";
    }

    private String getComponentString(final Optional<String> componentName, final Optional<String> componentVersionName) {
        String componentString = "?";
        if (componentName.isPresent()) {
            componentString = componentName.get();
            if (componentVersionName.isPresent()) {
                componentString += "' / '" + componentVersionName.get();
            }
        }
        return componentString;
    }

    public String getIssueDescription(final NotificationContentDetail detail, final Optional<PolicyRuleViewV2> optionalRule, final HubBucket hubBucket) {
        final StringBuilder issueDescription = new StringBuilder();

        String componentsLink = null;
        String vulnerableComponentsLink = null;
        if (detail.getProjectVersion().isPresent()) {
            final ProjectVersionView projectVersion = hubBucket.get(detail.getProjectVersion().get());
            componentsLink = hubService.getFirstLinkSafely(projectVersion, ProjectVersionView.COMPONENTS_LINK);
            vulnerableComponentsLink = hubService.getFirstLinkSafely(projectVersion, ProjectVersionView.VULNERABLE_COMPONENTS_LINK);
        }
        if (detail.isPolicy()) {
            issueDescription.append("The Black Duck Hub has detected a policy violation on Hub project ");
        } else {
            issueDescription.append("This issue tracks vulnerability status changes on Hub project ");
        }
        final String projectName = detail.getProjectName().orElse("?");
        final String projectVersionName = detail.getProjectVersionName().orElse("?");
        if (componentsLink == null) {
            issueDescription.append("'");
            issueDescription.append(projectName);
            issueDescription.append("' / '");
            issueDescription.append(projectVersionName);
            issueDescription.append("'");
        } else {
            issueDescription.append("['");
            issueDescription.append(projectName);
            issueDescription.append("' / '");
            issueDescription.append(projectVersionName);
            issueDescription.append("'|");
            issueDescription.append(componentsLink);
            issueDescription.append("]");
        }
        if (detail.getComponentName().isPresent()) {
            issueDescription.append(", component '");
            issueDescription.append(detail.getComponentName().get());
            if (detail.getComponentVersionName().isPresent()) {
                issueDescription.append("' / '");
                issueDescription.append(detail.getComponentVersionName().get());
            }
        }
        if (optionalRule.isPresent()) {
            final PolicyRuleViewV2 rule = optionalRule.get();
            issueDescription.append("'.");
            issueDescription.append(" The rule violated is: '");
            issueDescription.append(rule.name);
            issueDescription.append("'. Rule overridable: ");
            issueDescription.append(rule.overridable);
        } else {
            issueDescription.append("'. For details, see the comments below, or the project's ");
            if (vulnerableComponentsLink != null) {
                issueDescription.append("[vulnerabilities|");
                issueDescription.append(vulnerableComponentsLink);
                issueDescription.append("]");
            } else {
                issueDescription.append("vulnerabilities");
            }
            issueDescription.append(" in the Hub.");
        }

        if (detail.getComponentVersion().isPresent()) {
            try {
                final ComponentVersionView componentVersion = hubBucket.get(detail.getComponentVersion().get());
                final String licenseText = getComponentLicensesStringWithLinksAtlassianFormat(componentVersion);
                issueDescription.append("\nComponent license(s): ");
                issueDescription.append(licenseText);
            } catch (final IntegrationException e) {
                // omit license text
            }
        }
        return issueDescription.toString();
    }

    public String generateVulnerabilitiesComment(final VulnerabilityNotificationContent vulnerabilityContent) {
        final StringBuilder commentText = new StringBuilder();
        commentText.append("(Black Duck Hub JIRA plugin auto-generated comment)\n");
        generateVulnerabilitiesCommentText(commentText, vulnerabilityContent.newVulnerabilityIds, "added");
        generateVulnerabilitiesCommentText(commentText, vulnerabilityContent.updatedVulnerabilityIds, "updated");
        generateVulnerabilitiesCommentText(commentText, vulnerabilityContent.deletedVulnerabilityIds, "deleted");
        return commentText.toString();
    }

    public String getComponentLicensesStringPlainText(final ComponentVersionView componentVersion) throws IntegrationException {
        return getComponentLicensesString(componentVersion, false);
    }

    public String getComponentLicensesStringWithLinksAtlassianFormat(final ComponentVersionView componentVersion) throws IntegrationException {
        return getComponentLicensesString(componentVersion, true);
    }

    private void generateVulnerabilitiesCommentText(final StringBuilder commentText, final List<VulnerabilitySourceQualifiedId> vulns, final String verb) {
        commentText.append("Vulnerabilities " + verb + ": ");
        int index = 0;
        if (vulns != null && !vulns.isEmpty()) {
            for (final VulnerabilitySourceQualifiedId vuln : vulns) {
                commentText.append(vuln.vulnerabilityId + " (" + vuln.source + ")");
                if ((index + 1) < vulns.size()) {
                    commentText.append(", ");
                }
                index++;
            }
        } else {
            commentText.append("None");
        }
        commentText.append("\n");
    }

    private String getComponentLicensesString(final ComponentVersionView componentVersion, final boolean includeLinks) throws IntegrationException {
        String licensesString = "";
        if ((componentVersion != null) && (componentVersion.license != null) && (componentVersion.license.licenses != null)) {
            final ComplexLicenseType type = componentVersion.license.type;
            final StringBuilder sb = new StringBuilder();

            if (type != null) {
                final String licenseJoinString = (type == ComplexLicenseType.CONJUNCTIVE) ? HubJiraConstants.LICENSE_NAME_JOINER_AND : HubJiraConstants.LICENSE_NAME_JOINER_OR;
                int licenseIndex = 0;
                for (final ComplexLicenseView license : componentVersion.license.licenses) {
                    if (licenseIndex++ > 0) {
                        sb.append(licenseJoinString);
                    }
                    createLicenseString(sb, license, includeLinks);
                }

            } else {
                createLicenseString(sb, componentVersion.license, includeLinks);
            }
            licensesString = sb.toString();
        }
        return licensesString;
    }

    private void createLicenseString(final StringBuilder sb, final ComplexLicenseView license, final boolean includeLinks) throws IntegrationException {
        final String licenseTextUrl = getLicenseTextUrl(license);
        logger.debug("Link to licence text: " + licenseTextUrl);

        if (includeLinks) {
            sb.append("[");
        }
        sb.append(license.name);
        if (includeLinks) {
            sb.append("|");
            sb.append(licenseTextUrl);
            sb.append("]");
        }
    }

    private String getLicenseTextUrl(final ComplexLicenseView license) throws IntegrationException {
        final String licenseUrl = license.license;
        final ComplexLicenseView fullLicense = hubService.getResponse(licenseUrl, ComplexLicenseView.class);
        final String licenseTextUrl = hubService.getFirstLink(fullLicense, "text");
        return licenseTextUrl;
    }
}
