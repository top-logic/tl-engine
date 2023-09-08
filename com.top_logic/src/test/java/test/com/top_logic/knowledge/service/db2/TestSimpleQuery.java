/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static com.top_logic.knowledge.service.db2.SimpleQuery.queryResolved;
import static com.top_logic.knowledge.service.db2.SimpleQuery.queryUnresolved;

import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.CompiledQuery;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.SimpleQuery;

/**
 * {@link AbstractDBKnowledgeBaseTest} tests the {@link SimpleQuery}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestSimpleQuery extends AbstractDBKnowledgeBaseTest {

	public void testSearchReferencesOfCertainType() throws DataObjectException {
		Expression search = inSet(reference(E_NAME, REFERENCE_POLY_CUR_GLOBAL_NAME), allOf(E_NAME));
		SimpleQuery<KnowledgeObject> simpleAttributeMatch = queryUnresolved(type(E_NAME), search);

		CompiledQuery<KnowledgeObject> compiledQuery = kb().compileSimpleQuery(simpleAttributeMatch);

		Transaction tx = kb().beginTransaction();
		RevisionQueryArguments revisionArgs = revisionArgs();
		KnowledgeObject e1 = newE("notFoundReferenceNotSet");
		{
			List<KnowledgeObject> result = compiledQuery.search(revisionArgs);
			assertEquals(set(), toSet(result));
		}
		e1.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, newE("reference"));
		{
			List<KnowledgeObject> result = compiledQuery.search(revisionArgs);
			assertEquals(set(e1), toSet(result));
		}

		KnowledgeObject e2 = newE("notFoundReferenceNotSet");
		KnowledgeObject e6 = newE("notFoundReferenceWrongType");
		e6.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, newD("reference"));
		KnowledgeObject e3 = newE("found");
		e3.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, newE("reference"));
		tx.commit();

		{
			List<KnowledgeObject> result = compiledQuery.search(revisionArgs);
			Set<KnowledgeObject> searchResult = toSet(result);
			assertFalse("No reference set.", result.contains(e2));
			assertFalse("Reference has wrong type.", result.contains(e6));
			assertEquals(set(e1, e3), searchResult);
		}

		Transaction openTX = kb().beginTransaction();
		KnowledgeObject e4 = newE("found");
		e4.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, newE("reference"));
		KnowledgeObject e5 = newE("notFoundReferenceNotSet");
		KnowledgeObject e7 = newE("notFoundReferenceWrongType");
		e7.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, newD("reference"));
		e3.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, null);
		e1.delete();
		{
			List<KnowledgeObject> resultInTX = compiledQuery.search(revisionArgs);
			Set<KnowledgeObject> result = toSet(resultInTX);
			assertFalse("No reference set.", result.contains(e5));
			assertFalse("Reference has wrong type.", result.contains(e7));
			assertFalse("Reference was removed.", result.contains(e3));
			assertFalse("object was deleted.", result.contains(e1));
			assertEquals(set(e4), result);
		}
		openTX.rollback();
	}

	public void testSearchReferencesInTransaction() throws DataObjectException {
		SimpleQuery<KnowledgeObject> simpleAttributeMatch =
			queryUnresolved(type(E_NAME), eqBinary(reference(E_NAME, REFERENCE_POLY_CUR_GLOBAL_NAME), param("A_Ref")));
		simpleAttributeMatch.setQueryParameters(params(paramDecl(A_NAME, "A_Ref")));

		CompiledQuery<KnowledgeObject> compiledQuery = kb().compileSimpleQuery(simpleAttributeMatch);


		ConnectionPool connectionPool = kb().getConnectionPool();
		Transaction tx = kb().beginTransaction();
		KnowledgeObject reference = newE("reference");
		RevisionQueryArguments revisionArgs = revisionArgs();
		revisionArgs.setArguments(reference);
		KnowledgeObject e1 = newE("notFoundReferenceNotSet");
		{
			List<KnowledgeObject> result = compiledQuery.search(revisionArgs);
			assertEquals(set(), toSet(result));
		}
		setA1(e1, "found");
		e1.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		{
			List<KnowledgeObject> result = compiledQuery.search(revisionArgs);
			assertEquals(set(e1), toSet(result));
		}

		KnowledgeObject e2 = newE("notFoundReferenceNotSet");
		KnowledgeObject e3 = newE("found");
		e3.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		tx.commit();

		{
			List<KnowledgeObject> result = compiledQuery.search(revisionArgs);
			assertEquals(set(e1, e3), toSet(result));
		}

		Transaction openTX = kb().beginTransaction();
		KnowledgeObject e4 = newE("found");
		e4.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, reference);
		KnowledgeObject e5 = newE("notFoundReferenceNotSet");
		setA1(e3, "notFoundReferenceRemoved");
		e3.setAttributeValue(REFERENCE_POLY_CUR_GLOBAL_NAME, null);
		{
			List<KnowledgeObject> resultInTX = compiledQuery.search(revisionArgs);
			assertEquals(set(e1, e4), toSet(resultInTX));
		}
		openTX.rollback();
	}

	public void testSearchInTransactionWithParameters() throws DataObjectException {
		SimpleQuery<KnowledgeObject> simpleAttributeMatch =
			queryUnresolved(type(E_NAME), eqBinary(attribute(E_NAME, A1_NAME), param("a1_name")));
		simpleAttributeMatch.setQueryParameters(params(paramDecl(MOPrimitive.STRING, "a1_name")));

		CompiledQuery<KnowledgeObject> compiledQuery = kb().compileSimpleQuery(simpleAttributeMatch);

		Transaction tx = kb().beginTransaction();
		KnowledgeObject e1 = newE("found");
		KnowledgeObject e2 = newE("notFound");
		KnowledgeObject e3 = newE("found");
		tx.commit();

		{
			RevisionQueryArguments revisionArgs = revisionArgs();
			revisionArgs.setArguments("found");
			List<KnowledgeObject> result = compiledQuery.search(revisionArgs);
			assertEquals(set(e1, e3), toSet(result));
		}
		{
			RevisionQueryArguments revisionArgs = revisionArgs();
			revisionArgs.setArguments("notFound");
			List<KnowledgeObject> result = compiledQuery.search(revisionArgs);
			assertEquals(set(e2), toSet(result));
		}

		Transaction tx2 = kb().beginTransaction();
		KnowledgeObject e4 = newE("found");
		KnowledgeObject e5 = newE("notFound");
		{
			RevisionQueryArguments revisionArgs = revisionArgs();
			revisionArgs.setArguments("found");
			List<KnowledgeObject> resultInTX = compiledQuery.search(revisionArgs);
			assertEquals(set(e1, e3, e4), toSet(resultInTX));
		}
		{
			RevisionQueryArguments revisionArgs = revisionArgs();
			revisionArgs.setArguments("notFound");
			List<KnowledgeObject> resultInTX = compiledQuery.search(revisionArgs);
			assertEquals(set(e2, e5), toSet(resultInTX));
		}
		tx2.rollback();
	}

	public void testSearchInTransaction() throws DataObjectException {
		SimpleQuery<KnowledgeObject> simpleAttributeMatch =
			queryUnresolved(type(E_NAME), eqBinary(attribute(E_NAME, A1_NAME), literal("found")));
		_searchInTransaction(kb().compileSimpleQuery(simpleAttributeMatch), false);
	}

	public void testSearchInTransactionResolved() throws DataObjectException {
		SimpleQuery<EObj> simpleAttributeMatch =
			queryResolved(EObj.class, type(E_NAME), eqBinary(attribute(E_NAME, A1_NAME), literal("found")));
		_searchInTransaction(kb().compileSimpleQuery(simpleAttributeMatch), true);
	}

	private <E> void _searchInTransaction(CompiledQuery<E> compiledQuery, boolean r) throws DataObjectException,
			KnowledgeBaseException {
		KnowledgeObject e1;
		KnowledgeObject e2;
		KnowledgeObject e3;
		try (Transaction tx = kb().beginTransaction()) {
			e1 = newE("found");
			e2 = newE("notFound");
			e3 = newE("found");
			tx.commit();
		}

		List<E> result = compiledQuery.search(revisionArgs());
		assertEquals(set(resolve(e1, r), resolve(e3, r)), toSet(result));

		try (Transaction tx2 = kb().beginTransaction()) {
			KnowledgeObject e4 = newE("found");
			KnowledgeObject e5 = newE("notFound");
			List<E> resultInTX = compiledQuery.search(revisionArgs());
			assertEquals(set(resolve(e1, r), resolve(e3, r), resolve(e4, r)), toSet(resultInTX));
		}

		List<E> resultAfterRollback = compiledQuery.search(revisionArgs());
		assertEquals(set(resolve(e1, r), resolve(e3, r)), toSet(resultAfterRollback));
	}

	private Object resolve(KnowledgeItem item, boolean resolve) {
		return resolve ? item.getWrapper() : item;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestSimpleQuery}.
	 */
	public static Test suite() {
		return suite(TestSimpleQuery.class);
	}

}
