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
package com.blackducksoftware.integration.jira.hub;

import java.util.ArrayList;
import java.util.List;

public class FilteredNotificationResults {

	private final List<FilteredNotificationResult> policyViolationResults = new ArrayList<>();
	private final List<FilteredNotificationResult> policyViolationOverrideResults = new ArrayList<>();
	private final List<FilteredNotificationResult> vulnerabilityResults = new ArrayList<>();

	public List<FilteredNotificationResult> getPolicyViolationResults() {
		return policyViolationResults;
	}

	public List<FilteredNotificationResult> getPolicyViolationOverrideResults() {
		return policyViolationOverrideResults;
	}

	public List<FilteredNotificationResult> getVulnerabilityResults() {
		return vulnerabilityResults;
	}

	public void addPolicyViolationResult(final FilteredNotificationResult notificationResult) {
		policyViolationResults.add(notificationResult);
	}

	public void addPolicyViolationOverrideResult(final FilteredNotificationResult notificationResult) {
		policyViolationOverrideResults.add(notificationResult);
	}

	public void addVulnerabilityResult(final FilteredNotificationResult notificationResult) {
		vulnerabilityResults.add(notificationResult);
	}

	public void addAllResults(final FilteredNotificationResults results) {
		policyViolationResults.addAll(results.getPolicyViolationResults());
		policyViolationOverrideResults.addAll(results.getPolicyViolationOverrideResults());
		vulnerabilityResults.addAll(results.getVulnerabilityResults());
	}

	@Override
	public String toString() {
		return "FilteredNotificationResults [policyViolationResults=" + policyViolationResults
				+ ", policyViolationOverrideResults=" + policyViolationOverrideResults + ", vulnerabilityResults="
				+ vulnerabilityResults + "]";
	}

}
