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
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.StringUtils;

@XmlAccessorType(XmlAccessType.FIELD)
public class TargetFields implements Serializable, ErrorTracking {
    private static final long serialVersionUID = -9069924658532720147L;

    @XmlElement
    private Map<String, String> idToNameMapping = new HashMap<>();

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

    public Map<String, String> getIdToNameMapping() {
        return idToNameMapping;
    }

    public void setIdToNameMapping(Map<String, String> idToNameMapping) {
        this.idToNameMapping = idToNameMapping;
    }

    public void add(String id, String name) {
        this.idToNameMapping.put(id, name);
    }

    @Override
    public String toString() {
        return "TargetFields [idToNameMapping=" + idToNameMapping + ", errorMessage=" + errorMessage + "]";
    }
}
