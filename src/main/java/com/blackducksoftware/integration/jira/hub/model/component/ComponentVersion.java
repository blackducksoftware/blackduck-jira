package com.blackducksoftware.integration.jira.hub.model.component;

import java.util.Date;

import com.blackducksoftware.integration.hub.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ComponentVersion extends HubItem {
	public ComponentVersion(final MetaInformation meta) {
		super(meta);
	}

	private String versionName;
	private Date releasedOn;

	// License goes here

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(final String versionName) {
		this.versionName = versionName;
	}

	public Date getReleasedOn() {
		return releasedOn;
	}

	public void setReleasedOn(final Date releasedOn) {
		this.releasedOn = releasedOn;
	}

	@Override
	public String toString() {
		return "ComponentVersion [versionName=" + versionName + ", releasedOn=" + releasedOn + "]";
	}

}
