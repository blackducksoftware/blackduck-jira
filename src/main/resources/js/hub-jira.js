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

var errorMessageFieldId = "error-message-field";

var errorStatus = "error";
var successStatus = "success";

var hiddenClass = "hidden";

var hubJiraGroupsId = "hubJiraGroups";

var hubProjectMappingContainer = "hubProjectMappingContainer";
var hubProjectMappingElement = "hubProjectMappingElement";
var hubMappingStatus = "mappingStatus";

var jiraProjectListId = "jiraProjects";
var hubProjectListId = "hubProjects";

var jiraProjectListErrorId = "jiraProjectListError";
var hubProjectListErrorId = "hubProjectListError";

var jiraProjectErrorId = "jiraProjectError";

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

var ticketCreationFieldSetId = "ticketCreationFieldSet";
var ticketCreationLoadingErrorId = "ticketCreationLoadingError";
var ticketCreationErrorsTableId = "ticketCreationErrorsTable";
var ticketCreationErrorRowId = "ticketCreationErrorRow";


var ticketCreationErrorCounter = 0;
var mappingElementCounter = 0;

var gotJiraProjects = false;
var gotHubProjects = false;
var gotProjectMappings = false;

var jiraProjectMap = new Map();
var hubProjectMap = new Map();

function initTabs() {
	console.log("Initializing tabs");
	
    // Declare all variables
    var i, tabcontent, tablinks;

    // Get all elements with class="tabcontent" and hide them
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }

    // Get all elements with class="tablinks" and remove the class "active"
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }

    // Show the current tab, and add an "active" class to the link that opened the tab
    document.getElementById("Admin").style.display = "block";
    
    // evt.currentTarget.className += " active";
}

function openTab(evt, cityName) {
	console.log("Opening: " + cityName);
	
    // Declare all variables
    var i, tabcontent, tablinks;

    // Get all elements with class="tabcontent" and hide them
    tabcontent = document.getElementsByClassName("tabcontent");
    for (i = 0; i < tabcontent.length; i++) {
        tabcontent[i].style.display = "none";
    }

    // Get all elements with class="tablinks" and remove the class "active"
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }

    // Show the current tab, and add an "active" class to the link that opened the tab
    document.getElementById(cityName).style.display = "block";
    evt.currentTarget.className += " active";
}

function updateConfig() {
		putConfig(AJS.contextPath() + '/rest/hub-jira-integration/1.0/', 'Save successful.', 'The configuration is not valid.');
	}
	
	function updateAdminConfig() {
		putAdminConfig(AJS.contextPath() + '/rest/hub-jira-integration/1.0/admin', 'Save successful.', 'The configuration is not valid.');
	}

function populateForm() {
	AJS.$.ajax({
	    url: AJS.contextPath() + "/rest/hub-jira-integration/1.0/admin/",
	    dataType: "json",
	    success: function(admin) {
	      fillInJiraGroups(admin.hubJiraGroups, admin.jiraGroups);
	      
	      handleError('hubJiraGroupsError', admin.hubJiraGroupsError, true, false);
	    },
	    error: function(response){
	    	handleDataRetrievalError(response, "hubJiraGroupsError", "There was a problem retrieving the Admin configuration.", "Admin Error");
	    }
	  });

	  AJS.$.ajax({
	    url: AJS.contextPath() + "/rest/hub-jira-integration/1.0/interval/",
	    dataType: "json",
	    success: function(config) {
	      updateValue("intervalBetweenChecks", config.intervalBetweenChecks);
	      
	      handleError('intervalBetweenChecksError', config.intervalBetweenChecksError, true, false);
	    },
	    error: function(response){
	    	handleDataRetrievalError(response, "intervalBetweenChecksError", "There was a problem retrieving the Interval.", "Interval Error");
	    }
	  });
	  AJS.$.ajax({
		    url: AJS.contextPath() + "/rest/hub-jira-integration/1.0/jiraProjects/",
		    dataType: "json",
		    success: function(config) {
		      fillInJiraProjects(config.jiraProjects);
		      
		      handleError(jiraProjectListErrorId, config.jiraProjectsError, false, false);
		      handleError(errorMessageFieldId, config.errorMessage, true, false);
		      
		      gotJiraProjects = true;
		    },
		    error: function(response){
		    	handleDataRetrievalError(response, jiraProjectListErrorId, "There was a problem retrieving the Jira Projects.", "Jira Project Error");
		    }
		  });
	  AJS.$.ajax({
		    url: AJS.contextPath() + "/rest/hub-jira-integration/1.0/hubProjects/",
		    dataType: "json",
		    success: function(config) {
		      fillInHubProjects(config.hubProjects);
		     
		      handleError(hubProjectListErrorId, config.hubProjectsError, false, false);
		      handleError(errorMessageFieldId, config.errorMessage, true, false);
		      
		      gotHubProjects = true;
		    },
		    error: function(response){
		    	handleDataRetrievalError(response, hubProjectListErrorId, "There was a problem retrieving the Hub Projects.", "Hub Project Error");
		    }
		  });
	  AJS.$.ajax({
		    url: AJS.contextPath() + "/rest/hub-jira-integration/1.0/hubPolicies/",
		    dataType: "json",
		    success: function(config) {
		      addPolicyViolationRules(config.policyRules);

		      handleError(errorMessageFieldId, config.errorMessage, true, false);
		      handleError('policyRulesError', config.policyRulesError, true, false);
		    },
		    error: function(response){
		    	handleDataRetrievalError(response, "policyRulesError", "There was a problem retrieving the Hub Policy Rules.", "Hub Policy Rules Error");
		    },
		    complete: function(jqXHR, textStatus){
		    	 AJS.$('#policyRuleSpinner').remove();
		    }
		  });
	  AJS.$.ajax({
		    url: AJS.contextPath() + "/rest/hub-jira-integration/1.0/mappings/",
		    dataType: "json",
		    success: function(config) {
		      fillInMappings(config.hubProjectMappings);
		      
		      handleError(errorMessageFieldId, config.errorMessage, true, false);
		      handleError('hubProjectMappingsError', config.hubProjectMappingError, true, false);
		      
		      gotProjectMappings = true;
		    },
		    error: function(response){
		    	handleDataRetrievalError(response, "hubProjectMappingsError", "There was a problem retrieving the Project Mappings.", "Project Mapping Error");
		    },
		    complete: function(jqXHR, textStatus){
		    	 AJS.$('#projectMappingSpinner').remove();
		    }
		  });
	  AJS.$.ajax({
		    url: AJS.contextPath() + "/rest/hub-jira-integration/1.0/hubJiraTicketErrors/",
		    dataType: "json",
		    success: function(creationError) {
		    	updateTicketCreationErrors(creationError.hubJiraTicketErrors);
		    },
		    error: function(response){
		    	var fieldSet = AJS.$('#' + ticketCreationFieldSetId);
		    	if(fieldSet.hasClass('hidden')){
		    		fieldSet.removeClass('hidden');
				}
		    	handleDataRetrievalError(response, "ticketCreationLoadingError", "There was a problem retrieving the Ticket Creation Errors.", "Ticket Creation Error");
		    }
	  });
}


function resetSalKeys(){
	var restUrl = AJS.contextPath() + '/rest/hub-jira-integration/1.0/reset';
	AJS.$.ajax({
	    url: restUrl,
	    type: "PUT",
	    dataType: "json",
	    contentType: "application/json",
	    data: '{}',
	    processData: false,
	    success: function() {
	    	alert('Hub Jira keys reset!');
	    },
	    error: function(response){
	    	alert(response.responseText);
	    },
     	complete: function(jqXHR, textStatus){
	    	  stopProgressSpinner('resetSpinner');
	    }
	  });
	  
	  var ticketCreationErrorTable = AJS.$('#' + ticketCreationErrorsTableId);
	  ticketCreationErrorTable.empty();
}

function handleErrorResize(expansionIcon){
	var currentIcon = AJS.$(expansionIcon);
	var errorRow = currentIcon.closest("tr");
	var errorColumn = errorRow.find('td');
	var errorMessageDiv = AJS.$(errorColumn).children("div[name*='ticketCreationErrorMessageName']");
	var stackTraceDiv = AJS.$(errorColumn).children("div[name*='ticketCreationStackTraceName']");
	
	if(currentIcon.hasClass('fa-plus-square-o')){
		currentIcon.removeClass('fa-plus-square-o');
		currentIcon.addClass('fa-minus-square-o');
		if(!errorMessageDiv.hasClass(hiddenClass)){
			errorMessageDiv.addClass(hiddenClass);
		}
		if(stackTraceDiv.hasClass(hiddenClass)){
			stackTraceDiv.removeClass(hiddenClass);
		}
	} else if(currentIcon.hasClass('fa-minus-square-o')){
		currentIcon.removeClass('fa-minus-square-o');
		currentIcon.addClass('fa-plus-square-o');
		if(errorMessageDiv.hasClass(hiddenClass)){
			errorMessageDiv.removeClass(hiddenClass);
		}
		if(!stackTraceDiv.hasClass(hiddenClass)){
			stackTraceDiv.addClass(hiddenClass);
		}
	}
}

function updateTicketCreationErrors(hubJiraTicketErrors){
	if(hubJiraTicketErrors != null && hubJiraTicketErrors.length > 0){
		var fieldSet = AJS.$('#' + ticketCreationFieldSetId);
		if(fieldSet.hasClass(hiddenClass)){
			fieldSet.removeClass(hiddenClass);
		}
		var ticketCreationErrorTable = AJS.$('#' + ticketCreationErrorsTableId);
		for (j = 0; j < hubJiraTicketErrors.length; j++) {
			
			var ticketErrorRow = AJS.$("#" + ticketCreationErrorRowId).clone();
			ticketCreationErrorCounter = ticketCreationErrorCounter + 1;
			ticketErrorRow.removeClass(hiddenClass);
			ticketErrorRow.attr("id", ticketErrorRow.attr("id") + ticketCreationErrorCounter);
			ticketErrorRow.appendTo(ticketCreationErrorTable);
			
			var errorColumn = ticketErrorRow.find('td');
			
			var stackTraceDiv = AJS.$(errorColumn).children("div[name*='ticketCreationStackTraceName']");
		
			var stackTrace = hubJiraTicketErrors[j].stackTrace;
			
			var errorMessageDiv = AJS.$(errorColumn).children("div[name*='ticketCreationErrorMessageName']");
			var errorMessage = "";
			if(stackTrace.indexOf("\n") > -1){
				errorMessage = stackTrace.substring(0, stackTrace.indexOf("\n"));
				
				stackTraceDiv.text(stackTrace);
			} else {
				errorMessage = stackTrace;
				var expansionIconDiv = AJS.$(errorColumn).children("div[name*='expansionIconDiv']");
				var expansionIcon = AJS.$(expansionIconDiv).children("i[name*='expansionIcon']");
				if(expansionIcon.hasClass('fa-plus-square-o')){
					expansionIcon.removeClass('fa-plus-square-o');
					expansionIcon.addClass('fa-square-o');
				}
			}
			
			errorMessageDiv.text(errorMessage);
			
			var timeStampDiv = AJS.$(errorColumn).children("div[name*='ticketCreationTimeStampName']");
			var timeStamp = hubJiraTicketErrors[j].timeStamp;
			
			if(timeStampDiv.hasClass(hiddenClass)){
				timeStampDiv.removeClass(hiddenClass);
			}
			
			timeStampDiv.text(timeStamp);
		}
	}
}

function getJsonArrayFromErrors(errorRow){
	var jsonArray = "[";

	var errorColumn = AJS.$(errorRow).find('td');
	
	var creationErrorMessage = AJS.$(errorColumn).children("div[name*='ticketCreationErrorMessageName']");
	
	var creationErrorStackTrace = AJS.$(errorColumn).children("div[name*='ticketCreationStackTraceName']");
	
	var creationErrorTimeStamp = AJS.$(errorColumn).children("div[name*='ticketCreationTimeStampName']");
	
	var stackTrace = creationErrorStackTrace.text().trim();
	if(stackTrace){
		stackTrace = encodeURIComponent(stackTrace);
	} else {
		var errorMessage = creationErrorMessage.text().trim();
		
		stackTrace = encodeURIComponent(errorMessage);
	}
	var timeStamp = creationErrorTimeStamp.text().trim();
	timeStamp = encodeURIComponent(timeStamp);
	
	jsonArray += '{"'
		+'stackTrace" : "'  + stackTrace
		+'","' 
		+'timeStamp" : "'  + timeStamp
		+ '"}';
	jsonArray += "]";
	return jsonArray;
}

function handleErrorRemoval(trashIcon){
	var currentIcon = AJS.$(trashIcon);
	var errorRow = currentIcon.closest("tr");

	var restUrl = AJS.contextPath() + '/rest/hub-jira-integration/1.0/removeErrors';
	
	var hubJiraTicketErrors = getJsonArrayFromErrors(errorRow);
	AJS.$.ajax({
	    url: restUrl,
	    type: "PUT",
	    dataType: "json",
	    contentType: "application/json",
	    data: '{ "hubJiraTicketErrors": ' + hubJiraTicketErrors
	    + '}',
	    processData: false,
	    success: function() {
	    	alert('Error successfully removed');
	    },
	    error: function(response){
	    	try {
	    		var creationErrorObj = JSON.parse(response.responseText);
	    		alert(creationErrorObj.configError);
	    	} catch(err) {
	    		// in case the response is not our error object
	    		alert(response.responseText);
	    	}
	    }
	  });
	
	errorRow.remove();
	
	var ticketCreationErrorContainer = AJS.$("#" + ticketCreationErrorsTableId);
	var creationErrors = ticketCreationErrorContainer.find("tr[name*='"+ ticketCreationErrorRowId + "']");
	if(creationErrors.length <= 1){
		var fieldSet = AJS.$('#' + ticketCreationFieldSetId);
		if(!fieldSet.hasClass(hiddenClass)){
			fieldSet.addClass(hiddenClass);
		}
	}
}

function handleDataRetrievalError(response, errorId, errorText, dialogTitle){
	var errorField = AJS.$('#' + errorId);
	errorField.text(errorText);
	removeClassFromField(errorField, hiddenClass);
	addClassToField(errorField, "clickable");
	var error = JSON.parse(response.responseText);
	error = AJS.$(error);
	errorField.click(function() {showErrorDialog(dialogTitle, error.attr("message"), error.attr("status-code"), error.attr("stack-trace")) });
}

function showErrorDialog(header, errorMessage, errorCode, stackTrace){
	var errorDialog = new AJS.Dialog({
	    width: 800, 
	    height: 500, 
	    id: 'error-dialog', 
	    closeOnOutsideClick: true
	});

	errorDialog.addHeader(header);
	
	var errorBody = AJS.$('<div>', {
	});
	var errorMessage = AJS.$('<p>', {
		text : "Error Message : "+ errorMessage
	});
	var errorCode = AJS.$('<p>', {
		text : "Error Code : "+ errorCode
	});
	var errorStackTrace = AJS.$('<p>', {
		text : stackTrace
	});
	
	errorBody.append(errorMessage, errorCode, errorStackTrace);
	
	errorDialog.addPanel(header, errorBody, "panel-body");
	
	errorDialog.addButton("OK", function (dialog) {
		errorDialog.hide();
	});

	errorDialog.show();
}

AJS.$(document).ajaxComplete(function( event, xhr, settings ) {
	if(gotJiraProjects == true && gotHubProjects == true && gotProjectMappings == true){
	
	var mappingContainer = AJS.$("#" + hubProjectMappingContainer);
	var mappingElements = mappingContainer.find("tr[name*='"+ hubProjectMappingElement + "']");
	
	  if (mappingElements.length > 0 ) {
		  for (m = 0; m < mappingElements.length; m++) {
			  var currentJiraProject = AJS.$(mappingElements[m]).find("input[name*='jiraProject']");
			  var jiraProjectError = true;
			  if(currentJiraProject != null){
				  var key = String(currentJiraProject.attr("projectkey"));
				  if(key){
					  var test = jiraProjectMap.get(key);
					  if(test) {
						  var jiraProjectMappingParent = currentJiraProject.parent();
						  var jiraProjectMappingError = jiraProjectMappingParent.children("#"+jiraProjectErrorId);
						  if(test.projectError){
							  jiraProjectError = true;
							  jiraProjectMappingError.text(test.projectError.trim());
							  if(!jiraProjectMappingError.hasClass('error')){
								  jiraProjectMappingError.addClass('error');
							  }
						  } else{
							  jiraProjectError = false;
							  if(jiraProjectMappingError.hasClass('error')){
								  jiraProjectMappingError.removeClass('error');
							  }
						  }
					  }
				  }
			  }

			  if(jiraProjectError){
					if(!currentJiraProject.hasClass('error')){
						currentJiraProject.addClass('error');
					}
				} else{
					if(currentJiraProject.hasClass('error')){
						currentJiraProject.removeClass('error');
					}
				}
			  
			  var currentHubProject = AJS.$(mappingElements[m]).find("input[name*='hubProject']");
			  var hubProjectError = true;
			  if(currentHubProject != null){
				  var key = String(currentHubProject.attr("projectkey"));
				  if(key){
					  var test = hubProjectMap.get(key);
					  if(test) {
						  hubProjectError = false;
					  }
					  
				  }
			  }
			  if(hubProjectError){
					if(!currentHubProject.hasClass('error')){
						currentHubProject.addClass('error');
					}
				} else{
					if(currentHubProject.hasClass('error')){
						currentHubProject.removeClass('error');
					}
				}
			  if(jiraProjectError || hubProjectError){
					addMappingErrorStatus(AJS.$(mappingElements[m]));
			  } else {
					removeMappingErrorStatus(AJS.$(mappingElements[m]));
			  }
		  }
	  }
	}
});

function putAdminConfig(restUrl, successMessage, failureMessage) {

var hubJiraGroups = encodeURI(AJS.$("#" + hubJiraGroupsId).val()); 

	  AJS.$.ajax({
	    url: restUrl,
	    type: "PUT",
	    dataType: "json",
	    contentType: "application/json",
	    data: '{ "hubJiraGroups": "' + hubJiraGroups
	    + '"}',
	    processData: false,
	    success: function() {
	    	hideError('hubJiraGroupsError');
	    	
		    showStatusMessage(successStatus, 'Success!', successMessage);
	    },
	    error: function(response){
	    	try {
		    	var admin = JSON.parse(response.responseText);
		    	handleError('hubJiraGroupsError', admin.hubJiraGroupsError, true, true);
		    	
			    showStatusMessage(errorStatus, 'ERROR!', failureMessage);
	    	} catch(err) {
	    		// in case the response is not our error object
	    		alert(response.responseText);
	    	}
	    },
	    complete: function(jqXHR, textStatus){
	    	 stopProgressSpinner('adminSaveSpinner');
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
	    	hideError(errorMessageFieldId);
	    	hideError('intervalBetweenChecksError');
	    	hideError('hubProjectMappingsError');
	    	hideError('policyRulesError');
	    	
		    showStatusMessage(successStatus, 'Success!', successMessage);
	    },
	    error: function(response){
	    	try {
		    	var config = JSON.parse(response.responseText);
		    	handleError(errorMessageFieldId, config.errorMessage, true, true);
		    	handleError('intervalBetweenChecksError', config.intervalBetweenChecksError, true, true);
		    	handleError('hubProjectMappingsError', config.hubProjectMappingError, true, true);
		    	handleError('policyRulesError', config.policyRulesError, true, true);
		    	
			    showStatusMessage(errorStatus, 'ERROR!', failureMessage);
	    	} catch(err) {
	    		// in case the response is not our error object
	    		alert(response.responseText);
	    	}
	    },
	    complete: function(jqXHR, textStatus){
	    	 stopProgressSpinner('saveSpinner');
	    }
	  });
}

function getJsonArrayFromMapping(){
	var jsonArray = "[";
	var mappingContainer = AJS.$("#" + hubProjectMappingContainer);
	var mappingElements = mappingContainer.find("tr[name*='"+ hubProjectMappingElement + "']");
	for (i = 1; i < mappingElements.length; i++) {
		if(i > 1){
			jsonArray += ","
		}
		var mappingElement = mappingElements[i];
		var currentJiraProject = AJS.$(mappingElement).find("input[name*='jiraProject']");
		
		var currentJiraProjectDisplayName = currentJiraProject.val();
		var currentJiraProjectValue = currentJiraProject.attr('projectKey');
		var currentJiraProjectError = currentJiraProject.attr('projectError');
		
		var currentHubProject = AJS.$(mappingElement).find("input[name*='hubProject']");
		
		var currentHubProjectDisplayName = currentHubProject.val();
		var currentHubProjectValue = currentHubProject.attr('projectKey');
		var currentHubProjectError = currentHubProject.attr('projectError');
		
		
		if(isNullOrWhitespace(currentJiraProjectValue) || isNullOrWhitespace(currentHubProjectValue) || currentJiraProjectError || currentHubProjectError){
			addMappingErrorStatus(mappingElement);
		} else {
			removeMappingErrorStatus(mappingElement);
		}
		
		jsonArray += '{"'
			+'jiraProject" : {"' 
			+ jiraProjectDisplayName + '":"' + currentJiraProjectDisplayName 
			+ '","' 
			+ jiraProjectKey + '":"' + currentJiraProjectValue 
			+'"},"' 
			+ 'hubProject" : {"' 
			+  hubProjectDisplayName + '":"' + currentHubProjectDisplayName
			+ '","' 
			+  hubProjectKey + '":"' + currentHubProjectValue
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
		var policyRule = AJS.$(policyRules[i]);

		var currentPolicyRuleUrl = policyRule.attr("policyurl");
		var currentPolicyRuleDescription = policyRule.attr("title");
		var currentPolicyRuleName = policyRule.attr("name");
		var currentPolicyRuleChecked = policyRules[i].checked;
		
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
			var newPolicy = AJS.$('<div>', {});
			
			var newPolicyRuleCheckbox = AJS.$('<input>', {
			    type: "checkbox",
			    policyurl: decodeURI(policyRules[p].policyUrl),
			    title: decodeURI(policyRules[p].description),
			    name: decodeURI(policyRules[p].name),
			    checked : policyRules[p].checked
			});
			var description = decodeURI(policyRules[p].description);
			var newPolicyLabel = AJS.$('<label>', {
				text: policyRules[p].name,
				title: description,
			});
			newPolicyLabel.addClass("textStyle");
			newPolicyLabel.css("padding" , "0px 5px 0px 5px")
			if(!policyRules[p].enabled){
				newPolicyLabel.addClass("disabledPolicyRule");
			}
			
			
			newPolicy.append(newPolicyRuleCheckbox, newPolicyLabel)
			
			if(description){
				var newDescription = AJS.$('<i>', {
					title: description,
				});
				AJS.$(newDescription).addClass("fa");
				AJS.$(newDescription).addClass("fa-info-circle");
				AJS.$(newDescription).addClass("infoIcon");
				newPolicy.append(newDescription);
			}
			
			newPolicy.appendTo(policyRuleContainer);
			
			
		}
	}
}

function fillInJiraGroups(hubJiraGroups, jiraGroups){
	var splitHubJiraGroups = null;
	if(hubJiraGroups != null){
	  splitHubJiraGroups = hubJiraGroups.split(",");
	}
	var jiraGroupList = AJS.$("#"+hubJiraGroupsId);
	if(jiraGroups != null && jiraGroups.length > 0){
		for (j = 0; j < jiraGroups.length; j++) {
			var optionSelected = false;
			if(splitHubJiraGroups != null){
				for (g = 0; g < splitHubJiraGroups.length; g++) {
					if(splitHubJiraGroups[g] === jiraGroups[j]){
						optionSelected = true;
					}
				}
			}
		
			var newOption = AJS.$('<option>', {
			    value: jiraGroups[j],
			    text: jiraGroups[j],
			    selected: optionSelected
			});
			
			jiraGroupList.append(newOption);
		}
	} else if(splitHubJiraGroups != null){
		for (j = 0; j < splitHubJiraGroups.length; j++) {
			var newOption = AJS.$('<option>', {
			    value: splitHubJiraGroups[j],
			    text: splitHubJiraGroups[j],
			    selected: true
			});
			
			jiraGroupList.append(newOption);
		}
	}
	jiraGroupList.auiSelect2();
}

function fillInJiraProjects(jiraProjects){
	var mappingElement = AJS.$("#" + hubProjectMappingElement);
	var jiraProjectList = mappingElement.find("datalist[id='"+ jiraProjectListId +"']");
	if(jiraProjects != null && jiraProjects.length > 0){
		for (j = 0; j < jiraProjects.length; j++) {
			jiraProjectMap.set(String(jiraProjects[j].projectId), jiraProjects[j]);
			var newOption = AJS.$('<option>', {
			    value: jiraProjects[j].projectName,
			    projectKey: String(jiraProjects[j].projectId),
			    projectError: jiraProjects[j].projectError
			});
			
			jiraProjectList.append(newOption);
		}
	}
}

function fillInHubProjects(hubProjects){
	var mappingElement = AJS.$("#" + hubProjectMappingElement);
	
	var hubProjectList = mappingElement.find("datalist[id='"+ hubProjectListId +"']");
	if(hubProjects != null && hubProjects.length > 0){
		for (h = 0; h < hubProjects.length; h++) {
			hubProjectMap.set(hubProjects[h].projectUrl, hubProjects[h]);
			var newOption = AJS.$('<option>', {
			    value: hubProjects[h].projectName,
			    projectKey: hubProjects[h].projectUrl
			});
			hubProjectList.append(newOption);
		}
	}
}

function fillInMappings(storedMappings){
	var mappingContainer = AJS.$("#" + hubProjectMappingContainer);
	var mappingElements = mappingContainer.find("tr[name*='"+ hubProjectMappingElement + "']");
	// On loading the page, there should only be one original mapping element
	if(storedMappings != null && storedMappings.length > 0){
		for (i = 0; i < storedMappings.length; i++) {
			var newMappingElement = addNewMappingElement(hubProjectMappingElement);
			fillInMapping(newMappingElement, storedMappings[i]);
		}
	} else{
		addNewMappingElement(hubProjectMappingElement);
	}
}

function fillInMapping(mappingElement, storedMapping){
	var currentJiraProject = AJS.$(mappingElement).find("input[name*='jiraProject']");
	
	var storedJiraProject = storedMapping.jiraProject;
	var storedJiraProjectDisplayName = storedJiraProject.projectName;
	var storedJiraProjectValue = storedJiraProject.projectId;
	var storedJiraProjectError = storedJiraProject.projectError;
	
	currentJiraProject.val(storedJiraProjectDisplayName);
	currentJiraProject.attr("projectKey", storedJiraProjectValue);
	
	var currentHubProject = AJS.$(mappingElement).find("input[name*='hubProject']");
	
	var storedHubProject = storedMapping.hubProject;
	var storedHubProjectDisplayName = storedHubProject.projectName;
	var storedHubProjectValue = storedHubProject.projectUrl;
	
	currentHubProject.val(storedHubProjectDisplayName);
	currentHubProject.attr("projectKey", storedHubProjectValue);
}

function addNewMappingElement(fieldId){
	var elementToAdd = AJS.$("#" + fieldId).clone();
	mappingElementCounter = mappingElementCounter + 1;
	elementToAdd.attr("id", elementToAdd.attr("id") + mappingElementCounter);
	elementToAdd.appendTo("#" + hubProjectMappingContainer);
	
	removeClassFromField(elementToAdd, hiddenClass);
	
	removeMappingErrorStatus(elementToAdd);
	
	var currentJiraProject = AJS.$(elementToAdd).find("input[name*='jiraProject']");
	
	currentJiraProject.val("");
	currentJiraProject.attr("projectKey", "");
	if(currentJiraProject.hasClass('error')){
		currentJiraProject.removeClass('error');
	}
	var currentJiraProjectParent = currentJiraProject.parent();
	var currentJiraProjectError = currentJiraProjectParent.children("#"+jiraProjectErrorId);
	currentJiraProjectError.text("");
	if(currentJiraProjectError.hasClass('error')){
		currentJiraProjectError.removeClass('error');
	}
	
	var currentHubProject = AJS.$(elementToAdd).find("input[name*='hubProject']");
	
	currentHubProject.val("");
	currentHubProject.attr("projectKey", "");
	if(currentHubProject.hasClass('error')){
		currentHubProject.removeClass('error');
	}
	
	var mappingArea = AJS.$('#mappingArea')[0];
	if(mappingArea){
		AJS.$('#mappingArea').scrollTop(mappingArea.scrollHeight);
	}
	return elementToAdd;
}

function removeMappingElement(childElement){
	if(AJS.$("#" + hubProjectMappingContainer).find("tr[name*='"+ hubProjectMappingElement + "']").length > 1){
		AJS.$(childElement).closest("tr[name*='"+ hubProjectMappingElement + "']").remove();
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
    	   field.val(option.val());
    	   field.attr("projectKey", projectKey);
    	   
			var projectError = option.attr("projectError");
			
			var fieldParent = field.parent();
			var fieldError = fieldParent.children("#"+jiraProjectErrorId);
			if(projectError){
			fieldError.text(projectError);
				if(!fieldError.hasClass('error')){
					fieldError.addClass('error');
				}
				if(!field.hasClass('error')){
		   			field.addClass('error');
		   		}
			} else{
				fieldError.text("");
				if(field.hasClass('error')){
					field.removeClass('error');
				}
			}
			
    	   break;
    	}
    }
    if(!optionFound){
  	   field.attr("projectKey", "");
  	   if(!field.hasClass('error')){
  		   field.addClass('error');
  	   }
    }
}

function handleError(fieldId, configField, hideErrorValue, clearOldMessage) {
	if(configField){
		showError(fieldId, configField, clearOldMessage);
    } else if(hideErrorValue){
    	hideError(fieldId);
    } else{
    	showError(fieldId, "", true);
    }
}

function showError(fieldId, configField, clearOldMessage) {
	var newMessage = decodeURI(configField).trim();
	if(!clearOldMessage){
		var oldMessage = AJS.$("#" + fieldId).text().trim();
		if(oldMessage && oldMessage != newMessage){
			newMessage = oldMessage + ' .... ' + newMessage;
		}
	}
	AJS.$("#" + fieldId).text(newMessage);
  	removeClassFromFieldById(fieldId, hiddenClass);
}

function hideError(fieldId) {
	if(fieldId != errorMessageFieldId ){
	  AJS.$("#" + fieldId).text('');
  	  addClassToFieldById(fieldId, hiddenClass);
  	}
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

function toggleDisplay(icon, fieldId){
	var iconObject = AJS.$(icon);
	if(iconObject.hasClass('fa-angle-down')){
		removeClassFromField(icon, 'fa-angle-down');
		addClassToField(icon, 'fa-angle-right');
		
		addClassToFieldById(fieldId, hiddenClass);
	} else if(iconObject.hasClass('fa-angle-right')){
		removeClassFromField(icon, 'fa-angle-right');
		addClassToField(icon, 'fa-angle-down');
	
		removeClassFromFieldById(fieldId, hiddenClass);
	}
}

function addMappingErrorStatus(mappingElement){
	var mappingStatus = AJS.$(mappingElement).find("#" + hubMappingStatus);
	if(mappingStatus.find("i").length == 0){
		var newStatus = AJS.$('<i>', {
		});
		AJS.$(newStatus).addClass("error");
		AJS.$(newStatus).addClass("largeIcon");
		AJS.$(newStatus).addClass("fa");
		AJS.$(newStatus).addClass("fa-exclamation");

		newStatus.appendTo(mappingStatus);
	}
}

function removeMappingErrorStatus(mappingElement){
	var mappingStatus = AJS.$(mappingElement).find("#" + hubMappingStatus);
	if(mappingStatus.children().length > 0){
		AJS.$(mappingStatus).empty();
	}
}

function startProgressSpinner(spinnerId){
	 var spinner = AJS.$('#' + spinnerId);
	 
	 if(spinner.find("i").length == 0){
		var newSpinnerIcon = AJS.$('<i>', {
		});
		AJS.$(newSpinnerIcon).addClass("largeIcon");
		AJS.$(newSpinnerIcon).addClass("fa");
		AJS.$(newSpinnerIcon).addClass("fa-spinner");
		AJS.$(newSpinnerIcon).addClass("fa-spin");
		AJS.$(newSpinnerIcon).addClass("fa-fw");
		
		newSpinnerIcon.appendTo(spinner);
	 }
}

function stopProgressSpinner(spinnerId){
	var spinner = AJS.$('#'+spinnerId);
	if(spinner.children().length > 0){
		AJS.$(spinner).empty();
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
	
    $(document).ready(function() {
        console.log("DOM loaded")
        initTabs();
    });
	
})(AJS.$ || jQuery);
