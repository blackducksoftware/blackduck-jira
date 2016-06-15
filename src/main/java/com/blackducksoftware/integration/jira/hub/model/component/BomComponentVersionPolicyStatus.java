package com.blackducksoftware.integration.jira.hub.model.component;

import com.blackducksoftware.integration.hub.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.policy.api.PolicyStatusEnum;

public class BomComponentVersionPolicyStatus extends HubItem {

	private PolicyStatusEnum approvalStatus;

	public BomComponentVersionPolicyStatus(final MetaInformation meta) {
		super(meta);
	}

	public PolicyStatusEnum getApprovalStatus() {
		return approvalStatus;
	}

	public void setApprovalStatus(final PolicyStatusEnum approvalStatus) {
		this.approvalStatus = approvalStatus;
	}

}
