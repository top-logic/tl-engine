/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.text;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.tl.ai.index.lucene.Locator;

/**
 * Cuts the text of one attribute into chunks a model can embed and a hit can show.
 *
 * <p>
 * A text with page markers is split at the markers first, so a chunk never crosses a page and a hit
 * has one page number. Inside a page the text is cut at paragraph and sentence boundaries into chunks
 * of at most {@link #maxChars()} characters; a chunk repeats the last {@link #overlapSentences()}
 * sentences of the one before, so a statement that spans the cut is whole in one of them. A single
 * sentence longer than the limit is cut at white space.
 * </p>
 *
 * <p>
 * Every chunk's text is exactly the attribute's text between its {@link Locator#start()} and
 * {@link Locator#end()}: a passage quoted from a chunk is a passage of the attribute, and a reader can
 * open the attribute at the offset. The marker line itself is in no chunk.
 * </p>
 *
 * @param maxChars
 *        The largest chunk, in characters.
 * @param overlapSentences
 *        How many sentences a chunk repeats from the one before.
 * @param pageMarker
 *        A line that starts a page, with the page number as its first group; <code>null</code> for
 *        texts without pages.
 */
public record Chunker(int maxChars, int overlapSentences, Pattern pageMarker) {

	/** A blank line: a paragraph boundary, never inside a chunk's unit. */
	private static final Pattern PARAGRAPH = Pattern.compile("\\n[ \\t]*\\n");

	/**
	 * Creates a {@link Chunker}.
	 */
	public Chunker {
		if (maxChars < 50) {
			throw new IllegalArgumentException("A chunk must hold at least 50 characters, not " + maxChars + ".");
		}
		if (overlapSentences < 0) {
			throw new IllegalArgumentException("The overlap must not be negative.");
		}
	}

	/**
	 * A chunker without page markers.
	 */
	public static Chunker of(int maxChars, int overlapSentences) {
		return new Chunker(maxChars, overlapSentences, null);
	}

	/**
	 * A chunk of text with its place in the attribute.
	 *
	 * @param text
	 *        The attribute's text between the locator's offsets.
	 * @param locator
	 *        Where it lies.
	 */
	public record Chunk(String text, Locator locator) {
		// A plain carrier.
	}

	/**
	 * The chunks of a text, in text order, numbered from 0.
	 */
	public List<Chunk> chunk(String text) {
		List<Chunk> result = new ArrayList<>();
		if (text == null || text.isBlank()) {
			return result;
		}
		for (Page page : pages(text)) {
			for (int[] span : chunkSpans(text, page.start(), page.end())) {
				result.add(new Chunk(text.substring(span[0], span[1]),
					new Locator(result.size(), span[0], span[1], page.number())));
			}
		}
		return result;
	}

	/** A page of the text: from behind its marker line to the next marker line. */
	private record Page(int number, int start, int end) {
		// A plain carrier.
	}

	private List<Page> pages(String text) {
		List<Page> result = new ArrayList<>();
		if (pageMarker == null) {
			result.add(new Page(0, 0, text.length()));
			return result;
		}
		Matcher marker = pageMarker.matcher(text);
		int start = 0;
		int number = 0;
		while (marker.find()) {
			result.add(new Page(number, start, marker.start()));
			number = pageNumber(marker);
			start = marker.end();
		}
		result.add(new Page(number, start, text.length()));
		return result;
	}

	private static int pageNumber(Matcher marker) {
		if (marker.groupCount() < 1 || marker.group(1) == null) {
			return 0;
		}
		try {
			return Integer.parseInt(marker.group(1));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	/**
	 * The spans of the chunks of {@code text[start, end)}.
	 */
	private List<int[]> chunkSpans(String text, int start, int end) {
		List<int[]> units = units(text, start, end);
		List<int[]> result = new ArrayList<>();
		int next = 0;
		while (next < units.size()) {
			int first = next;
			int last = next;
			while (last + 1 < units.size() && units.get(last + 1)[1] - units.get(first)[0] <= maxChars) {
				last++;
			}
			result.add(new int[] { units.get(first)[0], units.get(last)[1] });
			if (last + 1 >= units.size()) {
				break;
			}
			// The next chunk repeats up to overlapSentences units, but always moves on by at least one.
			int overlap = Math.min(overlapSentences, last - first);
			while (overlap > 0 && units.get(last + 1)[1] - units.get(last + 1 - overlap)[0] > maxChars) {
				overlap--;
			}
			next = last + 1 - overlap;
		}
		return result;
	}

	/**
	 * The sentences of {@code text[start, end)}, trimmed, none crossing a paragraph boundary, none
	 * longer than {@link #maxChars()}.
	 */
	private List<int[]> units(String text, int start, int end) {
		List<int[]> result = new ArrayList<>();
		Matcher paragraph = PARAGRAPH.matcher(text).region(start, end);
		int from = start;
		while (paragraph.find()) {
			sentences(text, from, paragraph.start(), result);
			from = paragraph.end();
		}
		sentences(text, from, end, result);
		return result;
	}

	private void sentences(String text, int start, int end, List<int[]> result) {
		BreakIterator sentences = BreakIterator.getSentenceInstance(Locale.ROOT);
		sentences.setText(text.substring(start, end));
		int from = sentences.first();
		for (int to = sentences.next(); to != BreakIterator.DONE; from = to, to = sentences.next()) {
			addTrimmed(text, start + from, start + to, result);
		}
	}

	private void addTrimmed(String text, int start, int end, List<int[]> result) {
		while (start < end && Character.isWhitespace(text.charAt(start))) {
			start++;
		}
		while (end > start && Character.isWhitespace(text.charAt(end - 1))) {
			end--;
		}
		while (end - start > maxChars) {
			int cut = start + maxChars;
			int space = cut;
			while (space > start + maxChars / 2 && !Character.isWhitespace(text.charAt(space))) {
				space--;
			}
			if (space > start + maxChars / 2) {
				cut = space;
			}
			addTrimmed(text, start, cut, result);
			start = cut;
			while (start < end && Character.isWhitespace(text.charAt(start))) {
				start++;
			}
		}
		if (start < end) {
			result.add(new int[] { start, end });
		}
	}

}
