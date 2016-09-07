package com.blackducksoftware.integration.jira.mocks;

import java.util.Collection;
import java.util.List;

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

public class ConstantsManagerMock implements ConstantsManager {
	private int issueTypeIndex = 0;

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
	public GenericValue createIssueType(final String arg0, final Long arg1, final String arg2, final String arg3, final String arg4)
			throws CreateException {
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
		return null;
	}

	@Override
	public List<GenericValue> getAllIssueTypes() {
		return null;
	}

	@Override
	public GenericValue getConstantByName(final String arg0, final String arg1) {
		return null;
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
	public GenericValue getDefaultPriority() {
		return null;
	}

	@Override
	public Priority getDefaultPriorityObject() {
		return null;
	}

	@Override
	public List<GenericValue> getEditableSubTaskIssueTypes() {
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
	public GenericValue getIssueType(final String arg0) {
		return null;
	}

	@Override
	public IssueType getIssueTypeObject(final String arg0) {
		return null;
	}

	@Override
	public Collection<GenericValue> getIssueTypes() {
		return null;
	}

	@Override
	public Collection<GenericValue> getPriorities() {
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
	public GenericValue getResolution(final String arg0) {
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
	public Collection<GenericValue> getResolutions() {
		return null;
	}

	@Override
	public GenericValue getStatus(final String arg0) {
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
	public Collection<GenericValue> getStatuses() {
		return null;
	}

	@Override
	public Collection<IssueType> getSubTaskIssueTypeObjects() {
		return null;
	}

	@Override
	public Collection<GenericValue> getSubTaskIssueTypes() {
		return null;
	}

	@Override
	public IssueType insertIssueType(final String arg0, final Long arg1, final String arg2, final String arg3, final String arg4)
			throws CreateException {
		return generateMockIssueType();
	}

	private IssueType generateMockIssueType() {
		final IssueTypeMock newIssueType = new IssueTypeMock();
		newIssueType.setName("Mock Issue Type" + issueTypeIndex);
		newIssueType.setDescription("Mock Issue Type" + issueTypeIndex);
		newIssueType.setId("mockIssueType" + issueTypeIndex);
		issueTypeIndex++;
		return newIssueType;
	}

	@Override
	public IssueType insertIssueType(final String arg0, final Long arg1, final String arg2, final String arg3, final Long arg4)
			throws CreateException {
		return generateMockIssueType();
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
	public void updateIssueType(final String arg0, final String arg1, final Long arg2, final String arg3, final String arg4, final String arg5)
			throws DataAccessException {

	}

	@Override
	public void updateIssueType(final String arg0, final String arg1, final Long arg2, final String arg3, final String arg4, final Long arg5) {

	}

	@Override
	public void validateCreateIssueType(final String arg0, final String arg1, final String arg2, final String arg3, final ErrorCollection arg4,
			final String arg5) {

	}

	@Override
	public void validateCreateIssueTypeWithAvatar(final String arg0, final String arg1, final String arg2, final String arg3,
			final ErrorCollection arg4, final String arg5) {
	}

	@Override
	public Option<Pair<String, Reason>> validateName(final String arg0, final Option<IssueType> arg1) {
		return null;
	}

	public int getIssueTypesCreatedCount() {
		return issueTypeIndex;
	}

}
