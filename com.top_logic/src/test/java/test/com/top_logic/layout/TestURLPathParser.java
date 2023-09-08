/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.URLPathParser;

/**
 * Test case for {@link URLPathParser}
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestURLPathParser extends TestCase {

	public void testParse() {
		assertElements(list("foo"), URLPathParser.createURLPathParser("/foo"));
		assertElements(list("foo", "bar"), URLPathParser.createURLPathParser("/foo/bar"));
		assertElements(list("foo", "bar", ""), URLPathParser.createURLPathParser("/foo/bar/"));
	}

	public void testParseEmpty() {
		assertElements(list(), URLPathParser.createURLPathParser(""));
	}

	public void testParseNull() {
		assertElements(list(), URLPathParser.createURLPathParser(null));
	}

	public void testParseRoot() {
		assertElements(list(""), URLPathParser.createURLPathParser("/"));
	}

	private void assertElements(List<String> elements, URLPathParser parser) {
		assertEquals(elements, elements(parser));
	}

	private List<String> elements(URLPathParser parser) {
		ArrayList<String> result = new ArrayList<>();
		while (!parser.isEmpty()) {
			result.add(parser.removeResource());
		}
		return result;
	}

}
