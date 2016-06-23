package com.blackducksoftware.integration.jira.mocks;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;

public class ApplicationUserMock implements ApplicationUser {

	private String name;

	@Override
	public long getDirectoryId() {
		return 0;
	}

	@Override
	public User getDirectoryUser() {

		return null;
	}

	@Override
	public String getDisplayName() {

		return null;
	}

	@Override
	public String getEmailAddress() {

		return null;
	}

	@Override
	public String getKey() {

		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String getUsername() {

		return null;
	}

	@Override
	public boolean isActive() {

		return false;
	}

}
