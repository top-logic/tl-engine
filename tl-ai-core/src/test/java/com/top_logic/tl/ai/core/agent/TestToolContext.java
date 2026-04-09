/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import junit.framework.TestCase;

/**
 * Tests for {@link ToolContext}.
 */
public class TestToolContext extends TestCase {

	/**
	 * EMPTY context uses UNRESTRICTED security and AUTO_APPROVE callback.
	 */
	public void testEmptyContext() {
		ToolContext ctx = ToolContext.EMPTY;
		assertSame(SecurityContext.UNRESTRICTED, ctx.security());
		assertSame(ApprovalCallback.AUTO_APPROVE, ctx.approval());
	}

	/**
	 * Two-arg constructor sets both fields.
	 */
	public void testTwoArgConstructor() {
		ApprovalCallback callback = request -> new ApprovalResult.Approved();
		ToolContext ctx = new ToolContext(SecurityContext.UNRESTRICTED, callback);
		assertSame(SecurityContext.UNRESTRICTED, ctx.security());
		assertSame(callback, ctx.approval());
	}

	/**
	 * Null security throws.
	 */
	public void testNullSecurityThrows() {
		try {
			new ToolContext(null, ApprovalCallback.AUTO_APPROVE);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}

	/**
	 * Null approval throws.
	 */
	public void testNullApprovalThrows() {
		try {
			new ToolContext(SecurityContext.UNRESTRICTED, null);
			fail("Expected NullPointerException");
		} catch (NullPointerException e) {
			// expected
		}
	}
}
