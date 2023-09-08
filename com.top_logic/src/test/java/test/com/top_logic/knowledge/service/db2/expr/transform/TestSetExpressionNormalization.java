/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static test.com.top_logic.knowledge.service.db2.expr.QueryStructureTester.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioConstants;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.search.AllOf;
import com.top_logic.knowledge.search.Filter;
import com.top_logic.knowledge.search.MapTo;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.db2.expr.transform.SetExpressionNormalization;
import com.top_logic.knowledge.service.db2.expr.visit.ExpressionPrinter;

/**
 * Test case for {@link SetExpressionNormalization}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestSetExpressionNormalization extends TestCase implements KnowledgeBaseTestScenarioConstants {

	public TestSetExpressionNormalization(String name) {
		super(name);
	}
	
	public void testFilterNormalization() {
		SetExpression expr = 
			filter(
				filter(
					filter(allOf(B_NAME), eqBinary(attribute(A_NAME, A1_NAME), literal("a1"))),
					lt(attribute(A_NAME, A1_NAME), literal("bbb"))),
				eqBinary(attribute(B_NAME, B1_NAME), attribute(B_NAME, B2_NAME)));
		
		SetExpression result = normalize(expr);

		assertEquals(B_NAME, ((AllOf) ((Filter) result).getSource()).getTypeName());
	}
	
	public void testMapNormalization() {
		SetExpression expr = 
			map(
				map(
					map(
						allOf(AB_NAME), destination()),
					source()),
				source());
		
		SetExpression result = normalize(expr);

		assertEquals(AB_NAME, ((AllOf) ((MapTo) result).getSource()).getTypeName());
	}
	
	public void testFilterMapNormalization() {
		SetExpression expr = 
			filter(
				map(
					filter(
						allOf(AB_NAME), eqBinary(attribute(AB_NAME, AB1_NAME), literal("ab1"))),
					destination()),
				hasType(B_NAME));
		
		SetExpression result = normalize(expr);
	}
	
	public void testSetNormalization() {
		SetExpression expr = 
			intersection(
				substraction(intersection(intersection(allOf(B_NAME), allOf(C_NAME)), allOf(D_NAME)), allOf(X_NAME)),
				allOf(Y_NAME));
		
		SetExpression result = normalize(expr);
	}
	
	public void testAnyInsetNormalization() {
		SetExpression expr = filter(map(allOf(AB_NAME), destination()), inSet(anyOf(B_NAME)));
		
		SetExpression result = normalize(expr);
	}

	private SetExpression normalize(SetExpression expr) {
		SetExpression result = SetExpressionNormalization.normalizeSets(expr);
		
		Logger.debug("orig:   " + ExpressionPrinter.toString(expr), TestSetExpressionNormalization.class);
		Logger.debug("result: " + ExpressionPrinter.toString(result), TestSetExpressionNormalization.class);
		
		assertTree(result);
		return result;
	}
	
	/**
	 * Suite creation.
	 */
	public static Test suite() {
		Test innerTest = new TestSuite(TestSetExpressionNormalization.class);
		return ModuleLicenceTestSetup.setupModule(innerTest);
	}
	
}
