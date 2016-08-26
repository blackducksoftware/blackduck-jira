package com.blackducksoftware.integration.jira.mocks;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.atlassian.crowd.embedded.api.Directory;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.user.ApplicationUser;
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
	public boolean canUpdateGroupMembershipForUser(final User arg0) {

		return false;
	}

	@Override
	public boolean canUpdateUser(final User arg0) {

		return false;
	}

	@Override
	public boolean canUpdateUser(final ApplicationUser arg0) {

		return false;
	}

	@Override
	public boolean canUpdateUserPassword(final User arg0) {

		return false;
	}

	@Override
	public User findUserInDirectory(final String arg0, final Long arg1) {

		return null;
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
	public Set<User> getAllUsers() {

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
	public User getUser(final String arg0) {

		return null;
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
	public User getUserEvenWhenUnknown(final String arg0) {

		return null;
	}

	@Override
	public User getUserObject(final String arg0) {

		return null;
	}

	@Override
	public UserState getUserState(final User arg0) {

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
	public Collection<User> getUsers() {

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
	public void updateUser(final User arg0) {


	}

	@Override
	public void updateUser(final ApplicationUser arg0) {


	}

	@Override
	public boolean userCanUpdateOwnDetails(final ApplicationUser arg0) {

		return false;
	}

}
