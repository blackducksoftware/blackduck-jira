/**
 * Hub JIRA Plugin
 *
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
 */
package com.blackducksoftware.integration.jira.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.atlassian.application.api.ApplicationKey;
import com.atlassian.crowd.embedded.api.Group;
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
import com.atlassian.jira.user.UserDetails;
import com.atlassian.jira.user.util.UserUtil;

public class UserUtilMock implements UserUtil {

    private ApplicationUser user;

    public void setUser(final ApplicationUser user) {
        this.user = user;
    }

    @Override
    public boolean canActivateNumberOfUsers(final int arg0) {

        return false;
    }

    @Override
    public void clearActiveUserCount() {

    }

    @Override
    public int getActiveUserCount() {

        return 0;
    }

    @Override
    public Collection<ApplicationUser> getAllApplicationUsers() {

        return null;
    }

    @Override
    public Collection<ProjectComponent> getComponentsUserLeads(final ApplicationUser arg0) {

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
    public Collection<ApplicationUser> getJiraSystemAdministrators() {
        if (user != null) {
            final List<ApplicationUser> systemAdmins = new ArrayList<>();
            systemAdmins.add(user);
            return systemAdmins;
        }
        return null;
    }

    @Override
    public long getNumberOfAssignedIssuesIgnoreSecurity(final ApplicationUser arg0, final ApplicationUser arg1)
            throws SearchException {

        return 0;
    }

    @Override
    public long getNumberOfReportedIssuesIgnoreSecurity(final ApplicationUser arg0, final ApplicationUser arg1)
            throws SearchException {

        return 0;
    }

    @Override
    public Collection<Project> getProjectsLeadBy(final ApplicationUser arg0) {

        return null;
    }

    @Override
    public int getTotalUserCount() {

        return 0;
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
    public boolean isNonSysAdminAttemptingToDeleteSysAdmin(final ApplicationUser arg0, final ApplicationUser arg1) {

        return false;
    }

    @Override
    public void removeUser(final ApplicationUser arg0, final ApplicationUser arg1) {

    }

    @Override
    public boolean userExists(final String arg0) {

        return false;
    }

    @Override
    public void addToJiraUsePermission(final ApplicationUser arg0) throws PermissionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void addUserToGroup(final Group arg0, final ApplicationUser arg1) throws PermissionException, AddException {
        // TODO Auto-generated method stub

    }

    @Override
    public void addUserToGroups(final Collection<Group> arg0, final ApplicationUser arg1) throws PermissionException,
            AddException {
        // TODO Auto-generated method stub

    }

    @Override
    public void changePassword(final ApplicationUser arg0, final String arg1) throws UserNotFoundException,
            InvalidCredentialException, OperationNotPermittedException, PermissionException {
        // TODO Auto-generated method stub

    }

    @Override
    public ApplicationUser createUser(final UserDetails arg0, final boolean arg1, final int arg2,
            final Set<ApplicationKey> arg3) throws PermissionException, CreateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationUser createUserNoNotification(final String arg0, final String arg1, final String arg2,
            final String arg3) throws PermissionException, CreateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationUser createUserNoNotification(final String arg0, final String arg1, final String arg2,
            final String arg3, final Long arg4) throws PermissionException, CreateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationUser createUserWithNotification(final String arg0, final String arg1, final String arg2,
            final String arg3, final int arg4) throws PermissionException, CreateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationUser createUserWithNotification(final String arg0, final String arg1, final String arg2,
            final String arg3, final Long arg4, final int arg5) throws PermissionException, CreateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PasswordResetToken generatePasswordResetToken(final ApplicationUser arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationUser getUser(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationUser getUserObject(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeUserFromGroup(final Group arg0, final ApplicationUser arg1) throws PermissionException,
            RemoveException {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeUserFromGroups(final Collection<Group> arg0, final ApplicationUser arg1)
            throws PermissionException, RemoveException {
        // TODO Auto-generated method stub

    }

    @Override
    public PasswordResetTokenValidation validatePasswordResetToken(final ApplicationUser arg0, final String arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ApplicationUser> getAdministrators() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedSet<ApplicationUser> getAllUsersInGroupNames(final Collection<String> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<ApplicationUser> getAllUsersInGroupNamesUnsorted(final Collection<String> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedSet<ApplicationUser> getAllUsersInGroups(final Collection<Group> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ApplicationUser> getJiraAdministrators() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ApplicationUser> getSystemAdministrators() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ApplicationUser> getUsers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedSet<ApplicationUser> getUsersInGroupNames(final Collection<String> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SortedSet<ApplicationUser> getUsersInGroups(final Collection<Group> arg0) {
        // TODO Auto-generated method stub
        return null;
    }

}
