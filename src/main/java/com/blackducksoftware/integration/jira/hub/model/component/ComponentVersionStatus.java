package com.blackducksoftware.integration.jira.hub.model.component;

import com.google.gson.annotations.SerializedName;

public class ComponentVersionStatus {
	private String componentName;

	@SerializedName("componentVersion")
	private String componentVersionLink;

	@SerializedName("bomComponentVersionPolicyStatus")
	private String bomComponentVersionPolicyStatusLink;

	public String getComponentName() {
		return componentName;
	}

	public String getComponentVersionLink() {
		return componentVersionLink;
	}

	public String getBomComponentVersionPolicyStatusLink() {
		return bomComponentVersionPolicyStatusLink;
	}

	public void setComponentName(final String componentName) {
		this.componentName = componentName;
	}

	public void setComponentVersionLink(final String componentVersionLink) {
		this.componentVersionLink = componentVersionLink;
	}

	public void setBomComponentVersionPolicyStatusLink(final String bomComponentVersionPolicyStatusLink) {
		this.bomComponentVersionPolicyStatusLink = bomComponentVersionPolicyStatusLink;
	}

	@Override
	public String toString() {
		return "ComponentVersionStatus [componentName=" + componentName + ", componentVersion=" + componentVersionLink
				+ ", bomComponentVersionPolicyStatusLink=" + bomComponentVersionPolicyStatusLink + "]";
	}

}
