/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.build.doclet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.build.doclet.CamelCaseIterator;
import com.top_logic.build.doclet.TLDoclet;

/**
 * Testing camel case detection in {@link TLDoclet}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestCamelCaseWords extends TestCase {

	/**
	 * Test for standard cases.
	 */
	public void testStandard() {
		assertCamelCase("This contains invalid camelCase content.", "camelCase");
		assertCamelCase("This contains invalid camelCase content and mOre.", "camelCase", "mOre");
		assertCamelCase("This contains invalid camelCase\" content and \"mOre.", "camelCase", "mOre");
		assertCamelCase("This contains invalid \"camelCase\" content and \"mOre\".");
	}

	/**
	 * Test for complex cases.
	 */
	public void testCameCaseInComplexWords() {
		assertCamelCase("Refer to http://www.top-logic.com/?foo=camelCase and others.", "camelCase");
		assertCamelCase("Refer to http://www.top-logic.com/?foo=camelCase", "camelCase");
		// Quoted
		assertCamelCase("Refer to \"http://www.top-logic.com/?foo=camelCase\" and others.");
		assertCamelCase("Refer to \"http://www.top-logic.com/?foo=camelCase\"");
	}

	/**
	 * Test for mistyped link.
	 */
	public void testMissingLink() {
		assertCamelCase("See also #otherMethod().", "otherMethod");
	}

	private void assertCamelCase(String input, String... expected) {
		assertEquals(Arrays.asList(expected), findCamelCaseWords(input));
	}

	private List<String> findCamelCaseWords(String input) {
		Iterator<String> it = CamelCaseIterator.newIterator(input);
		if (it.hasNext()) {
			ArrayList<String> result = new ArrayList<String>();
			do {
				result.add(it.next());
			} while (it.hasNext());
			return result;
		}
		return Collections.emptyList();
	}
}

