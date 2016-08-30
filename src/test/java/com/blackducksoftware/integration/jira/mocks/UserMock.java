package com.blackducksoftware.integration.jira.mocks;

import com.atlassian.crowd.embedded.api.User;

public class UserMock implements User {

	private String name;

	@Override
	public String getName() {

		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public int compareTo(final User arg0) {

		return 0;
	}

	@Override
	public long getDirectoryId() {

		return 0;
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
	public boolean isActive() {

		return false;
	}

}
