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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class OptionsSerializable implements Serializable {

    @XmlElement
    private Boolean changeIssueStateEnabled;

    public Boolean getChangeIssueStateEnabled() {
        return changeIssueStateEnabled;
    }

    public void setChangeIssueStateEnabled(Boolean changeIssueStateEnabled) {
        this.changeIssueStateEnabled = changeIssueStateEnabled;
    }

    @Override
    public String toString() {
        return "OptionsSerializable [changeIssueStateEnabled=" + changeIssueStateEnabled + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((changeIssueStateEnabled == null) ? 0 : changeIssueStateEnabled.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        OptionsSerializable other = (OptionsSerializable) obj;
        if (changeIssueStateEnabled == null) {
            if (other.changeIssueStateEnabled != null) return false;
        } else if (!changeIssueStateEnabled.equals(other.changeIssueStateEnabled)) return false;
        return true;
    }
    
    
}
