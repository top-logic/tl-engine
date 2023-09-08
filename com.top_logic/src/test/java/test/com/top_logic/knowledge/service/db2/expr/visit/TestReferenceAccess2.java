/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr.visit;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.ConfiguredScenario;
import test.com.top_logic.knowledge.service.db2.DBKnowledgeBaseTestSetup;

import com.top_logic.dob.DataObjectException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test for using reference attributes in {@link KnowledgeBase} queries.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestReferenceAccess2 extends AbstractDBKnowledgeBaseTest {

	@Override
	protected LocalTestSetup createSetup(Test self) {
		return new DBKnowledgeBaseTestSetup(self, new ConfiguredScenario("webinf://kbase/TestReferenceAccess2.xml"));
	}

	/**
	 * Similar to {@link TestReferenceAccess#testMonomorphicReferenceAttribute()} but using
	 * different types for the base object and the referenced object.
	 */
	public void testNavigate() throws DataObjectException {
		Transaction tx = kb().beginTransaction();
		KnowledgeObject a1 = newA("a", newB("x"));
		KnowledgeObject a2 = newA("a", newB("y"));
		KnowledgeObject a3 = newA("b", newB("x"));

		assertNotNull(a1);
		assertNotNull(a2);
		assertNotNull(a3);

		tx.commit();

		List<KnowledgeObject> result = kb().search(queryUnresolved(
			filter(anyOf("A"),
				and(
					eqBinary(
						attribute("A", "foo"),
						literal("a")),
					eqBinary(
						attribute(
							reference("A", "b"),
							"B", "bar"),
						literal("x"))))));

		assertEquals(set(a1), toSet(result));
	}

	private KnowledgeObject newA(String foo, KnowledgeObject b) throws DataObjectException {
		KnowledgeObject result = kb().createKnowledgeObject("A");
		result.setAttributeValue("foo", foo);
		result.setAttributeValue("b", b);
		return result;
	}

	@Override
	protected KnowledgeObject newB(String bar) throws DataObjectException {
		KnowledgeObject result = kb().createKnowledgeObject("B");
		result.setAttributeValue("bar", bar);
		return result;
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suite(TestReferenceAccess2.class);
	}
}
