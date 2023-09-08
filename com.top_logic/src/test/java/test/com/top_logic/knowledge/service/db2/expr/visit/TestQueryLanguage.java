/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr.visit;

import static com.top_logic.basic.col.LongRangeSet.*;
import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static com.top_logic.knowledge.search.ExpressionFactory.union;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import junit.framework.Test;

import test.com.top_logic.basic.SingleTestFactory;
import test.com.top_logic.knowledge.service.TestKABasedCacheOrdered.BObjExtended;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioConstants;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.DObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;
import test.com.top_logic.knowledge.wrap.TestFlexWrapperCluster;

import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.col.LongRangeSet;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.IdentifierTypes;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOCollectionImpl;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.search.BranchParam;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.ExpressionFactory;
import com.top_logic.knowledge.search.HistoryQuery;
import com.top_logic.knowledge.search.HistoryQueryArguments;
import com.top_logic.knowledge.search.InSet;
import com.top_logic.knowledge.search.Operator;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.ParameterDeclaration;
import com.top_logic.knowledge.search.RangeParam;
import com.top_logic.knowledge.search.RevisionParam;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.BasicTypes;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.expr.sym.LiteralItemSymbol;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Test case with concrete scenarios for {@link DBKnowledgeBase#search(RevisionQuery)} and
 * {@link DBKnowledgeBase#search(HistoryQuery)}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestQueryLanguage extends AbstractDBKnowledgeBaseClusterTest {

	private static final String VALUE_AB1_1 = "ab1-1";
	private static final String VALUE_AB1_2 = "ab1-2";
	private static final String VALUE_BC1_1 = "bc1-1";
	private static final String VALUE_BC1_2 = "bc1-2";
	private static final String VALUE_A2_1 = "a2-1";
	private static final String VALUE_A2_2 = "a2-2";
	private static final String VALUE_B2_1 = "b2-1";
	private static final String VALUE_B2_2 = "b2-2";
	private static final String VALUE_D2_1 = "d2-1";
	private static final String VALUE_D2_2 = "d2-2";

	/**
	 * Tests lookup items in a InSet-expression.
	 */
	public void testLookupItemsInSet() throws DataObjectException {
		testLookupItemsInSet(REFERENCE_POLY_HIST_GLOBAL_NAME);
		testLookupItemsInSet(REFERENCE_POLY_HIST_LOCAL_NAME);
		testLookupItemsInSet(REFERENCE_POLY_CUR_GLOBAL_NAME);
		testLookupItemsInSet(REFERENCE_POLY_CUR_LOCAL_NAME);
		testLookupItemsInSet(REFERENCE_POLY_MIXED_GLOBAL_NAME);
		testLookupItemsInSet(REFERENCE_POLY_MIXED_LOCAL_NAME);
		testLookupItemsInSet(REFERENCE_MONO_HIST_GLOBAL_NAME);
		testLookupItemsInSet(REFERENCE_MONO_HIST_LOCAL_NAME);
		testLookupItemsInSet(REFERENCE_MONO_CUR_GLOBAL_NAME);
		testLookupItemsInSet(REFERENCE_MONO_CUR_LOCAL_NAME);
		testLookupItemsInSet(REFERENCE_MONO_MIXED_GLOBAL_NAME);
		testLookupItemsInSet(REFERENCE_MONO_MIXED_LOCAL_NAME);
	}

	private void testLookupItemsInSet(String attribute) throws DataObjectException, KnowledgeBaseException {
		KnowledgeObject match1 = newD("match");
		KnowledgeObject match2 = newD("match");
		KnowledgeObject noMatch = newD("noMatch");
		Transaction tx = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(attribute, match1);

		KnowledgeObject e2 = newE("e2");
		e2.setAttributeValue(attribute, noMatch);

		KnowledgeObject e3 = newE("e3");
		e3.setAttributeValue(attribute, match2);

		KnowledgeObject f1 = newF("t1");
		f1.setAttributeValue(attribute, match1);
		tx.commit();
		long r1 = tx.getCommitRevision().getCommitNumber();
		
		/* Searches items that reference either match1 or match2 */
		SetExpression literalSetFilter =
			filter(anyOf(E_NAME),
				inSet(reference(E_NAME, attribute), setLiteralOfEntries(match1, match2)));
		CompiledQuery<KnowledgeObject> queryLiteralSet = kb().compileQuery(queryUnresolved(literalSetFilter));
		assertEquals(set(e1, e3, f1), toSet(queryLiteralSet.search()));

		/* Searches items that reference an item in the given set parameter. */
		String param = "param";
		SetExpression setParamFilter =
			filter(anyOf(E_NAME), inSet(reference(E_NAME, attribute), setParam(param)));
		ParameterDeclaration paramDecl = paramDecl(MOCollectionImpl.createCollectionType(type(D_NAME)), param);
		CompiledQuery<KnowledgeObject> querySetParam =
			kb().compileQuery(queryUnresolved(params(paramDecl), setParamFilter, NO_ORDER));
		assertEquals(set(e1, e3, f1), toSet(querySetParam.search(revisionArgs().setArguments(list(match1, match2)))));

		Transaction chTX = begin();
		e1.setAttributeValue(attribute, noMatch);
		e2.setAttributeValue(attribute, match1);
		e3.setAttributeValue(attribute, null);
		f1.setAttributeValue(attribute, match2);
		chTX.commit();

		assertEquals(set(e2, f1), toSet(queryLiteralSet.search()));
		assertEquals(set(e2, f1), toSet(querySetParam.search(revisionArgs().setArguments(list(match1, match2)))));

		assertEquals(set(inRev(r1, e1), inRev(r1, e3), inRev(r1, f1)),
			toSet(queryLiteralSet.search(revisionArgs().setRequestedRevision(r1))));
		assertEquals(set(inRev(r1, e1), inRev(r1, e3), inRev(r1, f1)),
			toSet(querySetParam.search(revisionArgs().setArguments(list(match1, match2)).setRequestedRevision(r1))));

	}

	public void testAlternativeLookup() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject newD = newD("d_a1");
		KnowledgeObject newB = newB("b_a1");
		KnowledgeObject newC = newC("c_a1");
		assertTrue("Check that no subclasses are found need that types are subtypes.", newC.tTable()
			.isSubtypeOf(newB.tTable()));
		tx.commit();

		RevisionQuery<BObj> queryResolved = queryResolved(filter(anyOf(BD_NAME), hasType(B_NAME)), BObj.class);
		assertEquals(list(newB.getWrapper()), kb().search(queryResolved));
	}

	public void testAlternativeReferenceLookup() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(ALTERNATIVE_POLY_CUR_GLOBAL_NAME, newB("match"));

		KnowledgeObject e2 = newE("e2");
		e2.setAttributeValue(ALTERNATIVE_POLY_CUR_GLOBAL_NAME, newB("noMatchReferenceWrongAttribute"));

		KnowledgeObject e3 = newE("e3");
		e3.setAttributeValue(ALTERNATIVE_POLY_CUR_GLOBAL_NAME, newD("someD"));

		KnowledgeObject f1 = newF("t1");
		f1.setAttributeValue(ALTERNATIVE_POLY_CUR_GLOBAL_NAME, newC("match"));
		tx.commit();

		CompiledQuery<EObj> queryResolved = kb().compileQuery(queryResolved(
				filter(
					anyOf(E_NAME), 
					eval(
						reference(E_NAME, ALTERNATIVE_POLY_CUR_GLOBAL_NAME),
					and(
						instanceOf(B_NAME),
						eqBinary(attribute(A_NAME, A1_NAME), literal("match")))
				)
			), EObj.class));
		assertEquals(set(e1.getWrapper(), f1.getWrapper()), toSet(queryResolved.searchStream()));
	}

	public void testAlternativeReferenceEquality() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject equalityItem = newB("match");
		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(ALTERNATIVE_POLY_CUR_GLOBAL_NAME, equalityItem);

		KnowledgeObject e2 = newE("e2");
		e2.setAttributeValue(ALTERNATIVE_POLY_CUR_GLOBAL_NAME, newB("noMatchReferenceWrongAttribute"));

		KnowledgeObject f1 = newF("t1");
		f1.setAttributeValue(ALTERNATIVE_POLY_CUR_GLOBAL_NAME, equalityItem);
		tx.commit();

		CompiledQuery<Wrapper> queryResolved =
			kb().compileQuery(queryResolved(
				filter(
					anyOf(E_NAME), 
					eqBinary(
						reference(E_NAME, ALTERNATIVE_POLY_CUR_GLOBAL_NAME),
						literal(equalityItem)
					)
				)
				, Wrapper.class));
		assertEquals(set(e1.getWrapper(), f1.getWrapper()), toSet(queryResolved.searchStream()));
	}

	public void testAllOfAbstractType() {
		SetExpression as = allOf(A_NAME);
		try {
			kb().compileQuery(queryUnresolved(as));
			fail("Concrete item lookup of abstract type");
		} catch (RuntimeException ex) {
			if (ex.getLocalizedMessage().contains("Concrete item lookup of abstract type ")) {
				// expected
			} else {
				throw ex;
			}
		}
	}

	public void testAllOfAlternativeType() {
		SetExpression bOrD = allOf(BD_NAME);
		try {
			kb().compileQuery(queryUnresolved(bOrD));
			fail("Concrete item lookup of abstract type.");
		} catch (RuntimeException ex) {
			if (ex.getLocalizedMessage().contains("Concrete item lookup of abstract type ")) {
				// expected
			} else {
				throw ex;
			}
		}
	}

	public void testRedundantUnionSearch() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		KnowledgeObject b1 = newB("b1");
		KnowledgeObject c1 = newC("c1");
		KnowledgeObject d1 = newD("d1");
		tx.commit();

		Collection<?> ids = Arrays.asList(b1.getObjectName(), c1.getObjectName(), d1.getObjectName());
		SetExpression as = anyOf(A_NAME);
		SetExpression bs =
			filter(anyOf(B_NAME),
				inLiteralSet(attribute(BasicTypes.ITEM_TYPE_NAME, BasicTypes.IDENTIFIER_ATTRIBUTE_NAME), ids));

		// The search for bs is redundant, since B is a subtype of A.
		List<?> result = kb().search(queryUnresolved(union(as, bs)));

		assertEquals(set(b1, c1, d1), toSet(result));
	}

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

		List<KnowledgeObject> result1 = kb().search(queryUnresolved(filter));
		assertEquals(list(d1), result1);

		Transaction tx5 = begin();
		d1.setAttributeValue(D1_NAME, "noMatch");
		d2.setAttributeValue(D2_NAME, "noMatch");
		commit(tx5);

		List<KnowledgeObject> result2 = kb().search(queryUnresolved(filter));
		assertEquals("Change in current object has no effect to historic search.", list(d1), result2);

		Transaction tx6 = begin();
		d1.setAttributeValue(histAttr, inRev(tx4, d1));
		commit(tx6);

		List<KnowledgeObject> result3 = kb().search(queryUnresolved(filter));
		assertTrue(d1 + " points to " + inRev(tx4, d1) + " and " + d2 + " to " + inRev(tx3, d2)
			+ " in which the attributes match (but in different revisions).",
			result3.contains(d1));

		Transaction tx7 = begin();
		d1.setAttributeValue(histAttr, inRev(tx2, d1));
		commit(tx7);

		List<KnowledgeObject> result4 = kb().search(queryUnresolved(filter));
		assertFalse(d1 + " points to a revision in which the attribute " + D1_NAME + " was not set.",
			result4.contains(d1));
		assertEquals(list(), result4);

	}

	/**
	 * Tests searching of reference value of historic reference in {@link InSet} that contains
	 * reference values.
	 */
	public void testHistoricNavigationInSetValue() throws DataObjectException {
		String insetAttr = getReferenceAttr(MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		String testAttr = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);

		String histAttr = getReferenceAttr(MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		SetExpression inSet = map(allOf(E_NAME), reference(E_NAME, insetAttr));
		// D --> historic --> current --> x <-- mixed <-- E
		SetExpression filter =
			filter(allOf(D_NAME), inSet(reference(reference(D_NAME, histAttr), D_NAME, testAttr, null), inSet));
		SetExpression notFilter =
			filter(allOf(D_NAME),
				not(inSet(reference(reference(D_NAME, histAttr), D_NAME, testAttr, null), deepClone(inSet))));

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

		List<KnowledgeObject> result1 = kb().search(queryUnresolved(filter));
		assertFalse(d1 + " points to nothing", result1.contains(d1));
		assertFalse(d2 + " points current to " + d3 + " but no historic", result1.contains(d2));
		assertTrue(d3 + " points hist to itself points cur to " + d2 + " and " + e2 + " points to current " + d2,
			result1.contains(d3));
		assertEquals(list(d3), result1);
		assertEquals(set(d1, d2), toSet(kb().search(queryUnresolved(notFilter))));

		Transaction tx2 = begin();
		d1.setAttributeValue(histAttr, inRev(tx1, d2));
		e3.setAttributeValue(insetAttr, inRev(tx1, d3));
		commit(tx2);

		List<KnowledgeObject> result2 = kb().search(queryUnresolved(filter));
		assertTrue(d1 + " points hist to " + d2 + " in " + tx1.getCommitRevision().getCommitNumber()
			+ " points current to " + d3 + " and " + e3 + " points to historic " + d3 + " in "
			+ tx1.getCommitRevision().getCommitNumber(), result2.contains(d1));
		assertFalse(d3 + " points hist to " + d3 + " in " + tx1.getCommitRevision().getCommitNumber()
			+ " points current to " + d2 + " and " + e3 + " points to current " + d2, result2.contains(d3));
		assertEquals(list(d1), result2);
		assertEquals(set(d2, d3), toSet(kb().search(queryUnresolved(notFilter))));

	}

	/**
	 * Tests searching of reference value in {@link InSet} that contains reference values.
	 */
	public void testHistoricInSetValue() throws DataObjectException {
		String insetAttr = getReferenceAttr(MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);
		String testAttr = getReferenceAttr(MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		SetExpression inSet = map(allOf(E_NAME), reference(E_NAME, insetAttr));
		SetExpression filter = filter(allOf(D_NAME), inSet(reference(D_NAME, testAttr), inSet));
		SetExpression notFilter = filter(allOf(D_NAME), not(inSet(reference(D_NAME, testAttr), deepClone(inSet))));

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
		commit(tx1);

		List<KnowledgeObject> result1 = kb().search(queryUnresolved(filter));
		assertFalse(d1 + " points to nothing", result1.contains(d1));
		assertFalse(d2 + " points to " + d3 + " but no " + E_NAME + " points to " + d3, result1.contains(d2));
		assertTrue(d3 + " points to " + d2 + " and " + e2 + " points to " + d2, result1.contains(d3));
		assertEquals(list(d3), result1);
		assertEquals(set(d1, d2), toSet(kb().search(queryUnresolved(notFilter))));

		Transaction tx2 = begin();
		e3.setAttributeValue(insetAttr, inRev(tx1, d3));
		commit(tx2);

		List<KnowledgeObject> result2 = kb().search(queryUnresolved(filter));
		assertFalse(d2 + " points to current " + d3 + " but " + e3 + " points to historic " + d3, result2.contains(d2));
		assertEquals(list(d3), result2);
		assertEquals(set(d1, d2), toSet(kb().search(queryUnresolved(notFilter))));

	}

	/**
	 * Tests that an attribute match with a historic reference works.
	 */
	public void testMatchMixedNavigation() throws DataObjectException {
		String attr = getReferenceAttr(MONOMORPHIC, HistoryType.MIXED, BRANCH_GLOBAL);

		// Search E -> mixed attributeMatch
		Expression context = reference(E_NAME, attr);
		Expression filter = eqBinary(attribute(context, A_NAME, A2_NAME), literal("match"));
		RevisionQuery<KnowledgeObject> query = queryUnresolved(filter(allOf(E_NAME), filter));

		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject d1 = newD("d1");
		setA2(d1, "match");
		commit(tx1);

		assertFalse(e1 + " does not point to anything", kb().search(query).contains(e1));
		assertEquals(list(), kb().search(query));

		Transaction tx2 = begin();
		e1.setAttributeValue(attr, inRev(tx1, d1));
		commit(tx2);

		assertTrue(e1 + " points to correct object.", kb().search(query).contains(e1));
		assertEquals(list(e1), kb().search(query));

		Transaction tx3 = begin();
		setA2(d1, "noMatch");
		commit(tx3);

		assertTrue(e1 + ": Changes on current version of referenced object does not change historic item.",
			kb().search(query).contains(e1));
		assertEquals(list(e1), kb().search(query));

		Transaction tx4 = begin();
		e1.setAttributeValue(attr, d1);
		commit(tx4);

		assertFalse("Reference " + d1 + " does not match attribute.", kb().search(query).contains(e1));
		assertEquals(list(), kb().search(query));

		Transaction tx5 = begin();
		setA2(d1, "match");
		commit(tx5);

		assertTrue("Reference " + d1 + " match attribute.", kb().search(query).contains(e1));
		assertEquals(list(e1), kb().search(query));

	}

	/**
	 * Tests that an attribute match with a historic reference works.
	 */
	public void testMatchHistoricNavigation() throws DataObjectException {
		String attr = getReferenceAttr(MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		String flexAttr = "flex";
		String matchValue = "match";

		// Search E -> historic attributeMatch
		Expression context = reference(E_NAME, attr);
		Expression rowFilter = eqBinary(attribute(context, A_NAME, A2_NAME), literal(matchValue));
		Expression flexFilter = eqBinary(flex(context, MOPrimitive.STRING, flexAttr), literal(matchValue));
		RevisionQuery<KnowledgeObject> rowQuery = queryUnresolved(filter(allOf(E_NAME), rowFilter));
		RevisionQuery<KnowledgeObject> flexQuery = queryUnresolved(filter(allOf(E_NAME), flexFilter));

		Transaction tx1 = begin();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject d1 = newD("d1");
		setA2(d1, matchValue);
		d1.setAttributeValue(flexAttr, matchValue);
		commit(tx1);

		assertFalse(e1 + " does not point to anything", kb().search(rowQuery).contains(e1));
		assertEquals(list(), kb().search(rowQuery));
		assertEquals(list(), kb().search(flexQuery));

		Transaction tx2 = begin();
		e1.setAttributeValue(attr, inRev(tx1, d1));
		commit(tx2);

		assertTrue(e1 + " points to correct object.", kb().search(rowQuery).contains(e1));
		assertEquals(list(e1), kb().search(rowQuery));
		assertEquals(list(e1), kb().search(flexQuery));

		Transaction tx3 = begin();
		setA2(d1, "noMatch");
		d1.setAttributeValue(flexAttr, "noMatch");
		commit(tx3);

		assertTrue(e1 + ": Changes on current version of referenced object does not change historic reference.",
			kb().search(rowQuery).contains(e1));
		assertEquals(list(e1), kb().search(rowQuery));
		assertEquals(list(e1), kb().search(flexQuery));

		Transaction tx4 = begin();
		setA2(e1, "noMatch");
		commit(tx4);

		assertTrue(e1 + ": Changes on referee does not change historic reference.", kb().search(rowQuery).contains(e1));
		assertEquals(list(e1), kb().search(rowQuery));
		assertEquals(list(e1), kb().search(flexQuery));

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
		RevisionQuery<KnowledgeObject> query = queryUnresolved(filter(allOf(D_NAME), filter));

		Transaction tx1 = begin();
		KnowledgeObject d1 = newD("d1");
		setA2(d1, "match");
		KnowledgeObject d2 = newD("d2");
		setA2(d2, "match");
		KnowledgeObject d3 = newD("d3");
		KnowledgeObject d4 = newD("d4");
		KnowledgeObject d5 = newD("d5");
		d3.setAttributeValue(historicReference, d2);
		d4.setAttributeValue(currentReference, d2);
		d5.setAttributeValue(currentReference, d4);
		d2.setAttributeValue(currentReference, d3);
		d1.setAttributeValue(historicReference, d2);
		commit(tx1);

		assertFalse(d5 + " points current->current->attributeMatch", kb().search(query).contains(d5));
		assertFalse(d4 + " points current->attributeMatch", kb().search(query).contains(d4));
		assertFalse(d3 + " points historic->attributeMatch", kb().search(query).contains(d3));
		assertFalse(d2 + " points current->historic->attributeMatch", kb().search(query).contains(d2));
		assertFalse(d1 + " points historic->current->!attributeMatch", kb().search(query).contains(d1));
		assertEquals(list(), kb().search(query));

		Transaction tx2 = begin();
		newD("d6");
		commit(tx2);

		assertFalse(d5 + ": Match must not change after noOp commit", kb().search(query).contains(d5));
		assertFalse(d4 + ": Match must not change after noOp commit", kb().search(query).contains(d4));
		assertFalse(d3 + ": Match must not change after noOp commit", kb().search(query).contains(d3));
		assertFalse(d2 + ": Match must not change after noOp commit", kb().search(query).contains(d2));
		assertFalse(d1 + ": Match must not change after noOp commit", kb().search(query).contains(d1));
		assertEquals(list(), kb().search(query));

		Transaction tx3 = begin();
		setA2(d3, "match");
		commit(tx3);
		
		assertFalse(d1 + " navigates to past. No change has consequence", kb().search(query).contains(d1));
		assertEquals(list(), kb().search(query));

		Transaction tx4 = begin();
		d1.setAttributeValue(historicReference, inRev(tx3, d2));
		commit(tx4);

		assertTrue(d1 + " historic->points current->historic->attributeMatch", kb().search(query).contains(d1));
		assertEquals(list(d1), kb().search(query));


	}

	/**
	 * Tests setting an item as parameter.
	 * 
	 * <p>
	 * In a revision query executed in an old revision a current object is treated as unversioned.
	 * See also {@link LiteralItemSymbol#createRevisionExpr()} and #9603
	 * </p>
	 * 
	 */
	public void testHistoricReferenceMatch() throws DataObjectException {
		RevisionQuery<KnowledgeObject> query = queryUnresolved(list(paramDecl(BasicTypes.KNOWLEDGE_OBJECT_TYPE_NAME, "ref")), filter(
		allOf(E_NAME),
		eqBinary(
			reference(E_NAME, REFERENCE_MONO_CUR_LOCAL_NAME),
			param("ref"))), null);

		Transaction tx1 = kb().beginTransaction();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");

		KnowledgeObject d1 = newD("d1");
		tx1.commit();

		Transaction tx2 = kb().beginTransaction();
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, d1);
		tx2.commit();

		Transaction tx3 = kb().beginTransaction();
		e2.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, d1);
		tx3.commit();
		Long r3 = tx3.getCommitRevision().getCommitNumber();

		Transaction tx4 = kb().beginTransaction();
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, null);
		tx4.commit();

		assertEquals(set(e2),
			toSet(kb().search(query, revisionArgs().setArguments(d1))));
		assertEquals(set(inRev(r3, e2), inRev(r3, e1)),
			toSet(kb().search(query, revisionArgs().setArguments(inRev(r3, d1)).setRequestedRevision(r3))));
		assertEquals(
			"Titcket #9603: Special behaviour in query language. A current parameter is treated as unversioned, as this is the natural semantic.",
			set(inRev(r3, e2), inRev(r3, e1)),
			toSet(kb().search(query, revisionArgs().setArguments(d1).setRequestedRevision(r3))));
	}

	/**
	 * Test evaluating an attribute of a reference.
	 */
	public void testReferenceEval() throws DataObjectException {
		RevisionQuery<KnowledgeObject> query = queryUnresolved(filter(
		allOf(E_NAME),
		eqBinary(
			eval(reference(E_NAME, REFERENCE_MONO_CUR_LOCAL_NAME), attribute(D_NAME, D1_NAME)),
			literal("d1a"))));

		Transaction tx = begin();
		KnowledgeObject d1 = newD("d1");
		d1.setAttributeValue(D1_NAME, "d1a");
		KnowledgeObject d2 = newD("d2");
		d2.setAttributeValue(D1_NAME, "d1a");
		KnowledgeObject d3 = newD("d3");
		d3.setAttributeValue(D1_NAME, "d1b");
		KnowledgeObject d4 = newD("d4");

		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, d1);
		KnowledgeObject e2 = newE("e2");
		e2.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, d2);
		KnowledgeObject e3 = newE("e3");
		e3.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, d3);
		KnowledgeObject e4 = newE("e4");
		e4.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, d4);
		tx.commit();

		Set<KnowledgeObject> result = toSet(kb().search(query));
		assertEquals(set(e1, e2), result);
	}

	/**
	 * Test evaluating an attribute of a reference within an {@link InSet}.
	 */
	public void testReferenceEvalInsetExpression() throws DataObjectException {
		RevisionQuery<KnowledgeObject> query = queryUnresolved(filter(
		allOf(E_NAME),
		inSet(
			eval(reference(E_NAME, REFERENCE_MONO_CUR_LOCAL_NAME), attribute(D_NAME, D1_NAME)),
			setLiteral(Arrays.asList("d1a", "d1b")))));

		Transaction tx = begin();
		KnowledgeObject d1 = newD("d1");
		d1.setAttributeValue(D1_NAME, "d1a");
		KnowledgeObject d2 = newD("d2");
		d2.setAttributeValue(D1_NAME, "d1b");
		KnowledgeObject d3 = newD("d3");
		d3.setAttributeValue(D1_NAME, "d1c");
		KnowledgeObject d4 = newD("d4");

		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, d1);
		KnowledgeObject e2 = newE("e2");
		e2.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, d2);
		KnowledgeObject e3 = newE("e3");
		e3.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, d3);
		KnowledgeObject e4 = newE("e4");
		e4.setAttributeValue(REFERENCE_MONO_CUR_LOCAL_NAME, d4);
		tx.commit();

		Set<KnowledgeObject> result = toSet(kb().search(query));
		assertEquals(set(e1, e2), result);
	}

	/**
	 * Test dereferencing a branch-crossing polymorphic reference in an {@link InSet} expression.
	 */
	public void testBranchCrossingPolymorphicReferenceEvalInsetExpression() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		RevisionQuery<KnowledgeObject> query = queryUnresolved(filter(
		allOf(E_NAME),
		inSet(
			eval(reference(E_NAME, REFERENCE_POLY_CUR_GLOBAL_NAME), attribute(D_NAME, D1_NAME)),
			setLiteral(Arrays.asList("d1a", "d1b")))));

		Branch branch1 =
			kb().createBranch(kb().getTrunk(), kb().getRevision(kb().getLastRevision()),
				Collections.singleton(kb().lookupType(D_NAME)));
		Branch branch2 =
			kb().createBranch(kb().getTrunk(), kb().getRevision(kb().getLastRevision()),
				Collections.singleton(kb().lookupType(D_NAME)));

		Transaction tx = begin();
		KnowledgeObject d1 = newD("d1");
		d1.setAttributeValue(D1_NAME, "d1a");
		KnowledgeObject d2 = newD("d2");
		d2.setAttributeValue(D1_NAME, "d1b");
		KnowledgeObject d3 = newD("d3");
		d3.setAttributeValue(D1_NAME, "d1c");
		KnowledgeObject d4 = newD("d4");

		kb().setContextBranch(branch1);
		KnowledgeObject d11 = newD("d1-1");
		d11.setAttributeValue(D1_NAME, "d1a");
		KnowledgeObject d21 = newD("d2-1");
		d21.setAttributeValue(D1_NAME, "d1b");
		KnowledgeObject d31 = newD("d3-1");
		d31.setAttributeValue(D1_NAME, "d1c");
		KnowledgeObject d41 = newD("d4-1");

		kb().setContextBranch(branch2);
		KnowledgeObject d12 = newD("d1-2");
		d12.setAttributeValue(D1_NAME, "d1a");
		KnowledgeObject d22 = newD("d2-2");
		d22.setAttributeValue(D1_NAME, "d1b");
		KnowledgeObject d32 = newD("d3-2");
		d32.setAttributeValue(D1_NAME, "d1c");
		KnowledgeObject d42 = newD("d4-2");
		kb().setContextBranch(kb().getTrunk());

		KnowledgeObject e1 = newE("e1");
		e1.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, d1);
		KnowledgeObject e2 = newE("e2");
		e2.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, d2);
		KnowledgeObject e3 = newE("e3");
		e3.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, d3);
		KnowledgeObject e4 = newE("e4");
		e4.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, d4);

		KnowledgeObject e11 = newE("e1-1");
		e11.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, d11);
		KnowledgeObject e21 = newE("e2-1");
		e21.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, d21);
		KnowledgeObject e31 = newE("e3-1");
		e31.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, d31);
		KnowledgeObject e41 = newE("e4-1");
		e41.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, d41);

		KnowledgeObject e12 = newE("e1-2");
		e12.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, d12);
		KnowledgeObject e22 = newE("e2-2");
		e22.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, d22);
		KnowledgeObject e32 = newE("e3-2");
		e32.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, d32);
		KnowledgeObject e42 = newE("e4-2");
		e42.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, d42);
		tx.commit();

		try {
			Set<KnowledgeObject> result = toSet(kb().search(query));
			assertEquals(set(e1, e2, e11, e21, e12, e22), result);
		} catch (Exception e) {
			/* Exptected due to the known bug in ticket #9402: Polymorphic navigation. */
			return;
		}
		fail("Test should fail due to the known bug in ticket #9402: Polymorphic navigation.");
	}

	/**
	 * Test accessing a reference aspect in an {@link InSet}-Expression.
	 */
	public void testComplexInsetExpression() throws DataObjectException {
		String attr = UNTYPED_POLY_CUR_GLOBAL_NAME;
		RevisionQuery<KnowledgeObject> query = queryUnresolved(filter(
		allOf(E_NAME),
		inSet(
			unaryOperation(Operator.TYPE_NAME, reference(E_NAME, attr)),
			setLiteral(Arrays.asList(E_NAME, G_NAME)))));

		Transaction tx1 = kb().beginTransaction();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");
		KnowledgeObject e3 = newE("e3");
		KnowledgeObject e4 = newE("e4");
		KnowledgeObject e5 = newE("e5");
		KnowledgeObject e6 = newE("e6");
		assertNotNull(e6);

		KnowledgeObject d1 = newD("d1");
		KnowledgeObject g1 = newG("g1");

		e1.setAttributeValue(attr, e1);
		e2.setAttributeValue(attr, e5);
		e3.setAttributeValue(attr, g1);

		e4.setAttributeValue(attr, null);
		e5.setAttributeValue(attr, d1);
		tx1.commit();

		Set<KnowledgeObject> result = toSet(kb().search(query));

		assertEquals(set(e1, e2, e3), result);
	}

	/**
	 * Tests {@link ExpressionFactory#identifier(Expression)}.
	 */
	public void testIdentifierExpression() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Transaction tx = begin();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");
		KnowledgeObject reference = newD("reference");
		e1.setAttributeValue(REFERENCE_MONO_CUR_GLOBAL_NAME, reference);
		tx.commit();

		Branch branch1 = kb().createBranch(trunk(), tx.getCommitRevision(), null);

		Transaction tx2 = begin();
		KnowledgeItem referenceOnBranch = HistoryUtils.getKnowledgeItem(branch1, reference);
		e2.setAttributeValue(REFERENCE_MONO_CUR_GLOBAL_NAME, referenceOnBranch);
		tx2.commit();

		RevisionQuery<KnowledgeObject> branchLocalQuery = identifierExpressionQuery(reference);

		assertEquals(set(e1, e2), toSet(kb().search(branchLocalQuery)));
		Branch formerBranch = kb().setContextBranch(branch1);
		try {
			KnowledgeItem e1OnBranch = HistoryUtils.getKnowledgeItem(branch1, e1);
			assertEquals("Reference in e2 is set after branching", set(e1OnBranch),
				toSet(kb().search(branchLocalQuery)));

			RevisionQuery<KnowledgeObject> branchGlobalQuery = identifierExpressionQuery(reference);
			branchGlobalQuery.setBranchParam(BranchParam.all);
			assertEquals("Reference in e2 is set after branching", set(e1, e2, e1OnBranch),
				toSet(kb().search(branchGlobalQuery)));
		} finally {
			kb().setContextBranch(formerBranch);
		}

	}

	private RevisionQuery<KnowledgeObject> identifierExpressionQuery(KnowledgeObject reference) {
		Expression identifierLiteral = literal(reference.tId().getObjectName());
		Expression identifierExpr = identifier(reference(E_NAME, REFERENCE_MONO_CUR_GLOBAL_NAME));
		RevisionQuery<KnowledgeObject> branchLocalQuery =
			queryUnresolved(filter(allOf(E_NAME), eqBinary(identifierExpr, identifierLiteral)));
		return branchLocalQuery;
	}
	
	public void testColumnEquality() throws DataObjectException {
		Expression attributeEquality = eqBinary(attribute(B_NAME, B1_NAME), attribute(B_NAME, B2_NAME));
		SetExpression searchEqual = filter(allOf(B_NAME), attributeEquality);
		SetExpression searchNotEqual = filter(allOf(B_NAME), not(attributeEquality));
		RevisionQuery<KnowledgeObject> queryEqual = queryUnresolved(searchEqual);
		RevisionQuery<KnowledgeObject> queryNotEqual = queryUnresolved(searchNotEqual);

		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(B1_NAME, "not null");
		b1.setAttributeValue(B2_NAME, "not null");
		KnowledgeObject b2 = newB("b2");
		b2.setAttributeValue(B1_NAME, null);
		b2.setAttributeValue(B2_NAME, null);
		KnowledgeObject b3 = newB("b3");
		b3.setAttributeValue(B1_NAME, "not null");
		b3.setAttributeValue(B2_NAME, null);
		KnowledgeObject b4 = newB("b4");
		b4.setAttributeValue(B1_NAME, null);
		b4.setAttributeValue(B2_NAME, "not null");
		tx.commit();

		assertEquals("Ticket #4657: ", set(b1, b2), toSet(kb().search(queryEqual)));
		assertEquals("Ticket #4657: ", set(b3, b4), toSet(kb().search(queryNotEqual)));

	}

	/** 
	 * Tests that searching elements which have in concrete attribute a different value from a given one. 
	 */
	public void testSearchExcludedAttribute() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("b1");
		b1.setAttributeValue(B1_NAME, "b1_Attr");
		KnowledgeObject b2 = newB("b2");
		b2.setAttributeValue(B1_NAME, "not match");
		KnowledgeObject b3 = newB("b3");
		b3.setAttributeValue(B1_NAME, null);
		tx.commit();

		SetExpression filter = filter(allOf(B_NAME), not(eqBinary(attribute(B_NAME, B1_NAME), literal("b1_Attr"))));
		Set<KnowledgeObject> result = toSet(kb().search(queryUnresolved(filter)));

		assertFalse("b1 has excluded attribute set.", result.contains(b1));
		assertTrue("b2 has different attribute set.", result.contains(b2));
		assertTrue("Ticket #4657: b3 has attribute not set, especially not excluded attribute.", result.contains(b3));

	}

	public void testOrderSimpleAttribute() throws DataObjectException {
		testOrderAttribute(A1_NAME);
	}

	public void testOrderBinaryAttribute() throws DataObjectException {
		testOrderAttribute(B3_NAME);
	}

	private void testOrderAttribute(String attributeName) throws DataObjectException {
		List<String> names = createNames(20);
		long seed = 15843982487L;
		List<String> nameCopy = shuffle(names, seed);
		final Transaction tx = begin();
		for (int i = 0; i < nameCopy.size(); i++) {
			// randomise creation of elements
			KnowledgeObject newB = newB("some a1");
			newB.setAttributeValue(attributeName, nameCopy.get(i));
		}
		commit(tx);

		RevisionQuery<KnowledgeObject> query = queryUnresolved(allOf(B_NAME), order(attribute(B_NAME, attributeName)));
		List<KnowledgeObject> result = kb().search(query);
		assertEquals(names, mapToAttribute(result, attributeName));
		RevisionQuery<KnowledgeObject> queryDescending =
			queryUnresolved(allOf(B_NAME), orderDesc(attribute(B_NAME, attributeName)));

		List<KnowledgeObject> resultDescending = kb().search(queryDescending);
		List<String> reverse = new ArrayList<>(names);
		Collections.reverse(reverse);
		assertEquals(reverse, mapToAttribute(resultDescending, attributeName));
	}

	public void testRevision() throws DataObjectException {
		final String referenceName = KnowledgeBaseTestScenarioConstants.REFERENCE_POLY_HIST_GLOBAL_NAME;

		Transaction createTx = begin();
		KnowledgeItem e1 = newE("e1");
		KnowledgeItem e2 = newE("e2");
		commit(createTx);
		long r1 = createTx.getCommitRevision().getCommitNumber();
		Transaction assignTx = begin();
		KnowledgeItem reference = inRev(r1, e2);
		e1.setAttributeValue(referenceName, reference);
		commit(assignTx);
		
		{
			SetExpression search =
				filter(allOf(E_NAME), eqBinary(revision(reference(E_NAME, referenceName, null)), literal(r1)));
			List<KnowledgeObject> result = kb().search(queryUnresolved(search));
			assertEquals(list(e1), result);
		}
		{
			SetExpression search =
				filter(allOf(E_NAME),
					eqBinary(revision(reference(E_NAME, referenceName, null)), revision(literal(reference))));
			List<KnowledgeObject> result = kb().search(queryUnresolved(search));
			assertEquals(list(e1), result);
		}
		{
			SetExpression search =
				filter(allOf(E_NAME),
					eqBinary(revision(reference(E_NAME, referenceName, null)), revision(param("item"))));
			List<ParameterDeclaration> queryParameters = params(paramDecl(BasicTypes.ITEM_TYPE_NAME, "item"));
			RevisionQuery<KnowledgeObject> query = queryUnresolved(queryParameters, search, NO_ORDER);
			List<KnowledgeObject> result = kb().search(query, revisionArgs().setArguments(reference));
			assertEquals(list(e1), result);
		}
	}

	public void testBranch() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Transaction createTx = begin();
		KnowledgeItem b1 = newB("b1");
		KnowledgeItem b2 = newB("b2");
		commit(createTx);
		Branch branch = kb().createBranch(trunk(), lastRevision(), null);
		long branchId = branch.getBranchId();
		long trunkId = trunk().getBranchId();

		String paramName = "_branch";
		ParameterDeclaration paramDecl = paramDecl(IdentifierTypes.BRANCH_REFERENCE_MO_TYPE, paramName);
		List<ParameterDeclaration> queryParameter = list(paramDecl);
		RevisionQuery<KnowledgeObject> queryBranch =
			queryUnresolved(BranchParam.all, RangeParam.complete, queryParameter, filter(allOf(B_NAME), eqBinary(branch(), param(paramName))), NO_ORDER);
		
		RevisionQueryArguments revisionArgs = revisionArgs();
		revisionArgs.setArguments(Long.valueOf(branchId));
		List<KnowledgeObject> branchResult = kb().search(queryBranch, revisionArgs);
		Set<KnowledgeItem> branchExpected = set(onBranch(branch, b1), onBranch(branch, b2));
		assertEquals(branchExpected, toSet(branchResult));

		RevisionQuery<KnowledgeObject> queryTrunk =
			queryUnresolved(BranchParam.all, RangeParam.complete, NO_QUERY_PARAMETERS, filter(allOf(B_NAME), eqBinary(branch(), literal(trunkId))), NO_ORDER);
		List<KnowledgeObject> trunkResult = kb().search(queryTrunk);
		assertEquals(set(b1, b2), toSet(trunkResult));

	}

	public void testLimitationRange() throws DataObjectException {
		testLimitation(RangeParam.range);
	}

	public void testLimitationFirst() throws DataObjectException {
		testLimitation(RangeParam.first);
	}

	public void testNoLimitation() throws DataObjectException {
		testLimitation(RangeParam.complete);
	}

	public void testLimitationHead() throws DataObjectException {
		testLimitation(RangeParam.head);
	}

	private void testLimitation(RangeParam rangeParam) throws DataObjectException {
		Transaction createTx = begin();
		KnowledgeObject b1 = newB("b1");
		KnowledgeObject b2 = newB("b2");
		KnowledgeObject b3 = newB("b3");
		KnowledgeObject b4 = newB("b4");
		commit(createTx);

		SetExpression search = allOf(B_NAME);
		Order order = order(attribute(A_NAME, A1_NAME));
		RevisionQuery<KnowledgeObject> query = queryUnresolved(search, order);
		query.setRangeParam(rangeParam);
		RevisionQueryArguments queryArguments = revisionArgs();
		List<KnowledgeObject> expected;
		switch (rangeParam) {
			case complete:
				expected = list(b1, b2, b3, b4);
				break;
			case first:
				expected = list(b1);
				break;
			case head:
				queryArguments.setStopRow(3);
				expected = list(b1, b2, b3);
				break;
			case range:
				queryArguments.setStartRow(1);
				queryArguments.setStopRow(3);
				expected = list(b2, b3);
				break;
			default:
				throw RangeParam.unknownRangeParam(rangeParam);
		}
		List<KnowledgeObject> result = kb().search(query, queryArguments);
		assertEquals(expected, result);
	}

	public void testFindNotBranchedTypesHistoryQuery() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		// As B and D are both A's
		MetaObject bType = kb().getMORepository().getType(B_NAME);
		assertTrue(bType.isSubtypeOf(A_NAME));
		MetaObject dType = kb().getMORepository().getType(D_NAME);
		assertTrue(dType.isSubtypeOf(A_NAME));

		Transaction createTx = begin();
		KnowledgeObject b = newB("b1");
		TLID bName = b.tId().getObjectName();
		KnowledgeObject d = newD("d1");
		TLID dName = d.tId().getObjectName();
		commit(createTx);
		Branch branch = kb().createBranch(trunk(), lastRevision(), set(bType));
		Revision branchBaseRevision = branch.getBaseRevision();

		Transaction deleteOnTrunkTx = begin();
		b.delete();
		commit(deleteOnTrunkTx);

		Transaction deleteOnBranchTx = begin();
		onBranch(branch, d).delete();
		commit(deleteOnBranchTx);

		Branch formerBranch = HistoryUtils.setContextBranch(trunk());
		try {
			Map<?, List<LongRange>> searchTrunk = kb().search(historyQuery(anyOf(A_NAME)));
			ObjectBranchId bID = new ObjectBranchId(trunk().getBranchId(), bType, bName);
			List<LongRange> bLifeRange = searchTrunk.get(bID);
			assertNotNull(bLifeRange);
			assertEquals(LongRangeSet.range(createTx.getCommitRevision().getCommitNumber(), deleteOnTrunkTx
				.getCommitRevision().getCommitNumber() - 1), bLifeRange);
			ObjectBranchId dID = new ObjectBranchId(trunk().getBranchId(), dType, dName);
			List<LongRange> dLifeRange = searchTrunk.get(dID);
			assertNotNull(dLifeRange);
			assertEquals(LongRangeSet.range(createTx.getCommitRevision().getCommitNumber(), deleteOnBranchTx
				.getCommitRevision().getCommitNumber() - 1), dLifeRange);
		} finally {
			HistoryUtils.setContextBranch(formerBranch);
		}
		formerBranch = HistoryUtils.setContextBranch(branch);
		try {
			Map<?, List<LongRange>> searchTrunk = kb().search(historyQuery(anyOf(A_NAME)));
			ObjectBranchId bID = new ObjectBranchId(branch.getBranchId(), bType, bName);
			List<LongRange> bLifeRange = searchTrunk.get(bID);
			assertNotNull(bLifeRange);
			assertEquals(LongRangeSet.range(branchBaseRevision.getCommitNumber() + 1, Revision.CURRENT_REV),
				bLifeRange);
			// D shines throug so branch is trunk
			ObjectBranchId dID = new ObjectBranchId(trunk().getBranchId(), dType, dName);
			List<LongRange> dLifeRange = searchTrunk.get(dID);
			assertNotNull(dLifeRange);
			assertEquals(LongRangeSet.range(createTx.getCommitRevision().getCommitNumber(), deleteOnBranchTx
				.getCommitRevision().getCommitNumber() - 1), dLifeRange);
		} finally {
			HistoryUtils.setContextBranch(formerBranch);
		}

	}

	public void testFindNotBranchedTypes() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		// As B and D are both A's
		MetaObject bType = kb().getMORepository().getType(B_NAME);
		assertTrue(bType.isSubtypeOf(A_NAME));
		MetaObject dType = kb().getMORepository().getType(D_NAME);
		assertTrue(dType.isSubtypeOf(A_NAME));

		Transaction createTx = begin();
		KnowledgeObject b = newB("b1");
		KnowledgeObject d = newD("d1");
		commit(createTx);
		Branch branch = kb().createBranch(trunk(), lastRevision(), set(bType));

		Branch formerBranch = HistoryUtils.setContextBranch(trunk());
		try {
			List<KnowledgeObject> searchTrunk = kb().search(queryUnresolved(anyOf(A_NAME)));
			assertEquals(set(b, d), toSet(searchTrunk));
		} finally {
			HistoryUtils.setContextBranch(formerBranch);
		}
		formerBranch = HistoryUtils.setContextBranch(branch);
		try {
			List<KnowledgeObject> searchTrunk = kb().search(queryUnresolved(anyOf(A_NAME)));
			KnowledgeItem bBranch = onBranch(branch, b);
			KnowledgeItem dBranch = onBranch(branch, d);
			assertEquals(set(bBranch, dBranch), toSet(searchTrunk));
		} finally {
			HistoryUtils.setContextBranch(formerBranch);
		}

	}

	public void testOrderNameAttributeOfReferencedAttribute() throws DataObjectException {
		List<String> names = createNames(20);
		List<TLID> ids = createIds(20);
		Collections.sort(ids);
		final String referenceName = REFERENCE_POLY_HIST_GLOBAL_NAME;
		crateRefereeSearchSetup(ids, names, referenceName);

		RevisionQuery<KnowledgeObject> query =
			queryUnresolved(allOf(E_NAME), order(reference(E_NAME, referenceName, ReferencePart.name)));
		List<KnowledgeObject> result = kb().search(query);
		assertEquals(ids, mapToReferenceAttribute(result, referenceName, ReferencePart.name));

		RevisionQuery<KnowledgeObject> queryDescending =
			queryUnresolved(allOf(E_NAME), orderDesc(reference(E_NAME, referenceName, ReferencePart.name)));
		List<KnowledgeObject> resultDescending = kb().search(queryDescending);
		List<TLID> reverse = new ArrayList<>(ids);
		Collections.reverse(reverse);
		assertEquals(reverse, mapToReferenceAttribute(resultDescending, referenceName, ReferencePart.name));
	}

	private void crateRefereeSearchSetup(List<TLID> ids, List<String> names, String referenceAttribute)
			throws DataObjectException {
		List<TLID> idCopy = shuffle(ids, 15843982487L);
		final Transaction tx = begin();
		List<KnowledgeObject> referees = new ArrayList<>();
		for (int i = 0; i < idCopy.size(); i++) {
			// randomise creation of elements
			KnowledgeObject e = kb().createKnowledgeObject(idCopy.get(i), E_NAME);
			setA1(e, names.get(i));
			referees.add(e);
		}
		for (int i = 0; i < ids.size(); i++) {
			referees.get(i).setAttributeValue(referenceAttribute, referees.get(idCopy.indexOf(ids.get(i))));
		}
		commit(tx);
	}

	private List<?> mapToReferenceAttribute(List<KnowledgeObject> l, final String attributeName,
			final ReferencePart part) {
		return Mappings.map(new Mapping<KnowledgeItem, Object>() {

			@Override
			public Object map(KnowledgeItem input) {
				try {
					KnowledgeItem item = (KnowledgeItem) input.getAttributeValue(attributeName);
					ObjectKey objectKey = item.tId();
					switch (part) {
						case branch:
							return Long.valueOf(objectKey.getBranchContext());
							case name:
								return objectKey.getObjectName();
							case revision:
								return objectKey.getHistoryContext();
							case type:
								return objectKey.getObjectType();
						}
					throw ReferencePart.noSuchPart(part);
					} catch (NoSuchAttributeException ex) {
						throw new RuntimeException(ex);
					}
				}
		}, l);
	}

	private <T> List<T> shuffle(List<T> names, long seed) {
		List<T> nameCopy = new ArrayList<>(names);
		Collections.shuffle(nameCopy, new Random(seed));
		return nameCopy;
	}

	private List<TLID> createIds(int numberElements) {
		List<TLID> ids = new ArrayList<>();
		for (int i = 0; i < numberElements; i++) {
			ids.add(kb().createID());
		}
		return ids;
	}

	private List<String> createNames(int numberElements) {
		StringBuilder nulls = new StringBuilder();
		for (int i = 0, size = String.valueOf(numberElements).length(); i < size; i++) {
			nulls.append(0);
		}
		List<String> names = new ArrayList<>();
		for (int i = 0; i < numberElements; i++) {
			names.add("e" + nulls.substring(String.valueOf(i).length()) + i);
		}
		return names;
	}

	private List<String> mapToAttribute(List<? extends KnowledgeItem> l, final String attributeName) {
		return Mappings.map(new Mapping<KnowledgeItem, String>() {

			@Override
			public String map(KnowledgeItem input) {
				try {
					return (String) input.getAttributeValue(attributeName);
				} catch (NoSuchAttributeException ex) {
					throw new RuntimeException(ex);
				}
			}
		}, l);
	}

	public void testAllReferences() throws DataObjectException {

		final Transaction tx = begin();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject refernceObject = newD("d1");
		final ObjectKey destKey = refernceObject.tId();
		setReference(e1, refernceObject, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		setReference(e1, refernceObject, !MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		setReference(e1, refernceObject, !MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		setReference(e1, refernceObject, !MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		setReference(e1, refernceObject, MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		setReference(e1, refernceObject, MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		setReference(e1, refernceObject, MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		setReference(e1, refernceObject, MONOMORPHIC, HistoryType.HISTORIC, BRANCH_GLOBAL);
		tx.commit();
		final Revision commitRevision = tx.getCommitRevision();
		final ObjectKey stableDestKey = HistoryUtils.getKnowledgeItem(commitRevision, refernceObject).tId();

		check(e1, destKey, stableDestKey, ReferencePart.name);
		check(e1, destKey, stableDestKey, ReferencePart.branch);
		check(e1, destKey, stableDestKey, ReferencePart.type);
		check(e1, destKey, stableDestKey, ReferencePart.revision);
	}

	private void check(KnowledgeObject source, ObjectKey destKey, ObjectKey stableDestKey, ReferencePart aspect) {
		check(source, destKey, REFERENCE_POLY_CUR_LOCAL_NAME, aspect);
		check(source, stableDestKey, REFERENCE_POLY_HIST_LOCAL_NAME, aspect);
		check(source, destKey, REFERENCE_POLY_CUR_GLOBAL_NAME, aspect);
		check(source, stableDestKey, REFERENCE_POLY_HIST_GLOBAL_NAME, aspect);
		check(source, destKey, REFERENCE_MONO_CUR_LOCAL_NAME, aspect);
		check(source, stableDestKey, REFERENCE_MONO_HIST_LOCAL_NAME, aspect);
		check(source, destKey, REFERENCE_MONO_CUR_GLOBAL_NAME, aspect);
		check(source, stableDestKey, REFERENCE_MONO_HIST_GLOBAL_NAME, aspect);
	}

	private void check(KnowledgeObject source, final ObjectKey destKey, String referenceAttr,
					ReferencePart aspect) {
		Expression target;
		switch (aspect) {
			case type: {
				target = ExpressionFactory.type(literal(destKey));
				break;
			}
			case revision: {
				target = revision(literal(destKey));
				break;
			}
			case branch: {
				target = branch(literal(destKey));
				break;
			}
			case name: {
				target = identifier(literal(destKey));
				break;
			}
			default: {
				throw ReferencePart.noSuchPart(aspect);
			}
		}

		SetExpression expr = filter(allOf(E_NAME), eqBinary(reference(E_NAME, referenceAttr, aspect), target));
		assertEquals(set(source), toSet(kb().search(queryUnresolved(expr))));
	}

	public void testReferenceInHistory() throws DataObjectException {
		final Revision startReferenceAtE1;
		KnowledgeObject refernceObject;
		KnowledgeObject e1;
		KnowledgeObject e2;
		final ObjectKey destKey;
		{
			final Transaction tx = begin();
			e1 = newE("e1");
			refernceObject = newD("d1");
			destKey = refernceObject.tId();
			setReference(e1, refernceObject, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			tx.commit();
			startReferenceAtE1 = tx.getCommitRevision();
		}
		final Revision startReferenceAtE2;
		{
			final Transaction tx = begin();
			e2 = newE("e1");
			setReference(e2, refernceObject, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			tx.commit();
			startReferenceAtE2 = tx.getCommitRevision();
		}
		Revision endReferenceAtE2;
		{
			final Transaction tx = begin();
			setReference(e1, null, !MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
			tx.commit();
			endReferenceAtE2 = tx.getCommitRevision();
		}

		SetExpression expr =
			filter(
				allOf(E_NAME),
				eqBinary(reference(E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME, ReferencePart.name),
					literal(destKey.getObjectName())));

		final Map<?, List<LongRange>> searchResult = kb().search(historyQuery(expr));
		assertEquals(2, searchResult.size());
		assertEquals(LongRangeSet.range(startReferenceAtE1.getCommitNumber(), endReferenceAtE2.getCommitNumber() - 1),
			searchResult.get(ObjectBranchId.toObjectBranchId(e1.tId())));
		assertEquals(LongRangeSet.endSection(startReferenceAtE2.getCommitNumber()),
			searchResult.get(ObjectBranchId.toObjectBranchId(e2.tId())));
	}

	public void testIsNull() throws DataObjectException {
		KnowledgeObject b1;
		KnowledgeObject b2;

		{
			Transaction tx = begin();
			b1 = newB("d1");
			setB1(b1, "b1");
			b2 = newB("d2");
			setB1(b2, null);
			tx.commit();
		}

		SetExpression isNull = filter(allOf(B_NAME), isNull(attribute(B_NAME, B1_NAME)));
		assertEquals(set(b2), toSet(searchCurrentTrunk(isNull)));

		SetExpression constantNotNull = filter(allOf(B_NAME), isNull(literal("not Null")));
		assertEquals(set(), toSet(searchCurrentTrunk(constantNotNull)));
	}

	/**
	 * Tests searching objects in current that points to itself.
	 */
	public void testReferenceToSelfCurrent() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Transaction tx = begin();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");
		String pclAttribute = getReferenceAttr(!MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		String pcgAttribute = getReferenceAttr(!MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		e1.setAttributeValue(pclAttribute, e1);
		e1.setAttributeValue(pcgAttribute, e1);
		e2.setAttributeValue(pclAttribute, e1);
		tx.commit();

		Branch branch = HistoryUtils.createBranch(trunk(), lastRevision());
		KnowledgeItem e1OnBranch = HistoryUtils.getKnowledgeItem(branch, e1);

		RevisionQuery<KnowledgeObject> filterLocalReferenceToSelf =
			queryUnresolved(filter(allOf(E_NAME), eqBinary(reference(E_NAME, pclAttribute), context())));
		filterLocalReferenceToSelf.setBranchParam(BranchParam.all);

		Set<KnowledgeObject> resultLocal = toSet(kb().search(filterLocalReferenceToSelf));
		assertTrue("e1 points to itself.", resultLocal.contains(e1));
		assertFalse("e2 points to e1.", resultLocal.contains(e2));
		assertTrue("e1 on branch points to itself because reference is local.", resultLocal.contains(e1OnBranch));
		assertEquals(set(e1, e1OnBranch), resultLocal);

		RevisionQuery<KnowledgeObject> filterGlobalReferenceToSelf =
			queryUnresolved(filter(allOf(E_NAME), eqBinary(reference(E_NAME, pcgAttribute), context())));
		filterGlobalReferenceToSelf.setBranchParam(BranchParam.all);

		Set<KnowledgeObject> resultGlobal = toSet(kb().search(filterGlobalReferenceToSelf));
		assertTrue("e1 points to itself.", resultGlobal.contains(e1));
		assertFalse("e2 points to e1.", resultGlobal.contains(e2));
		assertFalse("e1 on branch points to e1 on trunk.", resultGlobal.contains(e1OnBranch));
		assertEquals(set(e1), resultGlobal);
	}

	/**
	 * Tests searching objects that points to itself in an historic context.
	 */
	public void testReferenceToSelfHistoric() throws DataObjectException {
		Transaction tx1 = begin();
		KnowledgeObject e2 = newE("e2");
		tx1.commit();
		Revision r1 = tx1.getCommitRevision();

		Transaction tx2 = begin();
		KnowledgeObject e1 = newE("e1");
		String phlAttribute = getReferenceAttr(!MONOMORPHIC, HistoryType.HISTORIC, !BRANCH_GLOBAL);
		e1.setAttributeValue(phlAttribute, e1);
		tx2.commit();
		Revision r2 = tx2.getCommitRevision();

		Transaction tx3 = begin();
		e2.setAttributeValue(phlAttribute, e1);
		tx3.commit();
		Revision r3 = tx3.getCommitRevision();

		RevisionQuery<KnowledgeObject> filterReferenceToSelf =
			queryUnresolved(filter(allOf(E_NAME), eqBinary(reference(E_NAME, phlAttribute), context())));

		List<KnowledgeObject> r1Result =
			kb().search(filterReferenceToSelf, revisionArgs().setRequestedRevision(r1.getCommitNumber()));
		assertEmpty(true, r1Result);
		List<KnowledgeObject> r3Result =
			kb().search(filterReferenceToSelf, revisionArgs().setRequestedRevision(r3.getCommitNumber()));
		assertEmpty(true, r3Result);
		List<KnowledgeObject> r2Result =
			kb().search(filterReferenceToSelf, revisionArgs().setRequestedRevision(r2.getCommitNumber()));
		assertEquals(list(HistoryUtils.getKnowledgeItem(r2, e1)), r2Result);
	}

	/**
	 * Tests searching object that points to itself via one indirection, i.e.
	 * 
	 * <pre>
	 * 	A -x-&gt; B -y-&gt; A
	 * </pre>
	 * 
	 * where -x-&gt; means reference via attribute x.
	 * 
	 */
	public void testReferenceToSelfAfterNavigation() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");
		String pcgAttribute = getReferenceAttr(!MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		String pclAttribute = getReferenceAttr(!MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		e1.setAttributeValue(pcgAttribute, e2);
		e2.setAttributeValue(pclAttribute, e1);
		tx.commit();

		RevisionQuery<KnowledgeObject> filterReferenceToSelfViaOneIndirection =
			queryUnresolved(filter(allOf(E_NAME),
		and(
			// attribute is defined in E
			eval(reference(E_NAME, pcgAttribute), hasType(E_NAME)),
			eqBinary(
				eval(reference(E_NAME, pcgAttribute), reference(E_NAME, pclAttribute)),
				context()))));

		Set<KnowledgeObject> result;
		try {
			result = toSet(kb().search(filterReferenceToSelfViaOneIndirection));
		} catch (Exception e) {
			/* Exptected due to the known bug in ticket #9402: Polymorphic navigation. */
			return;
		}
		fail("Test should fail due to the known bug in ticket #9402: Polymorphic navigation.");
		assertTrue(result.contains(e1));
		assertFalse(result.contains(e2));
	}

	/**
	 * Tests searching objects with same references.
	 */
	public void testSearchReferenceEquality() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		Transaction tx = begin();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");
		String pcgAttribute = getReferenceAttr(!MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		String pclAttribute = getReferenceAttr(!MONOMORPHIC, HistoryType.CURRENT, !BRANCH_GLOBAL);
		e1.setAttributeValue(pcgAttribute, e2);
		e1.setAttributeValue(pclAttribute, e2);
		tx.commit();

		Branch branch = HistoryUtils.createBranch(trunk(), lastRevision());
		KnowledgeItem e2Branch1 = HistoryUtils.getKnowledgeItem(branch, e2);
		KnowledgeItem e1Branch1 = HistoryUtils.getKnowledgeItem(branch, e1);

		RevisionQuery<KnowledgeObject> filterReferenceEquality =
			queryUnresolved(filter(allOf(E_NAME), eqBinary(reference(E_NAME, pcgAttribute), reference(E_NAME, pclAttribute))));
		filterReferenceEquality.setBranchParam(BranchParam.all);

		Set<KnowledgeObject> result = toSet(kb().search(filterReferenceEquality));
		assertTrue("Both references point to '" + e2 + "'", result.contains(e1));
		assertTrue("Both references point to null", result.contains(e2));
		assertTrue("Both references point to null", result.contains(e2Branch1));
		assertFalse("One reference points to object on trunk, the other to object on branch",
			result.contains(e1Branch1));
		assertEquals(set(e2, e1, e2Branch1), result);
	}

	/**
	 * Tests searching reference attributes.
	 */
	public void testSearchReferenceAttribute() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject e1 = newE("e1");
		KnowledgeObject e2 = newE("e2");
		String referenceAttribute = getReferenceAttr(!MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		e1.setAttributeValue(referenceAttribute, e2);
		tx.commit();

		RevisionQuery<KnowledgeObject> filterReferenceNull =
			queryUnresolved(filter(allOf(E_NAME), isNull(reference(E_NAME, referenceAttribute))));
		RevisionQuery<KnowledgeObject> filterReferenceE2 =
			queryUnresolved(filter(allOf(E_NAME), eqBinary(reference(E_NAME, referenceAttribute), literal(e2))));
		RevisionQuery<KnowledgeObject> filterReferenceE1 =
			queryUnresolved(filter(allOf(E_NAME), eqBinary(reference(E_NAME, referenceAttribute), literal(e1))));

		assertEquals(set(e2), toSet(kb().search(filterReferenceNull)));
		assertEquals(set(e1), toSet(kb().search(filterReferenceE2)));
		assertEquals(set(), toSet(kb().search(filterReferenceE1)));

		RevisionQuery<KnowledgeObject> filterReferenceParameter =
			queryUnresolved(filter(allOf(E_NAME), eqBinary(reference(E_NAME, referenceAttribute), param("param"))));
		filterReferenceParameter.setSearchParams(params(paramDecl(E_NAME, "param")));

		assertEquals(set(e1), toSet(kb().search(filterReferenceParameter, revisionArgs().setArguments(e2))));
		assertEquals(set(), toSet(kb().search(filterReferenceParameter, revisionArgs().setArguments(e1))));
	}

	/**
	 * Tests searching boolean attributes.
	 */
	public void testSearchBooleanAttribute() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x_true = newX("x10_true");
		x_true.setAttributeValue(X1_NAME, Boolean.TRUE);
		KnowledgeObject x_false = newX("x10_false");
		x_false.setAttributeValue(X1_NAME, Boolean.FALSE);
		tx.commit();
		
		SetExpression filterFalse = filter(allOf(X_NAME), eqBinary(attribute(X_NAME, X1_NAME), literal(Boolean.FALSE)));
		SetExpression filterTrue = filter(allOf(X_NAME), eqBinary(attribute(X_NAME, X1_NAME), literal(Boolean.TRUE)));
		SetExpression filterNotFalse =
			filter(allOf(X_NAME), eqBinary(attribute(X_NAME, X1_NAME), not(literal(Boolean.FALSE))));
		
		assertEquals(set(x_false), toSet(kb().search(queryUnresolved(filterFalse))));
		assertEquals(set(x_true), toSet(kb().search(queryUnresolved(filterTrue))));
		assertEquals(set(x_true), toSet(kb().search(queryUnresolved(filterNotFalse))));
	}

	/**
	 * Tests searching boolean attributes with query that do not produce {@link PreparedStatement}
	 * but executes SQL directly.
	 */
	public void testSearchBooleanAttribute2() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject x_true = newX("x10_true");
		x_true.setAttributeValue(X1_NAME, Boolean.TRUE);
		KnowledgeObject x_false = newX("x10_false");
		x_false.setAttributeValue(X1_NAME, Boolean.FALSE);
		tx.commit();

		SetExpression filterFalse = filter(allOf(X_NAME), eqBinary(attribute(X_NAME, X1_NAME), literal(Boolean.FALSE)));
		
		SetExpression filterTrue = filter(allOf(X_NAME), eqBinary(attribute(X_NAME, X1_NAME), literal(Boolean.TRUE)));
		SetExpression filterNotFalse =
			filter(allOf(X_NAME), eqBinary(attribute(X_NAME, X1_NAME), not(literal(Boolean.FALSE))));

		RevisionQuery<KnowledgeObject> falseQuery = queryUnresolved(filterFalse);
		falseQuery.setRangeParam(RangeParam.head);
		RevisionQuery<KnowledgeObject> trueQuery = queryUnresolved(filterTrue);
		trueQuery.setRangeParam(RangeParam.head);
		RevisionQuery<KnowledgeObject> notFalseQuery = queryUnresolved(filterNotFalse);
		notFalseQuery.setRangeParam(RangeParam.head);
		// Range is just a workaround to trigger that a statement is used and no prepared statement
		RevisionQueryArguments arguments = revisionArgs();
		arguments.setStopRow(Integer.MAX_VALUE);
		assertEquals(set(x_false), toSet(kb().search(falseQuery, arguments)));
		assertEquals(set(x_true), toSet(kb().search(trueQuery, arguments)));
		assertEquals(set(x_true), toSet(kb().search(notFalseQuery, arguments)));
	}

	/**
	 * Example for navigating over links that have a special marker.
	 */
	public void testFilteredNagivation() throws DataObjectException {
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final KnowledgeObject c1;
		final KnowledgeObject c2;
		final KnowledgeObject c3;
		final KnowledgeObject c4;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newC("b2");

			c1 = newC("c1");
			c2 = newC("c2");
			c3 = newC("c1");
			c4 = newC("c2");

			setBC1(newBC(b1, c1), "attr1");
			setBC1(newBC(b1, c2), "attr1");

			setBC1(newBC(b1, c3), "attr2");
			setBC1(newBC(b1, c4), "attr2");

			setBC1(newBC(b2, c1), "attr1");
			setBC1(newBC(b2, c2), "attr1");

			setBC1(newBC(b2, c3), "attr2");
			setBC1(newBC(b2, c4), "attr2");

			commit(tx);
		}

		SetExpression fromExactlyB1 = setLiteralOfEntries(b1);

		SetExpression onlyLinksAttributedWithAttr1 = filter(
			allOf(BC_NAME),
			eqBinary(attribute(BC_NAME, BC1_NAME), literal("attr1")));

		boolean forwards = false;
		SetExpression expr =
			navigate(forwards, fromExactlyB1, onlyLinksAttributedWithAttr1, C_NAME);

		List<?> result = kb().search(queryUnresolved(expr));
		assertEquals(set(c1, c2), toSet(result));
	}

	/**
	 * Example for navigating <em>backwards</em> over links that have a special marker.
	 */
	public void testBackwardFilteredNagivation() throws DataObjectException {
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final KnowledgeObject c1;
		final KnowledgeObject c2;
		final KnowledgeObject c3;
		final KnowledgeObject c4;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newC("b2");

			c1 = newC("c1");
			c2 = newC("c2");
			c3 = newC("c1");
			c4 = newC("c2");

			setBC1(newBC(c1, b1), "attr1"); // c1 --BC(attr1)--> b1
			setBC1(newBC(c2, b1), "attr1"); // c2 --BC(attr1)--> b1

			setBC1(newBC(c3, b1), "attr2"); // c3 --BC(attr2)--> b1
			setBC1(newBC(c4, b1), "attr2"); // c4 --BC(attr2)--> b1

			setBC1(newBC(c1, b2), "attr1"); // c1 --BC(attr1)--> b2
			setBC1(newBC(c2, b2), "attr1"); // c2 --BC(attr1)--> b2

			setBC1(newBC(c3, b2), "attr2"); // c3 --BC(attr2)--> b2
			setBC1(newBC(c4, b2), "attr2"); // c4 --BC(attr2)--> b2

			tx.commit();
		}

		SetExpression fromExactlyB1 = setLiteralOfEntries(b1);

		SetExpression onlyLinksAttributedWithAttr1 = filter(
			allOf(BC_NAME),
			eqBinary(attribute(BC_NAME, BC1_NAME), literal("attr1")));

		boolean backwards = true;
		SetExpression expr =
			navigate(backwards, fromExactlyB1, onlyLinksAttributedWithAttr1, C_NAME);

		Set<?> result = toSet(kb().search(queryUnresolved(expr)));
		assertEquals(set(c1, c2), result);
	}

	public void testNagivationWithOrder() throws SQLException, DataObjectException {
		final KnowledgeObject b1;
		final KnowledgeObject b2; // A C that is used as B.
		final KnowledgeObject c1;
		final KnowledgeObject c3;
		final KnowledgeObject c2;
		final KnowledgeObject d1;
		final KnowledgeObject d2;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newC("b2");
			
			c1 = newC("c1");
			c3 = newC("c3");
			c2 = newC("c2");
			
			setA2(c1, "B");
			setA2(c2, "C");
			setA2(c3, "D");
			
			d1 = newD("d1");
			d2 = newD("d2");
			
			setA2(d1, "A");
			setA2(d2, "Z");
			
			newBC(b1, c1);
			newBC(b2, c2);
			newBC(b2, c3);
			
			newBC(b1, d1);
			newBC(b1, d2);
			
			commit(tx);
		}
            
		SetExpression expr = navigateForwards(anyOf(B_NAME), BC_NAME, C_NAME);
            
		List<?> resultCurrent = kb().search(queryUnresolved(expr, order(attribute(A_NAME, A2_NAME))));
		// order(attribute(A_NAME, A2_NAME)
    
		assertEquals("Different reference targets found", set(c1, c2, c3), toSet(resultCurrent));
    
		expr = navigateForwards(anyOf(B_NAME), BC_NAME, A_NAME); // Now polymorphic
            
            resultCurrent = kb().search(queryUnresolved(expr, order(attribute(A_NAME, A2_NAME))));
		// order(attribute(A_NAME, A2_NAME)
    
		assertEquals("Different reference targets found", set(c1, c2, c3, d1, d2), toSet(resultCurrent));
    }

    public void testAssociationEndTypeQuery() throws DataObjectException {
		RevisionQuery<KnowledgeAssociation> query = queryUnresolved(filter(
		anyOf(AB_NAME),
		and(
			inSet(source(), allOf(B_NAME)),
			inSet(destination(), allOf(C_NAME)))), KnowledgeAssociation.class);
		
		final KnowledgeAssociation b1c2;
		{
			Transaction tx = begin();
			KnowledgeObject b1 = newB("b1");
			KnowledgeObject c1 = newC("c1");
			KnowledgeObject b2 = newB("b2");
			KnowledgeObject c2 = newC("c2");
			
			b1c2 = newAB(b1, c2);
			newAB(b1, b2);
			newAB(c1, c2);
			newAB(c1, b2);
			
			commit(tx);
		}
		
		List<?> result = kb().search(query);
		assertEquals(set(b1c2), toSet(result));
	}
	
	public void testInSetOfLiteralSetOfPrimitives() throws DataObjectException {
		RevisionQuery<KnowledgeObject> query = queryUnresolved(filter(
		anyOf(B_NAME),
		inSet(attribute(A_NAME, A1_NAME), setLiteral(set("b1", "b3", "b5")))));
		
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final KnowledgeObject b3;
		final KnowledgeObject b4;
		final KnowledgeObject b5;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b3");
			b4 = newB("b4");
			b5 = newB("b5");
			commit(tx);
		}
		
		assertNotNull(b2);
		assertNotNull(b4);
		
		List<?> result = kb().search(query);
		assertEquals(set(b1, b3, b5), toSet(result));
	}
	
	public void testInSetOfLiteralSetOfItems() throws DataObjectException {
		KnowledgeObject b1;
		KnowledgeObject b2;
		KnowledgeObject b3;
		KnowledgeObject b4;
		KnowledgeObject b5;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b3");
			b4 = newB("b4");
			b5 = newB("b5");
			commit(tx);
		}
		
		RevisionQuery<KnowledgeObject> query = queryUnresolved(filter(
		anyOf(B_NAME),
		inSet(setLiteral(set(b1, b3, b5)))));
		
		assertNotNull(b2);
		assertNotNull(b4);
		
		List<?> result = kb().search(query);
		assertEquals("Ticket #9934: ", set(b1, b3, b5), toSet(result));
	}
	
	public void testObjectComparison() throws DataObjectException {
		RevisionQuery<KnowledgeAssociation> query = queryUnresolved(params(
		paramDecl(B_NAME, "self")), filter(
		anyOf(AB_NAME),
		eqBinary(
			destination(), 
			param("self"))), null, KnowledgeAssociation.class);
		
		
		KnowledgeObject b1;
		KnowledgeObject b2;
		KnowledgeObject c1;
		KnowledgeObject d1;
		KnowledgeObject d2;
		KnowledgeObject d3;
		KnowledgeAssociation d1b1;
		KnowledgeAssociation d2b2;
		KnowledgeAssociation d3c1;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			c1 = newB("c1");
			d1 = newD("d1");
			d2 = newD("d2");
			d3 = newD("d3");
			d1b1 = newAB(d1, b1);
			d2b2 = newAB(d2, b2);
			d3c1 = newAB(d3, c1);
			commit(tx);
		}
		
		assertEquals(list(d1b1), kb().search(query, revisionArgs().setArguments(b1)));
		assertEquals(list(d2b2), kb().search(query, revisionArgs().setArguments(b2)));
		assertEquals(list(d3c1), kb().search(query, revisionArgs().setArguments(c1)));
	}

	public void testQueryParameter() throws DataObjectException {
		RevisionQuery<KnowledgeObject> query = queryUnresolved(params(
		paramDecl(MOPrimitive.INTEGER, "from"), 
		paramDecl(MOPrimitive.INTEGER, "to")), filter(
		anyOf(X_NAME),
		and(
			ge(
				attribute(X_NAME, X7_NAME),
				param("from")),
			le(
				attribute(X_NAME, X7_NAME),
				param("to")))), null);
		
		KnowledgeObject x1;
		KnowledgeObject x2;
		KnowledgeObject x3;
		KnowledgeObject x4;
		{
			Transaction tx = begin();
			x1 = newX("x1");
			setX7(x1, 10);
			x2 = newX("x2");
			setX7(x2, 15);
			x3 = newX("x3");
			setX7(x3, 39);
			x4 = newX("x4");
			setX7(x4, 3);
			commit(tx);
		}
		
		assertEquals(set(x1, x2), toSet(kb().search(query, revisionArgs().setArguments(9, 20))));
		assertEquals(set(x2, x3), toSet(kb().search(query, revisionArgs().setArguments(15, 39))));
		assertEquals(set(), toSet(kb().search(query, revisionArgs().setArguments(27, 38))));
		assertEquals(set(x1, x2, x3, x4), toSet(kb().search(query, revisionArgs().setArguments(0, 99))));
	}

	/**
	 * @see TestFlexWrapperCluster#testSearch()
	 */
	public void testNullComparision() throws DataObjectException {
		SetExpression expr = 
			filter(
				anyOf(B_NAME),
				eqBinary(
					attribute(B_NAME, B1_NAME),
					attribute(B_NAME, B2_NAME)));

		KnowledgeObject b1;
		KnowledgeObject b2;
		KnowledgeObject b3;
		KnowledgeObject b4;
		{
			Transaction tx = begin();
			// attribute b1 and b2 are both null
			b1 = newB("b1");
			
			b2 = newB("b2");
			setB1(b2, "v1");
			setB2(b2, "v1");
			
			b3 = newB("b3");
			setB1(b3, "v1");
			
			b4 = newB("b4");
			setB2(b4, "v1");
			commit(tx);
		}
		
		assertEquals(set(b1, b2), toSet(kb().search(queryUnresolved(expr), revisionArgs())));
	}
	
	public void testPolymorphicAttributeAccessOnReference() throws SQLException, DataObjectException {
		SetExpression expr = 
			filter(
				filter(
					map(
						anyOf(AB_NAME),
						destination()), 
					instanceOf(B_NAME)),
				eqBinary(
					attribute(B_NAME, B1_NAME),
					literal("vb1")));
		
		KnowledgeObject b1;
		KnowledgeObject b2;
		KnowledgeObject b3;
		KnowledgeObject b4;
		
		KnowledgeObject c1;
		KnowledgeObject c2;
		KnowledgeObject c3;
		KnowledgeObject c4;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b3");
			b4 = newB("b4");
			
			c1 = newC("c1");
			c2 = newC("c2");
			c3 = newC("c3");
			c4 = newC("c4");
			
			KnowledgeObject d1 = newD("d1");
			KnowledgeObject d2 = newD("d2");
			
			b1.setAttributeValue(B1_NAME, "vb1");
			c1.setAttributeValue(B1_NAME, "vb1");
			b2.setAttributeValue(B1_NAME, "not vb1");
			c2.setAttributeValue(B1_NAME, "not vb1");
			b4.setAttributeValue(B1_NAME, "vb1");
			c4.setAttributeValue(B1_NAME, "vb1");
			
			newAB(d1, b1);
			newAB(d1, b2);
			newAB(d1, c1);
			newAB(d1, c2);
			
			newAB(d1, d2);
			commit(tx);
		}
		
		List<KnowledgeObject> result = searchCurrentTrunk(expr);
		assertEquals(set(b1, c1), toSet(result));
	}
	
	public void testNotIn() throws SQLException, DataObjectException {
		KnowledgeObject b1;
		KnowledgeObject b2;
		
		KnowledgeObject c1;
		KnowledgeObject c2;
		
		KnowledgeObject d1;
		KnowledgeObject d2;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			c1 = newC("c1");
			d1 = newD("d1");
			
			b2 = newB("b2");
			c2 = newC("c2");
			d2 = newD("d2");
			
			newAB(b2, b1);
			newAB(c2, b1);
			newAB(d2, b1);
			commit(tx);
		}
		
		SetExpression expr =
			filter(anyOf(A_NAME), not(inSet(map(allOf(AB_NAME), source()))));
		
		List<KnowledgeObject> result = searchCurrentTrunk(expr);
		
		assertEquals(set(b1, c1, d1), toSet(result));
	}

	public void testPolymorphicMultiStepNavigation() throws SQLException, DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		KnowledgeObject c1;
		KnowledgeObject c2;
		KnowledgeObject c3;
		KnowledgeObject b1;
		KnowledgeObject b2; // A C that is used as B.
		KnowledgeObject b3;
		KnowledgeObject b4; // A C that is used as B.
		KnowledgeObject b5;
		KnowledgeObject b6; // A C that is used as B.
		KnowledgeObject d1;
		KnowledgeObject d1a;
		KnowledgeObject d2;
		KnowledgeObject d2a; // Union of references from d1 and d2
		KnowledgeObject d3; // Referenced from B but not from C
		KnowledgeObject d4; // Referenced from B but not from C
		KnowledgeObject d5; // Not referenced from B
		KnowledgeObject d6; // Not referenced from B
		long r1;
		{
			Transaction tx = begin();
			c1 = newC("c1");
			c2 = newC("c2");
			c3 = newC("c3");
			
			b1 = newB("b1");
			b2 = newC("b2");
			b3 = newB("b3"); 
			b4 = newC("b4");
			b5 = newB("b5"); 
			b6 = newC("b6");
			
			d1 = newD("d1");
			d1a = newD("d1a");
			d2 = newD("d2");
			d2a = newD("d2a");
			d3 = newD("d3");
			d4 = newD("d4");
			d5 = newD("d5");
			d6 = newD("d6");
			
			setA2(d1, "a2");
			setA2(d1a, "a2");
			setA2(d2, "a2");
			setA2(d2a, "a2");
			setA2(d3, "a2");
			setA2(d4, "a2");
			setA2(d5, "a2");
			
			newBC(b1, c1);
			newBC(b2, c2);
			newBC(b5, c3);
			newBC(b6, c3);
			
			newAB(b1, d1);
			newAB(b1, d1a);
			newAB(b2, d2);
			newAB(b1, d2a);
			newAB(b2, d2a);
			newAB(b3, d3);
			newAB(b4, d4);
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}
		
		long r2;
		{
			Transaction tx = begin();
			setA2(d1a, "not a2");
			commit(tx);
			r2 = tx.getCommitRevision().getCommitNumber();
		}
		
		assertNotNull(d6);
		
		SetExpression expr = 
			filter(
				navigateForwards(
					navigateBackwards(
						allOf(C_NAME),
						BC_NAME, B_NAME),
					AB_NAME, D_NAME),
				eqBinary(attribute(A_NAME, A2_NAME), literal("a2")));
		
		// Test search in current trunk.
		List<KnowledgeObject> resultCurrent = searchCurrentTrunk(expr);
		assertEquals(set(d1, d2, d2a), toSet(resultCurrent));
		
		// Test history search.
		List<KnowledgeObject> resultR1 = kb().search(queryUnresolved(expr), revisionArgs().setRequestedRevision(r1));
		Revision r1Rev = kb().getRevision(r1);
		assertEquals(
			set(
				HistoryUtils.getKnowledgeItem(r1Rev, d1), 
				HistoryUtils.getKnowledgeItem(r1Rev, d1a), 
				HistoryUtils.getKnowledgeItem(r1Rev, d2), 
				HistoryUtils.getKnowledgeItem(r1Rev, d2a)), 
			toSet(resultR1));

		// Create branch that is reverted to the initial r1 setup.
		Branch branch2 = kb().createBranch(kb().getTrunk(), kb().getRevision(r2), null);
		
		{
			Transaction tx = begin();
			setA2(onBranch(branch2, d1a), "a2");
			commit(tx);
		}
		
		// Again test that search in current trunk is not affected by the branch.
		List<KnowledgeObject> resultCurrentTrunk = searchCurrentTrunk(expr);
		assertEquals(set(d1, d2, d2a), toSet(resultCurrentTrunk));
		
		// Test query on branch2.
		List<KnowledgeObject> resultCurrentBranch2 =
			kb().search(queryUnresolved(expr), revisionArgs().setRequestedBranch(branch2));
		assertEquals(
			set(
				onBranch(branch2, d1),
				onBranch(branch2, d1a),
				onBranch(branch2, d2),
				onBranch(branch2, d2a)),
			toSet(resultCurrentBranch2));
	}

    public void testHistoricSearch() throws SQLException, DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		SetExpression expr = 
			filter(
				navigateForwards(
					filter(
						anyOf(A_NAME),
						attributeEqBinary(A_NAME, A2_NAME, "a2")),
					filter(
						anyOf(AB_NAME),
						attributeEqBinary(AB_NAME, AB1_NAME, "ab1")),
					B_NAME),
				eqBinary(attribute(B_NAME, B2_NAME), literal("b2")));
		
		
		KnowledgeObject sb1;
		KnowledgeObject sc1;
		KnowledgeObject sd1;
		
		KnowledgeObject db1;
		KnowledgeObject db2;
		KnowledgeObject db3;
		KnowledgeObject dc1;
		KnowledgeObject dc2;
		KnowledgeObject dc3;
		
		KnowledgeAssociation b1b1;
		KnowledgeAssociation b1b2;
		KnowledgeAssociation b1c3;
		KnowledgeAssociation c1c1;
		KnowledgeAssociation d1c2;
		KnowledgeAssociation d1b3;
		
		long r1;
		{
			Transaction tx = begin();
			// Create matching sources.
			sb1 = newB("sb1");
			sc1 = newC("sc1");
			sd1 = newD("sd1");
			setA2(sb1, "a2");
			setA2(sc1, "a2");
			setA2(sd1, "a2");
			
			// Create matching destination.
			db1 = newB("db1");
			db2 = newB("db2");
			db3 = newB("db3");
			dc1 = newC("dc1");
			dc2 = newC("dc2");
			dc3 = newC("dc3");
			db1.setAttributeValue(B2_NAME, "b2");
			db2.setAttributeValue(B2_NAME, "b2");
			db3.setAttributeValue(B2_NAME, "b2");
			dc1.setAttributeValue(B2_NAME, "b2");
			dc2.setAttributeValue(B2_NAME, "b2");
			dc3.setAttributeValue(B2_NAME, "b2");
			
			// Create matching associations.
			b1b1 = newAB(sb1, db1);
			b1b2 = newAB(sb1, db2);
			b1c3 = newAB(sb1, dc3);
			c1c1 = newAB(sc1, dc1);
			d1c2 = newAB(sd1, dc2);
			d1b3 = newAB(sd1, db3);
			b1b1.setAttributeValue(AB1_NAME, "ab1");
			b1b2.setAttributeValue(AB1_NAME, "ab1");
			b1c3.setAttributeValue(AB1_NAME, "ab1");
			c1c1.setAttributeValue(AB1_NAME, "ab1");
			d1c2.setAttributeValue(AB1_NAME, "ab1");
			d1b3.setAttributeValue(AB1_NAME, "ab1");
			
			// Create other objects.
			KnowledgeObject b1 = newB("b1");
			KnowledgeObject b2 = newB("b2");
			KnowledgeObject b3 = newB("b3");
			KnowledgeObject b4 = newB("b4");
			KnowledgeObject c1 = newC("c1");
			KnowledgeObject c2 = newC("c2");
			KnowledgeObject c3 = newC("c3");
			KnowledgeObject c4 = newC("c4");
			KnowledgeObject d1 = newD("d1");
			KnowledgeObject d2 = newD("d2");
			KnowledgeObject d3 = newD("d3");
			KnowledgeObject d4 = newD("d4");
			setA2(b1, "a2");
			setA2(c1, "a2");
			setA2(d1, "a2");
			setA2(b2, "not a2");
			setA2(c2, "not a2");
			setA2(d2, "not a2");
			b3.setAttributeValue(B2_NAME, "b2");
			c3.setAttributeValue(B2_NAME, "b2");
			b4.setAttributeValue(B2_NAME, "not b2");
			c4.setAttributeValue(B2_NAME, "not b2");
			{
				KnowledgeAssociation ab = newAB(b1, b4);
				ab.setAttributeValue(AB1_NAME, "ab1");
			}
			{
				KnowledgeAssociation ab = newAB(b1, c4);
				ab.setAttributeValue(AB1_NAME, "ab1");
			}
			{
				KnowledgeAssociation ab = newAB(b1, d4);
				ab.setAttributeValue(AB1_NAME, "ab1");
			}
			{
				KnowledgeAssociation ab = newAB(b2, b3);
				ab.setAttributeValue(AB1_NAME, "ab1");
			}
			{
				KnowledgeAssociation ab = newAB(b2, c3);
				ab.setAttributeValue(AB1_NAME, "ab1");
			}
			{
				KnowledgeAssociation ab = newAB(b2, d3);
				ab.setAttributeValue(AB1_NAME, "ab1");
			}
			{
				KnowledgeAssociation ab = newAB(b1, b3);
				ab.setAttributeValue(AB1_NAME, "not ab1");
			}
			{
				KnowledgeAssociation ab = newAB(b1, c3);
				ab.setAttributeValue(AB1_NAME, "not ab1");
			}
			{
				KnowledgeAssociation ab = newAB(b1, d3);
				ab.setAttributeValue(AB1_NAME, "not ab1");
			}
			
			
			// Create other objects and associations.
			commit(tx);
			r1 = tx.getCommitRevision().getCommitNumber();
		}
		
		// Test search in current trunk.
		assertEquals(set(db1, db2, db3, dc1, dc2, dc3), toSet(kb().search(queryUnresolved(expr))));
		
		long r2;
		{
			Transaction tx = begin();
			setA2(sb1, "not a2");
			commit(tx);
			r2 = tx.getCommitRevision().getCommitNumber();
		}
		
		assertEquals(set(db3, dc1, dc2), toSet(searchCurrentTrunk(expr)));
		assertEquals(inRev(r1, set(db1, db2, db3, dc1, dc2, dc3)),
			toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedRevision(r1))));
		
		Branch branch1 = kb().createBranch(kb().getTrunk(), kb().getRevision(r2), null);
		long r2a = getLastRevision();
		
		assertEquals(onBranch(branch1, set(db3, dc1, dc2)),
			toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedBranch(branch1))));
		
		long r3;
		{
			Transaction tx = begin();
			c1c1.setAttributeValue(AB1_NAME, "not ab1");
			commit(tx);
			r3 = tx.getCommitRevision().getCommitNumber();
		}
		
		assertEquals(set(db3, dc2), toSet(searchCurrentTrunk(expr)));
		assertEquals(inRev(r1, set(db1, db2, db3, dc1, dc2, dc3)),
			toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedRevision(r1))));
		assertEquals(inRev(r2, set(db3, dc1, dc2)),
			toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedRevision(r2))));
		assertEquals(onBranch(branch1, set(db3, dc1, dc2)),
			toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedBranch(branch1))));
		
		long r4;
		{
			Transaction tx = begin();
			dc2.setAttributeValue(B2_NAME, "not b2");
			commit(tx);
			r4 = tx.getCommitRevision().getCommitNumber();
		}
		
		assertEquals(set(db3), toSet(searchCurrentTrunk(expr)));
		assertEquals(inRev(r1, set(db1, db2, db3, dc1, dc2, dc3)),
			toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedRevision(r1))));
		assertEquals(inRev(r2, set(db3, dc1, dc2)),
			toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedRevision(r2))));
		assertEquals(inRev(r3, set(db3, dc2)), toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedRevision(r3))));
		assertEquals(onBranch(branch1, set(db3, dc1, dc2)),
			toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedBranch(branch1))));
		
		{
			Transaction tx = begin();
			d1b3.delete();
			commit(tx);
		}

		assertEquals(set(), toSet(searchCurrentTrunk(expr)));
		assertEquals(inRev(r1, set(db1, db2, db3, dc1, dc2, dc3)),
			toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedRevision(r1))));
		assertEquals(inRev(r2, set(db3, dc1, dc2)),
			toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedRevision(r2))));
		assertEquals(inRev(r3, set(db3, dc2)), toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedRevision(r3))));
		assertEquals(inRev(r4, set(db3)), toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedRevision(r4))));
		assertEquals(onBranch(branch1, set(db3, dc1, dc2)),
			toSet(kb().search(queryUnresolved(expr), revisionArgs().setRequestedBranch(branch1))));

		Map<Object, List<LongRange>> expected = new HashMap<>();
		period(expected, db1, r1, r1); 
		period(expected, db2, r1, r1); 
		period(expected, db3, r1, r4);
		period(expected, dc1, r1, r2a);
		period(expected, dc2, r1, r3);
		period(expected, dc3, r1, r1);
		
		Map<?, List<LongRange>> result = kb().search(historyQuery(expr));
		assertEquals(expected, result);
		
		assertHistorySearch(historyQuery(expr), historyArgs());
		assertHistorySearch(historyQuery(expr), historyArgs().setRequestedBranch(branch1));
		assertHistorySearch(historyQuery(BranchParam.all, RevisionParam.all, RangeParam.complete, Collections.<ParameterDeclaration>emptyList(), expr), historyArgs());
	}
	
	public void testPolymorhic() throws DataObjectException {
		KnowledgeObject b1;
		KnowledgeObject b2;
		KnowledgeObject b3;
		KnowledgeObject d1;
		KnowledgeObject d2;
		KnowledgeObject d3;
		{
			Transaction tx = begin();
			b1 = newB("b1");
			b2 = newB("b2");
			b3 = newB("b3");
			d1 = newD("d1");
			d2 = newD("d2");
			d3 = newD("d3");
			
			
			newAB(b1, d1);
			newAB(b2, d2);
			commit(tx);
		}
		
		SetExpression expression = map(anyOf(AB_NAME), destination());

		List<KnowledgeObject> result = searchCurrentTrunk(expression);
		assertEquals(set(d1, d2), toSet(result));
	}
	
	public void testPolymorhicInAccess() throws DataObjectException {
		SetExpression expression = filter(
			anyOf(D_NAME),
			eval(
				attribute(D_NAME, D1_NAME),
				inSet(map(
						filter(
							map(anyOf(AB_NAME), destination()),
							instanceOf(B_NAME)),
						attribute(B_NAME, B1_NAME)))));
		List<KnowledgeObject> result = searchCurrentTrunk(expression);
		assertEquals(set(), toSet(result));
	}
	
	public void testSearchOnDifferentBranch() throws DataObjectException {
		if (noMultipleBranches()) {
			// This test uses branches, but there is no multiple branch support.
			return;
		}
		String attr = REFERENCE_MONO_CUR_GLOBAL_NAME;
		Transaction createRefereeTx = begin();
		KnowledgeObject referee = newE("e1");
		commit(createRefereeTx);

		KnowledgeObject reference;
		Branch b1 = kb().createBranch(trunk(), lastRevision(), null);
		Branch oldCtxBranch = HistoryUtils.setContextBranch(b1);
		try {
			Transaction createReferenceTX = begin();
			reference = newD("d1");
			referee.setAttributeValue(attr, reference);
			commit(createReferenceTX);

		} finally {
			HistoryUtils.setContextBranch(oldCtxBranch);
		}
		Expression filter = eqBinary(attribute(reference(E_NAME, attr), A_NAME, A1_NAME), literal("d1"));
		assertEquals(list(referee), kb().search(queryUnresolved(filter(allOf(E_NAME), filter))));
	}

	public void testAttributeRange() throws DataObjectException {
		final KnowledgeObject b1;
		final KnowledgeObject b2;
		final KnowledgeObject b3;
		final KnowledgeObject b4;
		final KnowledgeObject b5;
		{
			Transaction tx = begin();
			b1 = newB("d1");
			setB1(b1, "b1");
			b2 = newB("d2");
			setB1(b2, "b17");
			b3 = newB("d3");
			setB1(b3, "b14");
			b4 = newB("d4");
			setB1(b4, "b19");
			b5 = newB("d5");
			setB1(b5, "adf");
			commit(tx);
		}

		final Expression range1 = ExpressionFactory.attributeRange(B_NAME, B1_NAME, "b1", "b17");
		final SetExpression expression1 = filter(anyOf(B_NAME), range1);
		final Set<KnowledgeObject> result1 = toSet(searchCurrentTrunk(expression1));
		assertEquals(set(b1, b3), result1);
		
		final Expression range2 = ExpressionFactory.attributeRange(B_NAME, B1_NAME, "b1", "b1");
		final SetExpression expression2 = filter(anyOf(B_NAME), range2);
		final Set<KnowledgeObject> result2 = toSet(searchCurrentTrunk(expression2));
		assertEquals(set(), result2);

		final Expression range3 = ExpressionFactory.attributeRange(B_NAME, B1_NAME, "b17", "b1");
		final SetExpression expression3 = filter(anyOf(B_NAME), range3);
		final Set<KnowledgeObject> result3 = toSet(searchCurrentTrunk(expression3));
		assertEquals(set(), result3);
	}

	/**
	 * Tests query in which parameter and constants occur.
	 * 
	 * Shows problem when creating SQL an {@link IndexOutOfBoundsException} occur when literal is
	 * evaluated after constant.
	 */
	public void testCompiledStatementConstantsAndParameters() throws DataObjectException {
		Transaction tx = begin();
		KnowledgeObject b1 = newB("XXX");
		KnowledgeObject b2 = newB("XXX");
		KnowledgeObject b3 = newB("YYY");
		b1.setAttributeValue(A2_NAME, "YYY");
		b2.setAttributeValue(A2_NAME, "XXX");
		b3.setAttributeValue(A2_NAME, "XXX");
		commit(tx);

		SetExpression allB = allOf(B_NAME);
		Expression constantEquality = eqBinary(attribute(A_NAME, A1_NAME), literal("XXX"));
		Expression parameterEquality = eqBinary(attribute(A_NAME, A2_NAME), param("param"));
		List<ParameterDeclaration> parameter = Collections.singletonList(paramDecl(MOPrimitive.STRING, "param"));
		// order is important: see comment
		Expression where = and(parameterEquality, constantEquality);
		RevisionQuery<KnowledgeObject> query =
			queryUnresolved(BranchParam.single, RangeParam.complete, parameter, filter(allB, where), NO_ORDER);
		List<KnowledgeObject> result = kb().search(query, revisionArgs().setArguments("XXX"));

		assertEquals(list(b2), result);
		
	}
	
	/**
	 * Tests that misconstructed queries are detected.
	 */
	public void testInvalidQueryDefinition() {
		// reference with non reference attribute
		RevisionQuery<BObjExtended> q1 = queryResolved(
			filter(allOf(B_NAME), eqBinary(reference(A_NAME, A1_NAME), literal("XXX"))), BObjExtended.class);
		try {
			CompiledQuery<BObjExtended> compileQuery = kb().compileQuery(q1);
			assertNull(compileQuery);
			fail("Attribute " + A1_NAME + " is not a reference.");
		} catch (RuntimeException ex) {
			if (!Pattern.compile("Attribute '[^']+' is not a MOReference").matcher(ex.getMessage()).find()) {
				throw ex;
			}
			// expected
		}

		// attribute with reference attribute
		RevisionQuery<DObj> q2 = queryResolved(
			filter(allOf(D_NAME), isNull(attribute(D_NAME, REFERENCE_MONO_HIST_GLOBAL_NAME))), DObj.class);
		try {
			CompiledQuery<DObj> compileQuery = kb().compileQuery(q2);
			assertNull(compileQuery);
			fail("Attribute " + REFERENCE_MONO_HIST_GLOBAL_NAME + " is a reference.");
		} catch (RuntimeException ex) {
			if (!Pattern.compile("Attribute '[^']+' is a MOReference").matcher(ex.getMessage()).find()) {
				throw ex;
			}
			// expected
		}
	}

	public void testRandomScenario() throws DataObjectException {
		SetExpression expr1 = filter(
			navigateForwards(
				filter(
					anyOf(A_NAME),
					attributeEqBinary(A_NAME, A2_NAME, VALUE_A2_1)),
				filter(
					anyOf(AB_NAME),
					attributeEqBinary(AB_NAME, AB1_NAME, VALUE_AB1_1)),
				B_NAME),
			eqBinary(attribute(B_NAME, B2_NAME), literal(VALUE_B2_1)));
		
		SetExpression expr2 = filter(
			anyOf(B_NAME),
			or(
				attributeEqBinary(A_NAME, A2_NAME, VALUE_A2_1),
				attributeEqBinary(B_NAME, B2_NAME, VALUE_B2_1)));
		
		Random rnd = new Random(42);
		
		createRandomScenario(rnd, 5000);
		
		assertHistorySearch(historyQuery(expr1), historyArgs());
		assertHistorySearch(historyQuery(expr2), historyArgs());
	}
	
	private enum Operation {
		createObject, createAssociation, updateObject, updateAssociation, deleteObject, deleteAssociation, commit;
	}
	
	public void createRandomScenario(Random rnd, int operationCnt) throws DataObjectException {
		Transaction tx = begin();
		
		List<String> a2Values = Arrays.asList(new String[] {VALUE_A2_1, VALUE_A2_2});
		List<String> b2Values = Arrays.asList(new String[] {VALUE_B2_1, VALUE_B2_2});
		List<String> d2Values = Arrays.asList(new String[] {VALUE_D2_1, VALUE_D2_2});
		List<String> ab1Values = Arrays.asList(new String[] {VALUE_AB1_1, VALUE_AB1_2});
		List<String> bc1Values = Arrays.asList(new String[] {VALUE_BC1_1, VALUE_BC1_2});
		
		List<KnowledgeObject> objects = new ArrayList<>();
		List<KnowledgeAssociation> associations = new ArrayList<>();
		
		Operation[] operations = {
			Operation.createObject,
			Operation.createObject,
			Operation.createObject,
			Operation.createAssociation,
			Operation.createAssociation,
			Operation.updateObject,
			Operation.updateObject,
			Operation.updateObject,
			Operation.updateObject,
			Operation.updateObject,
			Operation.updateObject,
			Operation.updateObject,
			Operation.updateAssociation,
			Operation.updateAssociation,
			Operation.updateAssociation,
			Operation.updateAssociation,
			Operation.updateAssociation,
			Operation.updateAssociation,
			Operation.updateAssociation,
			Operation.deleteObject,
			Operation.deleteAssociation,
			Operation.commit,
			Operation.commit,
			Operation.commit,
			Operation.commit,
			Operation.commit,
			Operation.commit,
		};
		
		for (int n = 0; n < operationCnt; n++) {
			int opIndex = rnd.nextInt(operations.length);
			switch (operations[opIndex]) {
			case createObject: {
				int objectCnt = objects.size();
				int objectNo = objectCnt;
				KnowledgeObject obj;
				switch (rnd.nextInt(3)) {
				case 0: {
					obj = newB("b" + objectNo);
					break;
				}
				case 1: {
					obj = newC("c" + objectNo);
					break;
				}
				case 2: {
					obj = newD("d" + objectNo);
					break;
				}
				default:
					throw new UnreachableAssertion("All cases covered.");
				}
				objects.add(obj);
				break;
			}
			
			case createAssociation: {
				int objectCnt = objects.size();
				if (objectCnt == 0) {
					break;
				}
				KnowledgeObject obj1 = objects.get(rnd.nextInt(objectCnt));
				KnowledgeObject obj2 = objects.get(rnd.nextInt(objectCnt));
				switch (rnd.nextInt(2)) {
				case 0: {
					KnowledgeAssociation ab = newAB(obj1, obj2);
					associations.add(ab);
					break;
				}
				case 1: {
					KnowledgeAssociation ab = newBC(obj1, obj2);
					associations.add(ab);
					break;
				}
				default:
					throw new UnreachableAssertion("All cases covered.");
				}
				break;
			}

			case updateObject: {
				int objectCnt = objects.size();
				if (objectCnt == 0) {
					break;
				}
				KnowledgeObject obj = objects.get(rnd.nextInt(objectCnt));
				switch (rnd.nextInt(2)) {
				case 0: {
					setA2(obj, a2Values.get(rnd.nextInt(a2Values.size())));
					break;
				}
				case 1: {
							if (obj.tTable().isSubtypeOf(B_NAME)) {
								obj.setAttributeValue(B2_NAME, b2Values.get(rnd.nextInt(b2Values.size())));
					} else {
								obj.setAttributeValue(D2_NAME, d2Values.get(rnd.nextInt(d2Values.size())));
					}
					break;
				}
				}
				break;
			}
			
			case updateAssociation: {
				int associationCnt = associations.size();
				if (associationCnt == 0) {
					break;
				}
				KnowledgeAssociation association = associations.get(rnd.nextInt(associationCnt));
					if (association.tTable().getName().equals(AB_NAME)) {
						association.setAttributeValue(AB1_NAME, ab1Values.get(rnd.nextInt(ab1Values.size())));
				} else {
					setBC1(association, bc1Values.get(rnd.nextInt(bc1Values.size())));
				}
				break;
			}

			case deleteObject: {
				int objectCnt = objects.size();
				if (objectCnt == 0) {
					break;
				}
				KnowledgeObject obj = objects.remove(rnd.nextInt(objectCnt));
				associations.removeAll(toList(obj.getOutgoingAssociations()));
				associations.removeAll(toList(obj.getIncomingAssociations()));
				
				obj.delete();
				break;
			}
			
			case deleteAssociation: {
				int associationCnt = associations.size();
				if (associationCnt == 0) {
					break;
				}
				KnowledgeAssociation association = associations.remove(rnd.nextInt(associationCnt));
				association.delete();
				break;
			}

			case commit: {
				commit(tx);
				tx = begin();
				break;
			}
			
			default:
				throw new UnreachableAssertion("All cases covered.");
			}
		}
		
		commit(tx);
	}
	
	
	private void assertHistorySearch(HistoryQuery historyQuery, HistoryQueryArguments historyArgs) {
		AbstractHistoryQueryTest.assertHistorySearch(kb(), historyQuery, historyArgs);
	}
	
	private long getLastRevision() {
		return kb().getLastRevision();
	}

	private void period(Map<Object, List<LongRange>> expected, KnowledgeObject item, long revMin, long revMax) {
		ObjectBranchId itemId = getObjectID(item);
		List<LongRange> range = range(revMin, revMax);
		List<LongRange> clash = expected.put(itemId, range);
		if (clash != null) {
			expected.put(itemId, LongRangeSet.union(clash, range));
		}
	}

	private Set<KnowledgeItem> onBranch(Branch branch, Set<? extends KnowledgeItem> set) throws DataObjectException {
		HashSet<KnowledgeItem> result = new HashSet<>();
		for (KnowledgeItem item : set) {
			result.add(onBranch(branch, item));
		}
		return result;
	}

	private Set<KnowledgeItem> inRev(long rev, Set<? extends KnowledgeItem> set) throws DataObjectException {
		HashSet<KnowledgeItem> result = new HashSet<>();
		for (KnowledgeItem item : set) {
			result.add(inRev(rev, item));
		}
		return result;
	}

	private KnowledgeItem inRev(long rev, KnowledgeItem item) throws DataObjectException {
		return HistoryUtils.getKnowledgeItem(kb().getRevision(rev), item);
	}

	private KnowledgeItem inRev(Transaction tx, KnowledgeItem item) throws DataObjectException {
		assertSame("Must only get items for committed transactions.", Transaction.STATE_COMMITTED, tx.getState());
		return HistoryUtils.getKnowledgeItem(tx.getCommitRevision(), item);
	}

	private List<KnowledgeObject> searchCurrentTrunk(SetExpression expr) {
		return kb().search(queryUnresolved(expr));
	}

	/**
	 * Suite of test case.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		if (false) {
			return suite(TestQueryLanguage.class, new SingleTestFactory("testLookupItemsInSet"));
//			return runOneTest(TestQueryLanguage.class, "testLookupItemsInSet", DBType.H2_DB);
		}
		return suite(TestQueryLanguage.class);
    }

}
