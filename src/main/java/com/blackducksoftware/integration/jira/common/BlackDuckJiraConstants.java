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

public class BlackDuckJiraConstants {
    private static final String BLACKDUCK_JIRA_GROUP = "hub-jira";

    public static final int PERIODIC_TASK_TIMEOUT_AS_MULTIPLE_OF_INTERVAL = 4;
    public static final String BLACKDUCK_JIRA_ERROR = BLACKDUCK_JIRA_GROUP + "-ticket-error";
    public static final String BLACKDUCK_JIRA_WORKFLOW_RESOURCE = "Hub Workflow.xml";

    // Issue type names (user visible)
    public static final String BLACKDUCK_POLICY_VIOLATION_ISSUE = "Black Duck Policy Violation";
    public static final String BLACKDUCK_VULNERABILITY_ISSUE = "Black Duck Security Vulnerability";

    // Field names (user visible)
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT = "Black Duck Project";
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION = "Black Duck Project Version";
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_URL = "Black Duck Project Version Url";
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT = "Black Duck Component";
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_URL = "Black Duck Component Url";
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION = "Black Duck Component Version";
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION_URL = "Black Duck Component Version Url";
    public static final String BLACKDUCK_CUSTOM_FIELD_POLICY_RULE = "Black Duck Policy Rule";
    public static final String BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE = "Black Duck Policy Rule Overridable";
    public static final String BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_DESCRIPTION = "Black Duck Policy Rule Description";
    public static final String BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_URL = "Black Duck Policy Rule Url";
    public static final String BLACKDUCK_CUSTOM_FIELD_LICENSE_NAMES = "Black Duck Component Licenses";
    public static final String BLACKDUCK_CUSTOM_FIELD_LICENSE_URL = "Black Duck Component License Url";
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_USAGE = "Black Duck Component Usages";
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN = "Black Duck Component Origins";
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID = "Black Duck Component Origin IDs";
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME = "Black Duck Project Version Nickname";
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_OWNER = "Black Duck Project Owner";
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED = "Black Duck Project Version Last Updated";

    // Configuration object names visible only to administrators
    public static final String BLACKDUCK_JIRA_WORKFLOW = "BDS Hub PlugIn Workflow";
    public static final String BLACKDUCK_FIELD_CONFIGURATION_SCHEME_NAME = "Black Duck Field Configuration Scheme";
    public static final String BLACKDUCK_FIELD_CONFIGURATION = "Black Duck Field Configuration";
    public static final String BLACKDUCK_POLICY_SCREEN_SCHEME_NAME = "Black Duck Policy Screen Scheme";
    public static final String BLACKDUCK_SECURITY_SCREEN_SCHEME_NAME = "Black Duck Security Screen Scheme";
    public static final String BLACKDUCK_POLICY_SCREEN_NAME = "Black Duck Policy Screen";
    public static final String BLACKDUCK_SECURITY_SCREEN_NAME = "Black Duck Security Screen";
    public static final String BLACKDUCK_SCREEN_TAB = "Black Duck PlugIn Screen Tab";
    public static final String BLACKDUCK_WORKFLOW_STATUS_OPEN = "Open";
    public static final String BLACKDUCK_WORKFLOW_STATUS_RESOLVED = "Resolved";
    public static final String BLACKDUCK_WORKFLOW_STATUS_CLOSED = "Closed";
    public static final String BLACKDUCK_WORKFLOW_TRANSITION_REMOVE_OR_OVERRIDE = "Resolve";
    public static final String BLACKDUCK_WORKFLOW_TRANSITION_READD_OR_OVERRIDE_REMOVED = "Re-Open";
    public static final String BLACKDUCK_POLICY_VIOLATION_REOPEN = "Automatically re-opened in response to a new Black Duck Policy Violation on this project / component / rule";
    public static final String BLACKDUCK_POLICY_VIOLATION_RESOLVE = "Automatically resolved in response to a Black Duck Policy Override on this project / component / rule";
    public static final String BLACKDUCK_POLICY_VIOLATION_CLEARED_RESOLVE = "Automatically resolved in response to a Black Duck Policy Violation Cleared event on this project / component / rule";
    public static final String BLACKDUCK_POLICY_VIOLATION_DETECTED_AGAIN_COMMENT = "This Policy Violation was detected again by Black Duck.";
    public static final String BLACKDUCK_POLICY_VIOLATION_CLEARED_COMMENT = "This Policy Violation was cleared in Black Duck.";
    public static final String BLACKDUCK_POLICY_VIOLATION_OVERRIDDEN_COMMENT = "This Policy Violation was overridden in Black Duck.";
    public static final String BLACKDUCK_VULNERABILITY_REOPEN = "Automatically re-opened in response to new Black Duck vulnerabilities on this project from this component";
    public static final String BLACKDUCK_VULNERABILITY_RESOLVE = "Automatically resolved; Black Duck reports no remaining vulnerabilities on this project from this component";
    public static final String BLACKDUCK_AVATAR_IMAGE_FILENAME_VULNERABILITY = "180 - duck_vulnerability.png";
    public static final String BLACKDUCK_AVATAR_IMAGE_PATH_VULNERABILITY = "/images/" + BLACKDUCK_AVATAR_IMAGE_FILENAME_VULNERABILITY;
    public static final String BLACKDUCK_AVATAR_IMAGE_FILENAME_POLICY = "180 - duck_policy.png";
    public static final String BLACKDUCK_AVATAR_IMAGE_PATH_POLICY = "/images/" + BLACKDUCK_AVATAR_IMAGE_FILENAME_POLICY;
    public static final String ISSUE_PROPERTY_KEY_NAME_VALUE_SEPARATOR = "=";
    public static final String ISSUE_PROPERTY_KEY_NAME_VALUE_PAIR_SEPARATOR = "|";
    public static final String ISSUE_PROPERTY_KEY_ISSUE_TYPE_NAME = "t";
    public static final String ISSUE_PROPERTY_KEY_ISSUE_TYPE_VALUE_POLICY = "p";
    public static final String ISSUE_PROPERTY_KEY_ISSUE_TYPE_VALUE_VULNERABILITY = "v";
    public static final String ISSUE_PROPERTY_KEY_JIRA_PROJECT_ID_NAME = "jp";
    public static final String ISSUE_PROPERTY_KEY_BLACKDUCK_PROJECT_VERSION_REL_URL_HASHED_NAME = "hpv";
    public static final String ISSUE_PROPERTY_KEY_BLACKDUCK_COMPONENT_REL_URL_HASHED_NAME = "hc";
    public static final String ISSUE_PROPERTY_KEY_BLACKDUCK_COMPONENT_VERSION_REL_URL_HASHED_NAME = "hcv";
    public static final String ISSUE_PROPERTY_KEY_BLACKDUCK_POLICY_RULE_REL_URL_HASHED_NAME = "hr";
    public static final String ISSUE_PROPERTY_ENTITY_NAME = "IssueProperty";
    public static final String VULNERABLE_COMPONENTS_LINK_NAME = "vulnerable-components";
    public static final String FIELD_COPY_MAPPING_WILDCARD = "*";
    public static final String VERSIONS_FIELD_ID = "versions";
    public static final String COMPONENTS_FIELD_ID = "components";

    // i18n.properties file location
    public static final String PROPERTY_FILENAME = "com/blackducksoftware/integration/jira/i18n_4.properties";

    // JIRA Custom Field display name labels
    private static final String BLACKDUCK_CUSTOM_FIELD_PREFIX = "blackduck.integration.jira.issue.custom";
    private static final String BLACKDUCK_CUSTOM_FIELD_SUFFIX = ".label";

    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_DISPLAYNAMEPROPERTY = BLACKDUCK_CUSTOM_FIELD_PREFIX + ".project" + BLACKDUCK_CUSTOM_FIELD_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_DISPLAYNAMEPROPERTY = BLACKDUCK_CUSTOM_FIELD_PREFIX + ".project.version" + BLACKDUCK_CUSTOM_FIELD_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_DISPLAYNAMEPROPERTY = BLACKDUCK_CUSTOM_FIELD_PREFIX + ".component" + BLACKDUCK_CUSTOM_FIELD_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION_DISPLAYNAMEPROPERTY = BLACKDUCK_CUSTOM_FIELD_PREFIX + ".component.version" + BLACKDUCK_CUSTOM_FIELD_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_DISPLAYNAMEPROPERTY = BLACKDUCK_CUSTOM_FIELD_PREFIX + ".policy.rule" + BLACKDUCK_CUSTOM_FIELD_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE_DISPLAYNAMEPROPERTY = BLACKDUCK_CUSTOM_FIELD_PREFIX + ".policy.rule.overridable" + BLACKDUCK_CUSTOM_FIELD_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_LICENSE_NAMES_DISPLAYNAMEPROPERTY = BLACKDUCK_CUSTOM_FIELD_PREFIX + ".licenses" + BLACKDUCK_CUSTOM_FIELD_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_USAGE_DISPLAYNAMEPROPERTY = BLACKDUCK_CUSTOM_FIELD_PREFIX + ".component.usage" + BLACKDUCK_CUSTOM_FIELD_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_DISPLAYNAMEPROPERTY = BLACKDUCK_CUSTOM_FIELD_PREFIX + ".component.origin" + BLACKDUCK_CUSTOM_FIELD_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID_DISPLAYNAMEPROPERTY = BLACKDUCK_CUSTOM_FIELD_PREFIX + ".component.origin.id" + BLACKDUCK_CUSTOM_FIELD_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME_DISPLAYNAMEPROPERTY = BLACKDUCK_CUSTOM_FIELD_PREFIX + ".project.version.nickname" + BLACKDUCK_CUSTOM_FIELD_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_OWNER_DISPLAYNAMEPROPERTY = BLACKDUCK_CUSTOM_FIELD_PREFIX + ".project.owner" + BLACKDUCK_CUSTOM_FIELD_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED_DISPLAYNAMEPROPERTY = BLACKDUCK_CUSTOM_FIELD_PREFIX + ".project.version.last.updated" + BLACKDUCK_CUSTOM_FIELD_SUFFIX;

    // Long names for fields
    private static final String BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX = ".long";

    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_DISPLAYNAMEPROPERTY_LONG = BLACKDUCK_CUSTOM_FIELD_PROJECT_DISPLAYNAMEPROPERTY + BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_DISPLAYNAMEPROPERTY_LONG = BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_DISPLAYNAMEPROPERTY + BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_DISPLAYNAMEPROPERTY_LONG = BLACKDUCK_CUSTOM_FIELD_COMPONENT_DISPLAYNAMEPROPERTY + BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION_DISPLAYNAMEPROPERTY_LONG = BLACKDUCK_CUSTOM_FIELD_COMPONENT_VERSION_DISPLAYNAMEPROPERTY + BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_DISPLAYNAMEPROPERTY_LONG = BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_DISPLAYNAMEPROPERTY + BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE_DISPLAYNAMEPROPERTY_LONG = BLACKDUCK_CUSTOM_FIELD_POLICY_RULE_OVERRIDABLE_DISPLAYNAMEPROPERTY + BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_LICENSE_NAMES_DISPLAYNAMEPROPERTY_LONG = BLACKDUCK_CUSTOM_FIELD_LICENSE_NAMES_DISPLAYNAMEPROPERTY + BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_USAGE_DISPLAYNAMEPROPERTY_LONG = BLACKDUCK_CUSTOM_FIELD_COMPONENT_USAGE_DISPLAYNAMEPROPERTY + BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_DISPLAYNAMEPROPERTY_LONG = BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_DISPLAYNAMEPROPERTY + BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID_DISPLAYNAMEPROPERTY_LONG = BLACKDUCK_CUSTOM_FIELD_COMPONENT_ORIGIN_ID_DISPLAYNAMEPROPERTY + BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME_DISPLAYNAMEPROPERTY_LONG = BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_NICKNAME_DISPLAYNAMEPROPERTY + BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_OWNER_DISPLAYNAMEPROPERTY_LONG = BLACKDUCK_CUSTOM_FIELD_PROJECT_OWNER_DISPLAYNAMEPROPERTY + BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX;
    public static final String BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED_DISPLAYNAMEPROPERTY_LONG = BLACKDUCK_CUSTOM_FIELD_PROJECT_VERSION_LAST_UPDATED_DISPLAYNAMEPROPERTY + BLACKDUCK_CUSTOM_FIELD_LONG_SUFFIX;

    // String used to join conjunctive and adjunctive licenses together into a string for display
    public static final String LICENSE_NAME_JOINER_OR = " OR ";
    public static final String LICENSE_NAME_JOINER_AND = " AND ";

    // Issue Property keys
    public static final String BLACKDUCK_ISSUE_PROPERTY_BOM_COMPONENT_KEY = "jiraPluginBomComponentKey";
    public static final String BLACKDUCK_ISSUE_PROPERTY_POLICY_RULE_KEY = "jiraPluginPolicyRuleKey";

    // Event Data Set key
    public static final String EVENT_DATA_SET_KEY_JIRA_EVENT_DATA = "jiraEventData";

    // Miscellaneous persistent data keys
    public static final String DATE_LAST_PHONED_HOME = "dateLastPhonedHome";
    public static final String BLACKDUCK_JIRA_ISSUE_LAST_COMMENT_KEY = "hubJiraLastCommentKey";
    public static final String BLACKDUCK_JIRA_ISSUE_LAST_BATCH_START_KEY = "hubJiralastBatchStartKey";

}
