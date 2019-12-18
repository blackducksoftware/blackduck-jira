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
package com.blackducksoftware.integration.jira.issue.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.blackducksoftware.integration.jira.common.exception.JiraIssueException;
import com.blackducksoftware.integration.jira.issue.conversion.output.AlertIssueSearchProperties;
import com.blackducksoftware.integration.jira.issue.conversion.output.IssueProperties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JiraIssuePropertyWrapper {
    public static final String ALERT_PROPERTY_KEY = "com-synopsys-integration-alert";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IssuePropertyService issuePropertyService;
    private final ProjectPropertyService projectPropertyService;
    private final JsonEntityPropertyManager jsonEntityPropertyManager;
    private Gson gson;

    public JiraIssuePropertyWrapper(IssuePropertyService issuePropertyService, ProjectPropertyService projectPropertyService, JsonEntityPropertyManager jsonEntityPropertyManager) {
        this.issuePropertyService = issuePropertyService;
        this.projectPropertyService = projectPropertyService;
        this.jsonEntityPropertyManager = jsonEntityPropertyManager;
        this.gson = new GsonBuilder().create();
    }

    public String getIssueProperty(Long issueId, ApplicationUser user, String propertyName) {
        PropertyResult propResult = issuePropertyService.getProperty(user, issueId, propertyName);
        if (propResult.isValid() && propResult.getEntityProperty().isDefined()) {
            return propResult.getEntityProperty().get().getValue();
        }
        return null;
    }

    public void addIssuePropertyJson(Long issueId, ApplicationUser user, String key, String jsonValue) throws JiraIssueException {
        logger.debug("addIssuePropertyJson(): issueId: " + issueId);
        if (isKeyOrValueBlank(key, jsonValue, "addIssuePropertyJson()")) {
            return;
        }
        EntityPropertyService.PropertyInput propertyInput = new EntityPropertyService.PropertyInput(jsonValue, key);

        SetPropertyValidationResult validationResult = issuePropertyService.validateSetProperty(user, issueId, propertyInput);
        if (validationResult.isValid()) {
            PropertyResult result = issuePropertyService.setProperty(user, validationResult);
            ErrorCollection errors = result.getErrorCollection();
            if (errors.hasAnyErrors()) {
                throw new JiraIssueException("addIssuePropertyJson", errors);
            }
        } else {
            throw new JiraIssueException("addIssuePropertyJson", validationResult.getErrorCollection());
        }
    }

    public void deleteIssueProperty(Long entityId, ApplicationUser user, String propertyKey) throws JiraIssueException {
        DeletePropertyValidationResult validationResult = projectPropertyService.validateDeleteProperty(user, entityId, propertyKey);
        if (validationResult.isValid()) {
            projectPropertyService.deleteProperty(user, validationResult);
        } else {
            throw new JiraIssueException("deleteIssueProperty", validationResult.getErrorCollection());
        }
    }

    public EntityProperty findProperty(String queryString) {
        List<EntityProperty> results = findProperties(queryString);
        if (results.isEmpty()) {
            logger.debug("No property found with that query string");
            return null;
        }
        return results.get(0);
    }

    public List<EntityProperty> findProperties(String queryString) {
        if (queryString == null) {
            return Arrays.asList();
        }
        logger.debug("Querying for property: " + queryString);
        EntityPropertyQuery<?> query = jsonEntityPropertyManager.query();
        EntityPropertyQuery.ExecutableQuery executableQuery = query.key(queryString);
        List<EntityProperty> props = executableQuery.find();
        return props;
    }

    public List<IssueProperties> findIssuePropertiesByBomComponentUri(String bomComponentUri) throws JiraIssueException {
        logger.debug("Find issue by Bom Component URI: " + bomComponentUri);
        List<IssueProperties> foundProperties = new ArrayList<>();

        List<EntityProperty> properties = findProperties(bomComponentUri);
        for (EntityProperty property : properties) {
            IssueProperties issueProperties = createIssuePropertiesFromJson(property.getValue());
            logger.debug("findIssuesByBomComponentUri(): propertyValue (converted from JSON): " + issueProperties);
            foundProperties.add(issueProperties);
        }
        return foundProperties;
    }

    public void addIssueProperties(Long issueId, ApplicationUser user, String key, IssueProperties propertiesObject) throws JiraIssueException {
        String jsonValue = "";
        if (null != propertiesObject) {
            jsonValue = gson.toJson(propertiesObject);
        }
        addIssuePropertyJson(issueId, user, key, jsonValue);
    }

    public void addAlertIssueProperties(Long issueId, ApplicationUser user, AlertIssueSearchProperties propertiesObject) throws JiraIssueException {
        String jsonValue = "";
        if (null != propertiesObject) {
            jsonValue = gson.toJson(propertiesObject);
        }
        addIssuePropertyJson(issueId, user, ALERT_PROPERTY_KEY, jsonValue);
    }

    public void addProjectProperty(Long issueId, ApplicationUser user, String key, Object value) throws JiraIssueException {
        String jsonValue = "";
        if (null != value) {
            jsonValue = gson.toJson(value);
        }
        addProjectPropertyJson(issueId, user, key, jsonValue);
    }

    public void addProjectPropertyJson(Long issueId, ApplicationUser user, String key, String jsonValue) throws JiraIssueException {
        logger.debug("addProjectPropertyJson(): issueId: " + issueId);
        if (isKeyOrValueBlank(key, jsonValue, "addProjectPropertyJson()")) {
            return;
        }
        EntityPropertyService.PropertyInput propertyInput = new EntityPropertyService.PropertyInput(jsonValue, key);

        SetPropertyValidationResult validationResult = projectPropertyService.validateSetProperty(user, issueId, propertyInput);

        if (validationResult.isValid()) {
            PropertyResult result = projectPropertyService.setProperty(user, validationResult);
            ErrorCollection errorCollection = result.getErrorCollection();
            if (errorCollection.hasAnyErrors()) {
                throw new JiraIssueException("addProjectPropertyJson", errorCollection);
            }
        } else {
            throw new JiraIssueException("addProjectPropertyJson", validationResult.getErrorCollection());
        }
    }

    private IssueProperties createIssuePropertiesFromJson(String json) throws JiraIssueException {
        try {
            return gson.fromJson(json, IssueProperties.class);
        } catch (Exception e) {
            throw new JiraIssueException("Could not deserialize issue properties.", "createIssuePropertiesFromJson");
        }
    }

    private boolean isKeyOrValueBlank(String key, String json, String method) {
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
