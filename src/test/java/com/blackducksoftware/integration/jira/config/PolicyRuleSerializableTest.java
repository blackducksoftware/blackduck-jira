package com.blackducksoftware.integration.jira.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PolicyRuleSerializableTest {

	@Test
	public void testPolicyRuleSerializable() {
		final String name1 = "name1";
		final String description1 = "description1";
		final String policyUrl1 = "policyUrl1";
		final Boolean checked1 = true;

		final String name2 = "name2";
		final String description2 = "description2";
		final String policyUrl2 = "policyUrl2";
		final Boolean checked2 = false;

		final PolicyRuleSerializable item1 = new PolicyRuleSerializable();
		item1.setName(name1);
		item1.setDescription(description1);
		item1.setPolicyUrl(policyUrl1);
		item1.setChecked(checked1);
		final PolicyRuleSerializable item2 = new PolicyRuleSerializable();
		item2.setName(name2);
		item2.setDescription(description2);
		item2.setPolicyUrl(policyUrl2);
		item2.setChecked(checked2);
		final PolicyRuleSerializable item3 = new PolicyRuleSerializable();
		item3.setName(name1);
		item3.setDescription(description1);
		item3.setPolicyUrl(policyUrl1);
		item3.setChecked(checked1);

		assertEquals(name1, item1.getName());
		assertEquals(description1, item1.getDescription());
		assertEquals(policyUrl1, item1.getPolicyUrl());
		assertEquals(checked1, item1.isChecked());

		assertEquals(name2, item2.getName());
		assertEquals(description2, item2.getDescription());
		assertEquals(policyUrl2, item2.getPolicyUrl());
		assertEquals(checked2, item2.isChecked());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("PolicyRuleSerializable [name=");
		builder.append(item1.getName());
		builder.append(", description=");
		builder.append(item1.getDescription());
		builder.append(", policyUrl=");
		builder.append(item1.getPolicyUrl());
		builder.append(", checked=");
		builder.append(item1.isChecked());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

}
