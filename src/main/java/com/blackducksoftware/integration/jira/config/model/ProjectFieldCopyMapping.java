/**
 * Black Duck JIRA Plugin
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
package com.blackducksoftware.integration.jira.config.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.synopsys.integration.util.Stringable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectFieldCopyMapping extends Stringable implements Serializable {
    private static final long serialVersionUID = -8343238734203251050L;

    @XmlElement
    private String jiraProjectName;

    @XmlElement
    private String hubProjectName;

    @XmlElement
    private String sourceFieldId;

    @XmlElement
    private String sourceFieldName;

    @XmlElement
    private String targetFieldId;

    @XmlElement
    private String targetFieldName;

    public ProjectFieldCopyMapping() {
    }

    public ProjectFieldCopyMapping(final String jiraProjectName, final String hubProjectName, final String sourceFieldId, final String sourceFieldName, final String targetFieldId, final String targetFieldName) {
        this.jiraProjectName = jiraProjectName;
        this.hubProjectName = hubProjectName;
        this.sourceFieldId = sourceFieldId;
        this.sourceFieldName = sourceFieldName;
        this.targetFieldId = targetFieldId;
        this.targetFieldName = targetFieldName;
    }

    public String getJiraProjectName() {
        return jiraProjectName;
    }

    public String getHubProjectName() {
        return hubProjectName;
    }

    public String getSourceFieldId() {
        return sourceFieldId;
    }

    public String getSourceFieldName() {
        return sourceFieldName;
    }

    public String getTargetFieldId() {
        return targetFieldId;
    }

    public String getTargetFieldName() {
        return targetFieldName;
    }

    public void setJiraProjectName(final String jiraProjectName) {
        this.jiraProjectName = jiraProjectName;
    }

    public void setHubProjectName(final String hubProjectName) {
        this.hubProjectName = hubProjectName;
    }

    public void setSourceFieldId(final String sourceFieldId) {
        this.sourceFieldId = sourceFieldId;
    }

    public void setSourceFieldName(final String sourceFieldName) {
        this.sourceFieldName = sourceFieldName;
    }

    public void setTargetFieldId(final String targetFieldId) {
        this.targetFieldId = targetFieldId;
    }

    public void setTargetFieldName(final String targetFieldName) {
        this.targetFieldName = targetFieldName;
    }

    @Override
    public String toString() {
        return "ProjectFieldCopyMapping [jiraProjectName=" + jiraProjectName + ", hubProjectName=" + hubProjectName + ", sourceFieldId=" + sourceFieldId
                + ", sourceFieldName=" + sourceFieldName + ", targetFieldId=" + targetFieldId + ", targetFieldName=" + targetFieldName + "]";
    }
}
