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

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;

import org.restlet.data.Method;

import com.blackducksoftware.integration.hub.api.HubItemRestService;
import com.blackducksoftware.integration.hub.api.HubRequest;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class VulnerableBomComponentRestService extends HubItemRestService<VulnerableBomComponentItem> {
	private static final Type ITEM_TYPE = new TypeToken<VulnerableBomComponentItem>() {
	}.getType();
	private static final Type ITEM_LIST_TYPE = new TypeToken<List<VulnerableBomComponentItem>>() {
	}.getType();

	public VulnerableBomComponentRestService(final RestConnection restConnection, final Gson gson, final JsonParser jsonParser) {
		super(restConnection, gson, jsonParser, ITEM_TYPE, ITEM_LIST_TYPE);
	}

	public List<VulnerableBomComponentItem> getVulnerableComponentsMatchingComponentName(
			final String vulnerableBomComponentsUrl, final String componentName) throws IOException,
			URISyntaxException,
			BDRestException {
		final HubRequest itemRequest = new HubRequest(getRestConnection(), getJsonParser());
		itemRequest.setMethod(Method.GET);
		itemRequest.setUrl(vulnerableBomComponentsUrl);
		itemRequest.setQ(componentName);

		final JsonObject jsonObject = itemRequest.executeForResponseJson();
		final List<VulnerableBomComponentItem> allItems = getAll(jsonObject, itemRequest);
		return allItems;
	}
}
