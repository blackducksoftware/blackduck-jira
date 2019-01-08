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
package com.blackducksoftware.integration.jira.common.jiraversion;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.blackducksoftware.integration.jira.JiraVersionCheck;
import com.blackducksoftware.integration.jira.common.exception.ConfigurationException;
import com.blackducksoftware.integration.jira.mocks.MockBuildUtilsInfoImpl;

public class JiraVersionTest {

    private JiraVersionCheck getJiraVersion(final String version, final int[] versionNumbers) throws ConfigurationException {
        final MockBuildUtilsInfoImpl mockBuildInfo = new MockBuildUtilsInfoImpl();
        mockBuildInfo.setVersion(version);
        mockBuildInfo.setVersionNumbers(versionNumbers);

        return new JiraVersionCheck(mockBuildInfo);
    }

    @Test
    public void testJira6_4_0() {
        final int[] versionNumbers = { 6, 4, 0 };
        try {
            getJiraVersion("6.4.0", versionNumbers);
            fail("Expected configuration exception");
        } catch (final ConfigurationException e) {
        }
    }

    @Test
    public void testJira7_0() throws ConfigurationException {
        final int[] versionNumbers = { 7, 0, 0 };
        try {
            getJiraVersion("7.0", versionNumbers);
            fail("Expected configuration exception");
        } catch (final ConfigurationException e) {

        }
    }

    @Test
    public void testJira7_1_0() throws ConfigurationException {
        final int[] versionNumbers = { 7, 1, 0 };
        try {
            getJiraVersion("7.1.0", versionNumbers);
            fail("Expected configuration exception");
        } catch (final ConfigurationException e) {
        }
    }

    @Test
    public void testJira7_1_99() throws ConfigurationException {
        final int[] versionNumbers = { 7, 1, 99 };
        try {
            getJiraVersion("7.1.99", versionNumbers);
            fail("Expected configuration exception");
        } catch (final ConfigurationException e) {
        }
    }

    @Test
    public void testJira7_2_99() throws ConfigurationException {
        final int[] versionNumbers = { 7, 2, 99 };
        try {
            getJiraVersion("7.2.99", versionNumbers);
            fail("Expected configuration exception");
        } catch (final ConfigurationException e) {
        }
    }

    @Test
    public void testJira7_2() throws ConfigurationException {
        final int[] versionNumbers = { 7, 2, 0 };
        try {
            getJiraVersion("7.2", versionNumbers);
            fail("Expected configuration exception");
        } catch (final ConfigurationException e) {
        }
    }

    @Test
    public void testJira7_3() throws ConfigurationException {
        final int[] versionNumbers = { 7, 3, 0 };
        final JiraVersionCheck jiraVersionCheck = getJiraVersion("7.3.0", versionNumbers);
        assertTrue(jiraVersionCheck.isSupported());
    }

    @Test
    public void testJira7_4() throws ConfigurationException {
        final int[] versionNumbers = { 7, 4, 0 };
        final JiraVersionCheck jiraVersionCheck = getJiraVersion("7.4.0", versionNumbers);
        assertTrue(jiraVersionCheck.isSupported());
    }

    @Test
    public void testJira7_5() throws ConfigurationException {
        final int[] versionNumbers = { 7, 5, 0 };
        final JiraVersionCheck jiraVersionCheck = getJiraVersion("7.5.0", versionNumbers);
        assertTrue(jiraVersionCheck.isSupported());
    }

    @Test
    public void testJira7_6() throws ConfigurationException {
        final int[] versionNumbers = { 7, 6, 0 };
        final JiraVersionCheck jiraVersionCheck = getJiraVersion("7.6.0", versionNumbers);
        assertTrue(jiraVersionCheck.isSupported());
    }

    @Test
    public void testJira7_7() throws ConfigurationException {
        final int[] versionNumbers = { 7, 7, 0 };
        final JiraVersionCheck jiraVersionCheck = getJiraVersion("7.7.0", versionNumbers);
        assertTrue(jiraVersionCheck.isSupported());
    }

    @Test
    public void testJira7_8() throws ConfigurationException {
        final int[] versionNumbers = { 7, 8, 0 };
        final JiraVersionCheck jiraVersionCheck = getJiraVersion("7.8.0", versionNumbers);
        assertTrue(jiraVersionCheck.isSupported());
    }

    @Test
    public void testJira7_9() throws ConfigurationException {
        final int[] versionNumbers = { 7, 9, 0 };
        final JiraVersionCheck jiraVersionCheck = getJiraVersion("7.9.0", versionNumbers);
        assertTrue(jiraVersionCheck.isSupported());
    }

    @Test
    public void testJira7_10() throws ConfigurationException {
        final int[] versionNumbers = { 7, 10, 0 };
        final JiraVersionCheck jiraVersionCheck = getJiraVersion("7.10.0", versionNumbers);
        assertTrue(jiraVersionCheck.isSupported());
    }

    @Test
    public void testJira7_11() throws ConfigurationException {
        final int[] versionNumbers = { 7, 11, 0 };
        final JiraVersionCheck jiraVersionCheck = getJiraVersion("7.11.0", versionNumbers);
        assertTrue(jiraVersionCheck.isSupported());
    }

    @Test
    public void testJira8_0() throws ConfigurationException {
        final int[] versionNumbers = { 8, 0, 0 };
        final JiraVersionCheck jiraVersionCheck = getJiraVersion("8.0", versionNumbers);
        assertFalse(jiraVersionCheck.isSupported());
    }

}
