/**
 * Hub JIRA Plugin
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
package com.blackducksoftware.integration.jira;

import java.util.Comparator;

public class JiraVersionComparator implements Comparator<JiraVersion> {
    @Override
    public int compare(final JiraVersion o1, final JiraVersion o2) {
        if (o1.getMajor() > o2.getMajor()) {
            return 1;
        }
        if (o1.getMajor() < o2.getMajor()) {
            return -1;
        }
        return Integer.compare(o1.getMinor(), o2.getMinor());
    }

}
