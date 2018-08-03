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
package com.blackducksoftware.integration.jira.config;

public class PluginConfigKeys {
    public static final String BLACKDUCK_CONFIG_JIRA_KEY_PREFIX = "com.blackducksoftware.integration.hub.jira";
    public static final String BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS = BLACKDUCK_CONFIG_JIRA_KEY_PREFIX + ".intervalBetweenChecks";
    public static final String BLACKDUCK_CONFIG_JIRA_PROJECT_MAPPINGS_JSON = BLACKDUCK_CONFIG_JIRA_KEY_PREFIX + ".hubProjectMappings";
    public static final String BLACKDUCK_CONFIG_FIELD_COPY_MAPPINGS_JSON = BLACKDUCK_CONFIG_JIRA_KEY_PREFIX + ".fieldCopyMappings";
    public static final String BLACKDUCK_CONFIG_JIRA_FIRST_SAVE_TIME = BLACKDUCK_CONFIG_JIRA_KEY_PREFIX + ".firstSaveTime";
    public static final String BLACKDUCK_CONFIG_JIRA_POLICY_RULES_JSON = BLACKDUCK_CONFIG_JIRA_KEY_PREFIX + ".policyRules";
    public static final String BLACKDUCK_CONFIG_CREATE_VULN_ISSUES_CHOICE = BLACKDUCK_CONFIG_JIRA_KEY_PREFIX + ".createVulnIssuesChoice";
    public static final String BLACKDUCK_CONFIG_LAST_RUN_DATE = BLACKDUCK_CONFIG_JIRA_KEY_PREFIX + ".lastRunDate";
    public static final String BLACKDUCK_CONFIG_JIRA_ADMIN_USER = BLACKDUCK_CONFIG_JIRA_KEY_PREFIX + ".jiraUser";
    public static final String BLACKDUCK_CONFIG_JIRA_ISSUE_CREATOR_USER = BLACKDUCK_CONFIG_JIRA_KEY_PREFIX + ".creator";
    public static final String BLACKDUCK_CONFIG_CREATOR_CANDIDATES_JSON = BLACKDUCK_CONFIG_JIRA_KEY_PREFIX + ".creatorCandidates";
    public static final String BLACKDUCK_CONFIG_GROUPS = "com.blackducksoftware.integration.hub.configuration.hubGroups";

}
