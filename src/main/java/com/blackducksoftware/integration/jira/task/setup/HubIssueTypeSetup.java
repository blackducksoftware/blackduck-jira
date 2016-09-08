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
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutSchemeEntity;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeManager;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntityImpl;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeManager;
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

	public void addIssueTypesToProjectIssueTypeScheme(final Project jiraProject, final List<IssueType> hubIssueTypes) {
		// Get Project's Issue Type Scheme
		final FieldConfigScheme issueTypeScheme = getProjectIssueTypeScheme(jiraProject);

		final Collection<IssueType> origIssueTypeObjects = getProjectIssueTypes(jiraProject);
		final Collection<String> issueTypeIds = new ArrayList<>();
		for (final IssueType origIssueTypeObject : origIssueTypeObjects) {
			issueTypeIds.add(origIssueTypeObject.getId());
		}

		// Add BDS Issue Types to it
		for (final IssueType bdIssueType : hubIssueTypes) {
			if (!origIssueTypeObjects.contains(bdIssueType)) {
				logger.debug("Adding issue type " + bdIssueType.getName() + " to issue type scheme "
						+ issueTypeScheme.getName());
				issueTypeIds.add(bdIssueType.getId());
				updateIssueTypeScheme(issueTypeScheme, issueTypeIds);
			} else {
				logger.debug("Issue type " + bdIssueType.getName() + " is already on issue type scheme "
						+ issueTypeScheme.getName());
			}
		}
		logger.debug("Project now has " + issueTypeIds.size() + " issue types");
		for (final String newIssueTypeId : issueTypeIds) {
			logger.debug("\tIssueTypeId: " + newIssueTypeId);
		}
	}

	public void addIssueTypesToProjectIssueTypeScreenSchemes(final Project project,
			final Map<IssueType, FieldScreenScheme> screenSchemesByIssueType) {

		final IssueTypeScreenScheme issueTypeScreenScheme = jiraServices.getIssueTypeScreenSchemeManager()
				.getIssueTypeScreenScheme(project);
		logger.debug("addIssueTypesToProjectIssueTypeScreenSchemes(): Project " + project.getName()
		+ ": Issue Type Screen Scheme: " + issueTypeScreenScheme.getName());

		final IssueTypeScreenSchemeManager issueTypeScreenSchemeManager = jiraServices
				.getIssueTypeScreenSchemeManager();
		final FieldScreenSchemeManager fieldScreenSchemeManager = jiraServices
				.getFieldScreenSchemeManager();
		final ConstantsManager constantsManager = jiraServices.getConstantsManager();

		final List<IssueType> origIssueTypes = getExistingIssueTypes(issueTypeScreenScheme);

		for (final IssueType issueType : screenSchemesByIssueType.keySet()) {
			if (origIssueTypes.contains(issueType)) {
				logger.debug("Issue Type " + issueType.getName() + " is already on Issue Type Screen Scheme "
						+ issueTypeScreenScheme.getName());
				continue;
			}
			final FieldScreenScheme screenScheme = screenSchemesByIssueType.get(issueType);
			logger.debug("Associating issue type " + issueType.getName() + " with screen scheme "
					+ screenScheme.getName() + " on issue type screen scheme " + issueTypeScreenScheme.getName());
			final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(
					issueTypeScreenSchemeManager, (GenericValue) null, fieldScreenSchemeManager, constantsManager);
			issueTypeScreenSchemeEntity.setIssueTypeId(issueType.getId());
			issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenSchemeManager.getFieldScreenScheme(screenScheme
					.getId()));
			issueTypeScreenScheme.addEntity(issueTypeScreenSchemeEntity);

		}
		logger.debug("Performing store() on " + issueTypeScreenScheme.getName());
		issueTypeScreenScheme.store();

	}

	private List<IssueType> getExistingIssueTypes(final IssueTypeScreenScheme issueTypeScreenScheme) {
		final List<IssueType> origIssueTypes = new ArrayList<>();
		final Collection<IssueTypeScreenSchemeEntity> origIssueTypeScreenSchemeEntities = issueTypeScreenScheme
				.getEntities();
		if (origIssueTypeScreenSchemeEntities == null) {
			return origIssueTypes;
		}
		for (final IssueTypeScreenSchemeEntity origIssueTypeScreenSchemeEntity : origIssueTypeScreenSchemeEntities) {
			final IssueType origIssueType = origIssueTypeScreenSchemeEntity.getIssueTypeObject();
			if (origIssueType == null) {
				logger.debug("Issue Type <null> found on Issue Type Screen Scheme " + issueTypeScreenScheme.getName());
			} else {
				logger.debug("Issue Type " + origIssueType.getName() + " found on Issue Type Screen Scheme "
						+ issueTypeScreenScheme.getName());
				origIssueTypes.add(origIssueType);
			}
		}
		return origIssueTypes;
	}

	public void associateIssueTypesWithFieldConfigurationsOnProjectFieldConfigurationScheme(final Project project,
			final FieldLayoutScheme bdsFieldConfigurationScheme, final List<IssueType> issueTypes, final FieldLayout fieldConfiguration) {

		final FieldConfigurationScheme projectFieldConfigurationScheme = getProjectFieldConfigScheme(project);
		if (projectFieldConfigurationScheme == null) {
			logger.debug("Project " + project.getName() + ": Field Configuration Scheme: <null>");
		} else {
			logger.debug("Project " + project.getName() + ": Field Configuration Scheme: "
					+ projectFieldConfigurationScheme.getName());
		}
		if (projectFieldConfigurationScheme == null) {
			// Associate the BDS Field Configuration Scheme
			// (bdsFieldConfigurationScheme) with the
			// Project
			logger.debug("Replacing the project's Field Configuration Scheme with "
					+ bdsFieldConfigurationScheme.getName());
			jiraServices.getFieldLayoutManager().addSchemeAssociation(project, bdsFieldConfigurationScheme.getId());
		}
		modifyProjectFieldConfigurationScheme(issueTypes, fieldConfiguration, projectFieldConfigurationScheme);
	}

	private void modifyProjectFieldConfigurationScheme(final List<IssueType> issueTypes,
			final FieldLayout fieldConfiguration, final FieldConfigurationScheme projectFieldConfigurationScheme) {
		// Modify projectFieldConfigurationScheme
		logger.debug("Modifying the project's Field Configuration Scheme");
		final FieldLayoutScheme fieldLayoutScheme = getFieldLayoutScheme(projectFieldConfigurationScheme);
		if (issueTypes != null && !issueTypes.isEmpty()) {
			for (final IssueType issueType : issueTypes) {

				boolean issueTypeAlreadyMappedToOurFieldConfig = false;
				final Collection<FieldLayoutSchemeEntity> fieldLayoutSchemeEntities = fieldLayoutScheme.getEntities();
				if (fieldLayoutSchemeEntities != null) {
					for (final FieldLayoutSchemeEntity fieldLayoutSchemeEntity : fieldLayoutSchemeEntities) {
						final IssueType existingIssueType = fieldLayoutSchemeEntity.getIssueTypeObject();
						if (existingIssueType == issueType
								&& fieldLayoutSchemeEntity.getFieldLayoutId().equals(fieldConfiguration.getId())) {
							issueTypeAlreadyMappedToOurFieldConfig = true;
							break;
						}
					}
				}
				if (issueTypeAlreadyMappedToOurFieldConfig) {
					logger.debug("Issue Type " + issueType.getName()
					+ " is already associated with Field Configuration Scheme "
					+ projectFieldConfigurationScheme.getName());
					continue;
				}

				final FieldLayoutSchemeEntity fieldLayoutSchemeEntity = jiraServices
						.getFieldLayoutManager()
						.createFieldLayoutSchemeEntity(fieldLayoutScheme, issueType.getId(), fieldConfiguration.getId());

				logger.debug("Adding to fieldLayoutScheme: " + fieldLayoutScheme.getName() + ": issueType "
						+ issueType.getName() + " ==> field configuration " + fieldConfiguration.getName());
				fieldLayoutScheme.addEntity(fieldLayoutSchemeEntity);
			}
		}
		logger.debug("Storing Field Configuration Scheme " + fieldLayoutScheme.getName());
		fieldLayoutScheme.store();
	}

	private FieldLayoutScheme getFieldLayoutScheme(final FieldConfigurationScheme fieldConfigurationScheme) {
		final FieldLayoutScheme fls = jiraServices.getFieldLayoutManager().getMutableFieldLayoutScheme(
				fieldConfigurationScheme.getId());
		logger.info("getFieldLayoutScheme(): FieldConfigurationScheme: " + fieldConfigurationScheme.getName()
		+ " ==> FieldLayoutScheme: " + fls.getName());
		return fls;
	}

	private FieldConfigurationScheme getProjectFieldConfigScheme(final Project project) {
		FieldConfigurationScheme projectFieldConfigScheme = null;
		try {
			logger.debug("Getting field configuration scheme for project " + project.getName() + " [ID: "
					+ project.getId() + "]");
			projectFieldConfigScheme = jiraServices.getFieldLayoutManager().getFieldConfigurationSchemeForProject(
					project.getId());
			logger.debug("\tprojectFieldConfigScheme: " + projectFieldConfigScheme);
		} catch (final Exception e) {
			logger.error(e.getMessage());
		}
		if (projectFieldConfigScheme == null) {
			logger.debug("Project " + project.getName() + " field config scheme: Default Field Configuration Scheme");
		} else {
			logger.debug("Project " + project.getName() + " field config scheme: " + projectFieldConfigScheme.getName());
		}

		return projectFieldConfigScheme;
	}

	private void updateIssueTypeScheme(final FieldConfigScheme issueTypeScheme, final Collection<String> issueTypeIds) {
		jiraServices.getIssueTypeSchemeManager().update(issueTypeScheme, issueTypeIds);
	}

	private Collection<IssueType> getProjectIssueTypes(final Project jiraProject) {
		return jiraServices.getIssueTypeSchemeManager().getIssueTypesForProject(jiraProject);
	}

	private FieldConfigScheme getProjectIssueTypeScheme(final Project project) {
		return jiraServices.getIssueTypeSchemeManager().getConfigScheme(project);
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
