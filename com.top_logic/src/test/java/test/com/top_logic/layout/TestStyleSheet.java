/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import static com.top_logic.layout.Flavor.*;
import static com.top_logic.layout.StyleSheet.*;
import static java.util.Arrays.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.layout.Style;
import com.top_logic.layout.StyleSheet;

/**
 * Test case for {@link StyleSheet}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestStyleSheet extends TestCase {

	/**
	 * Test flavor aggretation.
	 */
	public void testMatch() {
		Style<String> style = new StyleSheet<>(
			asList(
				rule(MANDATORY_DISABLED, "darkyellow"),
				rule(DISABLED, "grey"),
				rule(MANDATORY, "yellow")
			));
		
		assertNull(style.getValue(DEFAULT));
		assertEquals("grey", style.getValue(DISABLED));
		assertEquals("grey", style.getValue(IMMUTABLE));
		assertEquals("yellow", style.getValue(MANDATORY));
		assertEquals("yellow", style.getValue(aggregate(MANDATORY, EXPANDED)));
		assertEquals("darkyellow", style.getValue(MANDATORY_DISABLED));
		assertEquals("darkyellow", style.getValue(MANDATORY_IMMUTABLE));
	}
	
	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return new TestSuite(TestStyleSheet.class);
	}
	
}
