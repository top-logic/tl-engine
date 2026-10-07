/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.view.ViewContext;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} that combines multiple rules with short-circuit AND semantics.
 *
 * <p>
 * The first rule that returns a non-{@link ExecutableState#EXECUTABLE EXECUTABLE} state determines
 * the overall result. If all rules return {@link ExecutableState#EXECUTABLE}, the combined result is
 * {@link ExecutableState#EXECUTABLE}.
 * </p>
 */
public class CombinedViewExecutabilityRule implements ViewExecutabilityRule, ContextDependentRule, ObservableRule {

	private final List<ViewExecutabilityRule> _rules;

	/**
	 * Creates a new {@link CombinedViewExecutabilityRule}.
	 *
	 * @param rules
	 *        The rules to combine. Must not be {@code null}.
	 */
	public CombinedViewExecutabilityRule(List<ViewExecutabilityRule> rules) {
		_rules = rules;
	}

	/**
	 * Factory method that combines a list of rules into a single rule.
	 *
	 * <p>
	 * Optimizes for common cases: returns a no-op rule for an empty list, the single rule for a
	 * one-element list, and a {@link CombinedViewExecutabilityRule} otherwise.
	 * </p>
	 *
	 * @param rules
	 *        The rules to combine.
	 * @return A single combined rule.
	 */
	public static ViewExecutabilityRule combine(List<ViewExecutabilityRule> rules) {
		if (rules.isEmpty()) {
			return ViewExecutabilityRule.ALWAYS_EXECUTABLE;
		}
		if (rules.size() == 1) {
			return rules.get(0);
		}
		return new CombinedViewExecutabilityRule(rules);
	}

	/**
	 * Binds every combined rule that depends on the context of the command.
	 *
	 * <p>
	 * A combination built from rules that are bound already, as
	 * {@link ViewExecutabilityRules#build(List, ViewContext)} does, is not bound again.
	 * </p>
	 */
	@Override
	public void bind(ViewContext context) {
		for (ViewExecutabilityRule rule : _rules) {
			if (rule instanceof ContextDependentRule contextDependent) {
				contextDependent.bind(context);
			}
		}
	}

	/**
	 * Follows every combined rule that reports changes, so that a command following the combined
	 * rule hears from all of them.
	 */
	@Override
	public Runnable observe(Runnable revalidate) {
		List<Runnable> stops = new ArrayList<>();
		for (ViewExecutabilityRule rule : _rules) {
			if (rule instanceof ObservableRule observable) {
				stops.add(observable.observe(revalidate));
			}
		}
		return () -> stops.forEach(Runnable::run);
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		for (ViewExecutabilityRule rule : _rules) {
			ExecutableState state = rule.isExecutable(input);
			if (!state.isExecutable()) {
				return state;
			}
		}
		return ExecutableState.EXECUTABLE;
	}
}
