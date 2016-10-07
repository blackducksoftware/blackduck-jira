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
package com.blackducksoftware.integration.jira.task.conversion.vulncomprestservice;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.google.gson.annotations.SerializedName;

public class VulnerableBomComponentItem extends HubItem {

	private String componentName;
	private String componentVersionName;

	@SerializedName("componentVersion")
	private String componentVersionLink;

	private VulnerabilityWithRemediation vulnerabilityWithRemediation;

	// Also in Hub's response: License

	public VulnerableBomComponentItem(final MetaInformation meta, final String componentName,
			final String componentVersionName, final String componentVersionLink,
			final VulnerabilityWithRemediation vulnerabilityWithRemediation) {
		super(meta);

		this.componentName = componentName;
		this.componentVersionName = componentVersionName;
		this.componentVersionLink = componentVersionLink;
		this.vulnerabilityWithRemediation = vulnerabilityWithRemediation;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(final String componentName) {
		this.componentName = componentName;
	}

	public String getComponentVersionName() {
		return componentVersionName;
	}

	public void setComponentVersionName(final String componentVersionName) {
		this.componentVersionName = componentVersionName;
	}

	public String getComponentVersionLink() {
		return componentVersionLink;
	}

	public void setComponentVersionLink(final String componentVersionLink) {
		this.componentVersionLink = componentVersionLink;
	}

	public VulnerabilityWithRemediation getVulnerabilityWithRemediation() {
		return vulnerabilityWithRemediation;
	}

	public void setVulnerabilityWithRemediation(final VulnerabilityWithRemediation vulnerabilityWithRemediation) {
		this.vulnerabilityWithRemediation = vulnerabilityWithRemediation;
	}

	@Override
	public String toString() {
		return "VulnerableBomComponentItem [componentName=" + componentName + ", componentVersionName="
				+ componentVersionName + ", componentVersionLink=" + componentVersionLink
				+ ", vulnerabilityWithRemediation=" + vulnerabilityWithRemediation + "]";
	}
}
