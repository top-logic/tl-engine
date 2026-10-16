/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.text;

import java.util.List;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.citation.Finding;
import com.top_logic.tl.ai.index.lucene.Locator;

/**
 * Chunks never cross a page, are exact slices of the text, stay below the limit, repeat the last
 * sentence of the chunk before, and a passage quoted from a chunk passes the citation check.
 */
public class TestChunker extends TestCase {

	/** The marker of an extracted file's page, as Pietro writes it. */
	private static final Pattern PAGE = Pattern.compile("^\\[p\\.(\\d+)\\]$", Pattern.MULTILINE);

	private static final String TEXT = String.join("\n",
		"[p.1]",
		"The base plate is made of anodised aluminium. It carries the pump.",
		"",
		"The pump is mounted with four M6 screws.",
		"[p.2]",
		"Die Lieferzeit beträgt zwölf Wochen. Der Kunde wünscht",
		"eine kürzere Lieferzeit. Das Team prüft eine zweite Quelle.",
		"Die Prüfung der Schweißnähte erfolgt extern. Die Abnahme folgt danach.",
		"[p.3]",
		"Last page.");

	/** Each chunk has the page of its marker and is exactly the text between its offsets. */
	public void testPagesAndOffsets() {
		List<Chunker.Chunk> chunks = new Chunker(120, 1, PAGE).chunk(TEXT);

		assertFalse(chunks.isEmpty());
		int previousPage = 0;
		for (int i = 0; i < chunks.size(); i++) {
			Chunker.Chunk chunk = chunks.get(i);
			Locator locator = chunk.locator();
			assertEquals("Chunks are numbered in order.", i, locator.chunk());
			assertEquals("A chunk is a slice of the text.", TEXT.substring(locator.start(), locator.end()),
				chunk.text());
			assertFalse("The marker line is in no chunk: " + chunk.text(), chunk.text().contains("[p."));
			assertTrue("A chunk stays below the limit.", chunk.text().length() <= 120);
			assertTrue("Pages only grow.", locator.page() >= previousPage);
			previousPage = locator.page();
		}
		assertEquals(1, pageOf(chunks, "anodised aluminium"));
		assertEquals(1, pageOf(chunks, "four M6 screws"));
		assertEquals(2, pageOf(chunks, "zwölf Wochen"));
		assertEquals(2, pageOf(chunks, "Abnahme folgt"));
		assertEquals(3, pageOf(chunks, "Last page."));
	}

	/** Every sentence of the text is in some chunk. */
	public void testNothingIsLost() {
		List<Chunker.Chunk> chunks = new Chunker(60, 0, PAGE).chunk(TEXT);
		for (String sentence : List.of("It carries the pump.", "The pump is mounted with four M6 screws.",
			"Das Team prüft eine zweite Quelle.", "Die Abnahme folgt danach.", "Last page.")) {
			assertTrue("Lost: " + sentence, chunks.stream().anyMatch(c -> c.text().contains(sentence)));
		}
	}

	/** The next chunk on a page starts with the last sentence of the one before. */
	public void testOverlapRepeatsTheLastSentence() {
		List<Chunker.Chunk> chunks = new Chunker(100, 1, PAGE).chunk(TEXT);
		Chunker.Chunk first = find(chunks, "Die Lieferzeit");
		Chunker.Chunk second = chunks.get(first.locator().chunk() + 1);
		assertEquals(2, second.locator().page());
		assertTrue("Overlap: " + first.text() + " | " + second.text(),
			second.text().startsWith("Der Kunde wünscht") || second.text().startsWith("Das Team"));
		assertTrue(first.text().contains(second.text().substring(0, 15)));
	}

	/** A paragraph boundary ends a sentence even without a full stop. */
	public void testParagraphsAreBoundaries() {
		String text = "Heading without stop\n\nBody text follows here.";
		List<Chunker.Chunk> chunks = Chunker.of(50, 0).chunk(text);
		assertEquals(1, chunks.size());
		assertEquals(text, chunks.get(0).text());
		assertEquals(0, chunks.get(0).locator().page());
	}

	/** A sentence longer than the limit is cut at white space, and nothing is lost or doubled. */
	public void testLongSentenceIsCut() {
		StringBuilder longSentence = new StringBuilder();
		for (int i = 0; i < 60; i++) {
			longSentence.append("word").append(i).append(' ');
		}
		String text = longSentence.toString().trim() + ".";
		List<Chunker.Chunk> chunks = Chunker.of(100, 1).chunk(text);
		assertTrue(chunks.size() > 3);
		StringBuilder joined = new StringBuilder();
		for (Chunker.Chunk chunk : chunks) {
			assertTrue(chunk.text().length() <= 100);
			assertFalse("Cut at white space: " + chunk.text(), chunk.text().startsWith(" "));
			joined.append(chunk.text()).append(' ');
		}
		assertTrue(joined.toString().contains("word0 word1"));
		assertTrue(joined.toString().contains("word59."));
	}

	/** An empty or blank text has no chunk. */
	public void testBlankText() {
		assertTrue(Chunker.of(100, 1).chunk("  \n ").isEmpty());
		assertTrue(Chunker.of(100, 1).chunk(null).isEmpty());
	}

	/**
	 * A passage the model quotes from a chunk it was shown - with the line break of the extracted
	 * file as a space - passes the citation check against that chunk.
	 */
	public void testQuoteFromAChunkPassesTheCitationCheck() {
		List<Chunker.Chunk> chunks = new Chunker(200, 1, PAGE).chunk(TEXT);
		Chunker.Chunk chunk = find(chunks, "Der Kunde wünscht");
		assertTrue(Finding.occursIn(chunk.text(), "Der Kunde wünscht eine kürzere Lieferzeit."));
		assertFalse(Finding.occursIn(chunk.text(), "The base plate is made of steel."));
	}

	private static Chunker.Chunk find(List<Chunker.Chunk> chunks, String words) {
		for (Chunker.Chunk chunk : chunks) {
			if (chunk.text().contains(words)) {
				return chunk;
			}
		}
		throw new AssertionError("No chunk contains " + words);
	}

	private static int pageOf(List<Chunker.Chunk> chunks, String words) {
		return find(chunks, words).locator().page();
	}

}
