package com.blackducksoftware.integration.jira.mocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.impl.ImmutableGroup;
import com.atlassian.jira.bc.group.search.GroupPickerSearchService;

public class GroupPickerSearchServiceMock implements GroupPickerSearchService {
	private final HashMap<String, String> groupMap = new HashMap<>();
	final List<Group> groups = new ArrayList<>();

	public GroupPickerSearchServiceMock() {
	}

	public void addGroupByName(final String groupName) {
		groupMap.put(groupName, groupName);
		groups.add(new ImmutableGroup(groupName));
	}

	@Override
	public List<Group> findGroups(final String arg0) {
		return groups;
	}

	@Override
	public Group getGroupByName(final String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
