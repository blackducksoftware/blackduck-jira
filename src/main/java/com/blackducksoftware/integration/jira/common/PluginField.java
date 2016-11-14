
package com.blackducksoftware.integration.jira.common;

public enum PluginField {

    HUB_CUSTOM_FIELD_PROJECT("HUB_CUSTOM_FIELD_PROJECT", HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT),
    HUB_CUSTOM_FIELD_PROJECT_VERSION("HUB_CUSTOM_FIELD_PROJECT_VERSION", HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION),
    HUB_CUSTOM_FIELD_COMPONENT("HUB_CUSTOM_FIELD_COMPONENT", HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT),
    HUB_CUSTOM_FIELD_COMPONENT_VERSION("HUB_CUSTOM_FIELD_COMPONENT_VERSION", HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION),
    HUB_CUSTOM_FIELD_POLICY_RULE("HUB_CUSTOM_FIELD_POLICY_RULE", HubJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE);

    private final String id;

    private final String name;

    private PluginField(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
