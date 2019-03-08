/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.jira.config;

import java.util.Optional;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraFieldCopyConfigSerializable;

public class JiraConfigDeserializer {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    public Optional<BlackDuckJiraConfigSerializable> deserializeConfig(final PluginConfigurationDetails pluginConfigDetails) {
        if (pluginConfigDetails.getProjectMappingJson() == null) {
            logger.debug("BlackDuckNotificationCheckTask: Project Mappings not configured, therefore there is nothing to do.");
            return Optional.empty();
        }
        if (pluginConfigDetails.getPolicyRulesJson() == null) {
            logger.debug("BlackDuckNotificationCheckTask: Policy Rules not configured, therefore there is nothing to do.");
            return Optional.empty();
        }
        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectMappingsJson(pluginConfigDetails.getProjectMappingJson());
        config.setPolicyRulesJson(pluginConfigDetails.getPolicyRulesJson());
        logger.debug("Mappings:");
        for (final BlackDuckProjectMapping mapping : config.getHubProjectMappings()) {
            logger.debug(mapping.toString());
        }
        logger.debug("Policy Rules:");
        for (final PolicyRuleSerializable rule : config.getPolicyRules()) {
            logger.debug(rule.toString());
        }
        return Optional.of(config);
    }

    public BlackDuckJiraFieldCopyConfigSerializable deserializeFieldCopyConfig(final String fieldCopyMappingJson) {
        final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig = new BlackDuckJiraFieldCopyConfigSerializable();
        fieldCopyConfig.setJson(fieldCopyMappingJson);
        return fieldCopyConfig;
    }
}
