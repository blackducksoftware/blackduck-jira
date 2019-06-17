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
package com.blackducksoftware.integration.jira.config;

import org.apache.log4j.Logger;

import com.blackducksoftware.integration.jira.common.BlackDuckJiraLogger;
import com.blackducksoftware.integration.jira.common.settings.model.PluginIssueCreationConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.ProjectMappingConfigModel;
import com.blackducksoftware.integration.jira.common.settings.model.TicketCriteriaConfigModel;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraConfigSerializable;
import com.blackducksoftware.integration.jira.config.model.BlackDuckJiraFieldCopyConfigSerializable;

public class JiraConfigDeserializer {
    private final BlackDuckJiraLogger logger = new BlackDuckJiraLogger(Logger.getLogger(this.getClass().getName()));

    public BlackDuckJiraConfigSerializable deserializeConfig(final PluginIssueCreationConfigModel issueCreationConfig) {
        final ProjectMappingConfigModel projectMapping = issueCreationConfig.getProjectMapping();
        final TicketCriteriaConfigModel ticketCriteria = issueCreationConfig.getTicketCriteria();

        final BlackDuckJiraConfigSerializable config = new BlackDuckJiraConfigSerializable();
        config.setHubProjectMappingsJson(projectMapping.getMappingsJson());
        config.setPolicyRulesJson(ticketCriteria.getPolicyRulesJson());

        logger.debug("Mappings: " + config.getHubProjectMappings());
        logger.debug("Policy Rules: " + config.getPolicyRules());

        return config;
    }

    public BlackDuckJiraFieldCopyConfigSerializable deserializeFieldCopyConfig(final String fieldCopyMappingJson) {
        final BlackDuckJiraFieldCopyConfigSerializable fieldCopyConfig = new BlackDuckJiraFieldCopyConfigSerializable();
        fieldCopyConfig.setJson(fieldCopyMappingJson);
        return fieldCopyConfig;
    }

}
