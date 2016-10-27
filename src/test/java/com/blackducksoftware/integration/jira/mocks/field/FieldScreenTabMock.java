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
package com.blackducksoftware.integration.jira.mocks.field;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;

public class FieldScreenTabMock implements FieldScreenTab {

    private String name;

    private FieldScreen screen;

    private final List<FieldScreenLayoutItem> layoutItems = new ArrayList<>();

    @Override
    public void addFieldScreenLayoutItem(final String fieldId) {
        final FieldScreenLayoutItemMock layoutItem = new FieldScreenLayoutItemMock();
        final OrderableFieldMock field = new OrderableFieldMock();
        field.setId(fieldId);
        layoutItem.setOrderableField(field);
        layoutItems.add(layoutItem);

    }

    @Override
    public void addFieldScreenLayoutItem(final String arg0, final int arg1) {

    }

    @Override
    public FieldScreen getFieldScreen() {

        return screen;
    }

    @Override
    public FieldScreenLayoutItem getFieldScreenLayoutItem(final int arg0) {

        return null;
    }

    @Override
    public FieldScreenLayoutItem getFieldScreenLayoutItem(final String id) {
        for (final FieldScreenLayoutItem layoutItem : layoutItems) {
            if (layoutItem.getOrderableField().getId().equals(id)) {
                return layoutItem;
            }
        }
        return null;
    }

    @Override
    public List<FieldScreenLayoutItem> getFieldScreenLayoutItems() {

        return layoutItems;
    }

    @Override
    public GenericValue getGenericValue() {

        return null;
    }

    @Override
    public Long getId() {

        return null;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public int getPosition() {

        return 0;
    }

    @Override
    public boolean isContainsField(final String arg0) {

        return false;
    }

    @Override
    public boolean isModified() {

        return false;
    }

    @Override
    public void moveFieldScreenLayoutItemDown(final int arg0) {

    }

    @Override
    public void moveFieldScreenLayoutItemFirst(final int arg0) {

    }

    @Override
    public void moveFieldScreenLayoutItemLast(final int arg0) {

    }

    @Override
    public void moveFieldScreenLayoutItemToPosition(final Map<Integer, FieldScreenLayoutItem> arg0) {

    }

    @Override
    public void moveFieldScreenLayoutItemUp(final int arg0) {

    }

    @Override
    public void remove() {

    }

    @Override
    public FieldScreenLayoutItem removeFieldScreenLayoutItem(final int arg0) {

        return null;
    }

    @Override
    public void rename(final String arg0) {

    }

    @Override
    public void setFieldScreen(final FieldScreen screen) {
        this.screen = screen;

    }

    @Override
    public void setGenericValue(final GenericValue arg0) {

    }

    @Override
    public void setName(final String name) {
        this.name = name;

    }

    @Override
    public void setPosition(final int arg0) {

    }

    @Override
    public void store() {

    }

}
