/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
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

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(new ToggleCommand());

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
		super(null, "TLToggleButton", COMMANDS);
		_action = action;
		_active = initialActive;
		getReactState().put("label", label);
		getReactState().put("active", Boolean.valueOf(initialActive));
	}

	/**
	 * Command that handles the click event from the React client.
	 */
	static class ToggleCommand extends ControlCommand {

		ToggleCommand() {
			super("click");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.REACT_TOGGLE_BUTTON_CLICK;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactToggleButtonControl toggle = (ReactToggleButtonControl) control;
			boolean newActive = toggle._action.toggle(context, toggle._active);
			toggle._active = newActive;
			toggle.patchReactState(Map.of("active", Boolean.valueOf(newActive)));
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
