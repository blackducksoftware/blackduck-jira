/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
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
