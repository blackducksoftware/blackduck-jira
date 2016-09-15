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
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.jira.common;

public class HubJiraConstants {
	public final static String HUB_JIRA_GROUP = "hub-jira";
	public final static String HUB_JIRA_ERROR = HUB_JIRA_GROUP + "-ticket-error";
	public final static String HUB_JIRA_WORKFLOW_RESOURCE = "Hub Workflow.xml";
	public final static String HUB_JIRA_WORKFLOW = "Hub Workflow";

	public final static String HUB_POLICY_VIOLATION_ISSUE = "Hub Policy Violation";
	public final static String HUB_VULNERABILITY_ISSUE = "Hub Security Vulnerability";

	public final static String HUB_CUSTOM_FIELD_PROJECT = "Hub Project";
	public final static String HUB_CUSTOM_FIELD_PROJECT_VERSION = "Hub Project Version";
	public final static String HUB_CUSTOM_FIELD_COMPONENT = "Hub Component";
	public final static String HUB_CUSTOM_FIELD_COMPONENT_VERSION = "Hub Component Version";
	public final static String HUB_CUSTOM_FIELD_POLICY_RULE = "Hub Policy Rule";

	public final static String HUB_WORKFLOW_STATUS_OPEN = "Open";
	public final static String HUB_WORKFLOW_STATUS_RESOLVED = "Resolved";
	public final static String HUB_WORKFLOW_TRANSITION_REMOVE_OR_OVERRIDE = "Resolve";
	public final static String HUB_WORKFLOW_TRANSITION_READD_OR_OVERRIDE_REMOVED = "Re-Open";

	public final static String HUB_POLICY_VIOLATION_REOPEN = "Automatically re-opened in response to a new Black Duck Hub Policy Violation on this project / component / rule";
	public final static String HUB_POLICY_VIOLATION_RESOLVE = "Automatically resolved in response to a Black Duck Hub Policy Override on this project / component / rule";
}
