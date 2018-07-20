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
package com.blackducksoftware.integration.jira.common.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.blackducksoftware.integration.util.Stringable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class HubProjectMapping extends Stringable implements Serializable {
    private static final long serialVersionUID = -7375806850713790815L;

    @XmlElement
    private JiraProject jiraProject;

    @XmlElement
    private HubProject hubProject;

    public HubProjectMapping() {
    }

    public JiraProject getJiraProject() {
        return jiraProject;
    }

    public void setJiraProject(final JiraProject jiraProject) {
        this.jiraProject = jiraProject;
    }

    public HubProject getHubProject() {
        return hubProject;
    }

    public void setHubProject(final HubProject hubProject) {
        this.hubProject = hubProject;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("HubProjectMapping [jiraProject=");
        builder.append(jiraProject);
        builder.append(", hubProject=");
        builder.append(hubProject);
        builder.append("]");
        return builder.toString();
    }

}
