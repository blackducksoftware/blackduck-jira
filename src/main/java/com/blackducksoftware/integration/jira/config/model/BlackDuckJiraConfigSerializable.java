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
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.jira.common.model.BlackDuckProjectMapping;
import com.blackducksoftware.integration.jira.common.model.JiraProject;
import com.blackducksoftware.integration.jira.common.model.PolicyRuleSerializable;
import com.blackducksoftware.integration.jira.config.ErrorTracking;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.util.Stringable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BlackDuckJiraConfigSerializable extends Stringable implements Serializable, ErrorTracking {
    private static final long serialVersionUID = -8798933499737490438L;

    @XmlElement
    private String errorMessage;

    @XmlElement
    private String intervalBetweenChecks;

    @XmlElement
    private String generalSettingsError;

    @XmlElement
    private List<JiraProject> jiraProjects;

    @XmlElement
    private String jiraProjectsError;

    @XmlElement
    private SortedSet<String> creatorCandidates;

    @XmlElement
    private String creator;

    @XmlElement
    private List<String> hubProjects;

    @XmlElement
    private String hubProjectsError;

    @XmlElement
    private Set<BlackDuckProjectMapping> hubProjectMappings;

    @XmlElement
    private String hubProjectMappingError;

    @XmlElement
    private List<PolicyRuleSerializable> policyRules;

    @XmlElement
    private boolean createVulnerabilityIssues;

    @XmlElement
    private String policyRulesError;

    @XmlElement
    private String createVulnerabilityIssuesError;

    @XmlElement
    private boolean commentOnIssueUpdatesChoice;

    @XmlElement
    private String commentOnIssueUpdatesChoiceError;

    @XmlElement
    private boolean projectReviewerNotificationsChoice;

    @XmlElement
    private String projectReviewerNotificationsChoiceError;

    public BlackDuckJiraConfigSerializable() {
    }

    public static ErrorTracking fromJson(final String jsonString) {
        if (StringUtils.isNotBlank(jsonString)) {
            final Gson gson = new GsonBuilder().create();
            final ErrorTracking config = gson.fromJson(jsonString, BlackDuckJiraConfigSerializable.class);
            return config;
        }
        return null;
    }

    @Override
    public boolean hasErrors() {
        boolean hasErrors = false;
        if (StringUtils.isNotBlank(getErrorMessage())) {
            hasErrors = true;
        }
        if (StringUtils.isNotBlank(getGeneralSettingsError())) {
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

    public void enhanceMappingErrorMessage() {
        final StringBuilder sb = new StringBuilder();
        addMsg(sb, null, getHubProjectMappingError());
        addMsg(sb, "JIRA Project Error", getJiraProjectsError());
        addMsg(sb, "Black Duck Project Error", getHubProjectsError());
        final String msg = sb.toString();
        if (!StringUtils.isBlank(msg)) {
            setHubProjectMappingError(msg);
        }

    }

    private void addMsg(final StringBuilder sb, final String label, final String msg) {
        if (StringUtils.isBlank(msg)) {
            return;
        }
        if (sb.length() > 0) {
            sb.append("; ");
        }
        if (!StringUtils.isBlank(label)) {
            sb.append(label);
            sb.append(": ");
        }
        sb.append(msg);
    }

    public String getConsolidatedErrorMessage() {
        if (!hasErrors()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(getErrorMessage())) {
            if (sb.length() > 0) {
                sb.append("; ErrorMessage: " + getErrorMessage());
            }
            sb.append(getErrorMessage());
        }
        if (StringUtils.isNotBlank(getGeneralSettingsError())) {
            if (sb.length() > 0) {
                sb.append("; IntervalBetweenChecksError: " + getGeneralSettingsError());
            }
            sb.append(getGeneralSettingsError());
        }
        if (StringUtils.isNotBlank(getJiraProjectsError())) {
            if (sb.length() > 0) {
                sb.append("; JiraProjectsError: " + getJiraProjectsError());
            }
            sb.append(getJiraProjectsError());
        }
        if (StringUtils.isNotBlank(getHubProjectsError())) {
            if (sb.length() > 0) {
                sb.append("; BlackDuckProjectsError: " + getHubProjectsError());
            }
            sb.append(getHubProjectsError());
        }
        if (StringUtils.isNotBlank(getHubProjectMappingError())) {
            if (sb.length() > 0) {
                sb.append("; BlackDuckProjectMappingError: " + getHubProjectMappingError());
            }
            sb.append(getHubProjectMappingError());
        }
        if (StringUtils.isNotBlank(getPolicyRulesError())) {
            if (sb.length() > 0) {
                sb.append("; PolicyRulesError: " + getPolicyRulesError());
            }
            sb.append(getPolicyRulesError());
        }
        return sb.toString();
    }

    public String getIntervalBetweenChecks() {
        return intervalBetweenChecks;
    }

    public void setIntervalBetweenChecks(final String intervalBetweenChecks) {
        this.intervalBetweenChecks = intervalBetweenChecks;
    }

    public String getGeneralSettingsError() {
        return generalSettingsError;
    }

    public void setGeneralSettingsError(final String generalSettingsError) {
        this.generalSettingsError = generalSettingsError;
    }

    public List<JiraProject> getJiraProjects() {
        return jiraProjects;
    }

    public void setJiraProjects(final List<JiraProject> jiraProjects) {
        this.jiraProjects = jiraProjects;
    }

    public SortedSet<String> getCreatorCandidates() {
        return creatorCandidates;
    }

    public void setCreatorCandidates(final SortedSet<String> creatorCandidates) {
        this.creatorCandidates = creatorCandidates;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(final String creator) {
        this.creator = creator;
    }

    public String getJiraProjectsError() {
        return jiraProjectsError;
    }

    public void setJiraProjectsError(final String jiraProjectsError) {
        this.jiraProjectsError = jiraProjectsError;
    }

    public List<String> getHubProjects() {
        return hubProjects;
    }

    public void setHubProjects(final List<String> hubProjects) {
        this.hubProjects = hubProjects;
    }

    public String getHubProjectsError() {
        return hubProjectsError;
    }

    public void setHubProjectsError(final String hubProjectsError) {
        this.hubProjectsError = hubProjectsError;
    }

    public Set<BlackDuckProjectMapping> getHubProjectMappings() {
        return hubProjectMappings;
    }

    public void setHubProjectMappings(final Set<BlackDuckProjectMapping> hubProjectMappings) {
        this.hubProjectMappings = hubProjectMappings;
    }

    public String getHubProjectMappingsJson() {
        if (hubProjectMappings != null) {
            final Gson gson = new GsonBuilder().create();
            return gson.toJson(hubProjectMappings);
        }
        return null;
    }

    public void setHubProjectMappingsJson(final String hubProjectMappingsJson) {
        if (StringUtils.isNotBlank(hubProjectMappingsJson)) {
            final Gson gson = new GsonBuilder().create();
            final Type mappingType = new TypeToken<Set<BlackDuckProjectMapping>>() {
            }.getType();
            this.hubProjectMappings = gson.fromJson(hubProjectMappingsJson, mappingType);
        }
    }

    public String toJson() {
        final Gson gson = new GsonBuilder().create();
        return gson.toJson(this);
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

    public boolean isCreateVulnerabilityIssues() {
        return createVulnerabilityIssues;
    }

    public void setCreateVulnerabilityIssues(final boolean createVulnerabilityIssues) {
        this.createVulnerabilityIssues = createVulnerabilityIssues;
    }

    public String getPolicyRulesJson() {
        if (policyRules != null) {
            final Gson gson = new GsonBuilder().create();
            return gson.toJson(policyRules);
        }
        return null;
    }

    public void setPolicyRulesJson(final String policyRulesJson) {
        if (StringUtils.isNotBlank(policyRulesJson)) {
            final Gson gson = new GsonBuilder().create();
            final Type mappingType = new TypeToken<List<PolicyRuleSerializable>>() {
            }.getType();
            this.policyRules = gson.fromJson(policyRulesJson, mappingType);
        }
    }

    public String getPolicyRulesError() {
        return policyRulesError;
    }

    public void setPolicyRulesError(final String policyRulesError) {
        this.policyRulesError = policyRulesError;
    }

    public String getCreateVulnerabilityIssuesError() {
        return createVulnerabilityIssuesError;
    }

    public void setCreateVulnerabilityIssuesError(final String createVulnerabilityIssuesError) {
        this.createVulnerabilityIssuesError = createVulnerabilityIssuesError;
    }

    public boolean getCommentOnIssueUpdatesChoice() {
        return commentOnIssueUpdatesChoice;
    }

    public void setCommentOnIssueUpdatesChoice(final boolean commentOnIssueUpdatesChoice) {
        this.commentOnIssueUpdatesChoice = commentOnIssueUpdatesChoice;
    }

    public String getCommentOnIssueUpdatesChoiceError() {
        return commentOnIssueUpdatesChoiceError;
    }

    public void setCommentOnIssueUpdatesChoiceError(final String commentOnIssueUpdatesChoiceError) {
        this.commentOnIssueUpdatesChoiceError = commentOnIssueUpdatesChoiceError;
    }

    public boolean getProjectReviewerNotificationsChoice() {
        return projectReviewerNotificationsChoice;
    }

    public void setProjectReviewerNotificationsChoice(final boolean projectReviewerNotificationsChoice) {
        this.projectReviewerNotificationsChoice = projectReviewerNotificationsChoice;
    }

    public String getProjectReviewerNotificationsChoiceError() {
        return projectReviewerNotificationsChoiceError;
    }

    public void setProjectReviewerNotificationsChoiceError(final String projectReviewerNotificationsChoiceError) {
        this.projectReviewerNotificationsChoiceError = projectReviewerNotificationsChoiceError;
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
        final StringBuilder builder = new StringBuilder();
        builder.append("BlackDuckJiraConfigSerializable [errorMessage=");
        builder.append(errorMessage);
        builder.append(", intervalBetweenChecks=");
        builder.append(intervalBetweenChecks);
        builder.append(", generalSettingsError=");
        builder.append(generalSettingsError);
        builder.append(", jiraProjects=");
        builder.append(jiraProjects);
        builder.append(", jiraProjectsError=");
        builder.append(jiraProjectsError);
        builder.append(", creatorCandidates=");
        builder.append(creatorCandidates);
        builder.append(", creator=");
        builder.append(creator);
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
        builder.append(", commentOnIssueUpdatesChoice=");
        builder.append(commentOnIssueUpdatesChoice);
        builder.append(", commentOnIssueUpdatesChoiceError=");
        builder.append(commentOnIssueUpdatesChoiceError);
        builder.append(", projectReviewerNotificationsChoice=");
        builder.append(projectReviewerNotificationsChoice);
        builder.append(", projectReviewerNotificationsChoiceError=");
        builder.append(projectReviewerNotificationsChoiceError);
        builder.append("]");
        return builder.toString();
    }

}
