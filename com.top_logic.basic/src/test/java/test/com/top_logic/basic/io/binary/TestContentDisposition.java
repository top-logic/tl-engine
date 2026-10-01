/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary;

import junit.framework.TestCase;

import com.top_logic.basic.io.binary.ContentDisposition;

/**
 * Test of {@link ContentDisposition}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestContentDisposition extends TestCase {

	public void testNoName() {
		assertEquals("inline", ContentDisposition.headerValue(ContentDisposition.INLINE, null));
		assertEquals("attachment", ContentDisposition.headerValue(ContentDisposition.ATTACHMENT, ""));
	}

	public void testAsciiName() {
		assertEquals("inline; filename=\"report-2026.pdf\"; filename*=UTF-8''report-2026.pdf",
			ContentDisposition.headerValue(ContentDisposition.INLINE, "report-2026.pdf"));
	}

	public void testNonAsciiName() {
		assertEquals(
			"attachment; filename=\"_bersicht M_rz.pdf\"; filename*=UTF-8''%C3%9Cbersicht%20M%C3%A4rz.pdf",
			ContentDisposition.headerValue(ContentDisposition.ATTACHMENT, "Übersicht März.pdf"));
	}

	public void testSurrogatePair() {
		// One code point outside the BMP is replaced by a single fallback character.
		assertEquals("a_b", ContentDisposition.asciiFallback("a😀b"));
		assertEquals("a%F0%9F%98%80b", ContentDisposition.encodeRfc5987("a😀b"));
	}

	public void testSpecialCharacters() {
		assertEquals("a_b_c_d_.txt", ContentDisposition.asciiFallback("a\"b\\c%d\n.txt"));
		assertEquals("a%22b%5Cc%25d%3B%2C%27%2A.txt", ContentDisposition.encodeRfc5987("a\"b\\c%d;,'*.txt"));
		assertEquals("!#$&+-.^_`|~", ContentDisposition.encodeRfc5987("!#$&+-.^_`|~"));
	}

}
