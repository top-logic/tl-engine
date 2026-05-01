/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import junit.framework.TestCase;

/**
 * Tests for {@link ThinkingContent}.
 */
public class TestThinkingContent extends TestCase {

	/** ThinkingContent stores text. */
	public void testFields() {
		ThinkingContent content = new ThinkingContent("reasoning step");
		assertEquals("reasoning step", content.text());
	}

	/** Null text is rejected. */
	@SuppressWarnings("unused")
	public void testNullTextRejected() {
		try {
			new ThinkingContent(null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

}
