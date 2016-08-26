package com.blackducksoftware.integration.jira.task;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.blackducksoftware.integration.jira.common.HubJiraConstants;

public class JiraSettingsService {

	public static final DateTimeFormatter ERROR_TIME_FORMAT = new DateTimeFormatterBuilder().appendDayOfMonth(2)
			.appendLiteral('/').appendMonthOfYear(2).appendLiteral('/').appendYear(4, 4).appendLiteral(' ')
			.appendHourOfHalfday(1).appendLiteral(':').appendMinuteOfHour(1).appendHalfdayOfDayText().toFormatter();

	private final PluginSettings settings;

	public JiraSettingsService(final PluginSettings settings) {
		this.settings = settings;
	};

	public void addHubError(final Throwable throwable, final String methodAttempt) {
		addHubError(throwable, null, null, null, null, methodAttempt);
	}

	public void addHubError(final String errorMessage, final String methodAttempt) {
		addHubError(errorMessage, null, null, null, null, methodAttempt);
	}

	public void addHubError(final Throwable throwable, final String hubProject, final String hubProjectVersion,
			final String jiraProject, final String jiraUser, final String methodAttempt) {
		final StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		addHubError(sw.toString(), hubProject, hubProjectVersion, jiraProject, jiraUser, methodAttempt);
	}

	public void addHubError(final String errorMessage, final String hubProject, final String hubProjectVersion,
			final String jiraProject, final String jiraUser, final String methodAttempt) {
		final Object errorMapObject = settings.get(HubJiraConstants.HUB_JIRA_ERROR);
		final HashMap<String, String> errorMap;
		if(errorMapObject == null){
			errorMap = new HashMap<String, String>();
		} else {
			errorMap = (HashMap<String, String>) errorMapObject;
		}
		final StringBuilder suffixBuilder = new StringBuilder();
		if (StringUtils.isNotBlank(hubProject)) {
			suffixBuilder.append("Hub Project : ");
			suffixBuilder.append(hubProject);
			suffixBuilder.append(" / ");
		}
		if (StringUtils.isNotBlank(hubProjectVersion)) {
			suffixBuilder.append("Version : ");
			suffixBuilder.append(hubProjectVersion);
			suffixBuilder.append(" / ");
		}
		if (StringUtils.isNotBlank(jiraProject)) {
			suffixBuilder.append("Jira Project : ");
			suffixBuilder.append(jiraProject);
			suffixBuilder.append(" / ");
		}
		if (StringUtils.isNotBlank(jiraUser)) {
			suffixBuilder.append("Jira User : ");
			suffixBuilder.append(jiraUser);
			suffixBuilder.append(" / ");
		}
		suffixBuilder.append("Method : ");
		suffixBuilder.append(methodAttempt);

		final StringBuilder finalErrorBuilder = new StringBuilder();
		finalErrorBuilder.append(errorMessage.trim());
		finalErrorBuilder.append("\n");
		finalErrorBuilder.append(suffixBuilder.toString());

		errorMap.put(finalErrorBuilder.toString(), DateTime.now().toString(ERROR_TIME_FORMAT));
		settings.put(HubJiraConstants.HUB_JIRA_ERROR, errorMap);
	}
}
