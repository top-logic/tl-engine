/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.format;

import java.io.IOException;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.form.format.CommonMark;

/**
 * Test case for {@link CommonMark}.
 */
@SuppressWarnings("javadoc")
public class TestCommonMark extends TestCase {

	public void testMarkup() throws IOException {
		String html = write("A **bold** word.");
		assertTrue(html, html.contains("<strong>bold</strong>"));
	}

	public void testHtmlIsEscaped() throws IOException {
		String html = write("Text <script>alert(1)</script>");
		assertFalse(html, html.contains("<script"));
	}

	public void testScriptLinksAreDropped() throws IOException {
		String html = write("[link](javascript:alert(1)) ![image](javascript:alert(2))");
		assertFalse(html, html.contains("javascript:"));
		assertTrue("The link itself is kept: " + html, html.contains(">link</a>"));
	}

	public void testOrdinaryLinksAreKept() throws IOException {
		String html = write("[link](https://www.top-logic.com/)");
		assertTrue(html, html.contains("href=\"https://www.top-logic.com/\""));
	}

	public void testToHtmlIsTheWrittenFragment() throws IOException {
		String text = "# Title\n\nA [link](javascript:alert(1)) and *more* <b>text</b>.";
		assertEquals(write(text), CommonMark.toHtml(text));
	}

	private static String write(String text) throws IOException {
		TagWriter out = new TagWriter();
		CommonMark.writeCommonMark(out, text);
		return out.toString();
	}

}
