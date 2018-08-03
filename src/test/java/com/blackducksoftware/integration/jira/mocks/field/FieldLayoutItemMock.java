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

import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;

@SuppressWarnings("rawtypes")
public class FieldLayoutItemMock implements FieldLayoutItem {
    private OrderableField orderableField;

    private boolean isRequired;

    @Override
    public int compareTo(final FieldLayoutItem o) {

        return 0;
    }

    @Override
    public String getFieldDescription() {

        return null;
    }

    @Override
    public FieldLayout getFieldLayout() {

        return null;
    }

    @Override
    public OrderableField getOrderableField() {

        return orderableField;
    }

    public void setOrderableField(final OrderableField orderableField) {
        this.orderableField = orderableField;
    }

    @Override
    public String getRawFieldDescription() {

        return null;
    }

    @Override
    public String getRendererType() {

        return null;
    }

    @Override
    public boolean isHidden() {

        return false;
    }

    @Override
    public boolean isRequired() {

        return isRequired;
    }

    public void setIsRequired(final boolean isRequired) {
        this.isRequired = isRequired;
    }

}
