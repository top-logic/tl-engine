/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import junit.framework.TestCase;

import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.filter.TextMatchMode;

/**
 * Test for the {@link TextMatchMode search modes} of a {@link TextFilterState}.
 */
public class TestTextMatchModes extends TestCase {

	private static Predicate<String> matcher(String pattern, TextMatchMode mode) {
		return new TextFilterState(pattern, false, mode).matcher();
	}

	private static Predicate<String> caseSensitive(String pattern, TextMatchMode mode) {
		return new TextFilterState(pattern, true, mode).matcher();
	}

	/** The default mode finds the pattern anywhere, ignoring case. */
	public void testContainsIsDefault() {
		assertEquals(TextMatchMode.CONTAINS, new TextFilterState("ber", false, null).mode());
		Predicate<String> matcher = TextFilterState.contains("ber").matcher();
		assertTrue(matcher.test("Hamburg ber"));
		assertTrue(matcher.test("Bern"));
		assertTrue(matcher.test("Oberhausen"));
		assertFalse(matcher.test("Hamburg"));
	}

	/** A word must start with the pattern. */
	public void testWordStart() {
		Predicate<String> matcher = matcher("ber", TextMatchMode.WORD_START);
		assertTrue(matcher.test("Bern"));
		assertTrue(matcher.test("Max Berger"));
		assertTrue(matcher.test("Max-Berger"));
		assertFalse(matcher.test("Hamburg"));
		assertFalse(matcher.test("Oberhausen"));
	}

	/** The pattern must occur as a whole word. */
	public void testWholeWord() {
		Predicate<String> matcher = matcher("Bern", TextMatchMode.WHOLE_WORD);
		assertTrue(matcher.test("nach Bern fahren"));
		assertTrue(matcher.test("Bern"));
		assertTrue(matcher.test("(Bern)"));
		assertFalse(matcher.test("Berner"));
		assertFalse(matcher.test("Unterbern"));
	}

	/** Word boundaries are Unicode word boundaries. */
	public void testUnicodeWords() {
		assertFalse(matcher("Bär", TextMatchMode.WHOLE_WORD).test("Bären"));
		assertTrue(matcher("Bär", TextMatchMode.WHOLE_WORD).test("der Bär ist los"));
		assertFalse("A letter with diacritic is part of the word.",
			matcher("ber", TextMatchMode.WORD_START).test("über"));
		assertTrue(matcher("über", TextMatchMode.WORD_START).test("Das Überbleibsel"));
	}

	/** The pattern of the regular expression mode is a regular expression. */
	public void testRegexp() {
		Predicate<String> matcher = matcher("^B.*n$", TextMatchMode.REGEXP);
		assertTrue(matcher.test("Bern"));
		assertTrue(matcher.test("bremen"));
		assertFalse(matcher.test("Berner"));
		assertFalse(matcher.test("Hamburg"));
	}

	/** Every mode distinguishes upper and lower case on request. */
	public void testCaseSensitiveInEveryMode() {
		assertTrue(matcher("bern", TextMatchMode.CONTAINS).test("Bern"));
		assertFalse(caseSensitive("bern", TextMatchMode.CONTAINS).test("Bern"));
		assertTrue(caseSensitive("Bern", TextMatchMode.CONTAINS).test("Bern"));

		assertTrue(matcher("bern", TextMatchMode.WORD_START).test("Bern"));
		assertFalse(caseSensitive("bern", TextMatchMode.WORD_START).test("Bern"));
		assertTrue(caseSensitive("Ber", TextMatchMode.WORD_START).test("Bern"));

		assertTrue(matcher("bern", TextMatchMode.WHOLE_WORD).test("Bern"));
		assertFalse(caseSensitive("bern", TextMatchMode.WHOLE_WORD).test("Bern"));
		assertTrue(caseSensitive("Bern", TextMatchMode.WHOLE_WORD).test("Bern"));

		assertTrue(matcher("^bern$", TextMatchMode.REGEXP).test("Bern"));
		assertFalse(caseSensitive("^bern$", TextMatchMode.REGEXP).test("Bern"));
		assertTrue(caseSensitive("^Bern$", TextMatchMode.REGEXP).test("Bern"));
	}

	/** Outside the regular expression mode, special characters stand for themselves. */
	public void testSpecialCharactersLiteral() {
		assertTrue(matcher("a.b", TextMatchMode.CONTAINS).test("x a.b y"));
		assertFalse(matcher("a.b", TextMatchMode.CONTAINS).test("axb"));

		assertTrue(matcher("(x", TextMatchMode.WORD_START).test("foo (x)"));
		assertFalse(matcher("a.", TextMatchMode.WORD_START).test("ab"));

		assertTrue(matcher("c++", TextMatchMode.WHOLE_WORD).test("learn c++ now"));
		assertFalse(matcher("c++", TextMatchMode.WHOLE_WORD).test("abc++"));
		assertFalse(matcher("a*", TextMatchMode.WHOLE_WORD).test("aaa"));

		assertTrue("In regular expression mode, the dot is any character.",
			matcher("a.b", TextMatchMode.REGEXP).test("axb"));
	}

	/** The whole-field flag requires the whole text to match. */
	public void testWholeField() {
		Predicate<String> literal = new TextFilterState("Bern", false, TextMatchMode.WORD_START, true).matcher();
		assertTrue(literal.test("bern"));
		assertFalse(literal.test("Berner"));

		Predicate<String> regexp = new TextFilterState("B.*n", false, TextMatchMode.REGEXP, true).matcher();
		assertTrue(regexp.test("Bern"));
		assertFalse(regexp.test("Bern fahrt"));
	}

	/** The mode is persisted and read back. */
	public void testModePersisted() {
		for (TextMatchMode mode : TextMatchMode.values()) {
			TextFilterState state = new TextFilterState("x", true, mode);
			assertEquals(state, TextColumnFilter.textFromJson(TextColumnFilter.textToJson(state)));
		}
	}

	/** A stored state without mode reads its regular expression flag. */
	public void testStateWithoutModeRead() {
		Map<String, Object> regexp = new LinkedHashMap<>();
		regexp.put(TextColumnFilter.PATTERN, "^B");
		regexp.put(TextColumnFilter.REGEXP, Boolean.TRUE);
		assertEquals(new TextFilterState("^B", false, TextMatchMode.REGEXP),
			TextColumnFilter.textFromJson(regexp));

		Map<String, Object> plain = new LinkedHashMap<>();
		plain.put(TextColumnFilter.PATTERN, "B");
		plain.put(TextColumnFilter.REGEXP, Boolean.FALSE);
		plain.put(TextColumnFilter.WHOLE_FIELD, Boolean.TRUE);
		assertEquals(new TextFilterState("B", false, TextMatchMode.CONTAINS, true),
			TextColumnFilter.textFromJson(plain));
	}

}
