/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioConstants;

import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionCopy;

/**
 * Test for {@link QueryStructureTester}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestQueryStructureTester extends TestCase implements KnowledgeBaseTestScenarioConstants {

	public TestQueryStructureTester(String name) {
		super(name);
	}
	
	public void testTree() {
		SetExpression commonSubExpr = filter(allOf(B_NAME), eqBinary(attribute(A_NAME, A1_NAME), literal("7")));
		QueryStructureTester.assertTree(commonSubExpr);
		
		SetExpression illegalExpr = union(commonSubExpr, substraction(allOf(B_NAME), commonSubExpr));
		try {
			QueryStructureTester.assertTree(illegalExpr);
			fail("Not a tree.");
		} catch (AssertionFailedError ex) {
			// Expected.
		}
	}
	
	public void testDisjunct() {
		doTestDisjunct(filter(allOf(B_NAME), eqBinary(attribute(A_NAME, A1_NAME), literal("7"))));
		doTestDisjunct(allOf(B_NAME));
	}

	private void doTestDisjunct(SetExpression commonSubExpr) {
		QueryStructureTester.assertTree(commonSubExpr);

		SetExpression expr1 = union(allOf(C_NAME), substraction(allOf(B_NAME), commonSubExpr));
		SetExpression expr2 = substraction(allOf(C_NAME), substraction(allOf(B_NAME), commonSubExpr));
		SetExpression expr3 =
			substraction(allOf(C_NAME), substraction(allOf(B_NAME), ExpressionCopy.copy(commonSubExpr)));
		
		QueryStructureTester.assertDisjunct(expr1, expr3);
		QueryStructureTester.assertDisjunct(expr2, expr3);
		
		try {
			QueryStructureTester.assertDisjunct(commonSubExpr, commonSubExpr);
			fail("Not a tree.");
		} catch (AssertionFailedError ex) {
			// Expected.
		}
		
		try {
			QueryStructureTester.assertDisjunct(expr1, expr2);
			fail("Not a tree.");
		} catch (AssertionFailedError ex) {
			// Expected.
		}
	}

	public void testDisjunctExpr() {
		try {
			Expression literal = literal("a1");
			QueryStructureTester.assertDisjunct(
				filter(allOf(B_NAME), eqBinary(attribute(A_NAME, A1_NAME), literal)),
				filter(allOf(C_NAME), eqBinary(attribute(A_NAME, A2_NAME), literal)));
			fail("Not a tree.");
		} catch (AssertionFailedError ex) {
			// Expected.
		}
	}

	/**
	 * Suite creation.
	 */
	public static Test suite() {
		Test innerTest = new TestSuite(TestQueryStructureTester.class);
		return ModuleLicenceTestSetup.setupModule(innerTest);
	}

}
