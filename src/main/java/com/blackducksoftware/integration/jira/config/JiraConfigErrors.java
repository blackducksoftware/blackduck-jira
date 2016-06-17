package com.blackducksoftware.integration.jira.config;

public class JiraConfigErrors {

	public static final String HUB_SERVER_MISCONFIGURATION = "There was a problem with the Hub Server configuration. ";
	public static final String CHECK_HUB_SERVER_CONFIGURATION = "Please verify the Hub Server information is configured correctly. ";
	public static final String HUB_CONFIG_PLUGIN_MISSING = "Could not find the Hub Server configuration. Please verify the correct dependent Hub configuration plugin is installed. ";
	public static final String MAPPING_HAS_EMPTY_ERROR = "There are invalid mapping(s) with empty Project(s).";
	public static final String HUB_SERVER_NO_POLICY_SUPPORT_ERROR = "This version of the Hub does not support Policies.";
	public static final String NO_POLICY_RULES_FOUND_ERROR = "No Policy rules were found in the configured Hub server.";

	public static final String NO_INTERVAL_FOUND_ERROR = "No interval between checks was found.";
	public static final String INVALID_INTERVAL_FOUND_ERROR = "The interval must be greater than 0.";

	public static final String JIRA_PROJECT_NO_ISSUE_TYPES_FOUND_ERROR = "The Jira project does not have any issue types, we will not be able to create tickets for this project.";
	public static final String JIRA_PROJECT_MISSING_ISSUE_TYPES_ERROR = "The Jira project is missing the Bug issue type.";

}
