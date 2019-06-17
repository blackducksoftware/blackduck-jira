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
package com.blackducksoftware.integration.jira.task.issue.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.properties.IssuePropertyService;
import com.atlassian.jira.bc.project.property.ProjectPropertyService;
import com.atlassian.jira.entity.property.EntityProperty;
import com.atlassian.jira.entity.property.EntityPropertyQuery;
import com.atlassian.jira.entity.property.EntityPropertyService;
import com.atlassian.jira.entity.property.EntityPropertyService.DeletePropertyValidationResult;
import com.atlassian.jira.entity.property.EntityPropertyService.PropertyResult;
import com.atlassian.jira.entity.property.EntityPropertyService.SetPropertyValidationResult;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;

public class JiraIssuePropertyWrapper {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    private final IssuePropertyService issuePropertyService;
    private final ProjectPropertyService projectPropertyService;
    private final JsonEntityPropertyManager jsonEntityPropertyManager;

    public JiraIssuePropertyWrapper(final IssuePropertyService issuePropertyService, final ProjectPropertyService projectPropertyService, final JsonEntityPropertyManager jsonEntityPropertyManager) {
        this.issuePropertyService = issuePropertyService;
        this.projectPropertyService = projectPropertyService;
        this.jsonEntityPropertyManager = jsonEntityPropertyManager;
    }

    public String getIssueProperty(final Long issueId, final ApplicationUser user, final String propertyName) {
        final PropertyResult propResult = issuePropertyService.getProperty(user, issueId, propertyName);
        if (propResult.isValid() && propResult.getEntityProperty().isDefined()) {
            return propResult.getEntityProperty().get().getValue();
        }
        return null;
    }

    public Map<String, String> getIssueProperties(final Long issueId, final ApplicationUser user) {
        final Map<String, String> properties = new HashMap<>();
        final List<EntityProperty> entityProperties = issuePropertyService.getProperties(user, issueId);
        for (final EntityProperty entityProp : entityProperties) {
            properties.put(entityProp.getKey(), entityProp.getValue());
        }
        return properties;
    }

    public void addIssuePropertyJson(final Long issueId, final ApplicationUser user, final String key, final String jsonValue) throws JiraIssueException {
        logger.debug("addIssuePropertyJson(): issueId: " + issueId);
        if (isKeyOrValueBlank(key, jsonValue, "addIssuePropertyJson()")) {
            return;
        }
        final EntityPropertyService.PropertyInput propertyInput = new EntityPropertyService.PropertyInput(jsonValue, key);

        final SetPropertyValidationResult validationResult = issuePropertyService.validateSetProperty(user, issueId, propertyInput);
        if (validationResult.isValid()) {
            final PropertyResult result = issuePropertyService.setProperty(user, validationResult);
            final ErrorCollection errors = result.getErrorCollection();
            if (errors.hasAnyErrors()) {
                throw new JiraIssueException("addIssuePropertyJson", errors);
            }
        } else {
            throw new JiraIssueException("addIssuePropertyJson", validationResult.getErrorCollection());
        }
    }

    public void deleteIssueProperty(final Long entityId, final ApplicationUser user, final String propertyKey) throws JiraIssueException {
        final DeletePropertyValidationResult validationResult = projectPropertyService.validateDeleteProperty(user, entityId, propertyKey);
        if (validationResult.isValid()) {
            projectPropertyService.deleteProperty(user, validationResult);
        } else {
            throw new JiraIssueException("deleteIssueProperty", validationResult.getErrorCollection());
        }
    }

    public EntityProperty findProperty(final String queryString) {
        final List<EntityProperty> results = findProperties(queryString);
        if (results.isEmpty()) {
            logger.debug("No property found with that query string");
            return null;
        }
        return results.get(0);
    }

    public List<EntityProperty> findProperties(final String queryString) {
        logger.debug("Querying for property: " + queryString);
        final EntityPropertyQuery<?> query = jsonEntityPropertyManager.query();
        final EntityPropertyQuery.ExecutableQuery executableQuery = query.key(queryString);
        final List<EntityProperty> props = executableQuery.find();
        return props;
    }

    public void addProjectPropertyJson(final Long issueId, final ApplicationUser user, final String key, final String jsonValue) throws JiraIssueException {
        logger.debug("addProjectPropertyJson(): issueId: " + issueId);
        if (isKeyOrValueBlank(key, jsonValue, "addProjectPropertyJson()")) {
            return;
        }
        final EntityPropertyService.PropertyInput propertyInput = new EntityPropertyService.PropertyInput(jsonValue, key);

        final SetPropertyValidationResult validationResult = projectPropertyService.validateSetProperty(user, issueId, propertyInput);

        if (validationResult.isValid()) {
            final PropertyResult result = projectPropertyService.setProperty(user, validationResult);
            final ErrorCollection errorCollection = result.getErrorCollection();
            if (errorCollection.hasAnyErrors()) {
                throw new JiraIssueException("addProjectPropertyJson", errorCollection);
            }
        } else {
            throw new JiraIssueException("addProjectPropertyJson", validationResult.getErrorCollection());
        }
    }

    private boolean isKeyOrValueBlank(final String key, final String json, final String method) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(json)) {
            if (StringUtils.isBlank(key)) {
                logger.error(String.format("%s: key is blank", method, key, json));
            }
            if (StringUtils.isBlank(json)) {
                logger.error(String.format("%s: json is blank", method, key, json));
            }
            return true;
        } else {
            logger.debug(String.format("%s: key: %s; json: %s", method, key, json));
            return false;
        }
    }

}
