/**
 * Hub JIRA Plugin
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.jira.common.jiraversion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.mocks.MockBuildUtilsInfoImpl;

public class JiraVersionTest {

    private JiraVersion getJiraVersion(final String version, final int[] versionNumbers) throws ConfigurationException {
        final MockBuildUtilsInfoImpl mockBuildInfo = new MockBuildUtilsInfoImpl();
        mockBuildInfo.setVersion(version);
        mockBuildInfo.setVersionNumbers(versionNumbers);

        final JiraVersion jiraVersion = new JiraVersion(mockBuildInfo);
        return jiraVersion;
    }

    @Test
    public void testJira6_4_0() {
        final int[] versionNumbers = { 6, 4, 0 };
        JiraVersion jiraVersion;
        try {
            jiraVersion = getJiraVersion("6.4.0", versionNumbers);
            fail("Expected configuration exception");
        } catch (final ConfigurationException e) {

        }

    }

    @Test
    public void testJira7_0() throws ConfigurationException {
        final int[] versionNumbers = { 7, 0, 0 };
        JiraVersion jiraVersion;
        try {
            jiraVersion = getJiraVersion("7.0", versionNumbers);
            fail("Expected configuration exception");
        } catch (final ConfigurationException e) {

        }
    }

    @Test
    public void testJira7_1_0() throws ConfigurationException {
        final int[] versionNumbers = { 7, 1, 0 };
        final JiraVersion jiraVersion = getJiraVersion("7.1.0", versionNumbers);

        assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
        assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
        assertTrue(jiraVersion.isSupported());
    }

    @Test
    public void testJira7_1_99() throws ConfigurationException {
        final int[] versionNumbers = { 7, 1, 99 };
        final JiraVersion jiraVersion = getJiraVersion("7.1.99", versionNumbers);

        assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
        assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
        assertTrue(jiraVersion.isSupported());
    }

    @Test
    public void testJira7_2_99() throws ConfigurationException {
        final int[] versionNumbers = { 7, 2, 99 };
        final JiraVersion jiraVersion = getJiraVersion("7.2.99", versionNumbers);

        assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
        assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
        assertTrue(jiraVersion.isSupported());
    }

    @Test
    public void testJira7_2() throws ConfigurationException {
        final int[] versionNumbers = { 7, 2, 0 };
        final JiraVersion jiraVersion = getJiraVersion("7.2", versionNumbers);

        assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
        assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
        assertTrue(jiraVersion.isSupported());
    }

    @Test
    public void testJira8_0() throws ConfigurationException {
        final int[] versionNumbers = { 8, 0, 0 };
        final JiraVersion jiraVersion = getJiraVersion("8.0", versionNumbers);

        assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
        assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
        assertFalse(jiraVersion.isSupported());
    }

    @Test
    public void testJira7_3() throws ConfigurationException {
        final int[] versionNumbers = { 7, 3, 0 };
        final JiraVersion jiraVersion = getJiraVersion("7.3.0", versionNumbers);

        assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
        assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
        assertTrue(jiraVersion.isSupported());
    }

    @Test
    public void testJira7_4() throws ConfigurationException {
        final int[] versionNumbers = { 7, 4, 0 };
        final JiraVersion jiraVersion = getJiraVersion("7.4.0", versionNumbers);

        assertFalse(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_USERS));
        assertTrue(jiraVersion.hasCapability(JiraCapabilityEnum.GET_SYSTEM_ADMINS_AS_APPLICATIONUSERS));
        assertFalse(jiraVersion.isSupported());
    }

    @Test
    public void testMostRecentVersionString() throws ConfigurationException {
        final int[] versionNumbers = { 7, 4, 0 };
        final JiraVersion jiraVersion = getJiraVersion("7.4.0", versionNumbers);

        assertEquals("7.3.x", jiraVersion.getMostRecentJiraVersionSupportedString());
    }
}
