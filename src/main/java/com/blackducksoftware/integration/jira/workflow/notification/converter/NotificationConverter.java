package com.blackducksoftware.integration.jira.workflow.notification.converter;

import java.util.HashMap;
import java.util.Map;

import com.blackducksoftware.integration.jira.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.workflow.notification.NotificationDetailResult;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class NotificationConverter {

    public BlackDuckIssueModel convertFromNotificationToModel(NotificationDetailResult notificationDetailResult) {
        final Map<NotificationType, NotificationTypeParser> parserMapping = createParserMapping();
        final NotificationType type = notificationDetailResult.getType();
        final BlackDuckIssueModel blackDuckIssueModel = parserMapping.get(type).parseNotification(notificationDetailResult);
    }

    public <N extends NotificationTypeParser> Map<NotificationType, N> createParserMapping() {
        Map<NotificationType, N> parserMapping = new HashMap<>();

        return parserMapping;
    }
}
