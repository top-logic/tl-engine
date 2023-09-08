/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AbstractKBCache;
import com.top_logic.knowledge.service.db2.QueryCache;
import com.top_logic.knowledge.service.db2.SimpleQuery;
import com.top_logic.knowledge.service.db2.SimpleQueryCache;
import com.top_logic.knowledge.service.merge.MergeConflictException;

/**
 * The class {@link TestSimpleQueryCache} tests {@link SimpleQueryCache}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public abstract class TestSimpleQueryCache extends AbstractDBKnowledgeBaseTest {

	KnowledgeObject _b1;

	KnowledgeObject _b2;

	KnowledgeObject _b3;

	SimpleQuery<KnowledgeObject> _simpleQuery;

	private final List<QueryCache<?>> _installedCaches = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Transaction tx = begin();
		_b1 = newB("b1");
		_b1.setAttributeValue(A2_NAME, "match");
		_b2 = newB("b2");
		_b2.setAttributeValue(A2_NAME, "match");
		_b3 = newB("b3");
		_b3.setAttributeValue(A2_NAME, "No match");
		commit(tx);

		_simpleQuery =
			SimpleQuery.queryUnresolved(type(B_NAME), eqBinary(attribute(B_NAME, A2_NAME), literal("match")));
	}

	@Override
	protected void tearDown() throws Exception {
		for (QueryCache<?> cache : _installedCaches) {
			cache.invalidate();
		}
		super.tearDown();
	}

	public void testSimpleSearch() {
		SimpleQueryCache<KnowledgeObject> newQueryCache = newSimpleQueryCache();
		List<KnowledgeObject> searchResult = newQueryCache.getValue();
		assertEquals(set(_b1, _b2), toSet(searchResult));
	}

	SimpleQueryCache<KnowledgeObject> newSimpleQueryCache() {
		SimpleQueryCache<KnowledgeObject> cache = newQueryCache(_simpleQuery);
		_installedCaches.add(cache);
		return cache;
	}

	protected abstract SimpleQueryCache<KnowledgeObject> newQueryCache(SimpleQuery<KnowledgeObject> simpleQuery);

	public void testSearchInTransaction() throws InterruptedException, MergeConflictException, BrokenBarrierException,
			TimeoutException {
		final SimpleQueryCache<KnowledgeObject> cache = newSimpleQueryCache();
		assertEquals(set(_b1, _b2), toSet(cache.getValue()));
		final CyclicBarrier barrier1 = new CyclicBarrier(2);
		final CyclicBarrier barrier2 = new CyclicBarrier(2);
		TestFuture result = inParallel(new Execution() {

			@Override
			public void run() throws Exception {
				Transaction modTX = begin();
				_b2.setAttributeValue(A2_NAME, "no match");
				assertEquals(set(_b1), toSet(cache.getValue()));
				await(barrier1);
				await(barrier2);
				modTX.commit();
				await(barrier1);
			}
		});
		try {
			await(barrier1);
			assertEquals("Must not see values of foreign transaction.", set(_b1, _b2), toSet(cache.getValue()));
			await(barrier2);
			await(barrier1);
			assertEquals("Must not see values of commit until update of session revision.", set(_b1, _b2),
				toSet(cache.getValue()));

			updateSessionRevision();

			assertEquals("cache is not up to date.", set(_b1), toSet(cache.getValue()));
			result.check();
		} catch (TimeoutException ex) {
			result.check();
			throw ex;
		}

	}

	public void testSearchInRevisionBeforeCreation() throws DataObjectException, InterruptedException,
			BrokenBarrierException, TimeoutException {
		final AtomicReference<SimpleQueryCache<KnowledgeObject>> cache =
			new AtomicReference<>();

		final CyclicBarrier barrier1 = new CyclicBarrier(2);
		final CyclicBarrier barrier2 = new CyclicBarrier(2);
		TestFuture result = inParallel(new Execution() {

			@Override
			public void run() throws Exception {
				// initialise session revision
				_b1.getAttributeValue(A2_NAME);
				await(barrier1);
				await(barrier2);
				assertEquals("Session was started before change. Expected old value.", set(_b1, _b2), toSet(cache.get()
					.getValue()));
			}
		});
		try {

			await(barrier1);
			Transaction modTX = begin();
			_b2.setAttributeValue(A2_NAME, "no match");
			modTX.commit();
			cache.set(newSimpleQueryCache());

			assertEquals(set(_b1), toSet(cache.get().getValue()));
			await(barrier2);
			result.check();
		} catch (TimeoutException ex) {
			result.check();
			throw ex;
		}
	}

	public void testCreateCacheInOldRevision() throws InterruptedException, BrokenBarrierException,
			DataObjectException, TimeoutException {
		final AtomicReference<SimpleQueryCache<KnowledgeObject>> cache =
			new AtomicReference<>();

		final CyclicBarrier barrier1 = new CyclicBarrier(2);
		final CyclicBarrier barrier2 = new CyclicBarrier(2);
		TestFuture result = inParallel(new Execution() {

			@Override
			public void run() throws Exception {
				// initialise session revision
				_b1.getAttributeValue(A2_NAME);
				await(barrier1);
				await(barrier2);
				cache.set(newSimpleQueryCache());
				assertEquals("Session was started before change. Expected old value.", set(_b1, _b2), toSet(cache.get()
					.getValue()));
				await(barrier1);
			}
		});
		try {
			await(barrier1);
			Transaction modTX = begin();
			_b2.setAttributeValue(A2_NAME, "no match");
			modTX.commit();
			await(barrier2);
			await(barrier1);

			assertEquals(set(_b1), toSet(cache.get().getValue()));
			result.check();
		} catch (TimeoutException ex) {
			result.check();
			throw ex;
		}

	}

	void await(CyclicBarrier barrier) throws InterruptedException, BrokenBarrierException, TimeoutException {
		barrier.await(1, TimeUnit.SECONDS);
	}

	public void testUpdateCache() throws DataObjectException {
		SimpleQueryCache<KnowledgeObject> cache = newSimpleQueryCache();

		assertEquals(set(_b1, _b2), toSet(cache.getValue()));

		Transaction irrelevantChange = begin();
		newD("d1");
		irrelevantChange.commit();

		assertEquals(set(_b1, _b2), toSet(cache.getValue()));

		Transaction modTX = begin();
		_b2.setAttributeValue(A2_NAME, "no match");
		modTX.commit();

		assertEquals(set(_b1), toSet(cache.getValue()));
	}

	public void testSearchWithCacheReset() throws DataObjectException {
		SimpleQueryCache<KnowledgeObject> cache = newSimpleQueryCache();
		assertEquals(set(_b1, _b2), toSet(cache.getValue()));

		Transaction tx = begin();
		_b1.delete();
		commit(tx);
		assertEquals(set(_b2), toSet(cache.getValue()));

		resetCache(cache);

		Transaction tx2 = begin();
		_b3.setAttributeValue(A2_NAME, "match");
		commit(tx2);

		assertEquals(set(_b2, _b3), toSet(cache.getValue()));
	}

	private void resetCache(AbstractKBCache<?> cache) {
		SoftReference<?> cacheHolder = ReflectionUtils.getValue(cache, "_cacheReference", SoftReference.class);
		cacheHolder.clear();
	}

}
