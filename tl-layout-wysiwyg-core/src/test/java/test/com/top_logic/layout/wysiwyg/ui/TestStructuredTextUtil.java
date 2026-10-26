/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.wysiwyg.ui;

import junit.framework.TestCase;

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

}
