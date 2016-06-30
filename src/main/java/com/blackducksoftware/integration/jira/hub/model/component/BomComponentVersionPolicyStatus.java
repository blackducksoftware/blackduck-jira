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

import com.blackducksoftware.integration.hub.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.policy.api.PolicyStatusEnum;

public class BomComponentVersionPolicyStatus extends HubItem {
	public static final String POLICY_RULE_URL = "policy-rule";
	private PolicyStatusEnum approvalStatus;

	public BomComponentVersionPolicyStatus(final MetaInformation meta) {
		super(meta);
	}

	public PolicyStatusEnum getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(final PolicyStatusEnum approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

}
