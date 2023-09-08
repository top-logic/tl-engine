/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.util;

import static com.top_logic.basic.util.RegExpUtil.*;

import java.util.regex.Pattern;

import junit.framework.TestCase;

import com.top_logic.basic.util.RegExpUtil;

/**
 * Tests for {@link RegExpUtil}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TestRegExpUtil extends TestCase {

	/**
	 * Test for {@link RegExpUtil#replaceTheOnlyOne(CharSequence, java.util.regex.Pattern, String)}
	 */
	public void testReplaceTheOnlyOne() {
		String replacement = "X";
		assertEquals("", replaceTheOnlyOne("", Pattern.compile(""), ""));
		assertEquals("aXc", replaceTheOnlyOne("abc", Pattern.compile("b"), replacement));

		assertRuntimeException("abc", "Y", replacement,
			"RegExp did not match but no exception was thrown!");

		assertRuntimeException("a", "", replacement,
			"RegExp matched more than once but no exception was thrown!");

		assertRuntimeException("aa", "a", replacement,
			"RegExp matched more than once but no exception was thrown!");

		assertRuntimeException("abacaba", "aba", replacement,
			"RegExp matched more than once but no exception was thrown!");

		assertRuntimeException("abaaba", "aba", replacement,
			"RegExp matched more than once but no exception was thrown!");

		assertRuntimeException("ababa", "aba", replacement,
			"RegExp matched more than once but no exception was thrown!");
	}

	private void assertRuntimeException(String text, String regExp, String replacement, String errorMessage) {
		boolean errorThrown = false;
		try {
			replaceTheOnlyOne(text, Pattern.compile(regExp), replacement);
		} catch (RuntimeException expected) {
			errorThrown = true;
		}
		if (!errorThrown) {
			fail(errorMessage + " RegExp: '" + regExp + "'; Text: '" + text + "'");
		}
	}

	/**
	 * Test for {@link RegExpUtil#contains(CharSequence, Pattern)}.
	 */
	public void testContains() {
		assertTrue(contains("", Pattern.compile("")));
		assertTrue(contains("a", Pattern.compile("")));
		assertFalse(contains("", Pattern.compile("a")));
		assertTrue(contains("a", Pattern.compile("a")));
	}

	/**
	 * Test that {@link RegExpUtil#indexAfterLastMatch(String, Pattern)} throws an
	 * {@link NullPointerException} if any of the parameters is <code>null</code>.
	 */
	public void testIndexAfterLastMatchNull() {
		assertIndexAfterLastMatchThrowsNullPointerException("", null);
		assertIndexAfterLastMatchThrowsNullPointerException(null, Pattern.compile(""));
		assertIndexAfterLastMatchThrowsNullPointerException(null, null);
	}

	/**
	 * Test for {@link RegExpUtil#indexAfterLastMatch(String, Pattern)} returns the correct result.
	 */
	public void testIndexAfterLastMatch() {
		assertEquals(0, indexAfterLastMatch("", Pattern.compile("")));
		assertEquals(1, indexAfterLastMatch("a", Pattern.compile("")));
		assertEquals(-1, indexAfterLastMatch("", Pattern.compile("a")));
		assertEquals(1, indexAfterLastMatch("a", Pattern.compile("a")));
		assertEquals(2, indexAfterLastMatch("XaX", Pattern.compile("a")));
		assertEquals(4, indexAfterLastMatch("XaXaX", Pattern.compile("a")));
		assertEquals(3, indexAfterLastMatch("aaa", Pattern.compile("aa")));
		assertEquals(4, indexAfterLastMatch("XaaaX", Pattern.compile("aa")));
		assertEquals(5, indexAfterLastMatch("ababa", Pattern.compile("aba")));
		assertEquals(6, indexAfterLastMatch("XababaX", Pattern.compile("aba")));
		assertEquals(12, indexAfterLastMatch("XababaXababaX", Pattern.compile("aba")));
		assertEquals(0, indexAfterLastMatch("", Pattern.compile("\\A")));
		assertEquals(0, indexAfterLastMatch("a", Pattern.compile("\\A")));
		assertEquals(0, indexAfterLastMatch("", Pattern.compile("\\z")));
		assertEquals(1, indexAfterLastMatch("a", Pattern.compile("\\z")));
		assertEquals(0, indexAfterLastMatch("", Pattern.compile("^")));
		assertEquals(0, indexAfterLastMatch("a", Pattern.compile("^")));
		assertEquals(0, indexAfterLastMatch("", Pattern.compile("$")));
		assertEquals(1, indexAfterLastMatch("a", Pattern.compile("$")));
	}

	private void assertIndexAfterLastMatchThrowsNullPointerException(String text, Pattern pattern) {
		try {
			indexAfterLastMatch(text, pattern);
			fail("Expected an NullPointerException, but method returned normally!");
		} catch (NullPointerException exception) {
			// Correct
		}
	}
}
