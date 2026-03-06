/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.toggle;

import java.util.Map;

import com.top_logic.layout.react.ViewDisplayContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ReactControl} that renders a toggle button via the {@code TLToggleButton} React
 * component.
 *
 * <p>
 * The button displays an active/inactive visual state. When clicked, the {@code "click"} command is
 * dispatched to the server, which invokes the {@link ToggleAction} and updates the active state.
 * </p>
 */
public class ReactToggleButtonControl extends ReactControl {

	/** State key for the button label. */
	private static final String LABEL = "label";

	/** State key for the active/inactive toggle state. */
	private static final String ACTIVE = "active";

	private final ToggleAction _action;

	private boolean _active;

	/**
	 * Creates a new {@link ReactToggleButtonControl}.
	 *
	 * @param label
	 *        The button label.
	 * @param initialActive
	 *        The initial active state.
	 * @param action
	 *        The {@link ToggleAction} to execute when the button is clicked.
	 */
	public ReactToggleButtonControl(String label, boolean initialActive, ToggleAction action) {
		super(null, "TLToggleButton");
		_action = action;
		_active = initialActive;
		putState(LABEL, label);
		putState(ACTIVE, Boolean.valueOf(initialActive));
	}

	/**
	 * Updates the button label.
	 *
	 * @param label
	 *        The new label text.
	 */
	public void setLabel(String label) {
		putState(LABEL, label);
	}

	/**
	 * Programmatically sets the active state.
	 *
	 * @param active
	 *        The new active state.
	 */
	public void setActive(boolean active) {
		_active = active;
		putState(ACTIVE, Boolean.valueOf(active));
	}

	/**
	 * Handles the click command from the React client.
	 */
	@ReactCommand("click")
	HandlerResult handleClick(ViewDisplayContext context) {
		boolean newActive = _action.toggle(context, _active);
		_active = newActive;
		patchReactState(Map.of(ACTIVE, Boolean.valueOf(newActive)));
		return HandlerResult.DEFAULT_RESULT;
	}

}
