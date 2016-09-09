package com.blackducksoftware.integration.jira.mocks.field;

import java.util.Collection;
import java.util.Set;

import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;

public class FieldConfigurationSchemeMock implements FieldConfigurationScheme {

	private String name;

	private Long id;

	@Override
	public Set<Long> getAllFieldLayoutIds(final Collection<String> arg0) {

		return null;
	}

	@Override
	public String getDescription() {

		return null;
	}

	@Override
	public Long getFieldLayoutId(final String arg0) {

		return null;
	}

	@Override
	public Long getId() {

		return id;
	}

	@Override
	public String getName() {

		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setId(final Long id) {
		this.id = id;
	}

}
