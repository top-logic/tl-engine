/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import com.top_logic.tl.ai.tool.api.ToolResult;

import junit.framework.TestCase;

/**
 * Tests for {@link ToolResult}.
 */
public class TestToolResult extends TestCase {

	/**
	 * {@link ToolResult#of(String)} creates a result with no summary and default truncation hint.
	 */
	public void testOfSetsDefaults() {
		ToolResult result = ToolResult.of("hello");
		assertEquals("hello", result.text());
		assertNull(result.summary());
		assertEquals(-1, result.truncationHint());
	}

	/**
	 * Full constructor preserves all fields.
	 */
	public void testFullConstructor() {
		ToolResult result = new ToolResult("data", "short version", 500);
		assertEquals("data", result.text());
		assertEquals("short version", result.summary());
		assertEquals(500, result.truncationHint());
	}

	/**
	 * Null text is rejected.
	 */
	public void testNullTextThrows() {
		try {
			ToolResult.of(null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			assertTrue(e.getMessage().contains("Text"));
		}
	}

	/**
	 * Null summary is allowed.
	 */
	public void testNullSummaryAllowed() {
		ToolResult result = new ToolResult("data", null, -1);
		assertNull(result.summary());
	}

	/**
	 * Zero truncation hint is a valid value (treated as "use global default", same as -1).
	 */
	public void testZeroTruncationHint() {
		ToolResult result = new ToolResult("data", null, 0);
		assertEquals(0, result.truncationHint());
	}
}
