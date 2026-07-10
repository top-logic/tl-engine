/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.net.URI;

import junit.framework.TestCase;

/**
 * Tests for {@link WebFetchTool} URL validation and HTML extraction.
 */
public class TestWebFetchTool extends TestCase {

	public void testRejectsNonHttpScheme() {
		assertNotNull(WebFetchTool.validateUrl(URI.create("ftp://example.com/x")));
		assertNotNull(WebFetchTool.validateUrl(URI.create("file:///etc/passwd")));
	}

	public void testRejectsCredentialsInUrl() {
		assertNotNull(WebFetchTool.validateUrl(URI.create("https://user:pass@example.com/")));
	}

	public void testRejectsLoopbackAndPrivate() {
		assertNotNull(WebFetchTool.validateUrl(URI.create("http://127.0.0.1/")));
		assertNotNull(WebFetchTool.validateUrl(URI.create("http://localhost/")));
		assertNotNull(WebFetchTool.validateUrl(URI.create("http://10.0.0.1/")));
		assertNotNull(WebFetchTool.validateUrl(URI.create("http://192.168.1.1/")));
		assertNotNull(WebFetchTool.validateUrl(URI.create("http://169.254.169.254/")));
	}

	public void testHtmlToMarkdownStripsTagsAndScripts() {
		String html = "<html><head><title>x</title><style>.a{}</style></head>"
			+ "<body><nav>menu</nav><h1>Title</h1><p>Hello <b>world</b>.</p>"
			+ "<script>evil()</script><ul><li>one</li><li>two</li></ul></body></html>";
		String markdown = WebFetchTool.htmlToMarkdown(html, "https://example.com");

		assertTrue("heading as ATX", markdown.contains("# Title"));
		assertTrue("bold preserved", markdown.contains("**world**"));
		assertTrue("list items as bullets",
			markdown.contains("* one") && markdown.contains("* two"));
		assertFalse("no script body", markdown.contains("evil"));
		assertFalse("no style body", markdown.contains(".a{}"));
		assertFalse("no nav", markdown.contains("menu"));
	}

	public void testHtmlToMarkdownResolvesRelativeLinks() {
		String html = "<html><body><p>See <a href=\"/docs/page\">the docs</a>.</p></body></html>";
		String markdown = WebFetchTool.htmlToMarkdown(html, "https://example.com/start");

		assertTrue("absolute link in Markdown",
			markdown.contains("[the docs](https://example.com/docs/page)"));
	}
}
