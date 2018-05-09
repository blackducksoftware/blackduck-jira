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
package com.blackducksoftware.integration.jira.task.issue;

import java.util.List;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.enumeration.ComplexLicenseType;
import com.blackducksoftware.integration.hub.api.generated.view.ComplexLicenseView;
import com.blackducksoftware.integration.hub.api.generated.view.ComponentVersionView;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilityNotificationContent;
import com.blackducksoftware.integration.hub.notification.content.VulnerabilitySourceQualifiedId;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;

public class IssueFormatHelper {
    private final HubJiraLogger logger;
    private final HubService hubService;

    public IssueFormatHelper(final HubJiraLogger logger, final HubService hubService) {
        this.logger = logger;
        this.hubService = hubService;
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
