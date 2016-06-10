package com.blackducksoftware.integration.jira.config;

public enum PolicyRuleConditionFieldEnum {
	PROJECT_TIER("Project Tier"),
	VERSION_PHASE("Version Phase"),
	VERSION_DISTRIBUTION("Version Distribution"),
	SINGLE_VERSION("Component"),
	COMPONENT_USAGE("Component Usage"),
	LICENSE_FAMILY("License Family"),
	SINGLE_LICENSE("License"),
	NEWER_VERSIONS_COUNT("Newer Versions Count"),
	HIGH_SEVERITY_VULN_COUNT("High Severity Vulnerability Count"),
	MEDIUM_SEVERITY_VULN_COUNT("Medium Severity Vulnerability Count"),
	LOW_SEVERITY_VULN_COUNT("Low Severity Vulnerability Count");

	private final String displayValue;

	private PolicyRuleConditionFieldEnum(final String displayValue) {
		this.displayValue = displayValue;
	}

	public String getDisplayValue() {
		return displayValue;
	}

}
