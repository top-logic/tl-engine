/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.view.channel.ViewChannel;
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
public class CombinedViewExecutabilityRule implements ViewExecutabilityRule, ChannelDependentRule {

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
	 * The channels of every combined rule that names some, so that a command following the combined
	 * rule follows all of them.
	 */
	@Override
	public List<ViewChannel> observedChannels() {
		List<ViewChannel> result = new ArrayList<>();
		for (ViewExecutabilityRule rule : _rules) {
			if (rule instanceof ChannelDependentRule channelDependent) {
				result.addAll(channelDependent.observedChannels());
			}
		}
		return result;
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
