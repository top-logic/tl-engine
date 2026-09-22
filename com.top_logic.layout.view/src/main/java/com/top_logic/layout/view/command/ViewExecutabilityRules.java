/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.view.ViewContext;

/**
 * Factory for the combined {@link ViewExecutabilityRule} of a {@link ViewCommand} from its
 * configured {@link ViewCommand.Config#getExecutability() rules}.
 *
 * <p>
 * Instantiates the configured rules, {@link ContextDependentRule#bind(ViewContext) binds} any that
 * depend on the build-time {@link ViewContext} (e.g. security rules resolving an enclosing scope),
 * and combines them. Shared by every element that hosts commands (panels, buttons, forms, app bar)
 * so that rule construction — and in particular context binding — is uniform.
 * </p>
 */
public class ViewExecutabilityRules {

	/**
	 * Builds the combined executability rule for the given configured rules.
	 *
	 * @param ruleConfigs
	 *        The configured rules (from {@link ViewCommand.Config#getExecutability()}).
	 * @param context
	 *        The build-time context of the guarded command, passed to
	 *        {@link ContextDependentRule#bind(ViewContext)}.
	 * @return The combined rule, or {@link ViewExecutabilityRule#ALWAYS_EXECUTABLE} when no rules are
	 *         configured.
	 */
	public static ViewExecutabilityRule build(
			List<PolymorphicConfiguration<? extends ViewExecutabilityRule>> ruleConfigs, ViewContext context) {
		if (ruleConfigs.isEmpty()) {
			return ViewExecutabilityRule.ALWAYS_EXECUTABLE;
		}
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(ViewExecutabilityRules.class);
		List<ViewExecutabilityRule> rules = new ArrayList<>();
		for (PolymorphicConfiguration<? extends ViewExecutabilityRule> ruleConfig : ruleConfigs) {
			ViewExecutabilityRule rule = instantiation.getInstance(ruleConfig);
			if (rule != null) {
				bind(rule, context);
				rules.add(rule);
			}
		}
		return CombinedViewExecutabilityRule.combine(rules);
	}

	/**
	 * Puts the rule a command {@link ViewCommand#getIntrinsicRule() brings of its own} in front of
	 * the rules built for its use site.
	 *
	 * <p>
	 * The command's own rule is bound to the same context as the configured ones, so both decide
	 * from the place the command is displayed in.
	 * </p>
	 *
	 * @param context
	 *        The build-time context of the guarded command, {@code null} for a command built
	 *        outside a view (its own rule is then left unbound).
	 * @param command
	 *        The command contributing a rule of its own.
	 * @param configured
	 *        The rule built from the command's configuration.
	 * @return The rule deciding over the command, both parts taken into account.
	 */
	public static ViewExecutabilityRule withIntrinsicRule(ViewContext context, ViewCommand command,
			ViewExecutabilityRule configured) {
		ViewExecutabilityRule intrinsic = command.getIntrinsicRule();
		if (intrinsic == ViewExecutabilityRule.ALWAYS_EXECUTABLE) {
			return configured;
		}
		if (context != null) {
			bind(intrinsic, context);
		}
		if (configured == ViewExecutabilityRule.ALWAYS_EXECUTABLE) {
			return intrinsic;
		}
		return CombinedViewExecutabilityRule.combine(List.of(intrinsic, configured));
	}

	private static void bind(ViewExecutabilityRule rule, ViewContext context) {
		if (rule instanceof ContextDependentRule contextDependent) {
			contextDependent.bind(context);
		}
	}
}
