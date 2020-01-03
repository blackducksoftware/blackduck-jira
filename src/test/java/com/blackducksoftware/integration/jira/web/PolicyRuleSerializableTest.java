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
package com.blackducksoftware.integration.jira.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.jira.web.model.PolicyRuleSerializable;

public class PolicyRuleSerializableTest {

    @Test
    public void testPolicyRuleSerializable() {
        final String name1 = "name1";
        final String description1 = "description1";
        final String policyUrl1 = "policyUrl1";
        final Boolean checked1 = true;
        final Boolean enabled1 = true;

        final String name2 = "name2";
        final String description2 = "description2";
        final String policyUrl2 = "policyUrl2";
        final Boolean checked2 = false;
        final Boolean enabled2 = true;

        final PolicyRuleSerializable item1 = new PolicyRuleSerializable();
        item1.setName(name1);
        item1.setDescription(description1);
        item1.setPolicyUrl(policyUrl1);
        item1.setChecked(checked1);
        item1.setEnabled(enabled1);
        final PolicyRuleSerializable item2 = new PolicyRuleSerializable();
        item2.setName(name2);
        item2.setDescription(description2);
        item2.setPolicyUrl(policyUrl2);
        item2.setChecked(checked2);
        item2.setEnabled(enabled2);
        final PolicyRuleSerializable item3 = new PolicyRuleSerializable();
        item3.setName(name1);
        item3.setDescription(description1);
        item3.setPolicyUrl(policyUrl1);
        item3.setChecked(checked1);
        item3.setEnabled(enabled1);

        assertEquals(name1, item1.getName());
        assertEquals(description1, item1.getDescription());
        assertEquals(policyUrl1, item1.getPolicyUrl());
        assertEquals(checked1, item1.getChecked());
        assertEquals(enabled1, item1.getEnabled());

        assertEquals(name2, item2.getName());
        assertEquals(description2, item2.getDescription());
        assertEquals(policyUrl2, item2.getPolicyUrl());
        assertEquals(checked2, item2.getChecked());
        assertEquals(enabled2, item2.getEnabled());

        assertTrue(!item1.equals(item2));
        assertTrue(item1.equals(item3));

        assertTrue(item1.hashCode() != item2.hashCode());
        assertEquals(item1.hashCode(), item3.hashCode());

        final StringBuilder builder = new StringBuilder();
        builder.append("PolicyRuleSerializable [name=");
        builder.append(item1.getName());
        builder.append(", description=");
        builder.append(item1.getDescription());
        builder.append(", policyUrl=");
        builder.append(item1.getPolicyUrl());
        builder.append(", checked=");
        builder.append(item1.getChecked());
        builder.append(", enabled=");
        builder.append(item1.getEnabled());
        builder.append("]");

        assertEquals(builder.toString(), item1.toString());
    }

}
