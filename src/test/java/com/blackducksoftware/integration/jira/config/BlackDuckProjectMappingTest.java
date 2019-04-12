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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;

public class BlackDuckProjectMappingTest {

    @Test
    public void testBlackDuckProjectMapping() {
        final String jiraName1 = "name1";
        final Long jiraId1 = 0L;
        final String jiraKey1 = "key1";
        final String jiraProjectError1 = "error1";

        final JiraProject jira1 = new JiraProject();
        jira1.setProjectName(jiraName1);
        jira1.setProjectId(jiraId1);
        jira1.setProjectKey(jiraKey1);
        jira1.setProjectError(jiraProjectError1);

        final String blackDuckName1 = "name1";

        final String jiraName2 = "name2";
        final Long jiraId2 = 2L;
        final String jiraKey2 = "key2";
        final String jiraProjectError2 = "error2";

        final JiraProject jira2 = new JiraProject();
        jira2.setProjectName(jiraName2);
        jira2.setProjectId(jiraId2);
        jira2.setProjectKey(jiraKey2);
        jira2.setProjectError(jiraProjectError2);

        final String blackDuckName2 = "name2";

        final BlackDuckProjectMapping item1 = new BlackDuckProjectMapping();
        item1.setJiraProject(jira1);
        item1.setBlackDuckProjectName(blackDuckName1);
        final BlackDuckProjectMapping item2 = new BlackDuckProjectMapping();
        item2.setJiraProject(jira2);
        item2.setBlackDuckProjectName(blackDuckName2);
        final BlackDuckProjectMapping item3 = new BlackDuckProjectMapping();
        item3.setJiraProject(jira1);
        item3.setBlackDuckProjectName(blackDuckName1);

        assertEquals(jira1, item1.getJiraProject());
        assertEquals(blackDuckName1, item1.getBlackDuckProjectName());

        assertEquals(jira2, item2.getJiraProject());
        assertEquals(blackDuckName2, item2.getBlackDuckProjectName());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        final StringBuilder builder = new StringBuilder();
        builder.append("BlackDuckProjectMapping [jiraProject=");
        builder.append(item1.getJiraProject());
        builder.append(", blackDuckProjectName=");
        builder.append(item1.getBlackDuckProjectName());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
