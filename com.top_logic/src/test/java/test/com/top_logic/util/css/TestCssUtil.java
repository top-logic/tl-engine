/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.css;

import static com.top_logic.util.css.CssUtil.*;

import junit.framework.TestCase;

import com.top_logic.util.css.CssUtil;


/**
 * Test case for {@link CssUtil}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestCssUtil extends TestCase {

	public void testJoinTwoClasses() {
		assertEquals("foo bar", joinCssClasses("foo", "bar"));
		assertEquals("foo", joinCssClasses("foo", null));
		assertEquals("foo", joinCssClasses(null, "foo"));
		assertNull(joinCssClasses(null, null));
	}

	public void testJoinTwoClassesUnique() {
		assertEquals("foo bar", joinCssClassesUnique("foo", "bar"));
		assertEquals("foo", joinCssClassesUnique("foo", null));
		assertEquals("foo", joinCssClassesUnique(null, "foo"));
		assertNull(joinCssClassesUnique(null, null));

		assertEquals("foo", joinCssClassesUnique("foo", "foo"));
		assertEquals("foobar foo", joinCssClassesUnique("foobar", "foo"));
		assertEquals("foobar foo", joinCssClassesUnique("foobar foo", "foo"));
		assertEquals("foobar bar foo", joinCssClassesUnique("foobar bar", "foo"));
		assertEquals("foobar bar foo", joinCssClassesUnique("foobar bar foo", "foobar bar foo"));
		assertEquals("foobar foo bar baz", joinCssClassesUnique("foobar foo bar baz", "bar"));
		assertEquals("foo bar", joinCssClassesUnique("", "  foo  bar  "));
	}

	public void testRemoveClasses() {
		assertEquals("foo", removeCssClasses("foo bar", "bar"));
		assertEquals("foo", removeCssClasses("foo", null));
		assertNull(removeCssClasses(null, "foo"));
		assertNull(removeCssClasses(null, null));

		assertEquals(null, removeCssClasses("foo", "foo"));
		assertEquals("foobar", removeCssClasses("foobar foo", "foo"));
		assertEquals("foobar bar", removeCssClasses("foobar bar foo", "foo"));
		assertEquals(null, removeCssClasses("foobar bar foo", "foobar bar foo"));
		assertEquals("foobar foo baz", removeCssClasses("foobar foo bar baz", "bar"));
		assertEquals(null, removeCssClasses("foo bar", "  foo  bar  "));
	}

	public void testJoinManyClasses() {
		assertEquals("foo bar xxx", joinCssClasses("foo", "bar", "xxx"));
		assertEquals("foo bar", joinCssClasses("foo", null, "bar"));
		assertEquals("foo bar", joinCssClasses(null, "foo", "bar"));
		assertNull(joinCssClasses(null, null, null));
	}

	public void testNullWidthIsNotRelative() {
		assertFalse("Null width must be false!", isRelativeWidth(null));
	}

	public void testPercentWidthIsRelative() {
		assertTrue("Width with unit '%' must be recognized as relative!", isRelativeWidth("5%;"));
	}

	public void testPixelWidthIsNotRelative() {
		assertFalse("Width with unit 'px' must be recognized as not relative!", isRelativeWidth("5px;"));
	}

	public void testNullStringTerminationReturnsNull() {
		assertNull("Null string termination must return null!", terminateStyleDefinition(null));
	}

	public void testNonTerminatedStringBecomesTerminated() {
		assertTrue("Non terminated string must become terminated!", terminateStyleDefinition("5px").endsWith(";"));
	}

	public void testTerminatedStringWillNotBeDoubleTerminated() {
		assertFalse("Terminated string must become double terminated!", terminateStyleDefinition("5px;")
			.endsWith(";;"));
	}

	public void testNullStringWidthDefinitionReturnsNull() {
		assertNull("Null string width definition must return null!", ensureWidthStyle(null));
	}

	public void testPrependMissingWidthDefinition() {
		assertTrue("Missing width style definition must be prepended!", ensureWidthStyle("5px").startsWith("width:"));
	}

	public void testDontReturnDoubleWidthDefinition() {
		assertFalse("Dont prepend width style definition if it already exists!",
			ensureWidthStyle("width:5px").startsWith("width:width:"));
	}
}
