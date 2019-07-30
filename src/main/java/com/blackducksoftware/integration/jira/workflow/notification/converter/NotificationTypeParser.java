package com.blackducksoftware.integration.jira.workflow.notification.converter;

import com.blackducksoftware.integration.jira.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.workflow.notification.NotificationDetailResult;

public abstract class NotificationTypeParser {

    public abstract BlackDuckIssueModel parseNotification(NotificationDetailResult detailResult);
}
