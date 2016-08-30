package com.blackducksoftware.integration.jira.mocks;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.AssignableWorkflowScheme;
import com.atlassian.jira.workflow.AssignableWorkflowScheme.Builder;
import com.atlassian.jira.workflow.DraftWorkflowScheme;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowScheme;
import com.atlassian.jira.workflow.WorkflowSchemeManager;

public class WorkflowSchemeManagerMock implements WorkflowSchemeManager {

	private AssignableWorkflowScheme assignableWorkflowScheme;

	private boolean attemptedWorkflowUpdate;

	public boolean getAttemptedWorkflowUpdate() {
		return attemptedWorkflowUpdate;
	}

	public void setAssignableWorkflowScheme(final AssignableWorkflowScheme assignableWorkflowScheme) {
		this.assignableWorkflowScheme = assignableWorkflowScheme;
	}

	@Override
	public void addDefaultSchemeToProject(final GenericValue arg0) throws GenericEntityException {

	}

	@Override
	public void addDefaultSchemeToProject(final Project arg0) {

	}

	@Override
	public void addSchemeToProject(final GenericValue arg0, final GenericValue arg1) throws GenericEntityException {

	}

	@Override
	public void addSchemeToProject(final Project arg0, final Scheme arg1) {

	}

	@Override
	public GenericValue copyScheme(final GenericValue arg0) throws GenericEntityException {

		return null;
	}

	@Override
	public Scheme copyScheme(final Scheme arg0) {

		return null;
	}

	@Override
	public GenericValue createDefaultScheme() throws GenericEntityException {

		return null;
	}

	@Override
	public GenericValue createScheme(final String arg0, final String arg1) throws GenericEntityException {

		return null;
	}

	@Override
	public Scheme createSchemeAndEntities(final Scheme arg0) throws DataAccessException {

		return null;
	}

	@Override
	public GenericValue createSchemeEntity(final GenericValue arg0, final SchemeEntity arg1)
			throws GenericEntityException {

		return null;
	}

	@Override
	public Scheme createSchemeObject(final String arg0, final String arg1) {

		return null;
	}

	@Override
	public void deleteEntity(final Long arg0) throws GenericEntityException {

	}

	@Override
	public void deleteScheme(final Long arg0) throws GenericEntityException {

	}

	@Override
	public List<Scheme> getAssociatedSchemes(final boolean arg0) {

		return null;
	}

	@Override
	public GenericValue getDefaultScheme() throws GenericEntityException {

		return null;
	}

	@Override
	public Scheme getDefaultSchemeObject() {

		return null;
	}

	@Override
	public List<GenericValue> getEntities(final String arg0, final String arg1) throws GenericEntityException {

		return null;
	}

	@Override
	public GenericValue getEntity(final Long arg0) throws GenericEntityException {

		return null;
	}

	@Override
	public Collection<Group> getGroups(final Long arg0, final GenericValue arg1) {

		return null;
	}

	@Override
	public Collection<Group> getGroups(final Long arg0, final Project arg1) {

		return null;
	}

	@Override
	public List<GenericValue> getProjects(final GenericValue arg0) throws GenericEntityException {

		return null;
	}

	@Override
	public List<Project> getProjects(final Scheme arg0) {

		return null;
	}

	@Override
	public GenericValue getScheme(final Long arg0) throws GenericEntityException {

		return null;
	}

	@Override
	public GenericValue getScheme(final String arg0) throws GenericEntityException {

		return null;
	}

	@Override
	public Scheme getSchemeFor(final Project arg0) {

		return null;
	}

	@Override
	public Long getSchemeIdFor(final Project arg0) {

		return null;
	}

	@Override
	public Scheme getSchemeObject(final Long arg0) throws DataAccessException {

		return null;
	}

	@Override
	public Scheme getSchemeObject(final String arg0) throws DataAccessException {

		return null;
	}

	@Override
	public List<Scheme> getSchemeObjects() throws DataAccessException {

		return null;
	}

	@Override
	public List<GenericValue> getSchemes() throws GenericEntityException {

		return null;
	}

	@Override
	public List<GenericValue> getSchemes(final GenericValue arg0) throws GenericEntityException {

		return null;
	}

	@Override
	public List<Scheme> getUnassociatedSchemes() throws DataAccessException {

		return null;
	}

	@Override
	public Collection<User> getUsers(final Long arg0, final Project arg1) {

		return null;
	}

	@Override
	public Collection<User> getUsers(final Long arg0, final Issue arg1) {

		return null;
	}

	@Override
	public Collection<User> getUsers(final Long arg0, final GenericValue arg1) {

		return null;
	}

	@Override
	public Collection<User> getUsers(final Long arg0, final PermissionContext arg1) {

		return null;
	}

	@Override
	public boolean hasSchemeAuthority(final Long arg0, final GenericValue arg1) {

		return false;
	}

	@Override
	public boolean hasSchemeAuthority(final Long arg0, final GenericValue arg1, final User arg2, final boolean arg3) {

		return false;
	}

	@Override
	public boolean removeEntities(final GenericValue arg0, final Long arg1) throws RemoveException {

		return false;
	}

	@Override
	public boolean removeEntities(final String arg0, final String arg1) throws RemoveException {

		return false;
	}

	@Override
	public void removeSchemesFromProject(final GenericValue arg0) throws GenericEntityException {

	}

	@Override
	public void removeSchemesFromProject(final Project arg0) {

	}

	@Override
	public boolean schemeExists(final String arg0) throws GenericEntityException {

		return false;
	}

	@Override
	public void updateScheme(final GenericValue arg0) throws GenericEntityException {

	}

	@Override
	public void updateScheme(final Scheme arg0) {

	}

	@Override
	public void addWorkflowToScheme(final GenericValue arg0, final String arg1, final String arg2)
			throws GenericEntityException {

	}

	@Override
	public Builder assignableBuilder() {

		return null;
	}

	@Override
	public AssignableWorkflowScheme cleanUpSchemeDraft(final Project arg0, final User arg1) {

		return null;
	}

	@Override
	public void clearWorkflowCache() {

	}

	@Override
	public AssignableWorkflowScheme copyDraft(final DraftWorkflowScheme arg0, final User arg1, final String arg2) {

		return null;
	}

	@Override
	public DraftWorkflowScheme createDraft(final ApplicationUser arg0, final DraftWorkflowScheme arg1) {

		return null;
	}

	@Override
	public DraftWorkflowScheme createDraftOf(final ApplicationUser arg0, final AssignableWorkflowScheme arg1) {

		return null;
	}

	@Override
	public AssignableWorkflowScheme createScheme(final AssignableWorkflowScheme arg0) {

		return null;
	}

	@Override
	public boolean deleteWorkflowScheme(final WorkflowScheme arg0) {

		return false;
	}

	@Override
	public com.atlassian.jira.workflow.DraftWorkflowScheme.Builder draftBuilder(final AssignableWorkflowScheme arg0) {

		return null;
	}

	@Override
	public Collection<String> getActiveWorkflowNames() throws GenericEntityException, WorkflowException {

		return null;
	}

	@Override
	public Iterable<AssignableWorkflowScheme> getAssignableSchemes() {

		return null;
	}

	@Override
	public String getAssociationType() {

		return null;
	}

	@Override
	public GenericValue getDefaultEntity(final GenericValue arg0) throws GenericEntityException {

		return null;
	}

	@Override
	public AssignableWorkflowScheme getDefaultWorkflowScheme() {

		return null;
	}

	@Override
	public DraftWorkflowScheme getDraft(final long arg0) {

		return null;
	}

	@Override
	public DraftWorkflowScheme getDraftForParent(final AssignableWorkflowScheme arg0) {

		return null;
	}

	@Override
	public List<GenericValue> getEntities(final GenericValue arg0) throws GenericEntityException {

		return null;
	}

	@Override
	public List<GenericValue> getEntities(final GenericValue arg0, final Long arg1) throws GenericEntityException {

		return null;
	}

	@Override
	public List<GenericValue> getEntities(final GenericValue arg0, final String arg1) throws GenericEntityException {

		return null;
	}

	@Override
	public List<GenericValue> getEntities(final GenericValue arg0, final Long arg1, final String arg2)
			throws GenericEntityException {

		return null;
	}

	@Override
	public List<GenericValue> getEntities(final GenericValue arg0, final String arg1, final Long arg2)
			throws GenericEntityException {

		return null;
	}

	@Override
	public String getEntityName() {

		return null;
	}

	@Override
	public List<GenericValue> getNonDefaultEntities(final GenericValue arg0) throws GenericEntityException {

		return null;
	}

	@Override
	public AssignableWorkflowScheme getParentForDraft(final long arg0) {

		return null;
	}

	@Override
	public List<Project> getProjectsUsing(final AssignableWorkflowScheme arg0) {

		return null;
	}

	@Override
	public String getSchemeDesc() {

		return null;
	}

	@Override
	public String getSchemeEntityName() {

		return null;
	}

	@Override
	public Collection<GenericValue> getSchemesForWorkflow(final JiraWorkflow arg0) {

		return null;
	}

	@Override
	public Iterable<WorkflowScheme> getSchemesForWorkflowIncludingDrafts(final JiraWorkflow arg0) {

		return null;
	}

	@Override
	public Map<String, String> getWorkflowMap(final Project arg0) {

		return null;
	}

	@Override
	public String getWorkflowName(final Project arg0, final String arg1) {

		return null;
	}

	@Override
	public String getWorkflowName(final GenericValue arg0, final String arg1) {

		return null;
	}

	@Override
	public GenericValue getWorkflowScheme(final GenericValue arg0) throws GenericEntityException {

		return null;
	}

	@Override
	public AssignableWorkflowScheme getWorkflowSchemeObj(final long arg0) {

		return null;
	}

	@Override
	public AssignableWorkflowScheme getWorkflowSchemeObj(final String arg0) {

		return null;
	}

	@Override
	public AssignableWorkflowScheme getWorkflowSchemeObj(final Project arg0) {

		return assignableWorkflowScheme;
	}

	@Override
	public boolean hasDraft(final AssignableWorkflowScheme arg0) {

		return false;
	}

	@Override
	public boolean isActive(final WorkflowScheme arg0) {

		return false;
	}

	@Override
	public boolean isUsingDefaultScheme(final Project arg0) {

		return false;
	}

	@Override
	public void replaceSchemeWithDraft(final DraftWorkflowScheme arg0) {

	}

	@Override
	public DraftWorkflowScheme updateDraftWorkflowScheme(final ApplicationUser arg0, final DraftWorkflowScheme arg1) {

		return null;
	}

	@Override
	public void updateSchemesForRenamedWorkflow(final String arg0, final String arg1) {

	}

	@Override
	public AssignableWorkflowScheme updateWorkflowScheme(final AssignableWorkflowScheme workflow) {
		attemptedWorkflowUpdate = true;
		return null;
	}

	@Override
	public <T> T waitForUpdatesToFinishAndExecute(final AssignableWorkflowScheme arg0, final Callable<T> arg1)
			throws Exception {

		return null;
	}

}