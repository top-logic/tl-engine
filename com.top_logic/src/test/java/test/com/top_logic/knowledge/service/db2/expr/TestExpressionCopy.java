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

import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCopy;

/**
 * Test case for {@link ExpressionCopy}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestExpressionCopy extends TestCase implements KnowledgeBaseTestScenarioConstants {

	public TestExpressionCopy(String name) {
		super(name);
	}
	
	public void testCopy() {
		SetExpression language = 
			crossProduct(
				filter(allOf(B_NAME), inSet(filter(map(allOf(AB_NAME), source()), hasType(B_NAME)))),
				union(
					intersection(
						filter(
							map(filter(allOf(BC_NAME), eval(destination(), ge(literal(1), literal(1)))), destination()),
							and(
								and(
									eqBinary(attribute(A_NAME, A1_NAME), literal("7")),
									eqCi(attribute(A_NAME, A2_NAME), literal("a2"))),
								instanceOf(C_NAME))),
						filter(
							allOf(X_NAME),
							or(gt(literal(3), attribute(X_NAME, X1_NAME)),
								not(le(attribute(X_NAME, X1_NAME), literal(7)))))),
					substraction(
						filter(
							allOf(C_NAME), matches("*", attribute(A_NAME, A1_NAME))),
						none())), 
				setLiteralOfEntries("a1", "a2", "a3"),
				partition(allOf(B_NAME), tuple(attribute(A_NAME, A1_NAME), flex(MOPrimitive.LONG, "f1")),
					max(attribute(A_NAME, A2_NAME))),
				partition(allOf(B_NAME), attribute(A_NAME, A2_NAME), min(attribute(A_NAME, A2_NAME))),
				partition(allOf(B_NAME), attribute(A_NAME, A2_NAME), count()),
				partition(allOf(B_NAME), attribute(A_NAME, A2_NAME), sum(attribute(B_NAME, B1_NAME)))
			);
		
		SetExpression result = ExpressionCopy.copy(language);
		
		QueryStructureTester.assertEquals(language, result);
		QueryStructureTester.assertDisjunct(language, result);
	}

	/**
	 * Suite creation.
	 */
	public static Test suite() {
		Test innerTest = new TestSuite(TestExpressionCopy.class);
		return ModuleLicenceTestSetup.setupModule(innerTest);
	}

}
