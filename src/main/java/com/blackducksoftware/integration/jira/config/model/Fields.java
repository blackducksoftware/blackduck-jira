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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.jira.config.ErrorTracking;

@XmlAccessorType(XmlAccessType.FIELD)
public class Fields implements Serializable, ErrorTracking {
    private static final long serialVersionUID = -9069924658532720147L;

    @XmlElement
    private List<IdToNameMapping> idToNameMappings = new ArrayList<>();

    @XmlElement
    private String errorMessage;

    @Override
    public boolean hasErrors() {
        return StringUtils.isNotBlank(errorMessage);
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<IdToNameMapping> getIdToNameMappings() {
        return idToNameMappings;
    }

    public void setIdToNameMappings(final List<IdToNameMapping> idToNameMappings) {
        this.idToNameMappings = idToNameMappings;
    }

    public void add(final IdToNameMapping idToNameMapping) {
        this.idToNameMappings.add(idToNameMapping);
    }

    @Override
    public String toString() {
        return "Fields [idToNameMappings=" + idToNameMappings + ", errorMessage=" + errorMessage + "]";
    }
}
