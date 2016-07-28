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
package com.blackducksoftware.integration.jira.hub;

import java.util.ArrayList;
import java.util.List;

/**
 * The set of events generated from one or more notifications, grouped into
 * categories.
 *
 * @author sbillings
 *
 */
public class HubEvents {

	private final List<HubEvent> policyViolationEvents = new ArrayList<>();
	private final List<HubEvent> policyViolationOverrideEvents = new ArrayList<>();
	private final List<HubEvent> vulnerabilityEvents = new ArrayList<>();

	public List<HubEvent> getPolicyViolationEvents() {
		return policyViolationEvents;
	}

	public List<HubEvent> getPolicyViolationOverrideEvents() {
		return policyViolationOverrideEvents;
	}

	public List<HubEvent> getVulnerabilityEvents() {
		return vulnerabilityEvents;
	}

	public void addPolicyViolationEvent(final HubEvent notificationEvent) {
		policyViolationEvents.add(notificationEvent);
	}

	public void addPolicyViolationOverrideEvent(final HubEvent notificationEvent) {
		policyViolationOverrideEvents.add(notificationEvent);
	}

	public void addVulnerabilityEvent(final HubEvent notificationEvent) {
		vulnerabilityEvents.add(notificationEvent);
	}

	public void addAllEvents(final HubEvents results) {
		if (results != null) {
			policyViolationEvents.addAll(results.getPolicyViolationEvents());
			policyViolationOverrideEvents.addAll(results.getPolicyViolationOverrideEvents());
			vulnerabilityEvents.addAll(results.getVulnerabilityEvents());
		}
	}

	@Override
	public String toString() {
		return "FilteredNotificationEvents [policyViolationEvents=" + policyViolationEvents
				+ ", policyViolationOverrideEvents=" + policyViolationOverrideEvents + ", vulnerabilityEvents="
				+ vulnerabilityEvents + "]";
	}

}
