/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr.visit;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.DObj;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test case for accessing {@link MOReference} attributes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestReferenceAccess extends AbstractDBKnowledgeBaseTest {

	/**
	 * Search items pointing to another item with a certain attribute value.
	 */
	public void testMonomorphicReferenceAttribute() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		DObj d1 = DObj.newDObj("d1");
		d1.setMonoCurLocal(newDObj("dd1", "foo"));

		DObj d2 = DObj.newDObj("d2");
		d2.setMonoCurLocal(newDObj("dd2", "bar"));

		DObj d3 = DObj.newDObj("d3");
		d3.setMonoCurLocal(newDObj("dd3", "foo"));

		DObj d4 = DObj.newDObj("d4");
		d4.setMonoCurLocal(newDObj("dd3", "bar"));
		tx.commit();

		try {
			List<DObj> result = kb().search(queryResolved(
				filter(anyOf(D_NAME),
					eqBinary(
						attribute(
							reference(D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME),
							D_NAME, D1_NAME),
						literal("bar"))), DObj.class));

			assertEquals(set(d2, d4), toSet(result));
		} catch (AbortExecutionException ex) {
			fail("Ticket #18791: Access to attribute of reference failed.", ex);
		}
	}

	/**
	 * Search items pointing to another item with a certain attribute value.
	 */
	public void testPolymorphicReferenceAttribute() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		EObj e1 = EObj.newEObj("e1");
		e1.setPolyCurLocal(newD("d1", "foo"));

		EObj e2 = EObj.newEObj("e2");
		e2.setPolyCurLocal(newE("d2", "foo"));

		EObj e3 = EObj.newEObj("e3");
		e3.setPolyCurLocal(newD("d3", "bar"));

		EObj e4 = EObj.newEObj("e4");
		e4.setPolyCurLocal(newE("d4", "bar"));
		tx.commit();

		try {
			List<EObj> result = kb().search(queryResolved(
				filter(anyOf(E_NAME),
					eqBinary(
						attribute(
							reference(E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME),
							D_NAME, D1_NAME),
						literal("bar"))), EObj.class));

			assertEquals(set(e3, e4), toSet(result));
		} catch (AbortExecutionException ex) {
			/* Exptected due to the known bug in ticket #18791: Access to attribute of reference
			 * failed. */
			return;
		}
		fail("Test should fail due to the known bug in ticket #18791: Access to attribute of reference failed.");
	}

	/**
	 * Alternative formulation of {@link #testPolymorphicReferenceAttribute()}.
	 */
	public void testPolymorphicReferenceAttributeAlternative() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		EObj e1 = EObj.newEObj("e1");
		e1.setPolyCurLocal(newD("d1", "foo"));

		EObj e2 = EObj.newEObj("e2");
		e2.setPolyCurLocal(newE("d2", "foo"));

		EObj e3 = EObj.newEObj("e3");
		e3.setPolyCurLocal(newD("d3", "bar"));

		EObj e4 = EObj.newEObj("e4");
		e4.setPolyCurLocal(newE("d4", "bar"));
		tx.commit();

		try {
			List<EObj> result = kb().search(queryResolved(
				filter(anyOf(E_NAME),
					eqBinary(
						eval(
							reference(E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME),
							attribute(D_NAME, D1_NAME)),
						literal("bar"))), EObj.class));

			assertEquals(set(e3, e4), toSet(result));
		} catch (AbortExecutionException ex) {
			/* Exptected due to the known bug in ticket #18791: Access to attribute of reference
			 * failed. */
			return;
		}
		fail("Test should fail due to the known bug in ticket #18791: Access to attribute of reference failed.");
	}

	/**
	 * Search items pointing to an item pointing to another item with a certain attribute value.
	 */
	public void testMonomorphicReferenceOfReferenceAttribute() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		DObj d1 =
			DObj.newDObj("d1").setMonoCurLocal(newDObj("d1.d", "foo").setMonoCurLocal(newDObj("d1.d.d", "matched")));

		DObj d2 =
			DObj.newDObj("d2").setMonoCurLocal(newDObj("d2.d", "bar").setMonoCurLocal(newDObj("d2.d.d", "unmatched")));

		DObj d3 = DObj.newDObj("d3").setMonoCurLocal(newDObj("d3.d", "foo"));

		DObj d4 = DObj.newDObj("d4");
		tx.commit();

		try {
			List<DObj> result = kb().search(queryResolved(
				filter(anyOf(D_NAME),
					eqBinary(
						attribute(
							reference(
								reference(
									D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME),
								D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME),
							D_NAME, D1_NAME),
						literal("matched"))), DObj.class));

			assertEquals(set(d1), toSet(result));
		} catch (AbortExecutionException ex) {
			fail("Ticket #18791: Access to attribute of reference failed.", ex);
		}

		try {
			List<DObj> result = kb().search(queryResolved(
				filter(anyOf(D_NAME),
					not(
					eqBinary(
						attribute(
							reference(
								reference(
									D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME),
								D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME),
							D_NAME, D1_NAME),
						literal("matched")))), DObj.class));

			assertContainsNone(set(d1), toSet(result));
			assertContainsAll(set(d2, d3, d4), toSet(result));
		} catch (AbortExecutionException ex) {
			fail("Ticket #18791: Access to attribute of reference failed.", ex);
		}
	}

	/**
	 * Search items pointing to an item pointing to another item with a certain attribute value.
	 */
	public void testPolymorphicReferenceOfReferenceAttribute() throws DataObjectException {
		Transaction tx = kb().beginTransaction();

		DObj d1 =
			DObj.newDObj("d1").setPolyCurLocal(newDObj("d1.d", "foo").setPolyCurLocal(newDObj("d1.d.d", "matched")));
		DObj d2 =
			DObj.newDObj("d2").setPolyCurLocal(newDObj("d2.d", "foo").setPolyCurLocal(newDObj("d2.d.d", "unmatched")));

		DObj d3 =
			DObj.newDObj("d3").setPolyCurLocal(newEObj("d3.e", "foo").setPolyCurLocal(newDObj("d3.e.d", "matched")));
		DObj d4 =
			DObj.newDObj("d4").setPolyCurLocal(newEObj("d4.e", "foo").setPolyCurLocal(newDObj("d4.e.d", "unmatched")));
		
		DObj d5 =
			DObj.newDObj("d5").setPolyCurLocal(newDObj("d5.d", "foo").setPolyCurLocal(newEObj("d5.d.e", "matched")));
		DObj d6 =
			DObj.newDObj("d6").setPolyCurLocal(newDObj("d6.d", "foo").setPolyCurLocal(newEObj("d6.d.e", "unmatched")));
		
		DObj d7 =
			DObj.newDObj("d7").setPolyCurLocal(newEObj("d7.e", "foo").setPolyCurLocal(newEObj("d7.e.e", "matched")));
		DObj d8 =
			DObj.newDObj("d8").setPolyCurLocal(newEObj("d8.e", "foo").setPolyCurLocal(newEObj("d8.e.e", "unmatched")));

		DObj d9 = DObj.newDObj("d9").setPolyCurLocal(newDObj("d9.d", "foo"));
		DObj d10 = DObj.newDObj("d10").setPolyCurLocal(newEObj("d10.e", "foo"));
		DObj d11 = DObj.newDObj("d11");

		DObj e1 =
			DObj.newDObj("e1").setPolyCurLocal(newDObj("e1.d", "foo").setPolyCurLocal(newDObj("e1.d.d", "matched")));
		DObj e2 =
			DObj.newDObj("e2").setPolyCurLocal(newDObj("e2.d", "foo").setPolyCurLocal(newDObj("e2.d.d", "unmatched")));

		DObj e3 =
			DObj.newDObj("e3").setPolyCurLocal(newEObj("e3.e", "foo").setPolyCurLocal(newDObj("e3.e.d", "matched")));
		DObj e4 =
			DObj.newDObj("e4").setPolyCurLocal(newEObj("e4.e", "foo").setPolyCurLocal(newDObj("e4.e.d", "unmatched")));

		DObj e5 =
			DObj.newDObj("e5").setPolyCurLocal(newDObj("e5.d", "foo").setPolyCurLocal(newEObj("e5.d.e", "matched")));
		DObj e6 =
			DObj.newDObj("e6").setPolyCurLocal(newDObj("e6.d", "foo").setPolyCurLocal(newEObj("e6.d.e", "unmatched")));

		DObj e7 =
			DObj.newDObj("e7").setPolyCurLocal(newEObj("e7.e", "foo").setPolyCurLocal(newEObj("e7.e.e", "matched")));
		DObj e8 =
			DObj.newDObj("e8").setPolyCurLocal(newEObj("e8.e", "foo").setPolyCurLocal(newEObj("e8.e.e", "unmatched")));

		DObj e9 = DObj.newDObj("e9").setPolyCurLocal(newDObj("e9.d", "foo"));
		DObj e10 = DObj.newDObj("e10").setPolyCurLocal(newEObj("e10.e", "foo"));
		DObj e11 = DObj.newDObj("e11");
		tx.commit();

		try {
			List<EObj> result = kb().search(queryResolved(
				filter(anyOf(E_NAME),
					eqBinary(
						attribute(
							reference(
								reference(E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME),
								E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME),
							D_NAME, D1_NAME),
						literal("matched"))), EObj.class));

			assertEquals(set(d1, d3, d5, d7, e1, e3, e5, e7), toSet(result));
		} catch (AbortExecutionException ex) {
			/* Exptected due to the known bug in ticket #18791: Access to attribute of reference
			 * failed. */
			return;
		}

		try {
			List<EObj> result = kb().search(queryResolved(
				filter(anyOf(E_NAME),
					not(
					eqBinary(
						attribute(
							reference(
								reference(E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME),
								E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME),
							D_NAME, D1_NAME),
						literal("matched")))), EObj.class));

			assertContainsAll(set(d2, d4, d6, d8, d9, d10, d11, e2, e4, e6, e8, e9, e10, e11), toSet(result));
			assertContainsNone(set(d1, d3, d5, d7, e1, e3, e5, e7), toSet(result));
		} catch (AbortExecutionException ex) {
			/* Exptected due to the known bug in ticket #18791: Access to attribute of reference
			 * failed. */
			return;
		}
		fail("Test should fail due to the known bug in ticket #18791: Access to attribute of reference failed.");
	}

	/**
	 * Search items pointing to two other items with the same attribute value.
	 */
	public void testMonomorphicReferenceAttributeCompare() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		DObj d1 =
			DObj.newDObj("d1").setMonoCurLocal(newDObj("d1.d1", "foo")).setMonoCurGlobal(newDObj("d1.d2", "bar"));

		DObj d2 =
			DObj.newDObj("d2").setMonoCurLocal(newDObj("d2.d1", "foo")).setMonoCurGlobal(newDObj("d2.d2", "foo"));

		DObj d3 =
			DObj.newDObj("d3").setMonoCurLocal(newDObj("d3.d1", "bar")).setMonoCurGlobal(newDObj("d3.d2", "bar"));

		DObj d4 =
			DObj.newDObj("d4").setMonoCurLocal(newDObj("d4.d1", "bar"));

		DObj d5 =
			DObj.newDObj("d5").setMonoCurLocal(newDObj("d5.d1", null));

		DObj d6 =
			DObj.newDObj("d6").setMonoCurGlobal(newDObj("d6.d2", "bar"));

		DObj d7 =
			DObj.newDObj("d7");
		tx.commit();

		try {
			List<DObj> result = kb().search(queryResolved(
				filter(anyOf(D_NAME),
					and(
						and(
							not(isNull(reference(
								D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME))),
							not(isNull(reference(
								D_NAME, REFERENCE_MONO_CUR_GLOBAL_NAME)))),
						eqBinary(
							attribute(
								reference(
									D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME),
								D_NAME, D1_NAME),
							attribute(
								reference(
									D_NAME, REFERENCE_MONO_CUR_GLOBAL_NAME),
								D_NAME, D1_NAME)))), DObj.class));

			assertFalse("Attributes are not equal.", result.contains(d1));
			assertContainsNone("Not both references set.", set(d4, d6, d7), toSet(result));
			assertEquals(set(d2, d3), toSet(result));
		} catch (AbortExecutionException ex) {
			fail("Ticket #18791: Access to attribute of reference failed.", ex);
		}

		try {
			List<DObj> result = kb().search(queryResolved(
				filter(anyOf(D_NAME),
					not(and(
						and(
							not(isNull(reference(
								D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME))),
							not(isNull(reference(
								D_NAME, REFERENCE_MONO_CUR_GLOBAL_NAME)))),
						eqBinary(
						attribute(
							reference(
								D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME),
							D_NAME, D1_NAME),
						attribute(
							reference(
								D_NAME, REFERENCE_MONO_CUR_GLOBAL_NAME),
								D_NAME, D1_NAME))))), DObj.class));

			assertContainsNone(set(d2, d3), toSet(result));
			assertContainsAll(set(d1, d4, d5, d6, d7), toSet(result));
		} catch (AbortExecutionException ex) {
			fail("Ticket #18791: Access to attribute of reference failed.", ex);
		}
	}

	/**
	 * Search items pointing to two other items with the same attribute value.
	 */
	public void testPolymorphicReferenceAttributeCompare() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		DObj d1 =
			DObj.newDObj("d1").setPolyCurLocal(newDObj("d1.d1", "foo")).setPolyCurGlobal(newDObj("d1.d2", "bar"));

		DObj d1a =
			DObj.newDObj("d1a").setPolyCurLocal(newEObj("d1a.d1", "foo")).setPolyCurGlobal(newEObj("d1a.d2", "bar"));

		DObj d1b =
			DObj.newDObj("d1b").setPolyCurLocal(newEObj("d1b.d1", "foo")).setPolyCurGlobal(newDObj("d1b.d2", "bar"));

		DObj d2 =
			DObj.newDObj("d2").setPolyCurLocal(newDObj("d2.d1", "foo")).setPolyCurGlobal(newDObj("d2.d2", "foo"));

		DObj d2a =
			DObj.newDObj("d2a").setPolyCurLocal(newEObj("d2a.d1", "foo")).setPolyCurGlobal(newEObj("d2a.d2", "foo"));

		DObj d2b =
			DObj.newDObj("d2b").setPolyCurLocal(newEObj("d2b.d1", "foo")).setPolyCurGlobal(newDObj("d2b.d2", "foo"));

		DObj d3 =
			DObj.newDObj("d3").setPolyCurLocal(newDObj("d3.d1", "bar")).setPolyCurGlobal(newDObj("d3.d2", "bar"));

		DObj d4 =
			DObj.newDObj("d4").setPolyCurLocal(newDObj("d4.d1", "bar"));

		DObj d5 =
			DObj.newDObj("d5").setPolyCurLocal(newDObj("d5.d1", null));

		DObj d6 =
			DObj.newDObj("d6").setPolyCurGlobal(newDObj("d6.d2", "bar"));

		DObj d7 =
			DObj.newDObj("d7");
		tx.commit();

		try {
			List<DObj> result = kb().search(queryResolved(
				filter(anyOf(D_NAME),
					and(
						and(
							not(isNull(reference(
								D_NAME, REFERENCE_POLY_CUR_LOCAL_NAME))),
							not(isNull(reference(
								D_NAME, REFERENCE_POLY_CUR_GLOBAL_NAME)))),
						eqBinary(
							attribute(
								reference(
									D_NAME, REFERENCE_POLY_CUR_LOCAL_NAME),
								D_NAME, D1_NAME),
							attribute(
								reference(
									D_NAME, REFERENCE_POLY_CUR_GLOBAL_NAME),
								D_NAME, D1_NAME)))), DObj.class));

			assertContainsNone(set(d1, d1a, d1b, d4, d5, d6, d7), toSet(result));
			assertEquals(set(d2, d2a, d2b, d3), toSet(result));
		} catch (AbortExecutionException ex) {
			/* Exptected due to the known bug in ticket #18791: Access to attribute of reference
			 * failed. */
			return;
		}

		try {
			List<DObj> result = kb().search(queryResolved(
				filter(anyOf(D_NAME),
					not(and(
						and(
							not(isNull(reference(
								D_NAME, REFERENCE_POLY_CUR_LOCAL_NAME))),
							not(isNull(reference(
								D_NAME, REFERENCE_POLY_CUR_GLOBAL_NAME)))),
						eqBinary(
							attribute(
								reference(
									D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME),
								D_NAME, D1_NAME),
							attribute(
								reference(
									D_NAME, REFERENCE_MONO_CUR_GLOBAL_NAME),
								D_NAME, D1_NAME))))), DObj.class));

			assertContainsNone(set(d2, d2a, d2b, d3), toSet(result));
			assertContainsAll(set(d1, d1a, d1b, d4, d5, d6, d7), toSet(result));
		} catch (AbortExecutionException ex) {
			/* Exptected due to the known bug in ticket #18791: Access to attribute of reference
			 * failed. */
			return;
		}
		fail("Test should fail due to the known bug in ticket #18791: Access to attribute of reference failed.");
	}

	/**
	 * Search items pointing to another item with two attributes having the same value.
	 */
	public void testMonomorphicReferenceAttributesCompare() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		DObj d1 =
			newDObj("d1", "x").setMonoCurLocal(newDObj("d1.d1", "bar", "foo"));

		DObj d2 =
			newDObj("d2", "x").setMonoCurLocal(newDObj("d2.d1", "matched", "matched"));

		DObj d3 =
			newDObj("d3", "x").setMonoCurLocal(newDObj("d3.d1", "alsomatched", "alsomatched"));

		DObj d4 =
			newDObj("d4", "x").setMonoCurLocal(newDObj("d4.d1", null, null));

		DObj d5 =
			newDObj("d5", "x").setMonoCurLocal(newDObj("d5.d1", null, "foo"));

		DObj d6 =
			newDObj("d6", "x");
		tx.commit();

		try {
			List<DObj> result = kb().search(queryResolved(
				filter(anyOf(D_NAME),
					and(
						not(isNull(reference(
							D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME))),
						eqBinary(
							attribute(
								reference(
									D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME),
								D_NAME, D2_NAME),
							attribute(
								reference(
									D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME),
								D_NAME, D1_NAME)))), DObj.class));

			assertContainsNone(set(d1, d5, d6), toSet(result));
			assertEquals(set(d2, d3, d4), toSet(result));
		} catch (AbortExecutionException ex) {
			fail("Ticket #18791: Access to attribute of reference failed.", ex);
		}

		try {
			List<DObj> result = kb().search(queryResolved(
				filter(anyOf(D_NAME),
					not(and(
						not(isNull(reference(
							D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME))),
						eqBinary(
							attribute(
								reference(
									D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME),
								D_NAME, D2_NAME),
							attribute(
								reference(
									D_NAME, REFERENCE_MONO_CUR_LOCAL_NAME),
								D_NAME, D1_NAME))))), DObj.class));

			assertContainsNone(set(d2, d3, d4), toSet(result));
			assertContainsAll(set(d1, d5, d6), toSet(result));
		} catch (AbortExecutionException ex) {
			fail("Ticket #18791: Access to attribute of reference failed.", ex);
		}
	}

	/**
	 * Search items pointing to another item with two attributes having the same value.
	 */
	public void testPolymorphicReferenceAttributesCompare() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		DObj d1 =
			newDObj("d1", "x").setPolyCurLocal(newDObj("d1.d1", "bar", "foo"));

		DObj d1a =
			newDObj("d1", "x").setPolyCurLocal(newEObj("d1.d1", "bar", "foo"));

		DObj d2 =
			newDObj("d2", "x").setPolyCurLocal(newDObj("d2.d1", "matched", "matched"));

		DObj d2a =
			newDObj("d2", "x").setPolyCurLocal(newEObj("d2.d1", "matched", "matched"));

		DObj d3 =
			newDObj("d3", "x").setPolyCurLocal(newDObj("d3.d1", "alsomatched", "alsomatched"));

		DObj d4 =
			newDObj("d4", "x").setPolyCurLocal(newDObj("d4.d1", null, null));

		DObj d4a =
			newDObj("d4", "x").setPolyCurLocal(newEObj("d4.d1", null, null));

		DObj d5 =
			newDObj("d5", "x").setPolyCurLocal(newDObj("d5.d1", null, "foo"));

		DObj d6 =
			newDObj("d6", "x");
		tx.commit();

		try {
			List<DObj> result = kb().search(queryResolved(
				filter(anyOf(D_NAME),
					and(
						not(isNull(reference(
							D_NAME, REFERENCE_POLY_CUR_LOCAL_NAME))),
						eqBinary(
							attribute(
								reference(
									D_NAME, REFERENCE_POLY_CUR_LOCAL_NAME),
								D_NAME, D2_NAME),
							attribute(
								reference(
									D_NAME, REFERENCE_POLY_CUR_LOCAL_NAME),
								D_NAME, D1_NAME)))), DObj.class));

			assertContainsNone(set(d1, d1a, d5, d6), toSet(result));
			assertEquals(set(d2, d2a, d3, d4, d4a), toSet(result));
		} catch (AbortExecutionException ex) {
			/* Exptected due to the known bug in ticket #18791: Access to attribute of reference
			 * failed. */
			return;
		}

		try {
			List<DObj> result = kb().search(queryResolved(
				filter(anyOf(D_NAME),
					not(and(
						not(isNull(reference(
							D_NAME, REFERENCE_POLY_CUR_LOCAL_NAME))),
						eqBinary(
							attribute(
								reference(
									D_NAME, REFERENCE_POLY_CUR_LOCAL_NAME),
								D_NAME, D2_NAME),
							attribute(
								reference(
									D_NAME, REFERENCE_POLY_CUR_LOCAL_NAME),
								D_NAME, D1_NAME))))), DObj.class));

			assertContainsNone(set(d2, d2a, d3, d4, d4a), toSet(result));
			assertContainsAll(set(d1, d1a, d5, d6), toSet(result));
		} catch (AbortExecutionException ex) {
			/* Exptected due to the known bug in ticket #18791: Access to attribute of reference
			 * failed. */
			return;
		}
		fail("Test should fail due to the known bug in ticket #18791: Access to attribute of reference failed.");
	}

	private KnowledgeObject newD(String a1, String d1) throws DataObjectException {
		return setD1(newD(a1), d1);
	}

	private static DObj newDObj(String a1, String d1, String d2) {
		DObj result = newDObj(a1, d1);
		result.setD2(d2);
		return result;
	}

	private static DObj newDObj(String a1, String d1) {
		DObj result = DObj.newDObj(a1);
		result.setD1(d1);
		return result;
	}

	private static EObj newEObj(String a1, String d1, String d2) {
		EObj result = newEObj(a1, d1);
		result.setD2(d2);
		return result;
	}

	private static EObj newEObj(String a1, String d1) {
		EObj result = EObj.newEObj(a1);
		result.setD1(d1);
		return result;
	}

	private KnowledgeObject newE(String a1, String d1) throws DataObjectException {
		return setD1(newE(a1), d1);
	}

	private static KnowledgeObject setD1(KnowledgeObject result, String d1) throws DataObjectException {
		result.setAttributeValue(D1_NAME, d1);
		return result;
	}

	private static void assertContainsAll(Set<?> expectedSubset, Set<?> actual) {
		if (!actual.containsAll(expectedSubset)) {
			fail("Expected that '" + actual + "' contains all of '" + expectedSubset + "'.");
		}
	}

	private static void assertContainsNone(Set<?> expectedExcludes, Set<?> actual) {
		assertContainsNone(null, expectedExcludes, actual);
	}

	private static void assertContainsNone(String message, Set<?> expectedExcludes, Set<?> actual) {
		HashSet<Object> test = new HashSet<>(actual);
		test.retainAll(expectedExcludes);
		if (!test.isEmpty()) {
			String prefix = message == null ? "" : strip(message) + ": ";
			fail(prefix + "Unexpected elements '" + test + "' in '" + actual + "', must not contain any of '"
				+ expectedExcludes + "'.");
		}
	}

	private static String strip(String message) {
		if (message.endsWith(".")) {
			return message.substring(0, message.length() - 1);
		}
		return message;
	}

	public static Test suite() {
		return suite(TestReferenceAccess.class);
	}
}
