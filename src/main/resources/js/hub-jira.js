/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
var statusMessageFieldId = "aui-hub-message-field";
var statusMessageTitleId = "aui-hub-message-title";
var statusMessageTitleTextId = "aui-hub-message-title-text";
var statusMessageTextId = "aui-hub-message-text";

var errorStatus = "error";
var successStatus = "success";

var hiddenClass = "hidden";

var hubProjectMappingContainer = "hubProjectMappingContainer";
var hubProjectMappingElement = "hubProjectMappingElement";
var hubMappingStatus = "mappingStatus";

var jiraProjectListId = "jiraProjects";
var hubProjectListId = "hubProjects";

var jiraProjectDisplayName = "projectName";
var jiraProjectKey = "projectId";
var jiraProjectExists = "projectExists";
var hubProjectDisplayName = "projectName";
var hubProjectKey = "projectUrl";
var hubProjectExists = "projectExists";

var policyRuleTicketCreation = "policyRuleTicketCreation";  
var policyRuleName = "name";
var policyRuleDescription = "description";
var policyRuleUrl = "policyUrl";
var policyRuleChecked = "checked";


var vulnerabilityTicketCreation = "vulnerabilityTicketCreation"; 
var vulnerabilityTicketClosure = "vulnerabilityTicketClosure"; 

var spinning = false;

var mappingElementCounter = 0;

function updateConfig() {
		putConfig(AJS.contextPath() + '/rest/hub-jira-integration/1.0/', 'Save successful.', 'The configuration is not valid.');
	}

function populateForm() {
	  AJS.$.ajax({
	    url: AJS.contextPath() + "/rest/hub-jira-integration/1.0/",
	    dataType: "json",
	    success: function(config) {
	      updateValue("intervalBetweenChecks", config.intervalBetweenChecks);
	      fillInProjects(config.jiraProjects, config.hubProjects);
	      fillInMappings(config.hubProjectMappings);
	      addPolicyViolationRules(config.policyRules);
	      
	      handleError('intervalBetweenChecksError', config.intervalBetweenChecksError);
	      handleError('hubProjectMappingsError', config.hubProjectMappingError);
	      handleError('policyRulesError', config.policyRulesError);
	    }
	  });
}

function putConfig(restUrl, successMessage, failureMessage) {
	var jsonMappingArray = getJsonArrayFromMapping();
	var policyRuleArray = getJsonArrayFromPolicyRules();
	  AJS.$.ajax({
	    url: restUrl,
	    type: "PUT",
	    dataType: "json",
	    contentType: "application/json",
	    data: '{ "intervalBetweenChecks": "' + encodeURI(AJS.$("#intervalBetweenChecks").val())
	    + '", "hubProjectMappings": ' + jsonMappingArray
	    + ', "policyRules": ' + policyRuleArray
	    + '}',
	    processData: false,
	    success: function() {
	    	hideError('intervalBetweenChecksError');
	    	hideError('hubProjectMappingsError');
	    	hideError('policyRulesError');
	    	
		    showStatusMessage(successStatus, 'Success!', successMessage);
		    stopProgressSpinner();
	    },
	    error: function(response){
	    	var config = JSON.parse(response.responseText);
	    	handleError('intervalBetweenChecksError', config.intervalBetweenChecksError);
	    	handleError('hubProjectMappingsError', config.hubProjectMappingError);
	    	handleError('policyRulesError', config.policyRulesError);
	    	
		    showStatusMessage(errorStatus, 'ERROR!', failureMessage);
		    stopProgressSpinner();
	    }
	  });
}

function getJsonArrayFromMapping(){
	var jsonArray = "[";
	var mappingContainer = AJS.$("#" + hubProjectMappingContainer);
	var mappingElements = mappingContainer.children();
	for (i = 0; i < mappingElements.length; i++) {
		if(i > 0){
			jsonArray += ","
		}
		var mappingElement = mappingElements[i];
		var currentJiraProject = AJS.$(mappingElement).find("input[name*='jiraProject']");
		
		var currentJiraProjectDisplayName = currentJiraProject.val();
		var currentJiraProjectValue = currentJiraProject.attr('projectKey');
		var currentJiraProjectExists = currentJiraProject.attr('projectExists');
		
		var currentHubProject = AJS.$(mappingElement).find("input[name*='hubProject']");
		
		var currentHubProjectDisplayName = currentHubProject.val();
		var currentHubProjectValue = currentHubProject.attr('projectKey');
		var currentHubProjectExists = currentHubProject.attr('projectExists');
		
		if(isNullOrWhitespace(currentJiraProjectValue) || isNullOrWhitespace(currentHubProjectValue) || !currentJiraProjectExists || !currentHubProjectExists){
			addMappingErrorStatus(mappingElement);
		} else {
			removeMappingErrorStatus(mappingElement);
		}
		
		jsonArray += '{"'
			+'jiraProject" : {"' 
			+ jiraProjectDisplayName + '":"' + currentJiraProjectDisplayName 
			+ '","' 
			+ jiraProjectKey + '":"' + currentJiraProjectValue 
			+ '","' 
			+ jiraProjectExists + '":"' + currentJiraProjectExists 
			+'"},"' 
			+ 'hubProject" : {"' 
			+  hubProjectDisplayName + '":"' + currentHubProjectDisplayName
			+ '","' 
			+  hubProjectKey + '":"' + currentHubProjectValue
			+ '","' 
			+  hubProjectExists + '":"' + currentHubProjectExists
			+ '"}}';
	}
	jsonArray += "]";
	return jsonArray;
}

function getJsonArrayFromPolicyRules(){
	var jsonArray = "[";
	var policyRuleContainer = AJS.$("#" + policyRuleTicketCreation);
	var policyRules = policyRuleContainer.find("input");
	for (i = 0; i < policyRules.length; i++) {
		if(i > 0){
			jsonArray += ","
		}
		var policyRule = policyRules[i];

		var currentPolicyRuleUrl = policyRule.policyUrl;
		var currentPolicyRuleDescription = policyRule.title;
		var currentPolicyRuleName = policyRule.name;
		var currentPolicyRuleChecked = policyRule.checked;
		
		jsonArray += '{"'
			+ policyRuleName + '":"' + currentPolicyRuleName 
			+ '","' 
			+ policyRuleUrl + '":"' + currentPolicyRuleUrl 
			+ '","' 
			+ policyRuleDescription + '":"' + currentPolicyRuleDescription 
			+ '","' 
			+ policyRuleChecked + '":"' + currentPolicyRuleChecked 
			+ '"}';
	}
	jsonArray += "]";
	return jsonArray;
}

function updateValue(fieldId, configField) {
	if(configField){
		 AJS.$("#" + fieldId).val(decodeURI(configField));
    }
}

function addPolicyViolationRules(policyRules){
	var policyRuleContainer = AJS.$("#" + policyRuleTicketCreation);
	if(policyRules != null && policyRules.length > 0){
		for (p = 0; p < policyRules.length; p++) {
			var newPolicyRuleCheckbox = AJS.$('<input>', {
			    type: "checkbox",
			    policyUrl: decodeURI(policyRules[p].policyUrl),
			    title: decodeURI(policyRules[p].description),
			    name: decodeURI(policyRules[p].name),
			    checked : policyRules[p].checked
			});
			var newPolicyLabel = AJS.$('<label>', {
				text: policyRules[p].name,
				title: decodeURI(policyRules[p].description),
			});
			var newBreak = AJS.$('<br/>', {});
			newPolicyRuleCheckbox.appendTo(policyRuleContainer);
			newPolicyLabel.appendTo(policyRuleContainer);
			newBreak.appendTo(policyRuleContainer);
		}
	}
}

function fillInProjects(jiraProjects, hubProjects){
	var mappingElement = AJS.$("#" + hubProjectMappingElement);
	var jiraProjectList = mappingElement.find("datalist[id='"+ jiraProjectListId +"']");
	if(jiraProjects != null && jiraProjects.length > 0){
		for (j = 0; j < jiraProjects.length; j++) {
			var newOption = AJS.$('<option>', {
			    value: jiraProjects[j].projectName,
			    projectKey: jiraProjects[j].projectId,
			    projectExists: jiraProjects[j].projectExists
			});
			
			jiraProjectList.append(newOption);
		}
	}
	
	var hubProjectList = mappingElement.find("datalist[id='"+ hubProjectListId +"']");
	if(hubProjects != null && hubProjects.length > 0){
		for (h = 0; h < hubProjects.length; h++) {
			var newOption = AJS.$('<option>', {
			    value: hubProjects[h].projectName,
			    projectKey: hubProjects[h].projectUrl,
			    projectExists: hubProjects[h].projectExists
			});
			hubProjectList.append(newOption);
		}
	}
}

function fillInMappings(storedMappings){
	var mappingContainer = AJS.$("#" + hubProjectMappingContainer);
	var mappingElements = mappingContainer.children();
	// On loading the page, there should only be one original mapping element
	if(storedMappings != null && storedMappings.length > 0){
		fillInMapping(mappingElements[0], storedMappings[0]);
		
		for (i = 1; i < storedMappings.length; i++) {
			var newMappingElement = addNewMappingElement(hubProjectMappingElement);
			fillInMapping(newMappingElement, storedMappings[i]);
		}
	}
}

function fillInMapping(mappingElement, storedMapping){
	var currentJiraProject = AJS.$(mappingElement).find("input[name*='jiraProject']");
	
	var storedJiraProject = storedMapping.jiraProject;
	var storedJiraProjectDisplayName = storedJiraProject.projectName;
	var storedJiraProjectValue = storedJiraProject.projectId;
	var storedJiraProjectExists = storedJiraProject.projectExists;
	
	if(!storedJiraProjectExists){
		currentJiraProject.css("color", "red");
	} else{
		currentJiraProject.css("color", "black");
	}
	
	currentJiraProject.val(storedJiraProjectDisplayName);
	currentJiraProject.attr("projectKey", storedJiraProjectValue);
	currentJiraProject.attr("projectExists",storedJiraProjectExists)
	
	var currentHubProject = AJS.$(mappingElement).find("input[name*='hubProject']");
	
	var storedHubProject = storedMapping.hubProject;
	var storedHubProjectDisplayName = storedHubProject.projectName;
	var storedHubProjectValue = storedHubProject.projectUrl;
	var storedHubProjectExists = storedHubProject.projectExists;
	
	if(!storedHubProjectExists){
		currentHubProject.css("color", "red");
	} else{
		currentHubProject.css("color", "black");
	}
	currentHubProject.val(storedHubProjectDisplayName);
	currentHubProject.attr("projectKey", storedHubProjectValue);
	currentHubProject.attr("projectExists",storedHubProjectExists)
	
	if(isNullOrWhitespace(storedJiraProjectValue) || isNullOrWhitespace(storedHubProjectValue) || !storedJiraProjectExists || !storedHubProjectExists){
		addMappingErrorStatus(mappingElement);
	} else {
		removeMappingErrorStatus(mappingElement);
	}
}

function addNewMappingElement(fieldId){
	var elementToAdd = AJS.$("#" + fieldId).clone();
	mappingElementCounter = mappingElementCounter + 1;
	elementToAdd.attr("id", elementToAdd.attr("id") + mappingElementCounter);
	elementToAdd.appendTo("#" + hubProjectMappingContainer);
	
	removeMappingErrorStatus(elementToAdd);
	
	var currentJiraProject = AJS.$(elementToAdd).find("input[name*='jiraProject']");
	
	currentJiraProject.val("");
	currentJiraProject.attr("projectKey", "");
	currentJiraProject.attr("projectExists","true");
	currentJiraProject.css("color", "black");
	
	var currentHubProject = AJS.$(elementToAdd).find("input[name*='hubProject']");
	
	currentHubProject.val("");
	currentHubProject.attr("projectKey", "");
	currentHubProject.attr("projectExists","true");
	currentHubProject.css("color", "black");
	
	return elementToAdd;
}

function removeMappingElement(childElement){
	if(AJS.$("#" + hubProjectMappingContainer).children().length > 1){
		AJS.$(childElement).closest("div[name*='"+ hubProjectMappingElement + "']").remove();
	}
}

function onMappingInputChange(inputField){
	var field = AJS.$(inputField);
	var datalist = inputField.list;
	var options = datalist.options;
	
	var optionFound = false;
    for (var i=0;i<options.length;i++){
       if (options[i].value == inputField.value) { 
    	   optionFound = true;
    	   var option = AJS.$(options[i]);
    	   
    	   var projectKey = option.attr("projectKey");
    	   var projectExists = option.attr("projectExists");
    	   field.val(option.val());
    	   field.attr("projectKey", projectKey);
    	   field.attr("projectExists", projectExists)
    	   
    	   if(projectExists === 'false'){
    		   field.css("color", "red");
    		} else{
    			field.css("color", "black");
    		}
    	   break;
    	}
    }
    if(!optionFound){
  	   field.attr("projectKey", "");
  	   field.attr("projectExists", "false")
  	   field.css("color", "red");
    }
}

function handleError(fieldId, configField) {
	if(configField){
		showError(fieldId, configField);
    } else{
    	hideError(fieldId);
    }
}

function showError(fieldId, configField) {
	  AJS.$("#" + fieldId).text(decodeURI(configField));
  	  removeClassFromFieldById(fieldId, hiddenClass);
}

function hideError(fieldId) {
	  AJS.$("#" + fieldId).text('');
  	  addClassToFieldById(fieldId, hiddenClass);
}

function showStatusMessage(status, statusTitle, message) {
	resetStatusMessage();
	if(status == errorStatus){
		addClassToFieldById(statusMessageFieldId, 'error');
		addClassToFieldById(statusMessageTitleId, 'icon-error');
	} else if (status == successStatus){
		addClassToFieldById(statusMessageFieldId, 'success');
		addClassToFieldById(statusMessageTitleId, 'icon-success');
	}
	AJS.$("#" + statusMessageTitleTextId).text(statusTitle);
	AJS.$("#" + statusMessageTextId).text(message);
	removeClassFromFieldById(statusMessageFieldId, hiddenClass);
}

function resetStatusMessage() {
	removeClassFromFieldById(statusMessageFieldId,'error');
	removeClassFromFieldById(statusMessageFieldId,'success');
	removeClassFromFieldById(statusMessageTitleId,'icon-error');
	removeClassFromFieldById(statusMessageTitleId,'icon-success');
	AJS.$("#" + statusMessageTitleTextId).text('');
	AJS.$("#" + statusMessageTextId).text('');
	addClassToFieldById(statusMessageFieldId, hiddenClass);
}

function addClassToFieldById(fieldId, cssClass){
	if(!AJS.$("#" + fieldId).hasClass(cssClass)){
		AJS.$("#" + fieldId).addClass(cssClass);
	}
}

function removeClassFromFieldById(fieldId, cssClass){
	if(AJS.$("#" + fieldId).hasClass(cssClass)){
		AJS.$("#" + fieldId).removeClass(cssClass);
	}
}

function addClassToField(field, cssClass){
	if(!AJS.$(field).hasClass(cssClass)){
		AJS.$(field).addClass(cssClass);
	}
}

function removeClassFromField(field, cssClass){
	if(AJS.$(field).hasClass(cssClass)){
		AJS.$(field).removeClass(cssClass);
	}
}

function addMappingErrorStatus(mappingElement){
	var mappingStatus = AJS.$(mappingElement).find("#" + hubMappingStatus);
	if(mappingStatus.find("i").length == 0){
		var newStatus = AJS.$('<i>', {
			text : "X"
		});
		AJS.$(newStatus).addClass("fa");
		AJS.$(newStatus).addClass("fa-times");
		AJS.$(newStatus).addClass("errorMapping");

		newStatus.appendTo(mappingStatus);
	}
}

function removeMappingErrorStatus(mappingElement){
	var mappingStatus = AJS.$(mappingElement).find("#" + hubMappingStatus);
	if(mappingStatus.children().length > 0){
		AJS.$(mappingStatus).empty();
	}
}

function startProgressSpinner(){
	 if (!spinning) {
		 var spinner = AJS.$('.spinner');
		 spinner.spin('large');
		 spinning = true;
	 }
}

function stopProgressSpinner(){
	 if (spinning) {
		 AJS.$('.spinner').spinStop();
         spinning = false;
	 }
}

function isNullOrWhitespace(input) {
    if (input == null){ 
    	return true;
    }
    input = String(input);
    return input.trim().length < 1;
}

(function ($) {
	populateForm();
})(AJS.$ || jQuery);
