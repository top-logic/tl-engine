/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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

	/** ImageContent is a ContentPart. */
	public void testIsContentPart() {
		ContentPart part = new ImageContent("https://example.com/img.png", "image/png");
		assertTrue(part instanceof ImageContent);
	}

	/** Null url is rejected. */
	public void testNullUrlRejected() {
		try {
			new ImageContent(null, "image/png");
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/** Null mediaType is rejected. */
	public void testNullMediaTypeRejected() {
		try {
			new ImageContent("https://example.com/img.png", null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}
}
