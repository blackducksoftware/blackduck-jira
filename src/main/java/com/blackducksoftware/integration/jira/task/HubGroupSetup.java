package com.blackducksoftware.integration.jira.task;

import org.apache.log4j.Logger;

import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.security.groups.GroupManager;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;

public class HubGroupSetup {
	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));

	private final JiraSettingsService settingService;

	private final GroupManager groupManager;

	public HubGroupSetup(final JiraSettingsService settingService, final GroupManager groupManager) {
		this.settingService = settingService;
		this.groupManager = groupManager;
	}

	public void addHubJiraGroupToJira() {
		try {
			if (!groupManager.groupExists(HubJiraConstants.HUB_JIRA_GROUP)) {
				groupManager.createGroup(HubJiraConstants.HUB_JIRA_GROUP);
				logger.debug("Created the Group : " + HubJiraConstants.HUB_JIRA_GROUP);
			}
		} catch (OperationNotPermittedException | InvalidGroupException e) {
			logger.error("Failed to create the Group : " + HubJiraConstants.HUB_JIRA_GROUP, e);
			settingService.addHubError(e, "addHubJiraGroup");
		}
	}
}
