/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Transient notification message displayed at the bottom of the screen.
 *
 * <p>
 * Auto-dismisses after a configurable duration. Supports four variants (info, success, warning,
 * error) and an optional action button.
 * </p>
 */
public class ReactSnackbarControl extends ReactControl {

	private static final String REACT_MODULE = "TLSnackbar";

	/** @see #show(String) */
	private static final String MESSAGE = "message";

	/** @see #ReactSnackbarControl(String, String, int, Runnable) */
	private static final String VARIANT = "variant";

	/** @see #ReactSnackbarControl(String, String, int, Runnable) */
	private static final String DURATION = "duration";

	/** @see #show() */
	private static final String VISIBLE = "visible";

	/** @see #setAction(String, String) */
	private static final String ACTION = "action";

	/** @see #setAction(String, String) */
	private static final String ACTION_LABEL = "label";

	/** @see #setAction(String, String) */
	private static final String ACTION_COMMAND_NAME = "commandName";

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new DismissCommand());

	private Runnable _dismissHandler;

	/**
	 * Creates a snackbar control.
	 *
	 * @param message
	 *        The notification message.
	 * @param variant
	 *        "info", "success", "warning", or "error".
	 * @param dismissHandler
	 *        Called when the snackbar is dismissed.
	 */
	public ReactSnackbarControl(String message, String variant, Runnable dismissHandler) {
		this(message, variant, 5000, dismissHandler);
	}

	/**
	 * Creates a snackbar control with custom duration.
	 *
	 * @param message
	 *        The notification message.
	 * @param variant
	 *        "info", "success", "warning", or "error".
	 * @param duration
	 *        Auto-dismiss delay in ms (0 = sticky).
	 * @param dismissHandler
	 *        Called when the snackbar is dismissed.
	 */
	public ReactSnackbarControl(String message, String variant, int duration, Runnable dismissHandler) {
		super(null, REACT_MODULE, COMMANDS);
		_dismissHandler = dismissHandler;
		putState(MESSAGE, message);
		putState(VARIANT, variant);
		putState(DURATION, duration);
		putState(VISIBLE, false);
	}

	/**
	 * Shows the snackbar.
	 */
	public void show() {
		putState(VISIBLE, true);
	}

	/**
	 * Shows the snackbar with a new message.
	 *
	 * @param message
	 *        The new notification message.
	 */
	public void show(String message) {
		putState(MESSAGE, message);
		putState(VISIBLE, true);
	}

	/**
	 * Hides the snackbar.
	 */
	public void hide() {
		putState(VISIBLE, false);
	}

	/**
	 * Sets an action button on the snackbar.
	 *
	 * @param label
	 *        The button label.
	 * @param commandName
	 *        The command to send when clicked.
	 */
	public void setAction(String label, String commandName) {
		Map<String, String> action = new HashMap<>();
		action.put(ACTION_LABEL, label);
		action.put(ACTION_COMMAND_NAME, commandName);
		putState(ACTION, action);
	}

	/**
	 * Command sent when the snackbar is dismissed (by timer or user).
	 */
	public static class DismissCommand extends ControlCommand {

		/** Creates a {@link DismissCommand}. */
		public DismissCommand() {
			super("dismiss");
		}

		@Override
		public com.top_logic.basic.util.ResKey getI18NKey() {
			return I18NConstants.REACT_SNACKBAR_DISMISS;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactSnackbarControl snackbar = (ReactSnackbarControl) control;
			snackbar.hide();
			snackbar._dismissHandler.run();
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
