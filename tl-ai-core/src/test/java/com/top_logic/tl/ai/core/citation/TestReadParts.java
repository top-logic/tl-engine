/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import junit.framework.TestCase;

/**
 * What a run read of a document read in windows ({@link ReadingList#readPart}).
 */
public class TestReadParts extends TestCase {

	private static final String DOCUMENT = "AAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD";

	/** Windows read in order make up the document read so far, with the unread rest marked. */
	public void testWindowsInOrder() {
		ReadingList list = new ReadingList();

		assertEquals("AAAAAAAAAA\n\n[...]", list.readPart("https://a.example/", DOCUMENT, 0, 10));
		assertEquals("AAAAAAAAAABBBBBBBBBB\n\n[...]", list.readPart("https://a.example/", DOCUMENT, 10, 20));
		assertEquals(DOCUMENT, list.readPart("https://a.example/", DOCUMENT, 20, 40));
	}

	/** A part that was skipped is marked where it was left out; overlapping windows are read once. */
	public void testAGapAndAnOverlap() {
		ReadingList list = new ReadingList();
		list.readPart("https://a.example/", DOCUMENT, 30, 40);
		list.readPart("https://a.example/", DOCUMENT, 0, 12);

		assertEquals("AAAAAAAAAABB\n\n[...]\n\nDDDDDDDDDD", list.readPart("https://a.example/", DOCUMENT, 5, 10));
		assertEquals("AAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD",
			list.readPart("https://a.example/", DOCUMENT, 8, 33));
	}

	/** Read again with another text, the positions mean nothing any more: the parts start over. */
	public void testAChangedDocumentStartsOver() {
		ReadingList list = new ReadingList();
		list.readPart("https://a.example/", DOCUMENT, 0, 40);

		assertEquals("[...]\n\nzz", list.readPart("https://a.example/", "xxxxxxxxzz", 8, 10));
	}

	/** Each document has parts of its own, found by its address in any spelling. */
	public void testPartsPerDocument() {
		ReadingList list = new ReadingList();
		list.readPart("https://a.example/", DOCUMENT, 0, 10);

		assertEquals("[...]\n\nDDDDDDDDDD", list.readPart("https://b.example/", DOCUMENT, 30, 40));
		assertEquals("AAAAAAAAAABBBBBBBBBB\n\n[...]", list.readPart("HTTPS://A.EXAMPLE/", DOCUMENT, 10, 20));
	}
}
