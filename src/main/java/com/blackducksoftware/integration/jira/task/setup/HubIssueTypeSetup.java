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
package com.blackducksoftware.integration.jira.task.setup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;
import com.blackducksoftware.integration.jira.common.HubJiraLogger;
import com.blackducksoftware.integration.jira.exception.JiraException;
import com.blackducksoftware.integration.jira.task.JiraSettingsService;
import com.blackducksoftware.integration.jira.task.issue.JiraServices;

public class HubIssueTypeSetup {

	private final HubJiraLogger logger = new HubJiraLogger(Logger.getLogger(this.getClass().getName()));
	private final JiraServices jiraServices;
	private final JiraSettingsService settingService;

	private final Collection<IssueType> issueTypes;

	public HubIssueTypeSetup(final JiraServices jiraServices, final JiraSettingsService settingService,
			final Collection<IssueType> issueTypes) {
		this.jiraServices = jiraServices;
		this.settingService = settingService;
		this.issueTypes = issueTypes;
	}

	// create our issueTypes AND add them to each Projects workflow
	// scheme before we try addWorkflowToProjectsWorkflowScheme

	public List<IssueType> addIssueTypesToJira() throws JiraException {
		final List<IssueType> bdIssueTypes = new ArrayList<>();
		final List<String> existingBdIssueTypeNames = new ArrayList<>();

		for (final IssueType issueType : issueTypes) {
			if (issueType.getName().equals(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE)
					|| issueType.getName().equals(HubJiraConstants.HUB_VULNERABILITY_ISSUE)) {
				bdIssueTypes.add(issueType);
				existingBdIssueTypeNames.add(issueType.getName());
			}
		}
		if (!existingBdIssueTypeNames.contains(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE)) {
			final Long avatarId = getBlackduckAvatarId();
			final IssueType issueType = createIssueType(HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE,
					HubJiraConstants.HUB_POLICY_VIOLATION_ISSUE, avatarId);
			bdIssueTypes.add(issueType);
		}
		if (!existingBdIssueTypeNames.contains(HubJiraConstants.HUB_VULNERABILITY_ISSUE)) {
			final Long avatarId = getBlackduckAvatarId();
			final IssueType issueType = createIssueType(HubJiraConstants.HUB_VULNERABILITY_ISSUE,
					HubJiraConstants.HUB_VULNERABILITY_ISSUE, avatarId);
			bdIssueTypes.add(issueType);
		}

		return bdIssueTypes;
	}

	public void addIssueTypesToProject(final Project jiraProject, final List<IssueType> hubIssueTypes) {
		// TODO
	}

	private Long getBlackduckAvatarId() {
		// TODO: Get Black Duck Avatar, not anon
		return jiraServices.getAvatarManager().getAnonymousAvatarId();
	}

	private IssueType createIssueType(final String name, final String description, final Long avatarId)
			throws JiraException {
		logger.debug("Creating new issue type: " + name);
		IssueType newIssueType = null;
		try {
			newIssueType = jiraServices.getConstantsManager().insertIssueType(name, 0L, null, description,
					avatarId);
		} catch (final CreateException e) {
			throw new JiraException("Error creating Issue Type " + name + ": " + e.getMessage(), e);
		}
		logger.info("Created new issue type: " + newIssueType.getName() + " (id: " + newIssueType.getId());

		return newIssueType;
	}

}
