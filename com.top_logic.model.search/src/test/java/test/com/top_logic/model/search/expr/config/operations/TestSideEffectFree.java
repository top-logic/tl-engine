/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.operations;

import static com.top_logic.model.search.expr.query.QueryExecutor.*;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.model.search.expr.Literal;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.SearchExpressionFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.operations.MathFunctions;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptMethod;

/**
 * Test for {@link SideEffectFree} annotations on TL-Script functions.
 *
 * @see SideEffectFreeFunctions
 */
@SuppressWarnings("javadoc")
public class TestSideEffectFree extends AbstractSearchExpressionTest {

	private static final String PLAIN = SideEffectFreeFunctions.PREFIX + "Plain";

	private static final String FREE = SideEffectFreeFunctions.PREFIX + "Free";

	private static final String FOLDED = SideEffectFreeFunctions.PREFIX + "Folded";

	public void testUnannotatedHasSideEffects() throws Exception {
		TLScriptMethod call = call(PLAIN);
		assertFalse(call.isSideEffectFree());
		assertFalse(call.canEvaluateAtCompileTime(new Object[] { "a" }));
	}

	public void testAnnotatedIsSideEffectFree() throws Exception {
		TLScriptMethod call = call(FREE);
		assertTrue(call.isSideEffectFree());
		assertFalse(call.canEvaluateAtCompileTime(new Object[] { "a" }));
	}

	public void testFlaggedCanEvaluateAtCompileTime() throws Exception {
		TLScriptMethod call = call(FOLDED);
		assertTrue(call.isSideEffectFree());
		assertTrue(call.canEvaluateAtCompileTime(new Object[] { "a" }));
	}

	public void testFlaggedIsFolded() throws Exception {
		assertFolded("folded:a", FOLDED + "('a')");
	}

	public void testUnannotatedIsNotFolded() throws Exception {
		assertNotFolded("plain:a", PLAIN + "('a')");
	}

	public void testAnnotatedWithoutFlagIsNotFolded() throws Exception {
		assertNotFolded("free:a", FREE + "('a')");
	}

	public void testSecurityFlaggedIsNotFolded() throws Exception {
		assertNotFolded("a:true:3", SecurityFlagFunctions.PREFIX + "Combine('a', 3)");
	}

	public void testMathIsFolded() throws Exception {
		assertFolded(2.0, "mathSqrt(4)");
		assertFolded(3.0, "mathSqrt(mathPow(3, 2))");
		assertFolded(Math.PI, "mathPi()");
	}

	/**
	 * {@link MathFunctions#random()} must be evaluated anew each time.
	 */
	public void testMathRandomIsNotFolded() throws Exception {
		SearchExpression compiled = compileExpr(search("mathRandom()"));
		assertFalse(compiled instanceof Literal);
		assertInstanceof(execute(compiled), Double.class);
	}

	private void assertFolded(Object expected, String script) throws Exception {
		SearchExpression compiled = compileExpr(search(script));
		assertInstanceof(compiled, Literal.class);
		assertEquals(expected, ((Literal) compiled).getValue());
	}

	private void assertNotFolded(Object expected, String script) throws Exception {
		SearchExpression compiled = compileExpr(search(script));
		assertInstanceof(compiled, TLScriptMethod.class);
		assertEquals(expected, eval(compiled));
	}

	private static TLScriptMethod call(String function) throws Exception {
		TLScriptMethod.Builder builder = (TLScriptMethod.Builder) SearchBuilder.getInstance().getBuilder(function);
		assertNotNull("Function '" + function + "' not registered.", builder);
		return builder.build(null, new SearchExpression[] { SearchExpressionFactory.literal("a") });
	}

	public static Test suite() {
		return suite(TestSideEffectFree.class);
	}

}
