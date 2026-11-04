/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.wysiwyg.ui;

import java.io.StringReader;
import java.util.Base64;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

import com.top_logic.basic.io.binary.BinaryDataFactory;

import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextUtil;

/**
 * Test for {@link StructuredTextUtil#fromCommonMark(CharSequence)}.
 *
 * @author <a href="mailto:bernhard.haumacher@top-logic.com">Bernhard Haumacher</a>
 */
public class TestStructuredTextUtil extends TestCase {

	public void testPlainText() {
		StructuredText text = StructuredTextUtil.fromCommonMark("Hello World");

		assertFalse(text.isEmpty());
		assertTrue(text.getSourceCode().contains("Hello World"));
	}

	public void testEmptyIsEmpty() {
		assertTrue(StructuredTextUtil.fromCommonMark("").isEmpty());
	}

	public void testNull() {
		assertNull(StructuredTextUtil.fromCommonMark(null));
	}

	public void testRawHtmlIsEscaped() {
		StructuredText text = StructuredTextUtil.fromCommonMark("<script>alert('x')</script>");

		// The raw HTML must be escaped, not passed through as active markup.
		assertFalse("Raw HTML must not survive the conversion.", text.getSourceCode().contains("<script>"));
	}

	public void testMarkdown() {
		StructuredText text = StructuredTextUtil.fromCommonMark("*emphasis*");

		assertTrue(text.getSourceCode().contains("<em>"));
	}

	/**
	 * An image of a text written as HTML, with its void elements unclosed as the editor writes
	 * them, is inlined, and the result is well-formed XHTML.
	 */
	public void testInlinedImagesOfHtml() throws Exception {
		StructuredText text = new StructuredText(
			"<p>Line<br>break <img src=\"ref:pic.png\" alt=\"Pic\"></p><p>after</p>",
			Map.of("pic.png", BinaryDataFactory.createBinaryData(new byte[] { 1, 2, 3 }, "image/png", "pic.png")));

		String html = StructuredTextUtil.getCodeWithInlinedImages(text);

		assertTrue(html, html.contains("src=\"data:image/png;base64," + Base64.getEncoder().encodeToString(new byte[] { 1, 2, 3 }) + "\""));
		assertTrue(html, html.contains("<p>after</p>"));
		DocumentBuilderFactory.newInstance().newDocumentBuilder()
			.parse(new InputSource(new StringReader("<div>" + html + "</div>")));
	}

}
