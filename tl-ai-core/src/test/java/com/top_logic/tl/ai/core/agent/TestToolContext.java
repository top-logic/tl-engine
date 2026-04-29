/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;

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
		expectThrows(NullPointerException.class,
			() -> new ToolContext(null, ApprovalCallback.AUTO_APPROVE));
	}

	/**
	 * Null approval throws.
	 */
	public void testNullApprovalThrows() {
		expectThrows(NullPointerException.class,
			() -> new ToolContext(SecurityContext.UNRESTRICTED, null));
	}

	private static <T extends Throwable> T expectThrows(Class<T> expectedType, Runnable action) {
		try {
			action.run();
			fail("Expected " + expectedType.getSimpleName());
			return null;
		} catch (Throwable throwable) {
			if (!expectedType.isInstance(throwable)) {
				fail("Expected " + expectedType.getSimpleName() + " but got "
					+ throwable.getClass().getSimpleName());
			}
			return expectedType.cast(throwable);
		}
	}

	/**
	 * EMPTY context uses NOOP transaction scope.
	 */
	public void testEmptyContextHasNoOpTransaction() {
		ToolContext ctx = ToolContext.EMPTY;
		assertSame(TransactionScope.NOOP, ctx.transaction());
		assertTrue(ctx.transaction().isNoOp());
	}

	/**
	 * Three-arg constructor sets all fields.
	 */
	public void testThreeArgConstructor() {
		TransactionScope scope = new TransactionScope();
		ApprovalCallback callback = request -> new ApprovalResult.Approved();
		ToolContext ctx = new ToolContext(SecurityContext.UNRESTRICTED, callback, scope);
		assertSame(SecurityContext.UNRESTRICTED, ctx.security());
		assertSame(callback, ctx.approval());
		assertSame(scope, ctx.transaction());
	}

	/**
	 * Two-arg constructor uses NOOP transaction.
	 */
	public void testTwoArgConstructorUsesNoOpTransaction() {
		ApprovalCallback callback = request -> new ApprovalResult.Approved();
		ToolContext ctx = new ToolContext(SecurityContext.UNRESTRICTED, callback);
		assertSame(TransactionScope.NOOP, ctx.transaction());
	}

	/**
	 * EMPTY context has zero delegation depth.
	 */
	public void testEmptyContextHasZeroDelegationDepth() {
		ToolContext ctx = ToolContext.EMPTY;
		assertEquals(0, ctx.delegationDepth());
	}

	/**
	 * forDelegation increments depth by 1.
	 */
	public void testForDelegationIncrementsDepth() {
		ToolContext ctx = ToolContext.EMPTY;
		ToolContext child = ctx.forDelegation(TransactionScope.NOOP);
		assertEquals(1, child.delegationDepth());
		ToolContext grandchild = child.forDelegation(TransactionScope.NOOP);
		assertEquals(2, grandchild.delegationDepth());
	}

	/**
	 * forDelegation preserves security and approval.
	 */
	public void testForDelegationPreservesSecurityAndApproval() {
		ApprovalCallback callback = request -> new ApprovalResult.Approved();
		ToolContext ctx = new ToolContext(SecurityContext.UNRESTRICTED, callback);
		ToolContext child = ctx.forDelegation(TransactionScope.NOOP);
		assertSame(SecurityContext.UNRESTRICTED, child.security());
		assertSame(callback, child.approval());
	}

	/**
	 * forDelegation uses the given transaction scope.
	 */
	public void testForDelegationUsesGivenTransactionScope() {
		ToolContext ctx = ToolContext.EMPTY;
		TransactionScope childScope = new TransactionScope();
		ToolContext child = ctx.forDelegation(childScope);
		assertSame(childScope, child.transaction());
	}
}
