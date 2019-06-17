/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
 * https://www.synopsys.com/
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;

public class FieldScreenManagerMock implements FieldScreenManager {
    private final List<FieldScreenTab> updatedTabs = new ArrayList<>();
    private final List<FieldScreenTab> createdTabs = new ArrayList<>();
    private final List<FieldScreen> updatedScreens = new ArrayList<>();

    private FieldScreen defaultScreen;

    public List<FieldScreenTab> getUpdatedTabs() {
        return updatedTabs;
    }

    public List<FieldScreen> getUpdatedScreens() {
        return updatedScreens;
    }

    @Override
    public FieldScreenLayoutItem buildNewFieldScreenLayoutItem(final String orderableFieldId) {
        final FieldScreenLayoutItemMock fieldScreenLayoutItemMock = new FieldScreenLayoutItemMock();
        fieldScreenLayoutItemMock.setFieldId(orderableFieldId);
        return fieldScreenLayoutItemMock;
    }

    @Override
    public void createFieldScreen(final FieldScreen arg0) {
    }

    @Override
    public void createFieldScreenLayoutItem(final FieldScreenLayoutItem arg0) {
    }

    @Override
    public void createFieldScreenTab(final FieldScreenTab tab) {
        createdTabs.add(createMock(tab));
    }

    public void setDefaultFieldScreen(final FieldScreen defaultScreen) {
        if (FieldScreenMock.class.isInstance(defaultScreen)) {
            this.defaultScreen = defaultScreen;
        } else {
            final FieldScreenMock fieldScreenMock = new FieldScreenMock();
            fieldScreenMock.setId(defaultScreen.getId());
            fieldScreenMock.setName(defaultScreen.getName());
            fieldScreenMock.setDescription(defaultScreen.getDescription());
            fieldScreenMock.setGenericValue(defaultScreen.getGenericValue());
            this.defaultScreen = fieldScreenMock;
        }
    }

    @Override
    public FieldScreen getFieldScreen(final Long fieldId) {
        if (fieldId == FieldScreen.DEFAULT_SCREEN_ID) {
            return defaultScreen;
        }
        return null;
    }

    @Override
    public List<FieldScreenLayoutItem> getFieldScreenLayoutItems(final FieldScreenTab arg0) {
        return new ArrayList<>();
    }

    @Override
    public FieldScreenTab getFieldScreenTab(final Long arg0) {
        return null;
    }

    @Override
    public Collection<FieldScreenTab> getFieldScreenTabs(final String arg0) {
        return Stream.concat(createdTabs.stream(), updatedTabs.stream()).collect(Collectors.toList());
    }

    @Override
    public List<FieldScreenTab> getFieldScreenTabs(final FieldScreen arg0) {
        return Stream.concat(createdTabs.stream(), updatedTabs.stream()).collect(Collectors.toList());
    }

    @Override
    public Collection<FieldScreen> getFieldScreens() {
        return updatedScreens;
    }

    @Override
    public void refresh() {
    }

    @Override
    public void removeFieldScreen(final Long arg0) {
    }

    @Override
    public void removeFieldScreenItems(final String arg0) {
    }

    @Override
    public void removeFieldScreenLayoutItem(final FieldScreenLayoutItem arg0) {
    }

    @Override
    public void removeFieldScreenLayoutItems(final FieldScreenTab arg0) {
    }

    @Override
    public void removeFieldScreenTab(final Long arg0) {
    }

    @Override
    public void removeFieldScreenTabs(final FieldScreen arg0) {
    }

    @Override
    public void updateFieldScreen(final FieldScreen screen) {
        final FieldScreenMock fieldScreenMock = new FieldScreenMock();
        fieldScreenMock.setName(screen.getName());
        fieldScreenMock.setDescription(screen.getDescription());
        fieldScreenMock.setGenericValue(screen.getGenericValue());
        fieldScreenMock.setId(screen.getId());
        fieldScreenMock.store();

        updatedScreens.add(fieldScreenMock);
    }

    @Override
    public void updateFieldScreenLayoutItem(final FieldScreenLayoutItem arg0) {
    }

    @Override
    public void updateFieldScreenTab(final FieldScreenTab tab) {
        updatedTabs.add(createMock(tab));
    }

    private FieldScreenTabMock createMock(final FieldScreenTab tab) {
        final FieldScreen fieldScreen = tab.getFieldScreen();

        final FieldScreenMock fieldScreenMock = new FieldScreenMock();
        fieldScreenMock.setName(fieldScreen.getName());
        fieldScreenMock.setDescription(fieldScreen.getDescription());
        fieldScreenMock.setGenericValue(fieldScreen.getGenericValue());
        fieldScreenMock.setId(fieldScreen.getId());

        final FieldScreenTabMock fieldScreenTabMock = new FieldScreenTabMock();
        fieldScreenTabMock.setName(tab.getName());
        fieldScreenTabMock.setPosition(tab.getPosition());
        fieldScreenTabMock.setGenericValue(tab.getGenericValue());
        fieldScreenTabMock.setFieldScreen(fieldScreenMock);
        fieldScreenTabMock.store();

        for (FieldScreenLayoutItem fieldScreenLayoutItem : tab.getFieldScreenLayoutItems()) {
            fieldScreenTabMock.addFieldScreenLayoutItem(fieldScreenLayoutItem.getFieldId());
        }

        return fieldScreenTabMock;
    }

}
