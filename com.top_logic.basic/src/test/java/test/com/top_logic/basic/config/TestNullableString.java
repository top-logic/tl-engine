/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Test case for {@link Nullable} properties.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestNullableString extends TestCase {

	public interface A extends ConfigurationItem {

		String getNonNull();

		void setNonNull(String value);

		@Nullable
		String getNullable();

		void setNullable(String value);
	}

	private static final String NS =
		"xmlns:config='" + ConfigurationSchemaConstants.CONFIG_NS + "' " +
			"config:interface=" + "'" + A.class.getName() + "'";

	public void testCreate() {
		A a1 = TypedConfiguration.newConfigItem(A.class);
		checkEmpty(a1);
	}

	public void testParseMissing() throws ConfigurationException {
		A a1 = parseA("<config " + NS + "/>");
		checkEmpty(a1);
	}

	public void testParseEmpty() throws ConfigurationException {
		A a1 = parseA("<config " + NS + " non-null='' nullable=''/>");
		checkEmpty(a1);
	}

	public void testParseNonEmpty() throws ConfigurationException {
		A a1 = parseA("<config " + NS + " non-null='foo' nullable='foo'/>");
		assertEquals("foo", a1.getNullable());
		assertEquals("foo", a1.getNonNull());
	}

	public void testSetEmpty() throws ConfigurationException {
		A a1 = parseA("<config " + NS + " non-null='foo' nullable='foo'/>");
		a1.setNonNull("");
		a1.setNullable("");

		checkEmpty(a1);
	}

	public void testSetNull() throws ConfigurationException {
		A a1 = parseA("<config " + NS + " non-null='foo' nullable='foo'/>");
		a1.setNonNull(null);
		a1.setNullable(null);

		checkEmpty(a1);
	}

	private A parseA(String xml) throws ConfigurationException {
		return (A) TypedConfiguration.parse(CharacterContents.newContent(xml));
	}

	private void checkEmpty(A a1) {
		assertNull(a1.getNullable());
		assertEquals("", a1.getNonNull());
	}

}
