package com.blackducksoftware.integration.jira.common;

import java.util.Map;

import com.atlassian.jira.issue.fields.CustomField;

public class TicketInfoFromSetup {

	private Map<String, CustomField> customFields;

	public void setCustomFields(final Map<String, CustomField> customFields) {
		this.customFields = customFields;
	}

	public Map<String, CustomField> getCustomFields() {
		return customFields;
	}

}
