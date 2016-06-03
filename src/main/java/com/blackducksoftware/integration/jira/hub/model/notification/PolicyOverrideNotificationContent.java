package com.blackducksoftware.integration.jira.hub.model.notification;

import com.google.gson.annotations.SerializedName;

public class PolicyOverrideNotificationContent {
	private String projectName;
	private String projectVersionName;
	private String componentName;
	private String componentVersionName;
	private String firstName;
	private String lastName;

	@SerializedName("projectVersion")
	private String projectVersionLink;

	@SerializedName("componentVersion")
	private String componentVersionLink;

	@SerializedName("bomComponentVersionPolicyStatus")
	private String bomComponentVersionPolicyStatusLink;

	public String getProjectName() {
		return projectName;
	}

	public String getProjectVersionName() {
		return projectVersionName;
	}

	public String getComponentName() {
		return componentName;
	}

	public String getComponentVersionName() {
		return componentVersionName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getProjectVersionLink() {
		return projectVersionLink;
	}

	public String getComponentVersionLink() {
		return componentVersionLink;
	}

	public String getBomComponentVersionPolicyStatusLink() {
		return bomComponentVersionPolicyStatusLink;
	}

	@Override
	public String toString() {
		return "PolicyOverrideNotificationContent [projectName=" + projectName + ", projectVersionName="
				+ projectVersionName + ", componentName=" + componentName + ", componentVersionName="
				+ componentVersionName + ", firstName=" + firstName + ", lastName=" + lastName
				+ ", projectVersionLink=" + projectVersionLink + ", componentVersionLink=" + componentVersionLink
				+ ", bomComponentVersionPolicyStatusLink=" + bomComponentVersionPolicyStatusLink + "]";
	}

}
