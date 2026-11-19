/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Collections;
import java.util.function.Consumer;

import junit.framework.Test;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.CommitActions;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.knowledge.service.event.ModificationListener;
import com.top_logic.knowledge.service.event.PendingChanges;

/**
 * Test case for the {@link PendingChanges} handed to a {@link ModificationListener}.
 */
@SuppressWarnings("javadoc")
public class TestPendingChanges extends AbstractDBKnowledgeBaseTest {

	private static final String FLEX_ATTRIBUTE = "flex";

	private InspectingListener _listener;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_listener = new InspectingListener();
		kb().addModificationListener(_listener);
	}

	@Override
	protected void tearDown() throws Exception {
		kb().removeModificationListener(_listener);
		_listener = null;
		super.tearDown();
	}

	/**
	 * Commits the given transaction and runs the given inspection on the {@link PendingChanges}
	 * of the commit.
	 */
	private void commitInspecting(Transaction tx, Consumer<PendingChanges> inspection) throws Throwable {
		_listener.inspect(inspection);
		commit(tx);
		_listener.check();
	}

	public void testGroupedByTable() throws Throwable {
		Transaction createTx = begin();
		KnowledgeObject b1 = newB("b1");
		KnowledgeObject c1 = newC("c1");
		commitInspecting(createTx, changes -> {
			MetaObject bTable = b1.tTable();
			MetaObject cTable = c1.tTable();
			assertEquals(set(bTable, cTable), changes.getTables());
			assertEquals(list(b1), changes.getCreated(bTable));
			assertEquals(list(c1), changes.getCreated(cTable));
			assertEquals(Collections.emptyList(), changes.getUpdated(bTable));
			assertEquals(Collections.emptyList(), changes.getRemoved(cTable));
			assertEquals(set(b1.tId(), c1.tId()), changes.getCreated().keySet());
			assertTrue(changes.getUpdated().isEmpty());
			assertTrue(changes.getRemoved().isEmpty());
		});

		Transaction changeTx = begin();
		setA2(b1, "a2");
		c1.delete();
		KnowledgeObject b2 = newB("b2");
		commitInspecting(changeTx, changes -> {
			MetaObject bTable = b1.tTable();
			MetaObject cTable = c1.tTable();
			assertEquals(set(bTable, cTable), changes.getTables());
			assertEquals(list(b2), changes.getCreated(bTable));
			assertEquals(list(b1), changes.getUpdated(bTable));
			assertEquals(Collections.emptyList(), changes.getRemoved(bTable));
			assertEquals(Collections.emptyList(), changes.getCreated(cTable));
			assertEquals(Collections.emptyList(), changes.getUpdated(cTable));
			assertEquals(list(c1), changes.getRemoved(cTable));
		});
	}

	public void testChangedAttributes() throws Throwable {
		Transaction createTx = begin();
		KnowledgeObject b1 = newB("a1-old");
		setA2(b1, "a2-old");
		commit(createTx);

		Transaction changeTx = begin();
		setA1(b1, "a1-new");
		setA2(b1, "a2-intermediate");
		setA2(b1, "a2-old");
		commitInspecting(changeTx, changes -> {
			assertEquals(set(A1_NAME), changes.getChangedAttributes(b1));
			assertTrue(changes.isChanged(b1, A1_NAME));
			assertFalse("Attribute set back to its committed value reported as changed.",
				changes.isChanged(b1, A2_NAME));
			assertEquals("a1-old", changes.getCommittedValue(b1, A1_NAME));
			assertEquals("a2-old", changes.getCommittedValue(b1, A2_NAME));
		});
		assertEquals("a1-new", b1.getAttributeValue(A1_NAME));
	}

	public void testAllValuesSetBack() throws Throwable {
		Transaction createTx = begin();
		KnowledgeObject b1 = newB("a1-old");
		commit(createTx);

		Transaction changeTx = begin();
		setA1(b1, "a1-new");
		setA1(b1, "a1-old");
		commitInspecting(changeTx, changes -> {
			assertTrue(changes.getUpdated().containsKey(b1.tId()));
			assertEquals(Collections.emptySet(), changes.getChangedAttributes(b1));
		});
	}

	public void testFlexAttribute() throws Throwable {
		Transaction createTx = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flex-old");
		KnowledgeObject e2 = newE("e2");
		e2.setAttributeValue(FLEX_ATTRIBUTE, "flex-old");
		commit(createTx);

		Transaction changeTx = begin();
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flex-new");
		e2.setAttributeValue(FLEX_ATTRIBUTE, "flex-intermediate");
		e2.setAttributeValue(FLEX_ATTRIBUTE, "flex-old");
		setA2(e2, "a2");
		commitInspecting(changeTx, changes -> {
			assertEquals(set(FLEX_ATTRIBUTE), changes.getChangedAttributes(e1));
			assertEquals("flex-old", changes.getCommittedValue(e1, FLEX_ATTRIBUTE));
			assertNull("Flexible attribute without value.", changes.getCommittedValue(e1, "no-such-flex"));

			assertEquals("Flexible attribute set back to its committed value reported as changed.",
				set(A2_NAME), changes.getChangedAttributes(e2));
		});
		assertEquals("flex-new", e1.getAttributeValue(FLEX_ATTRIBUTE));
	}

	public void testFlexAttributeRemoved() throws Throwable {
		Transaction createTx = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flex-old");
		commit(createTx);

		Transaction changeTx = begin();
		e1.setAttributeValue(FLEX_ATTRIBUTE, null);
		commitInspecting(changeTx, changes -> {
			assertEquals(set(FLEX_ATTRIBUTE), changes.getChangedAttributes(e1));
			assertEquals("flex-old", changes.getCommittedValue(e1, FLEX_ATTRIBUTE));
		});
	}

	public void testCommittedValueOfRemoved() throws Throwable {
		Transaction createTx = begin();
		KnowledgeObject b1 = newB("a1-old");
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(FLEX_ATTRIBUTE, "flex-old");
		commit(createTx);

		Transaction deleteTx = begin();
		setA1(b1, "a1-new");
		b1.delete();
		e1.delete();
		commitInspecting(deleteTx, changes -> {
			assertEquals(set(b1.tId(), e1.tId()), changes.getRemoved().keySet());
			assertEquals("a1-old", changes.getCommittedValue(b1, A1_NAME));
			assertEquals("flex-old", changes.getCommittedValue(e1, FLEX_ATTRIBUTE));
			try {
				changes.getChangedAttributes(b1);
				fail("Changed attributes of a removed item.");
			} catch (IllegalArgumentException ex) {
				// Expected.
			}
		});
	}

	public void testNoCommittedValueOfCreated() throws Throwable {
		Transaction createTx = begin();
		KnowledgeObject b1 = newB("b1");
		commitInspecting(createTx, changes -> {
			try {
				changes.getCommittedValue(b1, A1_NAME);
				fail("Committed value of an item created in the transaction.");
			} catch (IllegalArgumentException ex) {
				// Expected.
			}
		});
	}

	/**
	 * {@link ModificationListener} running an inspection of the {@link PendingChanges} of the next
	 * commit.
	 */
	private static class InspectingListener implements ModificationListener {

		private Consumer<PendingChanges> _inspection;

		private boolean _inspected;

		private Throwable _failure;

		/**
		 * Runs the given inspection in the next commit.
		 */
		void inspect(Consumer<PendingChanges> inspection) {
			_inspection = inspection;
			_inspected = false;
			_failure = null;
		}

		/**
		 * Reports the failure of the last inspection.
		 */
		void check() throws Throwable {
			assertTrue("Listener was not called.", _inspected);
			if (_failure != null) {
				throw _failure;
			}
		}

		@Override
		public Modification createModification(KnowledgeBase kb, CommitActions commitActions,
				PendingChanges changes) {
			Consumer<PendingChanges> inspection = _inspection;
			if (inspection != null) {
				_inspection = null;
				_inspected = true;
				try {
					inspection.accept(changes);
				} catch (Throwable ex) {
					_failure = ex;
				}
			}
			return Modification.NONE;
		}

		@Override
		public Modification notifyUpcomingDeletion(KnowledgeBase kb, CommitActions commitActions,
				KnowledgeItem item) {
			return Modification.NONE;
		}
	}

	public static Test suite() {
		return suite(TestPendingChanges.class);
	}

}
