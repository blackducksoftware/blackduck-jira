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
package com.blackducksoftware.integration.jira.mocks;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserResolutionException;


public class UserManagerUIMock implements UserManager {

	String remoteUsername;
	boolean isSystemAdmin;

	List<String> userGroups = new ArrayList<>();


	public UserManagerUIMock() {
	}


	public void addGroup(final String group) {
		userGroups.add(group);
	}

	public void setRemoteUsername(final String remoteUsername) {
		this.remoteUsername = remoteUsername;
	}

	@Override
	public String getRemoteUsername() {
		return remoteUsername;
	}

	@Override
	public String getRemoteUsername(final HttpServletRequest request) {
		return remoteUsername;
	}

	@Override
	public boolean isUserInGroup(final String username, final String group) {
		return userGroups.contains(group);
	}

	public void setIsSystemAdmin(final boolean isSystemAdmin) {
		this.isSystemAdmin = isSystemAdmin;
	}

	@Override
	public boolean isSystemAdmin(final String username) {
		return isSystemAdmin;
	}

	@Override
	public boolean authenticate(final String username, final String password) {
		return false;
	}

	@Override
	public Principal resolve(final String username) throws UserResolutionException {
		return null;
	}

}
