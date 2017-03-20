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
package com.blackducksoftware.integration.jira.config;

public class JiraConfigErrors {

    public static final String HUB_SERVER_MISCONFIGURATION = "There was a problem with the Hub Server configuration. ";

    public static final String CHECK_HUB_SERVER_CONFIGURATION = "Please verify the Hub Server information is configured correctly. ";

    public static final String HUB_CONFIG_PLUGIN_MISSING = "The Hub Server connection details have not been configured.";

    public static final String MAPPING_HAS_EMPTY_ERROR = "There are invalid mapping(s).";

    public static final String NO_JIRA_PROJECTS_FOUND = "Could not find any JIRA Projects.";

    public static final String NO_HUB_PROJECTS_FOUND = "Could not find any Hub Projects for this User. This Hub user may not be assigned to any projects.";

    public static final String NO_CREATOR_CANDIDATES_FOUND = "Could not find any JIRA users eligible to be issue creators. Make sure Configuration Access has been set up, and that there are users in those groups.";

    public static final String HUB_SERVER_NO_POLICY_SUPPORT_ERROR = "This version of the Hub does not support Policies.";

    public static final String NO_POLICY_RULES_FOUND_ERROR = "No Policy rules were found in the configured Hub server.";

    public static final String POLICY_RULE_URL_ERROR = "Error getting policy rule URL";

    public static final String NO_INTERVAL_FOUND_ERROR = "No interval between checks was found.";

    public static final String NO_CREATOR_SPECIFIED_ERROR = "The 'JIRA Issue Creator' field is required.";

    public static final String UNAUTHORIZED_CREATOR_ERROR = "The user specified as the issue creator is not a valid JIRA user or has not been granted access to the Hub JIRA plugin.";

    public static final String INVALID_INTERVAL_FOUND_ERROR = "The interval must be greater than 0.";

    public static final String NON_SYSTEM_ADMINS_CANT_CHANGE_GROUPS = "Only JIRA system admins can update the list of JIRA groups that can access the Hub JIRA configuration.";

    public static final String NO_VALID_FIELD_CONFIGURATIONS = "At least one valid field configuration is required in order to save.";

    public static final String FIELD_CONFIGURATION_INVALID_SOURCE_FIELD = "The Hub Field in one of the field mappings is invalid.";

    public static final String FIELD_CONFIGURATION_INVALID_TARGET_FIELD = "The JIRA Field in one of the field mappings is invalid.";
}
