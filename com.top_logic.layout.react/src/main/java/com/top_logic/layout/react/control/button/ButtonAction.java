/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A command action in the view system.
 *
 * <p>
 * Replaces the old-world {@link com.top_logic.layout.basic.Command} interface for view-system
 * buttons. Receives a {@link ReactContext} instead of a
 * {@link com.top_logic.layout.DisplayContext}.
 * </p>
 */
@FunctionalInterface
public interface ButtonAction {

	/**
	 * Executes this action.
	 *
	 * @param context
	 *        The view display context.
	 * @return The result of the command execution.
	 */
	HandlerResult execute(ReactContext context);
}
