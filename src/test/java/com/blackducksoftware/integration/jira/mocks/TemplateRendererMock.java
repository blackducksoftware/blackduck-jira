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

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.atlassian.templaterenderer.RenderingException;
import com.atlassian.templaterenderer.TemplateRenderer;

public class TemplateRendererMock implements TemplateRenderer {

    String renderedString;

    public String getRenderedString() {
        return renderedString;
    }

    @Override
    public void render(final String arg0, final Writer arg1) throws RenderingException, IOException {
        renderedString = arg0;
    }

    @Override
    public void render(final String arg0, final Map<String, Object> arg1, final Writer arg2)
            throws RenderingException, IOException {

    }

    @Override
    public String renderFragment(final String arg0, final Map<String, Object> arg1) throws RenderingException {
        return null;
    }

    @Override
    public boolean resolve(final String arg0) {
        return false;
    }

}
