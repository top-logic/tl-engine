/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.util.OrderedLinkUtil.*;
import static java.lang.Math.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.DObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeItem;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.model.TLObject;

/**
 * Test case for {@link OrderedLinkQuery} with live result.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAssociationCacheOrderedLive extends TestAssociationCacheOrdered {

	private static final OrderedLinkQuery<EObj> ORDERED_LIVE = AssociationQuery.createOrderedLinkQuery("ordered",
		EObj.class, E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME, E1_NAME, null, true);

	private boolean _useListOperations;

	public void testAdd() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");
		final EObj e5 = EObj.newEObj("e5");

		List<EObj> list = getList(d1);
		list.add(e1);
		list.add(e2);
		list.add(0, e3);
		list.add(1, e4);
		list.add(4, e5);
		assertEquals(list(e3, e4, e1, e2, e5), list);
		tx1.commit();
		assertEquals(list(e3, e4, e1, e2, e5), list);
	}

	public void testRemove() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");
		final EObj e5 = EObj.newEObj("e5");

		List<EObj> list = getList(d1);
		list.addAll(list(e3, e4, e1, e2, e5));
		tx1.commit();

		Transaction tx2 = begin();
		list.remove(1);
		list.remove(0);
		list.remove(e5);
		assertEquals(list(e1, e2), list);
		tx2.commit();
		assertEquals(list(e1, e2), list);
	}

	public void testRemoveAll() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");
		final EObj e5 = EObj.newEObj("e5");

		List<EObj> list = getList(d1);
		list.addAll(list(e3, e4, e1, e2, e5));
		tx1.commit();

		Transaction tx2 = begin();
		list.removeAll(list(e4, e3, e5));
		assertEquals(list(e1, e2), list);
		tx2.commit();
		assertEquals(list(e1, e2), list);
	}

	public void testClear() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");
		final EObj e5 = EObj.newEObj("e5");

		List<EObj> list = getList(d1);
		list.addAll(list(e3, e4, e1, e2, e5));
		tx1.commit();

		Transaction tx2 = begin();
		list.clear();
		assertEquals(list(), list);
		tx2.commit();
		assertEquals(list(), list);
	}

	public void testAddAll() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");
		final EObj e5 = EObj.newEObj("e5");

		List<EObj> list = getList(d1);
		list.addAll(list(e1, e2));
		list.addAll(0, list(e3, e4));
		list.addAll(4, list(e5));
		list.addAll(3, Collections.<EObj> emptyList());
		assertEquals(list(e3, e4, e1, e2, e5), list);
		tx1.commit();
		assertEquals(list(e3, e4, e1, e2, e5), list);
	}

	public void testConcurrentModification() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");
		final EObj e5 = EObj.newEObj("e5");

		List<EObj> list = getList(d1);
		list.addAll(list(e1, e2, e3, e4, e5));
		tx1.commit();
		assertEquals(list(e1, e2, e3, e4, e5), list);

		{
			Transaction tx2 = begin();
			Iterator<EObj> it = list.iterator();
			setList(d1, list(e1, e2, e3, e4));
			assertConcurrentModification(it);
			tx2.commit();
		}

		{
			Transaction tx3 = begin();
			Iterator<EObj> it = list.iterator();

			assertTrue(it.hasNext());
			assertNotNull(it.next());

			setList(d1, list(e1, e2, e3));
			assertConcurrentModification(it);
			tx3.commit();
		}

		{
			Iterator<EObj> it = list.iterator();

			assertTrue(it.hasNext());
			assertNotNull(it.next());

			Transaction tx4 = begin();
			it.remove();

			assertTrue(it.hasNext());
			assertNotNull(it.next());

			setList(d1, list(e1, e2, e3, e4));
			assertConcurrentModification(it);
			tx4.commit();
		}
	}

	private void assertConcurrentModification(Iterator<EObj> it) {
		try {
			it.next();
			fail("Expecting concurrent modification detection.");
		} catch (ConcurrentModificationException ex) {
			// Expected.
		}
	}

	public void testIndexManagement() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");

		List<EObj> list = getList(d1);
		List<EObj> expected = new ArrayList<>(list(e1, e2));
		list.addAll(expected);
		tx1.commit();
		assertEquals(expected, list);

		assertEquals(tx1.getCommitRevision().getCommitNumber(), lastUpdate(e1));
		assertEquals(tx1.getCommitRevision().getCommitNumber(), lastUpdate(e2));

		Transaction tx2 = begin();

		// See index distribution: Appending reserves OrderedLinkUtil.APPEND_INC - 1 free slots
		// between each entry, inserting reserves OrderedLinkUtil.INSERT_INC slots. When inserting
		// between two appended elements, (APPEND_INC / INSERT_INC) - 1 inserts can be done with the
		// maximum increment of INSERT_INC slots. Afterwards log2(INSERT_INC) additional inserts are
		// possible before a complete reorganization is necessary.
		int maxInsertCnt = ((APPEND_INC / INSERT_INC) - 1) + log2(INSERT_INC);

		for (int n = 0; n < maxInsertCnt; n++) {
			EObj inserted = EObj.newEObj("inserted" + n);
			list.add(1 + n, inserted);
			expected.add(1 + n, inserted);
		}
		tx2.commit();

		assertEquals(expected, list);

		assertEquals(tx1.getCommitRevision().getCommitNumber(), lastUpdate(e1));
		assertEquals(tx1.getCommitRevision().getCommitNumber(), lastUpdate(e2));

		Transaction tx3 = begin();
		for (int n = 0; n < 1; n++) {
			EObj inserted = EObj.newEObj("newlyInserted" + n);
			list.add(1 + maxInsertCnt + n, inserted);
			expected.add(1 + maxInsertCnt + n, inserted);
		}
		tx3.commit();

		assertEquals(expected, list);

		assertEquals(tx3.getCommitRevision().getCommitNumber(), lastUpdate(e1));
		assertEquals(tx3.getCommitRevision().getCommitNumber(), lastUpdate(e2));

		for (int n = 0; n < 4; n++) {
			EObj inserted = EObj.newEObj("newlyInserted" + n);
			list.add(1, inserted);
			expected.add(1, inserted);
		}

		assertEquals(tx3.getCommitRevision().getCommitNumber(), lastUpdate(e1));
		assertEquals(tx3.getCommitRevision().getCommitNumber(), lastUpdate(e2));

	}

	private int log2(int x) {
		return (int) Math.round((log(x) / log(2)));
	}

	public void testIndexManagement2() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");
		tx1.commit();

		List<EObj> expected = new ArrayList<>();
		List<EObj> list = getList(d1);

		assertEquals(expected, list);

		for (int n = 0; n < 100; n++) {

			Transaction tx = begin();
			// Add a single item.
			EObj inserted = EObj.newEObj("inserted" + n);
			list.add(inserted);
			expected.add(inserted);
			tx.commit();

			assertEquals("Iteration: " + n, expected, list);
		}

	}

	public void testIndexManagement3() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");
		tx1.commit();

		List<EObj> expected = new ArrayList<>();
		List<EObj> list = getList(d1);

		assertEquals(expected, list);

		for (int n = 0; n < 20; n++) {
			Transaction tx = begin();

			List<EObj> inserted = new ArrayList<>();
			// Add an increasing number of items.
			for (int m = 0; m < n; m++) {
				EObj insertedElement = EObj.newEObj("inserted" + m + "." + n);
				inserted.add(insertedElement);
			}
			list.addAll(inserted);
			expected.addAll(inserted);
			tx.commit();

			assertEquals("Iteration: " + n, expected, list);
		}

	}

	public void testIndexManagement4() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");
		tx1.commit();

		List<EObj> expected = new ArrayList<>();
		List<EObj> list = getList(d1);

		assertEquals(expected, list);

		for (int n = 0; n < 20; n++) {
			Transaction tx = begin();

			List<EObj> inserted = new ArrayList<>();
			// Always add two a time.
			for (int m = 0; m < 2; m++) {
				EObj insertedElement = EObj.newEObj("inserted" + m + "." + n);
				inserted.add(insertedElement);
			}
			list.addAll(inserted);
			expected.addAll(inserted);
			tx.commit();

			assertEquals("Iteration: " + n, expected, list);
		}

	}

	public void testIndexManagementAddAll() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");

		List<EObj> list = getList(d1);
		List<EObj> expected = new ArrayList<>(list(e1, e2));
		list.addAll(expected);
		tx1.commit();
		assertEquals(expected, list);

		assertEquals(tx1.getCommitRevision().getCommitNumber(), lastUpdate(e1));
		assertEquals(tx1.getCommitRevision().getCommitNumber(), lastUpdate(e2));

		Transaction tx2 = begin();

		// There is a limit of OrderedLinkUtil.APPEND_INC - 1 objects
		// that can be inserted between two appended objects.
		int insertCnt = APPEND_INC - 1;

		List<EObj> toAdd = new ArrayList<>();
		for (int n = 0; n < insertCnt; n++) {
			EObj inserted = EObj.newEObj("inserted" + n);
			toAdd.add(inserted);
			expected.add(1 + n, inserted);
		}
		list.addAll(1, toAdd);
		tx2.commit();

		assertEquals(expected, list);

		assertEquals(tx1.getCommitRevision().getCommitNumber(), lastUpdate(e1));
		assertEquals(tx1.getCommitRevision().getCommitNumber(), lastUpdate(e2));

		Transaction tx4 = begin();

		int insertCnt3 = 1;
		List<EObj> toAdd3 = new ArrayList<>();
		for (int n = 0; n < insertCnt3; n++) {
			EObj inserted = EObj.newEObj("inserted" + n);
			toAdd3.add(inserted);
			expected.add(1 + n, inserted);
		}
		list.addAll(1, toAdd3);
		tx4.commit();

		assertEquals(expected, list);

		assertEquals(tx4.getCommitRevision().getCommitNumber(), lastUpdate(e1));
		assertEquals(tx4.getCommitRevision().getCommitNumber(), lastUpdate(e2));
	}

	private long lastUpdate(TLObject e1) {
		return ((DBKnowledgeItem) e1.tHandle()).getLastUpdate();
	}

	public void testEquals() throws DataObjectException {
		Transaction tx1 = begin();
		final DObj d1 = DObj.newDObj("b1");

		final EObj e1 = EObj.newEObj("e1");
		final EObj e2 = EObj.newEObj("e2");
		final EObj e3 = EObj.newEObj("e3");
		final EObj e4 = EObj.newEObj("e4");
		final EObj e5 = EObj.newEObj("e5");

		List<EObj> list = getList(d1);
		list.addAll(list(e3, e4, e1, e2, e5));
		assertTrue(list.equals(list(e3, e4, e1, e2, e5)));
		assertEquals(list(e3, e4, e1, e2, e5).hashCode(), list.hashCode());
		tx1.commit();
		assertTrue(list.equals(list(e3, e4, e1, e2, e5)));
		assertEquals(list(e3, e4, e1, e2, e5).hashCode(), list.hashCode());
	}

	public void testOrderedLinkCacheList() throws DataObjectException {
		_useListOperations = true;
		try {
			testOrderedLinkCache();
		} finally {
			_useListOperations = false;
		}
	}

	public void testAllocatingGlobalCacheWithLocalModificationsList() throws DataObjectException {
		_useListOperations = true;
		try {
			super.testAllocatingGlobalCacheWithLocalModifications();
		} finally {
			_useListOperations = false;
		}
	}

	@Override
	protected void setList(DObj owner, List<EObj> newList) {
		if (_useListOperations) {
			List<EObj> actual = getList(owner);

			ListIterator<EObj> it = actual.listIterator();
			Iterator<EObj> newIt = newList.iterator();
			for (; newIt.hasNext();) {
				EObj newValue = newIt.next();

				find:
				{
					while (it.hasNext()) {
						EObj existingValue = it.next();

						if (existingValue == newValue) {
							break find;
						} else {
							it.remove();
						}
					}

					it.add(newValue);
				}
			}
			while (it.hasNext()) {
				it.next();
				it.remove();
			}
		} else {
			super.setList(owner, newList);
		}
	}

	@Override
	protected void updateIndices(List<EObj> list, String attributeName) {
		OrderedLinkUtil.updateIndices(list, attributeName);
	}

	@Override
	protected OrderedLinkQuery<EObj> query() {
		return ORDERED_LIVE;
	}

	public static Test suite() {
		return suite(TestAssociationCacheOrderedLive.class);
	}

}
