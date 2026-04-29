/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.concurrent.CancellationException;

import junit.framework.TestCase;

/**
 * Tests for {@link CancellationToken}.
 */
public class TestCancellationToken extends TestCase {

	/** */
	public void testInitiallyNotCancelled() {
		CancellationToken token = new CancellationToken();
		assertFalse(token.isCancelled());
	}

	/** */
	public void testCancelSetsFlag() {
		CancellationToken token = new CancellationToken();
		token.cancel();
		assertTrue(token.isCancelled());
	}

	/** */
	public void testCheckCancelledDoesNothingWhenNotCancelled() {
		CancellationToken token = new CancellationToken();
		token.checkCancelled();
		// No exception thrown — test passes.
	}

	/** */
	public void testCheckCancelledThrowsWhenCancelled() {
		CancellationToken token = new CancellationToken();
		token.cancel();
		try {
			token.checkCancelled();
			fail("Expected CancellationException.");
		} catch (CancellationException e) {
			assertEquals("Tool execution cancelled.", e.getMessage());
		}
	}
}
