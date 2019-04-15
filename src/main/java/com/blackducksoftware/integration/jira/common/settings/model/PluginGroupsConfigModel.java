package com.blackducksoftware.integration.jira.common.settings.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.util.Stringable;

public class PluginGroupsConfigModel extends Stringable {
    public static final String BLACK_DUCK_GROUPS_LIST_DELIMETER = ",";

    private Collection<String> groups;

    public static PluginGroupsConfigModel fromDelimitedString(final String delimitedString) {
        if (StringUtils.isNotBlank(delimitedString)) {
            String[] groups = delimitedString.split(PluginGroupsConfigModel.BLACK_DUCK_GROUPS_LIST_DELIMETER);
            return PluginGroupsConfigModel.of(groups);
        }
        return none();
    }

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

    public String getGroupsStringDelimited() {
        return StringUtils.join(groups, BLACK_DUCK_GROUPS_LIST_DELIMETER);
    }

}
