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
package com.blackducksoftware.integration.jira.web.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.synopsys.integration.util.Stringable;

@XmlAccessorType(XmlAccessType.FIELD)
public class MigrationDetails extends Stringable implements Serializable {
    private static final long serialVersionUID = -6869352381133058302L;

    @XmlElement
    private List<String> migratedProjects;

    @XmlElement
    private List<String> projectsToMigrate;

    @XmlElement
    private String migrationStartTime;

    @XmlElement
    private String migrationEndTime;

    @XmlElement
    private String migrationStatus;

    public MigrationDetails() {
    }

    public List<String> getMigratedProjects() {
        return migratedProjects;
    }

    public void setMigratedProjects(List<String> migratedProjects) {
        this.migratedProjects = migratedProjects;
    }

    public List<String> getProjectsToMigrate() {
        return projectsToMigrate;
    }

    public void setProjectsToMigrate(List<String> projectsToMigrate) {
        this.projectsToMigrate = projectsToMigrate;
    }

    public String getMigrationStartTime() {
        return migrationStartTime;
    }

    public void setMigrationStartTime(String migrationStartTime) {
        this.migrationStartTime = migrationStartTime;
    }

    public String getMigrationEndTime() {
        return migrationEndTime;
    }

    public void setMigrationEndTime(String migrationEndTime) {
        this.migrationEndTime = migrationEndTime;
    }

    public String getMigrationStatus() {
        return migrationStatus;
    }

    public void setMigrationStatus(String migrationStatus) {
        this.migrationStatus = migrationStatus;
    }
}
