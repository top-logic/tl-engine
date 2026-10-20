/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.text;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.tools.text.TextGutter.Gutter;

/**
 * Tests for {@link TextGutter}.
 *
 * <p>
 * Two properties carry the weight. The line content must come out exactly as it went in, because a
 * reader quotes it back as an anchor and an anchor that was tidied on the way out never matches. And
 * the character budget must be spent on whole lines, because a line cut in half is an anchor that
 * looks complete and is not.
 * </p>
 */
public class TestTextGutter extends TestCase {

	/** Every line behind its number, in a column wide enough for the highest one. */
	public void testNumbersEveryLine() {
		Gutter gutter = TextGutter.render("a\nb\nc", null, null, Integer.MAX_VALUE);

		assertEquals("  1| a\n  2| b\n  3| c", gutter.text());
		assertEquals(3, gutter.totalLines());
		assertEquals(1, gutter.firstLine());
		assertEquals(3, gutter.lastLine());
		assertFalse(gutter.truncated());
	}

	/** The column grows with the highest number rendered, so the lines still line up. */
	public void testTheNumberColumnGrowsWithTheText() {
		Gutter gutter = TextGutter.render("x\n".repeat(1000), 999, 1000, Integer.MAX_VALUE);

		assertEquals(" 999| x\n1000| x", gutter.text());
	}

	/** Line content is verbatim, indentation and trailing spaces included. */
	public void testLineContentIsVerbatim() {
		Gutter gutter = TextGutter.render("\tindented  \n", 1, 1, Integer.MAX_VALUE);

		assertEquals("  1| \tindented  ", gutter.text());
	}

	/** A CRLF text does not show a stray carriage return at the end of every line. */
	public void testCrLfLinesCarryNoTerminator() {
		Gutter gutter = TextGutter.render("a\r\nb", null, null, Integer.MAX_VALUE);

		assertEquals("  1| a\n  2| b", gutter.text());
		assertEquals(2, gutter.totalLines());
	}

	/** A lone carriage return ends a line too. */
	public void testLoneCarriageReturnEndsALine() {
		assertEquals(2, TextGutter.lineCount("a\rb"));
	}

	/** A trailing line break opens one more, empty, line - which is what its author sees. */
	public void testATrailingBreakOpensAnEmptyLine() {
		Gutter gutter = TextGutter.render("a\n", null, null, Integer.MAX_VALUE);

		assertEquals("  1| a\n  2| ", gutter.text());
		assertEquals(2, gutter.totalLines());
	}

	/** The empty text is one line, which is empty. */
	public void testTheEmptyTextIsOneEmptyLine() {
		assertEquals(1, TextGutter.lineCount(""));
		assertEquals(1, TextGutter.lineCount(null));
	}

	/** A range renders what was asked for and reports what the whole text has. */
	public void testARange() {
		Gutter gutter = TextGutter.render("a\nb\nc\nd", 2, 3, Integer.MAX_VALUE);

		assertEquals("  2| b\n  3| c", gutter.text());
		assertEquals(2, gutter.firstLine());
		assertEquals(3, gutter.lastLine());
		assertEquals(4, gutter.totalLines());
	}

	/** A range that runs past the end stops at the end rather than refusing. */
	public void testARangePastTheEndIsClamped() {
		Gutter gutter = TextGutter.render("a\nb", 2, 99, Integer.MAX_VALUE);

		assertEquals("  2| b", gutter.text());
		assertEquals(2, gutter.lastLine());
	}

	/** A range that begins past the end renders nothing and says how many lines there are. */
	public void testARangeBeyondTheEndIsEmpty() {
		Gutter gutter = TextGutter.render("a\nb", 9, 12, Integer.MAX_VALUE);

		assertEquals("", gutter.text());
		assertEquals(2, gutter.totalLines());
	}

	/** A first line below one is the first line. */
	public void testAFirstLineBelowOne() {
		assertEquals("  1| a", TextGutter.render("a", 0, null, Integer.MAX_VALUE).text());
		assertEquals("  1| a", TextGutter.render("a", -5, null, Integer.MAX_VALUE).text());
	}

	/** The budget is spent on whole lines, and the caller is told where rendering stopped. */
	public void testTheBudgetStopsOnALineBoundary() {
		Gutter gutter = TextGutter.render("aaaa\nbbbb\ncccc", null, null, 20);

		assertEquals("  1| aaaa\n  2| bbbb", gutter.text());
		assertTrue(gutter.truncated());
		assertEquals(2, gutter.lastLine());
		assertEquals(3, gutter.totalLines());
	}

	/**
	 * A single line longer than the whole budget is rendered anyway rather than cut: half a line is
	 * an anchor that looks whole and never matches, and rendering nothing at all says less than
	 * rendering the line.
	 */
	public void testTheFirstLineIsRenderedHoweverLongItIs() {
		Gutter gutter = TextGutter.render("x".repeat(100) + "\ny", null, null, 10);

		assertTrue(gutter.text().startsWith("  1| xxxx"));
		assertTrue(gutter.truncated());
		assertEquals(1, gutter.lastLine());
	}

	/** An excerpt shows the lines around an offset, numbered as they are in the whole text. */
	public void testExcerptAroundAnOffset() {
		String text = "a\nb\nc\nd\ne\nf\ng";

		assertEquals("  3| c\n  4| d\n  5| e",
			TextGutter.excerpt(text, text.indexOf('d'), 1, Integer.MAX_VALUE));
	}

	/** An excerpt near the start is clamped rather than shifted. */
	public void testExcerptAtTheStart() {
		assertEquals("  1| a\n  2| b", TextGutter.excerpt("a\nb\nc", 0, 1, Integer.MAX_VALUE));
	}

	/** Nothing to show is the empty string, not a rendering of nothing. */
	public void testExcerptOfNothing() {
		assertEquals("", TextGutter.excerpt("a", -1, 2, Integer.MAX_VALUE));
		assertEquals("", TextGutter.excerpt(null, 0, 2, Integer.MAX_VALUE));
	}

	/**
	 * An excerpt that does not fit its budget is dropped rather than cut.
	 *
	 * <p>
	 * The one place the "never cut a line" rule of {@link TextGutter#render} has to be overruled: it
	 * hands back a single oversized line whole, which is right for a read the caller will anchor on
	 * and wrong for a confirmation that is only a convenience.
	 * </p>
	 */
	public void testAnExcerptTooLongToShowIsDropped() {
		assertEquals("", TextGutter.excerpt("x".repeat(5000), 0, 3, 4000));
	}

	/** One that does fit is shown in full. */
	public void testAnExcerptThatFitsIsShown() {
		assertEquals("  1| " + "x".repeat(100), TextGutter.excerpt("x".repeat(100), 0, 3, 4000));
	}
}
