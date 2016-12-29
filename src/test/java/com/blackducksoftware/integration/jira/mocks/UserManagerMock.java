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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserDetails;
import com.atlassian.jira.user.util.UserIdentity;
import com.atlassian.jira.user.util.UserManager;

public class UserManagerMock implements UserManager {

    private ApplicationUser user;

    public void setMockApplicationUser(final ApplicationUser user) {
        this.user = user;
    }

    @Override
    public boolean canDirectoryUpdateUserPassword(final Directory arg0) {

        return false;
    }

    @Override
    public boolean canRenameUser(final ApplicationUser arg0) {

        return false;
    }

    @Override
    public boolean canUpdateUser(final ApplicationUser arg0) {

        return false;
    }

    @Override
    public String generateRandomPassword() {

        return null;
    }

    @Override
    public Collection<ApplicationUser> getAllApplicationUsers() {

        return null;
    }

    @Override
    public Set<Group> getAllGroups() {

        return null;
    }

    @Override
    public Directory getDirectory(final Long arg0) {

        return null;
    }

    @Override
    public Group getGroup(final String arg0) {

        return null;
    }

    @Override
    public Group getGroupObject(final String arg0) {

        return null;
    }

    @Override
    public Collection<Group> getGroups() {

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
    public ApplicationUser getUserByKeyEvenWhenUnknown(final String arg0) {

        return null;
    }

    @Override
    public ApplicationUser getUserByName(final String name) {
        if (name != null && user != null && name.equals(user.getName())) {
            return user;
        }
        return null;
    }

    @Override
    public ApplicationUser getUserByNameEvenWhenUnknown(final String arg0) {

        return null;
    }

    @Override
    public UserState getUserState(final ApplicationUser arg0) {

        return null;
    }

    @Override
    public UserState getUserState(final String arg0, final long arg1) {

        return null;
    }

    @Override
    public List<Directory> getWritableDirectories() {

        return null;
    }

    @Override
    public boolean hasGroupWritableDirectory() {

        return false;
    }

    @Override
    public boolean hasPasswordWritableDirectory() {

        return false;
    }

    @Override
    public boolean hasWritableDirectory() {

        return false;
    }

    @Override
    public boolean isUserExisting(final ApplicationUser arg0) {

        return false;
    }

    @Override
    public void updateUser(final ApplicationUser arg0) {

    }

    @Override
    public boolean userCanUpdateOwnDetails(final ApplicationUser arg0) {

        return false;
    }

    @Override
    public boolean canUpdateGroupMembershipForUser(final ApplicationUser arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean canUpdateUserPassword(final ApplicationUser arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ApplicationUser createUser(final UserDetails arg0) throws CreateException, PermissionException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationUser findUserInDirectory(final String arg0, final Long arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationUser getUser(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationUser getUserEvenWhenUnknown(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ApplicationUser getUserObject(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<ApplicationUser> getAllUsers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<Directory> getDefaultCreateDirectory() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<ApplicationUser> getUserById(final Long arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<UserIdentity> getUserIdentityById(final Long arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<UserIdentity> getUserIdentityByKey(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<UserIdentity> getUserIdentityByUsername(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<ApplicationUser> getUsers() {
        // TODO Auto-generated method stub
        return null;
    }

}
