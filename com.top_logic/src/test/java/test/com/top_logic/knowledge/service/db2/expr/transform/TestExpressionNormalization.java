/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr.transform;

import static com.top_logic.knowledge.search.ExpressionFactory.*;
import static test.com.top_logic.knowledge.service.db2.expr.QueryStructureTester.*;
import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.db2.expr.transform.ExpressionNormalization;

/**
 * Test case for {@link ExpressionNormalization}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestExpressionNormalization extends BasicTestCase {

	public TestExpressionNormalization(String name) {
		super(name);
	}
	
	public void testAndOfOrNormalization() {
		Expression expr = and(or(attribute("A", "a1"), attribute("A", "a2")), or(attribute("B", "b1"), attribute("B", "b2")));
		Expression result = ExpressionNormalization.normalizeExpression(expr);
		
		assertTreeAndLog(result);
	}

	private void assertTreeAndLog(QueryPart result) {
		assertTree(result);
		Logger.debug(result.toString(), TestExpressionNormalization.class);
	}
	
	public void testNotAndNormalization() {
		Expression expr = not(and(attribute("A", "a1"), attribute("A", "a2")));
		Expression result = ExpressionNormalization.normalizeExpression(expr);
		
		assertTreeAndLog(result);
	}
	
	public void testNotOrNormalization() {
		Expression expr = not(or(attribute("A", "a1"), attribute("A", "a2")));
		Expression result = ExpressionNormalization.normalizeExpression(expr);
		
		assertTreeAndLog(result);
	}
	
	public void testNotNotNormalization() {
		Expression expr = not(or(not(and(attribute("A", "a1"), attribute("A", "a2"))), attribute("B", "b1")));
		Expression result = ExpressionNormalization.normalizeExpression(expr);
		
		assertTreeAndLog(result);
	}
	
	public void testDescend() {
		SetExpression expr = 
			filter(
				allOf("A"), 
				not(
					and(
						or(attribute("A", "a1"), attribute("A", "a2")), 
						inSet(
							map(
								filter(
									allOf("AB"), 
									not(or(attribute("AB", "ab1"), attribute("AB", "ab2")))), 
								source())))));
		
		ExpressionNormalization.normalizeExpressions(expr);
		
		assertTreeAndLog(expr);
	}
	

	/**
	 * Suite creation.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(new TestSuite(TestExpressionNormalization.class));
	}
	
}
