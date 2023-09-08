/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.DBKnowledgeBaseTestSetup;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioImpl;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.CObj;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.util.OrderedLinkUtil;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.model.TLObject;

/**
 * Test case for {@link OrderedLinkUtil}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestOrderedLinkUtil extends AbstractDBKnowledgeBaseTest {

	private static final int MIN_STABLE_INSERTS = 14;

	/**
	 * The internal ordering implementation relies on order attributes on links. The implementation
	 * ensures, that values are only updated, if necessary, see {@link #testMinimalUpdate()}. Due to
	 * the limited range of values used for the index attribute (32 - 1 bit), after approximately 31
	 * incremental insert operations at the same location, the order value on all currently active
	 * links must be update to allow for further inserts.
	 */
	private static final int INCREMENTAL_INSERT_CNT = 65;

	private static class ABOrdered {

		/**
		 * Name of {@link #TYPE}.
		 */
		static final String NAME = "AB_ORDERED";

		static final String ORDER_NAME = "order";

		/**
		 * Association that has an ordering attribute for ordering links.
		 * 
		 * @see #ORDER_NAME
		 */
		static final MOClass TYPE = new MOKnowledgeItemImpl(ABOrdered.NAME);
		static {
			TYPE.setAbstract(false);
			TYPE.setSuperclass(KnowledgeBaseTestScenarioImpl.KNOWLEDGE_ASSOCIATION);
			try {
				TYPE.addAttribute(new MOAttributeImpl(ABOrdered.ORDER_NAME, MOPrimitive.INTEGER, true));
			} catch (DuplicateAttributeException e) {
				throw new UnreachableAssertion(e);
			}
		}

		static final int getOrder(KnowledgeAssociation link) {
			try {
				return (int) link.getAttributeValue(ORDER_NAME);
			} catch (NoSuchAttributeException ex) {
				throw new IllegalArgumentException(
					"link of foreign type: expected '" + TYPE + "' actual '" + link.tTable() + "'", ex);
			}
		}

		static final int setOrder(KnowledgeAssociation link, int order) throws DataObjectException {
			try {
				return (int) link.setAttributeValue(ORDER_NAME, order);
			} catch (NoSuchAttributeException ex) {
				throw new IllegalArgumentException(
					"link of foreign type: expected '" + TYPE + "' actual '" + link.tTable() + "'", ex);
			}
		}

		static final OrderedLinkQuery<KnowledgeAssociation> ORDER_QUERY = AssociationQuery
			.createOutgoingOrderedQuery(
				ABOrdered.NAME + "_dest",
				ABOrdered.NAME, ABOrdered.ORDER_NAME);

		static final OrderedLinkQuery<KnowledgeAssociation> ORDER_QUERY_LIVE =
			AssociationQuery.createOrderedLinkQuery(ABOrdered.NAME + "_destLive", KnowledgeAssociation.class,
				ABOrdered.NAME, DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, ABOrdered.ORDER_NAME, null, true);

	}

	private static final List<? extends TLObject> EMPTY = Collections.emptyList();

	public void testLiveEndList() throws KnowledgeBaseException {
		Transaction tx = begin();
		BObj b1 = BObj.newBObj("b1");

		tx.commit();

		List<CObj> liveEndList = AbstractWrapper.resolveOrderedValue(b1, ABOrdered.ORDER_QUERY_LIVE, CObj.class);
		assertEquals(list(), liveEndList);

		Transaction tx2 = begin();
		CObj c1 = CObj.newCObj("c1");
		liveEndList.add(c1);
		CObj c2 = CObj.newCObj("c2");
		liveEndList.add(c2);
		CObj c3 = CObj.newCObj("c3");
		liveEndList.add(c3);
		assertEquals(list(c1, c2, c3), liveEndList);
		tx2.commit();
		assertEquals(list(c1, c2, c3), liveEndList);

		Transaction tx3 = begin();
		CObj removedC1 = liveEndList.remove(0);
		assertEquals(list(c2, c3), liveEndList);
		tx3.commit();
		assertEquals(list(c2, c3), liveEndList);
		assertEquals(c1, removedC1);
		assertTrue(removedC1.tValid());

		Transaction tx4 = begin();
		liveEndList.add(removedC1);
		assertEquals(list(c2, c3, c1), liveEndList);
		tx4.commit();

		Transaction tx5 = begin();
		// Adapt order number to force re-index on insert.
		List<KnowledgeAssociation> links = AbstractWrapper.resolveLinks(b1, ABOrdered.ORDER_QUERY_LIVE);
		assertEquals(3, links.size());
		ABOrdered.setOrder(links.get(0), 5);
		assertEquals(list(c2, c3, c1), liveEndList);
		ABOrdered.setOrder(links.get(1), 6);
		assertEquals(list(c2, c3, c1), liveEndList);
		ABOrdered.setOrder(links.get(2), 7);
		assertEquals(list(c2, c3, c1), liveEndList);

		CObj c4 = CObj.newCObj("c4");
		liveEndList.add(1, c4);
		assertTrue(
			"Ticket #24938: This tests finds the bug that setting order implicit changes list. For this case it is necessary that the new order of the first element is larger than the old order of the second element.",
			ABOrdered.getOrder(links.get(1)) > 6);
		assertEquals(list(c2, c4, c3, c1), liveEndList);
		tx5.commit();
		assertEquals(list(c2, c4, c3, c1), liveEndList);
	}

	public void testSetOrderedSet() throws Exception {
		Transaction tx = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");
		CObj c4 = CObj.newCObj("c4");
		CObj c5 = CObj.newCObj("c5");

		check(tx, b1, list(c5, c4, c3, c2, c1));
	}

	public void testSetOrderedBag() throws Exception {
		Transaction tx = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");
		CObj c4 = CObj.newCObj("c4");
		CObj c5 = CObj.newCObj("c5");

		check(tx, b1, list(c5, c4, c3, c2, c1, c1, c2, c4, c5));
	}

	public void testSetSingle() throws Exception {
		Transaction tx = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");

		check(tx, b1, list(c1));
	}

	public void testSetConstantBag() throws Exception {
		Transaction tx = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");

		check(tx, b1, list(c1, c1, c1));
	}

	public void testResetSingle() throws Exception {
		Transaction tx = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");

		check(tx, b1, list(c1));

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, EMPTY);
	}

	public void testRemoveFromConstantBag() throws Exception {
		Transaction tx = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");

		check(tx, b1, list(c1, c1, c1));
		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(0);

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, list(c1));

		unchanged.check();
	}

	public void testAddToSingle() throws Exception {
		Transaction tx = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");

		check(tx, b1, list(c1));
		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1);

		Transaction tx2 = kb().beginTransaction();
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");
		CObj c4 = CObj.newCObj("c4");
		check(tx2, b1, list(c2, c1, c3, c4));

		unchanged.check();
	}

	public void testSetEmpty() throws Exception {
		Transaction tx = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		check(tx, b1, EMPTY);
	}

	public void testRemoveSingle() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");

		check(tx1, b1, list(c1, c2, c3));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1, c3);

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, list(c1, c3));

		unchanged.check();
	}

	public void testRemoveSingleBag() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");

		check(tx1, b1, list(c1, c2, c2, c3));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1, c3).links(1);

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, list(c1, c2, c3));

		unchanged.check();
	}

	public void testRemoveAtBeginning() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");

		check(tx1, b1, list(c1, c2, c3));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c3);

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, list(c3));

		unchanged.check();
	}

	public void testRemoveAtBeginningBag() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");

		check(tx1, b1, list(c1, c1, c2, c3, c2));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c3).links(4);

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, list(c3, c2));

		unchanged.check();
	}

	public void testRemoveAtEnd() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");

		check(tx1, b1, list(c1, c2, c3));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1);

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, list(c1));

		unchanged.check();
	}

	public void testRemoveAtEndBag() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");

		check(tx1, b1, list(c1, c2, c3, c3, c2));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1).links(1);

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, list(c1, c2));

		unchanged.check();
	}

	public void testMoveToEnd() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");

		check(tx1, b1, list(c1, c2, c3));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1, c3);

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, list(c1, c3, c2));

		unchanged.check();
	}

	public void testMoveToBegin() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");

		check(tx1, b1, list(c1, c2, c3));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c2, c3);

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, list(c2, c1, c3));

		unchanged.check();
	}

	public void testInsertAtBeginning() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");

		check(tx1, b1, list(c1, c2, c3));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1, c2, c3);

		Transaction tx2 = kb().beginTransaction();
		CObj c4 = CObj.newCObj("c4");
		CObj c5 = CObj.newCObj("c5");
		check(tx2, b1, list(c4, c5, c1, c2, c3));

		unchanged.check();
	}

	public void testInsertInBetween() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");

		check(tx1, b1, list(c1, c2, c3));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1, c2, c3);

		Transaction tx2 = kb().beginTransaction();
		CObj c4 = CObj.newCObj("c4");
		CObj c5 = CObj.newCObj("c5");
		check(tx2, b1, list(c1, c2, c4, c5, c3));

		unchanged.check();
	}

	public void testInsertAtTheEnd() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");

		check(tx1, b1, list(c1, c2, c3));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1, c2, c3);

		Transaction tx2 = kb().beginTransaction();
		CObj c4 = CObj.newCObj("c4");
		CObj c5 = CObj.newCObj("c5");
		check(tx2, b1, list(c1, c2, c3, c4, c5));

		unchanged.check();
	}

	public void testInsertDuplicateInBag() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");

		check(tx1, b1, list(c1, c2, c3));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1, c3);

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, list(c1, c1, c3, c2, c3, c1));

		unchanged.check();
	}

	public void testInsertMany() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c0 = CObj.newCObj("c0");
		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");
		CObj c4 = CObj.newCObj("c4");

		check(tx1, b1, list(c1, c2, c3, c4));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1, c2, c3, c4);

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, list(c0, c0, c0, c0, c0, c1, c2, c3, c4));

		unchanged.check();
	}

	public void testDeleteManyAccepted() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c0 = CObj.newCObj("c0");
		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");
		CObj c4 = CObj.newCObj("c4");

		check(tx1, b1, list(c0, c0, c0, c1, c2, c3, c4));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1, c2, c3, c4);

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, list(c1, c2, c3, c4));

		unchanged.check();
	}

	public void testDeleteTooMuch() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c0 = CObj.newCObj("c0");
		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");
		CObj c4 = CObj.newCObj("c4");

		check(tx1, b1, list(c0, c0, c0, c0, c0, c1, c2, c3, c4));

		StabilizeCheck unchanged = StabilizeCheck.create(b1);

		Transaction tx2 = kb().beginTransaction();
		check(tx2, b1, list(c1, c2, c3, c4));

		unchanged.check();
	}

	public void testMinimalUpdate() throws Exception {
		Transaction tx = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");
	
		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");
		CObj c3 = CObj.newCObj("c3");
	
		check(tx, b1, list(c1, c2, c3));

		StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1, c2, c3);

		Transaction tx2 = kb().beginTransaction();
		CObj c4 = CObj.newCObj("c4");
		CObj c5 = CObj.newCObj("c5");
		check(tx2, b1, list(c1, c4, c5, c2, c3));

		unchanged.check();
	}

	public void testReindexBegin() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");

		check(tx1, b1, list(c1));

		int minStableInserts = 5;
		for (int n = 0; n < INCREMENTAL_INSERT_CNT; n++) {
			StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1);

			Transaction tx2 = kb().beginTransaction();
			CObj cNew = CObj.newCObj("cNew" + n);
			check(tx2, b1, list(cNew, c1));

			if (n < minStableInserts) {
				unchanged.check("At iteration " + n);
			}

			c1 = cNew;
		}
	}

	public void testReindexMiddle() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");

		check(tx1, b1, list(c1, c2));

		for (int n = 0; n < INCREMENTAL_INSERT_CNT; n++) {
			StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1, c2);

			Transaction tx2 = kb().beginTransaction();
			CObj cNew = CObj.newCObj("cNew" + n);
			check(tx2, b1, list(c1, cNew, c2));

			if (n < MIN_STABLE_INSERTS) {
				unchanged.check("At iteration " + n);
			}

			c1 = cNew;
		}
	}

	public void testReindexEnd() throws Exception {
		Transaction tx1 = kb().beginTransaction();
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");

		check(tx1, b1, list(c1, c2));

		for (int n = 0; n < INCREMENTAL_INSERT_CNT; n++) {
			StabilizeCheck unchanged = StabilizeCheck.create(b1).links(c1, c2);

			Transaction tx2 = kb().beginTransaction();
			CObj cNew = CObj.newCObj("cNew" + n);
			check(tx2, b1, list(c1, c2, cNew));

			if (n < MIN_STABLE_INSERTS) {
				unchanged.check("At iteration " + n);
			}

			c2 = cNew;
		}
	}

	public void testDetermineIndex() throws KnowledgeBaseException {
		OrderedLinkUtil.determineInsertOrder(Collections.<TLObject> emptyList(), 0, ABOrdered.ORDER_NAME);

		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");

		Transaction tx1 = kb().beginTransaction();
		
		check(tx1, b1, list(c1, c2));
		List<KnowledgeAssociation> links = AbstractWrapper.resolveLinks(b1, ABOrdered.ORDER_QUERY);
		StabilizeCheck unchanged =
			StabilizeCheck.create(b1).links(links.toArray(new KnowledgeAssociation[links.size()]));

		Number insertBefore = OrderedLinkUtil.determineInsertOrder(links, 0, ABOrdered.ORDER_NAME);
		unchanged.check();
		assertTrue(insertBefore.intValue() < ABOrdered.getOrder(links.get(0)));
		assertTrue(ABOrdered.getOrder(links.get(0)) < ABOrdered.getOrder(links.get(1)));

		Number insert = OrderedLinkUtil.determineInsertOrder(links, 1, ABOrdered.ORDER_NAME);
		unchanged.check();
		assertTrue(ABOrdered.getOrder(links.get(0)) < insert.intValue());
		assertTrue(insert.intValue() < ABOrdered.getOrder(links.get(1)));

		Number insertAfter = OrderedLinkUtil.determineInsertOrder(links, 2, ABOrdered.ORDER_NAME);
		unchanged.check();
		assertTrue(ABOrdered.getOrder(links.get(0)) < ABOrdered.getOrder(links.get(1)));
		assertTrue(ABOrdered.getOrder(links.get(1)) < insertAfter.intValue());
	}

	public void testDetermineIndexReindex() throws DataObjectException {
		BObj b1 = BObj.newBObj("b1");

		CObj c1 = CObj.newCObj("c1");
		CObj c2 = CObj.newCObj("c2");

		Transaction tx1 = kb().beginTransaction();

		check(tx1, b1, list(c1, c2));

		List<KnowledgeAssociation> links = AbstractWrapper.resolveLinks(b1, ABOrdered.ORDER_QUERY);

		ABOrdered.setOrder(links.get(0), 0);
		ABOrdered.setOrder(links.get(1), 1);
		Number insertBefore = OrderedLinkUtil.determineInsertOrder(links, 0, ABOrdered.ORDER_NAME);
		int order0 = ABOrdered.getOrder(links.get(0));
		int order1 = ABOrdered.getOrder(links.get(1));
		assertTrue(insertBefore.intValue() < order0);
		assertTrue(order0 < order1);

		ABOrdered.setOrder(links.get(0), 0);
		ABOrdered.setOrder(links.get(1), 1);
		Number insert = OrderedLinkUtil.determineInsertOrder(links, 1, ABOrdered.ORDER_NAME);
		order0 = ABOrdered.getOrder(links.get(0));
		order1 = ABOrdered.getOrder(links.get(1));
		assertTrue(order0 < insert.intValue());
		assertTrue(insert.intValue() < order1);

		ABOrdered.setOrder(links.get(0), 0);
		ABOrdered.setOrder(links.get(1), Integer.MAX_VALUE);
		Number insertAfter = OrderedLinkUtil.determineInsertOrder(links, 2, ABOrdered.ORDER_NAME);
		order0 = ABOrdered.getOrder(links.get(0));
		order1 = ABOrdered.getOrder(links.get(1));
		assertTrue(order0 < order1);
		assertTrue(order1 < insertAfter.intValue());
	}

	private void check(Transaction tx, BObj self, List<? extends TLObject> expectedValue) throws KnowledgeBaseException {
		OrderedLinkUtil.setOrderedValue(self, ABOrdered.ORDER_QUERY, expectedValue);

		assertEquals(expectedValue, AbstractWrapper.resolveOrderedValue(self, ABOrdered.ORDER_QUERY, CObj.class));
		tx.commit();
		assertEquals(expectedValue, AbstractWrapper.resolveOrderedValue(self, ABOrdered.ORDER_QUERY, CObj.class));
	}

	static class StabilizeCheck {
	
		private TLObject _src;

		private StabilizeCheck(TLObject src) {
			_src = src;
		}

		public static StabilizeCheck create(TLObject src) {
			return new StabilizeCheck(src);
		}
	
		private Map<KnowledgeAssociation, Revision> _lastUpdates = new HashMap<>();
	
		public StabilizeCheck links(TLObject... targets) {
			for (TLObject target : targets) {
				Iterator<KnowledgeAssociation> links =
					((KnowledgeObject) _src.tHandle()).getOutgoingAssociations(
						TestOrderedLinkUtil.ABOrdered.NAME,
						(KnowledgeObject) target.tHandle());
				assertTrue(links.hasNext());
				while (links.hasNext()) {
					KnowledgeAssociation link = links.next();
					_lastUpdates.put(link, HistoryUtils.getLastUpdate(link));
				}
			}
			return this;
		}

		public StabilizeCheck links(KnowledgeAssociation... links) {
			for (KnowledgeAssociation link : links) {
				_lastUpdates.put(link, HistoryUtils.getLastUpdate(link));
			}
			return this;
		}

		public StabilizeCheck links(int... indices) {
			List<KnowledgeAssociation> links =
				AbstractWrapper.resolveLinks(_src, TestOrderedLinkUtil.ABOrdered.ORDER_QUERY);
			for (int n : indices) {
				KnowledgeAssociation link = links.get(n);
				_lastUpdates.put(link, HistoryUtils.getLastUpdate(link));
			}
			return this;
		}
	
		public void check() {
			check(null);
		}

		public void check(String msgPart) {
			for (Entry<KnowledgeAssociation, Revision> entry : _lastUpdates.entrySet()) {
				KnowledgeObject dest;
				KnowledgeAssociation link = entry.getKey();
				try {
					dest = link.getDestinationObject();
				} catch (InvalidLinkException ex) {
					dest = null;
				}
				String destName = "'" + (dest == null ? "<deleted>" : dest) + "'";
				String custom = msgPart != null ? ": " + msgPart : "";
				assertTrue("Link to " + destName + " was deleted" + custom + ".", link.isAlive());
				assertEquals("Link to " + destName + " was updated." + custom + "", entry.getValue(),
					HistoryUtils.getLastUpdate(link));
			}
		}
	
	}

	@Override
	protected LocalTestSetup createSetup(Test self) {
		DBKnowledgeBaseTestSetup setup =
			new DBKnowledgeBaseTestSetup(self);
		setup.addAdditionalTypes(KnowledgeBaseTestScenarioImpl.newCopyProvider(ABOrdered.TYPE));
		return setup;
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suite(TestOrderedLinkUtil.class);
	}

}
