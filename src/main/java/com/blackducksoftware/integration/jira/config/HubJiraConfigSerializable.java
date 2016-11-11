/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.jira.common.HubProject;
import com.blackducksoftware.integration.jira.common.HubProjectMapping;
import com.blackducksoftware.integration.jira.common.JiraProject;
import com.blackducksoftware.integration.jira.common.PolicyRuleSerializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class HubJiraConfigSerializable implements Serializable, ErrorTracking {

    private static final long serialVersionUID = -3736258315416679501L;

    @XmlElement
    private String errorMessage;

    @XmlElement
    private String intervalBetweenChecks;

    @XmlElement
    private String intervalBetweenChecksError;

    @XmlElement
    private List<JiraProject> jiraProjects;

    @XmlElement
    private String jiraProjectsError;

    @XmlElement
    private List<HubProject> hubProjects;

    @XmlElement
    private String hubProjectsError;

    @XmlElement
    private Set<HubProjectMapping> hubProjectMappings;

    @XmlElement
    private String hubProjectMappingError;

    @XmlElement
    private List<PolicyRuleSerializable> policyRules;

    @XmlElement
    private String policyRulesError;

    @Override
    public boolean hasErrors() {
        boolean hasErrors = false;
        if (StringUtils.isNotBlank(getErrorMessage())) {
            hasErrors = true;
        }
        if (StringUtils.isNotBlank(getIntervalBetweenChecksError())) {
            hasErrors = true;
        }
        if (StringUtils.isNotBlank(getJiraProjectsError())) {
            hasErrors = true;
        }
        if (StringUtils.isNotBlank(getHubProjectsError())) {
            hasErrors = true;
        }
        if (StringUtils.isNotBlank(getHubProjectMappingError())) {
            hasErrors = true;
        }
        if (StringUtils.isNotBlank(getPolicyRulesError())) {
            hasErrors = true;
        }
        return hasErrors;
    }

    public String getIntervalBetweenChecks() {
        return intervalBetweenChecks;
    }

    public void setIntervalBetweenChecks(final String intervalBetweenChecks) {
        this.intervalBetweenChecks = intervalBetweenChecks;
    }

    public String getIntervalBetweenChecksError() {
        return intervalBetweenChecksError;
    }

    public void setIntervalBetweenChecksError(final String intervalBetweenChecksError) {
        this.intervalBetweenChecksError = intervalBetweenChecksError;
    }

    public List<JiraProject> getJiraProjects() {
        return jiraProjects;
    }

    public void setJiraProjects(final List<JiraProject> jiraProjects) {
        this.jiraProjects = jiraProjects;
    }

    public String getJiraProjectsError() {
        return jiraProjectsError;
    }

    public void setJiraProjectsError(final String jiraProjectsError) {
        this.jiraProjectsError = jiraProjectsError;
    }

    public List<HubProject> getHubProjects() {
        return hubProjects;
    }

    public void setHubProjects(final List<HubProject> hubProjects) {
        this.hubProjects = hubProjects;
    }

    public String getHubProjectsError() {
        return hubProjectsError;
    }

    public void setHubProjectsError(final String hubProjectsError) {
        this.hubProjectsError = hubProjectsError;
    }

    public Set<HubProjectMapping> getHubProjectMappings() {
        return hubProjectMappings;
    }

    public void setHubProjectMappings(final Set<HubProjectMapping> hubProjectMappings) {
        this.hubProjectMappings = hubProjectMappings;
    }

    public void setHubProjectMappingsJson(final String hubProjectMappingsJson) {
        if (StringUtils.isNotBlank(hubProjectMappingsJson)) {
            final Gson gson = new GsonBuilder().create();
            final Type mappingType = new TypeToken<Set<HubProjectMapping>>() {
            }.getType();
            this.hubProjectMappings = gson.fromJson(hubProjectMappingsJson, mappingType);
        }
    }

    public String getHubProjectMappingsJson() {
        if (hubProjectMappings != null) {
            final Gson gson = new GsonBuilder().create();
            return gson.toJson(hubProjectMappings);
        }
        return null;
    }

    public String toJson() {
        final Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
    }

    public static ErrorTracking fromJson(final String jsonString) {
        if (StringUtils.isNotBlank(jsonString)) {
            final Gson gson = new GsonBuilder().create();
            final ErrorTracking config = gson.fromJson(jsonString, HubJiraConfigSerializable.class);
            return config;
        }
        return null;
    }

    public String getHubProjectMappingError() {
        return hubProjectMappingError;
    }

    public void setHubProjectMappingError(final String hubProjectMappingError) {
        this.hubProjectMappingError = hubProjectMappingError;
    }

    public List<PolicyRuleSerializable> getPolicyRules() {
        return policyRules;
    }

    public void setPolicyRules(final List<PolicyRuleSerializable> policyRules) {
        this.policyRules = policyRules;
    }

    public void setPolicyRulesJson(final String policyRulesJson) {
        if (StringUtils.isNotBlank(policyRulesJson)) {
            final Gson gson = new GsonBuilder().create();
            final Type mappingType = new TypeToken<List<PolicyRuleSerializable>>() {
            }.getType();
            this.policyRules = gson.fromJson(policyRulesJson, mappingType);
        }
    }

    public String getPolicyRulesJson() {
        if (policyRules != null) {
            final Gson gson = new GsonBuilder().create();
            return gson.toJson(policyRules);
        }
        return null;
    }

    public String getPolicyRulesError() {
        return policyRulesError;
    }

    public void setPolicyRulesError(final String policyRulesError) {
        this.policyRulesError = policyRulesError;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
        result = prime * result + ((hubProjectMappingError == null) ? 0 : hubProjectMappingError.hashCode());
        result = prime * result + ((hubProjectMappings == null) ? 0 : hubProjectMappings.hashCode());
        result = prime * result + ((hubProjects == null) ? 0 : hubProjects.hashCode());
        result = prime * result + ((hubProjectsError == null) ? 0 : hubProjectsError.hashCode());
        result = prime * result + ((intervalBetweenChecks == null) ? 0 : intervalBetweenChecks.hashCode());
        result = prime * result + ((intervalBetweenChecksError == null) ? 0 : intervalBetweenChecksError.hashCode());
        result = prime * result + ((jiraProjects == null) ? 0 : jiraProjects.hashCode());
        result = prime * result + ((jiraProjectsError == null) ? 0 : jiraProjectsError.hashCode());
        result = prime * result + ((policyRules == null) ? 0 : policyRules.hashCode());
        result = prime * result + ((policyRulesError == null) ? 0 : policyRulesError.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HubJiraConfigSerializable)) {
            return false;
        }
        final HubJiraConfigSerializable other = (HubJiraConfigSerializable) obj;
        if (errorMessage == null) {
            if (other.errorMessage != null) {
                return false;
            }
        } else if (!errorMessage.equals(other.errorMessage)) {
            return false;
        }
        if (hubProjectMappingError == null) {
            if (other.hubProjectMappingError != null) {
                return false;
            }
        } else if (!hubProjectMappingError.equals(other.hubProjectMappingError)) {
            return false;
        }
        if (hubProjectMappings == null) {
            if (other.hubProjectMappings != null) {
                return false;
            }
        } else if (!hubProjectMappings.equals(other.hubProjectMappings)) {
            return false;
        }
        if (hubProjects == null) {
            if (other.hubProjects != null) {
                return false;
            }
        } else if (!hubProjects.equals(other.hubProjects)) {
            return false;
        }
        if (hubProjectsError == null) {
            if (other.hubProjectsError != null) {
                return false;
            }
        } else if (!hubProjectsError.equals(other.hubProjectsError)) {
            return false;
        }
        if (intervalBetweenChecks == null) {
            if (other.intervalBetweenChecks != null) {
                return false;
            }
        } else if (!intervalBetweenChecks.equals(other.intervalBetweenChecks)) {
            return false;
        }
        if (intervalBetweenChecksError == null) {
            if (other.intervalBetweenChecksError != null) {
                return false;
            }
        } else if (!intervalBetweenChecksError.equals(other.intervalBetweenChecksError)) {
            return false;
        }
        if (jiraProjects == null) {
            if (other.jiraProjects != null) {
                return false;
            }
        } else if (!jiraProjects.equals(other.jiraProjects)) {
            return false;
        }
        if (jiraProjectsError == null) {
            if (other.jiraProjectsError != null) {
                return false;
            }
        } else if (!jiraProjectsError.equals(other.jiraProjectsError)) {
            return false;
        }
        if (policyRules == null) {
            if (other.policyRules != null) {
                return false;
            }
        } else if (!policyRules.equals(other.policyRules)) {
            return false;
        }
        if (policyRulesError == null) {
            if (other.policyRulesError != null) {
                return false;
            }
        } else if (!policyRulesError.equals(other.policyRulesError)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("HubJiraConfigSerializable [errorMessage=");
        builder.append(errorMessage);
        builder.append(", intervalBetweenChecks=");
        builder.append(intervalBetweenChecks);
        builder.append(", intervalBetweenChecksError=");
        builder.append(intervalBetweenChecksError);
        builder.append(", jiraProjects=");
        builder.append(jiraProjects);
        builder.append(", jiraProjectsError=");
        builder.append(jiraProjectsError);
        builder.append(", hubProjects=");
        builder.append(hubProjects);
        builder.append(", hubProjectsError=");
        builder.append(hubProjectsError);
        builder.append(", hubProjectMappings=");
        builder.append(hubProjectMappings);
        builder.append(", hubProjectMappingError=");
        builder.append(hubProjectMappingError);
        builder.append(", policyRules=");
        builder.append(policyRules);
        builder.append(", policyRulesError=");
        builder.append(policyRulesError);
        builder.append("]");
        return builder.toString();
    }

}
