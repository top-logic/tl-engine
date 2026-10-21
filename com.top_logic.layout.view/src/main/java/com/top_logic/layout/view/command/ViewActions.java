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
	 * Instantiates a configured list of {@link ViewAction}s in the {@link ActionScope} of the given
	 * context.
	 *
	 * <p>
	 * Used by an action that runs nested actions with its own arguments, e.g. the actions of a
	 * transaction or of a branch.
	 * </p>
	 *
	 * @see #instantiate(InstantiationContext, List, ActionScope)
	 */
	public static List<ViewAction> instantiate(InstantiationContext context,
			List<PolymorphicConfiguration<? extends ViewAction>> configs) {
		return instantiate(context, configs, ActionScope.of(context));
	}

	/**
	 * Instantiates a configured list of {@link ViewAction}s that run with arguments of the given
	 * scope.
	 *
	 * <p>
	 * Each action is created in a context {@link ActionScope#bind(InstantiationContext) bound} to the
	 * scope, so that it finds the names of its arguments with
	 * {@link ActionScope#of(InstantiationContext)}. The options a configuration offers for the chain
	 * - the argument to process ({@link WithValueArgument}), the follow-up actions over the result
	 * ({@link WithResultActions}) - are applied by wrapping the action into an {@link ActionStep};
	 * the follow-up actions are instantiated in the scope of the action extended by the name of the
	 * result.
	 * </p>
	 *
	 * <p>
	 * In a {@link ActionScope#isTransactional() transactional} scope, an action that
	 * {@link ViewAction#changesViewState() changes the state of the view} is reported as an error
	 * and left out.
	 * </p>
	 *
	 * @param configs
	 *        The configurations to instantiate, in the order they run in.
	 * @param scope
	 *        The names of the arguments the actions run with.
	 * @return The instantiated actions, without the ones that could not be created (the failure is
	 *         reported to the given context).
	 */
	public static List<ViewAction> instantiate(InstantiationContext context,
			List<PolymorphicConfiguration<? extends ViewAction>> configs, ActionScope scope) {
		InstantiationContext scoped = scope.bind(context);
		List<ViewAction> result = new ArrayList<>(configs.size());
		for (PolymorphicConfiguration<? extends ViewAction> config : configs) {
			ViewAction action = instantiate(scoped, config, scope);
			if (action != null) {
				result.add(action);
			}
		}
		return List.copyOf(result);
	}

	private static ViewAction instantiate(InstantiationContext context,
			PolymorphicConfiguration<? extends ViewAction> config, ActionScope scope) {
		ViewAction action = context.getInstance(config);
		if (action == null) {
			return null;
		}
		if (scope.isTransactional() && action.changesViewState()) {
			context.error("The action at " + config.location()
				+ " changes the state of the view and must not run inside the transaction of '"
				+ WithTransactionAction.Config.TAG_NAME + "': if the commit fails, the view keeps its"
				+ " effect. Move it into the '" + WithResultActions.WITH_RESULT + "' of the '"
				+ WithTransactionAction.Config.TAG_NAME + "'.");
			return null;
		}

		String valueName = null;
		if (config instanceof WithValueArgument valueConfig) {
			valueName = nonEmpty(valueConfig.getValue());
			if (valueName != null && !scope.contains(valueName)) {
				context.error("The action argument '" + valueName + "' selected with '" + WithValueArgument.VALUE
					+ "' is not defined at " + config.location() + ", available: " + scope.names());
				valueName = null;
			}
		}

		String resultName = WithResultActions.RESULT;
		List<ViewAction> followUps = List.of();
		if (config instanceof WithResultActions resultConfig) {
			WithResult withResult = resultConfig.getWithResult();
			if (withResult != null) {
				resultName = WithResult.resultName(withResult);
				followUps = instantiate(context, withResult.getActions(), scope.with(resultName));
			}
		}

		if (valueName == null && followUps.isEmpty()) {
			return action;
		}
		return new ActionStep(action, valueName, resultName, followUps);
	}

	private static String nonEmpty(String name) {
		return name == null || name.isEmpty() ? null : name;
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
