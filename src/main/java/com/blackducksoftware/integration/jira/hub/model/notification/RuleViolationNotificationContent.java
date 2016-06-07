package com.blackducksoftware.integration.jira.hub.model.notification;

import java.util.List;

import com.blackducksoftware.integration.jira.hub.model.component.ComponentVersionStatus;
import com.google.gson.annotations.SerializedName;

public class RuleViolationNotificationContent {
	private String projectName;
	private String projectVersionName;
	private int componentVersionsInViolation;
	private List<ComponentVersionStatus> componentVersionStatuses;

	@SerializedName("projectVersion")
	private String projectVersionLink;

	public String getProjectName() {
		return projectName;
	}

	public String getProjectVersionName() {
		return projectVersionName;
	}

	public int getComponentVersionsInViolation() {
		return componentVersionsInViolation;
	}

	public List<ComponentVersionStatus> getComponentVersionStatuses() {
		return componentVersionStatuses;
	}

	public String getProjectVersionLink() {
		return projectVersionLink;
	}

	@Override
	public String toString() {
		return "RuleViolationNotificationContent [projectName=" + projectName + ", projectVersionName="
				+ projectVersionName + ", componentVersionsInViolation=" + componentVersionsInViolation
				+ ", componentVersionStatuses=" + componentVersionStatuses + ", projectVersionLink="
				+ projectVersionLink + "]";
	}

}
