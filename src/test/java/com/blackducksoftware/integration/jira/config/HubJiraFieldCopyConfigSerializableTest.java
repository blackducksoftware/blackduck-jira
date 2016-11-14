/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.jira.config;

import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.jira.common.PluginField;

public class HubJiraFieldCopyConfigSerializableTest {

    private static final String TARGET_FIELD_ID = "targetFieldId";

    private static final String HUB_PROJECT_NAME = "hubProjectName";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() {
        HubJiraFieldCopyConfigSerializable config = new HubJiraFieldCopyConfigSerializable();
        Set<ProjectFieldCopyMapping> mappings = new HashSet<>();
        ProjectFieldCopyMapping mapping = new ProjectFieldCopyMapping();
        mapping.setHubProjectName(HUB_PROJECT_NAME);
        mapping.setJiraProjectName("jiraProjectName");
        mapping.setPluginField(PluginField.HUB_CUSTOM_FIELD_COMPONENT);
        mapping.setTargetFieldId(TARGET_FIELD_ID);
        mappings.add(mapping);
        config.setProjectFieldCopyMappings(mappings);
        config.getSourceFields();
    }

}
