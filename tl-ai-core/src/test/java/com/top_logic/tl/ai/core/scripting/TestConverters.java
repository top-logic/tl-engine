/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import java.io.StringWriter;

import com.top_logic.basic.xml.TagWriter;

import junit.framework.TestCase;

/**
 * Tests for {@link Converters}.
 *
 * <p>
 * The text these functions render is model output, and a model writes down what {@code webFetch}
 * and the tool results put in front of it. The HTML they produce is written into the chat page and
 * is persisted with the conversation, so markup that arrives in that text must never become active
 * markup - not on the first render and not on any reopen. Two layers answer for that: the renderer
 * escapes raw HTML, and {@link HtmlSanitizer} filters what the renderer itself produced. The tests
 * here pin the result of both, from the Markdown side.
 * </p>
 */
public class TestConverters extends TestCase {

	public void testScriptTagIsEscaped() {
		String html = Converters.markdownToHtmlString("hello <script>alert(1)</script> world");

		assertFalse("Script tag must not pass through: " + html, html.contains("<script"));
		assertTrue("Script tag must be escaped: " + html, html.contains("&lt;script&gt;"));
	}

	public void testEventHandlerAttributeIsEscaped() {
		String html = Converters.markdownToHtmlString("<img src=x onerror=alert(1)>");

		// The event handler is still in the output, but as text of a paragraph: it is escaped
		// along with the tag it was written on, so no element ever carries it.
		assertFalse("Inline HTML must not pass through: " + html, html.contains("<img"));
		assertTrue("Inline HTML must be escaped: " + html, html.contains("&lt;img"));
		assertEquals("<p>&lt;img src=x onerror=alert(1)&gt;</p>", html.trim());
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

		assertFalse("Script in nested markdown must not pass through: " + html, html.contains("<script"));
		assertTrue("Nested markdown formatting must still render: " + html, html.contains("<h1>"));
	}

	/**
	 * A Markdown link whose target is a script URL. Escaping raw HTML does not catch this one -
	 * the brackets are Markdown, and the renderer writes the target into an {@code href} itself.
	 */
	public void testJavaScriptLinkLosesItsTarget() {
		assertNoHref("[click](javascript:alert(1))");
	}

	/**
	 * The same, spelled so that a case-sensitive check misses it. Browsers match a URL scheme
	 * without regard to case.
	 */
	public void testMixedCaseJavaScriptLinkLosesItsTarget() {
		assertNoHref("[click](JaVaScRiPt:alert(1))");
	}

	public void testVbScriptLinkLosesItsTarget() {
		assertNoHref("[click](vbscript:msgbox(1))");
	}

	public void testDataLinkLosesItsTarget() {
		assertNoHref("[click](data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==)");
	}

	public void testDataImageLosesItsSource() {
		String html = Converters.markdownToHtmlString(
			"![x](data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg==)");

		assertFalse("An image must not carry a data: source: " + html, html.contains("data:"));
		assertFalse("An image must not carry a data: source: " + html, html.contains("src="));
	}

	public void testAutolinkToScriptUrlStaysText() {
		String html = Converters.markdownToHtmlString("<javascript:alert(1)>");

		assertFalse("An autolink to a script URL must not become a link: " + html, html.contains("href"));
	}

	private void assertNoHref(String markdown) {
		String html = Converters.markdownToHtmlString(markdown);

		assertFalse("A link to a script URL must lose its target: " + html, html.contains("href"));
		assertFalse("A link to a script URL must lose its target: " + html, html.contains("javascript"));
		assertFalse("A link to a script URL must lose its target: " + html, html.contains("vbscript"));
		assertTrue("The link text must survive as text: " + html, html.contains("click"));
	}

	public void testLegitimateMarkdownStillRenders() {
		String html = Converters.markdownToHtmlString(
			"# Title\n\n- item **bold**\n\n```java\nint x = 1;\n```\n\n[link](https://example.com)");

		assertTrue("Heading must render: " + html, html.contains("<h1>Title</h1>"));
		assertTrue("List must render: " + html, html.contains("<li>"));
		assertTrue("Bold must render: " + html, html.contains("<strong>bold</strong>"));
		assertTrue("Code block must render: " + html, html.contains("<code"));
		assertTrue("Code language must survive: " + html, html.contains("language-java"));
		assertTrue("Link must render: " + html, html.contains("<a href=\"https://example.com\""));
		assertTrue("Links must open in a new tab: " + html, html.contains("target=\"_blank\""));
		assertTrue("Links must not hand over the opener: " + html, html.contains("rel=\"noopener noreferrer\""));
	}

	public void testTableRendersWithItsAlignment() {
		String html = Converters.markdownToHtmlString(
			"| left | right |\n|:-----|------:|\n| a | 1 |\n");

		assertTrue("Table must render: " + html, html.contains("<table>"));
		assertTrue("Header cell must render: " + html, html.contains("<th"));
		assertTrue("Body cell must render: " + html, html.contains("<td"));
		assertTrue("Column alignment must survive: " + html, html.contains("align=\"left\""));
		assertTrue("Column alignment must survive: " + html, html.contains("align=\"right\""));
	}

	public void testExtensionMarkupRenders() {
		String html = Converters.markdownToHtmlString("~~gone~~ ++new++ x^2^\n\n---\n");

		assertTrue("Strikethrough must render: " + html, html.contains("<del>gone</del>"));
		assertTrue("Insert must render: " + html, html.contains("<ins>new</ins>"));
		assertTrue("Superscript must render: " + html, html.contains("<sup>2</sup>"));
		assertTrue("Thematic break must render: " + html, html.contains("<hr"));
	}

	public void testEmojiRendersAsACharacter() {
		String html = Converters.markdownToHtmlString(":smile:");

		assertFalse("An emoji must not render as an image this application does not serve: " + html,
			html.contains("<img"));
		assertFalse("An emoji must not render as its shortcut: " + html, html.contains(":smile:"));
	}

	/**
	 * A root-relative link has to survive: {@code publishPage} returns one whenever the deployment
	 * declares no {@code %HOST%}, and the model puts it into its answer.
	 */
	public void testRootRelativeLinkSurvives() {
		String html = Converters.markdownToHtmlString("[report](/hype/html/a7f3b2)");

		assertTrue("A link into this application must survive: " + html,
			html.contains("href=\"/hype/html/a7f3b2\""));
	}

	public void testAnchorLinkSurvives() {
		String html = Converters.markdownToHtmlString("[see below](#details)");

		assertTrue("An anchor into the answer must survive: " + html, html.contains("href=\"#details\""));
	}

	public void testMailtoLinkSurvives() {
		String html = Converters.markdownToHtmlString("[mail](mailto:info@example.com)");

		assertTrue("A mailto link must survive: " + html, html.contains("href=\"mailto:info@example.com\""));
	}

	public void testImageFromTheWebSurvives() {
		String html = Converters.markdownToHtmlString("![chart](https://example.com/chart.png)");

		assertTrue("An https image must survive: " + html, html.contains("src=\"https://example.com/chart.png\""));
	}

	/**
	 * A captured screenshot is shown by a root-relative image (issue #216), so the whole
	 * Markdown-to-HTML path - renderer and filter - has to keep one.
	 */
	public void testRootRelativeImageSurvives() {
		String html = Converters.markdownToHtmlString("![page.png](/hype/screenshot/a7f3b2)");

		assertTrue("A screenshot of this application must survive: " + html,
			html.contains("src=\"/hype/screenshot/a7f3b2\""));
		assertTrue("with the alt text that says what it was: " + html, html.contains("page.png"));
	}

	public void testCodeBlockContentStaysLiteral() {
		String html = Converters.markdownToHtmlString("```html\n<b>bold</b>\n```");

		assertTrue("Code block content must be escaped as before: " + html, html.contains("&lt;b&gt;"));
	}

	public void testCodeBlockKeepsItsLineBreaks() {
		String html = Converters.markdownToHtmlString("```java\nint x = 1;\nint y = 2;\n```");

		assertTrue("The sanitizer must not re-indent a code block: " + html,
			html.contains("int x = 1;\nint y = 2;"));
	}

	/**
	 * What {@code ChatControl} writes into the page: the fragment the control renders has to carry
	 * the sanitized HTML, not the renderer's raw output.
	 */
	public void testControlFragmentWritesSanitizedHtml() throws Exception {
		StringWriter buffer = new StringWriter();
		try (TagWriter out = new TagWriter(buffer)) {
			Converters.markdownToHtml("[click](javascript:alert(1))\n\n<script>alert(2)</script>")
				.write(null, out);
		}
		String written = buffer.toString();

		assertFalse("The control must not write a script URL into the page: " + written,
			written.contains("javascript:"));
		assertFalse("The control must not write a script tag into the page: " + written,
			written.contains("<script"));
	}

	public void testNullAndEmptyInput() {
		assertEquals("", Converters.markdownToHtmlString(""));
	}

}
