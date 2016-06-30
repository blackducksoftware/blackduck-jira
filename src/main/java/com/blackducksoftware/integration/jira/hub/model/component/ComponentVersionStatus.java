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
package com.blackducksoftware.integration.jira.hub.model.component;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.util.HubUrlParser;
import com.google.gson.annotations.SerializedName;

public class ComponentVersionStatus {
	public static final String COMPONENT_URL_IDENTIFIER = "components";
	public static final String COMPONENT_VERSION_URL_IDENTIFIER = "versions";

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

	public UUID getComponentId() throws MissingUUIDException {
		if (StringUtils.isBlank(getComponentVersionLink())) {
			return null;
		}
		return HubUrlParser.getUUIDFromURLString(COMPONENT_URL_IDENTIFIER, getComponentVersionLink());
	}

	public UUID getComponentVersionId() throws MissingUUIDException {
		if (StringUtils.isBlank(getComponentVersionLink())) {
			return null;
		}
		return HubUrlParser.getUUIDFromURLString(COMPONENT_VERSION_URL_IDENTIFIER, getComponentVersionLink());
	}

	@Override
	public String toString() {
		return "ComponentVersionStatus [componentName=" + componentName + ", componentVersion=" + componentVersionLink
				+ ", bomComponentVersionPolicyStatusLink=" + bomComponentVersionPolicyStatusLink + "]";
	}

}
