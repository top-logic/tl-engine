/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import junit.framework.Test;

import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;

/**
 * The class {@link TestDBKnowledgeBaseCache} tests consistency of {@link DBKnowledgeBase} cache. It
 * can simulate a concurrent GC run during creation, deleting, or searching items.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDBKnowledgeBaseCache extends AbstractDBKnowledgeBaseTest {

	private static final String DANGLING_REFERENCES_FILED_NAME = "danglingReferences";

	private Object _origQueue;

	private DeactivatableReferenceQueue _danglingReferences;

	/**
	 * ReferenceQueue in which the {@link #poll()} method can be deactivated.
	 * 
	 * @see #setDeactivated(boolean)
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	class DeactivatableReferenceQueue extends ReferenceQueue<Object> {
		private boolean deactivated;

		@Override
		public Reference<? extends Object> poll() {
			if (deactivated) {
				return null;
			}

			return super.poll();
		}

		/**
		 * Deactivate/activate this queue.
		 * 
		 * <p>
		 * In a deactivated queue, the {@link #poll()} method always returns <code>null</code>.
		 * </p>
		 */
		public void setDeactivated(boolean deactivated) {
			this.deactivated = deactivated;
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_danglingReferences = new DeactivatableReferenceQueue();
		_origQueue = ReflectionUtils.setValue(kb(), DANGLING_REFERENCES_FILED_NAME, _danglingReferences);
	}

	@Override
	protected void tearDown() throws Exception {
		ReflectionUtils.setValue(kb(), DANGLING_REFERENCES_FILED_NAME, _origQueue);
		super.tearDown();
	}

	/**
	 * Test that removing outdated references from the knowledge base cache does not drop alive
	 * objects that are entered into the cache after the old reference has been cleared.
	 */
	// Deactivated because provoking an out-of-memory condition seems to damage a Java 11 VM
	// internally.
	// "java.lang.OutOfMemoryError: Java heap space: failed reallocation of scalar replaced objects"
	public void deactivatedTestCacheConsistency() throws DataObjectException {
		Object b1InternalKey;
		{
			Transaction tx = begin();
			// Allocate object and keep only its identity.
			b1InternalKey = newB("b1").tId();
			commit(tx);
		}

		// Allocate garbage to force the GC to clean up all unreferenced
		// objects.
		provokeOutOfMemory();

		final KnowledgeObject b1Reallocated;

		// Deactivate cache cleanup to simulate a concurrent GC run after
		// cleanupReferences() has been called in the following search.
		_danglingReferences.setDeactivated(true);
		try {
			b1Reallocated = (KnowledgeObject) kb().getObjectByAttribute(B_NAME, A1_NAME, "b1");
			Object b1ReallocatedKey = b1Reallocated.tId();

			assertNotSame(b1InternalKey, b1ReallocatedKey);
		} finally {
			_danglingReferences.setDeactivated(false);
		}

		KnowledgeObject b1Reallocated2 = (KnowledgeObject) kb().getObjectByAttribute(B_NAME, A1_NAME, "b1");

		// Make sure that the cache cleanup in the search for b1Reallocated2
		// did not drop the still active reference b1Reallocated.
		assertSame("Ticket #2563: Lookup dropped active object from cache.", b1Reallocated, b1Reallocated2);
	}

	/**
	 * Regression test for Ticket #26791.
	 */
	public void testUpdateWithOutDatedCurrentData() {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		tx.commit();

		CompiledQuery<KnowledgeObject> query = kb().compileQuery(queryUnresolved(allOf(B_NAME)).setFullLoad());
		/* Searching the stream creates the ResultSet that contains the database row for b1 in
		 * lifetime range [2, Long.MAX_VALUE] */
		try (CloseableIterator<KnowledgeObject> stream = query.searchStream()) {
			Transaction tx2 = begin();
			setA1(b1, "b1_new");
			tx2.commit();

			Transaction tx3 = begin();
			b1.delete();
			tx3.commit();

			/* Accessing the stream tries to apply the data for the time range [2, Long.MAX_VALUE]
			 * of the ResultSet, which must not fail and must actually be a NoOp. */
			assertTrue(stream.hasNext());
			KnowledgeObject loadedB = stream.next();
			assertFalse(loadedB.isAlive());
			assertFalse(stream.hasNext());
		}



	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestDBKnowledgeBaseCache}.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			String testName = "";
			return runOneTest(TestDBKnowledgeBaseCache.class, testName);
		}
		return suite(TestDBKnowledgeBaseCache.class);
	}

}
