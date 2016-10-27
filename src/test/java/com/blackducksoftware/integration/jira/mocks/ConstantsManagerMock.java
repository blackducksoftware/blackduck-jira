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

import org.mockito.Mockito;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.fugue.Option;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.lang.Pair;
import com.blackducksoftware.integration.jira.mocks.issue.IssueTypeMock;

public class ConstantsManagerMock implements ConstantsManager {
    private int issueTypeIndex = 0;

    private final List<IssueType> issueTypes = new ArrayList<>();

    public ConstantsManagerMock() {
    }

    @Override
    public boolean constantExists(final String arg0, final String arg1) {
        return false;
    }

    @Override
    public List<IssueConstant> convertToConstantObjects(final String arg0, final Collection arg1) {
        return null;
    }

    @Override
    public List<String> expandIssueTypeIds(final Collection<String> arg0) {
        return null;
    }

    @Override
    public List<String> getAllIssueTypeIds() {
        return null;
    }

    @Override
    public Collection<IssueType> getAllIssueTypeObjects() {
        return issueTypes;
    }

    @Override
    public IssueConstant getConstantByNameIgnoreCase(final String arg0, final String arg1) {
        return null;
    }

    @Override
    public IssueConstant getConstantObject(final String arg0, final String arg1) {
        return null;
    }

    @Override
    public Collection getConstantObjects(final String arg0) {
        return null;
    }

    @Override
    public Priority getDefaultPriorityObject() {
        return null;
    }

    @Override
    public IssueConstant getIssueConstant(final GenericValue arg0) {
        return null;
    }

    @Override
    public IssueConstant getIssueConstantByName(final String arg0, final String arg1) {
        return null;
    }

    @Override
    public IssueType getIssueTypeObject(final String arg0) {
        return null;
    }

    @Override
    public String getPriorityName(final String arg0) {
        return null;
    }

    @Override
    public Priority getPriorityObject(final String arg0) {
        return null;
    }

    @Override
    public Collection<Priority> getPriorityObjects() {
        return null;
    }

    @Override
    public Collection<IssueType> getRegularIssueTypeObjects() {
        return null;
    }

    @Override
    public Resolution getResolutionObject(final String arg0) {
        return null;
    }

    @Override
    public Collection<Resolution> getResolutionObjects() {
        return null;
    }

    @Override
    public Status getStatusByName(final String arg0) {
        return null;
    }

    @Override
    public Status getStatusByNameIgnoreCase(final String arg0) {
        return null;
    }

    @Override
    public Status getStatusByTranslatedName(final String arg0) {
        return null;
    }

    @Override
    public Status getStatusObject(final String arg0) {
        return null;
    }

    @Override
    public Collection<Status> getStatusObjects() {
        return null;
    }

    @Override
    public Collection<IssueType> getSubTaskIssueTypeObjects() {
        return null;
    }

    @Override
    public IssueType insertIssueType(final String name, final Long arg1, final String arg2, final String description,
            final String arg4) throws CreateException {
        final IssueType issueType = generateMockIssueType(name, description);
        issueTypes.add(issueType);

        return issueType;
    }

    private IssueType generateMockIssueType(final String name, final String description) {
        final IssueTypeMock newIssueType = new IssueTypeMock();
        newIssueType.setName(name);
        newIssueType.setDescription(description);
        newIssueType.setId("mockIssueTypeId" + issueTypeIndex);
        newIssueType.setValue(Mockito.mock(GenericValue.class));
        issueTypeIndex++;
        return newIssueType;
    }

    @Override
    public IssueType insertIssueType(final String name, final Long arg1, final String arg2, final String description,
            final Long arg4) throws CreateException {
        final IssueType issueType = generateMockIssueType(name, description);
        issueTypes.add(issueType);
        return issueType;
    }

    @Override
    public void invalidate(final IssueConstant arg0) {
    }

    @Override
    public void invalidateAll() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public void refreshIssueTypes() {

    }

    @Override
    public void refreshPriorities() {

    }

    @Override
    public void refreshResolutions() {

    }

    @Override
    public void refreshStatuses() {

    }

    @Override
    public void removeIssueType(final String arg0) throws RemoveException {

    }

    @Override
    public void storeIssueTypes(final List<GenericValue> arg0) throws DataAccessException {

    }

    @Override
    public void updateIssueType(final String arg0, final String arg1, final Long arg2, final String arg3,
            final String arg4, final String arg5) throws DataAccessException {

    }

    @Override
    public void updateIssueType(final String arg0, final String arg1, final Long arg2, final String arg3,
            final String arg4, final Long arg5) {

    }

    @Override
    public void validateCreateIssueType(final String arg0, final String arg1, final String arg2, final String arg3,
            final ErrorCollection arg4, final String arg5) {

    }

    @Override
    public void validateCreateIssueTypeWithAvatar(final String arg0, final String arg1, final String arg2,
            final String arg3, final ErrorCollection arg4, final String arg5) {
    }

    @Override
    public Option<Pair<String, Reason>> validateName(final String arg0, final Option<IssueType> arg1) {
        return null;
    }

    public int getIssueTypesCreatedCount() {
        return issueTypeIndex;
    }

    @Override
    public List<IssueConstant> getConstantsByIds(final CONSTANT_TYPE arg0, final Collection<String> arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Priority getDefaultPriority() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IssueType getIssueType(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Resolution getResolution(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Status getStatus(final String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void recalculateIssueTypeSequencesAndStore(final List<IssueType> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void recalculatePrioritySequencesAndStore(final List<Priority> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void recalculateResolutionSequencesAndStore(final List<Resolution> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void recalculateStatusSequencesAndStore(final List<Status> arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<IssueType> getEditableSubTaskIssueTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Priority> getPriorities() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Resolution> getResolutions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Status> getStatuses() {
        // TODO Auto-generated method stub
        return null;
    }

}
