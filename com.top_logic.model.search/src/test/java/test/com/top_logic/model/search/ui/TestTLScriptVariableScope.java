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

	private static Set<String> scope(String textToCursor) {
		return new HashSet<>(TLScriptVariableScope.inScopeVariables(textToCursor));
	}

	public void testTopLevelIsEmpty() {
		assertEquals(set(), scope("$"));
		assertEquals(set(), scope("count(all(`M:T`), x -> $x.foo())\n$"));
	}

	public void testSingleLambda() {
		assertEquals(set("x"), scope("x -> $"));
		assertEquals(set("x"), scope("list.filter(x -> $"));
	}

	public void testNestedLambdas() {
		assertEquals(set("x", "y"),
			scope("count(all(`M:T`), x -> $x.foo(y -> $"));
	}

	public void testCommaEndsLambdaBody() {
		assertEquals(set(), scope("foo(x -> $x, $"));
	}

	public void testChainedLambdasDoNotLeak() {
		assertEquals(set("y"),
			scope("list.filter(x -> $x > 0).map(y -> $"));
	}

	public void testClosedLambdaOutOfScope() {
		assertEquals(set(), scope("list.filter(x -> $x > 0).size == $"));
	}

	public void testMultiLine() {
		assertEquals(set("element"),
			scope("all(`M:T`)\n  .filter(element ->\n    $"));
	}

	public void testOptionalParameter() {
		assertEquals(set("a"), scope("tuple(a? -> $"));
	}

	public void testUnbalancedBracketsDoNotThrow() {
		assertEquals(set("x"), scope("foo(bar(x -> $"));
	}

	public void testShadowingDeduplicates() {
		assertEquals(set("x"), scope("x -> foo(x -> $"));
	}

	public void testSemicolonEndsLambdaScope() {
		// A lambda parameter is out of scope after the statement-terminating ';'.
		assertEquals(set(), scope("x -> $x; $"));
	}

	public void testAssignmentDefinesVariable() {
		assertEquals(set("fun"), scope("fun = 5; $"));
	}

	public void testAssignmentAcrossLambdaBody() {
		// The reported case: after 'fun = x -> $x+1;', 'x' is gone but 'fun' is available.
		assertEquals(set("fun"), scope("fun = x -> $x+1; $"));
	}

	public void testAssignmentNotVisibleInOwnRhs() {
		assertEquals(set(), scope("fun = $"));
	}

	public void testMultipleAssignments() {
		assertEquals(set("a", "fun"), scope("a = 5; fun = 3; $"));
	}

	public void testAssignmentVisibleInsideLaterLambda() {
		assertEquals(set("a", "x"), scope("a = 5; x -> $"));
	}

	public void testAssignmentIsBlockLocal() {
		// 'a' is defined inside the block; at the block-open position it is in scope,
		// while the still-being-defined 'fun' (its own RHS) is not.
		assertEquals(set("a"), scope("fun = {a = 1; $"));
	}

	public void testBracedLambdaStillCleared() {
		// Regression: closing '}' ends the lambda scope (this already worked).
		assertEquals(set(), scope("{x -> $x+1}; $"));
	}

	public void testComparisonIsNotAnAssignment() {
		// The '==' comparison operator must not be mistaken for a 'name =' assignment,
		// even when a NAME precedes it and the statement is terminated by ';'.
		assertEquals(set(), scope("size == 5; $"));
	}

	private static Set<String> set(String... names) {
		return new HashSet<>(List.of(names));
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestTLScriptVariableScope.class));
	}
}
