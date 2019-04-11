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
let gotCreatorCandidates = false;
let gotJiraProjects = false;
let jiraProjectMap = new Map();

function fillInCreatorCandidates(creatorCandidates) {
    console.log("fillInCreatorCandidates()");
    for (let i = 0; i < creatorCandidates.length; i++) {
        console.log("Creator candidate: " + creatorCandidates[i]);
    }

    const creatorElement = AJS.$("#" + "creatorCell");
    const creatorCandidatesList = creatorElement.find("datalist[id='" + "creatorCandidates" + "']");
    console.log("fillInCreatorCandidates() List: " + creatorCandidatesList);
    if (creatorCandidatesList.length > 0) {
        console.log("fillInCreatorCandidates(): removing option");
        clearList(creatorCandidatesList[0]);
    }
    if (creatorCandidates != null && creatorCandidates.length > 0) {
        for (let j = 0; j < creatorCandidates.length; j++) {
            console.log("Adding creator candidate: " + creatorCandidates[j]);
//			jiraProjectMap.set(String(jiraProjects[j].projectId), jiraProjects[j]);
            let newOption = AJS.$('<option>', {
                value: creatorCandidates[j],
                id: creatorCandidates[j]
            });
            creatorCandidatesList.append(newOption);
        }
    }
}

function fillInJiraProjects(jiraProjects) {
    const mappingElement = AJS.$("#" + hubProjectMappingElement);
    const jiraProjectList = mappingElement.find("datalist[id='" + jiraProjectListId + "']");
    if (jiraProjects != null && jiraProjects.length > 0) {
        for (let j = 0; j < jiraProjects.length; j++) {
            jiraProjectMap.set(String(jiraProjects[j].projectId), jiraProjects[j]);
            let newOption = AJS.$('<option>', {
                value: jiraProjects[j].projectName,
                projectKey: String(jiraProjects[j].projectId),
                projectWorkflowStatus: String(jiraProjects[j].workflowStatus),
                projectError: jiraProjects[j].projectError
            });

            jiraProjectList.append(newOption);
        }
    }
}

function addNewMappingElement(fieldId) {
    const elementToAdd = AJS.$("#" + fieldId).clone();
    mappingElementCounter = mappingElementCounter + 1;
    elementToAdd.attr("id", elementToAdd.attr("id") + mappingElementCounter);
    elementToAdd.appendTo("#" + hubProjectMappingContainer);

    removeClassFromField(elementToAdd, hiddenClass);
    removeMappingErrorStatus(elementToAdd);

    const currentJiraProject = AJS.$(elementToAdd).find("input[name*='jiraProject']");

    currentJiraProject.val("");
    currentJiraProject.attr("projectKey", "");
    if (currentJiraProject.hasClass('error')) {
        currentJiraProject.removeClass('error');
    }
    const currentJiraProjectParent = currentJiraProject.parent();
    const currentJiraProjectError = currentJiraProjectParent.children("#" + jiraProjectErrorId);
    currentJiraProjectError.text("");
    if (currentJiraProjectError.hasClass('error')) {
        currentJiraProjectError.removeClass('error');
    }

    const currentHubProject = AJS.$(elementToAdd).find("input[name*='hubProject']");

    currentHubProject.val("");
    currentHubProject.attr("projectKey", "");
    if (currentHubProject.hasClass('error')) {
        currentHubProject.removeClass('error');
    }

    const mappingArea = AJS.$('#mappingArea')[0];
    if (mappingArea) {
        AJS.$('#mappingArea').scrollTop(mappingArea.scrollHeight);
    }
    return elementToAdd;
}

function removeMappingElement(childElement) {
    if (AJS.$("#" + hubProjectMappingContainer).find("tr[name*='" + hubProjectMappingElement + "']").length > 1) {
        AJS.$(childElement).closest("tr[name*='" + hubProjectMappingElement + "']").remove();
    }
}

function getJsonArrayFromMapping() {
    let jsonArray = [];
    const mappingContainer = AJS.$("#" + hubProjectMappingContainer);
    const mappingElements = mappingContainer.find("tr[name*='" + hubProjectMappingElement + "']");
    for (i = 1; i < mappingElements.length; i++) {
        let mappingElement = mappingElements[i];
        let currentJiraProject = AJS.$(mappingElement).find("input[name*='jiraProject']");

        let currentJiraProjectDisplayName = currentJiraProject.val();
        let currentJiraProjectValue = currentJiraProject.attr('projectKey');
        let currentJiraProjectError = currentJiraProject.attr('projectError');

        let currentConfiguredForVulnerabilities = AJS.$(mappingElement).find("input[name*='configuredForVulnerabilitiesOption']");
        let currentJiraProjectConfiguredForVulnerabilities = currentConfiguredForVulnerabilities[0].checked;

        let currentIssueCreator = AJS.$(mappingElement).find("input[name*='issueCreator']");
        let currentJiraProjectIssueCreator = currentIssueCreator.val();

        let currentHubProject = AJS.$(mappingElement).find("input[name*='hubProject']");

        let currentHubProjectDisplayName = currentHubProject.val();
        let currentHubProjectError = currentHubProject.attr('projectError');


        if (isNullOrWhitespace(currentJiraProjectValue) || currentJiraProjectError || currentHubProjectError) {
            addMappingErrorStatus(mappingElement);
        } else {
            removeMappingErrorStatus(mappingElement);
        }

        jsonArray.push({
            jiraProject: {
                [jiraProjectDisplayName]: currentJiraProjectDisplayName,
                [jiraProjectKey]: currentJiraProjectValue,
                [jiraProjectIssueCreatorDisplayName]: currentJiraProjectIssueCreator,
                [jiraProjectConfiguredForVulnerabilitiesDisplayName]: currentJiraProjectConfiguredForVulnerabilities
            },
            blackDuckProjectName: currentHubProjectDisplayName
        });
    }
    return jsonArray;
}

function getJsonArrayFromPolicyRules() {
    let jsonArray = [];
    const policyRuleContainer = AJS.$("#" + policyRuleTicketCreation);
    const policyRules = policyRuleContainer.find("input");
    for (i = 0; i < policyRules.length; i++) {
        let policyRule = AJS.$(policyRules[i]);
        let currentPolicyRuleUrl = policyRule.attr("policyurl");
        // Names and descriptions with chars like ", <, and > cause problems on save
        // and we don't actually use them; just omitting them for now
//		var currentPolicyRuleDescription = policyRule.attr("title");
//		var currentPolicyRuleName = policyRule.attr("name");
        let currentPolicyRuleChecked = policyRules[i].checked;
        console.log("Constructing rules jsonArray, but omitting name");
        jsonArray.push({
            [policyRuleName]: 'name omitted by hub-jira.js',
            [policyRuleUrl]: currentPolicyRuleUrl,
            [policyRuleDescription]: 'description omitted by hub-jira.js',
            [policyRuleChecked]: currentPolicyRuleChecked
        });
    }
    return jsonArray;
}

function getCommentOnIssueUpdatesChoice() {
    const commentOnIssueUpdatesChoiceElement = AJS.$("#" + "commentOnIssueUpdatesChoice");

    if (commentOnIssueUpdatesChoiceElement[0].checked) {
        return "true";
    } else {
        return "false";
    }
}

function setCommentOnIssueUpdatesChoice(commentOnIssueUpdatesChoice) {
    const commentOnIssueUpdatesChoiceElement = AJS.$("#" + "commentOnIssueUpdatesChoice");
    console.log("commentOnIssueUpdatesChoiceElement: " + commentOnIssueUpdatesChoiceElement);
    if (commentOnIssueUpdatesChoiceElement.length == 0) {
        console.log("*** commentOnIssueUpdatesChoiceElement is not ready");
    }

    if (commentOnIssueUpdatesChoice) {
        console.log("Setting commentOnIssueUpdatesChoiceElement to Yes");
        commentOnIssueUpdatesChoiceElement[0].checked = true;
    } else {
        console.log("Setting commentOnIssueUpdatesChoiceElement to No");
        commentOnIssueUpdatesChoiceElement[0].checked = false;
    }
}

function setWorkflowStatus(workflowStatusSpan, newStatus) {
    console.log("workflowStatusElement: " + workflowStatusSpan);
    while (workflowStatusSpan.firstChild) {
        workflowStatusSpan.removeChild(workflowStatusSpan.firstChild);
    }
    workflowStatusSpan.appendChild(document.createTextNode(newStatus));

    const parentDivClassList = workflowStatusSpan.parentElement.classList;
    if (newStatus === 'Enabled') {
        parentDivClassList.add('workflowStatusEnabled');
        parentDivClassList.remove('workflowStatusPartial');
        parentDivClassList.remove('workflowStatusDisabled');
    } else if (newStatus.includes('only')) {
        parentDivClassList.remove('workflowStatusEnabled');
        parentDivClassList.add('workflowStatusPartial');
        parentDivClassList.remove('workflowStatusDisabled');
    } else {
        parentDivClassList.remove('workflowStatusEnabled');
        parentDivClassList.remove('workflowStatusPartial');
        parentDivClassList.add('workflowStatusDisabled');
    }
}

function getProjectReviewerNotificationsChoice() {
    const projectReviewerNotificationsChoiceElement = AJS.$("#" + "projectReviewerNotificationsChoice");

    return projectReviewerNotificationsChoiceElement[0].checked.toString();
}

function setProjectReviewerNotificationsChoice(projectReviewerNotificationsChoice) {
    const projectReviewerNotificationsChoiceElement = AJS.$("#" + "projectReviewerNotificationsChoice");
    console.log("projectReviewerNotificationsChoiceElement: " + projectReviewerNotificationsChoiceElement);
    if (projectReviewerNotificationsChoiceElement.length == 0) {
        console.log("*** projectReviewerNotificationsChoiceElement is not ready");
    }

    if (projectReviewerNotificationsChoice) {
        console.log("Setting projectReviewerNotificationsChoiceElement to Yes");
        projectReviewerNotificationsChoiceElement[0].checked = true;
    } else {
        console.log("Setting projectReviewerNotificationsChoiceElement to No");
        projectReviewerNotificationsChoiceElement[0].checked = false;
    }
}

function onCreatorInputChange(inputField) {
    console.log("onCreatorInputChange()");
    const field = AJS.$(inputField);
    const datalist = inputField.list;
    const options = datalist.options;

    let optionFound = false;
    for (let i = 0; i < options.length; i++) {
        if (options[i].value == inputField.value) {
            optionFound = true;
            let option = AJS.$(options[i]);

            let username = option.attr("id");
            console.log("onCreatorInputChange(): username: " + username);
        }
    }
}

function onMappingInputChange(inputField) {
    const field = AJS.$(inputField);
    const datalist = inputField.list;
    const options = datalist.options;

    let optionFound = false;
    for (let i = 0; i < options.length; i++) {
        if (options[i].value == inputField.value) {
            optionFound = true;
            let option = AJS.$(options[i]);

            let projectKey = option.attr("projectKey");
            field.val(option.val());
            field.attr("projectKey", projectKey);

            let projectError = option.attr("projectError");

            let fieldParent = field.parent();
            let fieldError = fieldParent.children("#" + jiraProjectErrorId);
            if (projectError) {
                fieldError.text(projectError);
                if (!fieldError.hasClass('error')) {
                    fieldError.addClass('error');
                }
                if (!field.hasClass('error')) {
                    field.addClass('error');
                }
            } else {
                fieldError.text("");
                if (field.hasClass('error')) {
                    field.removeClass('error');
                }
            }

            const mappingRow = inputField.parentElement.parentElement;
            const workflowStatusSpan = AJS.$(mappingRow).find("span[name='workflowStatus']")[0];
            setWorkflowStatus(workflowStatusSpan, option.attr("projectWorkflowStatus"));

            break;
        }
    }
    if (!optionFound) {
        field.attr("projectKey", "");
        if (!field.hasClass('error')) {
            field.addClass('error');
        }
    }
}

function onSelectAllCheckedOrUnchecked(selectAllCheckbox) {
    const boxIsChecked = selectAllCheckbox.checked;

    const mappingTable = AJS.$("#hubProjectMappingTable");
    const projectMappingCheckboxes = AJS.$(mappingTable).find("input[name*='configuredForVulnerabilitiesOption']")
    for (let i = 0; i < projectMappingCheckboxes.length; i++) {
        const mappingCheckbox = projectMappingCheckboxes[i];
        mappingCheckbox.checked = boxIsChecked;
    }
}
