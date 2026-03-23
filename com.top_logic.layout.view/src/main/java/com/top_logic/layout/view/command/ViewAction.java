/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.layout.react.ReactContext;

/**
 * Lightweight functional building block for composable view commands.
 *
 * <p>
 * Unlike {@link ViewCommand}, a {@link ViewAction} has no configuration for label, image,
 * executability, or other presentation concerns. It is purely functional: takes an input, produces
 * an output. Actions are chained in a {@link GenericViewCommand} where each action's return value
 * becomes the next action's input.
 * </p>
 */
public interface ViewAction {

	/**
	 * Executes this action.
	 *
	 * @param context
	 *        The React context.
	 * @param input
	 *        The input value, typically the output of the previous action in the chain, or
	 *        {@code null} for the first action.
	 * @return The result to pass to the next action, or the final result of the chain.
	 */
	Object execute(ReactContext context, Object input);
}
