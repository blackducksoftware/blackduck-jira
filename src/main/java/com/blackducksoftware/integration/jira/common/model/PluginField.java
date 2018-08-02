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
package com.blackducksoftware.integration.jira.common.model;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;

public enum PluginField {
    // @formatter:off
    HUB_CUSTOM_FIELD_PROJECT("HUB_CUSTOM_FIELD_PROJECT", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_DISPLAYNAMEPROPERTY, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_DISPLAYNAMEPROPERTY_LONG),
    HUB_CUSTOM_FIELD_PROJECT_VERSION("HUB_CUSTOM_FIELD_PROJECT_VERSION", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION_DISPLAYNAMEPROPERTY, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION_DISPLAYNAMEPROPERTY_LONG),
    HUB_CUSTOM_FIELD_PROJECT_VERSION_URL("HUB_CUSTOM_FIELD_PROJECT_VERSION_URL", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION_URL, null, null),
    HUB_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME("HUB_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME_DISPLAYNAMEPROPERTY, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME_DISPLAYNAMEPROPERTY_LONG),
    HUB_CUSTOM_FIELD_PROJECT_OWNER("HUB_CUSTOM_FIELD_PROJECT_OWNER", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_OWNER, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_OWNER_DISPLAYNAMEPROPERTY, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_OWNER_DISPLAYNAMEPROPERTY_LONG),

    HUB_CUSTOM_FIELD_COMPONENT("HUB_CUSTOM_FIELD_COMPONENT", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_DISPLAYNAMEPROPERTY, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_DISPLAYNAMEPROPERTY_LONG),
    HUB_CUSTOM_FIELD_COMPONENT_URL("HUB_CUSTOM_FIELD_COMPONENT_URL", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_URL, null, null),
    HUB_CUSTOM_FIELD_COMPONENT_VERSION("HUB_CUSTOM_FIELD_COMPONENT_VERSION", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION_DISPLAYNAMEPROPERTY, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION_DISPLAYNAMEPROPERTY_LONG),
    HUB_CUSTOM_FIELD_COMPONENT_VERSION_URL("HUB_CUSTOM_FIELD_COMPONENT_VERSION_URL", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION_URL, null, null),

    HUB_CUSTOM_FIELD_POLICY_RULE("HUB_CUSTOM_FIELD_POLICY_RULE", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE_DISPLAYNAMEPROPERTY, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE_DISPLAYNAMEPROPERTY_LONG),
    HUB_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE("HUB_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE_DISPLAYNAMEPROPERTY, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE_DISPLAYNAMEPROPERTY_LONG),
    HUB_CUSTOM_FIELD_POLICY_RULE_DESCRIPTION("HUB_CUSTOM_FIELD_POLICY_RULE_DESCRIPTION", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE_DESCRIPTION, null, null),
    HUB_CUSTOM_FIELD_POLICY_RULE_URL("HUB_CUSTOM_FIELD_POLICY_RULE_URL", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE_URL, null, null),

    HUB_CUSTOM_FIELD_LICENSE_NAMES("HUB_CUSTOM_FIELD_LICENSE_NAMES", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_LICENSE_NAMES, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_LICENSE_NAMES_DISPLAYNAMEPROPERTY, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_LICENSE_NAMES_DISPLAYNAMEPROPERTY_LONG),
    HUB_CUSTOM_FIELD_LICENSE_URL("HUB_CUSTOM_FIELD_LICENSE_URL", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_LICENSE_URL, null, null),
    HUB_CUSTOM_FIELD_COMPONENT_USAGE("HUB_CUSTOM_FIELD_COMPONENT_USAGE", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_USAGE, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_USAGE_DISPLAYNAMEPROPERTY, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_USAGE_DISPLAYNAMEPROPERTY_LONG),
    HUB_CUSTOM_FIELD_COMPONENT_ORIGIN("HUB_CUSTOM_FIELD_COMPONENT_ORIGIN", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN_DISPLAYNAMEPROPERTY, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN_DISPLAYNAMEPROPERTY_LONG),
    HUB_CUSTOM_FIELD_COMPONENT_ORIGIN_ID("HUB_CUSTOM_FIELD_COMPONENT_ORIGIN_ID", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN_ID, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN_ID_DISPLAYNAMEPROPERTY, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_ORIGIN_ID_DISPLAYNAMEPROPERTY_LONG),
    HUB_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED("HUB_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED", BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED_DISPLAYNAMEPROPERTY, BlackDuckJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED_DISPLAYNAMEPROPERTY_LONG);
    // @formatter:on

    private final String id;
    private final String name;
    private final String displayNameProperty;
    private final String longNameProperty;

    private PluginField(final String id, final String name, final String displayNameProperty, final String longNameProperty) {
        this.id = id;
        this.name = name;
        this.displayNameProperty = displayNameProperty;
        this.longNameProperty = longNameProperty;
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

    public String getLongNameProperty() {
        return longNameProperty;
    }

}
