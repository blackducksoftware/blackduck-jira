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
package com.blackducksoftware.integration.jira.common.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.synopsys.integration.util.Stringable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BlackDuckProjectMapping extends Stringable implements Serializable {
    private static final long serialVersionUID = -1650687378205611080L;

    @XmlElement
    private JiraProject jiraProject;

    // Keeping this around in order to read old data from the JIRA SAL
    @Deprecated
    @XmlElement
    private BlackDuckProject hubProject;

    @XmlElement
    private String blackDuckProjectName;

    public BlackDuckProjectMapping() {
    }

    public JiraProject getJiraProject() {
        return jiraProject;
    }

    public void setJiraProject(final JiraProject jiraProject) {
        this.jiraProject = jiraProject;
    }

    @Deprecated
    public BlackDuckProject getHubProject() {
        return hubProject;
    }

    @Deprecated
    public void setHubProject(final BlackDuckProject hubProject) {
        this.hubProject = hubProject;
    }

    public void setBlackDuckProjectName(final String blackDuckProjectName) {
        this.blackDuckProjectName = blackDuckProjectName;
    }

    public String getBlackDuckProjectName() {
        return blackDuckProjectName;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BlackDuckProjectMapping [jiraProject=");
        builder.append(jiraProject);
        builder.append(", hubProject=");
        builder.append(hubProject);
        builder.append(", blackDuckProjectName=");
        builder.append(blackDuckProjectName);
        builder.append("]");
        return builder.toString();
    }

}
