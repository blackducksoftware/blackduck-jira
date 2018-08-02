/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.jira.task;

import static org.junit.Assert.assertNull;

import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.common.BlackDuckJiraConstants;
import com.blackducksoftware.integration.jira.config.JiraSettingsService;

public class JiraSettingsServiceTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testNull() {
        final PluginSettings pluginSettings = Mockito.mock(PluginSettings.class);
        Mockito.when(pluginSettings.get(BlackDuckJiraConstants.HUB_JIRA_ERROR)).thenReturn(null);
        assertNull(JiraSettingsService.expireOldErrors(pluginSettings));
    }

    @Test
    public void testOld() {
        final PluginSettings pluginSettings = Mockito.mock(PluginSettings.class);
        Mockito.when(pluginSettings.get(BlackDuckJiraConstants.HUB_JIRA_ERROR)).thenReturn(new HashMap<String, String>());
        assertNull(JiraSettingsService.expireOldErrors(pluginSettings));
    }

    @Test
    public void testInvalidJson() {
        final PluginSettings pluginSettings = Mockito.mock(PluginSettings.class);
        Mockito.when(pluginSettings.get(BlackDuckJiraConstants.HUB_JIRA_ERROR)).thenReturn("abc");
        assertNull(JiraSettingsService.expireOldErrors(pluginSettings));
    }

    @Test
    public void testEmptyJson() {
        final PluginSettings pluginSettings = Mockito.mock(PluginSettings.class);
        Mockito.when(pluginSettings.get(BlackDuckJiraConstants.HUB_JIRA_ERROR)).thenReturn("");
        assertNull(JiraSettingsService.expireOldErrors(pluginSettings));
    }
}
