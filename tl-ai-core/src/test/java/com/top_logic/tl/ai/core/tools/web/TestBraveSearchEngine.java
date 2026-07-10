/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.List;

import junit.framework.TestCase;

/**
 * Tests for {@link BraveSearchEngine#parseResponse(String)}.
 */
public class TestBraveSearchEngine extends TestCase {

	public void testParseResults() {
		String json = "{\"query\":{\"original\":\"q\"},\"web\":{\"results\":["
			+ "{\"title\":\"T1\",\"url\":\"https://a.example/1\",\"description\":\"snippet one\"},"
			+ "{\"title\":\"T2\",\"url\":\"https://b.example/2\",\"description\":\"snippet two\"}"
			+ "]}}";

		WebSearchResults results = BraveSearchEngine.parseResponse(json);

		assertNull("Brave supplies no synthesized answer.", results.answer());
		List<WebSearchHit> hits = results.hits();
		assertEquals(2, hits.size());
		assertEquals("T1", hits.get(0).title());
		assertEquals("https://a.example/1", hits.get(0).url());
		assertEquals("snippet one", hits.get(0).snippet());
		assertNull(hits.get(0).content());
		assertEquals("snippet two", hits.get(1).snippet());
	}

	public void testParseEmptyResults() {
		WebSearchResults results = BraveSearchEngine.parseResponse("{\"web\":{\"results\":[]}}");
		assertNull(results.answer());
		assertTrue(results.hits().isEmpty());
	}

	public void testParseMissingWebSection() {
		WebSearchResults results = BraveSearchEngine.parseResponse("{\"query\":{\"original\":\"q\"}}");
		assertNull(results.answer());
		assertTrue(results.hits().isEmpty());
	}

	public void testParseMissingFields() {
		String json = "{\"web\":{\"results\":["
			+ "{\"url\":\"https://a.example/1\"},"
			+ "{\"title\":\"T2\",\"url\":\"https://b.example/2\",\"description\":\"\"}"
			+ "]}}";

		WebSearchResults results = BraveSearchEngine.parseResponse(json);

		List<WebSearchHit> hits = results.hits();
		assertEquals(2, hits.size());
		assertEquals("Absent title maps to empty string (msgbuf default).", "", hits.get(0).title());
		assertEquals("https://a.example/1", hits.get(0).url());
		assertNull("Absent description maps to null snippet.", hits.get(0).snippet());
		assertNull("Empty description maps to null snippet.", hits.get(1).snippet());
	}
}
