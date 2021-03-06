/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
 * https://www.synopsys.com/
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
package com.blackducksoftware.integration.jira.web.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.synopsys.integration.util.Stringable;

@XmlAccessorType(XmlAccessType.FIELD)
public class BlackDuckAdminConfigSerializable extends Stringable implements Serializable {
    private static final long serialVersionUID = -5925523949026662425L;

    @XmlElement
    private String hubJiraGroups;

    @XmlElement
    private List<String> jiraGroups;

    @XmlElement
    private String hubJiraGroupsError;

    public BlackDuckAdminConfigSerializable() {
    }

    public String getHubJiraGroups() {
        return hubJiraGroups;
    }

    public void setHubJiraGroups(final String hubJiraGroups) {
        this.hubJiraGroups = hubJiraGroups;
    }

    public List<String> getJiraGroups() {
        return jiraGroups;
    }

    public void setJiraGroups(final List<String> jiraGroups) {
        this.jiraGroups = jiraGroups;
    }

    public String getHubJiraGroupsError() {
        return hubJiraGroupsError;
    }

    public void setHubJiraGroupsError(final String hubJiraGroupsError) {
        this.hubJiraGroupsError = hubJiraGroupsError;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("BlackDuckAdminConfigSerializable [hubJiraGroups=");
        builder.append(hubJiraGroups);
        builder.append(", jiraGroups=");
        builder.append(jiraGroups);
        builder.append(", hubJiraGroupsError=");
        builder.append(hubJiraGroupsError);
        builder.append("]");
        return builder.toString();
    }

}
