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
package com.blackducksoftware.integration.jira.common;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.core.HubResponse;
import com.synopsys.integration.blackduck.api.core.HubView;
import com.synopsys.integration.blackduck.api.core.LinkMultipleResponses;
import com.synopsys.integration.blackduck.api.core.LinkSingleResponse;
import com.synopsys.integration.blackduck.api.generated.component.RemediationOptionsView;
import com.synopsys.integration.blackduck.api.generated.component.RiskCountView;
import com.synopsys.integration.blackduck.api.generated.enumeration.RiskCountType;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.RiskProfileView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.blackduck.exception.HubIntegrationException;
import com.synopsys.integration.blackduck.notification.content.VulnerabilitySourceQualifiedId;
import com.synopsys.integration.blackduck.notification.content.detail.NotificationContentDetail;
import com.synopsys.integration.blackduck.service.ComponentService;
import com.synopsys.integration.blackduck.service.HubService;
import com.synopsys.integration.blackduck.service.bucket.HubBucket;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class BlackDuckDataHelper {
    private final BlackDuckJiraLogger logger;
    private final HubService blackDuckService;
    private final HubBucket blackDuckBucket;

    public BlackDuckDataHelper(final BlackDuckJiraLogger logger, final HubService blackDuckService, final HubBucket blackDuckBucket) {
        this.logger = logger;
        this.blackDuckService = blackDuckService;
        this.blackDuckBucket = blackDuckBucket;
    }

    public String getBlackDuckBaseUrl() {
        final URL baseUrl = blackDuckService.getHubBaseUrl();
        return baseUrl.toString();
    }

    public VersionBomComponentView getBomComponent(final UriSingleResponse<VersionBomComponentView> bomComponentLocation) throws IntegrationException {
        IntegrationRestException restException = null;
        try {
            final VersionBomComponentView versionBomComponentView = getResponse(bomComponentLocation);
            return versionBomComponentView;
        } catch (final IntegrationRestException caughtRestException) {
            restException = caughtRestException;
        } catch (final Exception genericException) {
            logger.error(genericException);
            throw genericException;
        }
        throw restException;
    }

    public Optional<RemediationOptionsView> getRemediationInformation(final ComponentVersionView componentVersionView) {
        // TODO use the HubService once the Black Duck APIs have the link.
        final ComponentService componentService = new ComponentService(blackDuckService, logger);
        try {
            return Optional.ofNullable(componentService.getRemediationInformation(componentVersionView));
        } catch (final IntegrationException e) {
            logger.debug("Could not get remediation information: ");
            logger.debug(e.getMessage());
        }
        return Optional.empty();
    }

    public ProjectVersionWrapper getProjectVersionWrapper(final NotificationContentDetail detail) throws IntegrationException {
        final ProjectVersionWrapper projectVersionWrapper;
        if (detail.getProjectVersion().isPresent()) {
            final UriSingleResponse<ProjectVersionView> projectVersionResponse = detail.getProjectVersion().get();
            projectVersionWrapper = getProjectVersionWrapper(projectVersionResponse.uri);
        } else if (detail.getBomComponent().isPresent()) {
            final VersionBomComponentView versionBomComponent = getResponse(detail.getBomComponent().get());
            projectVersionWrapper = getProjectVersionWrapper(versionBomComponent);
        } else {
            throw new IntegrationException("No Black Duck project data available from the notification.");
        }
        return projectVersionWrapper;
    }

    public ProjectVersionWrapper getProjectVersionWrapper(final VersionBomComponentView versionBomComponent) throws IntegrationException {
        // TODO Stop using this when Black Duck supports going back to the project-version
        final String versionBomComponentHref = blackDuckService.getHref(versionBomComponent);
        final int componentsIndex = versionBomComponentHref.indexOf(ProjectVersionView.COMPONENTS_LINK);
        final String projectVersionUri = versionBomComponentHref.substring(0, componentsIndex - 1);

        return getProjectVersionWrapper(projectVersionUri);
    }

    private ProjectVersionWrapper getProjectVersionWrapper(final String projectVersionUri) throws IntegrationException {
        final ProjectVersionView projectVersion = getResponse(projectVersionUri, ProjectVersionView.class);
        final ProjectView project = blackDuckService.getResponse(projectVersion, ProjectVersionView.PROJECT_LINK_RESPONSE);

        final ProjectVersionWrapper wrapper = new ProjectVersionWrapper();
        wrapper.setProjectVersionView(projectVersion);
        wrapper.setProjectView(project);
        return wrapper;
    }

    public boolean doesNotificationOnlyHaveDeletes(final List<VulnerabilitySourceQualifiedId> addedIds, final List<VulnerabilitySourceQualifiedId> updatedIds, final List<VulnerabilitySourceQualifiedId> deletedIds) {
        return deletedIds != null && deletedIds.size() > 0 && addedIds != null && addedIds.size() == 0 && updatedIds != null && updatedIds.size() == 0;
    }

    public boolean doesSecurityRiskProfileHaveVulnerabilities(final RiskProfileView securityRiskProfile) {
        logger.debug("Checking if the component still has vulnerabilities...");
        final int vulnerablitiesCount = getSumOfRiskCounts(securityRiskProfile.counts);
        logger.debug("Number of vulnerabilities found: " + vulnerablitiesCount);
        if (vulnerablitiesCount > 0) {
            logger.debug("This component still has vulnerabilities");
            return true;
        }
        return false;
    }

    private int getSumOfRiskCounts(final List<RiskCountView> vulnerabilityCounts) {
        int count = 0;
        for (final RiskCountView riskCount : vulnerabilityCounts) {
            if (!RiskCountType.OK.equals(riskCount.countType)) {
                count += riskCount.count.intValue();
            }
        }
        return count;
    }

    public <T extends HubResponse> T getResponse(final String uri, final Class<T> clazz) throws IntegrationException {
        return getResponse(new UriSingleResponse<>(uri, clazz));
    }

    public <T extends HubResponse> T getResponse(final UriSingleResponse<T> uriSingleResponse) throws IntegrationException {
        T response = blackDuckBucket.get(uriSingleResponse);
        if (response == null) {
            response = blackDuckService.getResponse(uriSingleResponse);
            blackDuckBucket.addValid(uriSingleResponse.uri, response);
        }
        return response;
    }

    public <T extends HubResponse> T getResponse(final HubView view, final LinkSingleResponse<T> linkSingleResponse) throws IntegrationException {
        return blackDuckService.getResponse(view, linkSingleResponse);
    }

    public <T extends HubResponse> List<T> getAllResponses(final HubView view, final LinkMultipleResponses<T> linkMultipleResponses) throws IntegrationException {
        return blackDuckService.getAllResponses(view, linkMultipleResponses);
    }

    public <T extends HubResponse> T getResponseNullable(final String uri, final Class<T> clazz) {
        try {
            return getResponse(uri, clazz);
        } catch (final IntegrationException e) {
            logger.debug("Could not get response from Black Duck.", e);
        }
        return null;
    }

    public String getFirstLink(final HubView view, final String linkKey) throws IntegrationException {
        return blackDuckService.getFirstLink(view, linkKey);
    }

    public String getFirstLinkSafely(final HubView view, final String linkKey) {
        String link = null;
        try {
            link = blackDuckService.getFirstLink(view, linkKey);
        } catch (HubIntegrationException e) {
            logger.debug("Could not get link " + linkKey + " for view: " + view, e);
        }
        return link;
    }

    public String getHref(final HubView view) throws IntegrationException {
        return blackDuckService.getHref(view);
    }

    public String getHrefNullable(final HubView view) {
        String href = null;
        try {
            href = getHref(view);
        } catch (IntegrationException e) {
            logger.debug("Could not get href for view: " + view, e);
        }
        return href;
    }
}
