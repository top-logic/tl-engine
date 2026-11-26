/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import junit.framework.TestCase;

/**
 * Tests {@link RoleQuestions}: questions depending on each other in cycles are answered with the
 * least fixed point, independent of the order in which they are asked.
 */
@SuppressWarnings("javadoc")
public class TestRoleQuestions extends TestCase {

	private final Map<String, Supplier<Set<String>>> _definitions = new HashMap<>();

	private final Map<String, Integer> _evaluations = new HashMap<>();

	public void testQuestionWithoutCycle() {
		define("a", () -> union(Set.of("g"), ask("b")));
		define("b", () -> Set.of("h"));
		assertEquals(Set.of("g", "h"), ask("a"));
	}

	public void testCycleAloneGrantsNothing() {
		define("a", () -> ask("b"));
		define("b", () -> ask("a"));
		assertEquals(Set.of(), ask("a"));
		assertEquals(Set.of(), ask("b"));
	}

	public void testCycleWithSource() {
		define("a", () -> ask("b"));
		define("b", () -> union(Set.of("g"), ask("a")));
		assertEquals(Set.of("g"), ask("a"));
		assertEquals(Set.of("g"), ask("b"));
	}

	/**
	 * A project grants a role to its members and inherits it from its releases, a release inherits
	 * it from the project. A rule requires both, the role on the project (path) and the role on the
	 * release (gate). A provisional "nobody holds the role on the release", computed while the
	 * project is being answered, must not close the gate.
	 */
	public void testResultIsIndependentOfOrder() {
		defineProjectAndRelease();
		define("pathFirst", () -> intersection(ask("project"), ask("release")));
		assertEquals(Set.of("g"), ask("pathFirst"));

		_definitions.clear();
		defineProjectAndRelease();
		define("gateFirst", () -> intersection(ask("release"), ask("project")));
		assertEquals(Set.of("g"), ask("gateFirst"));
	}

	private void defineProjectAndRelease() {
		define("project", () -> union(Set.of("g"), ask("release")));
		define("release", () -> ask("project"));
	}

	/**
	 * A role is derived through a second pass over a cycle: the result of the first pass is the
	 * premise of a rule within the cycle.
	 */
	public void testFixedPointNeedsSeveralPasses() {
		define("a", () -> {
			Set<String> b = ask("b");
			Set<String> result = new HashSet<>(Set.of("g"));
			if (b.contains("g")) {
				result.add("h");
			}
			return result;
		});
		define("b", () -> {
			Set<String> a = ask("a");
			Set<String> result = new HashSet<>(a);
			if (a.contains("h")) {
				result.add("i");
			}
			return result;
		});
		assertEquals(Set.of("g", "h"), ask("a"));
		assertEquals(Set.of("g", "h", "i"), ask("b"));
	}

	public void testFinalAnswerIsKeptWhileOuterQuestionIsOpen() {
		define("outer", () -> union(ask("inner"), ask("inner")));
		define("inner", () -> Set.of("g"));
		assertEquals(Set.of("g"), ask("outer"));
		assertEquals(Integer.valueOf(1), _evaluations.get("inner"));
	}

	private void define(String key, Supplier<Set<String>> definition) {
		_definitions.put(key, definition);
	}

	private Set<String> ask(String key) {
		return RoleQuestions.ask(null, false, key, Collections.<String> emptySet(), () -> {
			_evaluations.merge(key, Integer.valueOf(1), Integer::sum);
			return _definitions.get(key).get();
		});
	}

	private static Set<String> union(Set<String> first, Set<String> second) {
		Set<String> result = new HashSet<>(first);
		result.addAll(second);
		return result;
	}

	private static Set<String> intersection(Set<String> first, Set<String> second) {
		Set<String> result = new HashSet<>(first);
		result.retainAll(second);
		return result;
	}

}
