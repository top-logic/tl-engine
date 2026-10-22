/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Rule that determines whether a {@link ViewCommand} can be executed given the current input.
 *
 * <p>
 * A rule decides over the {@link ActionArguments arguments} of what it guards: the arguments of a
 * command (see {@link CommandArguments}), or the single named value an element supplies, such as
 * the row of a table whose drag a rule allows. A rule deciding over a single value implements
 * {@link #isExecutable(Object)} only and receives the {@link ActionArguments#value() default
 * argument}; a rule that sees all arguments - a script rule, say - implements
 * {@link #isExecutableFor(ActionArguments)} as well.
 * </p>
 *
 * <p>
 * Multiple rules can be combined using {@link CombinedViewExecutabilityRule}. The first
 * non-{@link ExecutableState#EXECUTABLE EXECUTABLE} result wins (short-circuit AND).
 * </p>
 */
public interface ViewExecutabilityRule {

	/**
	 * A rule that always returns {@link ExecutableState#EXECUTABLE}.
	 */
	ViewExecutabilityRule ALWAYS_EXECUTABLE = input -> ExecutableState.EXECUTABLE;

	/**
	 * Configuration for {@link ViewExecutabilityRule}.
	 */
	interface Config extends PolymorphicConfiguration<ViewExecutabilityRule> {
		// Marker interface.
	}

	/**
	 * Determines the executability of the command for the given input.
	 *
	 * @param input
	 *        The current input value (may be {@code null}).
	 * @return The executable state. Return {@link ExecutableState#EXECUTABLE} if the command should
	 *         be enabled.
	 */
	ExecutableState isExecutable(Object input);

	/**
	 * Determines the executability of the command for the given arguments.
	 *
	 * <p>
	 * The default decides over the {@link ActionArguments#value() default argument} with
	 * {@link #isExecutable(Object)}.
	 * </p>
	 *
	 * @param arguments
	 *        The arguments the guarded operation runs with, built for the
	 *        {@link ActionScope scope} the rule was instantiated in.
	 * @return The executable state. Return {@link ExecutableState#EXECUTABLE} if the command should
	 *         be enabled.
	 */
	default ExecutableState isExecutableFor(ActionArguments arguments) {
		return isExecutable(arguments.value());
	}
}
