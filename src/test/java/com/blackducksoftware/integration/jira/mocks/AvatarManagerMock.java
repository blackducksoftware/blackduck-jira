package com.blackducksoftware.integration.jira.mocks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.Avatar.Type;
import com.atlassian.jira.avatar.AvatarImageDataProvider;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.avatar.Selection;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Consumer;

public class AvatarManagerMock implements AvatarManager {
	private final List<Avatar> avatarTemplatesUsedToCreateAvatars = new ArrayList<>();

	public AvatarManagerMock() {
	}

	public List<Avatar> getAvatarTemplatesUsedToCreateAvatars() {
		return avatarTemplatesUsedToCreateAvatars;
	}

	@Override
	public Avatar create(final Avatar arg0) throws DataAccessException {
		return null;
	}

	@Override
	public Avatar create(final Avatar avatarTemplate, final InputStream arg1, final Selection arg2)
			throws DataAccessException, IOException {
		return avatarTemplate;
	}

	@Override
	public Avatar create(final Type arg0, final String arg1, final AvatarImageDataProvider arg2) throws IOException {
		return null;
	}

	@Override
	public Avatar create(final String arg0, final String arg1, final Project arg2, final InputStream arg3, final Selection arg4)
			throws DataAccessException, IOException {
		return null;
	}

	@Override
	public Avatar create(final String arg0, final String arg1, final ApplicationUser arg2, final InputStream arg3, final Selection arg4)
			throws DataAccessException, IOException {
		return null;
	}

	@Override
	public boolean delete(final Long arg0) throws DataAccessException {
		return false;
	}

	@Override
	public boolean delete(final Long arg0, final boolean arg1) {
		return false;
	}

	@Override
	public List<Avatar> getAllSystemAvatars(final Type arg0) throws DataAccessException {
		return null;
	}

	@Override
	public Long getAnonymousAvatarId() {
		return Long.valueOf(1L);
	}

	@Override
	public File getAvatarBaseDirectory() {
		return null;
	}

	@Override
	public Avatar getById(final Long arg0) throws DataAccessException {
		return null;
	}

	@Override
	public Avatar getByIdTagged(final Long arg0) throws DataAccessException {
		return null;
	}

	@Override
	public List<Avatar> getCustomAvatarsForOwner(final Type arg0, final String arg1) throws DataAccessException {
		return null;
	}

	@Override
	public Long getDefaultAvatarId(final Type arg0) {
		return null;
	}

	@Override
	public boolean hasPermissionToEdit(final ApplicationUser arg0, final ApplicationUser arg1) {
		return false;
	}

	@Override
	public boolean hasPermissionToEdit(final ApplicationUser arg0, final Project arg1) {
		return false;
	}

	@Override
	public boolean hasPermissionToEdit(final User arg0, final Type arg1, final String arg2) {
		return false;
	}

	@Override
	public boolean hasPermissionToView(final ApplicationUser arg0, final ApplicationUser arg1) {
		return false;
	}

	@Override
	public boolean hasPermissionToView(final ApplicationUser arg0, final Project arg1) {
		return false;
	}

	@Override
	public boolean hasPermissionToView(final User arg0, final Type arg1, final String arg2) {
		return false;
	}

	@Override
	public boolean isAvatarOwner(final Avatar arg0, final String arg1) {
		return false;
	}

	@Override
	public void readAvatarData(final Avatar arg0, final ImageSize arg1, final Consumer<InputStream> arg2) throws IOException {
	}

	@Override
	public void update(final Avatar arg0) throws DataAccessException {
	}

}
