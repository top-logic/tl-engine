/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers.toggle;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Algorithm specifying how to read the state of a {@link ToggleCommandByExpression} and how to
 * store a state upate.
 */
public interface StateHandler {

	/**
	 * Retrieves the current state of a toggle button.
	 *
	 * @param component
	 *        The context component.
	 * @param model
	 *        The command's model of the toggle button.
	 * @return The current state of the button (<code>true</code> for active, <code>false</code> for
	 *         inaktive).
	 */
	boolean getState(LayoutComponent component, Object model);

	/**
	 * Stores the new state of the toggle button.
	 *
	 * @param component
	 *        The context component.
	 * @param model
	 *        The command's model of the toggle button.
	 * @param state
	 *        The new state value (<code>true</code> for active, <code>false</code> for inactive).
	 */
	void setState(LayoutComponent component, Object model, boolean state);

}
