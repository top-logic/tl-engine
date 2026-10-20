/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.citation;

import junit.framework.TestCase;

/**
 * Which addresses {@link SeenUrls} takes from a text, and which addresses count as the same.
 */
public class TestSeenUrls extends TestCase {

	/** Scheme and host case, the default port, the fragment and an empty path do not matter. */
	public void testTheSameAddressInAnotherSpelling() {
		SeenUrls seen = new SeenUrls();
		seen.add("HTTPS://Example.COM");

		assertTrue(seen.contains("https://example.com/"));
		assertTrue(seen.contains("https://example.com:443/#top"));
		assertFalse("http is another address", seen.contains("http://example.com/"));
	}

	/** Path and query are compared exactly: an added parameter makes another address. */
	public void testAnAddedParameterIsAnotherAddress() {
		SeenUrls seen = new SeenUrls();
		seen.add("https://example.com/report?id=7");

		assertTrue(seen.contains("https://example.com/report?id=7"));
		assertFalse(seen.contains("https://example.com/report?id=7&notes=secret"));
		assertFalse(seen.contains("https://example.com/report"));
		assertFalse(seen.contains("https://example.com/Report?id=7"));
	}

	/** Addresses in running text and Markdown, without the punctuation around them. */
	public void testAddressesInText() {
		SeenUrls seen = new SeenUrls();
		seen.addAllIn("Read https://a.example/one. Then [two](https://b.example/wiki/Foo_(bar)) and "
			+ "(see https://c.example/x), \"https://d.example/q?a=1&b=2\" or <https://e.example/>.");

		assertTrue(seen.contains("https://a.example/one"));
		assertTrue("a balanced parenthesis belongs to the address",
			seen.contains("https://b.example/wiki/Foo_(bar)"));
		assertTrue(seen.contains("https://c.example/x"));
		assertTrue(seen.contains("https://d.example/q?a=1&b=2"));
		assertTrue(seen.contains("https://e.example/"));
		assertFalse(seen.contains("https://a.example/one."));
	}

	/** What is no http(s) address is neither added nor found. */
	public void testOtherSchemesAreNoAddresses() {
		SeenUrls seen = new SeenUrls();
		seen.add("file:///etc/passwd");
		seen.add("not a url");

		assertFalse(seen.contains("file:///etc/passwd"));
		assertFalse(seen.contains("not a url"));
	}

	/** A message is read once. */
	public void testAMessageIsReadOnce() {
		SeenUrls seen = new SeenUrls();

		assertTrue(seen.firstLook("m1"));
		assertFalse(seen.firstLook("m1"));
		assertTrue(seen.firstLook("m2"));
	}

	/** A sub-agent's addresses join the lead's ledger with its sources. */
	public void testASubagentsAddressesJoinTheLead() {
		ReadingList lead = new ReadingList();
		ReadingList worker = new ReadingList();
		worker.seenUrls().add("https://found.example/by-the-worker");

		lead.addAll(worker);

		assertTrue(lead.seenUrls().contains("https://found.example/by-the-worker"));
	}

	/**
	 * A page shows what was typed into it: an address in a text the run typed is not taken from a
	 * page, while the page's other addresses are.
	 */
	public void testATypedAddressIsNotTakenFromAPage() {
		SeenUrls seen = new SeenUrls();
		seen.typed("see https://collector.example/?d=notes-of-the-run please");

		seen.addAllShownIn("- textbox \"query\": see https://collector.example/?d=notes-of-the-run please\n"
			+ "- link \"next\": https://archive.example/page-2");

		assertFalse(seen.contains("https://collector.example/?d=notes-of-the-run"));
		assertTrue(seen.contains("https://archive.example/page-2"));
		assertTrue(seen.hasTyped());
	}

	/** The user's and the lead's messages are not pages: what they write is given, typed or not. */
	public void testAMessageIsNotFilteredByTypedText() {
		SeenUrls seen = new SeenUrls();
		seen.typed("https://docs.example/manual");

		seen.addAllIn("Read https://docs.example/manual first.");

		assertTrue(seen.contains("https://docs.example/manual"));
	}

	/** What a sub-agent typed is not taken from a page its lead reads either: it may be the same page. */
	public void testASubagentsTypedTextsJoinTheLead() {
		ReadingList lead = new ReadingList();
		ReadingList worker = new ReadingList();
		worker.seenUrls().typed("https://collector.example/?d=x");

		lead.addAll(worker);
		lead.seenUrls().addAllShownIn("value: https://collector.example/?d=x");

		assertTrue(lead.seenUrls().hasTyped());
		assertFalse(lead.seenUrls().contains("https://collector.example/?d=x"));
	}

	/** An address is found in the form a browser requests it. */
	public void testAnAddressIsFoundAsABrowserRequestsIt() {
		SeenUrls seen = new SeenUrls();
		seen.add("https://wiki.example/wiki/Stra\u00dfe/../Br\u00fccke");

		assertTrue(seen.contains("https://wiki.example/wiki/Br%C3%BCcke"));
	}
}
