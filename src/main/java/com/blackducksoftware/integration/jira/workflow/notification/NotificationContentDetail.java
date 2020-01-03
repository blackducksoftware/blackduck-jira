/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
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
package com.blackducksoftware.integration.jira.workflow.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.synopsys.integration.blackduck.api.UriSingleResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.generated.view.ComponentVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ComponentView;
import com.synopsys.integration.blackduck.api.generated.view.IssueView;
import com.synopsys.integration.blackduck.api.generated.view.PolicyRuleView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;
import com.synopsys.integration.util.Stringable;

public class NotificationContentDetail extends Stringable {
    private final String notificationGroup;
    private final String contentDetailKey;

    private final Optional<String> projectName;
    private final Optional<String> projectVersionName;
    private final Optional<UriSingleResponse<ProjectVersionView>> projectVersion;

    private final Optional<String> componentName;
    private final Optional<UriSingleResponse<ComponentView>> component;

    private final Optional<String> componentVersionName;
    private final Optional<UriSingleResponse<ComponentVersionView>> componentVersion;

    private final Optional<String> policyName;
    private final Optional<UriSingleResponse<PolicyRuleView>> policy;

    private final Optional<String> componentVersionOriginName;
    private final Optional<UriSingleResponse<IssueView>> componentIssue;

    private final Optional<String> componentVersionOriginId;

    private final Optional<UriSingleResponse<VersionBomComponentView>> bomComponent;

    public final static String CONTENT_KEY_GROUP_BOM_EDIT = "bom_edit";
    public final static String CONTENT_KEY_GROUP_LICENSE = "license";
    public final static String CONTENT_KEY_GROUP_POLICY = "policy";
    public final static String CONTENT_KEY_GROUP_VULNERABILITY = "vulnerability";
    public final static String CONTENT_KEY_SEPARATOR = "|";

    // @formatter:off
    public static NotificationContentDetail createDetail(
            final String notificationGroup
            ,final Optional<String> projectName
            ,final Optional<String> projectVersionName
            ,final Optional<String> projectVersionUri
            ,final Optional<String> componentName
            ,final Optional<String> componentUri
            ,final Optional<String> componentVersionName
            ,final Optional<String> componentVersionUri
            ,final Optional<String> policyName
            ,final Optional<String> policyUri
            ,final Optional<String> componentVersionOriginName
            ,final Optional<String> componentIssueUri
            ,final Optional<String> componentVersionOriginId
            ,final Optional<String> bomComponent
            ) {
        return new NotificationContentDetail(
                notificationGroup
                ,projectName
                ,projectVersionName
                ,projectVersionUri
                ,componentName
                ,componentUri
                ,componentVersionName
                ,componentVersionUri
                ,policyName
                ,policyUri
                ,componentVersionOriginName
                ,componentIssueUri
                ,componentVersionOriginId
                ,bomComponent
                );
    }
    // @formatter:on

    // @formatter:off
    private NotificationContentDetail(
            final String notificationGroup
            ,final Optional<String> projectName
            ,final Optional<String> projectVersionName
            ,final Optional<String> projectVersion
            ,final Optional<String> componentName
            ,final Optional<String> component
            ,final Optional<String> componentVersionName
            ,final Optional<String> componentVersion
            ,final Optional<String> policyName
            ,final Optional<String> policy
            ,final Optional<String> componentVersionOriginName
            ,final Optional<String> componentIssue
            ,final Optional<String> componentVersionOriginId
            ,final Optional<String> bomComponent
            ) {
        this.notificationGroup = notificationGroup;
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.projectVersion = createUriSingleResponse(projectVersion, ProjectVersionView.class);
        this.componentName = componentName;
        this.component = createUriSingleResponse(component, ComponentView.class);
        this.componentVersionName = componentVersionName;
        this.componentVersion = createUriSingleResponse(componentVersion, ComponentVersionView.class);
        this.policyName = policyName;
        this.policy = createUriSingleResponse(policy, PolicyRuleView.class);
        this.componentVersionOriginName = componentVersionOriginName;
        this.componentIssue = createUriSingleResponse(componentIssue, IssueView.class);
        this.componentVersionOriginId = componentVersionOriginId;
        this.bomComponent = createUriSingleResponse(bomComponent, VersionBomComponentView.class);
        contentDetailKey = createContentDetailKey();
    }
    // @formatter:on

    private <T extends BlackDuckResponse> Optional<UriSingleResponse<T>> createUriSingleResponse(final Optional<String> uri, final Class<T> responseClass) {
        if (uri.isPresent()) {
            return Optional.of(new UriSingleResponse<>(uri.get(), responseClass));
        }
        return Optional.empty();
    }

    private String createContentDetailKey() {
        final StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(notificationGroup);
        keyBuilder.append(CONTENT_KEY_SEPARATOR);

        if (projectVersion.isPresent()) {
            keyBuilder.append(projectVersion.get().getUri().hashCode());
        }
        keyBuilder.append(CONTENT_KEY_SEPARATOR);

        if (component.isPresent()) {
            keyBuilder.append(component.get().getUri().hashCode());
        }
        keyBuilder.append(CONTENT_KEY_SEPARATOR);

        if (componentVersion.isPresent()) {
            keyBuilder.append(componentVersion.get().getUri().hashCode());
        }
        keyBuilder.append(CONTENT_KEY_SEPARATOR);

        if (policy.isPresent()) {
            keyBuilder.append(policy.get().getUri().hashCode());
            keyBuilder.append(CONTENT_KEY_SEPARATOR);
        }

        if (bomComponent.isPresent()) {
            keyBuilder.append(bomComponent.get().getUri().hashCode());
        }
        keyBuilder.append(CONTENT_KEY_SEPARATOR);

        final String key = keyBuilder.toString();
        return key;
    }

    public boolean hasComponentVersion() {
        return componentVersion.isPresent();
    }

    public boolean hasOnlyComponent() {
        return component.isPresent();
    }

    public boolean isPolicy() {
        return policy.isPresent();
    }

    public boolean isVulnerability() {
        return CONTENT_KEY_GROUP_VULNERABILITY.equals(notificationGroup);
    }

    public boolean isBomEdit() {
        return CONTENT_KEY_GROUP_BOM_EDIT.equals(notificationGroup);
    }

    public List<UriSingleResponse<? extends BlackDuckResponse>> getPresentLinks() {
        final List<UriSingleResponse<? extends BlackDuckResponse>> presentLinks = new ArrayList<>();
        if (projectVersion.isPresent()) {
            presentLinks.add(projectVersion.get());
        }
        if (component.isPresent()) {
            presentLinks.add(component.get());
        }
        if (componentVersion.isPresent()) {
            presentLinks.add(componentVersion.get());
        }
        if (policy.isPresent()) {
            presentLinks.add(policy.get());
        }
        return presentLinks;
    }

    public String getNotificationGroup() {
        return notificationGroup;
    }

    public String getContentDetailKey() {
        return contentDetailKey;
    }

    public Optional<String> getProjectName() {
        return projectName;
    }

    public Optional<String> getProjectVersionName() {
        return projectVersionName;
    }

    public Optional<UriSingleResponse<ProjectVersionView>> getProjectVersion() {
        return projectVersion;
    }

    public Optional<String> getComponentName() {
        return componentName;
    }

    public Optional<UriSingleResponse<ComponentView>> getComponent() {
        return component;
    }

    public Optional<String> getComponentVersionName() {
        return componentVersionName;
    }

    public Optional<UriSingleResponse<ComponentVersionView>> getComponentVersion() {
        return componentVersion;
    }

    public Optional<String> getPolicyName() {
        return policyName;
    }

    public Optional<UriSingleResponse<PolicyRuleView>> getPolicy() {
        return policy;
    }

    public Optional<String> getComponentVersionOriginName() {
        return componentVersionOriginName;
    }

    public Optional<UriSingleResponse<IssueView>> getComponentIssue() {
        return componentIssue;
    }

    public Optional<String> getComponentVersionOriginId() {
        return componentVersionOriginId;
    }

    public Optional<UriSingleResponse<VersionBomComponentView>> getBomComponent() {
        return bomComponent;
    }

}
