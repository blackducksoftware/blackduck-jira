package com.blackducksoftware.integration.jira.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PolicyRuleSerializable implements Serializable {

	private static final long serialVersionUID = -4449986971190551171L;

	@XmlElement
	private String name;

	@XmlElement
	private String description;

	@XmlElement
	private String policyUrl;

	@XmlElement
	private boolean checked;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getPolicyUrl() {
		return policyUrl;
	}

	public void setPolicyUrl(final String policyUrl) {
		this.policyUrl = policyUrl;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(final boolean checked) {
		this.checked = checked;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (checked ? 1231 : 1237);
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((policyUrl == null) ? 0 : policyUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PolicyRuleSerializable)) {
			return false;
		}
		final PolicyRuleSerializable other = (PolicyRuleSerializable) obj;
		if (checked != other.checked) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (policyUrl == null) {
			if (other.policyUrl != null) {
				return false;
			}
		} else if (!policyUrl.equals(other.policyUrl)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("PolicyRule [name=");
		builder.append(name);
		builder.append(", description=");
		builder.append(description);
		builder.append(", policyUrl=");
		builder.append(policyUrl);
		builder.append(", checked=");
		builder.append(checked);
		builder.append("]");
		return builder.toString();
	}

}
