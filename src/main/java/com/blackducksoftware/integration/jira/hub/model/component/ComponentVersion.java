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

import java.util.Date;

import com.blackducksoftware.integration.hub.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ComponentVersion extends HubItem {
	private String versionName;
	private Date releasedOn;

	public ComponentVersion(final MetaInformation meta) {
		super(meta);
	}

	// License goes here

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(final String versionName) {
		this.versionName = versionName;
	}

	public Date getReleasedOn() {
		return releasedOn;
	}

	public void setReleasedOn(final Date releasedOn) {
		this.releasedOn = releasedOn;
	}

	@Override
	public String toString() {
		return "ComponentVersion [versionName=" + versionName + ", releasedOn=" + releasedOn + "]";
	}

}
