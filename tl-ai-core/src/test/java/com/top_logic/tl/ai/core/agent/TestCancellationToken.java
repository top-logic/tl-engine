/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.concurrent.CancellationException;

import junit.framework.TestCase;

import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Tests for {@link CancellationToken}.
 */
public class TestCancellationToken extends TestCase {

	/**
	 * {@link CancellationToken#checkCancelled()} throws {@link CancellationException}
	 * with the expected message when the token has been cancelled.
	 */
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
