/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.expr;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.knowledge.search.Expression;
import com.top_logic.knowledge.search.Function;
import com.top_logic.knowledge.search.Order;
import com.top_logic.knowledge.search.QueryPart;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.db2.expr.visit.DefaultDescendingQueryVisitor;
import com.top_logic.knowledge.service.db2.expr.visit.ExpressionPrinter;

/**
 * Structural tests for {@link QueryPart expressions}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class QueryStructureTester extends DefaultDescendingQueryVisitor<Void, Void, Void, Void, Void, Map<QueryPart, QueryPart>> {

	public static void assertTree(QueryPart expr) {
		assertDisjunct(expr, new HashMap<>());
	}
	
	public static void assertDisjunct(QueryPart expr1, QueryPart expr2) {
		HashMap<QueryPart, QueryPart> nodes = new HashMap<>();
		assertDisjunct(expr1, nodes);
		assertDisjunct(expr2, nodes);
	}
	
	public static void assertEquals(QueryPart expr1, QueryPart expr2) {
		String s1 = ExpressionPrinter.toString(expr1);
		String s2 = ExpressionPrinter.toString(expr2);
		TestCase.assertEquals(s1, s2);
	}
	
	private static void assertDisjunct(QueryPart expr, Map<QueryPart, QueryPart> other) {
		test(expr, other);
		expr.visitQuery(new QueryStructureTester(), other);
	}
	
	@Override
	protected Void descendExpr(QueryPart parent, Expression expr, Map<QueryPart, QueryPart> arg) {
		test(expr, arg);
		return super.descendExpr(parent, expr, arg);
	}
	
	@Override
	protected Void descendFun(QueryPart parent, Function fun, Map<QueryPart, QueryPart> arg) {
		test(fun, arg);
		return super.descendFun(parent, fun, arg);
	}
	
	@Override
	protected Void descendOrder(QueryPart parent, Order expr, Map<QueryPart, QueryPart> arg) {
		test(expr, arg);
		return super.descendOrder(parent, expr, arg);
	}
	
	@Override
	protected Void descendSet(QueryPart parent, SetExpression expr, Map<QueryPart, QueryPart> arg) {
		test(expr, arg);
		return super.descendSet(parent, expr, arg);
	}

	private static void test(QueryPart expr, Map<QueryPart, QueryPart> arg) {
		QueryPart clash = arg.put(expr, expr);
		if (clash != null) {
			if (clash == expr) {
				TestCase.fail("Expression found more than once: " + ExpressionPrinter.toString(expr));
			}
		}
	}
	
}