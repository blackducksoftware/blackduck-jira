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

import com.blackducksoftware.integration.jira.common.model.BlackDuckProject;

public class BlackDuckProjectTest {

    @Test
    public void testBlackDuckProject() {
        final String name1 = "name1";
        final String projectUrl1 = "projectUrl1";

        final String name2 = "name2";
        final String projectUrl2 = "projectUrl2";

        final BlackDuckProject item1 = new BlackDuckProject();
        item1.setProjectName(name1);
        item1.setProjectUrl(projectUrl1);
        final BlackDuckProject item2 = new BlackDuckProject();
        item2.setProjectName(name2);
        item2.setProjectUrl(projectUrl2);
        final BlackDuckProject item3 = new BlackDuckProject();
        item3.setProjectName(name1);
        item3.setProjectUrl(projectUrl1);

        assertEquals(name1, item1.getProjectName());
        assertEquals(projectUrl1, item1.getProjectUrl());

        assertEquals(name2, item2.getProjectName());
        assertEquals(projectUrl2, item2.getProjectUrl());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        final StringBuilder builder = new StringBuilder();
        builder.append("BlackDuckProject [projectName=");
        builder.append(item1.getProjectName());
        builder.append(", projectUrl=");
        builder.append(item1.getProjectUrl());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
