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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.exception.AddException;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserUtil;

public class UserUtilMock implements UserUtil {

	private User user;

	public void setUser(final User user) {
		this.user = user;
	}

	@Override
	public void addToJiraUsePermission(final User arg0) throws PermissionException {

	}

	@Override
	public void addUserToGroup(final Group arg0, final User arg1) throws PermissionException, AddException {

	}

	@Override
	public void addUserToGroups(final Collection<Group> arg0, final User arg1)
			throws PermissionException, AddException {

	}

	@Override
	public boolean canActivateNumberOfUsers(final int arg0) {

		return false;
	}

	@Override
	public boolean canActivateUsers(final Collection<String> arg0) {

		return false;
	}

	@Override
	public void changePassword(final User arg0, final String arg1) throws UserNotFoundException,
			InvalidCredentialException, OperationNotPermittedException, PermissionException {

	}

	@Override
	public void clearActiveUserCount() {

	}

	@Override
	public User createUserNoNotification(final String arg0, final String arg1, final String arg2, final String arg3)
			throws PermissionException, CreateException {

		return null;
	}

	@Override
	public User createUserNoNotification(final String arg0, final String arg1, final String arg2, final String arg3,
			final Long arg4) throws PermissionException, CreateException {

		return null;
	}

	@Override
	public User createUserWithNotification(final String arg0, final String arg1, final String arg2, final String arg3,
			final int arg4) throws PermissionException, CreateException {

		return null;
	}

	@Override
	public User createUserWithNotification(final String arg0, final String arg1, final String arg2, final String arg3,
			final Long arg4, final int arg5) throws PermissionException, CreateException {

		return null;
	}

	@Override
	public PasswordResetToken generatePasswordResetToken(final User arg0) {

		return null;
	}

	@Override
	public int getActiveUserCount() {

		return 0;
	}

	@Override
	public Collection<User> getAdministrators() {

		return null;
	}

	@Override
	public Collection<ApplicationUser> getAllApplicationUsers() {

		return null;
	}

	@Override
	public Set<User> getAllUsers() {

		return null;
	}

	@Override
	public SortedSet<User> getAllUsersInGroupNames(final Collection<String> arg0) {

		return null;
	}

	@Override
	public Set<User> getAllUsersInGroupNamesUnsorted(final Collection<String> arg0) {

		return null;
	}

	@Override
	public SortedSet<User> getAllUsersInGroups(final Collection<Group> arg0) {

		return null;
	}

	@Override
	public Collection<ProjectComponent> getComponentsUserLeads(final User arg0) {

		return null;
	}

	@Override
	public Collection<ProjectComponent> getComponentsUserLeads(final ApplicationUser arg0) {

		return null;
	}

	@Override
	public String getDisplayableNameSafely(final User arg0) {

		return null;
	}

	@Override
	public String getDisplayableNameSafely(final ApplicationUser arg0) {

		return null;
	}

	@Override
	public Group getGroup(final String arg0) {

		return null;
	}

	@Override
	public SortedSet<String> getGroupNamesForUser(final String arg0) {

		return null;
	}

	@Override
	public Group getGroupObject(final String arg0) {

		return null;
	}

	@Override
	public SortedSet<Group> getGroupsForUser(final String arg0) {

		return null;
	}

	@Override
	public Collection<User> getJiraAdministrators() {

		return null;
	}

	@Override
	public Collection<User> getJiraSystemAdministrators() {
		if (user != null) {
			final List<User> systemAdmins = new ArrayList<>();
			systemAdmins.add(user);
			return systemAdmins;
		}
		return null;
	}

	@Override
	public long getNumberOfAssignedIssuesIgnoreSecurity(final User arg0, final User arg1) throws SearchException {

		return 0;
	}

	@Override
	public long getNumberOfAssignedIssuesIgnoreSecurity(final ApplicationUser arg0, final ApplicationUser arg1)
			throws SearchException {

		return 0;
	}

	@Override
	public long getNumberOfReportedIssuesIgnoreSecurity(final User arg0, final User arg1) throws SearchException {

		return 0;
	}

	@Override
	public long getNumberOfReportedIssuesIgnoreSecurity(final ApplicationUser arg0, final ApplicationUser arg1)
			throws SearchException {

		return 0;
	}

	@Override
	public Collection<Project> getProjectsLeadBy(final User arg0) {

		return null;
	}

	@Override
	public Collection<Project> getProjectsLeadBy(final ApplicationUser arg0) {

		return null;
	}

	@Override
	public Collection<User> getSystemAdministrators() {

		return null;
	}

	@Override
	public int getTotalUserCount() {

		return 0;
	}

	@Override
	public User getUser(final String arg0) {

		return null;
	}

	@Override
	public ApplicationUser getUserByKey(final String arg0) {

		return null;
	}

	@Override
	public ApplicationUser getUserByName(final String arg0) {

		return null;
	}

	@Override
	public User getUserObject(final String arg0) {

		return null;
	}

	@Override
	public Collection<User> getUsers() {

		return null;
	}

	@Override
	public SortedSet<User> getUsersInGroupNames(final Collection<String> arg0) {

		return null;
	}

	@Override
	public SortedSet<User> getUsersInGroups(final Collection<Group> arg0) {

		return null;
	}

	@Override
	public boolean hasExceededUserLimit() {

		return false;
	}

	@Override
	public boolean isNonSysAdminAttemptingToDeleteSysAdmin(final User arg0, final User arg1) {

		return false;
	}

	@Override
	public boolean isNonSysAdminAttemptingToDeleteSysAdmin(final ApplicationUser arg0, final ApplicationUser arg1) {

		return false;
	}

	@Override
	public void removeUser(final User arg0, final User arg1) {

	}

	@Override
	public void removeUser(final ApplicationUser arg0, final ApplicationUser arg1) {

	}

	@Override
	public void removeUserFromGroup(final Group arg0, final User arg1) throws PermissionException, RemoveException {

	}

	@Override
	public void removeUserFromGroups(final Collection<Group> arg0, final User arg1)
			throws PermissionException, RemoveException {

	}

	@Override
	public boolean userExists(final String arg0) {

		return false;
	}

	@Override
	public PasswordResetTokenValidation validatePasswordResetToken(final User arg0, final String arg1) {

		return null;
	}

}
