/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.core.agent;

import junit.framework.TestCase;

import com.top_logic.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.ai.core.agent.approval.ApprovalResult;
import com.top_logic.ai.core.security.SecurityContextProxy;

/**
 * Tests for {@link ToolContext}.
 */
public class TestToolContext extends TestCase {

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

	/**
	 * A script that a run executes starts its nested run with the security context of that run, with
	 * every limit of it, not with a new context of the account.
	 */
	public void testForCurrentUserInARunHasTheContextOfTheRun() {
		SecurityContext run = new SecurityContextProxy(SecurityContext.UNRESTRICTED);
		try (RunSecurityBinding.Binding bound = RunSecurityBinding.bind(run)) {
			assertSame(run, ToolContext.forCurrentUser().security());
		}
		assertNull("The binding must end with its scope.", RunSecurityBinding.current());
	}

	/**
	 * A tool call that names no run, inside a run, keeps the context of that run: it must not end the
	 * limits of the run for the script of the tool.
	 */
	public void testACallThatNamesNoRunKeepsTheRun() {
		SecurityContext run = new SecurityContextProxy(SecurityContext.UNRESTRICTED);
		try (RunSecurityBinding.Binding bound = RunSecurityBinding.bind(run)) {
			try (RunSecurityBinding.Binding none = RunSecurityBinding.bind(null)) {
				assertSame(run, ToolContext.forCurrentUser().security());
			}
			assertSame(run, RunSecurityBinding.current());
		}
	}
}
