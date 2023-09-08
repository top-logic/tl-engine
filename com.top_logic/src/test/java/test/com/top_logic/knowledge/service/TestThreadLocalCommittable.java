/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.Test;

import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.ThreadLocalCommitable;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test for {@link ThreadLocalCommitable}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestThreadLocalCommittable extends AbstractDBKnowledgeBaseTest {

	/**
	 * {@link ThreadLocalCommitable} for tests.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class TestingThreadLocalCommittable extends ThreadLocalCommitable {

		boolean _committed;

		boolean _rolledBack;

		/**
		 * Creates a new {@link TestingThreadLocalCommittable}.
		 */
		protected TestingThreadLocalCommittable(KnowledgeBase kb) throws SQLException {
			super(KBUtils.getConnectionPool(kb));
		}

		@Override
		public boolean commit(CommitContext aContext) {
			_committed = true;
			return super.commit(aContext);
		}

		@Override
		public boolean rollback(CommitContext aContext) {
			_rolledBack = true;
			return super.rollback(aContext);
		}

	}

	public void testCommit() throws SQLException {
		TestingThreadLocalCommittable successCommitable = new TestingThreadLocalCommittable(kb());

		Transaction tx = begin(successCommitable);
		newB("b1");
		commit(tx, false, successCommitable);

		assertTrue(successCommitable._committed);
	}

	public void testCommitRollback() throws SQLException {
		/* Ensure that the first committable commits correct, and the second commit fails. Then a
		 * rollback on the first must be triggered. As the order of listener is not fix (it is a
		 * set) it must be determined dynamically which committable have not to commit correct. */
		final AtomicReference<TestingThreadLocalCommittable> first = new AtomicReference<>();
		class FailingCommitCommittable extends TestingThreadLocalCommittable {

			protected FailingCommitCommittable() throws SQLException {
				super(kb());
			}

			@Override
			public boolean commit(CommitContext aContext) {
				if (first.get() == null || first.get() == this) {
					first.set(this);
					return super.commit(aContext);
				} else {
					return false;
				}
			}

		}

		TestingThreadLocalCommittable commitable1 = new FailingCommitCommittable();
		TestingThreadLocalCommittable commitable2 = new FailingCommitCommittable();
		Transaction tx2 = begin(commitable1, commitable2);
		newB("b2");
		AssertNoErrorLogListener listener = new AssertNoErrorLogListener(false);
		listener.activate();
		try {
			commit(tx2, true, commitable1, commitable1);
			listener.assertNoErrorLogged("Rollback on all commitables must be successful: ");
		} finally {
			listener.deactivate();
		}
		assertNotNull(first.get());
		assertTrue(first.get()._committed);
		assertTrue(first.get()._rolledBack);
		TestingThreadLocalCommittable second = first.get() == commitable1 ? commitable2 : commitable1;
		assertFalse(second._committed);
		assertTrue(second._rolledBack);
	}

	private void commit(Transaction tx, boolean expectFailure, TestingThreadLocalCommittable... commitables) {
		try {
			internalCommitTx(tx, null, expectFailure);
		} finally {
			ArrayUtil.forEach(commitables, committable -> kb().removeCommittable(committable));
		}
	}

	private Transaction begin(ThreadLocalCommitable... commitables) {
		Transaction tx = begin();
		ArrayUtil.forEach(commitables, committable -> committable.begin(kb()));
		return tx;
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestThreadLocalCommittable}.
	 */
	public static Test suite() {
		return AbstractDBKnowledgeBaseTest.suite(TestThreadLocalCommittable.class);
	}

}

