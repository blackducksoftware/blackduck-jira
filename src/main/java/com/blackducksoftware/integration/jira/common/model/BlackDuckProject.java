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

// Keeping this around in order to read old data from the JIRA SAL
// Deprecated in 4.2.0, delete when customers all upgrade to 4.2.0+
@Deprecated
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BlackDuckProject extends Stringable implements Serializable {
    private static final long serialVersionUID = -3423054294095627163L;

    @XmlElement
    private String projectName;

    @XmlElement
    private String projectUrl;

    public BlackDuckProject() {
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(final String projectUrl) {
        this.projectUrl = projectUrl;
    }

    @Override
    public String toString() {
        return "BlackDuckProject [projectName=" + projectName + ", projectUrl=" + projectUrl + "]";
    }

}
