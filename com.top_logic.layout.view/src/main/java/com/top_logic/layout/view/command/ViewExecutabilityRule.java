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
}
