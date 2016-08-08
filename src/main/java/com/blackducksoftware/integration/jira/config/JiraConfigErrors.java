/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.jira.config;

public class JiraConfigErrors {

	public static final String HUB_SERVER_MISCONFIGURATION = "There was a problem with the Hub Server configuration. ";
	public static final String CHECK_HUB_SERVER_CONFIGURATION = "Please verify the Hub Server information is configured correctly. ";
	public static final String HUB_CONFIG_PLUGIN_MISSING = "Could not find the Hub Server configuration. Please verify the correct dependent Hub configuration plugin is installed. ";
	public static final String MAPPING_HAS_EMPTY_ERROR = "There are invalid mapping(s).";
	public static final String NO_JIRA_PROJECTS_FOUND = "Could not find any Jira Projects.";
	public static final String NO_HUB_PROJECTS_FOUND = "Could not find any Hub Projects for this User. This Hub user may not be assigned to any projects.";
	public static final String HUB_SERVER_NO_POLICY_SUPPORT_ERROR = "This version of the Hub does not support Policies.";
	public static final String NO_POLICY_RULES_FOUND_ERROR = "No Policy rules were found in the configured Hub server.";

	public static final String NO_INTERVAL_FOUND_ERROR = "No interval between checks was found.";
	public static final String INVALID_INTERVAL_FOUND_ERROR = "The interval must be greater than 0.";

	public static final String JIRA_PROJECT_NO_ISSUE_TYPES_FOUND_ERROR = "The Jira project does not have any issue types, we will not be able to create tickets for this project.";
	public static final String JIRA_PROJECT_MISSING_ISSUE_TYPES_ERROR = "The Jira project is missing the Task issue type.";

}
