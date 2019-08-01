package com.blackducksoftware.integration.jira.workflow.notification.converter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.blackducksoftware.integration.jira.issue.model.BlackDuckIssueModel;
import com.blackducksoftware.integration.jira.workflow.notification.NotificationContentDetail;
import com.blackducksoftware.integration.jira.workflow.notification.NotificationDetailResult;
import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class NotificationConverter {

    public List<BlackDuckIssueModel> convertFromNotificationToModels(NotificationDetailResult notificationDetailResult) {
        final Map<NotificationType, NotificationTypeParser> parserMapping = createParserMapping();
        final NotificationType type = notificationDetailResult.getType();
        List<BlackDuckIssueModel> convertedIssues = new LinkedList<>();
        for (NotificationContentDetail notificationContentDetail : notificationDetailResult.getNotificationContentDetails()) {
            final BlackDuckIssueModel blackDuckIssueModel = parserMapping.get(type).parseNotification(notificationContentDetail);
            convertedIssues.add(blackDuckIssueModel);
        }

        return convertedIssues;
    }

    public <N extends NotificationTypeParser> Map<NotificationType, N> createParserMapping() {
        Map<NotificationType, N> parserMapping = new HashMap<>();

        return parserMapping;
    }
}
