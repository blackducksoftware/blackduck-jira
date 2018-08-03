/*
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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

var hubCustomFields = [
    "BDS Hub Project", "BDS Hub Project Version", "BDS Hub Project Version Url", "BDS Hub Component", 
	"BDS Hub Component Url", "BDS Hub Component Version", "BDS Hub Component Version Url", "BDS Hub Policy Rule", 
	"BDS Hub Policy Rule Description", "BDS Hub Policy Rule Overridable", "BDS Hub Policy Rule Url", "BDS Hub Component Licenses", 
	"BDS Hub Component Usage", "BDS Hub Component Origin", "BDS Hub Component Origin ID", "BDS Hub Project Version Nickname", 
	"BDS Hub Project Owner", "BDS Hub Project Version Last Updated", "BDS Hub Component License Url"
	];

function hideHubCustomFields() {
	var detailsModule = AJS.$('#' + detailsModuleId);
	if (detailsModule.length > 0) {
	var customFieldsModule = AJS.$(detailsModule).find('#' + customFieldsModuleId);
		if (customFieldsModule.length > 0) {
			var customFieldPropertyList =  AJS.$(customFieldsModule).find(".property-list");
			if (customFieldPropertyList.length > 0) {
				var properties = customFieldPropertyList.children();
				if (properties.length > 0) {
					for (i=0; i < properties.length; i++) {
						checkPropertyAndHideHubField(properties[i]);
					}
				} else {
					setTimeout(hideHubCustomFields, 100);
				}
			} else {
				setTimeout(hideHubCustomFields, 100);
			}
		} else {
			setTimeout(hideHubCustomFields, 100);
		}
	} else {
		setTimeout(hideHubCustomFields, 100);
	}
}


function checkPropertyAndHideHubField(property) {
	var customFieldPropertyLabel =  AJS.$(property).find("strong.name");
	var customFieldPropertyValueField =  AJS.$(property).find("div.value");
	
	var customFieldName = AJS.$(customFieldPropertyLabel).prop("title");
	var arrayIndex = hubCustomFields.indexOf(customFieldName);
	if (arrayIndex >= 0) {
		var displayStyle = AJS.$(property).css("display");
		if(displayStyle && displayStyle != "none") {
			//AJS.$(property).css("display", "none");
			AJS.$(property).remove();
		}
	}
	
	AJS.$(customFieldPropertyValueField).change(function() {
	    alert("The text has been changed.");
	});
}

function replaceProjectOwnerField() {
	changeHubFieldInModule("BDS Hub Project Owner", "Black Duck Owner", "peoplemodule", "li.people-details", "dt");
}

function changeHubFieldInModule(fieldName, newValue, moduleId, innerListId, innerTagPropertyString) {
	var moduleObject = AJS.$('#' + moduleId);
	if (moduleObject != null) {
		var tagObject = AJS.$(moduleObject).find(innerListId);
		if (tagObject != null) {
			var tagObjects = tagObject.children();
			if (tagObjects.length > 0) {
				for (i=0; i<tagObjects.length;i++) {
					var innerTag = AJS.$(tagObjects[i]).find(innerTagPropertyString)[0];
					if (innerTag.innerHTML != null && innerTag.innerHTML.includes(fieldName)) {
						innerTag.innerHTML = newValue + ':';
					}
				}
			} else {
				setTimeout(changeHubFieldInModule, 100, fieldName, newValue, moduleId, innerListId, innerTagPropertyString);
			}
		} else {
			setTimeout(changeHubFieldInModule, 100, fieldName, newValue, moduleId, innerListId, innerTagPropertyString);
		}
	} else {
		setTimeout(changeHubFieldInModule, 100, fieldName, newValue, moduleId, innerListId, innerTagPropertyString);
	}
}
