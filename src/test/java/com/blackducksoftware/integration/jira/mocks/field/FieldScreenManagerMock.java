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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public FieldScreenLayoutItem buildNewFieldScreenLayoutItem(final String arg0) {

        return null;
    }

    @Override
    public void createFieldScreen(final FieldScreen arg0) {
    }

    @Override
    public void createFieldScreenLayoutItem(final FieldScreenLayoutItem arg0) {

    }

    @Override
    public void createFieldScreenTab(final FieldScreenTab tab) {
        createdTabs.add(tab);
    }

    public void setDefaultFieldScreen(final FieldScreen defaultScreen) {
        this.defaultScreen = defaultScreen;
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

        return new ArrayList<FieldScreenLayoutItem>();
    }

    @Override
    public FieldScreenTab getFieldScreenTab(final Long arg0) {

        return null;
    }

    @Override
    public Collection<FieldScreenTab> getFieldScreenTabs(final String arg0) {

        return null;
    }

    @Override
    public List<FieldScreenTab> getFieldScreenTabs(final FieldScreen arg0) {

        return new ArrayList<>();
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
        updatedScreens.add(screen);

    }

    @Override
    public void updateFieldScreenLayoutItem(final FieldScreenLayoutItem arg0) {

    }

    @Override
    public void updateFieldScreenTab(final FieldScreenTab tab) {
        updatedTabs.add(tab);

    }

}
