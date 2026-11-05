/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.dom;

import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.config.dom.FreeVariables;
import com.top_logic.model.search.expr.interpreter.DefResolver;
import com.top_logic.model.search.expr.interpreter.I18NConstants;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for {@link FreeVariables}.
 *
 * <p>
 * Each test states the free variables of a script and checks that the compilation of the script,
 * which resolves its variables with {@link DefResolver}, agrees: it fails with an unresolved
 * variable exactly when {@link FreeVariables} reports one.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestFreeVariables extends AbstractSearchExpressionTest {

	public void testFreeVariable() throws Exception {
		assertFree("$x", "x");
	}

	public void testSeveralFreeVariables() throws Exception {
		assertFree("list($b, $a, $b)", "b", "a");
	}

	public void testNoVariable() throws Exception {
		assertFree("1 + 2");
	}

	public void testLambdaParameter() throws Exception {
		assertFree("x -> $x");
	}

	public void testLambdaParameterInMap() throws Exception {
		assertFree("list(1, 2).map(x -> $x + 1)");
	}

	public void testFreeInLambda() throws Exception {
		assertFree("x -> $y", "y");
	}

	public void testNestedShadowingLambda() throws Exception {
		assertFree("x -> x -> $x");
	}

	public void testUseAfterShadowingLambda() throws Exception {
		assertFree("x -> list((x -> $x)(1), $x)");
	}

	public void testUseOutsideLambda() throws Exception {
		assertFree("list((x -> $x)(1), $x)", "x");
	}

	public void testTupleEarlierCoord() throws Exception {
		assertFree("tuple(a -> 1, b -> $a)");
	}

	public void testTupleLaterCoord() throws Exception {
		assertFree("tuple(a -> $b, b -> 1)", "b");
	}

	public void testTupleOwnCoord() throws Exception {
		assertFree("tuple(a -> $a)", "a");
	}

	public void testTupleCoordOutsideTuple() throws Exception {
		assertFree("list(tuple(a -> 1), $a)", "a");
	}

	public void testBlockAssignBeforeUse() throws Exception {
		assertFree("{x = 1; $x + 1;}");
	}

	public void testBlockAssignAfterUse() throws Exception {
		assertFree("{$x; x = 1; $x;}", "x");
	}

	public void testBlockAssignOwnValue() throws Exception {
		assertFree("{x = $x; $x;}", "x");
	}

	public void testBlockAssignOutsideBlock() throws Exception {
		assertFree("{ {x = 1; $x;}; $x;}", "x");
	}

	public void testHtmlEmbeddedExpression() throws Exception {
		assertFree("{{{<b>{$x}</b>}}}", "x");
	}

	public void testHtmlAttribute() throws Exception {
		assertFree("{{{<a href=\"{$y}\">text</a>}}}", "y");
	}

	public void testHtmlInLambda() throws Exception {
		assertFree("x -> {{{<b>{$x}</b>}}}");
	}

	public void testAccess() throws Exception {
		assertFree("$x.size()", "x");
	}

	public void testUndefined() throws Exception {
		assertEquals(Set.of("c"), FreeVariables.undefined(parse("list($a, $c)"), List.of("a", "b")));
	}

	private void assertFree(String script, String... expected) throws Exception {
		Set<String> free = FreeVariables.of(parse(script));
		assertEquals(script, List.of(expected), List.copyOf(free));

		String unresolved = compileUnresolved(script);
		if (free.isEmpty()) {
			assertNull("Compilation reports an unresolved variable in: " + script, unresolved);
		} else {
			assertNotNull("Compilation reports no unresolved variable in: " + script, unresolved);
			assertTrue("Compilation reports '" + unresolved + "' in: " + script, free.contains(unresolved));
		}
	}

	/**
	 * The variable the compilation of the given script reports as unresolved, or {@code null} if
	 * it compiles.
	 */
	private static String compileUnresolved(String script) throws Exception {
		try {
			QueryExecutor.compile(parse(script));
			return null;
		} catch (TopLogicException ex) {
			ResKey key = ex.getErrorKey();
			if (key.plain().equals(I18NConstants.ERROR_UNRESOLVED_VARIABLE__NAME.fill("").plain())) {
				return String.valueOf(key.arguments()[0]);
			}
			throw ex;
		}
	}

	public static Test suite() {
		return suite(TestFreeVariables.class);
	}

}
