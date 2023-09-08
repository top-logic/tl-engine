/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.DObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DeletedObjectAccess;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.wrap.AbstractWrapper;

/**
 * Test case for {@link OrderedLinkQuery}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public abstract class TestAssociationCacheOrdered extends AbstractDBKnowledgeBaseClusterTest {

	private static final AssociationSetQuery<EObj> UNORDERED = AssociationQuery.createQuery("unordered", EObj.class,
		E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME);

	public void testOrderedLinkCache() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");

		setList(d1, list(e2, e4, e1, e3));
		assertEquals(list(e2, e4, e1, e3), getList(d1));

		// Reorder.
		setList(d1, list(e1, e2, e3, e4));
		assertEquals(list(e1, e2, e3, e4), getList(d1));
		tx1.commit();

		Transaction tx2 = begin();
		final EObj e5 = EObj.newEObj("e5");
		final EObj e6 = EObj.newEObj("e6");

		// Remove.
		setList(d1, list(e2, e3));
		assertEquals(list(e2, e3), getList(d1));
		tx2.commit();
		assertEquals(list(e2, e3), getList(d1));

		// Add.
		Transaction tx3 = begin();
		setList(d1, list(e5, e2, e6, e3));
		assertEquals(list(e5, e2, e6, e3), getList(d1));

		assertEquals(list(e5, e2, e6, e3), getList(d1));
		tx3.commit();
		assertEquals(list(e5, e2, e6, e3), getList(d1));

		// Clear.
		Transaction tx4 = begin();
		setList(d1, list(e6));
		assertEquals(list(e6), getList(d1));

		setList(d1, Collections.<EObj> emptyList());
		assertEquals(list(), getList(d1));
		tx4.commit();
		assertEquals(list(), getList(d1));
	}

	public void testAllocatingGlobalCacheWithLocalModifications() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");

		setList(d1, list(e2, e4, e1, e3));
		tx1.commit();

		Transaction tx2 = begin();
		// Reorder attributes without having allocated a cache before.
		setList(d1, list(e1, e2, e3, e4));

		// Allocate cache with local modifications.
		assertEquals(list(e1, e2, e3, e4), getList(d1));

		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertEquals(list(e2, e4, e1, e3), getList(d1));
			}
		});

		tx2.commit();
		assertEquals(list(e1, e2, e3, e4), getList(d1));
		inThread(new Execution() {
			@Override
			public void run() throws Exception {
				assertEquals(list(e1, e2, e3, e4), getList(d1));
			}
		});

	}

	public void testUpdateCacheOnConcurrentChange() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("d1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");

		setList(d1, list(e1, e2));
		tx1.commit();

		Transaction tx2 = begin();

		EObj e4 = EObj.newEObj("e4");
		setList(d1, list(e1, e2, e4));

		/* Install cache, that is updated with the concurrent change during commit. */
		List<EObj> cachedList = getList(d1);
		assertEquals(list(e1, e2, e4), cachedList);

		AtomicReference<EObj> concurrentCreated = new AtomicReference<>();

		inThread(new Execution() {

			@Override
			public void run() throws Exception {
				Transaction tx3 = begin();
				concurrentCreated.set(EObj.newEObj("e3"));
				setList(d1, list(concurrentCreated.get(), e1, e2));
				tx3.commit();
			}

		});

		try {
			tx2.commit();
		} catch (DeletedObjectAccess ex) {
			fail(
				"Ticket #24984: When updating local cache during commit with concurrent changes, the values of the cocurren commit must be accessed in the 'new' commit revision.",
				ex);
		}
		assertEquals(list(concurrentCreated.get(), e1, e2, e4), getList(d1));
	}

	protected void addChildElement(final DObj owner, final EObj child) {
		child.setReference(REFERENCE_POLY_CUR_LOCAL_NAME, owner);
		updateIndices(list(child), E1_NAME);
	}

	protected List<EObj> getList(DObj d1) {
		return AbstractWrapper.resolveLinks(d1, query());
	}

	protected void setList(DObj owner, List<EObj> list) {
		Set<EObj> current = getSet(owner);
		HashSet<EObj> toRemove = new HashSet<>(current);
		toRemove.removeAll(list);
		for (EObj element : toRemove) {
			element.setReference(REFERENCE_POLY_CUR_LOCAL_NAME, null);
		}
		String attributeName = E1_NAME;
		updateIndices(list, attributeName);
		for (EObj element : list) {
			if (!current.contains(element)) {
				element.setReference(REFERENCE_POLY_CUR_LOCAL_NAME, owner);
			}
		}
	}

	protected abstract void updateIndices(List<EObj> list, String attributeName);

	protected Set<EObj> getSet(DObj owner) {
		return AbstractWrapper.resolveLinks(owner, UNORDERED);
	}

	protected abstract OrderedLinkQuery<EObj> query();

}
