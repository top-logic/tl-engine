/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.model.search.ui.TLScriptVariableScope;

/**
 * Test for {@link TLScriptVariableScope}.
 */
public class TestTLScriptVariableScope extends BasicTestCase {

	/**
	 * The in-scope variable names for the given script prefix.
	 *
	 * <p>
	 * In every test the trailing <code>$</code> marks the cursor position at which completion is
	 * requested.
	 * </p>
	 */
	private static Set<String> scope(String textToCursor) {
		return new HashSet<>(TLScriptVariableScope.inScopeVariables(textToCursor));
	}

	/**
	 * Without an enclosing lambda or a preceding assignment, no variable is in scope - both at the
	 * very start and after a complete, already-closed expression.
	 */
	public void testTopLevelIsEmpty() {
		assertEquals(set(), scope("$"));
		assertEquals(set(), scope("count(all(`M:T`), x -> $x.foo())\n$"));
	}

	/**
	 * A lambda parameter is in scope within the lambda body.
	 */
	public void testSingleLambda() {
		assertEquals(set("x"), scope("x -> $"));
		assertEquals(set("x"), scope("list.filter(x -> $"));
	}

	/**
	 * The parameters of all enclosing lambdas are in scope at a nested position.
	 */
	public void testNestedLambdas() {
		assertEquals(set("x", "y"),
			scope("count(all(`M:T`), x -> $x.foo(y -> $"));
	}

	/**
	 * A lambda body ends at the argument-separating comma, so the parameter is not in scope in a
	 * following argument.
	 */
	public void testCommaEndsLambdaBody() {
		assertEquals(set(), scope("foo(x -> $x, $"));
	}

	/**
	 * The parameter of an earlier link in a method chain does not leak into a later link.
	 */
	public void testChainedLambdasDoNotLeak() {
		assertEquals(set("y"),
			scope("list.filter(x -> $x > 0).map(y -> $"));
	}

	/**
	 * Once a lambda's body has been closed, its parameter is no longer in scope.
	 */
	public void testClosedLambdaOutOfScope() {
		assertEquals(set(), scope("list.filter(x -> $x > 0).size == $"));
	}

	/**
	 * A lambda body spanning multiple lines keeps its parameter in scope.
	 */
	public void testMultiLine() {
		assertEquals(set("element"),
			scope("all(`M:T`)\n  .filter(element ->\n    $"));
	}

	/**
	 * The optional-parameter form of a tuple coordinate (<code>a? -&gt; ...</code>) binds the
	 * parameter.
	 */
	public void testOptionalParameter() {
		assertEquals(set("a"), scope("tuple(a? -> $"));
	}

	/**
	 * Incomplete input with unclosed brackets (as produced while typing) yields a best-effort result
	 * without throwing.
	 */
	public void testUnbalancedBracketsDoNotThrow() {
		assertEquals(set("x"), scope("foo(bar(x -> $"));
	}

	/**
	 * A name bound by two nested lambdas is reported only once.
	 */
	public void testShadowingDeduplicates() {
		assertEquals(set("x"), scope("x -> foo(x -> $"));
	}

	/**
	 * A lambda parameter is out of scope after the statement-terminating <code>;</code>.
	 */
	public void testSemicolonEndsLambdaScope() {
		assertEquals(set(), scope("x -> $x; $"));
	}

	/**
	 * A <code>name = ...;</code> assignment defines a local variable that is offered afterwards.
	 */
	public void testAssignmentDefinesVariable() {
		assertEquals(set("fun"), scope("fun = 5; $"));
	}

	/**
	 * The reported case: after <code>fun = x -&gt; $x+1;</code> the lambda parameter <code>x</code> is
	 * gone, but the assigned variable <code>fun</code> is available.
	 */
	public void testAssignmentAcrossLambdaBody() {
		assertEquals(set("fun"), scope("fun = x -> $x+1; $"));
	}

	/**
	 * An assigned variable is not yet in scope within its own right-hand side.
	 */
	public void testAssignmentNotVisibleInOwnRhs() {
		assertEquals(set(), scope("fun = $"));
	}

	/**
	 * Several assignments in the same block accumulate; all are in scope afterwards.
	 */
	public void testMultipleAssignments() {
		assertEquals(set("a", "fun"), scope("a = 5; fun = 3; $"));
	}

	/**
	 * An assigned variable stays in scope inside a lambda that follows the assignment.
	 */
	public void testAssignmentVisibleInsideLaterLambda() {
		assertEquals(set("a", "x"), scope("a = 5; x -> $"));
	}

	/**
	 * An assignment is block-local: inside the block <code>a</code> is in scope, while the
	 * still-being-defined <code>fun</code> (its own right-hand side) is not.
	 */
	public void testAssignmentIsBlockLocal() {
		assertEquals(set("a"), scope("fun = {a = 1; $"));
	}

	/**
	 * Regression: a closing <code>}</code> ends the lambda scope of a braced block.
	 */
	public void testBracedLambdaStillCleared() {
		assertEquals(set(), scope("{x -> $x+1}; $"));
	}

	/**
	 * The <code>==</code> comparison operator must not be mistaken for a <code>name =</code>
	 * assignment, even when a name precedes it and the statement is terminated by <code>;</code>.
	 */
	public void testComparisonIsNotAnAssignment() {
		assertEquals(set(), scope("size == 5; $"));
	}

	/**
	 * Builds an expected result set from the given variable names.
	 */
	private static Set<String> set(String... names) {
		return new HashSet<>(List.of(names));
	}

	/**
	 * The {@link Test} suite of this test case.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestTLScriptVariableScope.class));
	}
}
