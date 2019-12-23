package com.blackducksoftware.integration.jira.data.accessor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.jira.data.PluginConfigKeys;

public class MigrationAccessor {
    private final JiraSettingsAccessor jiraSettingsAccessor;

    public MigrationAccessor(JiraSettingsAccessor jiraSettingsAccessor) {
        this.jiraSettingsAccessor = jiraSettingsAccessor;
    }

    public List<String> getMigratedProjects() {
        String storedMigratedProjects = jiraSettingsAccessor.getStringValue(PluginConfigKeys.PROJECTS_MIGRATED_TO_ALERT);
        if (null == storedMigratedProjects) {
            return new ArrayList<>();
        }
        return Stream.of(storedMigratedProjects.split(",")).collect(Collectors.toCollection(ArrayList::new));
    }

    public void updateMigratedProjects(List<String> migratedProjects) {
        String migratedProjectsToStore = StringUtils.join(migratedProjects, ",");
        jiraSettingsAccessor.setValue(PluginConfigKeys.PROJECTS_MIGRATED_TO_ALERT, migratedProjectsToStore);
    }

}
