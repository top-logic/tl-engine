/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.text;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.tl.ai.core.text.TextEdits;

/**
 * Renders a text with a line number in front of every line, for a tool answer a model has to quote
 * passages back out of.
 *
 * <p>
 * <b>The gutter is not the text.</b> Every line is written as it is stored, behind a number and a
 * {@code "| "} that are this tool's own framing. A reader that means to quote a passage has to drop
 * the framing first, which is what the reading tool's description says in as many words. The
 * alternative - no numbers at all - costs the ability to say "the anchor occurs on lines 4 and 17",
 * which is what makes an ambiguous anchor fixable in one step instead of several.
 * </p>
 *
 * <p>
 * Line breaks are counted the way their author writes them: {@code \r\n}, {@code \n} and a lone
 * {@code \r} each end one line. What the gutter renders is the line content without its terminator,
 * so a value stored with CRLF does not show a stray carriage return at the end of every line.
 * </p>
 */
public final class TextGutter {

	/** What separates the line number from the line. */
	private static final String SEPARATOR = "| ";

	/** The narrowest line-number column, so that short values still line up. */
	private static final int MIN_NUMBER_WIDTH = 3;

	private TextGutter() {
		// Static utility.
	}

	/**
	 * A rendered range of a text.
	 *
	 * @param text
	 *        The lines of the range, each behind its number. Empty when the range is empty.
	 * @param firstLine
	 *        The 1-based number of the first line rendered.
	 * @param lastLine
	 *        The 1-based number of the last line rendered; {@code firstLine - 1} when none was.
	 * @param totalLines
	 *        How many lines the whole text has, whatever was rendered.
	 * @param truncated
	 *        Whether rendering stopped at the character budget rather than at the end of the asked-for
	 *        range.
	 */
	public record Gutter(String text, int firstLine, int lastLine, int totalLines, boolean truncated) {
		// Intentionally empty.
	}

	/**
	 * Renders the given line range of the given text.
	 *
	 * @param text
	 *        The text to render. <code>null</code> is read as empty.
	 * @param fromLine
	 *        The first line to render, 1-based; <code>null</code> or below 1 means the first.
	 * @param toLine
	 *        The last line to render, 1-based and inclusive; <code>null</code> means the last.
	 * @param maxChars
	 *        How many characters the rendering may take. Rendering stops on a line boundary before
	 *        the budget is exceeded, never inside a line: a line cut in half would be quoted back as
	 *        an anchor that never matches.
	 * @return What was rendered and what was left out; never <code>null</code>.
	 */
	public static Gutter render(String text, Integer fromLine, Integer toLine, int maxChars) {
		List<String> lines = split(text);
		int total = lines.size();
		int first = Math.max(1, fromLine == null ? 1 : fromLine);
		int last = Math.min(total, toLine == null ? total : toLine);
		if (first > total || last < first) {
			return new Gutter("", first, first - 1, total, false);
		}
		int width = numberWidth(last);
		StringBuilder rendered = new StringBuilder();
		int reached = first - 1;
		for (int line = first; line <= last; line++) {
			String entry = number(line, width) + SEPARATOR + lines.get(line - 1);
			if (rendered.length() > 0 && rendered.length() + 1 + entry.length() > maxChars) {
				return new Gutter(rendered.toString(), first, reached, total, true);
			}
			if (rendered.length() > 0) {
				rendered.append('\n');
			}
			rendered.append(entry);
			reached = line;
		}
		return new Gutter(rendered.toString(), first, reached, total, false);
	}

	/**
	 * The lines around the given offset, rendered like {@link #render}.
	 *
	 * <p>
	 * For showing back what an edit changed without making the caller read the whole value again.
	 * </p>
	 *
	 * @param text
	 *        The text the offset points into.
	 * @param offset
	 *        A character offset in it; a negative one renders nothing.
	 * @param contextLines
	 *        How many lines above and below to show as well.
	 * @param maxChars
	 *        How many characters the excerpt may take. A handful of lines is usually nothing, but a
	 *        value held on one very long line is a handful of that line.
	 * @return The rendering, or the empty string if there is nothing to show.
	 */
	public static String excerpt(String text, int offset, int contextLines, int maxChars) {
		if (text == null || offset < 0) {
			return "";
		}
		int line = TextEdits.lineOf(text, offset);
		String rendered = render(text, line - contextLines, line + contextLines, maxChars).text();
		// render() never cuts a line, so one line longer than the whole budget comes back whole. That
		// is right for a read - half a line is an anchor that looks complete and never matches - and
		// wrong here, where the excerpt is a convenience. Showing nothing is the better failure.
		return rendered.length() > maxChars ? "" : rendered;
	}

	/**
	 * How many lines the given text has; an empty text has one, which is empty.
	 */
	public static int lineCount(String text) {
		return split(text).size();
	}

	/**
	 * The lines of the text, without their terminators.
	 */
	private static List<String> split(String text) {
		List<String> lines = new ArrayList<>();
		String value = text == null ? "" : text;
		int start = 0;
		int i = 0;
		while (i < value.length()) {
			char c = value.charAt(i);
			if (c == '\n') {
				lines.add(value.substring(start, i));
				i++;
			} else if (c == '\r') {
				lines.add(value.substring(start, i));
				i += i + 1 < value.length() && value.charAt(i + 1) == '\n' ? 2 : 1;
			} else {
				i++;
				continue;
			}
			start = i;
		}
		lines.add(value.substring(start));
		return lines;
	}

	private static int numberWidth(int highest) {
		return Math.max(MIN_NUMBER_WIDTH, String.valueOf(highest).length());
	}

	private static String number(int line, int width) {
		String digits = String.valueOf(line);
		return digits.length() >= width ? digits : " ".repeat(width - digits.length()) + digits;
	}
}
