/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.List;

import junit.framework.TestCase;

/**
 * Tests for {@link GoogleSearchEngine#parseResponse(String)}.
 */
public class TestGoogleSearchEngine extends TestCase {

	public void testParseItems() {
		String json = "{\"kind\":\"customsearch#search\",\"items\":["
			+ "{\"title\":\"T1\",\"link\":\"https://a.example/1\",\"snippet\":\"snippet one\"},"
			+ "{\"title\":\"T2\",\"link\":\"https://b.example/2\",\"snippet\":\"snippet two\"}"
			+ "]}";

		WebSearchResults results = GoogleSearchEngine.parseResponse(json);

		assertNull("Google supplies no synthesized answer.", results.answer());
		List<WebSearchHit> hits = results.hits();
		assertEquals(2, hits.size());
		assertEquals("T1", hits.get(0).title());
		assertEquals("https://a.example/1", hits.get(0).url());
		assertEquals("snippet one", hits.get(0).snippet());
		assertNull(hits.get(0).content());
		assertEquals("https://b.example/2", hits.get(1).url());
	}

	public void testParseAbsentItems() {
		WebSearchResults results = GoogleSearchEngine.parseResponse(
			"{\"kind\":\"customsearch#search\",\"searchInformation\":{\"totalResults\":\"0\"}}");
		assertNull(results.answer());
		assertTrue(results.hits().isEmpty());
	}

	public void testParseEmptyItems() {
		WebSearchResults results = GoogleSearchEngine.parseResponse("{\"items\":[]}");
		assertNull(results.answer());
		assertTrue(results.hits().isEmpty());
	}

	public void testParseMissingFields() {
		String json = "{\"items\":["
			+ "{\"link\":\"https://a.example/1\"},"
			+ "{\"title\":\"T2\",\"link\":\"https://b.example/2\",\"snippet\":\"\"}"
			+ "]}";

		WebSearchResults results = GoogleSearchEngine.parseResponse(json);

		List<WebSearchHit> hits = results.hits();
		assertEquals(2, hits.size());
		assertEquals("Absent title maps to empty string (msgbuf default).", "", hits.get(0).title());
		assertEquals("https://a.example/1", hits.get(0).url());
		assertNull("Absent snippet maps to null.", hits.get(0).snippet());
		assertNull("Empty snippet maps to null.", hits.get(1).snippet());
	}
}
