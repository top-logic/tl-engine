/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import junit.framework.TestCase;

/**
 * Tests for {@link ThinkingContent}.
 */
public class TestThinkingContent extends TestCase {

	/** ThinkingContent stores text and continuity token. */
	public void testFields() {
		ThinkingContent content = new ThinkingContent("reasoning step", "sig123");
		assertEquals("reasoning step", content.text());
		assertEquals("sig123", content.continuityToken());
	}

	/** Continuity token may be null. */
	public void testNullToken() {
		ThinkingContent content = new ThinkingContent("reasoning step", null);
		assertEquals("reasoning step", content.text());
		assertNull(content.continuityToken());
	}

	/** Null text is rejected. */
	@SuppressWarnings("unused")
	public void testNullTextRejected() {
		try {
			new ThinkingContent(null, null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}
}
