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
package com.blackducksoftware.integration.jira.config.model;

import java.io.Serializable;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.synopsys.integration.util.Stringable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ProjectPatternRestModel extends Stringable implements Serializable {
    private static final long serialVersionUID = -8758361473598586323L;

    private Collection<String> projects;
    private String regexString;
    private String errorMessage;

    public ProjectPatternRestModel() {
    }

    public Collection<String> getProjects() {
        return projects;
    }

    public void setProjects(final Collection<String> projects) {
        this.projects = projects;
    }

    public String getRegexString() {
        return regexString;
    }

    public void setRegexString(final String regexString) {
        this.regexString = regexString;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
