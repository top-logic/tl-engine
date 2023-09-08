/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.Test;

import org.apache.commons.collections4.BidiMap;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.DObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;

import com.top_logic.basic.NamedConstant;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.IndexedLinkQuery;

/**
 * Test case for {@link IndexedLinkQuery} with live result.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAssociationCacheIndexedLive extends TestAssociationCacheIndexed {

	static final IndexedLinkQuery<String, EObj> INDEXED_LIVE_REFERERS = IndexedLinkQuery.indexedLinkQuery(
		new NamedConstant("indexedLiveReferers"),
		EObj.class, E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME,
		A1_NAME, String.class,
		null, true);

	public void testAddToValues() {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");

		e1.setPolyCurLocal(d1);
		e2.setPolyCurLocal(d1);
		tx1.commit();

		assertIndex(d1, e1, e2);

		Set<EObj> values = resolveIndex(d1).values();
		assertEquals(set(e1, e2), values);
		Transaction tx2 = begin();
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");

		values.add(e3);
		assertEquals(set(e1, e2, e3), values);

		values.add(e4);
		assertEquals(set(e1, e2, e3, e4), values);
		tx2.commit();

		assertIndex(d1, e1, e2, e3, e4);
	}

	public void testAddingTwice() {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");

		BidiMap<String, EObj> index = resolveIndex(d1);
		assertTrue(index.values().add(e1));
		assertTrue(index.values().addAll(list(e2, e3)));
		assertIndex(d1, e1, e2, e3);

		assertFalse(index.values().add(e1));
		assertFalse(index.values().addAll(list(e2, e3)));

		tx1.commit();
		assertIndex(d1, e1, e2, e3);
	}

	public void testRemovingNonMember() {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");

		BidiMap<String, EObj> index = resolveIndex(d1);
		assertTrue(index.values().addAll(list(e1, e2)));
		assertIndex(d1, e1, e2);
		tx1.commit();

		Transaction tx2 = begin();
		assertFalse(index.values().remove(e3));
		assertFalse(index.values().remove(null));
		assertFalse(index.values().remove("foo"));
		assertTrue(index.values().remove(e2));
		assertFalse(index.values().remove(e2));
		assertFalse(index.values().removeAll(list(e2, e3)));
		assertTrue(index.values().removeAll(list(e1, e3)));

		tx2.commit();
		assertIndex(d1);
	}

	public void testRemoveFromMap() {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");

		BidiMap<String, EObj> index = resolveIndex(d1);

		index.values().addAll(list(e1, e2, e3, e4));
		assertIndex(d1, e1, e2, e3, e4);

		assertNull(index.remove("foobar"));

		assertEquals(e1, index.remove("e1"));
		assertIndex(d1, e2, e3, e4);

		assertEquals("e2", index.removeValue(e2));
		assertIndex(d1, e3, e4);

		assertNull(index.removeValue(e2));

		index.clear();
		assertIndex(d1);

		tx1.commit();
	}

	public void testRemoveFromKeySetIterator() {
		final DObj d1 = scenario();

		Set<String> keySet = resolveIndex(d1).keySet();
		Iterator<String> it = keySet.iterator();
		assertTrue(it.hasNext());
		String firstKey = it.next();
		assertNotNull(firstKey);
		Transaction tx2 = begin();
		it.remove();
		assertTrue(it.hasNext());
		String secondKey = it.next();
		assertNotNull(secondKey);
		it.remove();

		HashSet<String> leftOver = new HashSet<>();
		while (it.hasNext()) {
			leftOver.add(it.next());
		}
		tx2.commit();

		check(d1, firstKey, secondKey, leftOver);
	}

	public void testRemoveFromValuesIterator() {
		final DObj d1 = scenario();

		Set<EObj> values = resolveIndex(d1).values();
		Iterator<EObj> it = values.iterator();
		assertTrue(it.hasNext());
		EObj first = it.next();
		assertNotNull(first);
		Transaction tx2 = begin();
		it.remove();
		assertTrue(it.hasNext());
		EObj second = it.next();
		assertNotNull(second);
		it.remove();

		HashSet<String> leftOver = new HashSet<>();
		while (it.hasNext()) {
			leftOver.add(it.next().getA1());
		}
		tx2.commit();

		check(d1, first.getA1(), second.getA1(), leftOver);
	}

	public void testRemoveFromEntrySetIterator() {
		final DObj d1 = scenario();

		Set<Entry<String, EObj>> entrySet = resolveIndex(d1).entrySet();
		Iterator<Entry<String, EObj>> it = entrySet.iterator();
		assertTrue(it.hasNext());
		Entry<String, EObj> first = it.next();
		assertNotNull(first.getKey());
		assertNotNull(first.getValue());
		assertEquals(first.getKey(), first.getValue().getA1());
		Transaction tx2 = begin();
		it.remove();
		assertTrue(it.hasNext());
		Entry<String, EObj> second = it.next();
		assertNotNull(second.getKey());
		assertNotNull(second.getValue());
		assertEquals(second.getKey(), second.getValue().getA1());
		it.remove();

		HashSet<String> leftOver = new HashSet<>();
		while (it.hasNext()) {
			leftOver.add(it.next().getValue().getA1());
		}
		tx2.commit();

		check(d1, first.getKey(), second.getKey(), leftOver);
	}

	public void testConcurrentModificationGlobal() {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");

		BidiMap<String, EObj> index = resolveIndex(d1);
		assertTrue(index.values().addAll(list(e1, e2, e3)));
		tx1.commit();
		assertIndex(d1, e1, e2, e3);

		// Start iterating the global view of the cache.
		Iterator<Entry<String, EObj>> it1 = index.entrySet().iterator();
		Iterator<String> it2 = index.keySet().iterator();
		Iterator<EObj> it3 = index.values().iterator();

		Transaction tx2 = begin();
		final EObj e4 = EObj.newEObj("e4");

		assertTrue(it1.hasNext());
		assertTrue(it2.hasNext());
		assertTrue(it3.hasNext());

		assertNotNull(it1.next());
		assertNotNull(it2.next());
		assertNotNull(it3.next());

		e4.setPolyCurLocal(d1);

		assertConcurrentModificationException(it1);
		assertConcurrentModificationException(it2);
		assertConcurrentModificationException(it3);

		tx2.commit();
	}

	public void testConcurrentModificationLocal() {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");

		BidiMap<String, EObj> index = resolveIndex(d1);
		assertTrue(index.values().addAll(list(e1, e2, e3)));
		tx1.commit();
		assertIndex(d1, e1, e2, e3);

		Transaction tx2 = begin();
		final EObj e4 = EObj.newEObj("e4");
		e4.setPolyCurLocal(d1);

		// Start iterating the local view of the cache.
		Iterator<Entry<String, EObj>> it1 = index.entrySet().iterator();
		Iterator<String> it2 = index.keySet().iterator();
		Iterator<EObj> it3 = index.values().iterator();

		assertTrue(it1.hasNext());
		assertTrue(it2.hasNext());
		assertTrue(it3.hasNext());

		assertNotNull(it1.next());
		assertNotNull(it2.next());
		assertNotNull(it3.next());

		final EObj e5 = EObj.newEObj("e5");
		e5.setPolyCurLocal(d1);

		assertConcurrentModificationException(it1);
		assertConcurrentModificationException(it2);
		assertConcurrentModificationException(it3);

		tx2.commit();
	}

	public void testConcurrentModificationAfterChangeFromIterator() {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");

		BidiMap<String, EObj> index = resolveIndex(d1);
		assertTrue(index.values().addAll(list(e1, e2, e3)));
		tx1.commit();
		assertIndex(d1, e1, e2, e3);

		// Start iterating the global view of the cache.
		Iterator<EObj> it3 = index.values().iterator();

		Transaction tx2 = begin();

		assertTrue(it3.hasNext());
		assertNotNull(it3.next());

		// Perform change from iterator.
		it3.remove();

		assertTrue(it3.hasNext());
		EObj value;
		assertNotNull(value = it3.next());

		// Perform change from outside.
		value.tDelete();

		assertConcurrentModificationException(it3);

		tx2.commit();
	}

	private void assertConcurrentModificationException(Iterator<?> it) {
		try {
			it.next();
			fail("Must fail.");
		} catch (ConcurrentModificationException ex) {
			// expected.
		}
	}

	private void check(final DObj d1, String firstKey, String secondKey, Set<String> leftOver) {
		assertEquals(2, leftOver.size());
		assertFalse(leftOver.contains(firstKey));
		assertFalse(leftOver.contains(secondKey));
		assertEquals(leftOver, resolveIndex(d1).keySet());
	}

	private DObj scenario() {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");

		e1.setPolyCurLocal(d1);
		e2.setPolyCurLocal(d1);
		e3.setPolyCurLocal(d1);
		e4.setPolyCurLocal(d1);
		tx1.commit();

		assertIndex(d1, e1, e2, e3, e4);
		return d1;
	}

	@Override
	protected IndexedLinkQuery<String, EObj> query() {
		return INDEXED_LIVE_REFERERS;
	}

	public static Test suite() {
		return suite(TestAssociationCacheIndexedLive.class);
	}

}
