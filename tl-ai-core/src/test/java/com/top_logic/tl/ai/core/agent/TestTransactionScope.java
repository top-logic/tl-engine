/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import junit.framework.TestCase;

/**
 * Tests for {@link TransactionScope}.
 *
 * <p>Tests the state machine and no-op behavior without a running TopLogic
 * environment. KB integration tests require the TopLogic test harness.</p>
 */
public class TestTransactionScope extends TestCase {

	/**
	 * NOOP scope: isActive() always false, holdsLock() always false.
	 */
	public void testNoOpIsInactive() {
		TransactionScope scope = TransactionScope.NOOP;
		assertFalse(scope.isActive());
		assertFalse(scope.holdsLock());
	}

	/**
	 * NOOP scope: begin/commit/rollback are safe no-ops.
	 */
	public void testNoOpLifecycle() {
		TransactionScope scope = TransactionScope.NOOP;
		scope.begin();
		assertFalse(scope.isActive());
		scope.commit();
		scope.rollback();
	}

	/**
	 * NOOP scope: acquireLock/releaseLock are safe no-ops.
	 */
	public void testNoOpLocking() {
		TransactionScope scope = TransactionScope.NOOP;
		scope.acquireLock("test");
		assertFalse(scope.holdsLock());
		scope.releaseLock();
	}

	/**
	 * NOOP scope: close is a safe no-op.
	 */
	public void testNoOpClose() {
		TransactionScope scope = TransactionScope.NOOP;
		scope.close();
	}

	/**
	 * NOOP scope: isNoOp returns true.
	 */
	public void testNoOpFlag() {
		assertTrue(TransactionScope.NOOP.isNoOp());
	}

	/**
	 * Live scope: isNoOp returns false.
	 */
	public void testLiveIsNotNoOp() {
		try (TransactionScope scope = new TransactionScope()) {
			assertFalse(scope.isNoOp());
		}
	}
}
