/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.toggle;

import com.top_logic.layout.react.ReactContext;

/**
 * Callback for toggling a boolean state.
 *
 * <p>
 * The callback receives the current active state and returns the new active state.
 * </p>
 */
@FunctionalInterface
public interface ToggleAction {

	/**
	 * Toggles the state.
	 *
	 * @param context
	 *        The current view display context.
	 * @param currentActive
	 *        The current active state.
	 * @return The new active state.
	 */
	boolean toggle(ReactContext context, boolean currentActive);

}
