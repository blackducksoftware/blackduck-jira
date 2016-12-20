/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
public class Fields implements Serializable, ErrorTracking {
    private static final long serialVersionUID = -9069924658532720147L;

    @XmlElement
    private List<IdToNameMapping> idToNameMappings = new ArrayList<>();

    @XmlElement
    private String errorMessage;

    @Override
    public boolean hasErrors() {
        if (StringUtils.isBlank(errorMessage)) {
            return false;
        }
        return true;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<IdToNameMapping> getIdToNameMappings() {
        return idToNameMappings;
    }

    public void setIdToNameMappings(List<IdToNameMapping> idToNameMappings) {
        this.idToNameMappings = idToNameMappings;
    }

    public void add(IdToNameMapping idToNameMapping) {
        this.idToNameMappings.add(idToNameMapping);
    }

    @Override
    public String toString() {
        return "Fields [idToNameMappings=" + idToNameMappings + ", errorMessage=" + errorMessage + "]";
    }
}
