/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.jira.common.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;
import com.synopsys.integration.blackduck.api.manual.component.AffectedProjectVersion;
import com.synopsys.integration.blackduck.api.manual.component.ComponentVersionStatus;
import com.synopsys.integration.blackduck.api.manual.component.PolicyInfo;

public class NotificationContentDetailFactory {
    private final Gson gson;
    private final JsonParser jsonParser;

    public NotificationContentDetailFactory(final Gson gson, final JsonParser jsonParser) {
        this.gson = gson;
        this.jsonParser = jsonParser;
    }

    public NotificationDetailResult generateContentDetails(final CommonNotificationView view) {
        final NotificationType type = view.getType();
        final String notificationJson = view.getJson();
        final JsonObject jsonObject = jsonParser.parse(notificationJson).getAsJsonObject();

        NotificationContent notificationContent = null;
        String notificationGroup = null;
        final List<NotificationContentDetail> notificationContentDetails = new ArrayList<>();

        if (NotificationType.POLICY_OVERRIDE.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), PolicyOverrideNotificationContent.class);
            notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_POLICY;
            populateContentDetails(notificationContentDetails, notificationGroup, (PolicyOverrideNotificationContent) notificationContent);
        } else if (NotificationType.RULE_VIOLATION.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), RuleViolationNotificationContent.class);
            notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_POLICY;
            populateContentDetails(notificationContentDetails, notificationGroup, (RuleViolationNotificationContent) notificationContent);
        } else if (NotificationType.RULE_VIOLATION_CLEARED.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), RuleViolationClearedNotificationContent.class);
            notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_POLICY;
            populateContentDetails(notificationContentDetails, notificationGroup, (RuleViolationClearedNotificationContent) notificationContent);
        } else if (NotificationType.VULNERABILITY.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), VulnerabilityNotificationContent.class);
            notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_VULNERABILITY;
            populateContentDetails(notificationContentDetails, notificationGroup, (VulnerabilityNotificationContent) notificationContent);
        } else if (NotificationType.BOM_EDIT.equals(type)) {
            notificationContent = gson.fromJson(jsonObject.get("content"), BomEditContent.class);
            notificationGroup = NotificationContentDetail.CONTENT_KEY_GROUP_BOM_EDIT;
            populateContentDetails(notificationContentDetails, notificationGroup, (BomEditContent) notificationContent);
        }

        return new NotificationDetailResult(notificationContent, view.getContentType(), view.getCreatedAt(), view.getType(), notificationGroup, Optional.ofNullable(view.getNotificationState()), notificationContentDetails);
    }

    public void populateContentDetails(final List<NotificationContentDetail> notificationContentDetails, final String notificationGroup, final PolicyOverrideNotificationContent content) {
        for (final PolicyInfo policyInfo : content.getPolicyInfos()) {
            final String componentValue;
            if (content.getComponentVersion() != null) {
                componentValue = null;
            } else {
                componentValue = content.getComponent();
            }
            final NotificationContentDetail detail = NotificationContentDetail.createDetail(notificationGroup, Optional.of(content.getProjectName()), Optional.of(content.getProjectVersionName()), Optional.of(content.getProjectVersion()),
                Optional.of(content.getComponentName()), Optional.ofNullable(componentValue), Optional.ofNullable(content.getComponentVersionName()), Optional.ofNullable(content.getComponentVersion()),
                Optional.of(policyInfo.getPolicyName()),
                Optional.of(policyInfo.getPolicy()), Optional.empty(), Optional.empty(), Optional.empty(), Optional.ofNullable(content.getBomComponent()));
            notificationContentDetails.add(detail);
        }
    }

    public void populateContentDetails(final List<NotificationContentDetail> notificationContentDetails, final String notificationGroup, final RuleViolationNotificationContent content) {
        final Map<String, String> uriToName = content.getPolicyInfos().stream().collect(Collectors.toMap(policyInfo -> policyInfo.getPolicy(), policyInfo -> policyInfo.getPolicyName()));
        for (final ComponentVersionStatus componentVersionStatus : content.getComponentVersionStatuses()) {
            for (final String policyUri : componentVersionStatus.getPolicies()) {
                final String policyName = uriToName.get(policyUri);
                final String componentValue;
                if (componentVersionStatus.getComponentVersion() != null) {
                    componentValue = null;
                } else {
                    componentValue = componentVersionStatus.getComponent();
                }
                final NotificationContentDetail detail = NotificationContentDetail
                                                             .createDetail(notificationGroup, Optional.of(content.getProjectName()), Optional.of(content.getProjectVersionName()), Optional.of(content.getProjectVersion()),
                                                                 Optional.of(componentVersionStatus.getComponentName()), Optional.ofNullable(componentValue), Optional.ofNullable(componentVersionStatus.getComponentVersionName()),
                                                                 Optional.ofNullable(componentVersionStatus.getComponentVersion()),
                                                                 Optional.of(policyName), Optional.of(policyUri), Optional.empty(), Optional.ofNullable(componentVersionStatus.getComponentIssueLink()), Optional.empty(),
                                                                 Optional.ofNullable(componentVersionStatus.getBomComponent()));
                notificationContentDetails.add(detail);
            }
        }
    }

    public void populateContentDetails(final List<NotificationContentDetail> notificationContentDetails, final String notificationGroup, final RuleViolationClearedNotificationContent content) {
        final Map<String, String> uriToName = content.getPolicyInfos().stream().collect(Collectors.toMap(policyInfo -> policyInfo.getPolicy(), policyInfo -> policyInfo.getPolicyName()));
        for (final ComponentVersionStatus componentVersionStatus : content.getComponentVersionStatuses()) {
            for (final String policyUri : componentVersionStatus.getPolicies()) {
                final String policyName = uriToName.get(policyUri);
                final String componentValue;
                if (componentVersionStatus.getComponentVersion() != null) {
                    componentValue = null;
                } else {
                    componentValue = componentVersionStatus.getComponent();
                }
                final NotificationContentDetail detail = NotificationContentDetail
                                                             .createDetail(notificationGroup, Optional.of(content.getProjectName()), Optional.of(content.getProjectVersionName()), Optional.of(content.getProjectVersion()),
                                                                 Optional.of(componentVersionStatus.getComponentName()), Optional.ofNullable(componentValue), Optional.ofNullable(componentVersionStatus.getComponentVersionName()),
                                                                 Optional.ofNullable(componentVersionStatus.getComponentVersion()),
                                                                 Optional.of(policyName), Optional.of(policyUri), Optional.empty(), Optional.of(componentVersionStatus.getComponentIssueLink()), Optional.empty(),
                                                                 Optional.ofNullable(componentVersionStatus.getBomComponent()));
                notificationContentDetails.add(detail);
            }
        }
    }

    public void populateContentDetails(final List<NotificationContentDetail> notificationContentDetails, final String notificationGroup, final VulnerabilityNotificationContent content) {
        for (final AffectedProjectVersion projectVersion : content.getAffectedProjectVersions()) {
            final NotificationContentDetail detail = NotificationContentDetail
                                                         .createDetail(notificationGroup, Optional.of(projectVersion.getProjectName()), Optional.of(projectVersion.getProjectVersionName()), Optional.of(projectVersion.getProjectVersion()),
                                                             Optional.of(content.getComponentName()), Optional.empty(), Optional.of(content.getVersionName()), Optional.of(content.getComponentVersion()), Optional.empty(), Optional.empty(),
                                                             Optional.ofNullable(content.getComponentVersionOriginName()), Optional.ofNullable(projectVersion.getComponentIssueUrl()),
                                                             Optional.ofNullable(content.getComponentVersionOriginId()),
                                                             Optional.ofNullable(projectVersion.getBomComponent()));
            notificationContentDetails.add(detail);
        }
    }

    private void populateContentDetails(final List<NotificationContentDetail> notificationContentDetails, final String notificationGroup, final BomEditContent notificationContent) {
        final NotificationContentDetail detail = NotificationContentDetail
                                                     .createDetail(notificationGroup, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                                                         Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.ofNullable(notificationContent.bomComponent));
        notificationContentDetails.add(detail);
    }

}