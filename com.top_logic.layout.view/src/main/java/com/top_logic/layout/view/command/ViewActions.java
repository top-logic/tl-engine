/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.react.control.button.ButtonTone;

/**
 * Utilities for the {@link ViewAction} lists that a command or a composite action is configured
 * with.
 */
public class ViewActions {

	/**
	 * Instantiates a configured list of {@link ViewAction}s.
	 *
	 * @param configs
	 *        The configurations to instantiate, in the order they run in.
	 * @return The instantiated actions, without the ones that could not be created (the failure is
	 *         reported to the given context).
	 */
	public static List<ViewAction> instantiate(InstantiationContext context,
			List<PolymorphicConfiguration<? extends ViewAction>> configs) {
		return configs.stream()
			.<ViewAction> map(config -> context.getInstance(config))
			.filter(action -> action != null)
			.toList();
	}

	/**
	 * Whether one of the given actions applies the values entered into the enclosing form.
	 *
	 * @see ViewAction#appliesFormState()
	 */
	public static boolean appliesFormState(List<ViewAction> actions) {
		return actions.stream().anyMatch(ViewAction::appliesFormState);
	}

	/**
	 * The tone of a chain made of the given actions: {@link ButtonTone#DANGER} as soon as one of
	 * them {@link ViewAction#getTone() is destructive}, {@link ButtonTone#DEFAULT} otherwise.
	 */
	public static ButtonTone tone(List<ViewAction> actions) {
		return actions.stream().anyMatch(action -> action.getTone() == ButtonTone.DANGER)
			? ButtonTone.DANGER
			: ButtonTone.DEFAULT;
	}

	/**
	 * The combination of the rules the given actions {@link ViewAction#getIntrinsicRule() bring of
	 * their own}.
	 *
	 * <p>
	 * Every given action runs whenever the chain runs, so the chain can be carried out only where
	 * each of their rules allows it.
	 * </p>
	 *
	 * @return The {@link CombinedViewExecutabilityRule#combine(List) combined} rule, a fresh instance
	 *         per call; {@link ViewExecutabilityRule#ALWAYS_EXECUTABLE} when no action brings a rule.
	 */
	public static ViewExecutabilityRule intrinsicRule(List<ViewAction> actions) {
		List<ViewExecutabilityRule> rules = new ArrayList<>();
		for (ViewAction action : actions) {
			ViewExecutabilityRule rule = action.getIntrinsicRule();
			if (rule != ViewExecutabilityRule.ALWAYS_EXECUTABLE) {
				rules.add(rule);
			}
		}
		return CombinedViewExecutabilityRule.combine(rules);
	}
}
