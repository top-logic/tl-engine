/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.sql.SQLException;

import junit.framework.TestCase;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;

/**
 * Tests for {@link TransactionScope}.
 *
 * <p>Tests the state machine and no-op behavior without a running TopLogic
 * environment. KB integration tests require the TopLogic test harness.</p>
 *
 * @see TestCommitFailureReason
 */
public class TestTransactionScope extends TestCase {

	/**
	 * The engine hides the real failure behind one constant, so the cause chain has to be reported.
	 *
	 * <p>
	 * {@code DefaultDBContext} wraps any unexpected {@link Throwable} that escaped the commit as
	 * {@code new KnowledgeBaseException("Database operation failed.", ex)}, and that string is all
	 * {@code getMessage()} returns. Without the cause a deployment that hits this has nothing to go
	 * on, and neither does the model.
	 * </p>
	 */
	public void testCommitFailureNamesTheRootCause() {
		KnowledgeBaseException failure = new KnowledgeBaseException("Database operation failed.",
			new IllegalStateException("wrapper",
				new SQLException("Data too long for column 'name'")));

		String described = TransactionScope.describeCommitFailure(failure);

		assertTrue("The engine's own message must survive: " + described,
			described.startsWith("Database operation failed."));
		assertTrue("The deepest cause is the one that says what went wrong: " + described,
			described.contains("Data too long for column 'name'"));
		assertTrue("Naming the exception type tells a reader what kind of failure it was: "
			+ described, described.contains("SQLException"));
		assertFalse("An intermediate wrapper says nothing and must not be reported: " + described,
			described.contains("wrapper"));
	}

	/** A failure with no cause is reported as it is, without an empty parenthesis. */
	public void testCommitFailureWithoutACauseIsUnchanged() {
		assertEquals("Database operation failed.", TransactionScope.describeCommitFailure(
			new KnowledgeBaseException("Database operation failed.", (KnowledgeBase) null)));
	}

	/** A cause that only repeats the message adds nothing and is not appended twice. */
	public void testCommitFailureDoesNotRepeatItself() {
		assertEquals("Constraint violated.", TransactionScope.describeCommitFailure(
			new KnowledgeBaseException("Constraint violated.",
				new SQLException("Constraint violated."))));
	}

	/**
	 * A driver may quote a whole statement; the reason ends up in an LLM prompt, so it is bounded.
	 */
	public void testCommitFailureCauseIsBounded() {
		String huge = "x".repeat(5000);
		String described = TransactionScope.describeCommitFailure(
			new KnowledgeBaseException("Database operation failed.",
				new SQLException(huge)));

		assertTrue("A multi-KB driver message must not reach the prompt in full: "
			+ described.length(), described.length() < 700);
		assertTrue("The truncation has to be visible: " + described, described.endsWith("...)"));
	}

	/**
	 * A commit failure the engine did not wrap is still described, not dropped.
	 *
	 * <p>
	 * Issue #410: {@code DefaultDBContext.commitTransaction} rethrows a
	 * {@code KnowledgeBaseRuntimeException} - and a veto's {@code I18NRuntimeException} - as itself,
	 * so "the exception a failed commit throws" is not one type. Describing only
	 * {@link KnowledgeBaseException} is what let such a failure leave the run untranslated. The
	 * veto family needs resolved resources and is covered by
	 * {@link TestCommitFailureReason#testVetoedCommitFailsAsATransactionFailure()}; this is the
	 * plain one.
	 * </p>
	 */
	public void testUnwrappedCommitFailureIsDescribedToo() {
		String described = TransactionScope.describeCommitFailure(
			new IllegalStateException("Commit aborted.",
				new SQLException("Deadlock found when trying to get lock")));

		assertTrue("The failure's own message must survive: " + described,
			described.startsWith("Commit aborted."));
		assertTrue("The deepest cause is the one that says what went wrong: " + described,
			described.contains("Deadlock found when trying to get lock"));
	}

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
