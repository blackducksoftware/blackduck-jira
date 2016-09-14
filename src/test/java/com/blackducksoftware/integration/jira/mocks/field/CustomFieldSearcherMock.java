package com.blackducksoftware.integration.jira.mocks.field;

import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;

public class CustomFieldSearcherMock implements CustomFieldSearcher {

	@Override
	public SearcherInformation<CustomField> getSearchInformation() {

		return null;
	}

	@Override
	public SearchInputTransformer getSearchInputTransformer() {

		return null;
	}

	@Override
	public SearchRenderer getSearchRenderer() {

		return null;
	}

	@Override
	public void init(final CustomField arg0) {


	}

	@Override
	public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler() {

		return null;
	}

	@Override
	public CustomFieldSearcherModuleDescriptor getDescriptor() {

		return null;
	}

	@Override
	public void init(final CustomFieldSearcherModuleDescriptor arg0) {


	}

}
