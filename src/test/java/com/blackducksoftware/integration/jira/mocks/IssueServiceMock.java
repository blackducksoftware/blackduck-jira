package com.blackducksoftware.integration.jira.mocks;

import java.util.Map;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.TransitionOptions;

public class IssueServiceMock implements IssueService {

	@Override
	public IssueResult assign(final ApplicationUser arg0, final AssignValidationResult arg1) {

		return null;
	}

	@Override
	public IssueResult assign(final User arg0, final AssignValidationResult arg1) {

		return null;
	}

	@Override
	public IssueResult create(final ApplicationUser arg0, final CreateValidationResult arg1) {

		return null;
	}

	@Override
	public IssueResult create(final User arg0, final CreateValidationResult arg1) {

		return null;
	}

	@Override
	public IssueResult create(final ApplicationUser arg0, final CreateValidationResult arg1, final String arg2) {

		return null;
	}

	@Override
	public IssueResult create(final User arg0, final CreateValidationResult arg1, final String arg2) {

		return null;
	}

	@Override
	public ErrorCollection delete(final ApplicationUser arg0, final DeleteValidationResult arg1) {

		return null;
	}

	@Override
	public ErrorCollection delete(final User arg0, final DeleteValidationResult arg1) {

		return null;
	}

	@Override
	public ErrorCollection delete(final ApplicationUser arg0, final DeleteValidationResult arg1,
			final EventDispatchOption arg2, final boolean arg3) {

		return null;
	}

	@Override
	public ErrorCollection delete(final User arg0, final DeleteValidationResult arg1, final EventDispatchOption arg2,
			final boolean arg3) {

		return null;
	}

	@Override
	public IssueResult getIssue(final ApplicationUser arg0, final Long arg1) {

		return null;
	}

	@Override
	public IssueResult getIssue(final User arg0, final Long arg1) {

		return null;
	}

	@Override
	public IssueResult getIssue(final ApplicationUser arg0, final String arg1) {

		return null;
	}

	@Override
	public IssueResult getIssue(final User arg0, final String arg1) {

		return null;
	}

	@Override
	public boolean isEditable(final Issue arg0, final ApplicationUser arg1) {

		return false;
	}

	@Override
	public boolean isEditable(final Issue arg0, final User arg1) {

		return false;
	}

	@Override
	public IssueInputParameters newIssueInputParameters() {

		return new IssueInputParametersMock();
	}

	@Override
	public IssueInputParameters newIssueInputParameters(final Map<String, String[]> arg0) {

		return null;
	}

	@Override
	public IssueResult transition(final ApplicationUser arg0, final TransitionValidationResult arg1) {

		return null;
	}

	@Override
	public IssueResult transition(final User arg0, final TransitionValidationResult arg1) {

		return null;
	}

	@Override
	public IssueResult update(final ApplicationUser arg0, final UpdateValidationResult arg1) {

		return null;
	}

	@Override
	public IssueResult update(final User arg0, final UpdateValidationResult arg1) {

		return null;
	}

	@Override
	public IssueResult update(final ApplicationUser arg0, final UpdateValidationResult arg1,
			final EventDispatchOption arg2, final boolean arg3) {

		return null;
	}

	@Override
	public IssueResult update(final User arg0, final UpdateValidationResult arg1, final EventDispatchOption arg2,
			final boolean arg3) {

		return null;
	}

	@Override
	public AssignValidationResult validateAssign(final ApplicationUser arg0, final Long arg1, final String arg2) {

		return null;
	}

	@Override
	public AssignValidationResult validateAssign(final User arg0, final Long arg1, final String arg2) {

		return null;
	}

	@Override
	public CreateValidationResult validateCreate(final ApplicationUser arg0, final IssueInputParameters arg1) {

		return null;
	}

	@Override
	public CreateValidationResult validateCreate(final User arg0, final IssueInputParameters arg1) {

		return null;
	}

	@Override
	public DeleteValidationResult validateDelete(final ApplicationUser arg0, final Long arg1) {

		return null;
	}

	@Override
	public DeleteValidationResult validateDelete(final User arg0, final Long arg1) {

		return null;
	}

	@Override
	public CreateValidationResult validateSubTaskCreate(final ApplicationUser arg0, final Long arg1,
			final IssueInputParameters arg2) {

		return null;
	}

	@Override
	public CreateValidationResult validateSubTaskCreate(final User arg0, final Long arg1,
			final IssueInputParameters arg2) {

		return null;
	}

	@Override
	public TransitionValidationResult validateTransition(final ApplicationUser arg0, final Long arg1, final int arg2,
			final IssueInputParameters arg3) {

		return null;
	}

	@Override
	public TransitionValidationResult validateTransition(final User arg0, final Long arg1, final int arg2,
			final IssueInputParameters arg3) {

		return null;
	}

	@Override
	public TransitionValidationResult validateTransition(final ApplicationUser arg0, final Long arg1, final int arg2,
			final IssueInputParameters arg3, final TransitionOptions arg4) {

		return null;
	}

	@Override
	public TransitionValidationResult validateTransition(final User arg0, final Long arg1, final int arg2,
			final IssueInputParameters arg3, final TransitionOptions arg4) {

		return null;
	}

	@Override
	public UpdateValidationResult validateUpdate(final ApplicationUser arg0, final Long arg1,
			final IssueInputParameters arg2) {

		return null;
	}

	@Override
	public UpdateValidationResult validateUpdate(final User arg0, final Long arg1, final IssueInputParameters arg2) {

		return null;
	}

}
