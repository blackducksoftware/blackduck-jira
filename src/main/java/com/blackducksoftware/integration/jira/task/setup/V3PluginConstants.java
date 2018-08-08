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
package com.blackducksoftware.integration.jira.task.setup;

import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;

// Used for reference when upgrading the plugin
public class V3PluginConstants {
    public static final String V3_POLICY_VIOLATION_ISSUE = "Hub Policy Violation";
    public static final String V3_VULNERABILITY_ISSUE = "Hub Security Vulnerability";
    public static final String V3_JIRA_WORKFLOW = "BDS Hub PlugIn Workflow";
    public static final String V3_FIELD_CONFIGURATION_SCHEME_NAME = "BDS Hub PlugIn Field Configuration Scheme";
    public static final String V3_FIELD_CONFIGURATION = "BDS Hub PlugIn Field Configuration";
    public static final String V3_POLICY_SCREEN_SCHEME_NAME = "BDS Hub PlugIn Policy Screen Scheme";
    public static final String V3_SECURITY_SCREEN_SCHEME_NAME = "BDS Hub PlugIn Security Screen Scheme";
    public static final String V3_POLICY_SCREEN_NAME = "BDS Hub PlugIn Policy Screen";
    public static final String V3_SECURITY_SCREEN_NAME = "BDS Hub PlugIn Security Screen";
    public static final String V3_SCREEN_TAB = "BDS Hub PlugIn Screen Tab";

    public static Map<String, String> V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP = new HashMap<>();
    static {
        // This should never be modified
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Project", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Project Version", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Project Version Url", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_URL);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Component", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_COMPONENT);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Component Url", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_COMPONENT_URL);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Component Version", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Component Version Url", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION_URL);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Policy Rule", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Policy Rule Overridable", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Policy Rule Description", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_DESCRIPTION);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Policy Rule Url", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_URL);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Component Licenses", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_LICENSE_NAMES);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Component License Url", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_LICENSE_URL);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Component Usage", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_COMPONENT_USAGE);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Component Origin", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Component Origin ID", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Project Version Nickname", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Project Owner", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT_OWNER);
        V3_TO_LATEST_CUSTOM_FIELD_NAME_MAP.put("BDS Hub Project Version Last Updated", BlackDuckJiraConstants.BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED);
    }

}
