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
