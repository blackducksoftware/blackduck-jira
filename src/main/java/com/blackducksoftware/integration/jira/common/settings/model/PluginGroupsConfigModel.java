package com.blackducksoftware.integration.jira.common.settings.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.synopsys.integration.util.Stringable;

public class PluginGroupsConfigModel extends Stringable {
    private Collection<String> groups;

    public static PluginGroupsConfigModel of(final String[] groups) {
        if (groups != null) {
            return new PluginGroupsConfigModel(Arrays.asList(groups));
        }
        return none();
    }

    public static PluginGroupsConfigModel none() {
        return new PluginGroupsConfigModel(Collections.emptySet());
    }

    private PluginGroupsConfigModel(final Collection<String> groups) {
        this.groups = groups;
    }

    public Collection<String> getGroups() {
        return groups;
    }

}
