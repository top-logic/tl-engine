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

	private static Set<String> set(String... names) {
		return new HashSet<>(List.of(names));
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestTLScriptVariableScope.class));
	}
}
