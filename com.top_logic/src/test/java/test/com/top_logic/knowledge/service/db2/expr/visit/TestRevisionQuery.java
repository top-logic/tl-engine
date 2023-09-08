/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr.visit;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseClusterTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test case for {@link RevisionQuery}.
 * 
 * @see TestQueryLanguage
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestRevisionQuery extends AbstractDBKnowledgeBaseClusterTest {

	public void testFlexAttributeQuery() {
		Transaction tx1 = kb().beginTransaction();
		BObj b0 = BObj.newBObj("b1");
		BObj b1 = BObj.newBObj("b1");
		BObj b2 = BObj.newBObj("b2");
		BObj b3 = BObj.newBObj("b3");
		BObj b4 = BObj.newBObj("b4");
		tx1.commit();
		
		Transaction tx3 = kb().beginTransaction();
		b1.setF1("value");
		b2.setF1("value");
		b4.setF1("value");
		tx3.commit();

		Transaction tx4 = kb().beginTransaction();
		b1.setF2("value");
		b3.setF2("value");
		b4.setF2("value");
		tx4.commit();

		Transaction tx5 = kb().beginTransaction();
		b4.setF1("othervalue");
		b4.setF2("othervalue");
		tx5.commit();

		List<KnowledgeObject> result1 =
			kb().search(
				queryUnresolved(filter(allOf(B_NAME), eqBinary(flex(MOPrimitive.STRING, BObj.F1_NAME), literal("value")))));

		assertEquals("Ticket #9042: Flex attribute seach failed.",
			set(b1.tHandle(), b2.tHandle()),
			toSet(result1));
	}

	/**
	 * Suite of test case.
	 */
	public static Test suite() {
		return suite(TestRevisionQuery.class);
	}

}
