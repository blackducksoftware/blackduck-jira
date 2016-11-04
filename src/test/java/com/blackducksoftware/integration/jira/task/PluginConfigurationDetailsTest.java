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
package com.blackducksoftware.integration.jira.task;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.common.HubJiraConfigKeys;

public class PluginConfigurationDetailsTest {

    @Test
    public void test() {
        final PluginSettings settings = Mockito.mock(PluginSettings.class);
        Mockito.when(settings.get(HubJiraConfigKeys.HUB_CONFIG_JIRA_INTERVAL_BETWEEN_CHECKS)).thenReturn("3");
        PluginConfigurationDetails details = new PluginConfigurationDetails(settings);
        assertEquals(3, details.getIntervalMinutes());
    }

}
