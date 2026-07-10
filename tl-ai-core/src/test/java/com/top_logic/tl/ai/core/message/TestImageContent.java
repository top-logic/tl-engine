/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import junit.framework.TestCase;

/**
 * Tests for {@link ImageContent}.
 */
public class TestImageContent extends TestCase {

	/** ImageContent stores url and mediaType. */
	public void testFields() {
		ImageContent img = new ImageContent("https://example.com/img.png", "image/png");
		assertEquals("https://example.com/img.png", img.url());
		assertEquals("image/png", img.mediaType());
	}

	/** Null url is rejected. */
	@SuppressWarnings("unused")
	public void testNullUrlRejected() {
		try {
			new ImageContent(null, "image/png");
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/** Null mediaType is rejected. */
	@SuppressWarnings("unused")
	public void testNullMediaTypeRejected() {
		try {
			new ImageContent("https://example.com/img.png", null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}
}
