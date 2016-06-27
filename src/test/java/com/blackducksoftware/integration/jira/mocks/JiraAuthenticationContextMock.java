package com.blackducksoftware.integration.jira.mocks;

import java.util.Locale;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.OutlookDate;

public class JiraAuthenticationContextMock implements JiraAuthenticationContext {

	@Override
	public void clearLoggedInUser() {

	}

	@Override
	public I18nHelper getI18nBean() {

		return null;
	}

	@Override
	public I18nHelper getI18nHelper() {

		return null;
	}

	@Override
	public Locale getLocale() {

		return null;
	}

	@Override
	public User getLoggedInUser() {

		return null;
	}

	@Override
	public OutlookDate getOutlookDate() {

		return null;
	}

	@Override
	public String getText(final String arg0) {

		return null;
	}

	@Override
	public ApplicationUser getUser() {

		return null;
	}

	@Override
	public boolean isLoggedInUser() {

		return false;
	}

	@Override
	public void setLoggedInUser(final User arg0) {

	}

	@Override
	public void setLoggedInUser(final ApplicationUser arg0) {

	}

}
