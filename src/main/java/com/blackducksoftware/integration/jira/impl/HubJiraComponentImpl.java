package com.blackducksoftware.integration.jira.impl;

import com.atlassian.sal.api.ApplicationProperties;
import com.blackducksoftware.integration.jira.api.HubJiraComponent;

public class HubJiraComponentImpl implements HubJiraComponent
{
	private final ApplicationProperties applicationProperties;

	public HubJiraComponentImpl(final ApplicationProperties applicationProperties)
	{
		this.applicationProperties = applicationProperties;
	}

	@Override
	public String getName()
	{
		if(null != applicationProperties)
		{
			return "hubJira:" + applicationProperties.getDisplayName();
		}

		return "hubJira";
	}
}