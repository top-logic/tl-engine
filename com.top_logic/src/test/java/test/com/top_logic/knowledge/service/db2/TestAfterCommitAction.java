/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AfterCommitAction;
import com.top_logic.knowledge.service.CommitActions;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.event.CommitChecker;
import com.top_logic.knowledge.service.event.CommitVetoException;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.knowledge.service.event.ModificationListener;

/**
 * Test case for {@link AfterCommitAction}s registered through {@link CommitActions}.
 */
@SuppressWarnings("javadoc")
public class TestAfterCommitAction extends AbstractDBKnowledgeBaseTest {

	/**
	 * Revisions observed by the executed actions, one entry per execution.
	 */
	private List<Long> _executions;

	/**
	 * Events received by the executed actions, one entry per execution.
	 */
	private List<UpdateEvent> _events;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_executions = new ArrayList<>();
		_events = new ArrayList<>();
	}

	private AfterCommitAction recordExecution() {
		return changes -> {
			_executions.add(kb().getLastRevision());
			_events.add(changes);
		};
	}

	private void assertEventOf(Transaction tx, UpdateEvent event, KnowledgeItem... created) {
		assertNotNull("Action received no event for a commit with changes.", event);
		assertEquals("Action received the event of another commit.",
			tx.getCommitRevision().getCommitNumber(), event.getCommitNumber());
		for (KnowledgeItem item : created) {
			assertTrue("Created object missing in the event: " + item,
				event.getCreatedObjects().containsKey(item.tId()));
		}
	}

	public void testExecutedAfterSuccessfulCommit() throws Throwable {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		tx.afterCommit(recordExecution());
		assertEquals("Action executed before commit.", 0, _executions.size());
		commit(tx);

		assertEquals("Action not executed exactly once.", 1, _executions.size());
		assertEquals("Action did not see the committed revision.",
			Long.valueOf(tx.getCommitRevision().getCommitNumber()), _executions.get(0));
		assertEventOf(tx, _events.get(0), b1);
	}

	public void testExecutedAfterCommitWithoutChanges() throws Throwable {
		Transaction tx = begin();
		tx.afterCommit(recordExecution());
		commit(tx);

		assertEquals("Action not executed exactly once.", 1, _executions.size());
		assertNull("Action received an event for a commit without changes.", _events.get(0));
	}

	public void testExecutedAfterOutermostCommit() throws Throwable {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		Transaction nested = begin();
		KnowledgeObject b2 = newB("b2");
		nested.afterCommit(recordExecution());
		commit(nested);
		assertEquals("Action executed before the outermost commit.", 0, _executions.size());
		commit(tx);

		assertEquals("Action not executed exactly once.", 1, _executions.size());
		assertEventOf(tx, _events.get(0), b1, b2);
	}

	public void testNotExecutedOnNestedRollback() throws Throwable {
		Transaction tx = begin();
		newB("b1");
		tx.afterCommit(recordExecution());
		Transaction nested = begin();
		newB("b2");
		nested.afterCommit(recordExecution());
		rollback(nested);

		// The rollback of a nested transaction invalidates the outer one.
		commit(tx, true);
		assertEquals("Action of rolled back nested transaction executed.", 0, _executions.size());

		Transaction tx2 = begin();
		newB("b3");
		commit(tx2);
		assertEquals("Action of rolled back nested transaction executed later.", 0, _executions.size());
	}

	public void testActionStartsOwnTransaction() throws Throwable {
		List<KnowledgeObject> created = new ArrayList<>();
		Transaction tx = begin();
		newB("b1");
		tx.afterCommit(changes -> {
			Transaction followUp = begin();
			try {
				created.add(newB("b2"));
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
			commit(followUp);
		});
		commit(tx);

		assertEquals(1, created.size());
		assertTrue("Object of the follow-up transaction not committed.", created.get(0).isAlive());
	}

	public void testFailingActionDoesNotAffectCommit() throws Throwable {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		tx.afterCommit(changes -> {
			throw new RuntimeException("Expected failure of an after-commit action.");
		});
		tx.afterCommit(recordExecution());
		commit(tx);

		assertTrue(b1.isAlive());
		assertEquals("Subsequent action not executed.", 1, _executions.size());
	}

	public void testNotExecutedOnRollback() throws Throwable {
		Transaction tx = begin();
		newB("b1");
		tx.afterCommit(recordExecution());
		rollback(tx);
		assertEquals("Action executed after rollback.", 0, _executions.size());

		// The action of the rolled back transaction is not executed with the next commit.
		Transaction tx2 = begin();
		newB("b2");
		commit(tx2);
		assertEquals("Action of rolled back transaction executed later.", 0, _executions.size());
	}

	public void testNotExecutedOnVeto() throws Throwable {
		CommitChecker veto = event -> {
			throw new CommitVetoException(event);
		};
		Transaction tx = begin();
		newB("b1");
		tx.afterCommit(recordExecution());
		kb().addCommitChecker(veto);
		try {
			commit(tx, true);
		} finally {
			kb().removeCommitChecker(veto);
		}
		assertEquals("Action executed after vetoed commit.", 0, _executions.size());

		Transaction tx2 = begin();
		newB("b2");
		commit(tx2);
		assertEquals("Action of vetoed transaction executed later.", 0, _executions.size());
	}

	public void testRegisteredFromModificationListener() throws Throwable {
		List<CommitActions> handles = new ArrayList<>();
		RegisteringListener listener = new RegisteringListener(handles);
		kb().addModificationListener(listener);
		try {
			Transaction tx = begin();
			KnowledgeObject b1 = newB("b1");
			commit(tx);

			assertEquals("Listener not called exactly once.", 1, handles.size());
			assertEquals("Action not executed exactly once.", 1, _executions.size());
			assertEquals("Action did not see the committed revision.",
				Long.valueOf(tx.getCommitRevision().getCommitNumber()), _executions.get(0));
			assertEquals("Modification did not create an object.", 1, listener.getImplicitlyCreated().size());
			KnowledgeItem implicit = listener.getImplicitlyCreated().get(0);
			assertTrue("Implicitly created object not committed.", implicit.isAlive());
			assertEventOf(tx, _events.get(0), b1, implicit);
		} finally {
			kb().removeModificationListener(listener);
		}
	}

	public void testRegisteredFromModificationListenerNotExecutedOnVeto() throws Throwable {
		List<CommitActions> handles = new ArrayList<>();
		ModificationListener listener = new RegisteringListener(handles);
		CommitChecker veto = event -> {
			throw new CommitVetoException(event);
		};
		kb().addModificationListener(listener);
		kb().addCommitChecker(veto);
		try {
			Transaction tx = begin();
			newB("b1");
			commit(tx, true);
		} finally {
			kb().removeCommitChecker(veto);
			kb().removeModificationListener(listener);
		}

		assertEquals("Listener not called exactly once.", 1, handles.size());
		assertEquals("Action executed after vetoed commit.", 0, _executions.size());
	}

	/**
	 * {@link ModificationListener} whose {@link Modification} creates an object of its own and
	 * registers an {@link AfterCommitAction} for each commit with created objects.
	 */
	private class RegisteringListener implements ModificationListener {

		private final List<CommitActions> _handles;

		private final List<KnowledgeItem> _implicitlyCreated = new ArrayList<>();

		RegisteringListener(List<CommitActions> handles) {
			_handles = handles;
		}

		/**
		 * The objects created by the {@link Modification}s of this listener.
		 */
		List<KnowledgeItem> getImplicitlyCreated() {
			return _implicitlyCreated;
		}

		@Override
		public Modification createModification(KnowledgeBase kb, CommitActions commitActions,
				Map<ObjectKey, ? extends KnowledgeItem> createdObjects,
				Map<ObjectKey, ? extends KnowledgeItem> updatedObjects,
				Map<ObjectKey, ? extends KnowledgeItem> removedObjects) {
			if (createdObjects.isEmpty()) {
				return Modification.NONE;
			}
			_handles.add(commitActions);
			return () -> {
				_implicitlyCreated.add(newB("implicit"));
				commitActions.afterCommit(recordExecution());
			};
		}

		@Override
		public Modification notifyUpcomingDeletion(KnowledgeBase kb, CommitActions commitActions,
				KnowledgeItem item) {
			return Modification.NONE;
		}
	}

	public static Test suite() {
		return suite(TestAfterCommitAction.class);
	}

}
