/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import junit.framework.TestCase;

/**
 * Tests for {@link ExecutionResult}.
 */
public class TestExecutionResult extends TestCase {

	/** Normal result with response and no block. */
	public void testNormalResult() {
		ExecutionResult result = new ExecutionResult("Hello", null);

		assertEquals("Hello", result.response());
		assertNull(result.blockReason());
		assertFalse(result.isBlocked());
	}

	/** Blocked result with a reason. */
	public void testBlockedResult() {
		ExecutionResult result = new ExecutionResult("response", "unsafe content");

		assertEquals("response", result.response());
		assertEquals("unsafe content", result.blockReason());
		assertTrue(result.isBlocked());
	}

	/** Null response is allowed (agent failure). */
	public void testNullResponse() {
		ExecutionResult result = new ExecutionResult(null, null);

		assertNull(result.response());
		assertFalse(result.isBlocked());
	}
}
