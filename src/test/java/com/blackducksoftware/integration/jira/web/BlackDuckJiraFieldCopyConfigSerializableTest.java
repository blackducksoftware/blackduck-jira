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
package com.blackducksoftware.integration.jira.web;

import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.jira.common.model.PluginField;
import com.blackducksoftware.integration.jira.web.model.BlackDuckJiraFieldCopyConfigSerializable;
import com.blackducksoftware.integration.jira.web.model.ProjectFieldCopyMapping;

public class BlackDuckJiraFieldCopyConfigSerializableTest {
    private static final String TARGET_FIELD_ID = "targetFieldId";
    private static final String BLACKDUCK_PROJECT_NAME = "blackDuckProjectName";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() {
        final BlackDuckJiraFieldCopyConfigSerializable config = new BlackDuckJiraFieldCopyConfigSerializable();
        final Set<ProjectFieldCopyMapping> mappings = new HashSet<>();
        final ProjectFieldCopyMapping mapping = new ProjectFieldCopyMapping();
        mapping.setHubProjectName(BLACKDUCK_PROJECT_NAME);
        mapping.setJiraProjectName("jiraProjectName");
        mapping.setSourceFieldId(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT.getId());
        mapping.setSourceFieldName(PluginField.BLACKDUCK_CUSTOM_FIELD_COMPONENT.getName());
        mapping.setTargetFieldId(TARGET_FIELD_ID);
        mappings.add(mapping);
        config.setProjectFieldCopyMappings(mappings);
        config.getSourceFields();
    }

}
