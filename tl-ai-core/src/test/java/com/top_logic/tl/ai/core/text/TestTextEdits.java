/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.text;

import java.util.Arrays;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.text.TextEdits.Failure;
import com.top_logic.tl.ai.core.text.TextEdits.Match;
import com.top_logic.tl.ai.core.text.TextEdits.Matching;
import com.top_logic.tl.ai.core.text.TextEdits.Result;

/**
 * Tests for {@link TextEdits}: what an anchored edit does, and what it refuses.
 *
 * <p>
 * The refusals carry as much weight here as the applications do. The whole point of the class is
 * that an anchor which does not say unambiguously what the caller meant changes <b>nothing</b>, and
 * that what comes back instead is enough to fix the anchor in one further step. A test suite that
 * only checked the happy path would pass on an implementation that silently edited the first match.
 * </p>
 */
public class TestTextEdits extends TestCase {

	// -- replace: the unique match --

	/** The ordinary case: one occurrence, replaced, everything else byte for byte as it was. */
	public void testReplaceTheOneOccurrence() {
		Result result = TextEdits.replace("alpha beta gamma", "beta", "BETA");

		assertApplied(result, "alpha BETA gamma");
		assertEquals(Match.EXACT, result.match());
		assertEquals(1, result.replacements());
	}

	/** An empty replacement deletes the passage; that is a legitimate edit, not a missing argument. */
	public void testReplaceWithNothingDeletes() {
		assertApplied(TextEdits.replace("alpha beta gamma", " beta", ""), "alpha gamma");
	}

	/** A <code>null</code> replacement is read as empty rather than refused. */
	public void testNullReplacementDeletes() {
		assertApplied(TextEdits.replace("alpha beta", " beta", null), "alpha");
	}

	/** An unset value and one set to the empty string are the same text to edit. */
	public void testNullSourceIsTheEmptyText() {
		assertApplied(TextEdits.replace(null, "", "first line"), "first line");
	}

	/** The anchor may be the whole text. */
	public void testAnchorMayBeTheWholeText() {
		assertApplied(TextEdits.replace("whole", "whole", "other"), "other");
	}

	/** Regex metacharacters in the anchor are characters, not syntax. */
	public void testAnchorIsNotAPattern() {
		assertApplied(TextEdits.replace("cost is $1.50 (net)", "$1.50 (net)", "$2.00 (net)"),
			"cost is $2.00 (net)");
	}

	/** The replacement is inserted literally, including what a regex engine would read as a group. */
	public void testReplacementIsNotAPattern() {
		assertApplied(TextEdits.replace("a b", "b", "$0\\1"), "a $0\\1");
	}

	/** The change offset points at the first changed character of the edited text. */
	public void testChangeOffsetPointsAtTheChange() {
		Result result = TextEdits.replace("alpha beta gamma", "beta", "BETA");

		assertEquals(6, result.changeOffset());
		assertEquals("BETA", result.text().substring(result.changeOffset(), result.changeOffset() + 4));
	}

	// -- replace: ambiguity --

	/** Two occurrences change nothing at all, which is the contract this class exists for. */
	public void testAmbiguousAnchorChangesNothing() {
		Result result = TextEdits.replace("beta\nbeta", "beta", "BETA");

		assertRefused(result, Failure.AMBIGUOUS);
		assertNull("Nothing may come back that a caller could mistake for an edited text.",
			result.text());
	}

	/** The refusal counts the occurrences and names the lines they are on. */
	public void testAmbiguousAnchorNamesTheLines() {
		Result result = TextEdits.replace("beta\nx\nbeta\ny\nbeta", "beta", "BETA");

		assertRefused(result, Failure.AMBIGUOUS);
		assertTrue("lines: " + Arrays.toString(result.lines()),
			Arrays.equals(new int[] { 1, 3, 5 }, result.lines()));
		assertContains(result.message(), "occurs 3 times");
		assertContains(result.message(), "lines 1, 3, 5");
	}

	/** Beyond ten occurrences the rest are counted rather than listed. */
	public void testManyOccurrencesAreCounted() {
		Result result = TextEdits.replace("x\n".repeat(14), "x", "y");

		assertRefused(result, Failure.AMBIGUOUS);
		assertEquals(14, result.lines().length);
		assertContains(result.message(), "and 4 more");
	}

	/** Extending the anchor until it is unique is the remedy the refusal names, and it works. */
	public void testAnExtendedAnchorResolvesTheAmbiguity() {
		String text = "beta\nx\nbeta\ny";

		assertRefused(TextEdits.replace(text, "beta", "BETA"), Failure.AMBIGUOUS);
		assertApplied(TextEdits.replace(text, "beta\ny", "BETA\ny"), "beta\nx\nBETA\ny");
	}

	/** Overlapping candidates are counted as the non-overlapping scan finds them, left to right. */
	public void testOverlappingCandidatesAreScannedLeftToRight() {
		Result result = TextEdits.replaceAll("aaaa", "aa", "b");

		assertApplied(result, "bb");
		assertEquals(2, result.replacements());
	}

	// -- replaceAll --

	/** Asking for every occurrence is the caller saying it does not need to tell them apart. */
	public void testReplaceAllTakesEveryOccurrence() {
		Result result = TextEdits.replaceAll("beta\nbeta\nbeta", "beta", "BETA");

		assertApplied(result, "BETA\nBETA\nBETA");
		assertEquals(3, result.replacements());
		assertTrue(Arrays.equals(new int[] { 1, 2, 3 }, result.lines()));
	}

	/** An anchor that occurs once behaves the same under replaceAll. */
	public void testReplaceAllWithOneOccurrence() {
		assertApplied(TextEdits.replaceAll("alpha", "alpha", "ALPHA"), "ALPHA");
	}

	/** replaceAll still refuses an anchor that occurs nowhere. */
	public void testReplaceAllStillNeedsAMatch() {
		assertRefused(TextEdits.replaceAll("alpha", "beta", "BETA"), Failure.NOT_FOUND);
	}

	// -- not found --

	/** An anchor that is nowhere changes nothing. */
	public void testMissingAnchorChangesNothing() {
		Result result = TextEdits.replace("alpha beta", "gamma", "GAMMA");

		assertRefused(result, Failure.NOT_FOUND);
		assertNull(result.text());
		assertEquals(0, result.replacements());
	}

	/**
	 * The refusal says where the anchor stopped agreeing with the text, which is the one fact that
	 * turns a second attempt into a correction rather than another guess.
	 */
	public void testMissingAnchorSaysWhereItStoppedAgreeing() {
		Result result = TextEdits.replace("line one\nline two\nline three",
			"line two and a half", "x");

		assertRefused(result, Failure.NOT_FOUND);
		assertContains(result.message(), "line two");
		assertContains(result.message(), "line 2");
	}

	/** An anchor with nothing in common with the text says so rather than quoting an empty prefix. */
	public void testTotallyDifferentAnchorSaysSo() {
		Result result = TextEdits.replace("alpha", "zzz", "x");

		assertRefused(result, Failure.NOT_FOUND);
		assertContains(result.message(), "neither does its beginning");
	}

	/** Nothing matches in an empty text except the empty anchor. */
	public void testNothingMatchesInAnEmptyText() {
		assertRefused(TextEdits.replace("", "anything", "x"), Failure.NOT_FOUND);
	}

	// -- the empty anchor --

	/** An empty anchor addresses nothing in a text that is not empty, so it is refused. */
	public void testEmptyAnchorIsRefused() {
		Result result = TextEdits.replace("alpha", "", "x");

		assertRefused(result, Failure.EMPTY_ANCHOR);
		assertContains(result.message(), "copied from the text as it is stored");
	}

	/** A <code>null</code> anchor is the empty one, and is refused the same way. */
	public void testNullAnchorIsRefused() {
		assertRefused(TextEdits.replace("alpha", null, "x"), Failure.EMPTY_ANCHOR);
	}

	/**
	 * Against the empty text the empty anchor addresses the one position there is, so it writes the
	 * first content. That is what lets a caller holding only this operation fill an unset value.
	 */
	public void testEmptyAnchorWritesTheEmptyText() {
		Result result = TextEdits.replace("", "", "first content");

		assertApplied(result, "first content");
		assertEquals(1, result.replacements());
		assertEquals(0, result.changeOffset());
	}

	/** An anchor of nothing but whitespace is a legitimate exact anchor: a blank line. */
	public void testWhitespaceOnlyAnchorIsAnExactAnchor() {
		Result result = TextEdits.replace("a\n\n\nb", "\n\n\n", "\n");

		assertApplied(result, "a\nb");
		assertEquals(Match.EXACT, result.match());
	}

	/**
	 * A whitespace-only anchor that does not occur exactly is refused rather than matched against
	 * every gap in the text by the tolerant level.
	 */
	public void testWhitespaceOnlyAnchorHasNoTolerantReading() {
		assertRefused(TextEdits.replace("a b", "\n\n", "x"), Failure.NOT_FOUND);
	}

	// -- the line-ending level --

	/** A value stored with CRLF and quoted back with LF still matches, and says that it did. */
	public void testCrLfStoredAndLfQuoted() {
		Result result = TextEdits.replace("one\r\ntwo\r\nthree", "one\ntwo", "ONE\nTWO");

		assertApplied(result, "ONE\nTWO\r\nthree");
		assertEquals(Match.LINE_ENDINGS, result.match());
		assertContains(result.message(), "line endings");
	}

	/** And the other way round: stored with LF, quoted with CRLF. */
	public void testLfStoredAndCrLfQuoted() {
		Result result = TextEdits.replace("one\ntwo\nthree", "one\r\ntwo", "X");

		assertApplied(result, "X\nthree");
		assertEquals(Match.LINE_ENDINGS, result.match());
	}

	/** A lone carriage return is a line break too, and is numbered as one. */
	public void testLoneCarriageReturnIsALineBreak() {
		Result result = TextEdits.replace("one\rtwo\rthree", "two", "TWO");

		assertApplied(result, "one\rTWO\rthree");
		assertEquals(3, TextEdits.lineOf("one\rtwo\rthree", 8));
	}

	/**
	 * <b>What is replaced is the range as stored.</b> The fallback decides where the passage is, never
	 * what it consists of, so the CRLF that ends the matched range is not dragged into the replacement.
	 */
	public void testTheLineEndingFallbackReplacesTheStoredRange() {
		Result result = TextEdits.replace("a\r\nb\r\nc", "b", "B");

		assertApplied(result, "a\r\nB\r\nc");
		assertEquals("The anchor is there character for character.", Match.EXACT, result.match());
	}

	/** An anchor spanning a CRLF replaces exactly the stored characters it spans. */
	public void testTheReplacedRangeEndsWhereTheAnchorEnds() {
		Result result = TextEdits.replace("a\r\nb\r\nc", "a\nb", "X");

		assertApplied(result, "X\r\nc");
	}

	// -- the whitespace level --

	/**
	 * A line quoted back with its indentation retyped as spaces still finds its passage - and what is
	 * replaced is the passage, so the indentation <em>as stored</em> stays the tab it was.
	 *
	 * <p>
	 * That asymmetry is the point: the fallback decides where the passage is, and the caller's idea of
	 * the whitespace around it is exactly the part that was wrong. An anchor that really means to
	 * change an indentation has to be exact, and then the exact level applies it.
	 * </p>
	 */
	public void testALineRetypedWithADifferentIndentStillFindsItsPassage() {
		Result result = TextEdits.replace("if (x) {\n\treturn 1;\n}", "    return 1;", "return 2;");

		assertApplied(result, "if (x) {\n\treturn 2;\n}");
		assertEquals(Match.WHITESPACE, result.match());
		assertContains(result.message(), "whitespace");
	}

	/** An indentation is changed with an exact anchor, which the cascade tries first. */
	public void testAnExactAnchorChangesTheIndentation() {
		Result result = TextEdits.replace("if (x) {\n\treturn 1;\n}", "\treturn 1;", "    return 1;");

		assertApplied(result, "if (x) {\n    return 1;\n}");
		assertEquals(Match.EXACT, result.match());
	}

	/** The anchor's own leading and trailing whitespace is not part of the passage it means. */
	public void testTheAnchorsOwnOuterWhitespaceIsDropped() {
		Result result = TextEdits.replace("alpha beta gamma", "  beta\n ", "BETA");

		assertApplied(result, "alpha BETA gamma");
		assertEquals(Match.WHITESPACE, result.match());
	}

	/** Double spaces in the stored text, single in the anchor. */
	public void testCollapsedRunsMatch() {
		Result result = TextEdits.replace("alpha   beta", "alpha beta", "x");

		assertApplied(result, "x");
		assertEquals(Match.WHITESPACE, result.match());
	}

	/**
	 * A non-breaking space is whitespace for this purpose. Java does not call it whitespace, and a
	 * text that went through a word processor is full of it.
	 */
	public void testNonBreakingSpaceMatchesAPlainSpace() {
		Result result = TextEdits.replace("alpha beta", "alpha beta", "x");

		assertApplied(result, "x");
		assertEquals(Match.WHITESPACE, result.match());
	}

	/** So are the typographic spaces: thin, en, ideographic. */
	public void testTypographicSpacesMatchAPlainSpace() {
		assertApplied(TextEdits.replace("a b", "a b", "x"), "x");
		assertApplied(TextEdits.replace("a b", "a b", "x"), "x");
		assertApplied(TextEdits.replace("a　b", "a b", "x"), "x");
	}

	/**
	 * <b>Only whitespace is tolerated.</b> A curly quote is not a straight one, an en dash is not a
	 * hyphen, and a text that differs from the anchor in a character the author chose is not the text
	 * the caller meant. Guessing there would break the byte-for-byte promise the whole design rests on.
	 */
	public void testTypographyIsNotNormalised() {
		assertRefused(TextEdits.replace("say “hello”", "say \"hello\"", "x"),
			Failure.NOT_FOUND);
		assertRefused(TextEdits.replace("1–2", "1-2", "x"), Failure.NOT_FOUND);
	}

	/** Case is not whitespace either. */
	public void testCaseIsNotNormalised() {
		assertRefused(TextEdits.replace("Alpha", "alpha", "x"), Failure.NOT_FOUND);
	}

	/**
	 * The whitespace level replaces exactly the stored characters the anchor spans, collapsed runs
	 * included - what goes in is what the caller wrote, what goes out is what stood there.
	 */
	public void testTheWhitespaceFallbackReplacesTheStoredRun() {
		Result result = TextEdits.replace("x   y z", "x y", "Q");

		assertApplied(result, "Q z");
	}

	/** Non-Latin text is text: the anchor is characters, and surrogate pairs survive. */
	public void testNonLatinTextIsJustText() {
		assertApplied(TextEdits.replace("Grüße aus München", "München", "Köln"), "Grüße aus Köln");
		assertApplied(TextEdits.replace("a 😀 b", "😀", "!"), "a ! b");
		assertApplied(TextEdits.replace("日本語のテキスト", "テキスト", "文章"), "日本語の文章");
	}

	// -- the cascade order --

	/** An exact match wins over a tolerant one, even when both exist. */
	public void testExactBeatsTolerant() {
		Result result = TextEdits.replace("a b\na  b", "a b", "X");

		assertApplied(result, "X\na  b");
		assertEquals(Match.EXACT, result.match());
	}

	/**
	 * A level is taken as a whole: if the exact level finds two occurrences, that is an ambiguity, not
	 * a reason to try the next level.
	 */
	public void testAnAmbiguousExactLevelIsNotEscapedByFallingBack() {
		assertRefused(TextEdits.replace("a b\na b\na  b", "a b", "X"), Failure.AMBIGUOUS);
	}

	/** The tolerant level counts occurrences that differ only in whitespace, and says so. */
	public void testTheTolerantLevelCanBeAmbiguousOnItsOwn() {
		Result result = TextEdits.replace("a  b\na   b", "a b", "X");

		assertRefused(result, Failure.AMBIGUOUS);
		assertContains(result.message(), "ignoring whitespace");
	}

	// -- strict matching --

	/** STRICT takes the anchor at its word: a near miss is a miss. */
	public void testStrictDoesNotFallBack() {
		assertRefused(TextEdits.replace("one\r\ntwo", "one\ntwo", "X", Matching.STRICT),
			Failure.NOT_FOUND);
		assertRefused(TextEdits.replace("a  b", "a b", "X", Matching.STRICT), Failure.NOT_FOUND);
	}

	/** STRICT still applies an exact match. */
	public void testStrictAppliesAnExactMatch() {
		Result result = TextEdits.replace("a b", "a b", "X", Matching.STRICT);

		assertApplied(result, "X");
		assertEquals(Match.EXACT, result.match());
	}

	// -- insertion --

	/** insertAfter keeps the anchor and puts the text behind it. */
	public void testInsertAfterKeepsTheAnchor() {
		Result result = TextEdits.insertAfter("# Title\n\nBody.", "# Title\n", "\n## Section\n");

		assertApplied(result, "# Title\n\n## Section\n\nBody.");
		assertContains(result.message(), "behind");
	}

	/** insertBefore keeps the anchor and puts the text in front of it. */
	public void testInsertBeforeKeepsTheAnchor() {
		Result result = TextEdits.insertBefore("Body.", "Body.", "# Title\n\n");

		assertApplied(result, "# Title\n\nBody.");
		assertContains(result.message(), "in front of");
	}

	/** Insertion needs a unique anchor for the same reason a replacement does. */
	public void testInsertionRefusesAnAmbiguousAnchor() {
		assertRefused(TextEdits.insertAfter("x\nx", "x", "!"), Failure.AMBIGUOUS);
	}

	/** The change offset of an insertion points at the inserted text, not at the anchor. */
	public void testInsertAfterChangeOffsetPointsAtTheInsertion() {
		Result result = TextEdits.insertAfter("abc", "abc", "!");

		assertEquals(3, result.changeOffset());
	}

	/** Insertion into the empty text through the empty anchor. */
	public void testInsertIntoTheEmptyText() {
		assertApplied(TextEdits.insertAfter("", "", "content"), "content");
		assertApplied(TextEdits.insertBefore(null, "", "content"), "content");
	}

	// -- line numbers --

	/** Line numbering starts at one and counts every convention's break exactly once. */
	public void testLineNumbering() {
		assertEquals(1, TextEdits.lineOf("abc", 0));
		assertEquals(1, TextEdits.lineOf("abc", 3));
		assertEquals(2, TextEdits.lineOf("a\nb", 2));
		assertEquals(2, TextEdits.lineOf("a\r\nb", 3));
		assertEquals(2, TextEdits.lineOf("a\rb", 2));
		assertEquals(3, TextEdits.lineOf("a\r\nb\r\nc", 6));
	}

	/** An offset past the end is numbered as the last line rather than refused. */
	public void testLineNumberBeyondTheEnd() {
		assertEquals(2, TextEdits.lineOf("a\nb", 99));
		assertEquals(1, TextEdits.lineOf(null, 5));
	}

	/** The line of a match in a CRLF text is the line its author sees. */
	public void testMatchLinesInACrLfText() {
		Result result = TextEdits.replaceAll("a\r\nb\r\na", "a", "X");

		assertTrue(Arrays.toString(result.lines()),
			Arrays.equals(new int[] { 1, 3 }, result.lines()));
	}

	// -- the result object --

	/** The line array handed out is the caller's own: mutating it cannot corrupt the result. */
	public void testLinesAreHandedOutByCopy() {
		Result result = TextEdits.replaceAll("a\na", "a", "b");

		result.lines()[0] = 99;

		assertEquals(1, result.lines()[0]);
	}

	/** A refusal carries no text, no match level and no offset. */
	public void testARefusalCarriesNothingUsableAsAText() {
		Result result = TextEdits.replace("a", "b", "c");

		assertFalse(result.applied());
		assertNull(result.text());
		assertNull(result.match());
		assertEquals(-1, result.changeOffset());
		assertEquals(0, result.lines().length);
		assertNotNull(result.message());
	}

	private static void assertApplied(Result result, String expected) {
		assertTrue("Expected the edit to be applied, but: " + result.message(), result.applied());
		assertNull(result.failure());
		assertEquals(expected, result.text());
	}

	private static void assertRefused(Result result, Failure expected) {
		assertFalse("Expected the edit to be refused, but it produced: " + result.text(),
			result.applied());
		assertEquals(expected, result.failure());
	}

	private static void assertContains(String actual, String expected) {
		assertTrue("Expected to find '" + expected + "' in: " + actual, actual.contains(expected));
	}
}
