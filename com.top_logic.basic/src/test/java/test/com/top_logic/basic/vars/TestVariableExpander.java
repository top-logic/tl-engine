/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.vars;

import junit.framework.TestCase;

import com.top_logic.basic.vars.VariableExpander;

/**
 * Test case for {@link VariableExpander}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestVariableExpander extends TestCase {

	public void testExpand() {
		VariableExpander expander = new VariableExpander();
		expander.addVariable("foo", "bar");
		assertEquals(null, expander.expand(null));
		assertEquals("bar", expander.expand("%foo%"));
		assertEquals("xybar", expander.expand("xy%foo%"));
		assertEquals("barxy", expander.expand("%foo%xy"));
		assertEquals("xybarxy", expander.expand("xy%foo%xy"));
		assertEquals("xy%xybarxy%xy", expander.expand("xy%xy%foo%xy%xy"));
		assertEquals("%xy%xybarxy%xy%", expander.expand("%xy%xy%foo%xy%xy%"));
		assertEquals("foo%foo", expander.expand("foo%foo"));
		assertEquals("foo%", expander.expand("foo%"));
		assertEquals("%foo", expander.expand("%foo"));
		assertEquals("foo", expander.expand("foo"));
	}

	public void testRecursion() {
		VariableExpander expander = new VariableExpander();
		expander.addVariable("a", "a%b%a");
		expander.addVariable("b", "b%c%b");
		expander.addVariable("c", "ccc");
		expander.resolveRecursion();
		assertEquals("xabcccbax", expander.expand("x%a%x"));
		assertEquals("abcccbaxbcccbxcccxbcccbxabcccba", expander.expand("%a%x%b%x%c%x%b%x%a%"));
	}

	public void testSelfRecursion() {
		VariableExpander expander = new VariableExpander();
		expander.addVariable("a", "a%b%a");
		expander.addVariable("b", "b%c%b");
		expander.addVariable("c", "c%a%c");
		try {
			expander.resolveRecursion();
			fail("Self recursion must be detected.");
		} catch (IllegalStateException ex) {
			assertEquals("Self recursive variable definition: a -> a%b%a -> abc%a%cba -> ...", ex.getMessage());
		}
	}

	public void testDerive() {
		VariableExpander expander = new VariableExpander();
		expander.addVariable("a", "a%b%a");
		expander.addVariable("b", "bb");
		expander.resolveRecursion();

		VariableExpander expander2 = expander.derive();
		expander2.addVariable("b", "xx");
		expander2.resolveRecursion();

		assertEquals("abba", expander2.expand("%a%"));
		assertEquals("xx", expander2.expand("%b%"));
	}
}
