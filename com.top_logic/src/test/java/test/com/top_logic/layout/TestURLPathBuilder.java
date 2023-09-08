/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import junit.framework.TestCase;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.URLPathBuilder;
import com.top_logic.layout.URLPathParser;

/**
 * Tests for {@link URLPathBuilder}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestURLPathBuilder extends TestCase {

	public void testEscapeChars() {
		URLPathBuilder builder = URLPathBuilder.newEmptyBuilder();

		builder.addResource("äöß");
		builder.addResource("abc");
		builder.addResource(" + ");
		builder.appendParameter("param1", " + ");
		assertEquals(URLPathParser.PATH_SEPARATOR
			+ escape("ä") + escape("ö") + escape("ß")
			+ URLPathParser.PATH_SEPARATOR
			+ "abc"
			+ URLPathParser.PATH_SEPARATOR
			+ "%20+%20?param1=+%2B+", builder.getURL());
	}

	private String escape(String cha) {
		StringBuilder result = new StringBuilder();
		byte[] bytes = cha.getBytes(StringServices.CHARSET_UTF_8);
		for (byte b : bytes) {
			result.append("%");
			result.append(Integer.toHexString(Byte.toUnsignedInt(b)).toUpperCase());
		}
		return result.toString();
	}
}

