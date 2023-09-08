/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections4.BidiMap;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.DObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;

import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.IndexedLinkQuery;

/**
 * Base class for testing {@link IndexedLinkQuery}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public abstract class TestAssociationCacheIndexed extends AbstractDBKnowledgeBaseClusterTest {

	static final AssociationSetQuery<EObj> REFERERS = AssociationQuery.createQuery("values",
		EObj.class, E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME);

	public void testIndexedLinkCache() {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");
		final DObj d2 = DObj.newDObj("b2");
		final DObj d3 = DObj.newDObj("b3");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");

		assertIndex(d1);

		e1.setPolyCurLocal(d1);
		assertIndex(d1, e1);

		e2.setPolyCurLocal(d1);
		assertIndex(d1, e1, e2);

		e3.setPolyCurLocal(d2);
		e4.setPolyCurLocal(d2);

		assertIndex(d1, e1, e2);
		assertIndex(d3);
		tx1.commit();
		assertIndex(d2, e3, e4);

		Transaction tx2a = begin();
		e4.setPolyCurLocal((DObj) null);
		tx2a.commit();
		assertIndex(d2, e3);

		Transaction tx2b = begin();
		e1.setPolyCurLocal(d2);
		tx2b.commit();

		assertIndex(d1, e2);
		assertIndex(d2, e1, e3);
		assertIndex(d3);

		refetchNode2();
		DObj d2Node2 = node2Obj(d2);
		EObj e1Node2 = node2Obj(e1);
		EObj e3Node2 = node2Obj(e3);
		assertIndex(d2Node2, e1Node2, e3Node2);

		Transaction tx3 = begin();
		assertIndex(d2, e1, e3);
		e3.tDelete();
		assertIndex(d2, e1);
		e1.tDelete();
		assertIndex(d2);
		tx3.commit();
		assertIndex(d2);

		assertIndex(d2Node2, e1Node2, e3Node2);
		refetchNode2();
		assertIndex(d2Node2);

	}

	public void testAllocatingGlobalCacheWithLocalModifications() {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");
		final EObj e5 = EObj.newEObj("e5");

		setValue(d1, set(e1, e2, e3, e4));
		tx1.commit();

		Transaction tx2 = begin();
		// Change indexing value locally and add to collection.
		e3.tDelete();
		e5.setA1("e1.5");
		e5.setPolyCurLocal(d1);

		// Allocate cache with local modifications.
		assertEquals(set("e1", "e2", "e4", "e1.5"), getKeys(d1));

		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertEquals(set("e1", "e2", "e3", "e4"), getKeys(d1));
			}
		});

		tx2.commit();
		assertEquals(set("e1", "e2", "e4", "e1.5"), getKeys(d1));
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertEquals(set("e1", "e2", "e4", "e1.5"), getKeys(d1));
			}
		});

	}

	Set<EObj> getValue(DObj owner) {
		return resolveIndex(owner).values();
	}

	Set<String> getKeys(DObj owner) {
		return resolveIndex(owner).keySet();
	}

	/**
	 * Update the indexed collection without touching the indexed cache.
	 */
	void setValue(DObj owner, Collection<EObj> values) {
		Set<EObj> current = resolveValues(owner);
		HashSet<EObj> toRemove = new HashSet<>(current);
		toRemove.removeAll(values);
		for (EObj value : toRemove) {
			value.setPolyCurLocal((EObj) null);
		}

		for (EObj value : values) {
			if (current.contains(value)) {
				continue;
			}
			value.setPolyCurLocal(owner);
		}
	}

	public void testIndexedLinkCacheExistingClash() {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e1Clash = EObj.newEObj("e1");
		tx1.commit();

		Transaction tx2 = begin();
		e1.setPolyCurLocal(d1);
		e2.setPolyCurLocal(d1);
		e1Clash.setPolyCurLocal(d1);
		tx2.commit();

		assertEquals(set("e1", "e2"), resolveIndex(d1).keySet());
	}

	public void testIndexedLinkCacheCreatingClash() {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e1Clash = EObj.newEObj("e1");
		tx1.commit();

		Transaction tx2 = begin();
		e1.setPolyCurLocal(d1);
		e2.setPolyCurLocal(d1);
		assertIndex(d1, e1, e2);
		e1Clash.setPolyCurLocal(d1);
		assertEquals(set("e1", "e2"), resolveIndex(d1).keySet());
		tx2.commit();
	}

	protected void assertIndex(DObj d, EObj... es) {
		{
			BidiMap<String, EObj> index = resolveIndex(d);
			for (EObj e : es) {
				assertTrue(index.containsKey(e.getA1()));
				assertTrue(index.containsValue(e));
				assertEquals(e, index.get(e.getA1()));
				assertEquals(e.getA1(), index.getKey(e));
				assertTrue(index.keySet().contains(e.getA1()));
				assertTrue(index.values().contains(e));

				assertTrue(index.inverseBidiMap().containsValue(e.getA1()));
				assertTrue(index.inverseBidiMap().containsKey(e));
				assertEquals(e, index.inverseBidiMap().getKey(e.getA1()));
				assertEquals(e.getA1(), index.inverseBidiMap().get(e));
				assertTrue(index.inverseBidiMap().values().contains(e.getA1()));
				assertTrue(index.inverseBidiMap().keySet().contains(e));
			}
			assertTrue(index.isEmpty() == (es.length == 0));
			assertTrue(index.keySet().isEmpty() == (es.length == 0));
			assertTrue(index.values().isEmpty() == (es.length == 0));
			assertTrue(index.entrySet().isEmpty() == (es.length == 0));
			assertTrue(index.keySet().containsAll(keys(es)));
			assertTrue(index.values().containsAll(Arrays.asList(es)));

			assertTrue(index.inverseBidiMap().isEmpty() == (es.length == 0));
			assertTrue(index.inverseBidiMap().keySet().isEmpty() == (es.length == 0));
			assertTrue(index.inverseBidiMap().values().isEmpty() == (es.length == 0));
			assertTrue(index.inverseBidiMap().entrySet().isEmpty() == (es.length == 0));
			assertTrue(index.inverseBidiMap().values().containsAll(keys(es)));
			assertTrue(index.inverseBidiMap().keySet().containsAll(Arrays.asList(es)));

			assertEquals(es.length, index.size());
			assertEquals(es.length, index.keySet().size());
			assertEquals(es.length, index.values().size());
			assertEquals(es.length, index.entrySet().size());

			assertEquals(es.length, index.inverseBidiMap().size());
			assertEquals(es.length, index.inverseBidiMap().keySet().size());
			assertEquals(es.length, index.inverseBidiMap().values().size());
			assertEquals(es.length, index.inverseBidiMap().entrySet().size());
		}
	}

	private Collection<?> keys(EObj[] es) {
		ArrayList<String> result = new ArrayList<>(es.length);
		for (EObj e : es) {
			result.add(e.getA1());
		}
		return result;
	}

	protected BidiMap<String, EObj> resolveIndex(DObj d) {
		return kb().resolveLinks(d.tHandle(), query());
	}

	protected Set<EObj> resolveValues(DObj d) {
		return kb().resolveLinks(d.tHandle(), REFERERS);
	}

	protected abstract IndexedLinkQuery<String, EObj> query();

}
