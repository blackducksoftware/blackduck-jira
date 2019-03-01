/*
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
var detailsModuleId = "details-module";
var peopleModuleId = "peoplemodule";
var customFieldsModuleId = "customfieldmodule";

var blackDuckCustomFields = [
    "Black Duck Project", "Black Duck Project Version", "Black Duck Project Version Url", "Black Duck Component",
    "Black Duck Component Url", "Black Duck Component Version", "Black Duck Component Version Url", "Black Duck Policy Rule",
    "Black Duck Policy Rule Description", "Black Duck Policy Rule Overridable", "Black Duck Policy Rule Severity",
    "Black Duck Policy Rule Url", "Black Duck Component Licenses", "Black Duck Component Usages", "Black Duck Component Origins",
    "Black Duck Component Origin IDs", "Black Duck Component Reviewer", "Black Duck Project Version Nickname", "Black Duck Project Owner",
    "Black Duck Project Version Last Updated", "Black Duck Component License Url"
];

function hideBlackDuckCustomFields() {
    var detailsModule = AJS.$('#' + detailsModuleId);
    if (detailsModule.length > 0) {
        var customFieldsModule = AJS.$(detailsModule).find('#' + customFieldsModuleId);
        if (customFieldsModule.length > 0) {
            var customFieldPropertyList = AJS.$(customFieldsModule).find(".property-list");
            if (customFieldPropertyList.length > 0) {
                var properties = customFieldPropertyList.children();
                if (properties.length > 0) {
                    for (i = 0; i < properties.length; i++) {
                        checkPropertyAndHideBlackDuckField(properties[i]);
                    }
                } else {
                    setTimeout(hideBlackDuckCustomFields, 100);
                }
            } else {
                setTimeout(hideBlackDuckCustomFields, 100);
            }
        } else {
            setTimeout(hideBlackDuckCustomFields, 100);
        }
    } else {
        setTimeout(hideBlackDuckCustomFields, 100);
    }
}


function checkPropertyAndHideBlackDuckField(property) {
    var customFieldPropertyLabel = AJS.$(property).find("strong.name");
    var customFieldPropertyValueField = AJS.$(property).find("div.value");

    var customFieldName = AJS.$(customFieldPropertyLabel).prop("title");
    var arrayIndex = blackDuckCustomFields.indexOf(customFieldName);
    if (arrayIndex >= 0) {
        var displayStyle = AJS.$(property).css("display");
        if (displayStyle && displayStyle != "none") {
            //AJS.$(property).css("display", "none");
            AJS.$(property).remove();
        }
    }

    AJS.$(customFieldPropertyValueField).change(function () {
        alert("The text has been changed.");
    });
}

function replaceProjectOwnerField() {
    changeBlackDuckFieldInModule("Black Duck Project Owner", "Black Duck Owner", "peoplemodule", "li.people-details", "dt");
}

function replaceComponentReviewerField() {
    changeBlackDuckFieldInModule("Black Duck Component Reviewer", "Black Duck Reviewer", "peoplemodule", "li.people-details", "dt");
}

function changeBlackDuckFieldInModule(fieldName, newValue, moduleId, innerListId, innerTagPropertyString) {
    var moduleObject = AJS.$('#' + moduleId);
    if (moduleObject != null) {
        var tagObject = AJS.$(moduleObject).find(innerListId);
        if (tagObject != null) {
            var tagObjects = tagObject.children();
            if (tagObjects.length > 0) {
                for (i = 0; i < tagObjects.length; i++) {
                    var innerTag = AJS.$(tagObjects[i]).find(innerTagPropertyString)[0];
                    if (innerTag.innerHTML != null && innerTag.innerHTML.includes(fieldName)) {
                        innerTag.innerHTML = newValue + ':';
                    }
                }
            } else {
                setTimeout(changeBlackDuckFieldInModule, 100, fieldName, newValue, moduleId, innerListId, innerTagPropertyString);
            }
        } else {
            setTimeout(changeBlackDuckFieldInModule, 100, fieldName, newValue, moduleId, innerListId, innerTagPropertyString);
        }
    } else {
        setTimeout(changeBlackDuckFieldInModule, 100, fieldName, newValue, moduleId, innerListId, innerTagPropertyString);
    }
}
