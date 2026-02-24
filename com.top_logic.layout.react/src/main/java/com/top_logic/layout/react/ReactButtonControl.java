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
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ReactControl} that renders a button via the {@code TLButton} React component.
 *
 * <p>
 * When the button is clicked on the client, the {@code "click"} command is dispatched to the
 * server, which invokes the {@link Command} provided at construction time.
 * </p>
 */
public class ReactButtonControl extends ReactControl {

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(new ClickCommand());

	private final Command _action;

	/**
	 * Creates a new {@link ReactButtonControl}.
	 *
	 * @param label
	 *        The button label.
	 * @param action
	 *        The {@link Command} to execute when the button is clicked.
	 */
	public ReactButtonControl(String label, Command action) {
		super(null, "TLButton", COMMANDS);
		_action = action;
		getReactState().put("label", label);
	}

	/**
	 * Command that handles the click event from the React client.
	 */
	static class ClickCommand extends ControlCommand {

		ClickCommand() {
			super("click");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.REACT_BUTTON_CLICK;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			return ((ReactButtonControl) control)._action.executeCommand(context);
		}
	}

}
