/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.List;

import junit.framework.TestCase;

/**
 * Tests for {@link TavilySearchEngine#parseResponse(String)}.
 */
public class TestTavilySearchEngine extends TestCase {

	public void testParseResultsAndAnswer() {
		String json = "{\"query\":\"q\",\"answer\":\"The answer.\",\"results\":["
			+ "{\"title\":\"T1\",\"url\":\"https://a.example/1\",\"content\":\"snippet one\"},"
			+ "{\"title\":\"T2\",\"url\":\"https://b.example/2\",\"content\":\"snippet two\",\"raw_content\":\"full body\"}"
			+ "],\"response_time\":1.2}";

		WebSearchResults results = TavilySearchEngine.parseResponse(json);

		assertEquals("The answer.", results.answer());
		List<WebSearchHit> hits = results.hits();
		assertEquals(2, hits.size());
		assertEquals("T1", hits.get(0).title());
		assertEquals("https://a.example/1", hits.get(0).url());
		assertEquals("snippet one", hits.get(0).snippet());
		assertNull(hits.get(0).content());
		assertEquals("full body", hits.get(1).content());
	}

	public void testParseEmptyResults() {
		WebSearchResults results = TavilySearchEngine.parseResponse("{\"results\":[]}");
		assertNull(results.answer());
		assertTrue(results.hits().isEmpty());
	}
}
