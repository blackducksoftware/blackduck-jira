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

import org.junit.Assert;
import org.junit.Test;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.common.PluginSettingsWrapper;
import com.blackducksoftware.integration.jira.mocks.PluginSettingsMock;

public class PluginSettingsWrapperTest {

    @Test
    public void testPasswordEncoding() {
        final PluginSettings pluginSettings = new PluginSettingsMock();
        final PluginSettingsWrapper pluginSettingsWrapper = new PluginSettingsWrapper(pluginSettings);

        final String testPassword = "test password";
        pluginSettingsWrapper.setBlackDuckProxyPassword(testPassword);

        final String savedProxyPass = (String) pluginSettings.get(BlackDuckConfigKeys.CONFIG_PROXY_PASS);
        System.out.println("Saved encoded password is: " + savedProxyPass);
        Assert.assertNotEquals(testPassword, savedProxyPass);

        final String secondSavedProxyPass = (String) pluginSettings.get(BlackDuckConfigKeys.CONFIG_PROXY_PASS);
        Assert.assertEquals(savedProxyPass, secondSavedProxyPass);
    }

    @Test
    public void testPasswordDecoding() {
        final PluginSettings pluginSettings = new PluginSettingsMock();
        final PluginSettingsWrapper pluginSettingsWrapper = new PluginSettingsWrapper(pluginSettings);

        final String testPassword = "test password";
        pluginSettingsWrapper.setBlackDuckProxyPassword(testPassword);
        final String savedProxyPass = pluginSettingsWrapper.getBlackDuckProxyPassword();
        Assert.assertEquals(testPassword, savedProxyPass);
    }
}
