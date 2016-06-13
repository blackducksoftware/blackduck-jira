package com.blackducksoftware.integration.jira.hub;

import java.text.ParseException;
import java.util.Date;

public class NotificationDateRange {
	private final Date startDate;
	private final Date endDate;

	public NotificationDateRange(Date startDate) throws ParseException {
		this.startDate = startDate;
		this.endDate = new Date();
	}

	public NotificationDateRange(Date startDate, Date endDate) throws ParseException {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NotificationDateRange other = (NotificationDateRange) obj;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NotificationDateRange [startDate=" + startDate + ", endDate=" + endDate + "]";
	}
}
