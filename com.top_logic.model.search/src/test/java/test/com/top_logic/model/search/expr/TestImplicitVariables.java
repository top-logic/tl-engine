/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.util.List;

import junit.framework.Test;

import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.dom.Expr.Define;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ModelReferenceChecker;

/**
 * Test for scripts with implicitly defined variables compiled by
 * {@link QueryExecutor#compile(Expr, List)} and wrapped by {@link Define#withVariables(List, Expr)}.
 */
@SuppressWarnings("javadoc")
public class TestImplicitVariables extends AbstractSearchExpressionTest {

	private static final List<String> ABC = List.of("a", "b", "c");

	/**
	 * The values are bound to the variables by name, independent of the order in which the script
	 * uses them.
	 */
	public void testBindByName() throws Exception {
		QueryExecutor executor = QueryExecutor.compile(parse("[$c, $a, $b]"), ABC);

		assertEquals(List.of("C", "A", "B"), executor.execute("A", "B", "C"));
	}

	/**
	 * A script need not use all implicitly defined variables.
	 */
	public void testUnusedVariable() throws Exception {
		QueryExecutor executor = QueryExecutor.compile(parse("$b"), ABC);

		assertEquals("B", executor.execute("A", "B", "C"));
	}

	/**
	 * Arguments following the variable values are passed to the script itself: a script that is a
	 * function receives them as its own arguments.
	 */
	public void testSurplusArgumentsReachFunction() throws Exception {
		QueryExecutor executor = QueryExecutor.compile(parse("x -> [$a, $b, $x]"), List.of("a", "b"));

		assertEquals(List.of("A", "B", "X"), executor.execute("A", "B", "X"));
	}

	/**
	 * A script that is no function ignores arguments following the variable values.
	 */
	public void testSurplusArgumentsIgnored() throws Exception {
		QueryExecutor executor = QueryExecutor.compile(parse("$a"), List.of("a"));

		assertEquals("A", executor.execute("A", "X", "Y"));
	}

	/**
	 * Without variable names, the expression is compiled unchanged.
	 */
	public void testNoVariables() throws Exception {
		Expr expr = parse("x -> $x");

		assertSame(expr, Define.withVariables(List.of(), expr));
		assertEquals("X", QueryExecutor.compile(expr, List.of()).execute("X"));
	}

	/**
	 * An absent expression results in no executor.
	 */
	public void testCompileOptional() {
		assertNull(QueryExecutor.compileOptional(null, ABC));
	}

	/**
	 * The syntax check knows the implicitly defined variables.
	 */
	public void testCheck() throws Exception {
		Expr expr = parse("[$a, $c]");

		ModelReferenceChecker.checkModelElements(expr, ABC);
		try {
			ModelReferenceChecker.checkModelElements(expr, List.of("a"));
			fail("Undefined variable 'c' not reported.");
		} catch (RuntimeException ex) {
			// Expected.
		}
	}

	public static Test suite() {
		return suite(TestImplicitVariables.class);
	}

}
