/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.configured;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.compile.transform.HasSideEffects;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Decides which configured TL-Script functions are free of side effects, read from their compiled
 * bodies.
 *
 * <p>
 * A configured function has a side effect, iff its body contains a built-in function that is not
 * {@link GenericMethod#isSideEffectFree() free of side effects}, or calls a configured function
 * with a side effect. The analysis computes the least fixpoint of the property "has a side effect"
 * over the call graph of the configured functions: a function whose body directly contains an
 * effect is marked, and the mark is propagated backwards along the calls until nothing changes.
 * Functions that call each other or themselves without an effect in any of their bodies are
 * therefore free of side effects.
 * </p>
 *
 * <p>
 * A function without compiled body, or whose body calls a function that is not configured, counts
 * as having a side effect.
 * </p>
 *
 * <p>
 * The analysis is an immutable snapshot of the {@link QueryExecutor executors} it was computed from.
 * It neither compiles a script nor asks a call of a configured function whether it is free of side
 * effects, so it can not recurse into itself.
 * </p>
 *
 * @see ConfiguredTLScriptFunctions#isSideEffectFree(String)
 */
final class ConfiguredFunctionSideEffects {

	private final Map<String, QueryExecutor> _executors;

	private final Set<String> _sideEffectFree;

	/**
	 * Analyzes the given compiled configured functions.
	 *
	 * @param executors
	 *        The compiled bodies of all configured functions by function name. The map is not
	 *        modified and is the identity this analysis is {@link #isBasedOn(Map) based on}.
	 */
	ConfiguredFunctionSideEffects(Map<String, QueryExecutor> executors) {
		_executors = executors;
		_sideEffectFree = analyze(executors);
	}

	/**
	 * Whether this analysis was computed from the given executors.
	 */
	boolean isBasedOn(Map<String, QueryExecutor> executors) {
		return _executors == executors;
	}

	/**
	 * Whether the configured function with the given name is free of side effects.
	 *
	 * @return <code>false</code> for a name that is not a configured function.
	 */
	boolean isSideEffectFree(String functionName) {
		return _sideEffectFree.contains(functionName);
	}

	private static Set<String> analyze(Map<String, QueryExecutor> executors) {
		Map<String, Set<String>> callersByCallee = new HashMap<>();
		Set<String> withEffect = new HashSet<>();
		Deque<String> worklist = new ArrayDeque<>();

		for (Entry<String, QueryExecutor> entry : executors.entrySet()) {
			String function = entry.getKey();
			BodyEffects body = BodyEffects.analyze(entry.getValue());
			boolean directEffect = body.hasDirectEffect();
			for (String callee : body.getCallees()) {
				if (!executors.containsKey(callee)) {
					directEffect = true;
				}
				callersByCallee.computeIfAbsent(callee, x -> new HashSet<>()).add(function);
			}
			if (directEffect) {
				withEffect.add(function);
				worklist.add(function);
			}
		}

		while (!worklist.isEmpty()) {
			String callee = worklist.removeFirst();
			for (String caller : callersByCallee.getOrDefault(callee, Collections.emptySet())) {
				if (withEffect.add(caller)) {
					worklist.add(caller);
				}
			}
		}

		Set<String> result = new HashSet<>(executors.keySet());
		result.removeAll(withEffect);
		return Collections.unmodifiableSet(result);
	}

	/**
	 * {@link HasSideEffects} applied to every part of a configured function's body, that collects
	 * the calls of configured functions instead of deciding on them.
	 */
	private static final class BodyEffects extends HasSideEffects {

		private final Set<String> _callees = new LinkedHashSet<>();

		private boolean _directEffect;

		static BodyEffects analyze(QueryExecutor executor) {
			BodyEffects result = new BodyEffects();
			if (executor == null) {
				result._directEffect = true;
				return result;
			}
			try {
				SearchExpression body = executor.getSearch();
				result._directEffect = body.visit(result, null).booleanValue();
			} catch (RuntimeException ex) {
				result._directEffect = true;
			}
			return result;
		}

		/**
		 * Whether the body contains a built-in function with a side effect.
		 */
		boolean hasDirectEffect() {
			return _directEffect;
		}

		/**
		 * The names of the configured functions called in the body.
		 */
		Set<String> getCallees() {
			return _callees;
		}

		@Override
		public Boolean visitGenericMethod(GenericMethod expr, Void arg) {
			Boolean self;
			if (expr instanceof QueryExecutorMethod call) {
				_callees.add(call.getName());
				self = Boolean.FALSE;
			} else {
				self = super.visitGenericMethod(expr, arg);
			}
			return combine(self, combine(descendParts(expr, arg, expr.getArguments())));
		}

	}

}
