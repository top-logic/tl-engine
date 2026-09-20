/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import junit.framework.TestCase;

/**
 * Tests for {@link PdfContent}.
 */
public class TestPdfContent extends TestCase {

	/** Null data is rejected. */
	@SuppressWarnings("unused")
	public void testNullDataRejected() {
		try {
			new PdfContent(null, "application/pdf");
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/** Empty data is rejected. */
	@SuppressWarnings("unused")
	public void testEmptyDataRejected() {
		try {
			new PdfContent("", "application/pdf");
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected
		}
	}

	/** Null mediaType defaults to application/pdf. */
	public void testNullMediaTypeDefaults() {
		PdfContent pdf = new PdfContent("base64data", null);
		assertEquals("application/pdf", pdf.mediaType());
	}

}
