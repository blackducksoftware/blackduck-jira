/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
 * https://www.synopsys.com/
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
package com.blackducksoftware.integration.jira.workflow.setup;

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
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.common.exception.JiraException;
import com.blackducksoftware.integration.jira.dal.PluginErrorAccessor;
import com.blackducksoftware.integration.jira.web.JiraServices;

public class BlackDuckIssueTypeSetup {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final JiraServices jiraServices;
    private final PluginErrorAccessor pluginErrorAccessor;
    private final Collection<IssueType> issueTypes;
    private final ApplicationUser jiraUser;
    private final BlackDuckAvatars blackDuckAvatars;

    public BlackDuckIssueTypeSetup(final JiraServices jiraServices, final PluginErrorAccessor pluginErrorAccessor, final Collection<IssueType> issueTypes, final String jiraUserName) throws ConfigurationException {
        this.jiraServices = jiraServices;
        this.pluginErrorAccessor = pluginErrorAccessor;
        this.issueTypes = issueTypes;

        if (jiraUserName != null) {
            logger.debug("Looking up user from config info: " + jiraUserName);
            jiraUser = jiraServices.getUserManager().getUserByName(jiraUserName);
        } else {
            logger.debug("Getting user from AuthContext");
            jiraUser = jiraServices.getAuthContext().getLoggedInUser();
        }
        if (jiraUser == null) {
            logger.error("User is null");
            throw new ConfigurationException("User is null");
        }
        logger.debug("User name: " + jiraUser.getName() + "; ID: " + jiraUser.getKey());

        blackDuckAvatars = new BlackDuckAvatars(jiraServices, jiraUser);
    }

    public List<IssueType> addBdsIssueTypesToJira() {
        final List<IssueType> bdIssueTypes = new ArrayList<>();
        try {
            final List<String> existingBdIssueTypeNames = collectExistingBdsIssueTypeNames(bdIssueTypes);
            addBdsIssueType(bdIssueTypes, existingBdIssueTypeNames, BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE);
            addBdsIssueType(bdIssueTypes, existingBdIssueTypeNames, BlackDuckJiraConstants.BLACKDUCK_SECURITY_POLICY_VIOLATION_ISSUE);
            addBdsIssueType(bdIssueTypes, existingBdIssueTypeNames, BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE);
        } catch (final Exception e) {
            logger.error(e);
            pluginErrorAccessor.addBlackDuckError(e, "addIssueTypesToJira()");
        }

        return bdIssueTypes;
    }

    private List<String> collectExistingBdsIssueTypeNames(final List<IssueType> bdIssueTypes) {
        final List<String> existingBdIssueTypeNames = new ArrayList<>();
        for (final IssueType issueType : issueTypes) {
            final String issueTypeName = issueType.getName();
            if (issueTypeName.equals(BlackDuckJiraConstants.BLACKDUCK_POLICY_VIOLATION_ISSUE) || issueTypeName.equals(BlackDuckJiraConstants.BLACKDUCK_SECURITY_POLICY_VIOLATION_ISSUE)
                    || issueTypeName.equals(BlackDuckJiraConstants.BLACKDUCK_VULNERABILITY_ISSUE)) {
                bdIssueTypes.add(issueType);
                existingBdIssueTypeNames.add(issueTypeName);
            }
        }
        return existingBdIssueTypeNames;
    }

    private void addBdsIssueType(final List<IssueType> bdIssueTypes, final List<String> existingBdIssueTypeNames, final String issueTypeName) throws JiraException {
        final Long avatarId;
        if (!existingBdIssueTypeNames.contains(issueTypeName)) {
            avatarId = blackDuckAvatars.getAvatarId(issueTypeName);
            logger.debug("Creating issue type " + issueTypeName + " with avatar ID " + avatarId);
            final IssueType issueType = createIssueType(issueTypeName, issueTypeName, avatarId);
            bdIssueTypes.add(issueType);
        }
    }

    // This method is to simplify upgrading
    private void renameBdsIssueType(final List<IssueType> bdIssueTypes, final int index, final String newName, final String newDescription) {
        final IssueType oldIssueType = bdIssueTypes.get(index);
        jiraServices.getConstantsManager().updateIssueType(oldIssueType.getId(), newName, oldIssueType.getSequence(), null, newDescription, oldIssueType.getAvatar().getId());
        final String constantType = ConstantsManager.CONSTANT_TYPE.ISSUE_TYPE.getType();
        final IssueType updatedIssueType = (IssueType) jiraServices.getConstantsManager().getIssueConstantByName(constantType, newName);
        bdIssueTypes.remove(index);
        bdIssueTypes.add(index, updatedIssueType);
    }

    public void addIssueTypesToProjectIssueTypeScheme(final Project jiraProject, final List<IssueType> blackDuckIssueTypes) {
        // Get Project's Issue Type Scheme
        try {
            final FieldConfigScheme issueTypeScheme = jiraServices.getIssueTypeSchemeManager().getConfigScheme(jiraProject);

            final Collection<IssueType> origIssueTypeObjects = jiraServices.getIssueTypeSchemeManager().getIssueTypesForProject(jiraProject);
            final Collection<String> issueTypeIds = new ArrayList<>();
            for (final IssueType origIssueTypeObject : origIssueTypeObjects) {
                issueTypeIds.add(origIssueTypeObject.getId());
            }

            // Add BDS Issue Types to it
            boolean changesMadeToIssueTypeScheme = false;
            for (final IssueType bdIssueType : blackDuckIssueTypes) {
                if (!origIssueTypeObjects.contains(bdIssueType)) {
                    logger.debug("Adding issue type " + bdIssueType.getName() + " to issue type scheme " + issueTypeScheme.getName());
                    issueTypeIds.add(bdIssueType.getId());
                    changesMadeToIssueTypeScheme = true;

                } else {
                    logger.debug("Issue type " + bdIssueType.getName() + " is already on issue type scheme " + issueTypeScheme.getName());
                }
            }
            if (changesMadeToIssueTypeScheme) {
                logger.debug("Updating Issue Type Scheme " + issueTypeScheme.getName());
                jiraServices.getIssueTypeSchemeManager().update(issueTypeScheme, issueTypeIds);
            } else {
                logger.debug("Issue Type Scheme " + issueTypeScheme.getName() + " already included Black Duck Issue Types");
            }
        } catch (final Exception e) {
            logger.error(e);
            String jiraProjectName = null;
            if (jiraProject != null) {
                jiraProjectName = jiraProject.getName();
            }
            pluginErrorAccessor.addBlackDuckError(e, null, null, jiraProjectName, null, null, "addIssueTypesToProjectIssueTypeScheme()");
        }
    }

    public void addIssueTypesToProjectIssueTypeScreenSchemes(final Project project, final Map<IssueType, FieldScreenScheme> screenSchemesByIssueType) {
        final IssueTypeScreenScheme issueTypeScreenScheme = jiraServices.getIssueTypeScreenSchemeManager().getIssueTypeScreenScheme(project);
        logger.debug("addIssueTypesToProjectIssueTypeScreenSchemes(): Project " + project.getName() + ": Issue Type Screen Scheme: " + issueTypeScreenScheme.getName());
        final List<IssueType> origIssueTypes = getExistingIssueTypes(issueTypeScreenScheme);
        final List<IssueTypeScreenSchemeEntity> entitiesToRemove = new ArrayList<>();
        final List<IssueTypeScreenSchemeEntity> entitiesToAdd = new ArrayList<>();
        for (final IssueType issueType : screenSchemesByIssueType.keySet()) {
            if (origIssueTypes.contains(issueType)) {
                logger.debug("Issue Type " + issueType.getName() + " is already on Issue Type Screen Scheme " + issueTypeScreenScheme.getName() + "; Will check its accuracy");
            }
            final FieldScreenScheme fieldScreenScheme = screenSchemesByIssueType.get(issueType);
            logger.debug("Associating issue type " + issueType.getName() + " with screen scheme " + fieldScreenScheme.getName() + " on issue type screen scheme " + issueTypeScreenScheme.getName());

            final boolean entityExists = checkForExistingEntity(fieldScreenScheme, issueTypeScreenScheme.getEntities(), issueType, entitiesToRemove);
            if (!entityExists) {
                logger.debug("Associating issueType " + issueType.getName() + " with FieldScreenScheme " + fieldScreenScheme.getName());
                final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = buildIssueTypeScreenSchemeEntity(issueType, fieldScreenScheme);
                entitiesToAdd.add(issueTypeScreenSchemeEntity);
            }
        }
        final boolean changesMade = adjustEntities(issueTypeScreenScheme, entitiesToRemove, entitiesToAdd);
        if (changesMade) {
            logger.debug("Performing store() on " + issueTypeScreenScheme.getName());
            issueTypeScreenScheme.store();
        } else {
            logger.debug("Issue Type Screen Scheme " + issueTypeScreenScheme.getName() + " already had the correct Black Duck IssueType associations");
        }
    }

    private boolean adjustEntities(final IssueTypeScreenScheme issueTypeScreenScheme, final List<IssueTypeScreenSchemeEntity> entitiesToRemove, final List<IssueTypeScreenSchemeEntity> entitiesToAdd) {
        boolean changesMade = false;
        for (final IssueTypeScreenSchemeEntity entityToRemove : entitiesToRemove) {
            logger.debug("Removing entity for issueTypeId " + entityToRemove.getIssueTypeId());
            issueTypeScreenScheme.removeEntity(entityToRemove.getIssueTypeId());
            changesMade = true;
        }
        for (final IssueTypeScreenSchemeEntity entityToAdd : entitiesToAdd) {
            issueTypeScreenScheme.addEntity(entityToAdd);
            changesMade = true;
        }
        return changesMade;
    }

    /**
     * See if entity exists. If it does, returns true. If it exists but is wrong, returns false but adds it to entitiesToRemove.
     */
    private boolean checkForExistingEntity(final FieldScreenScheme fieldScreenScheme, final Collection<IssueTypeScreenSchemeEntity> existingEntities, final IssueType issueType, final List<IssueTypeScreenSchemeEntity> entitiesToRemove) {
        boolean entityExists = false;
        if (existingEntities != null) {
            for (final IssueTypeScreenSchemeEntity existingEntity : existingEntities) {
                final FieldScreenScheme existingFieldScreenScheme = existingEntity.getFieldScreenScheme();
                final IssueType existingIssueType = existingEntity.getIssueType();
                final IssueTypeScreenScheme existingIssueTypeScreenScheme = existingEntity.getIssueTypeScreenScheme();
                final String existingIssueTypeName;
                final String existingIssueTypeId;
                if (existingIssueType == null) {
                    existingIssueTypeName = null;
                    existingIssueTypeId = null;
                } else {
                    existingIssueTypeName = existingIssueType.getName();
                    existingIssueTypeId = existingIssueType.getId();
                }
                logger.debug("existingFieldScreenScheme: " + existingFieldScreenScheme.getName() + "; existingIssueType: " + existingIssueTypeName + "; existingIssueTypeScreenScheme: " + existingIssueTypeScreenScheme.getName());

                if ((existingIssueTypeId == issueType.getId()) && (existingFieldScreenScheme.getId().equals(fieldScreenScheme.getId()))) {
                    logger.debug("The fieldScreenScheme -- issueTypeScreenScheme already exists");
                    entityExists = true;
                    break;
                } else if (existingIssueTypeId == issueType.getId()) {
                    logger.debug("issueType " + issueType.getName() + " is associated with FieldScreenScheme "
                                     + existingFieldScreenScheme.getName() + " which is wrong. Will remove this association");
                    entitiesToRemove.add(existingEntity);
                }
            }
        }
        return entityExists;
    }

    private IssueTypeScreenSchemeEntity buildIssueTypeScreenSchemeEntity(final IssueType issueType, final FieldScreenScheme fieldScreenScheme) {
        final FieldScreenSchemeManager fieldScreenSchemeManager = jiraServices.getFieldScreenSchemeManager();
        final IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity = new IssueTypeScreenSchemeEntityImpl(jiraServices.getIssueTypeScreenSchemeManager(), (GenericValue) null, fieldScreenSchemeManager, jiraServices.getConstantsManager());
        issueTypeScreenSchemeEntity.setIssueTypeId(issueType.getId());
        issueTypeScreenSchemeEntity.setFieldScreenScheme(fieldScreenSchemeManager.getFieldScreenScheme(fieldScreenScheme.getId()));
        return issueTypeScreenSchemeEntity;
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
                logger.debug("Issue Type " + origIssueType.getName() + " found on Issue Type Screen Scheme " + issueTypeScreenScheme.getName());
                origIssueTypes.add(origIssueType);
            }
        }
        return origIssueTypes;
    }

    /**
     * Returns true if this setup had already been done.
     */
    public boolean associateIssueTypesWithFieldConfigurationsOnProjectFieldConfigurationScheme(final Project project,
        final FieldLayoutScheme bdsFieldConfigurationScheme, final List<IssueType> issueTypes,
        final FieldLayout fieldConfiguration) {
        boolean wasAlreadySetUp = true;
        final FieldConfigurationScheme projectFieldConfigurationScheme = getProjectFieldConfigScheme(project);
        if (projectFieldConfigurationScheme == null) {
            logger.debug("Project " + project.getName() + ": Field Configuration Scheme: <null> (Default)");
            logger.debug("\tReplacing the project's Field Configuration Scheme with " + bdsFieldConfigurationScheme.getName());
            jiraServices.getFieldLayoutManager().addSchemeAssociation(project, bdsFieldConfigurationScheme.getId());
            wasAlreadySetUp = false;
        } else {
            logger.debug("Project " + project.getName() + ": Field Configuration Scheme: " + projectFieldConfigurationScheme.getName());
            wasAlreadySetUp = modifyProjectFieldConfigurationScheme(issueTypes, fieldConfiguration, projectFieldConfigurationScheme);
        }
        return wasAlreadySetUp;
    }

    /**
     * Returns true if this setup had already been done.
     */
    private boolean modifyProjectFieldConfigurationScheme(final List<IssueType> issueTypes, final FieldLayout fieldConfiguration, final FieldConfigurationScheme projectFieldConfigurationScheme) {
        logger.debug("Modifying the project's Field Configuration Scheme");
        boolean changesMade = false;
        final FieldLayoutScheme fieldLayoutScheme = getFieldLayoutScheme(projectFieldConfigurationScheme);
        if (issueTypes != null) {
            for (final IssueType issueType : issueTypes) {
                boolean issueTypeAlreadyMappedToOurFieldConfig = false;
                final Collection<FieldLayoutSchemeEntity> fieldLayoutSchemeEntities = fieldLayoutScheme.getEntities();
                if (fieldLayoutSchemeEntities != null) {
                    for (final FieldLayoutSchemeEntity fieldLayoutSchemeEntity : fieldLayoutSchemeEntities) {
                        final IssueType existingIssueType = fieldLayoutSchemeEntity.getIssueTypeObject();
                        if (existingIssueType == issueType && fieldLayoutSchemeEntity.getFieldLayoutId() != null && fieldLayoutSchemeEntity.getFieldLayoutId().equals(fieldConfiguration.getId())) {
                            issueTypeAlreadyMappedToOurFieldConfig = true;
                            break;
                        }
                    }
                }
                if (issueTypeAlreadyMappedToOurFieldConfig) {
                    logger.debug("Issue Type " + issueType.getName() + " is already associated with Field Configuration Scheme " + projectFieldConfigurationScheme.getName());
                    continue;
                }

                final FieldLayoutSchemeEntity fieldLayoutSchemeEntity = jiraServices.getFieldLayoutManager().createFieldLayoutSchemeEntity(fieldLayoutScheme, issueType.getId(), fieldConfiguration.getId());

                logger.debug("Adding to fieldLayoutScheme: " + fieldLayoutScheme.getName() + ": issueType " + issueType.getName() + " ==> field configuration " + fieldConfiguration.getName());
                fieldLayoutScheme.addEntity(fieldLayoutSchemeEntity);
                changesMade = true;
            }
        }
        if (changesMade) {
            logger.debug("Storing Field Configuration Scheme " + fieldLayoutScheme.getName());
            fieldLayoutScheme.store();
            return false;
        } else {
            logger.debug("Field Configuration Scheme " + fieldLayoutScheme.getName() + " already had Black Duck IssueType associated");
            return true;
        }
    }

    private FieldLayoutScheme getFieldLayoutScheme(final FieldConfigurationScheme fieldConfigurationScheme) {
        final FieldLayoutScheme fls = jiraServices.getFieldLayoutManager()
                                          .getMutableFieldLayoutScheme(fieldConfigurationScheme.getId());
        logger.debug("getFieldLayoutScheme(): FieldConfigurationScheme: " + fieldConfigurationScheme.getName() + " ==> FieldLayoutScheme: " + fls.getName());
        return fls;
    }

    private FieldConfigurationScheme getProjectFieldConfigScheme(final Project project) {
        FieldConfigurationScheme projectFieldConfigScheme = null;
        try {
            logger.debug("Getting field configuration scheme for project " + project.getName() + " [ID: " + project.getId() + "]");
            projectFieldConfigScheme = jiraServices.getFieldLayoutManager().getFieldConfigurationSchemeForProject(project.getId());
            logger.debug("\tprojectFieldConfigScheme: " + projectFieldConfigScheme.getName());
        } catch (final Exception e) {
            logger.error(e.getMessage());
        }
        if (projectFieldConfigScheme == null) {
            logger.debug("Project " + project.getName() + " field config scheme: Default Field Configuration Scheme");
        } else {
            logger.debug(
                "Project " + project.getName() + " field config scheme: " + projectFieldConfigScheme.getName());
        }
        return projectFieldConfigScheme;
    }

    private IssueType createIssueType(final String name, final String description, final Long avatarId) throws JiraException {
        logger.debug("Creating new issue type: " + name);

        IssueType newIssueType = null;
        try {
            newIssueType = jiraServices.getConstantsManager().insertIssueType(name, 0L, null, description, avatarId);
        } catch (final CreateException e) {
            throw new JiraException("Error creating Issue Type " + name + ": " + e.getMessage(), e);
        }
        logger.info("Created new issue type: " + newIssueType.getName() + " (id: " + newIssueType.getId() + ")");

        return newIssueType;
    }

}
