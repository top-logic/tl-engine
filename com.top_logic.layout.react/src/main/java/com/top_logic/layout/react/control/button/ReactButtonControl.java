/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ReactControl} that renders a button via the {@code TLButton} React component.
 *
 * <p>
 * When the button is clicked on the client, the {@code "click"} command is dispatched to the
 * server, which invokes the {@link ButtonAction} provided at construction time.
 * </p>
 */
public class ReactButtonControl extends ReactControl {

	/** State key for the button label. */
	private static final String LABEL = "label";

	/** State key for the disabled flag. */
	private static final String DISABLED = "disabled";

	private final ButtonAction _action;

	/**
	 * Creates a new {@link ReactButtonControl}.
	 *
	 * @param label
	 *        The button label.
	 * @param action
	 *        The {@link ButtonAction} to execute when the button is clicked.
	 */
	public ReactButtonControl(String label, ButtonAction action) {
		super(null, "TLButton");
		_action = action;
		putState(LABEL, label);
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
	 * Sets the disabled state of the button.
	 *
	 * @param disabled
	 *        Whether the button should be disabled.
	 */
	public void setDisabled(boolean disabled) {
		putState(DISABLED, disabled);
	}

	/**
	 * Handles the click command from the React client.
	 */
	@ReactCommand("click")
	HandlerResult handleClick(ReactContext context) {
		return _action.execute(context);
	}

}
