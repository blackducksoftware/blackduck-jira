package com.blackducksoftware.integration.jira.workflow.notification.converter;

import com.blackducksoftware.integration.jira.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.workflow.notification.NotificationContentDetail;

public abstract class NotificationTypeParser {

    public abstract BlackDuckIssueModel parseNotification(NotificationContentDetail notificationContentDetail);
}
