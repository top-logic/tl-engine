/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr.visit;

import static com.top_logic.basic.col.LongRangeSet.*;
import static com.top_logic.basic.col.LongRangeSet.union;
import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.TLID;
import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.col.LongRangeSet;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.search.BranchParam;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.RangeParam;
import com.top_logic.knowledge.search.RevisionParam;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test case for {@link HistoryQuery}.
 * 
 * @see TestQueryLanguage
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestHistoryQuery extends AbstractHistoryQueryTest {
	
	/**
	 * Tests searching of attribute values of historic reference in {@link InSet}.
	 */
	public void testHistoricAttributeValueInSetValue() throws DataObjectException {
		String histAttr = getReferenceAttr(MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		SetExpression inSet = map(allOf(D_NAME), attribute(reference(D_NAME, histAttr), D_NAME, D2_NAME));
		// D --> historic --> D1-attribute --> x <-- D2-attribute <-- historic <-- D
		SetExpression filter =
			filter(allOf(D_NAME),
				inSet(attribute(reference(D_NAME, histAttr), D_NAME, D1_NAME), inSet));
		HistoryQuery historyQuery = historyQuery(filter);

		Transaction tx1 = begin();
		KnowledgeObject d1 = newD("d1");
		KnowledgeObject d2 = newD("d2");
		commit(tx1);

		Transaction tx2 = begin();
		d2.setAttributeValue(D2_NAME, "match");
		commit(tx2);

		Transaction tx3 = begin();
		d1.setAttributeValue(D1_NAME, "match");
		commit(tx3);

		Transaction tx4 = begin();
		d1.setAttributeValue(histAttr, inRev(tx3, d1));
		d2.setAttributeValue(histAttr, inRev(tx3, d2));
		commit(tx4);

		Map<?, List<LongRange>> result1 = kb().search(historyQuery);
		assertEquals(1, result1.size());
		assertEquals(endSection(tx4), result1.get(getObjectID(d1)));

		Transaction tx5 = begin();
		d1.setAttributeValue(D1_NAME, "noMatch");
		d2.setAttributeValue(D2_NAME, "noMatch");
		commit(tx5);

		Map<?, List<LongRange>> result2 = kb().search(historyQuery);
		assertEquals(1, result2.size());
		assertEquals("Change in current object has no effect to historic search.", endSection(tx4),
			result2.get(getObjectID(d1)));

		Transaction tx6 = begin();
		d1.setAttributeValue(histAttr, inRev(tx4, d1));
		commit(tx6);

		Map<?, List<LongRange>> result3 = kb().search(historyQuery);
		assertEquals(d1 + " points to " + inRev(tx4, d1) + " and " + d2 + " to " + inRev(tx3, d2)
			+ " in which the attributes match (but in different revisions).", endSection(tx4),
			result3.get(getObjectID(d1)));

		Transaction tx7 = begin();
		d1.setAttributeValue(histAttr, inRev(tx2, d1));
		commit(tx7);

		Map<?, List<LongRange>> result4 = kb().search(historyQuery);
		assertEquals(1, result4.size());
		assertEquals(d1 + " points to a revision in which the attribute " + D1_NAME + " was not set.", range(tx4, tx6),
			result4.get(getObjectID(d1)));

		assertHistorySearch(kb(), historyQuery, historyArgs());
	}

	/**
	 * Tests searching of reference value of historic reference in {@link InSet} that contains
	 * reference values.
	 */
	public void testHistoricNavigationInSetValue() throws DataObjectException {
		String insetAttr = getReferenceAttr(MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		String testAttr = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		String histAttr = getReferenceAttr(MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		SetExpression inSet = map(allOf(E_NAME), reference(E_NAME, insetAttr));
		// D --> historic --> current --> x <-- historic <-- E
		SetExpression filter =
			filter(allOf(D_NAME), inSet(reference(reference(D_NAME, histAttr), D_NAME, testAttr, null), inSet));

		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");
		KnowledgeObject e3 = newE("e3");
		KnowledgeObject d1 = newD("d1");
		KnowledgeObject d2 = newD("d2");
		KnowledgeObject d3 = newD("d3");
		e1.setAttributeValue(insetAttr, d1);
		e2.setAttributeValue(insetAttr, d2);
		d3.setAttributeValue(testAttr, d2);
		d2.setAttributeValue(testAttr, d3);
		d3.setAttributeValue(histAttr, d3);
		commit(tx1);

		Map<?, List<LongRange>> result1 = kb().search(historyQuery(filter));
		assertFalse(d1 + " points to nothing", result1.containsKey(getObjectID(d1)));
		assertFalse(d2 + " points current to " + d3 + " but no historic", result1.containsKey(getObjectID(d2)));
		assertTrue(d3 + " points hist to itself points cur to " + d2 + " and " + e2 + " points to current " + d2,
			result1.containsKey(getObjectID(d3)));
		assertEquals(endSection(tx1), result1.get(getObjectID(d3)));

		Transaction tx2 = begin();
		d1.setAttributeValue(histAttr, inRev(tx1, d2));
		e2.setAttributeValue(insetAttr, inRev(tx1, d3));
		commit(tx2);

		Map<?, List<LongRange>> result2 = kb().search(historyQuery(filter));
		assertTrue(d1 + " points hist to " + d2 + " in " + tx1.getCommitRevision().getCommitNumber()
			+ " points current to " + d3 + " and " + e3 + " points to historic " + d3 + " in "
			+ tx1.getCommitRevision().getCommitNumber(), result2.containsKey(getObjectID(d1)));
		assertTrue(d3 + " in " + tx1.getCommitRevision().getCommitNumber() + " fullfills query.",
			result2.containsKey(getObjectID(d3)));
		assertEquals(range(tx1, tx1), result2.get(getObjectID(d3)));
		assertEquals(endSection(tx2), result2.get(getObjectID(d1)));
	}

	/**
	 * Tests that an attribute match with a current reference of a historic reference works.
	 */
	public void testMatchHistoricCurrentNavigation() throws DataObjectException {
		String historicReference = getReferenceAttr(MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		String currentReference = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);

		// Search D -> historic -> current -> attributeMatch
		Expression context = reference(reference(D_NAME, historicReference), D_NAME, currentReference, null);
		Expression filter = eqBinary(attribute(context, A_NAME, A2_NAME), literal("match"));
		HistoryQuery query = historyQuery(filter(allOf(D_NAME), filter));

		Transaction tx1 = begin();
		KnowledgeObject d1 = newD("d1");
		setA2(d1, "match");
		KnowledgeObject d2 = newD("d2");
		setA2(d2, "match");
		KnowledgeObject d3 = newD("d3");
		d3.setAttributeValue(historicReference, d2);
		d2.setAttributeValue(currentReference, d3);
		d1.setAttributeValue(historicReference, d2);
		commit(tx1);

		assertSame(0, kb().search(query).size());

		Transaction tx3 = begin();
		setA2(d3, "match");
		commit(tx3);

		assertEquals(d1 + " navigates to past. No change has consequence", null, kb().search(query)
			.get(getObjectID(d1)));
		assertSame(0, kb().search(query).size());

		Transaction tx4 = begin();
		d1.setAttributeValue(historicReference, inRev(tx3, d2));
		commit(tx4);

		assertEquals(endSection(tx4), kb().search(query).get(getObjectID(d1)));

	}

	/**
	 * Tests that an attribute match with a historic reference of a current reference of a current
	 * reference works.
	 */
	public void testMatchCurrentCurrentHistoricNavigation() throws DataObjectException {
		String historicReference = getReferenceAttr(MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		String currentReference = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);

		// Search D -> current -> current -> historic -> attributeMatch
		Expression curr1 = reference(D_NAME, currentReference);
		Expression curr2 = reference(curr1, D_NAME, currentReference, null);
		Expression context = reference(curr2, D_NAME, historicReference, null);
		Expression filter = eqBinary(attribute(context, A_NAME, A2_NAME), literal("match"));
		HistoryQuery query = historyQuery(filter(allOf(D_NAME), filter));

		Transaction tx1 = begin();
		KnowledgeObject d1 = newD("d1");
		setA2(d1, "match");
		KnowledgeObject d2 = newD("d2");
		setA2(d2, "nomatch");
		KnowledgeObject d3 = newD("d3");
		d3.setAttributeValue(historicReference, d1);
		commit(tx1);

		Transaction tx2 = begin();
		d2.setAttributeValue(currentReference, d3);
		commit(tx2);

		Transaction tx3 = begin();
		d3.setAttributeValue(historicReference, d2);
		d1.setAttributeValue(currentReference, d2);
		commit(tx3);

		assertSame("At no time there is current reference to a current reference to a correct historic reference.", 0,
			kb().search(query).size());
	}

	/**
	 * Test primitive attribute match of a historic reference.
	 */
	public void testHistoricAttributeMatch() throws DataObjectException {
		testReferenceAttributeMatch(true, true);
	}

	/**
	 * Test primitive attribute match of a historic reference on each branch.
	 */
	public void testHistoricAttributeMatchGlobal() throws DataObjectException {
		testReferenceAttributeMatch(false, true);
	}

	/**
	 * Test primitive attribute match of a current reference.
	 */
	public void testCurrentAttributeMatch() throws DataObjectException {
		testReferenceAttributeMatch(true, false);
	}

	/**
	 * Test primitive attribute match of a current reference on each branch.
	 */
	public void testCurrentAttributeMatchGlobal() throws DataObjectException {
		testReferenceAttributeMatch(false, false);
	}

	private void testReferenceAttributeMatch(boolean branchLocalSearch, boolean historicAttribute)
			throws DataObjectException {
		String matchValue = "match";
		HistoryType historyType = historicAttribute ? HistoryType.HISTORIC : HistoryType.CURRENT;
		String attr = getReferenceAttr(MONOMORPHIC, historyType, BRANCH_GLOBAL);
		SetExpression filter =
			filter(allOf(D_NAME),
				eqBinary(attribute(reference(D_NAME, attr), D_NAME, D1_NAME), literal(matchValue)));
		BranchParam branchParam = branchLocalSearch ? BranchParam.single : BranchParam.all;
		HistoryQuery query = newHistoryQuery(branchParam, filter);

		Transaction tx1 = begin();
		KnowledgeObject referer = newD("d1");
		KnowledgeObject d1 = newD("d2");
		d1.setAttributeValue(D1_NAME, matchValue);
		referer.setAttributeValue(attr, d1);
		commit(tx1);
		List<LongRange> expectedTX1Result = endSection(tx1);

		Map<?, List<LongRange>> result1 = kb().search(query);
		assertEquals(expectedTX1Result, result1.get(getObjectID(referer)));

		Transaction tx2 = begin();
		d1.setAttributeValue(D1_NAME, "unmatch");
		commit(tx2);
		List<LongRange> expectedTX2Result;
		if (historicAttribute) {
			expectedTX2Result = expectedTX1Result;
		} else {
			expectedTX2Result = LongRangeSet.substract(expectedTX1Result, endSection(tx2));
		}
		Map<?, List<LongRange>> result2 = kb().search(query);
		if (historicAttribute) {
			assertEquals("Change on objects that are historic references are not relevant.", expectedTX2Result,
				result2.get(getObjectID(referer)));
		} else {
			assertEquals("Change on objects that are current references are relevant.", expectedTX2Result,
				result2.get(getObjectID(referer)));
		}

		Transaction tx3 = begin();
		referer.setAttributeValue(A2_NAME, "irrelevant");
		commit(tx3);

		Map<?, List<LongRange>> result3 = kb().search(query);
		assertEquals("Change on irrelevant attributes must not change result.", expectedTX2Result,
			result3.get(getObjectID(referer)));

		if (historicAttribute) {
			Transaction tx4 = begin();
			referer.setAttributeValue(attr, d1);
			commit(tx4);
			List<LongRange> expectedTX4Result = LongRangeSet.substract(expectedTX2Result, endSection(tx4));

			Map<?, List<LongRange>> result4 = kb().search(query);
			assertEquals(expectedTX4Result, result4.get(getObjectID(referer)));

			Transaction tx5 = begin();
			referer.setAttributeValue(attr, inRev(tx1, d1));
			commit(tx5);

			Map<?, List<LongRange>> result5 = kb().search(query);
			assertEquals(union(expectedTX4Result, endSection(tx5)), result5.get(getObjectID(referer)));
		}

		assertHistorySearch(kb(), query, historyArgs());
	}

	/**
	 * Checks that two attributes are equal iff they are both <code>null</code> or both not
	 * <code>null</code> and equal.
	 * 
	 * This test especially the absurd SQL semantic of <code>equal</code>.
	 */
	public void testAttributeEqualityNullCheckQuery() throws DataObjectException {
		String attr1 = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		String attr2 = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);

		Transaction tx1 = begin();
		KnowledgeObject referer = newE("e1");
		KnowledgeObject d1 = newD("d1");
		commit(tx1);

		Transaction tx2 = begin();
		referer.setAttributeValue(attr1, d1);
		commit(tx2);

		Transaction tx3 = begin();
		referer.setAttributeValue(attr2, d1);
		commit(tx3);

		Transaction tx4 = begin();
		referer.setAttributeValue(attr1, null);
		commit(tx4);

		Expression eq = eqBinary(reference(E_NAME, attr1), reference(E_NAME, attr2));
		Map<?, List<LongRange>> eqResult = kb().search(historyQuery(filter(allOf(E_NAME), deepClone(eq))));
		List<LongRange> rangeEq = eqResult.get(getObjectID(referer));
		List<LongRange> range1 = range(tx1, tx1);
		List<LongRange> range2 = range(tx3, tx3);
		assertEquals(union(range1, range2), rangeEq);

		Map<?, List<LongRange>> notEqResult = kb().search(historyQuery(filter(allOf(E_NAME), deepClone(not(eq)))));
		List<LongRange> rangeNotEq = notEqResult.get(getObjectID(referer));
		List<LongRange> range3 = range(tx2, tx2);
		List<LongRange> range4 = endSection(tx4);
		assertEquals(union(range3, range4), rangeNotEq);

		List<LongRange> refererLifetime = endSection(tx1);
		assertEquals(refererLifetime, union(rangeEq, rangeNotEq));
		assertEquals(intersect(refererLifetime, invert(rangeEq)), rangeNotEq);
		assertEquals(intersect(refererLifetime, invert(rangeNotEq)), rangeEq);

	}

	public void testReferenceHistoricAttributeQuery() throws DataObjectException {
		String attr = getReferenceAttr(MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		Transaction tx1 = begin();
		KnowledgeObject referer = newE("e1");
		KnowledgeObject d1 = newD("d1");
		KnowledgeObject d2 = newD("other");
		commit(tx1);

		KnowledgeItem reference = HistoryUtils.getKnowledgeItem(tx1.getCommitRevision(), d1);
		KnowledgeItem otherRef = HistoryUtils.getKnowledgeItem(tx1.getCommitRevision(), d2);
		checkReference(referer, attr, reference, otherRef);
	}

	public void testReferenceHistoricCurrentQuery() throws DataObjectException {
		String attr = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		Transaction tx1 = begin();
		KnowledgeObject referer = newE("e1");
		KnowledgeObject reference = newD("d1");
		KnowledgeObject otherRef = newD("other");
		commit(tx1);

		checkReference(referer, attr, reference, otherRef);
	}

	public void testReferenceMixedCurrentQuery() throws DataObjectException {
		String attr = getReferenceAttr(MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		Transaction tx1 = begin();
		KnowledgeObject referer = newE("e1");
		KnowledgeObject reference = newD("d1");
		commit(tx1);

		KnowledgeItem otherRef = HistoryUtils.getKnowledgeItem(tx1.getCommitRevision(), reference);

		try {
			checkReference(referer, attr, reference, otherRef);
			// Check other way around
			checkReference(referer, attr, otherRef, reference);
			fail("Mixed references in HistoryQuery's currently not allowed. Remove this check if allowed.");
		} catch (IllegalArgumentException ex) {
			// Mixed references currently not allowed
		}
	}

	private void checkReference(KnowledgeObject referer, String attr, KnowledgeItem reference, KnowledgeItem otherRef)
			throws DataObjectException {
		assertNull("Tests expects that nothing is set to ensure that reference is not set in first transaction.",
			referer.getAttributeValue(attr));

		Transaction tx1 = begin();
		// just to produce new revision.
		referer.setAttributeValue(A1_NAME, "otherA1_1");
		commit(tx1);

		Transaction tx2 = begin();
		referer.setAttributeValue(attr, reference);
		commit(tx2);

		Transaction tx3 = begin();
		referer.setAttributeValue(attr, null);
		commit(tx3);

		Transaction tx4 = begin();
		referer.setAttributeValue(attr, otherRef);
		commit(tx4);

		Transaction tx5 = begin();
		referer.setAttributeValue(attr, reference);
		commit(tx5);

		Transaction tx6 = begin();
		// just to produce new revision.
		referer.setAttributeValue(A1_NAME, "otherA1_2");
		commit(tx6);

		ObjectKey refererKey = referer.tId();
		Transaction tx7 = begin();
		referer.delete();
		commit(tx7);

		Transaction tx8 = begin();
		KnowledgeItem revivedReferer = revive(kb(), refererKey);
		revivedReferer.setAttributeValue(A1_NAME, "revived");
		revivedReferer.setAttributeValue(attr, reference);
		commit(tx8);

		// produce no longer match in other node
		// should not be visible in node 1
		refetchNode2();
		Transaction tx9 = beginNode2();
		node2Item(revivedReferer).setAttributeValue(attr, node2Item(otherRef));
		KnowledgeObject eNode2 = kbNode2().createKnowledgeObject(E_NAME);
		setA1(eNode2, "a1");
		eNode2.setAttributeValue(attr, node2Item(reference));
		commitNode2(tx9);

		Transaction tx10 = beginNode2();
		node2Item(revivedReferer).setAttributeValue(attr, node2Item(reference));
		commitNode2(tx10);

		for (BranchParam branchParam : new BranchParam[] {BranchParam.single, BranchParam.all}) {
		{
			// check node 1
			SetExpression search = filter(allOf(E_NAME), eqBinary(reference(E_NAME, attr), literal(reference)));
				HistoryQuery historyQuery = newHistoryQuery(branchParam, search);
			Map<?, List<LongRange>> result = kb().search(historyQuery);

			assertEquals(1, result.size());

			List<LongRange> resultRange = result.get(getObjectID(revivedReferer));
			assertNotNull(resultRange);

			List<LongRange> range1 = range(tx2, tx2);
			List<LongRange> range2 = range(tx5, tx6);
			List<LongRange> range3 = endSection(tx8);
			List<LongRange> expected = union(range1, union(range2, range3));
			// just values after tx1 are under control
			expected = intersect(expected, endSection(tx1));

			assertEquals(expected, resultRange);
			
			assertHistorySearch(kb(), historyQuery, historyArgs());
		}

		{
			// check node 2
			SetExpression search = filter(allOf(E_NAME), eqBinary(reference(E_NAME, attr), literal(node2Item(reference))));
			HistoryQuery historyQuery =
					newHistoryQuery(branchParam, search);
			Map<?, List<LongRange>> result = kbNode2().search(historyQuery);

			assertEquals(2, result.size());

			List<LongRange> resultRange = result.get(ObjectBranchId.toObjectBranchId(node2Key(revivedReferer)));
			assertNotNull(resultRange);

			List<LongRange> range1 = range(tx2, tx2);
			List<LongRange> range2 = range(tx5, tx6);
			List<LongRange> range3 = range(tx8, tx8);
			List<LongRange> range4 = endSection(tx10);
			List<LongRange> expected = union(range1, union(range2, union(range3, range4)));
			// just values after tx1 are under control
			expected = intersect(expected, endSection(tx1));

			assertEquals(expected, resultRange);

			List<LongRange> eNode2Result = result.get(getObjectID(eNode2));
			assertEquals(endSection(tx9), eNode2Result);

			assertHistorySearch(kbNode2(), historyQuery, historyArgs());
		}
		}
	}

	private KnowledgeItem revive(KnowledgeBase kb, ObjectKey key) throws DataObjectException {
		Branch branch = HistoryUtils.getHistoryManager(kb).getBranch(key.getBranchContext());
		TLID id = key.getObjectName();
		String typeName = key.getObjectType().getName();
		return kb.createKnowledgeObject(branch, id, typeName);
	}
	
	private class Setup {
		
		Transaction _tx1;
		Transaction _tx2;
		Transaction _tx3;
		Transaction _tx4;
		Transaction _tx5;
		Transaction _tx6;
		Transaction _tx7;
		Transaction _tx8;
		KnowledgeItem _b1;
		KnowledgeItem _b2;

		Setup() {
			_tx1 = kb().beginTransaction();
			BObj bObj1 = BObj.newBObj("b1");
			BObj bObj2 = BObj.newBObj("b2");
			_tx1.commit();
			
			_tx2 = kb().beginTransaction();
			bObj1.setA2("a2value");
			bObj1.setA1("b1_new");
			_tx2.commit();

			_tx3 = kb().beginTransaction();
			bObj1.setF1("f1value");
			bObj2.setF1("f1value");
			_tx3.commit();

			_tx4 = kb().beginTransaction();
			bObj1.setF2("f2value");
			_tx4.commit();

			_tx5 = kb().beginTransaction();
			bObj1.setF2(null);
			bObj1.setA2(null);
			bObj1.setA1("b1");
			_tx5.commit();

			_tx6 = kb().beginTransaction();
			bObj1.setF2("f2new");
			_tx6.commit();

			_tx7 = kb().beginTransaction();
			bObj1.setF1("f1new");
			bObj2.setF1("f1new");
			_tx7.commit();
			
			_tx8 = kb().beginTransaction();
			bObj1.setF1("f1value");
			_tx8.commit();
			
			_b1 = bObj1.tHandle();
			_b2 = bObj2.tHandle();
		}
	}

	public void testAttributeNotNullQuery() {
		Setup setup = new Setup();

		Map<?, List<LongRange>> result =
			kb().search(historyQuery(filter(allOf(B_NAME), not(isNull(attribute(B_NAME, A2_NAME))))));

		List<LongRange> b1Range = result.get(getObjectID(setup._b1));
		assertNotNull(b1Range);
		List<LongRange> range1 = range(setup._tx2, setup._tx4);
		assertEquals(range1, b1Range);
	}

	public void testAttributeEqualityQuery() {
		testAttributeEqualityQuery(true);
	}

	public void testAttributeEqualityGlobalQuery() {
		testAttributeEqualityQuery(false);
	}

	private void testAttributeEqualityQuery(boolean branchLocalSearch) {
		Setup setup = new Setup();

		SetExpression search = filter(allOf(B_NAME), eqBinary(attribute(B_NAME, A2_NAME), literal("a2value")));
		BranchParam branchParam = branchLocalSearch ? BranchParam.single : BranchParam.all;
		HistoryQuery historyQuery = newHistoryQuery(branchParam, search);
		Map<?, List<LongRange>> result = kb().search(historyQuery);

		List<LongRange> b1Range = result.get(getObjectID(setup._b1));
		assertNotNull(b1Range);
		assertEquals(1, b1Range.size());
		List<LongRange> range1 = range(setup._tx2, setup._tx4);
		assertEquals(range1, b1Range);

		List<LongRange> b2Range2 = result.get(getObjectID(setup._b2));
		assertNull("b2 has never matched the query.", b2Range2);

		assertHistorySearch(kb(), historyQuery, historyArgs());
	}

	public void testAttributeInequalityQuery() {
		Setup setup = new Setup();

		HistoryQuery historyQuery =
			historyQuery(filter(allOf(B_NAME), not(eqBinary(attribute(B_NAME, A1_NAME), literal("b1_new")))));
		Map<?, List<LongRange>> result = kb().search(historyQuery);

		List<LongRange> b1Range = result.get(getObjectID(setup._b1));
		List<LongRange> range1 = range(setup._tx1, setup._tx1);
		List<LongRange> range2 = endSection(setup._tx5);
		assertEquals(union(range1, range2), b1Range);

		List<LongRange> b2Range2 = result.get(getObjectID(setup._b2));
		assertNotNull(b2Range2);
		assertEquals(1, b2Range2.size());
		assertEquals(range(HistoryUtils.getCreateRevision(setup._b2), Revision.CURRENT), b2Range2);

		assertHistorySearch(kb(), historyQuery, historyArgs());
	}

	public void testFlexAttributeNotNullQuery() {
		Setup setup = new Setup();

		HistoryQuery historyQuery =
			historyQuery(filter(allOf(B_NAME), not(isNull(flex(MOPrimitive.STRING, BObj.F2_NAME)))));
		Map<?, List<LongRange>> result = kb().search(historyQuery);

		List<LongRange> b1Range = result.get(getObjectID(setup._b1));
		assertNotNull(b1Range);
		List<LongRange> range1 = range(setup._tx4, setup._tx4);
		List<LongRange> range2 = endSection(setup._tx6);
		assertEquals(union(range1, range2), b1Range);

		assertHistorySearch(kb(), historyQuery, historyArgs());
	}

	public void testFlexAttributeEqualityQuery() {
		testFlexAttributeEqualityQuery(true);
	}

	public void testFlexAttributeEqualityGlobalQuery() {
		testFlexAttributeEqualityQuery(false);
	}

	private void testFlexAttributeEqualityQuery(boolean branchLocalSearch) {
		Setup setup = new Setup();

		SetExpression search = filter(allOf(B_NAME), eqBinary(flex(MOPrimitive.STRING, BObj.F1_NAME), literal("f1value")));
		BranchParam branchParam = branchLocalSearch ? BranchParam.single : BranchParam.all;
		HistoryQuery historyQuery = newHistoryQuery(branchParam, search);
		Map<?, List<LongRange>> result = kb().search(historyQuery);

		List<LongRange> b1Range = result.get(getObjectID(setup._b1));
		assertNotNull(b1Range);
		assertEquals(2, b1Range.size());
		List<LongRange> range1 = range(setup._tx3, setup._tx6);
		List<LongRange> range2 = endSection(setup._tx8);
		assertEquals(union(range1, range2), b1Range);

		List<LongRange> b2Range2 = result.get(getObjectID(setup._b2));
		assertNotNull(b2Range2);
		assertEquals(1, b2Range2.size());
		assertEquals(range(setup._tx3, setup._tx6), b2Range2);

		assertHistorySearch(kb(), historyQuery, historyArgs());
	}

	public void testFlexAttributeInequalityQuery() {
		Setup setup = new Setup();

		HistoryQuery historyQuery =
			historyQuery(filter(allOf(B_NAME),
				not(eqBinary(flex(MOPrimitive.STRING, BObj.F1_NAME), literal("f1value")))));
		Map<?, List<LongRange>> result = kb().search(historyQuery);

		List<LongRange> b1Range = result.get(getObjectID(setup._b1));
		List<LongRange> range1 = range(setup._tx1, setup._tx2);
		List<LongRange> range2 = range(setup._tx7, setup._tx7);
		assertEquals(union(range1, range2), b1Range);

		List<LongRange> b2Range2 = result.get(getObjectID(setup._b2));
		assertNotNull(b2Range2);
		assertEquals(2, b2Range2.size());
		List<LongRange> range3 = range(setup._tx1, setup._tx2);
		List<LongRange> range4 = endSection(setup._tx7);
		assertEquals(union(range3, range4), b2Range2);

		assertHistorySearch(kb(), historyQuery, historyArgs());
	}

	public void testSimpleSearch() throws DataObjectException {
		testSimpleSearch(true);
	}

	public void testSimpleGlobalSearch() throws DataObjectException {
		testSimpleSearch(false);
	}

	private void testSimpleSearch(boolean branchLocalSearch) throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeItem b1 = newB("b1");
		tx1.commit();

		Transaction tx2 = begin();
		KnowledgeObject b2 = newB("b2");
		b1.delete();
		tx2.commit();

		Transaction tx3 = begin();
		b1 = revive(kb(), b1.tId());
		b1.setAttributeValue(A1_NAME, "b1");
		b2.delete();
		tx3.commit();

		SetExpression search = allOf(B_NAME);
		BranchParam branchParam = branchLocalSearch ? BranchParam.single : BranchParam.all;
		Map<?, List<LongRange>> result = kb().search(newHistoryQuery(branchParam, search));

		List<LongRange> b1Range = result.get(getObjectID(b1));
		List<LongRange> range1 = range(tx1, tx1);
		List<LongRange> range2 = endSection(tx3);
		assertEquals(union(range1, range2), b1Range);

		List<LongRange> b2Range2 = result.get(getObjectID(b2));
		assertEquals(range(tx2, tx2), b2Range2);
	}

	private HistoryQuery newHistoryQuery(BranchParam branchParam, SetExpression search) {
		return ExpressionFactory.historyQuery(branchParam, RevisionParam.all, RangeParam.complete, NO_QUERY_PARAMETERS,
		search);
	}

	/**
	 * Searches all objects such that either one or another referenced object has a given attribute
	 * value.
	 */
	public void testOrReferenceAttributeMatch() throws DataObjectException {
		String matchValue = "match";
		SetExpression search = filter(allOf(E_NAME), or(
			eqBinary(attribute(reference(E_NAME, REFERENCE_MONO_CUR_GLOBAL_NAME), A_NAME, A2_NAME),
					literal(matchValue)),
			eqBinary(attribute(reference(E_NAME, REFERENCE_MONO_CUR_LOCAL_NAME), A_NAME, A2_NAME),
					literal(matchValue))
			));

		Transaction tx1 = begin();
		KnowledgeObject referer = newE("e1");
		KnowledgeObject ref1 = newD("reference1");
		referer.setAttributeValue(REFERENCE_MONO_CUR_GLOBAL_NAME, ref1);
		KnowledgeObject ref2 = newD("reference2");
		referer.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, ref2);
		commit(tx1);

		Transaction tx2 = begin();
		ref1.setAttributeValue(A2_NAME, matchValue);
		commit(tx2);

		Transaction tx3 = begin();
		ref2.setAttributeValue(A2_NAME, matchValue);
		commit(tx3);

		Map<?, List<LongRange>> result = kb().search(newHistoryQuery(BranchParam.all, search));
		assertEquals(result.get(getObjectID(referer)), endSection(tx2));
	}

	/**
	 * Suite of test case.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			return runOneTest(TestHistoryQuery.class, "");
		}
		return suite(TestHistoryQuery.class);
	}

}
