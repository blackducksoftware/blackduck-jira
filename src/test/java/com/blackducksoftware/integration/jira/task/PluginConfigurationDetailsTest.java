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
package com.blackducksoftware.integration.jira.task;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.config.PluginConfigKeys;
import com.blackducksoftware.integration.jira.config.PluginConfigurationDetails;

public class PluginConfigurationDetailsTest {

    @Test
    public void test() {
        final PluginSettings settings = Mockito.mock(PluginSettings.class);
        Mockito.when(settings.get(PluginConfigKeys.BLACKDUCK_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS)).thenReturn("3");
        PluginConfigurationDetails details = new PluginConfigurationDetails(settings);
        assertEquals(3, details.getIntervalMinutes());
    }

}
