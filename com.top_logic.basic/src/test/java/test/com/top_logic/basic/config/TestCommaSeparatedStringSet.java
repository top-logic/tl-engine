/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Test for {@link CommaSeparatedStringSet}.
 */
@SuppressWarnings("javadoc")
public class TestCommaSeparatedStringSet extends TestCase {

	private static final String PROPERTY = "attributes";

	public void testSpecificationIndependentOfIterationOrder() {
		assertEquals("dest,metaAttribute,source", spec("source", "dest", "metaAttribute"));
		assertEquals("dest,metaAttribute,source", spec("dest", "source", "metaAttribute"));
		assertEquals("dest,metaAttribute,source", spec("metaAttribute", "source", "dest"));
		assertEquals("dest,source", CommaSeparatedStringSet.INSTANCE.getSpecification(
			new HashSet<>(Arrays.asList("source", "dest"))));
	}

	public void testEmptySpecification() {
		assertEquals("", CommaSeparatedStringSet.INSTANCE.getSpecification(new HashSet<>()));
		assertEquals("", CommaSeparatedStringSet.INSTANCE.getSpecification(null));
	}

	public void testRoundTrip() throws ConfigurationException {
		Set<String> value = linkedSet("source", "dest", "metaAttribute");
		String specification = CommaSeparatedStringSet.INSTANCE.getSpecification(value);
		assertEquals(value, CommaSeparatedStringSet.INSTANCE.getValue(PROPERTY, specification));
		assertEquals(specification, CommaSeparatedStringSet.INSTANCE.getSpecification(
			CommaSeparatedStringSet.INSTANCE.getValue(PROPERTY, specification)));
	}

	public void testParseUnsorted() throws ConfigurationException {
		assertEquals(linkedSet("source", "dest"), CommaSeparatedStringSet.INSTANCE.getValue(PROPERTY, "source,dest"));
		assertEquals(linkedSet("source", "dest"), CommaSeparatedStringSet.INSTANCE.getValue(PROPERTY, "dest,source"));
	}

	private static String spec(String... values) {
		return CommaSeparatedStringSet.INSTANCE.getSpecification(linkedSet(values));
	}

	private static Set<String> linkedSet(String... values) {
		return new LinkedHashSet<>(Arrays.asList(values));
	}

}
