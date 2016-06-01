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

var spinning = false;

function updateConfig() {
		putConfig(AJS.contextPath() + '/rest/hub-integration/1.0/', 'Save successful.', 'The configuration is not valid.');
	}

function testConnection() {
		putConfig(AJS.contextPath() + '/rest/hub-integration/1.0/testConnection', 'Test Connection successful.', 'Test Connection failed.');
	}

function putConfig(restUrl, successMessage, failureMessage) {
	  AJS.$.ajax({
		    url: restUrl,
		    type: "PUT",
		    dataType: "json",
		    contentType: "application/json",
		    data: '{ "hubUrl": "' + encodeURI(AJS.$("#hubServerUrl").val())
		    + '", "timeout": "' + encodeURI(AJS.$("#hubTimeout").val())
		    + '", "username": "' + encodeURI(AJS.$("#hubUsername").val())
		    + '", "password": "' + encodeURI(AJS.$("#hubPassword").val())
		    + '", "hubProxyHost": "' + encodeURI(AJS.$("#proxyHost").val())
		    + '", "hubProxyPort": "' + encodeURI(AJS.$("#proxyPort").val())
		    + '", "hubNoProxyHosts": "' + encodeURI(AJS.$("#noProxyHost").val())
		    + '", "hubProxyUser": "' + encodeURI(AJS.$("#proxyUsername").val())
		    + '", "hubProxyPassword": "' + encodeURI(AJS.$("#proxyPassword").val())
		    + '"}',
		    processData: false,
		    success: function() {
		    	hideError('hubServerUrlError');
		    	hideError('hubTimeoutError');
		    	hideError('hubUsernameError');
		    	hideError('hubPasswordError');
		    	hideError('proxyHostError');
		    	hideError('proxyPortError');
			    hideError('proxyUsernameError');
			    hideError('proxyPasswordError');
			    hideError('noProxyHostError');
			    hideError('testConnectionError');
			      
			    showStatusMessage(successStatus, 'Success!', successMessage);
			    stopProgressSpinner();
		    },
		    error: function(response){
		    	var config = JSON.parse(response.responseText);
		    	handleError('hubServerUrlError', config.hubUrlError);
			    handleError('hubTimeoutError', config.timeoutError);
			    handleError('hubUsernameError', config.usernameError);
			    handleError('hubPasswordError', config.passwordError);
			    handleError('proxyHostError', config.hubProxyHostError);
			    handleError('proxyPortError', config.hubProxyPortError);
			    handleError('proxyUsernameError', config.hubProxyUserError);
			    handleError('proxyPasswordError', config.hubProxyPasswordError);
			    handleError('noProxyHostError', config.hubNoProxyHostsError);
			    handleError('testConnectionError', config.testConnectionError);
			    
			    showStatusMessage(errorStatus, 'ERROR!', failureMessage);
			    stopProgressSpinner();
		    }
		  });
}

function populateForm() {
	  AJS.$.ajax({
	    url: AJS.contextPath() + "/rest/hub-integration/1.0/",
	    dataType: "json",
	    success: function(config) {
	      updateValue("hubServerUrl", config.hubUrl);
	      updateValue("hubTimeout", config.timeout);
	      updateValue("hubUsername", config.username);
	      updateValue("hubPassword", config.password);
	      updateValue("proxyHost", config.hubProxyHost);
	      updateValue("proxyPort", config.hubProxyPort);
	      updateValue("proxyUsername", config.hubProxyUser);
	      updateValue("proxyPassword", config.hubProxyPassword);
	      updateValue("noProxyHost", config.hubNoProxyHosts);
	      
	      handleError('hubServerUrlError', config.hubUrlError);
	      handleError('hubTimeoutError', config.timeoutError);
	      handleError('hubUsernameError', config.usernameError);
	      handleError('hubPasswordError', config.passwordError);
	      handleError('proxyHostError', config.hubProxyHostError);
	      handleError('proxyPortError', config.hubProxyPortError);
	      handleError('proxyUsernameError', config.hubProxyUserError);
	      handleError('proxyPasswordError', config.hubProxyPasswordError);
	      handleError('noProxyHostError', config.hubNoProxyHostsError);
	    }
	  });
	}

function updateValue(fieldId, configField) {
	if(configField){
		 AJS.$("#" + fieldId).val(decodeURI(configField));
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
  	  removeClassFromField(fieldId, 'hidden');
}

function hideError(fieldId) {
	  AJS.$("#" + fieldId).text('');
  	  addClassToField(fieldId, 'hidden');
}

function showStatusMessage(status, statusTitle, message) {
	resetStatusMessage();
	if(status == errorStatus){
		addClassToField(statusMessageFieldId, 'error');
		addClassToField(statusMessageTitleId, 'icon-error');
	} else if (status == successStatus){
		addClassToField(statusMessageFieldId, 'success');
		addClassToField(statusMessageTitleId, 'icon-success');
	}
	AJS.$("#" + statusMessageTitleTextId).text(statusTitle);
	AJS.$("#" + statusMessageTextId).text(message);
	removeClassFromField(statusMessageFieldId, 'hidden');
}

function resetStatusMessage() {
	removeClassFromField(statusMessageFieldId,'error');
	removeClassFromField(statusMessageFieldId,'success');
	removeClassFromField(statusMessageTitleId,'icon-error');
	removeClassFromField(statusMessageTitleId,'icon-success');
	AJS.$("#" + statusMessageTitleTextId).text('');
	AJS.$("#" + statusMessageTextId).text('');
	addClassToField(statusMessageFieldId, 'hidden');
}

function addClassToField(fieldId, cssClass){
	if(!AJS.$("#" + fieldId).hasClass(cssClass)){
		AJS.$("#" + fieldId).addClass(cssClass);
	}
}

function removeClassFromField(fieldId, cssClass){
	if(AJS.$("#" + fieldId).hasClass(cssClass)){
		AJS.$("#" + fieldId).removeClass(cssClass);
	}
}

function startProgressSpinner(){
	 if (!spinning) {
		 var spinner = AJS.$('.spinner');
		 AJS.$('.spinner').spin('large');
		 spinning = true;
	 }
}

function stopProgressSpinner(){
	 if (spinning) {
		 AJS.$('.spinner').spinStop();
         spinning = false;
	 }
}

(function ($) {
	populateForm();
})(AJS.$ || jQuery);

