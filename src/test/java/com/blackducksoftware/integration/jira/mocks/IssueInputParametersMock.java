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
package com.blackducksoftware.integration.jira.mocks;

import java.util.Collection;
import java.util.Map;

import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;

public class IssueInputParametersMock implements IssueInputParameters {

	private Long projectId;
	private String issueTypeId;
	private String summary;
	private String reporterId;
	private String description;


	@Override
	public String getDescription() {

		return description;
	}

	@Override
	public IssueInputParameters setDescription(final String description) {
		this.description = description;
		return this;
	}

	@Override
	public IssueInputParameters setProjectId(final Long projectId) {
		this.projectId = projectId;
		return this;
	}

	@Override
	public IssueInputParameters setReporterId(final String reporterId) {
		this.reporterId = reporterId;
		return this;
	}

	@Override
	public IssueInputParameters setSummary(final String summary) {
		this.summary = summary;
		return this;
	}

	@Override
	public String getReporterId() {

		return reporterId;
	}

	@Override
	public Long getProjectId() {

		return projectId;
	}

	@Override
	public String getSummary() {

		return summary;
	}

	@Override
	public String getIssueTypeId() {

		return issueTypeId;
	}

	@Override
	public IssueInputParameters setIssueTypeId(final String issueTypeId) {
		this.issueTypeId = issueTypeId;
		return this;
	}

	@Override
	public IssueInputParameters addCustomFieldValue(final Long arg0, final String... arg1) {

		return null;
	}

	@Override
	public IssueInputParameters addCustomFieldValue(final String arg0, final String... arg1) {

		return null;
	}

	@Override
	public void addFieldToForcePresent(final String arg0) {

	}

	@Override
	public boolean applyDefaultValuesWhenParameterNotProvided() {

		return false;
	}

	@Override
	public Map<String, String[]> getActionParameters() {

		return null;
	}

	@Override
	public Long[] getAffectedVersionIds() {

		return null;
	}

	@Override
	public String getAssigneeId() {

		return null;
	}

	@Override
	public String getCommentValue() {

		return null;
	}

	@Override
	public Long[] getComponentIds() {

		return null;
	}

	@Override
	public String[] getCustomFieldValue(final Long arg0) {

		return null;
	}

	@Override
	public String[] getCustomFieldValue(final String arg0) {

		return null;
	}


	@Override
	public String getDueDate() {

		return null;
	}

	@Override
	public String getEnvironment() {

		return null;
	}

	@Override
	public Map<String, Object> getFieldValuesHolder() {

		return null;
	}

	@Override
	public Long[] getFixVersionIds() {

		return null;
	}

	@Override
	public String getFormToken() {

		return null;
	}

	@Override
	public HistoryMetadata getHistoryMetadata() {

		return null;
	}


	@Override
	public Long getOriginalEstimate() {

		return null;
	}

	@Override
	public String getOriginalEstimateAsDurationString() {

		return null;
	}

	@Override
	public String getPriorityId() {

		return null;
	}


	@Override
	public Collection<String> getProvidedFields() {

		return null;
	}

	@Override
	public Long getRemainingEstimate() {

		return null;
	}

	@Override
	public String getRemainingEstimateAsDurationString() {

		return null;
	}


	@Override
	public String getResolutionDate() {

		return null;
	}

	@Override
	public String getResolutionId() {

		return null;
	}

	@Override
	public Long getSecurityLevelId() {

		return null;
	}

	@Override
	public String getStatusId() {

		return null;
	}

	@Override
	public Long getTimeSpent() {

		return null;
	}

	@Override
	public boolean isFieldPresent(final String arg0) {

		return false;
	}

	@Override
	public boolean isFieldSet(final String arg0) {

		return false;
	}

	@Override
	public boolean onlyValidatePresentFieldsWhenRetainingExistingValues() {

		return false;
	}

	@Override
	public boolean retainExistingValuesWhenParameterNotProvided() {

		return false;
	}

	@Override
	public IssueInputParameters setAffectedVersionIds(final Long... arg0) {

		return null;
	}

	@Override
	public void setApplyDefaultValuesWhenParameterNotProvided(final boolean arg0) {


	}

	@Override
	public IssueInputParameters setAssigneeId(final String arg0) {

		return null;
	}

	@Override
	public IssueInputParameters setComment(final String arg0) {

		return null;
	}

	@Override
	public IssueInputParameters setComment(final String arg0, final Long arg1) {

		return null;
	}

	@Override
	public IssueInputParameters setComment(final String arg0, final String arg1) {

		return null;
	}

	@Override
	public IssueInputParameters setComponentIds(final Long... arg0) {

		return null;
	}


	@Override
	public IssueInputParameters setDueDate(final String arg0) {

		return null;
	}

	@Override
	public IssueInputParameters setEnvironment(final String arg0) {

		return null;
	}

	@Override
	public void setFieldValuesHolder(final Map<String, Object> arg0) {


	}

	@Override
	public IssueInputParameters setFixVersionIds(final Long... arg0) {

		return null;
	}

	@Override
	public IssueInputParameters setHistoryMetadata(final HistoryMetadata arg0) {

		return null;
	}

	@Override
	public IssueInputParameters setOriginalAndRemainingEstimate(final String arg0, final String arg1) {

		return null;
	}

	@Override
	public IssueInputParameters setOriginalAndRemainingEstimate(final Long arg0, final Long arg1) {

		return null;
	}

	@Override
	public IssueInputParameters setOriginalEstimate(final Long arg0) {

		return null;
	}

	@Override
	public IssueInputParameters setOriginalEstimate(final String arg0) {

		return null;
	}

	@Override
	public IssueInputParameters setPriorityId(final String arg0) {

		return null;
	}


	@Override
	public void setProvidedFields(final Collection<String> arg0) {


	}

	@Override
	public IssueInputParameters setRemainingEstimate(final String arg0) {

		return null;
	}

	@Override
	public IssueInputParameters setRemainingEstimate(final Long arg0) {

		return null;
	}


	@Override
	public IssueInputParameters setResolutionDate(final String arg0) {

		return null;
	}

	@Override
	public IssueInputParameters setResolutionId(final String arg0) {

		return null;
	}

	@Override
	public void setRetainExistingValuesWhenParameterNotProvided(final boolean arg0) {


	}

	@Override
	public void setRetainExistingValuesWhenParameterNotProvided(final boolean arg0, final boolean arg1) {


	}

	@Override
	public IssueInputParameters setSecurityLevelId(final Long arg0) {

		return null;
	}

	@Override
	public void setSkipScreenCheck(final boolean arg0) {


	}

	@Override
	public IssueInputParameters setStatusId(final String arg0) {

		return null;
	}


	@Override
	public IssueInputParameters setTimeSpent(final Long arg0) {

		return null;
	}

	@Override
	public boolean skipScreenCheck() {

		return false;
	}

}
