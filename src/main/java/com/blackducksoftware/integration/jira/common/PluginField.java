/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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

public enum PluginField {

    HUB_CUSTOM_FIELD_PROJECT("HUB_CUSTOM_FIELD_PROJECT", HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT, HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_DISPLAYNAMEPROPERTY),
    HUB_CUSTOM_FIELD_PROJECT_VERSION("HUB_CUSTOM_FIELD_PROJECT_VERSION", HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION, HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION_DISPLAYNAMEPROPERTY),
    HUB_CUSTOM_FIELD_COMPONENT("HUB_CUSTOM_FIELD_COMPONENT", HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT, HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_DISPLAYNAMEPROPERTY),
    HUB_CUSTOM_FIELD_COMPONENT_VERSION("HUB_CUSTOM_FIELD_COMPONENT_VERSION", HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION, HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION_DISPLAYNAMEPROPERTY),
    HUB_CUSTOM_FIELD_POLICY_RULE("HUB_CUSTOM_FIELD_POLICY_RULE", HubJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE, HubJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE_DISPLAYNAMEPROPERTY),
    HUB_CUSTOM_FIELD_LICENSE_NAMES("HUB_CUSTOM_FIELD_LICENSE_NAMES", HubJiraConstants.HUB_CUSTOM_FIELD_LICENSE_NAMES, HubJiraConstants.HUB_CUSTOM_FIELD_LICENSE_NAMES_DISPLAYNAMEPROPERTY);

    private final String id;

    private final String name;

    private final String displayNameProperty;

    private PluginField(final String id, final String name, final String displayNameProperty) {
        this.id = id;
        this.name = name;
        this.displayNameProperty = displayNameProperty;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayNameProperty() {
        return displayNameProperty;
    }

}
