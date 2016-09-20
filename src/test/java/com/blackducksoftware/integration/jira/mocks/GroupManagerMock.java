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

import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.exception.embedded.InvalidGroupException;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;

public class GroupManagerMock implements GroupManager {

	private final HashMap<String, String> groupMap = new HashMap<>();

	private boolean groupCreateAttempted;

	public void addGroupByName(final String groupName) {
		groupMap.put(groupName, groupName);
	}

	public boolean getGroupCreateAttempted() {
		return groupCreateAttempted;
	}

	@Override
	public void addUserToGroup(final User arg0, final Group arg1) throws GroupNotFoundException, UserNotFoundException,
			OperationNotPermittedException, OperationFailedException {

	}

	@Override
	public Group createGroup(final String arg0) throws OperationNotPermittedException, InvalidGroupException {
		groupCreateAttempted = true;
		return null;
	}

	@Override
	public Collection<Group> getAllGroups() {

		return null;
	}

	@Override
	public Collection<User> getDirectUsersInGroup(final Group arg0) {

		return null;
	}

	@Override
	public Group getGroup(final String arg0) {

		return null;
	}

	@Override
	public Group getGroupEvenWhenUnknown(final String arg0) {

		return null;
	}

	@Override
	public Collection<String> getGroupNamesForUser(final String arg0) {

		return null;
	}

	@Override
	public Collection<String> getGroupNamesForUser(final User arg0) {

		return null;
	}

	@Override
	public Collection<String> getGroupNamesForUser(final ApplicationUser arg0) {

		return null;
	}

	@Override
	public Group getGroupObject(final String arg0) {

		return null;
	}

	@Override
	public Collection<Group> getGroupsForUser(final String arg0) {

		return null;
	}

	@Override
	public Collection<Group> getGroupsForUser(final User arg0) {

		return null;
	}

	@Override
	public Collection<String> getUserNamesInGroup(final Group arg0) {

		return null;
	}

	@Override
	public Collection<String> getUserNamesInGroup(final String arg0) {

		return null;
	}

	@Override
	public Collection<User> getUsersInGroup(final String arg0) {

		return null;
	}

	@Override
	public Collection<User> getUsersInGroup(final Group arg0) {

		return null;
	}

	@Override
	public boolean groupExists(final String groupName) {
		if (StringUtils.isNotBlank(groupMap.get(groupName))) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isUserInGroup(final String arg0, final String arg1) {

		return false;
	}

	@Override
	public boolean isUserInGroup(final User arg0, final Group arg1) {

		return false;
	}

	@Override
	public boolean isUserInGroup(final User arg0, final String arg1) {

		return false;
	}

}
