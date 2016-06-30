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
