/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.layout.view.ViewContext;
import com.top_logic.tool.execution.ExecutableState;

/**
 * A {@link ViewExecutabilityRule} deciding over the arguments it is given with a selected argument
 * as {@link ActionArguments#defaultName() default}.
 *
 * <p>
 * A rule deciding over a single value sees the {@link ActionArguments#value() default argument}.
 * Where the arguments of a run name a different default than the argument the rule is meant to
 * decide over - the value an action processes, the first input of a command whose trigger supplies
 * the main argument - this rule hands the wrapped rule the arguments re-defaulted to the selected
 * one. Arguments without the selected name are passed unchanged.
 * </p>
 *
 * <p>
 * Binding to the {@link ContextDependentRule#bind(ViewContext) context} and the
 * {@link ObservableRule#observe(Runnable) observation} are forwarded to the wrapped rule.
 * </p>
 */
public final class DefaultArgumentRule implements ViewExecutabilityRule, ContextDependentRule, ObservableRule {

	private final ViewExecutabilityRule _rule;

	private final String _name;

	/**
	 * Creates a {@link DefaultArgumentRule}.
	 *
	 * @param rule
	 *        The rule to decide.
	 * @param name
	 *        The name of the argument the rule sees as default.
	 */
	public DefaultArgumentRule(ViewExecutabilityRule rule, String name) {
		_rule = rule;
		_name = name;
	}

	/**
	 * The given rule deciding with the given argument as default.
	 *
	 * @param rule
	 *        The rule to decide.
	 * @param name
	 *        The name of the argument the rule sees as default, {@code null} to keep the default
	 *        of the arguments.
	 * @return The rule itself, when there is nothing to select.
	 */
	public static ViewExecutabilityRule select(ViewExecutabilityRule rule, String name) {
		if (name == null || rule == ViewExecutabilityRule.ALWAYS_EXECUTABLE) {
			return rule;
		}
		return new DefaultArgumentRule(rule, name);
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		return _rule.isExecutable(input);
	}

	@Override
	public ExecutableState isExecutableFor(ActionArguments arguments) {
		return _rule.isExecutableFor(arguments.has(_name) ? arguments.withDefault(_name) : arguments);
	}

	@Override
	public void bind(ViewContext context) {
		if (_rule instanceof ContextDependentRule contextDependent) {
			contextDependent.bind(context);
		}
	}

	@Override
	public Runnable observe(Runnable revalidate) {
		if (_rule instanceof ObservableRule observable) {
			return observable.observe(revalidate);
		}
		return () -> {
			// Nothing observed.
		};
	}
}
