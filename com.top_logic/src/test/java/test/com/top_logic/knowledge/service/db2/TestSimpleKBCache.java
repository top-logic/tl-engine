/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;


import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Test;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DeletedObjectAccess;
import com.top_logic.knowledge.service.db2.SimpleKBCache;


/**
 * Test for {@link SimpleKBCache}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestSimpleKBCache extends AbstractDBKnowledgeBaseTest {
	
	KnowledgeObject _e1;

	SimpleKBCache<Map<String, KnowledgeItem>> _allReferencedObjects;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Transaction tx = begin();
		_e1 = newE("e1");
		_allReferencedObjects = new AllReferencedObjects(_e1);
		commit(tx);
	}

	public void testUpdateValue() throws DataObjectException {
		Transaction tx = begin();
		assertEquals(0, _allReferencedObjects.getValue().size());
		KnowledgeObject d1 = newD("d1");
		_e1.setAttributeValue(REFERENCE_MONO_CUR_GLOBAL_NAME, d1);
		assertEquals(d1, _allReferencedObjects.getValue().get(REFERENCE_MONO_CUR_GLOBAL_NAME));
		commit(tx);
		assertEquals(d1, _allReferencedObjects.getValue().get(REFERENCE_MONO_CUR_GLOBAL_NAME));
	}

	public void testAccessDeletedInOldRevision() throws InterruptedException, DataObjectException, TimeoutException,
			BrokenBarrierException {
		final CyclicBarrier barrier1 = new CyclicBarrier(3);
		final CyclicBarrier barrier2 = new CyclicBarrier(3);
		TestFuture result = inParallel(new Execution() {

			@Override
			public void run() throws Exception {
				assertEquals("No reference yet.", null,
					_allReferencedObjects.getValue().get(REFERENCE_MONO_CUR_GLOBAL_NAME));
				await(barrier1);
				await(barrier2);
				await(barrier1);
				await(barrier2);
				assertEquals("Foreign commit is not seen yet.", null,
					_allReferencedObjects.getValue().get(REFERENCE_MONO_CUR_GLOBAL_NAME));
			}
		});
		TestFuture result2 = inParallel(new Execution() {

			@Override
			public void run() throws Exception {
				Transaction tx = begin();
				KnowledgeObject d1 = newD("d1");
				_e1.setAttributeValue(REFERENCE_MONO_CUR_GLOBAL_NAME, d1);
				assertEquals(d1, _allReferencedObjects.getValue().get(REFERENCE_MONO_CUR_GLOBAL_NAME));
				await(barrier1);
				await(barrier2);
				assertEquals("Foreign transaction is not seen.", d1,
					_allReferencedObjects.getValue().get(REFERENCE_MONO_CUR_GLOBAL_NAME));
				await(barrier1);
				await(barrier2);
				assertEquals("Foreign commit is not seen yet.", d1,
					_allReferencedObjects.getValue().get(REFERENCE_MONO_CUR_GLOBAL_NAME));
				commit(tx, "Merge must not be successful.", true);
			}
		});
		try {
			await(barrier1);
			Transaction tx = begin();
			assertTrue(_allReferencedObjects.getValue().isEmpty());
			_e1.delete();
			try {
				_allReferencedObjects.getValue();
				fail(_e1 + " was deleted.");
			} catch (DeletedObjectAccess ex) {
				// expected;
			}

			await(barrier2);
			await(barrier1);
			commit(tx);
			await(barrier2);
			result.check();
			result2.check();
		} catch (TimeoutException ex) {
			result2.check();
			result.check();
			throw ex;
		}
	}

	void await(CyclicBarrier barrier) throws InterruptedException, BrokenBarrierException, TimeoutException {
		barrier.await(5, TimeUnit.SECONDS);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestSimpleKBCache}.
	 */
	public static Test suite() {
		return suite(TestSimpleKBCache.class);
	}

}

