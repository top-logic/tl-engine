/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Committable;
import com.top_logic.knowledge.service.CommittableAdapter;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.ReflectionUtils;

/**
 * Test case for a commit of {@link DBKnowledgeBase} whose publication fails after the database
 * transaction has been committed.
 *
 * <p>
 * The failure is provoked through the commit-published hook of {@link DBKnowledgeBase}, which runs
 * after the database commit while the changed objects and caches are published locally. The
 * transaction must count as committed: {@link Committable}s are not rolled back, and the
 * after-commit actions of the transaction run.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestCommitPublicationFailure extends AbstractDBKnowledgeBaseTest {

	private static final String PREPARE = "prepare";

	private static final String COMMIT = "commit";

	private static final String ROLLBACK = "rollback";

	public void testNoRollbackAfterDatabaseCommit() throws Throwable {
		RecordingCommittable committable = new RecordingCommittable(true);
		List<UpdateEvent> executions = new ArrayList<>();
		CollectingLogListener errors = new CollectingLogListener(AssertNoErrorLogListener.ERROR_AND_ABOVE, false);

		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		ObjectKey key = b1.tId();
		kb().addCommittable(committable);
		tx.afterCommit(executions::add);

		setCommitPublishedHook(() -> {
			throw new ExpectedFailure("Publication failure after the database commit.");
		});
		errors.activate();
		try {
			commit(tx);
		} finally {
			errors.deactivate();
			setCommitPublishedHook(null);
		}

		assertEquals("Committable rolled back after the database commit.", List.of(PREPARE, COMMIT),
			committable.getCalls());
		assertNotNull("No commit revision.", tx.getCommitRevision());
		long commitNumber = tx.getCommitRevision().getCommitNumber();

		List<LogEntry> logged = errors.getLogEntries();
		assertEquals("Publication failure not logged exactly once: " + logged, 1, logged.size());
		String message = logged.get(0).getMessage();
		assertTrue(message, message.contains("revision " + commitNumber));

		assertTrue("Committed object rolled back.", b1.isAlive());
		assertEquals("b1", b1.getAttributeValue(A1_NAME));

		assertEquals("After-commit action not executed exactly once.", 1, executions.size());
		UpdateEvent event = executions.get(0);
		assertEquals(commitNumber, event.getCommitNumber());
		assertTrue("Created object missing in the event.", event.getCreatedObjects().containsKey(key));

		inParallel(() -> {
			KnowledgeItem resolved = kb().resolveObjectKey(key);
			assertNotNull("Committed object not visible from another thread.", resolved);
			assertEquals("b1", resolved.getAttributeValue(A1_NAME));
		}).check();

		// The knowledge base is usable for the following transaction.
		Transaction tx2 = begin();
		KnowledgeObject b2 = newB("b2");
		b1.setAttributeValue(A1_NAME, "b1-changed");
		commit(tx2);
		assertTrue(b2.isAlive());
		assertEquals("b1-changed", b1.getAttributeValue(A1_NAME));
	}

	public void testRollbackWhenCommitFailsBeforeDatabaseCommit() throws Throwable {
		RecordingCommittable committable = new RecordingCommittable(false);
		List<UpdateEvent> executions = new ArrayList<>();

		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		ObjectKey key = b1.tId();
		kb().addCommittable(committable);
		tx.afterCommit(executions::add);
		commit(tx, true);

		assertEquals("Committable not rolled back after a failed commit.", List.of(PREPARE, COMMIT, ROLLBACK),
			committable.getCalls());
		assertFalse("Object of a failed commit is alive.", b1.isAlive());
		assertNull("Object of a failed commit is visible.", kb().resolveObjectKey(key));
		assertEquals("After-commit action executed after a failed commit.", 0, executions.size());

		// The knowledge base is usable for the following transaction.
		Transaction tx2 = begin();
		KnowledgeObject b2 = newB("b2");
		commit(tx2);
		assertTrue(b2.isAlive());
	}

	private void setCommitPublishedHook(Runnable hook) {
		ReflectionUtils.setValue(kb(), TestRefetchLockHolder.COMMIT_PUBLISHED_HOOK, hook);
	}

	/**
	 * {@link Committable} recording the calls it receives.
	 */
	private static class RecordingCommittable extends CommittableAdapter {

		private final boolean _commitSucceeds;

		private final List<String> _calls = new ArrayList<>();

		/**
		 * Creates a {@link RecordingCommittable}.
		 *
		 * @param commitSucceeds
		 *        Whether {@link #commit(CommitContext)} reports success.
		 */
		RecordingCommittable(boolean commitSucceeds) {
			_commitSucceeds = commitSucceeds;
		}

		/**
		 * The names of the received calls in order.
		 */
		List<String> getCalls() {
			return _calls;
		}

		@Override
		public boolean prepare(CommitContext context) {
			_calls.add(PREPARE);
			return true;
		}

		@Override
		public boolean commit(CommitContext context) {
			_calls.add(COMMIT);
			return _commitSucceeds;
		}

		@Override
		public boolean rollback(CommitContext context) {
			_calls.add(ROLLBACK);
			return true;
		}
	}

	public static Test suite() {
		return suite(TestCommitPublicationFailure.class);
	}

}
