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

import com.blackducksoftware.integration.jira.web.ErrorTracking;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.util.Stringable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BlackDuckJiraFieldCopyConfigSerializable extends Stringable implements Serializable, ErrorTracking {
    private static final long serialVersionUID = 7103882445919763746L;

    @XmlElement
    private String errorMessage;

    @XmlElement
    private Set<ProjectFieldCopyMapping> projectFieldCopyMappings;

    public Set<ProjectFieldCopyMapping> getProjectFieldCopyMappings() {
        return projectFieldCopyMappings;
    }

    public void setProjectFieldCopyMappings(final Set<ProjectFieldCopyMapping> projectFieldCopyMappings) {
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
        final List<String> sourceFields = new ArrayList<>();
        if (projectFieldCopyMappings != null) {
            for (final ProjectFieldCopyMapping mapping : projectFieldCopyMappings) {
                sourceFields.add(mapping.getSourceFieldName());
            }
        }
        return sourceFields;
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
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "BlackDuckJiraFieldCopyConfigSerializable [projectFieldCopyMappings=" + projectFieldCopyMappings + "]";
    }

}
