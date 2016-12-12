
package com.blackducksoftware.integration.jira.common;

public enum PluginField {

    HUB_CUSTOM_FIELD_PROJECT("HUB_CUSTOM_FIELD_PROJECT", HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT, HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_DISPLAYNAMEPROPERTY),
    HUB_CUSTOM_FIELD_PROJECT_VERSION("HUB_CUSTOM_FIELD_PROJECT_VERSION", HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION, HubJiraConstants.HUB_CUSTOM_FIELD_PROJECT_VERSION_DISPLAYNAMEPROPERTY),
    HUB_CUSTOM_FIELD_COMPONENT("HUB_CUSTOM_FIELD_COMPONENT", HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT, HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_DISPLAYNAMEPROPERTY),
    HUB_CUSTOM_FIELD_COMPONENT_VERSION("HUB_CUSTOM_FIELD_COMPONENT_VERSION", HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION, HubJiraConstants.HUB_CUSTOM_FIELD_COMPONENT_VERSION_DISPLAYNAMEPROPERTY),
    HUB_CUSTOM_FIELD_POLICY_RULE("HUB_CUSTOM_FIELD_POLICY_RULE", HubJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE, HubJiraConstants.HUB_CUSTOM_FIELD_POLICY_RULE_DISPLAYNAMEPROPERTY);

    private final String id;

    private final String name;

    private final String displayNameProperty;

    private PluginField(String id, String name, String displayNameProperty) {
        this.id = id;
        this.name = name;
        this.displayNameProperty = displayNameProperty;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDisplayNameProperty() {
        return displayNameProperty;
    }

}
