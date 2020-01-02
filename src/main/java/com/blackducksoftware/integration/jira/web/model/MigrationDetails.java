package com.blackducksoftware.integration.jira.web.model;

import java.io.Serializable;
import java.util.Date;
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
    private Date migrationStartTime;

    @XmlElement
    private Date migrationEndTime;

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

    public Date getMigrationStartTime() {
        return migrationStartTime;
    }

    public void setMigrationStartTime(Date migrationStartTime) {
        this.migrationStartTime = migrationStartTime;
    }

    public Date getMigrationEndTime() {
        return migrationEndTime;
    }

    public void setMigrationEndTime(Date migrationEndTime) {
        this.migrationEndTime = migrationEndTime;
    }

    public String getMigrationStatus() {
        return migrationStatus;
    }

    public void setMigrationStatus(String migrationStatus) {
        this.migrationStatus = migrationStatus;
    }
}
