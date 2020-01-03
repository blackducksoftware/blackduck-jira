/**
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2020 Synopsys, Inc.
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
package com.blackducksoftware.integration.jira.mocks;

import com.atlassian.event.api.EventPublisher;

public class EventPublisherMock implements EventPublisher {

    public boolean registered = false;

    public boolean published = false;

    @Override
    public void register(final Object arg0) {
        registered = true;
    }

    @Override
    public void unregister(final Object arg0) {
        registered = false;
        published = false;
    }

    @Override
    public void unregisterAll() {
        registered = false;
        published = false;
    }

    @Override
    public void publish(final Object arg0) {
        published = true;
    }
}
