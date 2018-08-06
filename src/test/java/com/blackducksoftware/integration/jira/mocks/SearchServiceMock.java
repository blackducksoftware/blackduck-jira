/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.jira.mocks;

import java.util.Collections;
import java.util.List;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;

public class SearchServiceMock implements SearchService {
    private List<Issue> issuesList = Collections.emptyList();

    public void setIssueResults(final List<Issue> issuesResults) {
        if (issuesResults != null) {
            this.issuesList = issuesResults;
        }
    }

    @Override
    public boolean doesQueryFitFilterForm(final ApplicationUser arg0, final Query arg1) {
        // Auto-generated method stub
        return false;
    }

    @Override
    public String getGeneratedJqlString(final Query arg0) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String getIssueSearchPath(final ApplicationUser arg0, final IssueSearchParameters arg1) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String getJqlString(final Query arg0) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public QueryContext getQueryContext(final ApplicationUser arg0, final Query arg1) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public String getQueryString(final ApplicationUser arg0, final Query arg1) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public SearchContext getSearchContext(final ApplicationUser arg0, final Query arg1) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public QueryContext getSimpleQueryContext(final ApplicationUser arg0, final Query arg1) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public ParseResult parseQuery(final ApplicationUser arg0, final String arg1) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public Query sanitiseSearchQuery(final ApplicationUser arg0, final Query arg1) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public SearchResults search(final ApplicationUser arg0, final Query arg1, final PagerFilter arg2) throws SearchException {
        final SearchResults results = new SearchResults(issuesList, arg2);
        return results;
    }

    @Override
    public long searchCount(final ApplicationUser arg0, final Query arg1) throws SearchException {
        // Auto-generated method stub
        return 0;
    }

    @Override
    public long searchCountOverrideSecurity(final ApplicationUser arg0, final Query arg1) throws SearchException {
        // Auto-generated method stub
        return 0;
    }

    @Override
    public SearchResults searchOverrideSecurity(final ApplicationUser arg0, final Query arg1, final PagerFilter arg2) throws SearchException {
        // Auto-generated method stub
        return null;
    }

    @Override
    public MessageSet validateQuery(final ApplicationUser arg0, final Query arg1) {
        // Auto-generated method stub
        return null;
    }

    @Override
    public MessageSet validateQuery(final ApplicationUser arg0, final Query arg1, final Long arg2) {
        // Auto-generated method stub
        return null;
    }

}
