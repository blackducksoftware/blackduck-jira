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
package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class HubJiraFieldCopyConfigSerializable implements Serializable, ErrorTracking {
    private static final long serialVersionUID = 2893090613500813058L;

    @XmlElement
    private String errorMessage;

    @XmlElement
    private Set<ProjectFieldCopyMapping> projectFieldCopyMappings;

    public Set<ProjectFieldCopyMapping> getProjectFieldCopyMappings() {
        return projectFieldCopyMappings;
    }

    public void setProjectFieldCopyMappings(Set<ProjectFieldCopyMapping> projectFieldCopyMappings) {
        this.projectFieldCopyMappings = projectFieldCopyMappings;
    }

    public void setJson(final String projectFieldCopyMappingsJson) {
        if (StringUtils.isNotBlank(projectFieldCopyMappingsJson)) {
            final Gson gson = new GsonBuilder().create();
            final Type mappingType = new TypeToken<Set<ProjectFieldCopyMapping>>() {
            }.getType();
            this.projectFieldCopyMappings = gson.fromJson(projectFieldCopyMappingsJson, mappingType);
        } else {
            this.projectFieldCopyMappings = new HashSet<>();
        }
    }

    public String getJson() {
        if (projectFieldCopyMappings != null) {
            final Gson gson = new GsonBuilder().create();
            return gson.toJson(projectFieldCopyMappings);
        }
        return null;
    }

    public List<String> getSourceFields() {
        List<String> sourceFields = new ArrayList<>();
        if (projectFieldCopyMappings != null) {
            for (ProjectFieldCopyMapping mapping : projectFieldCopyMappings) {
                sourceFields.add(mapping.getSourceFieldName());
            }
        }
        return sourceFields;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
        result = prime * result + ((projectFieldCopyMappings == null) ? 0 : projectFieldCopyMappings.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        HubJiraFieldCopyConfigSerializable other = (HubJiraFieldCopyConfigSerializable) obj;
        if (errorMessage == null) {
            if (other.errorMessage != null) return false;
        } else if (!errorMessage.equals(other.errorMessage)) return false;
        if (projectFieldCopyMappings == null) {
            if (other.projectFieldCopyMappings != null) return false;
        } else if (!projectFieldCopyMappings.equals(other.projectFieldCopyMappings)) return false;
        return true;
    }

    @Override
    public boolean hasErrors() {
        boolean hasErrors = false;
        if (StringUtils.isNotBlank(getErrorMessage())) {
            hasErrors = true;
        }
        return hasErrors;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "HubJiraFieldCopyConfigSerializable [projectFieldCopyMappings=" + projectFieldCopyMappings + "]";
    }

}
