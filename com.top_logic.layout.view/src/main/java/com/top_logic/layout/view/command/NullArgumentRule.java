/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} that rejects the guarded operation while an argument is
 * {@code null}.
 *
 * <p>
 * Without {@link Config#getInputs() inputs}, the rule decides over the
 * {@link ActionArguments#value() main argument}. With inputs, it decides over the arguments of
 * those names: the rule fails as soon as one of them is {@code null}.
 * </p>
 *
 * <p>
 * A subclass only supplies the {@link ExecutableState} the rule fails with.
 * </p>
 */
public abstract class NullArgumentRule implements ViewExecutabilityRule {

	/**
	 * Configuration of a {@link NullArgumentRule}.
	 */
	public interface Config extends ViewExecutabilityRule.Config {

		/**
		 * @see #getInputs()
		 */
		String INPUTS = "inputs";

		/**
		 * The names of the arguments that must not be {@code null}.
		 *
		 * <p>
		 * Each name is the name of an argument of the guarded operation, e.g. one of the inputs of
		 * a command. When no name is given, the main argument is checked.
		 * </p>
		 */
		@Name(INPUTS)
		@Format(CommaSeparatedStrings.class)
		List<String> getInputs();
	}

	private final ActionScope _scope;

	private final List<String> _names;

	private final ExecutableState _failure;

	/**
	 * Creates a {@link NullArgumentRule} checking the main argument.
	 *
	 * @param failure
	 *        The state the rule returns for a {@code null} argument.
	 */
	protected NullArgumentRule(ExecutableState failure) {
		_scope = ActionScope.NONE;
		_names = List.of();
		_failure = failure;
	}

	/**
	 * Creates a {@link NullArgumentRule} from configuration.
	 *
	 * <p>
	 * The configured {@link Config#getInputs() inputs} must be names of the {@link ActionScope} the
	 * rule is instantiated in; an unknown name is reported to the context.
	 * </p>
	 *
	 * @param context
	 *        The context the rule is instantiated in, carrying its {@link ActionScope}.
	 * @param config
	 *        The configuration of the rule.
	 * @param failure
	 *        The state the rule returns for a {@code null} argument.
	 */
	protected NullArgumentRule(InstantiationContext context, Config config, ExecutableState failure) {
		_scope = ActionScope.of(context);
		_names = List.copyOf(config.getInputs());
		_failure = failure;

		for (String name : _names) {
			if (!_scope.contains(name)) {
				context.error("The argument '" + name + "' checked by '" + Config.INPUTS
					+ "' is not defined at " + config.location() + ", available: " + _scope.names());
			}
		}
	}

	/**
	 * Decides over the given value as the default argument.
	 *
	 * <p>
	 * Without {@link Config#getInputs() inputs}, the rule fails for a {@code null} value. With
	 * inputs, every other argument of the scope is {@code null}, see
	 * {@link ActionScope#arguments(Object)}, and the rule decides with
	 * {@link #isExecutableFor(ActionArguments)}.
	 * </p>
	 */
	@Override
	public ExecutableState isExecutable(Object input) {
		if (_names.isEmpty()) {
			return input == null ? _failure : ExecutableState.EXECUTABLE;
		}
		return isExecutableFor(_scope.arguments(input));
	}

	@Override
	public ExecutableState isExecutableFor(ActionArguments arguments) {
		if (_names.isEmpty()) {
			return isExecutable(arguments.value());
		}
		for (String name : _names) {
			if (arguments.get(name) == null) {
				return _failure;
			}
		}
		return ExecutableState.EXECUTABLE;
	}
}
