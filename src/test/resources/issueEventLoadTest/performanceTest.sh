#!/bin/bash
USER=admin:admin
SERVER=http://localhost:2990/jira
PROJECT_KEY=TEST
PROJECT_URL="$SERVER/rest/api/2/project"
ISSUE_URL="$SERVER/rest/api/2/issue"
TEMP_FILE=temp_data.json
MAX_ISSUES=1000

run() {
    create_project
    execute_function_n_times create_issue
    execute_function_n_times edit_issue
    execute_function_n_times delete_issue
    delete_project
}

execute_function_n_times() {
    for index in `seq 1 $MAX_ISSUES`;
    do
        $1 $index
    done
}

create_project() {
    echo "{ \"key\": \"$PROJECT_KEY\",
    \"name\": \"$PROJECT_KEY\",
    \"projectTypeKey\": \"business\",
    \"projectTemplateKey\": \"com.atlassian.jira-core-project-templates:jira-core-project-management\",
    \"description\": \"Auto Generated Test Project\",
    \"lead\": \"admin\",
    \"url\": \"http://www.blackducksoftware.com\",
    \"assigneeType\": \"PROJECT_LEAD\" }" > $TEMP_FILE

    json_data=$(cat $TEMP_FILE)
    curl -D- -u $USER -X POST --data "${json_data}" -H "Content-Type: application/json" $PROJECT_URL
}

delete_project() {
    curl -D- -u $USER -X DELETE --data "${json_data}" -H "Content-Type: application/json" $PROJECT_URL/$PROJECT_KEY
}

create_issue() {
    echo "{ \"fields\": {
    \"project\": { \"key\": \"TEST\" },
    \"summary\": \"Issue $1\",
    \"description\": \"Auto generated issue\",
    \"issuetype\": { \"name\": \"Task\" } } }" > $TEMP_FILE
    json_data=$(cat $TEMP_FILE);
    curl -D- -u $USER -X POST --data "${json_data}" -H "Content-Type: application/json" $ISSUE_URL
}

edit_issue() {
    echo "{ \"fields\": {
    \"summary\": \"Issue $1 Updated\",
    \"assignee\": { \"name\": \"admin\" } } }" > $TEMP_FILE
    json_data=$(cat $TEMP_FILE);
    curl -D- -u $USER -X PUT --data "${json_data}" -H "Content-Type: application/json" $ISSUE_URL/$PROJECT_KEY-$1
}

delete_issue() {
    curl -D- -u $USER -X DELETE -H "Content-Type: application/json" $ISSUE_URL/$PROJECT_KEY-$1
}

run
