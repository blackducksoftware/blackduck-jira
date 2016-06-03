package com.blackducksoftware.integration.jira.hub.model.project;

import com.google.gson.annotations.SerializedName;

public class ProjectVersion {
	private String projectName;
	private String projectVersionName;

	@SerializedName("projectVersion")
	private String projectVersionLink;

	public String getProjectName() {
		return projectName;
	}

	public String getProjectVersionName() {
		return projectVersionName;
	}

	public String projectVersionLink() {
		return projectVersionLink;
	}

	@Override
	public String toString() {
		return "ProjectVersion [projectName=" + projectName + ", projectVersionName=" + projectVersionName
				+ ", projectVersionLink=" + projectVersionLink + "]";
	}

}
