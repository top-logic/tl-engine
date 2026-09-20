/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * What an agent is told when the transaction of its turn cannot commit.
 *
 * <p>
 * The case reproduced here is the one a real session hit: {@code executeTLScript} ran a script that
 * the model rights vetoed, its own transaction - nested inside the transaction of the agent turn -
 * rolled back, and the engine poisoned the outer one. The agent's later commit then failed with
 * </p>
 *
 * <pre>
 * Commit failed: Commit failed: msg:class.com.top_logic.knowledge.service.db2.
 *     I18NConstants.NESTED_TRANSACTION_ROLLED_BACK__COMMIT_REASON/[/sExecute TLScript]/n
 * </pre>
 *
 * <p>
 * which is three separate defects in one line: an unresolved resource key, a duplicated prefix, and
 * a reason the rollback never named. All three are asserted against below.
 * </p>
 *
 * <p>
 * That the message is then handed to the agent loop's rollback event unchanged, rather than prefixed
 * a second time, is pinned by {@code TestAgentLoop.testCommitFailureTriggersRollback}.
 * </p>
 *
 * @see TransactionScope#commit()
 */
public class TestCommitFailureReason extends BasicTestCase {

	/** What the model rights say when they veto a create, in the shape a tool would report it. */
	private static final String VETO = "Creating an instance of 'Note' is not permitted.";

	/** The commit message {@code TLScriptExecutor} opens its transaction with. */
	private static final ResKey SCRIPT_TRANSACTION = ResKey.text("Execute TLScript");

	/** A {@code {0}}-style placeholder a resolved message left standing. */
	private static final Pattern UNFILLED_PLACEHOLDER = Pattern.compile("\\{\\d+\\}");

	/** What the engine says when a model constraint refused the commit. */
	private static final ResKey CONSTRAINTS_VIOLATED =
		com.top_logic.util.model.I18NConstants.CONSTRAINTS_VIOLATED;

	/**
	 * A poisoned transaction fails with a sentence, once, naming why.
	 */
	public void testPoisonedCommitReportsTheResolvedReason() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		try (TransactionScope scope = new TransactionScope()) {
			scope.begin();
			poisonWithVetoedNestedTransaction(kb);

			try {
				scope.commit();
				fail("A transaction poisoned by a nested rollback must not commit.");
			} catch (TransactionException ex) {
				String message = ex.getMessage();

				assertFalse("An unresolved resource key must never reach the model: " + message,
					message.contains("msg:"));
				assertFalse("An unfilled placeholder is not a sentence either: " + message,
					UNFILLED_PLACEHOLDER.matcher(message).find());
				assertEquals("The failure must be announced once, not once per wrapping layer: "
					+ message, 1, occurrences(message, TransactionScope.COMMIT_FAILED_PREFIX));
				assertTrue("The reason the nested transaction gave must survive into the message: "
					+ message, message.contains(VETO));
			}
		}
	}

	/**
	 * A commit the application's own constraints refuse fails as a {@link TransactionException},
	 * not as whatever the engine happened to throw.
	 *
	 * <p>
	 * The case of issue #410: a write left a mandatory attribute unset, so
	 * {@code ModelService.checkCommit} vetoed the commit with the {@link TopLogicException}
	 * {@code "Cannot store, constraints violated."}. That is an {@link I18NRuntimeException}, and
	 * the engine rethrows it unwrapped - it is not a {@link KnowledgeBaseException}. A
	 * {@link TransactionScope#commit()} that caught only the latter therefore let it past, past the
	 * agent loop's rollback handling, out of the whole run, and into the chat's catch-all, which
	 * announced it to the browser as {@code provider_error / UNKNOWN} - "the model reported an
	 * error" for a failure of the application.
	 * </p>
	 */
	public void testVetoedCommitFailsAsATransactionFailure() {
		try (TransactionScope scope = new TransactionScope()) {
			scope.begin();
			// Mandatory per tl.ai.core:Agent#name, and deliberately left unset.
			TlAiCoreFactory.getInstance().createAgent();

			try {
				scope.commit();
				fail("A commit the model constraints veto must not succeed.");
			} catch (TransactionException ex) {
				String message = ex.getMessage();
				String summary = Resources.getInstance().getString(CONSTRAINTS_VIOLATED);

				assertTrue("The reason the application gave must reach the caller: " + message,
					message.contains(summary));
				// The summary alone ("Cannot store, constraints violated.") is what a real session
				// was left with, and it names neither the object nor the attribute to fix. The
				// engine nests those under it as causes; they are the actionable half.
				assertTrue("The refused type and attribute must survive into the message: " + message,
					message.contains("tl.ai.core:Agent"));
			} catch (RuntimeException ex) {
				fail("A failed commit must be reported as a transaction failure, not as "
					+ ex.getClass().getName() + ": " + ex.getMessage());
			} finally {
				scope.rollback();
			}
		}
	}

	/**
	 * A rollback leaves no revision behind that a later reader could mistake for this turn's.
	 *
	 * <p>
	 * A {@link TransactionScope} lives for a whole run: one KB transaction per turn, and
	 * {@link TransactionScope#commit()} nulls the transaction so the scope is reusable. The commit
	 * revision, however, used to survive a rollback, so after a turn that failed the scope still
	 * reported the revision an <em>earlier</em> turn had committed.
	 * </p>
	 */
	public void testRollbackClearsTheCommitRevision() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		try (TransactionScope scope = new TransactionScope()) {
			scope.begin();
			// A commit that changed nothing produces no revision, so this turn has to write.
			TlAiCoreFactory.getInstance().createAgent()
				.tUpdate(TlAiCoreFactory.getNameAgentAttr(), "revision-probe");
			scope.commit();
			assertNotNull("A successful commit produces a revision.", scope.commitRevision());

			scope.begin();
			poisonWithVetoedNestedTransaction(kb);
			try {
				scope.commit();
				fail("A transaction poisoned by a nested rollback must not commit.");
			} catch (TransactionException expected) {
				// The commit deliberately leaves the transaction intact so the caller can roll back.
			}
			scope.rollback();

			assertNull("Nothing was persisted by this turn, so there is no revision to report.",
				scope.commitRevision());
		}
	}

	/**
	 * Does to the running transaction what a vetoed {@code executeTLScript} does: opens a nested
	 * transaction and rolls it back naming the veto, which marks the outer one as failed.
	 */
	private static void poisonWithVetoedNestedTransaction(KnowledgeBase kb) {
		try (Transaction nested = kb.beginTransaction(SCRIPT_TRANSACTION)) {
			nested.rollback(ResKey.text(VETO), null);
		}
	}

	private static int occurrences(String text, String part) {
		int count = 0;
		for (int at = text.indexOf(part); at >= 0; at = text.indexOf(part, at + part.length())) {
			count++;
		}
		return count;
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestCommitFailureReason.class),
				ModelService.Module.INSTANCE));
	}

}
