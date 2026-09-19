/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import junit.framework.TestCase;

/**
 * Tests for {@link HtmlSanitizer}, from the HTML side.
 *
 * <p>
 * {@link TestConverters} pins what a Markdown answer ends up as. These tests pin the filter itself,
 * so that a caller which hands it HTML from anywhere else gets the same guarantees.
 * </p>
 */
public class TestHtmlSanitizer extends TestCase {

	public void testScriptElementIsDropped() {
		String html = HtmlSanitizer.sanitize("<p>before</p><script>alert(1)</script><p>after</p>");

		assertFalse("A script element must not survive: " + html, html.contains("script"));
		assertTrue("The surrounding text must survive: " + html, html.contains("before"));
		assertTrue("The surrounding text must survive: " + html, html.contains("after"));
	}

	public void testEventHandlerIsDropped() {
		String html = HtmlSanitizer.sanitize("<img src=\"https://example.com/a.png\" onerror=\"alert(1)\" alt=\"a\">");

		assertFalse("An event handler must not survive: " + html, html.contains("onerror"));
		assertTrue("The image itself must survive: " + html, html.contains("src=\"https://example.com/a.png\""));
	}

	public void testFrameIsDropped() {
		String html = HtmlSanitizer.sanitize("<iframe src=\"https://evil.example\"></iframe>");

		assertFalse("A frame must not survive: " + html, html.contains("iframe"));
	}

	public void testFormIsDropped() {
		String html = HtmlSanitizer.sanitize(
			"<form action=\"https://evil.example\"><input name=\"p\" type=\"password\"></form>");

		assertFalse("A form must not survive: " + html, html.contains("<form"));
		assertFalse("An input must not survive: " + html, html.contains("<input"));
	}

	public void testStyleIsDropped() {
		String html = HtmlSanitizer.sanitize(
			"<style>body{display:none}</style><p style=\"position:fixed;top:0\">x</p>");

		assertFalse("A style element must not survive: " + html, html.contains("<style"));
		assertFalse("A style attribute must not survive: " + html, html.contains("style="));
		assertTrue("The paragraph must survive: " + html, html.contains("x"));
	}

	public void testScriptUrlLosesItsAttribute() {
		for (String url : new String[] {
			"javascript:alert(1)", "JaVaScRiPt:alert(1)", "vbscript:msgbox(1)",
			"data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==", "file:///etc/passwd"
		}) {
			String html = HtmlSanitizer.sanitize("<a href=\"" + url + "\">x</a>");

			assertFalse("A " + url + " target must not survive: " + html, html.contains("href"));
			assertTrue("The link text must survive: " + html, html.contains("x"));
		}
	}

	public void testMarkdownMarkupSurvives() {
		String source = "<h1>T</h1><h2>t</h2><p><strong>b</strong> <em>i</em> <code>c</code></p>"
			+ "<ul><li>a</li></ul><ol><li>b</li></ol><blockquote><p>q</p></blockquote>"
			+ "<del>d</del><ins>n</ins><sup>s</sup><sub>u</sub><hr>"
			+ "<table><thead><tr><th align=\"left\">h</th></tr></thead>"
			+ "<tbody><tr><td align=\"right\">c</td></tr></tbody></table>"
			+ "<pre><code class=\"language-java\">int x;</code></pre>";
		String html = HtmlSanitizer.sanitize(source);

		for (String kept : new String[] {
			"<h1>", "<h2>", "<strong>", "<em>", "<code", "<ul>", "<ol>", "<li>", "<blockquote>",
			"<del>", "<ins>", "<sup>", "<sub>", "<hr>", "<table>", "<thead>", "<tbody>",
			"align=\"left\"", "align=\"right\"", "<pre>", "language-java"
		}) {
			assertTrue("Markdown markup must survive: " + kept + " missing from " + html, html.contains(kept));
		}
	}

	public void testLinkAttributesSurvive() {
		String html = HtmlSanitizer
			.sanitize("<a href=\"https://example.com\" target=\"_blank\" rel=\"noopener noreferrer\">x</a>");

		assertTrue("Target must survive: " + html, html.contains("target=\"_blank\""));
		assertTrue("Rel must survive: " + html, html.contains("rel=\"noopener noreferrer\""));
	}

	/**
	 * A link into this application stays relative. {@code publishPage} returns a root-relative URL
	 * whenever the deployment declares no {@code %HOST%}, so rewriting it to an absolute one
	 * against the filter's own resolution base would point the reader at the wrong host.
	 */
	public void testRelativeLinkIsNeitherDroppedNorRewritten() {
		String html = HtmlSanitizer.sanitize("<a href=\"/hype/html/a7f3b2\">page</a>");

		assertTrue("A relative link must stay as written: " + html, html.contains("href=\"/hype/html/a7f3b2\""));
		assertFalse("The resolution base must not leak into the output: " + html, html.contains("localhost"));
	}

	public void testWhitespaceIsNotReflowed() {
		String html = HtmlSanitizer.sanitize("<p>a<br>b</p>");

		assertEquals("Pretty-printing would change the spacing the reader sees.", "<p>a<br>b</p>", html);
	}

	public void testNullAndEmptyInput() {
		assertNull(HtmlSanitizer.sanitize(null));
		assertEquals("", HtmlSanitizer.sanitize(""));
	}

}
