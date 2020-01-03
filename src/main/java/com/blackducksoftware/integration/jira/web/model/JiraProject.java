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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.synopsys.integration.util.Stringable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class JiraProject extends Stringable implements Serializable {
    private static final long serialVersionUID = 7739038743028968165L;

    @XmlElement
    private String projectName;
    @XmlElement
    private Long projectId;
    @XmlElement
    private String issueCreator;
    @XmlElement
    private String assigneeUserId;
    @XmlElement
    private String projectKey;
    @XmlElement
    private String projectError;
    @XmlElement
    private Boolean configuredForVulnerabilities;
    @XmlElement
    private String workflowStatus;

    public JiraProject() {
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public Long getProjectId() {
        return projectId;
    }

    public String getIssueCreator() {
        return issueCreator;
    }

    public void setIssueCreator(final String issueCreator) {
        this.issueCreator = issueCreator;
    }

    public String getAssigneeUserId() {
        return assigneeUserId;
    }

    public void setAssigneeUserId(final String assigneeUserId) {
        this.assigneeUserId = assigneeUserId;
    }

    public void setProjectId(final Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(final String projectKey) {
        this.projectKey = projectKey;
    }

    public String getProjectError() {
        return projectError;
    }

    public void setProjectError(final String projectError) {
        this.projectError = projectError;
    }

    public Boolean isConfiguredForVulnerabilities() {
        return configuredForVulnerabilities != null ? configuredForVulnerabilities : Boolean.FALSE;
    }

    public void setConfiguredForVulnerabilities(final Boolean configuredForVulnerabilities) {
        this.configuredForVulnerabilities = configuredForVulnerabilities;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(final String workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("JiraProject [projectName=");
        builder.append(projectName);
        builder.append(", projectId=");
        builder.append(projectId);
        builder.append(", issueCreator=");
        builder.append(issueCreator);
        builder.append(", projectKey=");
        builder.append(projectKey);
        builder.append(", assigneeUserId=");
        builder.append(assigneeUserId);
        builder.append(", projectError=");
        builder.append(projectError);
        builder.append(", configuredForVulnerabilities=");
        builder.append(configuredForVulnerabilities);
        builder.append(", workflowStatus=");
        builder.append(workflowStatus);
        builder.append("]");
        return builder.toString();
    }

}
