/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import junit.framework.TestCase;

/**
 * Tests for {@link Converters}, especially that raw HTML in markdown is escaped and does not
 * reach the browser as active markup (stored XSS via LLM/tool output).
 */
public class TestConverters extends TestCase {

	public void testScriptTagIsEscaped() {
		String html = Converters.markdownToHtmlString("hello <script>alert(1)</script> world");

		assertFalse("Script tag must not pass through: " + html, html.contains("<script>"));
		assertTrue("Script tag must be escaped: " + html, html.contains("&lt;script&gt;"));
	}

	public void testEventHandlerAttributeIsEscaped() {
		String html = Converters.markdownToHtmlString("<img src=x onerror=alert(1)>");

		assertFalse("Inline HTML must not pass through: " + html, html.contains("<img"));
		assertTrue("Inline HTML must be escaped: " + html, html.contains("&lt;img"));
	}

	public void testHtmlBlockIsEscaped() {
		String html = Converters.markdownToHtmlString(
			"<div onclick=\"alert(1)\">\n\n<iframe src=\"https://evil.example\"></iframe>");

		assertFalse("HTML block must not pass through: " + html, html.contains("<div"));
		assertFalse("HTML block must not pass through: " + html, html.contains("<iframe"));
	}

	public void testHtmlInNestedMarkdownBlockIsEscaped() {
		String html = Converters.markdownToHtmlString(
			"```markdown\n# Heading\n\n<script>alert(1)</script>\n```");

		assertFalse("Script in nested markdown must not pass through: " + html, html.contains("<script>"));
		assertTrue("Nested markdown formatting must still render: " + html, html.contains("<h1>"));
	}

	public void testLegitimateMarkdownStillRenders() {
		String html = Converters.markdownToHtmlString(
			"# Title\n\n- item **bold**\n\n```java\nint x = 1;\n```\n\n[link](https://example.com)");

		assertTrue("Heading must render: " + html, html.contains("<h1>Title</h1>"));
		assertTrue("List must render: " + html, html.contains("<li>"));
		assertTrue("Bold must render: " + html, html.contains("<strong>bold</strong>"));
		assertTrue("Code block must render: " + html, html.contains("<code"));
		assertTrue("Link must render: " + html, html.contains("<a href=\"https://example.com\""));
		assertTrue("Links must open in a new tab: " + html, html.contains("target=\"_blank\""));
	}

	public void testCodeBlockContentStaysLiteral() {
		String html = Converters.markdownToHtmlString("```html\n<b>bold</b>\n```");

		assertTrue("Code block content must be escaped as before: " + html, html.contains("&lt;b&gt;"));
	}
}
