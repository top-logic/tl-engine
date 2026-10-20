/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.tool.execution.ExecutableState;

/**
 * A step of an action chain: an action together with the argument it processes and the follow-up
 * actions it runs over its result.
 *
 * <p>
 * Created by {@link ViewActions} from the configuration options {@link WithValueArgument} and
 * {@link WithResultActions}, so that every action offering them behaves the same, without the
 * action knowing about them.
 * </p>
 */
public class ActionStep extends ArgumentsViewAction {

	private final ViewAction _action;

	private final String _valueName;

	private final String _resultName;

	private final List<ViewAction> _followUps;

	/**
	 * Creates an {@link ActionStep}.
	 *
	 * @param action
	 *        The action to run.
	 * @param valueName
	 *        The name of the argument the action processes, {@code null} for the default argument.
	 * @param resultName
	 *        The name of the action's result for the follow-up actions.
	 * @param followUps
	 *        The actions to run after the action, with its result in addition to the arguments of
	 *        the chain; empty to run the action alone.
	 */
	public ActionStep(ViewAction action, String valueName, String resultName, List<ViewAction> followUps) {
		_action = action;
		_valueName = valueName;
		_resultName = resultName;
		_followUps = List.copyOf(followUps);
	}

	/**
	 * The action this step runs.
	 */
	public ViewAction getAction() {
		return _action;
	}

	/**
	 * The actions run after {@link #getAction() the action} over its result.
	 */
	public List<ViewAction> getFollowUps() {
		return _followUps;
	}

	/**
	 * Runs the action and then its follow-up actions; resumes the chain with the result of the
	 * action.
	 */
	@Override
	public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
		ActionArguments input = _valueName == null ? arguments : arguments.withDefault(_valueName);
		if (_followUps.isEmpty()) {
			_action.execute(context, input, continuation);
		} else {
			ViewActionChain.followUp(context, _action, input, _followUps,
				result -> arguments.with(_resultName, result), continuation);
		}
	}

	@Override
	public boolean appliesFormState() {
		return _action.appliesFormState() || ViewActions.appliesFormState(_followUps);
	}

	/**
	 * The rule of the action, combined with the rules of the follow-up actions, which run whenever
	 * the action runs.
	 *
	 * <p>
	 * The rule of the action decides over the argument the action processes: with a selected
	 * {@link WithValueArgument#getValue() value}, that argument is the default the rule sees.
	 * </p>
	 */
	@Override
	public ViewExecutabilityRule getIntrinsicRule() {
		List<ViewExecutabilityRule> rules = new ArrayList<>(2);
		ViewExecutabilityRule own = _action.getIntrinsicRule();
		if (own != ViewExecutabilityRule.ALWAYS_EXECUTABLE) {
			rules.add(_valueName == null ? own : new SelectedArgumentRule(own, _valueName));
		}
		ViewExecutabilityRule followUps = ViewActions.intrinsicRule(_followUps);
		if (followUps != ViewExecutabilityRule.ALWAYS_EXECUTABLE) {
			rules.add(followUps);
		}
		return CombinedViewExecutabilityRule.combine(rules);
	}

	/**
	 * Destructive as soon as the action or one of its follow-up actions is.
	 */
	@Override
	public ButtonTone getTone() {
		return _action.getTone() == ButtonTone.DANGER ? ButtonTone.DANGER : ViewActions.tone(_followUps);
	}

	/**
	 * A rule deciding over the arguments with a selected argument as default.
	 */
	private static final class SelectedArgumentRule
			implements ViewExecutabilityRule, ContextDependentRule, ObservableRule {

		private final ViewExecutabilityRule _rule;

		private final String _name;

		SelectedArgumentRule(ViewExecutabilityRule rule, String name) {
			_rule = rule;
			_name = name;
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
}
