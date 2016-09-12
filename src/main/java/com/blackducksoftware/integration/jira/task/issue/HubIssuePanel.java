package com.blackducksoftware.integration.jira.task.issue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;

public class HubIssuePanel extends AbstractJiraContextProvider {

	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

	private final CustomFieldManager customFieldManager;

	public HubIssuePanel(final CustomFieldManager customFieldManager) {
		this.customFieldManager = customFieldManager;
	}

	@Override
	public Map<String, String> getContextMap(final User user, final JiraHelper jiraHelper) {
		final Map<String, String> contextMap = new HashMap();
		final Issue currentIssue = (Issue)
				jiraHelper.getContextParams().get("issue");
		if (currentIssue != null) {
			final String hubProject = getCustomFieldValue(currentIssue, customFieldManager,
					HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT);
			if (hubProject != null) {
				contextMap.put("hubProject", hubProject);
			}
			final String hubProjectVersion = getCustomFieldValue(currentIssue, customFieldManager,
					HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION);
			if (hubProjectVersion != null) {
				contextMap.put("hubProjectVersion", hubProjectVersion);
			}
			final String hubComponent = getCustomFieldValue(currentIssue, customFieldManager,
					HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT);
			if (hubComponent != null) {
				contextMap.put("hubComponent", hubComponent);
			}
			final String hubComponentVersion = getCustomFieldValue(currentIssue, customFieldManager,
					HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION);
			if (hubComponentVersion != null) {
				contextMap.put("hubComponentVersion", hubComponentVersion);
			}
			final String hubPolicyRule = getCustomFieldValue(currentIssue, customFieldManager,
					HubJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE);
			if (hubPolicyRule != null) {
				contextMap.put("hubPolicyRule", hubPolicyRule);
			}
		}
		return contextMap;
	}

	private String getCustomFieldValue(final Issue currentIssue, final CustomFieldManager customFieldManager,
			final String fieldName) {
		final CustomField hubCustomField = customFieldManager.getCustomFieldObjectByName(fieldName);
		if (hubCustomField != null) {
			final String hubFieldValue = (String) currentIssue.getCustomFieldValue(hubCustomField);
			if (StringUtils.isNotBlank(hubFieldValue)) {
				return hubFieldValue;
			}
		}
		return null;
	}

}
