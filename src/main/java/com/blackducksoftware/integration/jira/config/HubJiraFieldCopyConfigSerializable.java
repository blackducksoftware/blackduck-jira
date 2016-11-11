
package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;
import java.lang.reflect.Type;
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
        }
    }

    public String getJson() {
        if (projectFieldCopyMappings != null) {
            final Gson gson = new GsonBuilder().create();
            return gson.toJson(projectFieldCopyMappings);
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((projectFieldCopyMappings == null) ? 0 : projectFieldCopyMappings.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        HubJiraFieldCopyConfigSerializable other = (HubJiraFieldCopyConfigSerializable) obj;
        if (projectFieldCopyMappings == null) {
            if (other.projectFieldCopyMappings != null) return false;
        } else if (!projectFieldCopyMappings.equals(other.projectFieldCopyMappings)) return false;
        return true;
    }

    @Override
    public boolean hasErrors() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getErrorMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        // TODO Auto-generated method stub
    }

}
