/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioConstants;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.db2.expr.transform.UnionExtraction;
import com.top_logic.knowledge.service.db2.expr.visit.ExpressionPrinter;

/**
 * Test case for {@link UnionExtraction}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestUnionExtraction extends TestCase implements KnowledgeBaseTestScenarioConstants {

	public TestUnionExtraction(String name) {
		super(name);
	}

	public void testTrafo() {
		SetExpression expr =
			filter(union(allOf(B_NAME), allOf(C_NAME)),
				eqBinary(attribute(A_NAME, A1_NAME), attribute(A_NAME, A2_NAME)));
		
		SetExpression result = extractUnions(expr);
	}
	
	public void testInSet() {
		SetExpression expr = filter(map(allOf(AB_NAME), destination()), inSet(union(allOf(B_NAME), allOf(C_NAME))));
		
		SetExpression result = extractUnions(expr);
	}

	public void testComplexInSet() {
		SetExpression expr =
			filter(
				map(allOf(AB_NAME), destination()),
				inSet(filter(union(allOf(B_NAME), allOf(C_NAME)),
					eqBinary(attribute(A_NAME, A1_NAME), attribute(A_NAME, A2_NAME)))));
		
		SetExpression result = extractUnions(expr);
	}
	
	private SetExpression extractUnions(SetExpression expr) {
		SetExpression result = UnionExtraction.extractUnions(expr);
		
		QueryStructureTester.assertTree(result);
		
		Logger.debug("orig:   " + ExpressionPrinter.toString(expr), TestUnionExtraction.class);
		Logger.debug("result: " + ExpressionPrinter.toString(result), TestUnionExtraction.class);
		return result;
	}
	
	
	/**
	 * Suite creation.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(new TestSuite(TestUnionExtraction.class));
	}
	
}
