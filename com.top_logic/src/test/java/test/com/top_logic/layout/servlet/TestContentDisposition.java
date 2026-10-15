/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.servlet;

import junit.framework.TestCase;

import com.top_logic.layout.servlet.ContentDisposition;

/**
 * Test for {@link ContentDisposition}: the file name arrives intact, in whatever characters it is
 * written, and cannot break the header.
 */
public class TestContentDisposition extends TestCase {

	/**
	 * Tests a plain ASCII name.
	 */
	public void testAsciiName() {
		assertEquals("attachment; filename=\"report.xlsx\"; filename*=UTF-8''report.xlsx",
			ContentDisposition.attachment("report.xlsx"));
	}

	/**
	 * Tests a name with characters beyond ASCII, a space and a quote: encoded in UTF-8 for
	 * {@code filename*}, replaced in the plain {@code filename}.
	 */
	public void testNonAsciiName() {
		assertEquals("attachment; filename=\"_bersicht _1_ 2026.xlsx\"; "
			+ "filename*=UTF-8''%C3%9Cbersicht%20%221%22%202026.xlsx",
			ContentDisposition.attachment("Übersicht \"1\" 2026.xlsx"));
	}

	/**
	 * Tests the disposition of a file displayed in the browser.
	 */
	public void testInline() {
		assertTrue(ContentDisposition.inline("a.pdf").startsWith("inline; filename=\"a.pdf\""));
	}

}
