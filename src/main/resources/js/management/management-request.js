/*
 * Black Duck JIRA Plugin
 *
 * Copyright (C) 2019 Synopsys, Inc.
 * https://www.synopsys.com/
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
function closeOldIssues() {
    var oldUrl = $("jiraCleanOldIssues").val();
    AJS.$.ajax({
        url: createRequestPath('config/blackduck'),
        dataType: "json",
        success: function (config) {
            console.log("Successful get of hub details for " + config.hubUrl);

            updateValue("hubServerUrl", config.hubUrl);
            updateValue("hubTimeout", config.timeout);
            updateValue("hubTrustCert", config.trustCert);
            updateValue("bdApiToken", config.apiToken);
            updateValue("proxyHost", config.hubProxyHost);
            updateValue("proxyPort", config.hubProxyPort);
            updateValue("proxyUsername", config.hubProxyUser);
            updateValue("proxyPassword", config.hubProxyPassword);

            checkProxyConfig();

            handleError(errorMessageFieldId, config.errorMessage, true, true);
            handleErrorHubDetails('hubServerUrlErrorRow', 'hubServerUrlError', config.hubUrlError);
            handleErrorHubDetails('hubTimeoutErrorRow', 'hubTimeoutError', config.timeoutError);
            handleErrorHubDetails('hubTrustCertErrorRow', 'hubTrustCertError', config.trustCertError);
            handleErrorHubDetails('bdApiTokenErrorRow', 'bdApiTokenError', config.apiTokenError);
            handleErrorHubDetails('proxyHostErrorRow', 'proxyHostError', config.hubProxyHostError);
            handleErrorHubDetails('proxyPortErrorRow', 'proxyPortError', config.hubProxyPortError);
            handleErrorHubDetails('proxyUsernameErrorRow', 'proxyUsernameError', config.hubProxyUserError);
            handleErrorHubDetails('proxyPasswordErrorRow', 'proxyPasswordError', config.hubProxyPasswordError);

        }, error: function (response) {
            console.log("putConfig(): " + response.responseText);
            alert("There was an error loading the configuration.");
            handleDataRetrievalError(response, 'configurationError', "There was a problem retrieving the configuration.", "Configuration Error");
        },
        complete: function (jqXHR, textStatus) {
            stopProgressSpinner('hubDetailsProgressSpinner');
            console.log("Completed get of hub details: " + textStatus);
        }
    });
}
