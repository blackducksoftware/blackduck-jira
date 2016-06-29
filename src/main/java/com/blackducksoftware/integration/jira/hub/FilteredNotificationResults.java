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

}
